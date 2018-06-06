package nl.tudelft.cs4160.trustchain_android.inbox;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;

import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;
import nl.tudelft.cs4160.trustchain_android.network.peer.Peer;

public class InboxItem implements Serializable {
    private Peer peer;
    private ArrayList<Integer> halfBlockSequenceNumbers;
    long serialVersionUID = -5061379998789838600L;

    /**
     * Inbox item constructor
     * @param peer the peer which this inboxitem will be about
     * @param halfBlockSequenceNumbers the list of the sequence numbers of the unread blocks
     */
    public InboxItem(Peer peer, ArrayList<Integer> halfBlockSequenceNumbers) {
        this.peer = peer;
        this.halfBlockSequenceNumbers = halfBlockSequenceNumbers;
    }


    public ArrayList<Integer> getHalfBlocks() {
        return halfBlockSequenceNumbers;
    }

    public void addHalfBlocks(Integer block) {
        halfBlockSequenceNumbers.add(block);
    }

    public void setHalfBlocks(ArrayList<Integer> halfBlocks) {
        this.halfBlockSequenceNumbers = halfBlocks;
    }

    /**
     * return the amount of unread blocks
     * @return
     */
    public int getAmountUnread() {
        if (halfBlockSequenceNumbers != null) {
            return halfBlockSequenceNumbers.size();
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InboxItem other = (InboxItem) o;

        if (halfBlockSequenceNumbers != null ? !halfBlockSequenceNumbers.equals(other.halfBlockSequenceNumbers) : other.halfBlockSequenceNumbers != null)
            return false;
        return this.peer.equals(other.peer);
    }

    public Peer getPeer(){
       return peer;
    }
}
