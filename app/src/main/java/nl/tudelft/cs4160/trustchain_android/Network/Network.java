package nl.tudelft.cs4160.trustchain_android.Network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.Util.ByteArrayConverter;
import nl.tudelft.cs4160.trustchain_android.Util.Key;
import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;
import nl.tudelft.cs4160.trustchain_android.appToApp.PeerList;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.WanVote;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.BlockMessage;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.IntroductionRequest;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.IntroductionResponse;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Message;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Puncture;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.PunctureRequest;
import nl.tudelft.cs4160.trustchain_android.database.TrustChainDBHelper;
import nl.tudelft.cs4160.trustchain_android.main.PeerListAdapter;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlock.createBlock;
import static nl.tudelft.cs4160.trustchain_android.message.MessageProto.Message.newBuilder;

/**
 * Created by michiel on 11-1-2018.
 */

public class Network {

    public static String CONNECTABLE_ADDRESS = "145.94.185.61";
    final static int UNKNOWN_PEER_LIMIT = 20;
    final static String HASH_ID = "hash_id";
    final static int DEFAULT_PORT = 1873;
    final static int KNOWN_PEER_LIMIT = 10;
    private static final int BUFFER_SIZE = 65536;

    private DatagramChannel channel;

    private PeerList peerList;
    private String hashId;

    private boolean willExit = false;

    private int connectionType;
    private ByteBuffer outBuffer;
    private InetSocketAddress internalSourceAddress;
    private String networkOperator;

    private static Network network;
    private String publicKey;
    private TrustChainDBHelper dbHelper;

    private Network() {}

    public static Network getInstance(Context context, DatagramChannel channel) {
        if(network == null) {
            network = new Network();
            network.initVariables(context, channel);
        }
        return network;
    }

    private void initVariables(Context context, DatagramChannel channel) {
        TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
        networkOperator = telephonyManager.getNetworkOperatorName();

        dbHelper = new TrustChainDBHelper(context);
        peerList = new PeerList();
        outBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        hashId = UserNameStorage.getUserName(context);
        publicKey = ByteArrayConverter.bytesToHexString(Key.loadKeys(context).getPublic().getEncoded());
        this.channel = channel;
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
        for (PeerAppToApp p : peerList.getList()) {
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
    public synchronized void sendMessage(Message message, PeerAppToApp peer) throws IOException {
        message.putPubKey(publicKey);

        Log.d("App-To-App Log", "Sending " + message);
        outBuffer.clear();
        message.writeToByteBuffer(outBuffer);
        outBuffer.flip();
        channel.send(outBuffer, peer.getAddress());
        peer.sentData();
        //updatePeerLists();
    }
}
