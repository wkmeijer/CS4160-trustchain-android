package nl.tudelft.cs4160.trustchain_android.inbox;

import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;

/**
 * Created by timbu on 08/01/2018.
 */

public class InboxItem {
    private PeerAppToApp peer;
    private int unreadCounter;

    public InboxItem(PeerAppToApp peer, int unreadCounter) {
        this.peer = peer;
        this.unreadCounter = unreadCounter;
    }

    public PeerAppToApp getPeer() {
        return peer;
    }

    public void setPeer(PeerAppToApp peer) {
        this.peer = peer;
    }

    public int getUnreadCounter() {
        return unreadCounter;
    }

    public void setUnreadCounter(int unreadCounter) {
        this.unreadCounter = unreadCounter;
    }
}
