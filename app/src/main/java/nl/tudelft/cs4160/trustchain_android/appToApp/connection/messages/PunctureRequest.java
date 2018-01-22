package nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages;

import java.net.InetSocketAddress;
import java.util.Map;

import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;

/**
 * Created by jaap on 5/31/16.
 */
public class PunctureRequest extends Message {
    final private static String SOURCE = "source";
    final private static String PUNCTURE_PEER = "puncture_peer";

    public PunctureRequest(String peerId, InetSocketAddress destination, InetSocketAddress source, PeerAppToApp puncturePeer, String pubKey) {
        super(PUNCTURE_REQUEST_ID, peerId, destination, pubKey);
        put(SOURCE, createAddressMap(source));
        put(PUNCTURE_PEER, createPeerMap(puncturePeer));
    }

    public static Message fromMap(Map map) throws MessageException {
        String peerId = (String) map.get(PEER_ID);
        InetSocketAddress destination = Message.createMapAddress((Map) map.get(DESTINATION));
        InetSocketAddress source = Message.createMapAddress((Map) map.get(SOURCE));
        PeerAppToApp puncturePeer = Message.createMapPeer((Map) map.get(PUNCTURE_PEER));
        String pubKey = (String) map.get(PUB_KEY);
        return new PunctureRequest(peerId, destination, source, puncturePeer, pubKey);
    }

    public PeerAppToApp getPuncturePeer() throws MessageException {
        return createMapPeer((Map) get(PUNCTURE_PEER));
    }

    @Override
    public String toString() {
        return "PunctureRequest{" + super.toString() + "}";
    }
}
