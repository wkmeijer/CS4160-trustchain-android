package nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;

/**
 * Created by jaap on 5/31/16.
 */
public class IntroductionResponse extends Message {
    final private static String NETWORK_OPERATOR = "network_operator";
    final private static String CONNECTION_TYPE = "connection_type";
    final private static String INTERNAL_SOURCE = "internal_source";
    final private static String INVITEE = "invitee";
    final private static String PEX = "pex";

    private List<PeerAppToApp> pex;

    public IntroductionResponse(String peerId, InetSocketAddress internalSource, InetSocketAddress destination, PeerAppToApp invitee,
                                long connectionType, List<PeerAppToApp> pex, String networkOperator, String pubKey) {
        super(INTRODUCTION_RESPONSE_ID, peerId, destination, pubKey);
        this.pex = pex;
        put(CONNECTION_TYPE, connectionType);
        put(INTERNAL_SOURCE, createAddressMap(internalSource));
        if (invitee != null)
            put(INVITEE, createPeerMap(invitee));
        put(NETWORK_OPERATOR, networkOperator);
        List<Map> pexMap = new ArrayList<>();
        for (PeerAppToApp peer : pex) {
            pexMap.add(createPeerMap(peer));
        }
        put(PEX, pexMap);
    }

    public static Message fromMap(Map map) throws MessageException {
        String peerId = (String) map.get(PEER_ID);
        InetSocketAddress internalSource = Message.createMapAddress((Map) map.get(INTERNAL_SOURCE));
        InetSocketAddress destination = Message.createMapAddress((Map) map.get(DESTINATION));
        PeerAppToApp invitee = null;
        if (map.containsKey(INVITEE))
            invitee = Message.createMapPeer((Map) map.get(INVITEE));
        long connectionType = (long) map.get(CONNECTION_TYPE);
        String networkOperator = (String) map.get(NETWORK_OPERATOR);
        List<Map> pexMaps = (List<Map>) map.get(PEX);
        List<PeerAppToApp> pex = new ArrayList<>();
        for (Map m : pexMaps) {
            pex.add(Message.createMapPeer(m));
        }
        String pubKey = (String) map.get(PUB_KEY);
        return new IntroductionResponse(peerId, internalSource, destination, invitee, connectionType, pex, networkOperator, pubKey);
    }

    public String getNetworkOperator() {
        return (String) get(NETWORK_OPERATOR);
    }

    public List<PeerAppToApp> getPex() {
        return pex;
    }

    @Override
    public String toString() {
        return "IntroductionResponse{" + super.toString() + "}";
    }

    public long getConnectionType() {
        return (long) get(CONNECTION_TYPE);
    }
}
