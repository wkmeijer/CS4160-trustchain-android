package nl.tudelft.cs4160.trustchain_android.Network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.offbynull.portmapper.PortMapperFactory;
import com.offbynull.portmapper.gateway.Bus;
import com.offbynull.portmapper.gateway.Gateway;
import com.offbynull.portmapper.gateways.network.NetworkGateway;
import com.offbynull.portmapper.gateways.process.ProcessGateway;
import com.offbynull.portmapper.mapper.MappedPort;
import com.offbynull.portmapper.mapper.PortMapper;
import com.offbynull.portmapper.mapper.PortType;
import com.offbynull.portmapper.mappers.pcp.PcpPortMapper;

import org.libsodium.jni.Sodium;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.SharedPreferences.InboxItemStorage;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.PubKeyAndAddressPairStorage;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.Util.ByteArrayConverter;
import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.BlockMessage;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.CrawlRequest;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.IntroductionRequest;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.IntroductionResponse;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Message;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.MessageException;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Puncture;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.PunctureRequest;
import nl.tudelft.cs4160.trustchain_android.bencode.BencodeReadException;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.inbox.InboxItem;
import nl.tudelft.cs4160.trustchain_android.main.OverviewConnectionsActivity;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

import static nl.tudelft.cs4160.trustchain_android.message.MessageProto.Message.newBuilder;

public class Network {
    private final String TAG = this.getClass().getName();
    private static final int BUFFER_SIZE = 65536;
    private DatagramChannel channel;
    private String hashId;
    private int connectionType;
    private ByteBuffer outBuffer;
    private static InetSocketAddress internalSourceAddress;
    private InetSocketAddress externalSourceAddress;
    private String networkOperator;
    private static Network network;
    private String publicKey;
    private static NetworkCommunicationListener networkCommunicationListener;
    private static CrawlRequestListener crawlRequestListener;

    /**
     * Emtpy constructor
     */
    private Network() {
    }

    /**
     * Get the network instance.
     * If the network isn't initizlized create a network and set the variables.
     * @param context
     * @return
     */
    public static Network getInstance(Context context) {
        if (network == null) {
            network = new Network();
            network.initVariables(context);
        }
        return network;
    }

    /**
     * Set the network communication listener.
     * @param networkCommunicationListener
     */
    public void setNetworkCommunicationListener(NetworkCommunicationListener networkCommunicationListener) {
        Network.networkCommunicationListener = networkCommunicationListener;
    }

    /**
     * Set the crawl request listener
     * @param crawlRequestListener
     */
    public void setCrawlRequestListener(CrawlRequestListener crawlRequestListener) {
        Network.crawlRequestListener = crawlRequestListener;
    }

    /**
     * Initialize the variables.
     * @param context is for retrieving from storage.
     */
    private void initVariables(Context context) {
        TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
        networkOperator = telephonyManager.getNetworkOperatorName();
        outBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        hashId = UserNameStorage.getUserName(context);
        publicKey = ByteArrayConverter.bytesToHexString(Key.loadKeys(context).getPublicKeyPair().toBytes());
        openChannel();
        showLocalIpAddress();
    }

    /**
     * Oopen the network channel on the default port.
     */
    private void openChannel() {
        try {
            channel = DatagramChannel.open();
            channel.socket().bind(new InetSocketAddress(OverviewConnectionsActivity.DEFAULT_PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * On receive data via the channel.
     * @param inputBuffer
     * @return
     * @throws IOException
     */
    public SocketAddress receive(ByteBuffer inputBuffer) throws IOException {
        if (!channel.isOpen()) {
            openChannel();
        }
        return channel.receive(inputBuffer);
    }

    /**
     * Close the channel
     */
    public void closeChannel() {
        channel.socket().close();
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Request and display the current connection type.
     */
    public void updateConnectionType(ConnectivityManager cm) {
        try {
            cm.getActiveNetworkInfo().getType();
        } catch (Exception e) {
            return;
        }

        connectionType = cm.getActiveNetworkInfo().getType();
        String typename = cm.getActiveNetworkInfo().getTypeName();
        String subtypeName = cm.getActiveNetworkInfo().getSubtypeName();

        if (networkCommunicationListener != null) {
            networkCommunicationListener.updateConnectionType(connectionType, typename, subtypeName);
        }
    }

    /**
     * Send an introduction request.
     *
     * @param peer the destination.
     * @throws IOException
     */
    public void sendIntroductionRequest(PeerAppToApp peer) throws IOException {
        IntroductionRequest request = new IntroductionRequest(hashId, peer.getAddress(), connectionType, networkOperator, publicKey);
        sendMessage(request, peer);
    }

    /**
     * Send a block message via the network to a peer
     * @param peer the receiving peer
     * @param block the data
     * @param isNewBlock determine if this is a new block or a old block as respond to a crawlrequest.
     * @throws IOException
     */
    public void sendBlockMessage(PeerAppToApp peer, MessageProto.TrustChainBlock block, boolean isNewBlock) throws IOException {
        MessageProto.Message message = newBuilder().setHalfBlock(block).build();
        BlockMessage request = new BlockMessage(hashId, peer.getAddress(), publicKey, message,isNewBlock);
        sendMessage(request, peer);
    }

    /**
     * Send a crawl request message via the network to a peer
     * @param peer the receiving peer
     * @param request the data
     * @throws IOException
     */
    public void sendCrawlRequest(PeerAppToApp peer, MessageProto.CrawlRequest request) throws IOException {
        CrawlRequest req = new CrawlRequest(hashId, peer.getAddress(), publicKey, request);
        sendMessage(req, peer);
    }

    /**
     * Send a puncture request.
     *
     * @param peer         the destination.
     * @param puncturePeer the inboxItem to puncture.
     * @throws IOException
     */
    public void sendPunctureRequest(PeerAppToApp peer, PeerAppToApp puncturePeer) throws IOException {
        PunctureRequest request = new PunctureRequest(hashId, peer.getAddress(), internalSourceAddress, puncturePeer, publicKey);
        sendMessage(request, peer);
    }

    /**
     * Send a puncture.
     *
     * @param peer the destination.
     * @throws IOException
     */
    public void sendPuncture(PeerAppToApp peer) throws IOException {
        Puncture puncture = new Puncture(hashId, peer.getAddress(), internalSourceAddress, publicKey);
        sendMessage(puncture, peer);
        sendPcp(peer);
    }

    /**
     * Sends a specially crafted PCP message according to the RFC 6887 standard to the carrier grade
     * NAT so it will allow incoming connections.
     * @param peer
     * @throws IOException
     */
    public void sendPcpNotWorkingAtm() throws IOException {
        Gateway network = NetworkGateway.create();
        Gateway process = ProcessGateway.create();
        Bus networkBus = network.getBus();
        Bus processBus = process.getBus();

        try {
            // Discover port forwarding devices and take the first one found
            List<PcpPortMapper> pcpMappers = PcpPortMapper.identify(networkBus, processBus, externalSourceAddress.getAddress());
            PortMapper mapper = pcpMappers.get(0);

            // Map internal port 12345 to some external port (55555 preferred)
            //
            // IMPORTANT NOTE: Many devices prevent you from mapping ports that are <= 1024
            // (both internal and external ports). Be mindful of this when choosing which
            // ports you want to map.
            MappedPort mappedPort = mapper.mapPort(PortType.UDP, OverviewConnectionsActivity.DEFAULT_PORT, externalSourceAddress.getPort(), 600);
            Log.i(TAG, "Port mapping added: " + mappedPort);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendPcp() throws IOException {

        // create a pcp request header
        byte[] pcpHeader = new byte[24];

        // version = 2 (8 bits)
        pcpHeader[0] = (byte) 0x02;

        // R = 0 (request) (1 bit)
        // Opcode = 1 (MAP)(7 bit)
        pcpHeader[1] = (byte) 0x01;

        // Reserved must be all zero (16 bit)
        pcpHeader[2] = (byte) 0x00;
        pcpHeader[3] = (byte) 0x00;

        // Requested Lifetime 120 seconds (32 bit)
        byte[] lifetime = ByteBuffer.allocate(4).putInt(120).array();
        for(int i = 0; i<4; i++) {
            pcpHeader[i+4] = lifetime[i];
        }

        // pcp client ip address, must be same source address as in ip header (128 bits)
        // for ipv4 first 80 bits are zero, next 16 bits are 1 and last 32 bits are ipv4 address
        if(internalSourceAddress.getAddress().getClass().equals(Inet4Address.class)) {
            for(int i = 8; i<18; i++) {
                pcpHeader[i] = (byte) 0x00;
            }
            pcpHeader[18] = (byte) 0xFF;
            pcpHeader[19] = (byte) 0xFF;
            byte[] intAddressBytes = internalSourceAddress.getAddress().getAddress();
            for(int i = 0; i<4; i++) {
                pcpHeader[i+20] = intAddressBytes[i];
            }

        } else {
            // for ipv6 simply the bit representation
            byte[] intAddressBytes = internalSourceAddress.getAddress().getAddress();
            for(int i = 0; i<16; i++) {
                pcpHeader[i+8] = intAddressBytes[i];
            }
        }

        // create a map opcode request
        byte[] mapRequest = new byte[36];

        // mapping nonce (96 bit)
        // TODO: create proper random nonce
        for(int i = 0; i<12; i=i) {
            byte[] rand = ByteBuffer.allocate(4).putInt(Sodium.randombytes_random()).array();
            for(int j = 0; j<4; j++) {
                mapRequest[i] = rand[j];
                i++;
            }
        }

        // protocol = 17 (UDP) (8 bit)
        mapRequest[12] = (byte) 0x11;

        // reserved all 0 (24 bit)
        mapRequest[13] = (byte) 0x00;
        mapRequest[14] = (byte) 0x00;
        mapRequest[15] = (byte) 0x00;

        // Internal Port (16 bit)
        byte[] internalPort = ByteBuffer.allocate(2).putShort((short) OverviewConnectionsActivity.DEFAULT_PORT).array();
        mapRequest[16] = internalPort[0];
        mapRequest[17] = internalPort[1];

        // Suggested External port (16 bit)
//        mapRequest[18] = internalPort[0];
//        mapRequest[19] = internalPort[1];

        byte[] externalPort = ByteBuffer.allocate(2).putShort((short) externalSourceAddress.getPort()).array();
        mapRequest[18] = externalPort[0];
        mapRequest[19] = externalPort[1];

        // Suggested external ip address (128 bits)
        InetAddress externalAddress = externalSourceAddress.getAddress();
        // for ipv4 first 80 bits are zero, next 16 bits are 1 and last 32 bits are ipv4 address
        if(externalAddress.getClass().equals(Inet4Address.class)) {
            for(int i = 20; i<30; i++) {
                mapRequest[i] = (byte) 0x00;
            }
            mapRequest[30] = (byte) 0xFF;
            mapRequest[31] = (byte) 0xFF;
            byte[] extAddressBytes = externalAddress.getAddress();
            for(int i = 0; i<4; i++) {
                mapRequest[i+32] = extAddressBytes[i];
            }

        } else {
            // for ipv6 simply the bit representation
            byte[] extAddressBytes = externalAddress.getAddress();
            for(int i = 0; i<16; i++) {
                mapRequest[i+20] = extAddressBytes[i];
            }
        }



        byte[] pcpRequest = new byte[pcpHeader.length + mapRequest.length];
        System.arraycopy(pcpHeader,0,pcpRequest,0,pcpHeader.length);
        System.arraycopy(mapRequest,0,pcpRequest,pcpHeader.length,mapRequest.length);

        ByteBuffer byteBuffer = ByteBuffer.wrap(pcpRequest);

        InetSocketAddress dest = new InetSocketAddress("130.161.211.254",1873);
        // 5351 is apparently the port the server listens to
        channel.send(byteBuffer,new InetSocketAddress(externalAddress.getHostAddress(),5351));
        Log.i(TAG, "Sent pcp packet");
    }

    public void sendPcp(PeerAppToApp peer) throws IOException {

        // create a pcp request header
        byte[] pcpHeader = new byte[24];

        // version = 2 (8 bits)
        pcpHeader[0] = (byte) 0x02;

        // R = 0 (request) (1 bit)
        // Opcode = 2 (PEER)(7 bit)
        pcpHeader[1] = (byte) 0x02;

        // Reserved must be all zero (16 bit)
        pcpHeader[2] = (byte) 0x00;
        pcpHeader[3] = (byte) 0x00;

        // Requested Lifetime 120 seconds (32 bit)
        byte[] lifetime = ByteBuffer.allocate(4).putInt(120).array();
        for (int i = 0; i < 4; i++) {
            pcpHeader[i + 4] = lifetime[i];
        }

        // pcp client ip address, must be same source address as in ip header (128 bits)
        // for ipv4 first 80 bits are zero, next 16 bits are 1 and last 32 bits are ipv4 address
        if (internalSourceAddress.getAddress().getClass().equals(Inet4Address.class)) {
            for (int i = 8; i < 18; i++) {
                pcpHeader[i] = (byte) 0x00;
            }
            pcpHeader[18] = (byte) 0xFF;
            pcpHeader[19] = (byte) 0xFF;
            byte[] intAddressBytes = internalSourceAddress.getAddress().getAddress();
            for (int i = 0; i < 4; i++) {
                pcpHeader[i + 20] = intAddressBytes[i];
            }

        } else {
            // for ipv6 simply the bit representation
            byte[] intAddressBytes = internalSourceAddress.getAddress().getAddress();
            for (int i = 0; i < 16; i++) {
                pcpHeader[i + 8] = intAddressBytes[i];
            }
        }

        // create a map opcode request
        byte[] peerRequest = new byte[56];

        // mapping nonce (96 bit)
        // TODO: create proper random nonce
        for (int i = 0; i < 12; i = i) {
            byte[] rand = ByteBuffer.allocate(4).putInt(Sodium.randombytes_random()).array();
            for (int j = 0; j < 4; j++) {
                peerRequest[i] = rand[j];
                i++;
            }
        }

        // protocol = 17 (UDP) (8 bit)
        peerRequest[12] = (byte) 0x11;

        // reserved all 0 (24 bit)
        peerRequest[13] = (byte) 0x00;
        peerRequest[14] = (byte) 0x00;
        peerRequest[15] = (byte) 0x00;

        // Internal Port (16 bit)
        byte[] internalPort = ByteBuffer.allocate(2).putShort((short) OverviewConnectionsActivity.DEFAULT_PORT).array();
        peerRequest[16] = internalPort[0];
        peerRequest[17] = internalPort[1];

        // Suggested External port (16 bit)
//        mapRequest[18] = internalPort[0];
//        mapRequest[19] = internalPort[1];

        byte[] externalPort = ByteBuffer.allocate(2).putShort((short) externalSourceAddress.getPort()).array();
        peerRequest[18] = externalPort[0];
        peerRequest[19] = externalPort[1];

        // Suggested external ip address (128 bits)
        InetAddress externalAddress = externalSourceAddress.getAddress();
        // for ipv4 first 80 bits are zero, next 16 bits are 1 and last 32 bits are ipv4 address
        if (externalAddress.getClass().equals(Inet4Address.class)) {
            for (int i = 20; i < 30; i++) {
                peerRequest[i] = (byte) 0x00;
            }
            peerRequest[30] = (byte) 0xFF;
            peerRequest[31] = (byte) 0xFF;
            byte[] extAddressBytes = externalAddress.getAddress();
            for (int i = 0; i < 4; i++) {
                peerRequest[i + 32] = extAddressBytes[i];
            }

        } else {
            // for ipv6 simply the bit representation
            byte[] extAddressBytes = externalAddress.getAddress();
            for (int i = 0; i < 16; i++) {
                peerRequest[i + 20] = extAddressBytes[i];
            }
        }

        // Port of the remote peer (16 bit)
        byte[] remotePort = ByteBuffer.allocate(2).putShort((short) peer.getAddress().getPort()).array();
        peerRequest[36] = remotePort[0];
        peerRequest[37] = remotePort[1];

        // reserved must be 0 (16 bit)
        peerRequest[38] = (byte) 0x00;
        peerRequest[39] = (byte) 0x00;

        // remote Peer IP address (128 bit)
        InetAddress remoteAddress = peer.getAddress().getAddress();
        // for ipv4 first 80 bits are zero, next 16 bits are 1 and last 32 bits are ipv4 address
        if (remoteAddress.getClass().equals(Inet4Address.class)) {
            for (int i = 40; i < 50; i++) {
                peerRequest[i] = (byte) 0x00;
            }
            peerRequest[50] = (byte) 0xFF;
            peerRequest[51] = (byte) 0xFF;
            byte[] remoteAddressBytes = remoteAddress.getAddress();
            for (int i = 0; i < 4; i++) {
                peerRequest[i + 52] = remoteAddressBytes[i];
            }

        } else {
            // for ipv6 simply the bit representation
            byte[] remoteAddressBytes = remoteAddress.getAddress();
            for (int i = 0; i < 16; i++) {
                peerRequest[i + 40] = remoteAddressBytes[i];
            }
        }

        byte[] pcpRequest = new byte[pcpHeader.length + peerRequest.length];
        System.arraycopy(pcpHeader, 0, pcpRequest, 0, pcpHeader.length);
        System.arraycopy(peerRequest, 0, pcpRequest, pcpHeader.length, peerRequest.length);

        ByteBuffer byteBuffer = ByteBuffer.wrap(pcpRequest);

        InetSocketAddress dest = new InetSocketAddress("130.161.211.254", 1873);
        // 5351 is apparently the port the server listens to
        channel.send(byteBuffer, new InetSocketAddress(externalAddress.getHostAddress(), 5351));
        Log.i(TAG, "Sent pcp peer request");
    }

    /**
     * Send an introduction response.
     *
     * @param peer    the destination.
     * @param invitee the invitee to which the destination inboxItem will send a puncture request.
     * @throws IOException
     */
    public void sendIntroductionResponse(PeerAppToApp peer, PeerAppToApp invitee) throws IOException {
        List<PeerAppToApp> pexPeers = new ArrayList<>();
        for (PeerAppToApp p : networkCommunicationListener.getPeerHandler().getPeerList()) {
            if (p.hasReceivedData() && p.getPeerId() != null && p.isAlive())
                pexPeers.add(p);
        }

        IntroductionResponse response = new IntroductionResponse(hashId, internalSourceAddress, peer
                .getAddress(), invitee, connectionType, pexPeers, networkOperator, publicKey);
        sendMessage(response, peer);
    }

    /**
     * Send a message to given inboxItem.
     *
     * @param message the message to send.
     * @param peer    the destination inboxItem.
     * @throws IOException
     */
    private synchronized void sendMessage(Message message, PeerAppToApp peer) throws IOException {
        message.putPubKey(publicKey);
        Log.d(TAG, "Sending " + message);
        outBuffer.clear();
        message.writeToByteBuffer(outBuffer);
        outBuffer.flip();
        channel.send(outBuffer, peer.getAddress());
        peer.sentData();
        if (networkCommunicationListener != null) {
            networkCommunicationListener.updatePeerLists();
        }
    }

    /**
     * Show local ip address.
     */
    private void showLocalIpAddress() {
        ShowLocalIPTask showLocalIPTask = new ShowLocalIPTask();
        showLocalIPTask.execute();
    }

    /**
     * Handle incoming data.
     *
     * @param data    the data {@link ByteBuffer}.
     * @param address the incoming address.
     */
    public void dataReceived(Context context, ByteBuffer data, InetSocketAddress address) {
        // If we don't have an internal address, try to find it again instead of handling the message.
        if (internalSourceAddress == null) {
            showLocalIpAddress();
            return;
        }

        try {
            Message message = Message.createFromByteBuffer(data);
            Log.d(TAG, "Received " + message);

            String peerId = message.getPeerId();

            if (networkCommunicationListener != null) {
                networkCommunicationListener.updateWan(message);

                PeerAppToApp peer = networkCommunicationListener.getOrMakePeer(peerId, address, PeerAppToApp.INCOMING);

                String pubKey = message.getPubKey();
                String ip = address.getAddress().toString().replace("/", "") + ":" + address.getPort();
                PubKeyAndAddressPairStorage.addPubkeyAndAddressPair(context, pubKey, ip);
                if (peer == null) return;
                peer.received(data);
                switch (message.getType()) {
                    case Message.INTRODUCTION_REQUEST_ID:
                        networkCommunicationListener.handleIntroductionRequest(peer, (IntroductionRequest) message);
                        Log.i(TAG,"Introduction request received");
                        break;
                    case Message.INTRODUCTION_RESPONSE_ID:
                        networkCommunicationListener.handleIntroductionResponse(peer, (IntroductionResponse) message);
                        Log.i(TAG,"Introduction response received");
                        break;
                    case Message.PUNCTURE_ID:
                        networkCommunicationListener.handlePuncture(peer, (Puncture) message);
                        Log.i(TAG,"Puncture received");
                        break;
                    case Message.PUNCTURE_REQUEST_ID:
                        networkCommunicationListener.handlePunctureRequest(peer, (PunctureRequest) message);
                        Log.i(TAG,"Puncture request received");
                        break;
                    case Message.BLOCK_MESSAGE_ID:
                        Log.i(TAG,"Block request received");
                        BlockMessage blockMessage = (BlockMessage) message;
                        addPeerToInbox(pubKey, address, context, peerId);
                        if (blockMessage.isNewBlock()) {
                            addBlockToInbox(pubKey,blockMessage,context);
                            networkCommunicationListener.handleBlockMessageRequest(peer, blockMessage);
                            if(crawlRequestListener != null) {
                                crawlRequestListener.blockAdded(blockMessage);
                            }
                        }else{
                            if(crawlRequestListener != null) {
                                crawlRequestListener.handleCrawlRequestBlockMessageRequest(peer, blockMessage);
                            }
                        }
                        break;
                    case Message.CRAWL_REQUEST_ID:
                        Log.i(TAG,"Crawl request received");
                        networkCommunicationListener.handleCrawlRequest(peer, (CrawlRequest) message);
                        break;
                }
                networkCommunicationListener.updatePeerLists();
            }
        } catch (BencodeReadException | IOException | MessageException e) {
            e.printStackTrace();
            Log.e(TAG,"Received Packet: " + ByteArrayConverter.bytesToHexString(data.array()));
        }
    }

    /**
     * Add peer to inbox.
     * This means storing the InboxItem object in the local preferences.
     * @param pubKey
     * @param address Socket address
     * @param context needed for storage
     * @param peerId
     */
    private static void addPeerToInbox(String pubKey,InetSocketAddress address, Context context, String peerId) {
        if (pubKey != null) {
            String ip = address.getAddress().toString().replace("/", "");
            InboxItem i = new InboxItem(peerId, new ArrayList<Integer>(), ip, pubKey, address.getPort());
            InboxItemStorage.addInboxItem(context, i);
        }
    }

    /**
     * Add a block reference to the InboxItem and store this again locally.
     * @param pubKey
     * @param blockMessage the block of which the reference should be stored.
     * @param context needed for storage
     */
    private static void addBlockToInbox(String pubKey,BlockMessage blockMessage, Context context) {
        if (pubKey != null) {
            try {
                InboxItemStorage.addHalfBlock(context, blockMessage.getPubKey(), blockMessage.getMessageProto().getHalfBlock().getSequenceNumber());
            } catch (MessageException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Show local ip visually to the user.
     */
    private static class ShowLocalIPTask extends AsyncTask<Void, Void, InetAddress> {
        @Override
        protected InetAddress doInBackground(Void... params) {
            try {
                for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface intf = (NetworkInterface) en.nextElement();
                    for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                            return inetAddress;
                        }
                    }
                }
            } catch (SocketException ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(InetAddress inetAddress) {
            super.onPostExecute(inetAddress);
            if (inetAddress != null) {
                internalSourceAddress = new InetSocketAddress(inetAddress, OverviewConnectionsActivity.DEFAULT_PORT);
                if (networkCommunicationListener != null) {
                    networkCommunicationListener.updateInternalSourceAddress(internalSourceAddress.toString());
                }
            }
        }
    }

    public void setExternalSourceAddress(InetSocketAddress externalSourceAddress) {
        this.externalSourceAddress = externalSourceAddress;
    }
}
