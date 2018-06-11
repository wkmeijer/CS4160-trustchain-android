package nl.tudelft.cs4160.trustchain_android.network.peer;

public interface PeerListener {

    void updateActivePeers();
    void updateNewPeers();
}
