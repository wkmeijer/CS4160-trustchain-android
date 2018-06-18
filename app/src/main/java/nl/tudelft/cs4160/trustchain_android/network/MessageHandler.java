package nl.tudelft.cs4160.trustchain_android.network;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper;
import nl.tudelft.cs4160.trustchain_android.block.ValidationResult;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.peer.PeerHandler;
import nl.tudelft.cs4160.trustchain_android.storage.database.TrustChainDBHelper;

public class MessageHandler {
    private TrustChainDBHelper dbHelper;
    private Network network;
    private PeerHandler peerHandler;
    final String TAG = "MesssageHandler";

    public MessageHandler(Network network, TrustChainDBHelper dbHelper, PeerHandler peerHandler) {
        this.network = network;
        this.dbHelper = dbHelper;
        this.peerHandler = peerHandler;
    }

    /**
     * Handle an introduction request. Send a puncture request to the included invitee.
     *
     * @param peer    the peer requesting the introduction
     * @param request the message.
     * @throws IOException
     */
    public void handleIntroductionRequest(Peer peer, MessageProto.IntroductionRequest request) throws IOException {
        peer.setConnectionType((int) request.getConnectionType());
        if (getPeerHandler().size() > 1) {
            List<Peer> excludePeers = new ArrayList<>();
            excludePeers.add(peer);
            Peer invitee = getPeerHandler().getEligiblePeer(excludePeers);
            if (invitee != null) {
                network.sendIntroductionResponse(peer, invitee);
                network.sendPunctureRequest(invitee, peer);
                Log.d("Network", "Introducing " + invitee.getAddress() + " to " + peer.getAddress());
            }
        } else {
            Log.d("Network", "Peerlist too small, can't handle introduction request");
            // send a response anyway containing this device, for heartbeat timer purposes
            network.sendIntroductionResponse(peer, peer);
        }
    }

    /**
     * Handle an introduction response. Parse incoming peers.
     *
     * @param peer    the peer that sent this response.
     * @param response the message.
     */
    public void handleIntroductionResponse(Peer peer, MessageProto.IntroductionResponse response) {
        peer.setConnectionType((int) response.getConnectionType());
        List<MessageProto.Peer> peersList = response.getPeersList();
        for (MessageProto.Peer peerProto : peersList) {
            Peer p = new Peer(peerProto);
            Log.d(TAG, "From " + peer + " | found peer in pexList: " + p);

            if (!getPeerHandler().publicKeyPair.equals(p.getPublicKeyPair())) {
                getPeerHandler().getOrMakePeer(p.getAddress(), p.getPublicKeyPair(), p.getName());
            }
        }
    }

    /**
     * Handle a puncture request. Sends a puncture to the puncture inboxItem included in the message.
     *
     * @param peer    the origin peer.
     * @param request the message.
     * @throws IOException
     */
    public void handlePunctureRequest(Peer peer, MessageProto.PunctureRequest request) throws IOException {
        Peer puncturePeer = new Peer(request.getPuncturePeer());
        if (!getPeerHandler().peerExistsInList(puncturePeer)) {
            network.sendPuncture(puncturePeer);
        }
    }

    /**
     * Handle a puncture. Does nothing because the only purpose of a puncture is to punch a hole in the NAT.
     *
     * @param peer    the origin inboxItem.
     * @param puncture the message.
     * @throws IOException
     */
    public void handlePuncture(Peer peer, MessageProto.Puncture puncture) throws IOException {
    }

    /**
     * Handle the received (half) block.
     * This block is placed in in the TrustChainDB, except if it is INVALID.
     * @param peer the sending peer
     * @param block the data send
     * @throws IOException
     */
    public void handleReceivedBlock(Peer peer, MessageProto.TrustChainBlock block) {
        try {
            if (TrustChainBlockHelper.validate(block,dbHelper).getStatus() != ValidationResult.INVALID ) {
                dbHelper.replaceInDB(block);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle crawl request
     * @param peer the sending peer
     * @param request the crawlRequest
     * @throws IOException
     */
    public void handleCrawlRequest(Peer peer, MessageProto.CrawlRequest request) throws IOException {
        //ToDo for future application sending the entire chain is a bit too much
        for (MessageProto.TrustChainBlock block : dbHelper.getAllBlocks()) {
            network.sendBlockMessage(peer, block);
        }
    }

    /**
     * Return the peer handler object.
     * @return
     */
    public PeerHandler getPeerHandler() {
        return peerHandler;
    }

    public void setPeerHandler(PeerHandler peerHandler) {
        this.peerHandler = peerHandler;
    }
}
