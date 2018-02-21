package nl.tudelft.cs4160.trustchain_android.Network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.SharedPreferences.InboxItemStorage;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.PubKeyAndAddressPairStorage;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.Util.ByteArrayConverter;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
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
import nl.tudelft.cs4160.trustchain_android.inbox.InboxItem;
import nl.tudelft.cs4160.trustchain_android.main.OverviewConnectionsActivity;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

import static nl.tudelft.cs4160.trustchain_android.message.MessageProto.Message.newBuilder;

/**
 * Created by michiel on 11-1-2018.
 */
public class Network {
    private final String TAG = this.getClass().getName();

    private static final int BUFFER_SIZE = 65536;
    private DatagramChannel channel;
    private String hashId;
    private int connectionType;
    private ByteBuffer outBuffer;
    private static InetSocketAddress internalSourceAddress;
    private String networkOperator;
    private static Network network;
    private String publicKey;
    private static NetworkCommunicationListener networkCommunicationListener;
    private static CrawlRequestListener crawlRequestListener;

    private Network() {
    }

    public static Network getInstance(Context context) {
        if (network == null) {
            network = new Network();
            network.initVariables(context);
        }
        return network;
    }

    public void setNetworkCommunicationListener(NetworkCommunicationListener networkCommunicationListener) {
        Network.networkCommunicationListener = networkCommunicationListener;
    }

    public void setCrawlRequestListener(CrawlRequestListener crawlRequestListener) {
        Network.crawlRequestListener = crawlRequestListener;
    }

    private void initVariables(Context context) {
        TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
        networkOperator = telephonyManager.getNetworkOperatorName();
        outBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        hashId = UserNameStorage.getUserName(context);
        publicKey = ByteArrayConverter.bytesToHexString(Key.loadKeys(context).getPublicKeyPair().toBytes());
        openChannel();
        showLocalIpAddress();
    }

    private void openChannel() {
        try {
            channel = DatagramChannel.open();
            channel.socket().bind(new InetSocketAddress(OverviewConnectionsActivity.DEFAULT_PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SocketAddress receive(ByteBuffer inputBuffer) throws IOException {
        if (!channel.isOpen()) {
            openChannel();
        }
        return channel.receive(inputBuffer);
    }

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

    public void sendBlockMessage(PeerAppToApp peer, MessageProto.TrustChainBlock block, boolean isNewBlock) throws IOException {
        MessageProto.Message message = newBuilder().setHalfBlock(block).build();
        BlockMessage request = new BlockMessage(hashId, peer.getAddress(), publicKey, message,isNewBlock);
        sendMessage(request, peer);
    }

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
                        break;
                    case Message.INTRODUCTION_RESPONSE_ID:
                        networkCommunicationListener.handleIntroductionResponse(peer, (IntroductionResponse) message);
                        break;
                    case Message.PUNCTURE_ID:
                        networkCommunicationListener.handlePuncture(peer, (Puncture) message);
                        break;
                    case Message.PUNCTURE_REQUEST_ID:
                        networkCommunicationListener.handlePunctureRequest(peer, (PunctureRequest) message);
                        break;
                    case Message.BLOCK_MESSAGE_ID:
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
                        networkCommunicationListener.handleCrawlRequest(peer, (CrawlRequest) message);
                        break;
                }
                networkCommunicationListener.updatePeerLists();
            }
        } catch (BencodeReadException | IOException | MessageException e) {
            e.printStackTrace();
        }
    }

    private static void addPeerToInbox(String pubKey,InetSocketAddress address, Context context, String peerId) {
        if (pubKey != null) {
            String ip = address.getAddress().toString().replace("/", "");
            InboxItem i = new InboxItem(peerId, new ArrayList<Integer>(), ip, pubKey, address.getPort());
            InboxItemStorage.addInboxItem(context, i);
        }
    }
    private static void addBlockToInbox(String pubKey,BlockMessage blockMessage, Context context) {
        if (pubKey != null) {
            try {
                InboxItemStorage.addHalfBlock(context, blockMessage.getPubKey(), blockMessage.getMessageProto().getHalfBlock().getSequenceNumber());
            } catch (MessageException e) {
                e.printStackTrace();
            }
        }
    }


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
}
