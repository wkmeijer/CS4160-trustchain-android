package nl.tudelft.cs4160.trustchain_android.network.peer;

/**
 * Created by timbu on 15/01/2018.
 */

public interface PeerListener {

    void updateActivePeers();
    void updateNewPeers();
}
