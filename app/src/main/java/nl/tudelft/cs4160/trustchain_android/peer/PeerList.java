package nl.tudelft.cs4160.trustchain_android.peer;

import java.util.ArrayList;

public class PeerList {
    private ArrayList<Peer> list;

    public PeerList(ArrayList<Peer> list) {
        this.list = list;
    }

    public PeerList() {
        this.list = new ArrayList<>();
    }

    public ArrayList<Peer> getList() {
        return list;
    }

    /**
     * Remove duplicate peers from the peerlist.
     */
    public void removeDuplicates() {
        for (int i = 0; i < list.size(); i++) {
            Peer p1 = list.get(i);
            for (int j = 0; j < list.size(); j++) {
                Peer p2 = list.get(j);
                if (j != i && p1.getPeerId() != null && p1.getPeerId().equals(p2.getPeerId())) {
                    list.remove(p2);
                }
            }
        }
    }

    public void add(Peer p) {
        this.list.add(p);
    }

    public void remove(Peer p) {
        this.list.remove(p);
    }

    public int size() {
        return list.size();
    }

    public boolean peerExistsInList(Peer peer) {
        if (peer.getPeerId() == null) return false;
        for (Peer p : this.list) {
            if (peer.getPeerId().equals(p.getPeerId())) {
                return true;
            }
        }
        return false;
    }
}
