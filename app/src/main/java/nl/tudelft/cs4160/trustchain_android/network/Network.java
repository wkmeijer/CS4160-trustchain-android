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
import nl.tudelft.cs4160.trustchain_android.message.MessageProto.Message;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto.TrustChainBlock;
import nl.tudelft.cs4160.trustchain_android.network.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.network.peer.PeerHandler;
import nl.tudelft.cs4160.trustchain_android.peersummary.PeerSummaryActivity;
import nl.tudelft.cs4160.trustchain_android.storage.database.TrustChainDBHelper;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.InboxItemStorage;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.PubKeyAndAddressPairStorage;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.UserNameStorage;

public class Network {
    private final String TAG = this.getClass().getName();
    private static final int BUFFER_SIZE = 65536;
    private DatagramChannel channel;
    private String name;
    private int connectionType;
    private static InetSocketAddress internalSourceAddress;
    private String networkOperator;
    private static Network network;
    private PublicKeyPair publicKey;
    private MessageHandler messageHandler;
    private static NetworkStatusListener networkStatusListener;
    private static PeerSummaryActivity mutualBlockListener;

    public final static int INTRODUCTION_REQUEST_ID = 1;
    public final static int INTRODUCTION_RESPONSE_ID = 2;
    public final static int PUNCTURE_REQUEST_ID = 3;
    public final static int PUNCTURE_ID = 4;
    public final static int BLOCK_MESSAGE_ID = 5;
    public final static int CRAWL_REQUEST_ID = 6;

    /**
     * Empty constructor
     */
    private Network() {
    }

    /**
     * Get the network instance.
     * If the network isn't initialized create a network and set the variables.
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
     * Initialize the variables.
     * @param context is for retrieving from storage.
     */
    private void initVariables(Context context) {
        TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
        networkOperator = telephonyManager.getNetworkOperatorName();
        publicKey = Key.loadKeys(context).getPublicKeyPair();
        messageHandler = new MessageHandler(network, new TrustChainDBHelper(context),
                new PeerHandler(publicKey,UserNameStorage.getUserName(context)));
        name = UserNameStorage.getUserName(context);
        openChannel();
        showLocalIpAddress();
    }

    /**
     * Set the network communication listener.
     * @param networkStatusListener
     */
    public void setNetworkStatusListener(NetworkStatusListener networkStatusListener) {
        Network.networkStatusListener = networkStatusListener;
    }

    /**
     * Set the crawl request listener
     * @param mutualBlockListener
     */
    public void setMutualBlockListener(PeerSummaryActivity mutualBlockListener) {
        Network.mutualBlockListener = mutualBlockListener;
    }

    /**
     * Open the network channel on the default port.
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
     * @param inputBuffer the received data
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

        if (networkStatusListener != null) {
            networkStatusListener.updateConnectionType(connectionType, typename, subtypeName);
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

        Message.Builder messageBuilder = Message.newBuilder()
                .setSourcePublicKey(ByteString.copyFrom(publicKey.toBytes()))
                .setSourceName(name)
                .setDestinationAddress(ByteString.copyFrom(peer.getAddress().getAddress().getAddress()))
                .setDestinationPort(peer.getAddress().getPort())
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
    public void sendBlockMessage(Peer peer, TrustChainBlock block) throws IOException {
        Message message = Message.newBuilder()
                .setSourcePublicKey(ByteString.copyFrom(publicKey.toBytes()))
                .setSourceName(name)
                .setDestinationAddress(ByteString.copyFrom(peer.getAddress().getAddress().getAddress()))
                .setDestinationPort(peer.getAddress().getPort())
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
        Message message = Message.newBuilder()
                .setSourcePublicKey(ByteString.copyFrom(publicKey.toBytes()))
                .setSourceName(name)
                .setDestinationAddress(ByteString.copyFrom(peer.getAddress().getAddress().getAddress()))
                .setDestinationPort(peer.getAddress().getPort())
                .setType(CRAWL_REQUEST_ID)
                .setPayload(MessageProto.Payload.newBuilder().setCrawlRequest(request))
                .build();

        sendMessage(message, peer);
    }

    /**
     * Send a puncture request.
     *
     * @param peer         the destination.
     * @param puncturePeer the Peer to puncture.
     * @throws IOException
     */
    public void sendPunctureRequest(Peer peer, Peer puncturePeer) throws IOException {
        MessageProto.PunctureRequest pRequest = MessageProto.PunctureRequest.newBuilder()
                .setSourceSocket(internalSourceAddress.toString())
                .setPuncturePeer(puncturePeer.getProtoPeer())
                .build();

        Message message = Message.newBuilder()
                .setSourcePublicKey(ByteString.copyFrom(publicKey.toBytes()))
                .setSourceName(name)
                .setDestinationAddress(ByteString.copyFrom(peer.getAddress().getAddress().getAddress()))
                .setDestinationPort(peer.getAddress().getPort())
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

        Message message = Message.newBuilder()
                .setSourcePublicKey(ByteString.copyFrom(publicKey.toBytes()))
                .setSourceName(name)
                .setDestinationAddress(ByteString.copyFrom(peer.getAddress().getAddress().getAddress()))
                .setDestinationPort(peer.getAddress().getPort())
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
        List<MessageProto.Peer> peers = new ArrayList<>();
        for (Peer p : networkStatusListener.getPeerHandler().getPeerList()) {
            if (p.isReceivedFrom() && p.getName() != null && p.isAlive())
                peers.add(p.getProtoPeer());
        }

        MessageProto.IntroductionResponse response = MessageProto.IntroductionResponse.newBuilder()
                .setConnectionType(connectionType)
                .setNetworkOperator(networkOperator)
                .setInternalSourceSocket(internalSourceAddress.toString())
                .setInvitee(invitee.getProtoPeer())
                .addAllPeers(peers)
                .build();

        Message message = Message.newBuilder()
                .setSourcePublicKey(ByteString.copyFrom(publicKey.toBytes()))
                .setSourceName(name)
                .setDestinationAddress(ByteString.copyFrom(peer.getAddress().getAddress().getAddress()))
                .setDestinationPort(peer.getAddress().getPort())
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
    private synchronized void sendMessage(Message message, Peer peer) throws IOException {
        ByteBuffer outputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        channel.send(outputBuffer.wrap(message.toByteArray()), peer.getAddress());
        Log.i(TAG, "Sending " + message);
        peer.sentData();
        if (networkStatusListener != null) {
            networkStatusListener.updatePeerLists();
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
     *  - Tries to parse the bytes into a proto Message.
     *  - Updates the external ip address according to where the message was sent.
     *  - Add the new connection as a new peer or update an existing peer.
     *  - Send the message along for further processing.
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
            Message message = Message.parseFrom(data);
            Log.i(TAG, "Received " + message.toString());

            if (networkStatusListener != null) {
                networkStatusListener.updateWan(message);

                PublicKeyPair sourcePubKey = new PublicKeyPair(message.getSourcePublicKey().toByteArray());
                Peer peer = networkStatusListener.getPeerHandler().getOrMakePeer(address,sourcePubKey,message.getSourceName());
                if (peer == null) {
                    return;
                }

                peer.receivedData();
                PubKeyAndAddressPairStorage.addPubkeyAndAddressPair(context, sourcePubKey, address);
                handleMessage(peer, message, sourcePubKey, context);
                networkStatusListener.updatePeerLists();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks which message type we've received and calls the appropriate functions to handle
     * further processing of the message.
     * @param peer the peer that sent this message
     * @param message the message that was received
     * @param pubKeyPair the publickeypair associated with the sender
     * @param context context which is used to update the inbox
     * @throws Exception
     */
    public void handleMessage(Peer peer, Message message, PublicKeyPair pubKeyPair, Context context) throws Exception {
        switch (message.getType()) {
            case INTRODUCTION_REQUEST_ID:
                messageHandler.handleIntroductionRequest(peer, message.getPayload().getIntroductionRequest());
                break;
            case INTRODUCTION_RESPONSE_ID:
                messageHandler.handleIntroductionResponse(peer, message.getPayload().getIntroductionResponse());
                break;
            case PUNCTURE_ID:
                messageHandler.handlePuncture(peer, message.getPayload().getPuncture());
                break;
            case PUNCTURE_REQUEST_ID:
                messageHandler.handlePunctureRequest(peer, message.getPayload().getPunctureRequest());
                break;
            case BLOCK_MESSAGE_ID:
                TrustChainBlock block = message.getPayload().getBlock();

                // update the inbox
                addPeerToInbox(pubKeyPair, peer, context);
                addBlockToInbox(block, context);

                messageHandler.handleReceivedBlock(peer, block);
                if (mutualBlockListener != null) {
                    mutualBlockListener.blockAdded(block);
                }
                break;
            case CRAWL_REQUEST_ID:
                messageHandler.handleCrawlRequest(peer, message.getPayload().getCrawlRequest());
                break;
        }
    }

    /**
     * Add peer to inbox.
     * This means storing the InboxItem object in the local preferences.
     * @param pubKeyPair keypair associated with this peer
     * @param peer the peer that needs to be added
     * @param context needed for storage
     */
    private static void addPeerToInbox(PublicKeyPair pubKeyPair, Peer peer, Context context) {
        if (pubKeyPair != null) {
            InboxItem i = new InboxItem(peer, new ArrayList<>());
            InboxItemStorage.addInboxItem(context, i);
        }
    }

    /**
     * Add a block reference to the InboxItem and store this again locally.
     * @param block the block of which the reference should be stored.
     * @param context needed for storage
     */
    private static void addBlockToInbox(TrustChainBlock block, Context context) {
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
                if (networkStatusListener != null) {
                    networkStatusListener.updateInternalSourceAddress(internalSourceAddress.toString().replace("/",""));
                }
            }
        }
    }
}
