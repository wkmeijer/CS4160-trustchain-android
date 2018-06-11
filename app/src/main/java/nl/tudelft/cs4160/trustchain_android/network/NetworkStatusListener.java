package nl.tudelft.cs4160.trustchain_android.network;

import java.net.UnknownHostException;

import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.peer.PeerHandler;

public interface NetworkStatusListener {
    void updateInternalSourceAddress(String address);
    void updatePeerLists();
    void updateWan(MessageProto.Message message) throws UnknownHostException;
    void updateConnectionType(int connectionType, String typename, String subtypename);
    PeerHandler getPeerHandler();
}
