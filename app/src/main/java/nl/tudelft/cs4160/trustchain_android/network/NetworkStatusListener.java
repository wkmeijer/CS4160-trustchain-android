package nl.tudelft.cs4160.trustchain_android.network;

import java.io.IOException;
import java.net.UnknownHostException;

import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.network.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.network.peer.PeerHandler;

public interface NetworkStatusListener {
    void updateInternalSourceAddress(String address);
    void updatePeerLists();
    void updateWan(MessageProto.Message message) throws UnknownHostException;
    void updateConnectionType(int connectionType, String typename, String subtypename);
    PeerHandler getPeerHandler();
}
