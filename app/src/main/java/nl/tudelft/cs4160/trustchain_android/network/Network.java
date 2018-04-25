package nl.tudelft.cs4160.trustchain_android.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.protobuf.ByteString;

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

import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;
import nl.tudelft.cs4160.trustchain_android.inbox.InboxItem;
import nl.tudelft.cs4160.trustchain_android.main.OverviewConnectionsActivity;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.network.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.peersummary.PeerSummaryActivity;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.InboxItemStorage;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.PubKeyAndAddressPairStorage;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.UserNameStorage;

public class Network {
    private final String TAG = this.getClass().getName();
    private static final int BUFFER_SIZE = 65536;
    private DatagramChannel channel;
    private String hashId;
    private int connectionType;
    private static InetSocketAddress internalSourceAddress;
    private String networkOperator;
    private static Network network;
    private PublicKeyPair publicKey;
    private static NetworkCommunicationListener networkCommunicationListener;
    private static PeerSummaryActivity mutualBlockListener;

    public final static int INTRODUCTION_REQUEST_ID = 1;
    public final static int INTRODUCTION_RESPONSE_ID = 2;
    public final static int PUNCTURE_REQUEST_ID = 3;
    public final static int PUNCTURE_ID = 4;
    public final static int BLOCK_MESSAGE_ID = 5;
    public final static int CRAWL_REQUEST_ID = 6;

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
    public synchronized static Network getInstance(Context context) {
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
     * @param mutualBlockListener
     */
    public void setMutualBlockListener(PeerSummaryActivity mutualBlockListener) {
        Network.mutualBlockListener = mutualBlockListener;
    }

    /**
     * Initialize the variables.
     * @param context is for retrieving from storage.
     */
    private void initVariables(Context context) {
        TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
        networkOperator = telephonyManager.getNetworkOperatorName();
        hashId = UserNameStorage.getUserName(context);
        publicKey = Key.loadKeys(context).getPublicKeyPair();
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
     * A introduction request is build and put into a message.
     * @param peer the destination.
     * @throws IOException
     */
    public void sendIntroductionRequest(Peer peer) throws IOException {
        MessageProto.IntroductionRequest request = MessageProto.IntroductionRequest.newBuilder()
                .setConnectionType(connectionType)
                .setNetworkOperator(networkOperator)
                .build();

        MessageProto.Message.Builder messageBuilder = MessageProto.Message.newBuilder()
                .setSourcePeerId(hashId)
                .setDestinationAddress(ByteString.copyFrom(peer.getAddress().getAddress().getAddress()))
                .setDestinationPort(peer.getAddress().getPort())
                .setPublicKey(ByteString.copyFrom(publicKey.toBytes()))
                .setType(INTRODUCTION_REQUEST_ID)
                .setPayload(MessageProto.Payload.newBuilder().setIntroductionRequest(request));

        sendMessage(messageBuilder.build(), peer);
    }

    /**
     * Send a block message via the network to a peer
     * @param peer the receiving peer
     * @param block the data
     * @throws IOException
     */
    public void sendBlockMessage(Peer peer, MessageProto.TrustChainBlock block) throws IOException {
        MessageProto.Message message = MessageProto.Message.newBuilder()
                .setSourcePeerId(hashId)
                .setDestinationAddress(ByteString.copyFrom(peer.getAddress().getAddress().getAddress()))
                .setDestinationPort(peer.getAddress().getPort())
                .setPublicKey(ByteString.copyFrom(publicKey.toBytes()))
                .setType(BLOCK_MESSAGE_ID)
                .setPayload(MessageProto.Payload.newBuilder().setBlock(block))
                .build();

        sendMessage(message, peer);
    }

    /**
     * Send a crawl request message via the network to a peer
     * @param peer the receiving peer
     * @param request the data
     * @throws IOException
     */
    public void sendCrawlRequest(Peer peer, MessageProto.CrawlRequest request) throws IOException {
        MessageProto.Message message = MessageProto.Message.newBuilder()
                .setSourcePeerId(hashId)
                .setDestinationAddress(ByteString.copyFrom(peer.getAddress().getAddress().getAddress()))
                .setDestinationPort(peer.getAddress().getPort())
                .setPublicKey(ByteString.copyFrom(publicKey.toBytes()))
                .setType(CRAWL_REQUEST_ID)
                .setPayload(MessageProto.Payload.newBuilder().setCrawlRequest(request))
                .build();

        sendMessage(message, peer);
    }

    /**
     * Send a puncture request.
     *
     * @param peer         the destination.
     * @param puncturePeer the inboxItem to puncture.
     * @throws IOException
     */
    public void sendPunctureRequest(Peer peer, Peer puncturePeer) throws IOException {
        MessageProto.PunctureRequest pRequest = MessageProto.PunctureRequest.newBuilder()
                .setSourceSocket(internalSourceAddress.toString())
                .setPuncturePeer(ByteString.copyFrom(Peer.serialize(puncturePeer)))
                .build();

        MessageProto.Message message = MessageProto.Message.newBuilder()
                .setSourcePeerId(hashId)
                .setDestinationAddress(ByteString.copyFrom(peer.getAddress().getAddress().getAddress()))
                .setDestinationPort(peer.getAddress().getPort())
                .setPublicKey(ByteString.copyFrom(publicKey.toBytes()))
                .setType(PUNCTURE_REQUEST_ID)
                .setPayload(MessageProto.Payload.newBuilder().setPunctureRequest(pRequest))
                .build();

        sendMessage(message, peer);
    }

    /**
     * Send a puncture.
     *
     * @param peer the destination.
     * @throws IOException
     */
    public void sendPuncture(Peer peer) throws IOException {
        MessageProto.Puncture puncture = MessageProto.Puncture.newBuilder()
                .setSourceSocket(internalSourceAddress.toString())
                .build();

        MessageProto.Message message = MessageProto.Message.newBuilder()
                .setSourcePeerId(hashId)
                .setDestinationAddress(ByteString.copyFrom(peer.getAddress().getAddress().getAddress()))
                .setDestinationPort(peer.getAddress().getPort())
                .setPublicKey(ByteString.copyFrom(publicKey.toBytes()))
                .setType(PUNCTURE_ID)
                .setPayload(MessageProto.Payload.newBuilder().setPuncture(puncture))
                .build();

        sendMessage(message, peer);
    }

    /**
     * Send an introduction response.
     *
     * @param peer    the destination.
     * @param invitee the invitee to which the destination inboxItem will send a puncture request.
     * @throws IOException
     */
    public void sendIntroductionResponse(Peer peer, Peer invitee) throws IOException {
        List<ByteString> pexPeers = new ArrayList<>();
        for (Peer p : networkCommunicationListener.getPeerHandler().getPeerList()) {
            if (p.hasReceivedData() && p.getPeerId() != null && p.isAlive())
                pexPeers.add(ByteString.copyFrom(Peer.serialize(p)));
        }

        MessageProto.IntroductionResponse response = MessageProto.IntroductionResponse.newBuilder()
                .setConnectionType(connectionType)
                .setNetworkOperator(networkOperator)
                .setInternalSourceSocket(internalSourceAddress.toString())
                .setInvitee(ByteString.copyFrom(Peer.serialize(invitee)))
                .addAllPex(pexPeers)
                .build();

        MessageProto.Message message = MessageProto.Message.newBuilder()
                .setSourcePeerId(hashId)
                .setDestinationAddress(ByteString.copyFrom(peer.getAddress().getAddress().getAddress()))
                .setDestinationPort(peer.getAddress().getPort())
                .setPublicKey(ByteString.copyFrom(publicKey.toBytes()))
                .setType(INTRODUCTION_RESPONSE_ID)
                .setPayload(MessageProto.Payload.newBuilder().setIntroductionResponse(response))
                .build();

        sendMessage(message, peer);
    }

    /**
     * Send a message to given inboxItem.
     *
     * @param message the message to send.
     * @param peer    the destination inboxItem.
     * @throws IOException
     */
    private synchronized void sendMessage(MessageProto.Message message, Peer peer) throws IOException {
        ByteBuffer outputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        channel.send(outputBuffer.wrap(message.toByteArray()), peer.getAddress());
        Log.i(TAG, "Sending " + message);
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
            MessageProto.Message message = MessageProto.Message.parseFrom(data);
            Log.i(TAG, "Received " + message.toString());
            String peerId = message.getSourcePeerId();

            if (networkCommunicationListener != null) {
                networkCommunicationListener.updateWan(message);

                Peer peer = networkCommunicationListener.getPeerHandler().getOrMakePeer(peerId, address);

                PublicKeyPair pubKeyPair = new PublicKeyPair(message.getPublicKey().toByteArray());
                String ip = address.getAddress().toString().replace("/", "") + ":" + address.getPort();
                PubKeyAndAddressPairStorage.addPubkeyAndAddressPair(context, pubKeyPair, ip);
                if (peer == null) return;
                peer.received(data);
                switch (message.getType()) {
                    case INTRODUCTION_REQUEST_ID:
                        networkCommunicationListener.handleIntroductionRequest(peer, message.getPayload().getIntroductionRequest());
                        break;
                    case INTRODUCTION_RESPONSE_ID:
                        networkCommunicationListener.handleIntroductionResponse(peer, message.getPayload().getIntroductionResponse());
                        break;
                    case PUNCTURE_ID:
                        networkCommunicationListener.handlePuncture(peer, message.getPayload().getPuncture());
                        break;
                    case PUNCTURE_REQUEST_ID:
                        networkCommunicationListener.handlePunctureRequest(peer, message.getPayload().getPunctureRequest());
                        break;
                    case BLOCK_MESSAGE_ID:
                        MessageProto.TrustChainBlock block = message.getPayload().getBlock();
                        addPeerToInbox(pubKeyPair, address, context, peerId);
                        addBlockToInbox(block, context);
                        networkCommunicationListener.handleReceivedBlock(peer, block);
                        if (mutualBlockListener != null) {
                            mutualBlockListener.blockAdded(block);
                        }
                        break;
                    case CRAWL_REQUEST_ID:
                        networkCommunicationListener.handleCrawlRequest(peer, message.getPayload().getCrawlRequest());
                        break;
                }
                networkCommunicationListener.updatePeerLists();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Add peer to inbox.
     * This means storing the InboxItem object in the local preferences.
     * @param pubKeyPair
     * @param address Socket address
     * @param context needed for storage
     * @param peerId
     */
    private static void addPeerToInbox(PublicKeyPair pubKeyPair,InetSocketAddress address, Context context, String peerId) {
        if (pubKeyPair != null) {
            String ip = address.getAddress().toString().replace("/", "");
            InboxItem i = new InboxItem(peerId, new ArrayList<Integer>(), ip, pubKeyPair, address.getPort());
            InboxItemStorage.addInboxItem(context, i);
        }
    }

    /**
     * Add a block reference to the InboxItem and store this again locally.
     * @param block the block of which the reference should be stored.
     * @param context needed for storage
     */
    private static void addBlockToInbox(MessageProto.TrustChainBlock block, Context context) {
        InboxItemStorage.addHalfBlock(context, new PublicKeyPair(block.getPublicKey().toByteArray())
                , block.getSequenceNumber());
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
                    networkCommunicationListener.updateInternalSourceAddress(internalSourceAddress.toString().replace("/",""));
                }
            }
        }
    }
}
