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
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.SharedPreferences.InboxItemStorage;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.PubKeyAndAddressPairStorage;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.SharedPreferencesStorage;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.Util.ByteArrayConverter;
import nl.tudelft.cs4160.trustchain_android.Util.Key;
import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;
import nl.tudelft.cs4160.trustchain_android.appToApp.PeerHandler;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.PeerListener;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.BlockMessage;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.IntroductionRequest;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.IntroductionResponse;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Message;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.MessageException;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Puncture;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.PunctureRequest;
import nl.tudelft.cs4160.trustchain_android.bencode.BencodeReadException;
import nl.tudelft.cs4160.trustchain_android.connection.CommunicationSingleton;
import nl.tudelft.cs4160.trustchain_android.database.TrustChainDBHelper;
import nl.tudelft.cs4160.trustchain_android.inbox.InboxItem;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlock.createBlock;
import static nl.tudelft.cs4160.trustchain_android.message.MessageProto.Message.newBuilder;

/**
 * Created by michiel on 11-1-2018.
 */
public class Network {
    private static final int BUFFER_SIZE = 65536;
    public final static int DEFAULT_PORT = 1873;

    private DatagramChannel channel;

    private PeerHandler peerHandler;
    private String hashId;

    private boolean willExit = false;

    private int connectionType;
    private ByteBuffer outBuffer;
    private static InetSocketAddress internalSourceAddress;
    private String networkOperator;

    private static Network network;
    private String publicKey;
    private TrustChainDBHelper dbHelper;
    private static NetworkCommunicationListener networkCommunicationListener;

    private Network() {}

    public static Network getInstance(Context context, DatagramChannel channel) {
        if (network == null) {
            network = new Network();
            network.initVariables(context, channel);
        }
        return network;
    }

    public void setNetworkCommunicationListener(NetworkCommunicationListener networkCommunicationListener) {
        Network.networkCommunicationListener = networkCommunicationListener;
    }

    public void setPeerListener(PeerListener peerListener) {
        this.peerHandler.setPeerListener(peerListener);
    }

    public PeerHandler getPeerHandler() {
        return peerHandler;
    }

    private void initVariables(Context context, DatagramChannel channel) {
        TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
        networkOperator = telephonyManager.getNetworkOperatorName();
        dbHelper = new TrustChainDBHelper(context);
        peerHandler = new PeerHandler(UserNameStorage.getUserName(context));
        outBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        hashId = UserNameStorage.getUserName(context);
        publicKey = ByteArrayConverter.bytesToHexString(Key.loadKeys(context).getPublic().getEncoded());
        this.channel = channel;
        showLocalIpAddress();
    }

    public void setPeersFromSavedInstance(ArrayList<PeerAppToApp> peers) {
        this.peerHandler.setPeerList(peers);
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

    public void sendBlockMessage(PeerAppToApp peer) throws IOException {
        MessageProto.TrustChainBlock block = createBlock(new byte[0], dbHelper, publicKey.getBytes(), null, publicKey.getBytes());
        MessageProto.Message message = newBuilder().setHalfBlock(block).build();
        BlockMessage request = new BlockMessage(hashId, peer.getAddress(), publicKey, message);
        sendMessage(request, peer);
    }

    /**
     * Send a puncture request.
     *
     * @param peer         the destination.
     * @param puncturePeer the inboxItem to puncture.
     * @throws IOException
     */
    private void sendPunctureRequest(PeerAppToApp peer, PeerAppToApp puncturePeer) throws IOException {
        PunctureRequest request = new PunctureRequest(hashId, peer.getAddress(), internalSourceAddress, puncturePeer, publicKey);
        sendMessage(request, peer);
    }

    /**
     * Send a puncture.
     *
     * @param peer the destination.
     * @throws IOException
     */
    private void sendPuncture(PeerAppToApp peer) throws IOException {
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
    private void sendIntroductionResponse(PeerAppToApp peer, PeerAppToApp invitee) throws IOException {
        List<PeerAppToApp> pexPeers = new ArrayList<>();
        for (PeerAppToApp p : peerHandler.getPeerList()) {
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

        Log.d("Network", "Sending " + message);
        outBuffer.clear();
        message.writeToByteBuffer(outBuffer);
        outBuffer.flip();
        channel.send(outBuffer, peer.getAddress());
        peer.sentData();
        if(networkCommunicationListener != null) {
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
        try {
            Message message = Message.createFromByteBuffer(data);
            Log.d("Network", "Received " + message);

            String id = message.getPeerId();
            String pubKey = message.getPubKey();

            String ip = address.getAddress().toString().replace("/", "");
            PubKeyAndAddressPairStorage.addPubkeyAndAddressPair(context, pubKey, ip);
            InboxItem i = new InboxItem(id, new ArrayList<Integer>(), ip, pubKey, address.getPort());
            InboxItemStorage.addInboxItem(context, i);

            Log.d("Network", "Stored following ip for pubkey: " + pubKey + " " + PubKeyAndAddressPairStorage.getAddressByPubkey(context, pubKey));

            Log.d("Network", "pubkey address map " + SharedPreferencesStorage.getAll(context).toString());

            if(networkCommunicationListener != null) {
                networkCommunicationListener.updateWan(message);

                PeerAppToApp peer = networkCommunicationListener.getOrMakePeer(id, address, PeerAppToApp.INCOMING);

                if (peer == null) return;
                peer.received(data);
                switch (message.getType()) {
                    case Message.INTRODUCTION_REQUEST:
                        handleIntroductionRequest(peer, (IntroductionRequest) message);
                        break;
                    case Message.INTRODUCTION_RESPONSE:
                        handleIntroductionResponse(peer, (IntroductionResponse) message);
                        break;
                    case Message.PUNCTURE:
                        handlePuncture(peer, (Puncture) message);
                        break;
                    case Message.PUNCTURE_REQUEST:
                        handlePunctureRequest(peer, (PunctureRequest) message);
                        break;
                    case Message.BLOCK_MESSAGE:
                        handleBlockMessageRequest(peer, (BlockMessage) message);

                }
                networkCommunicationListener.updatePeerLists();
            }
        } catch (BencodeReadException | IOException | MessageException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle an introduction request. Send a puncture request to the included invitee.
     *
     * @param peer    the origin inboxItem.
     * @param message the message.
     * @throws IOException
     */
    private void handleIntroductionRequest(PeerAppToApp peer, IntroductionRequest message) throws IOException {
        peer.setNetworkOperator(message.getNetworkOperator());
        peer.setConnectionType((int) message.getConnectionType());
        if (getPeerHandler().size() > 1) {
            PeerAppToApp invitee = getPeerHandler().getEligiblePeer(peer);
            if (invitee != null) {
                sendIntroductionResponse(peer, invitee);
                sendPunctureRequest(invitee, peer);
                Log.d("Network", "Introducing " + invitee.getAddress() + " to " + peer.getAddress());
            }
        } else {
            Log.d("Network", "Peerlist too small, can't handle introduction request");
            sendIntroductionResponse(peer, null);
        }
    }

    /**
     * Handle an introduction response. Parse incoming PEX peers.
     *
     * @param peer    the origin inboxItem.
     * @param message the message.
     */
    private void handleIntroductionResponse(PeerAppToApp peer, IntroductionResponse message) {
        peer.setConnectionType((int) message.getConnectionType());
        peer.setNetworkOperator(message.getNetworkOperator());
        List<PeerAppToApp> pex = message.getPex();
        for (PeerAppToApp pexPeer : pex) {
            if (getPeerHandler().hashId.equals(pexPeer.getPeerId())) continue;
            getPeerHandler().getOrMakePeer(pexPeer.getPeerId(), pexPeer.getAddress(), PeerAppToApp.OUTGOING);
        }
    }

    /**
     * Handle a puncture. Does nothing because the only purpose of a puncture is to punch a hole in the NAT.
     *
     * @param peer    the origin inboxItem.
     * @param message the message.
     * @throws IOException
     */
    private void handlePuncture(PeerAppToApp peer, Puncture message) throws IOException {}

    /**
     * Handle a puncture request. Sends a puncture to the puncture inboxItem included in the message.
     *
     * @param peer    the origin inboxItem.
     * @param message the message.
     * @throws IOException
     * @throws MessageException
     */
    private void handlePunctureRequest(PeerAppToApp peer, PunctureRequest message) throws IOException, MessageException {
        if (!getPeerHandler().peerExistsInList(message.getPuncturePeer())) {
            sendPuncture(message.getPuncturePeer());
        }
    }

    private void handleBlockMessageRequest(PeerAppToApp peer, BlockMessage message) throws IOException, MessageException {
        MessageProto.Message msg = message.getMessageProto();
        if(msg.getCrawlRequest().getPublicKey().size() == 0){
            MessageProto.TrustChainBlock block = msg.getHalfBlock();
            InboxItemStorage.addHalfBlock(CommunicationSingleton.getContext(), block.getPublicKey().toString(), block.getLinkSequenceNumber());
            CommunicationSingleton.getDbHelper().insertInDB(block);
            Log.d("testTheStacks", block.toString());
        }
    }

    static class ShowLocalIPTask extends AsyncTask<Void, Void, InetAddress> {
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
                internalSourceAddress = new InetSocketAddress(inetAddress, DEFAULT_PORT);
            }
            if(networkCommunicationListener != null) {
                networkCommunicationListener.updateInternalSourceAddress(internalSourceAddress.toString());
            }
        }
    }
}
