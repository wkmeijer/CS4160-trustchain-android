package nl.tudelft.cs4160.trustchain_android.peer.connection;

/**
 * Created by timbu on 15/01/2018.
 */

public interface PeerListener {

    void updateIncomingPeers();
    void updateOutgoingPeers();
}
