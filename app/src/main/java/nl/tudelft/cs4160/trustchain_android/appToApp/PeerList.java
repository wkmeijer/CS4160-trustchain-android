package nl.tudelft.cs4160.trustchain_android.appToApp;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by timbu on 02/12/2017.
 */

public class PeerList {
    private ArrayList<PeerAppToApp> list;

    public PeerList(ArrayList<PeerAppToApp> list) {
        this.list = list;
    }

    public PeerList() {
        this.list = new ArrayList<>();
    }

    public ArrayList<PeerAppToApp> getList() {
        return list;
    }

    /**
     * Remove duplicate peers from the peerlist.
     */
    public void removeDuplicates() {
        for (int i = 0; i < list.size(); i++) {
            PeerAppToApp p1 = list.get(i);
            for (int j = 0; j < list.size(); j++) {
                PeerAppToApp p2 = list.get(j);
                if (j != i && p1.getPeerId() != null && p1.getPeerId().equals(p2.getPeerId())) {
                    list.remove(p2);
                }
            }
        }
    }

    public void add(PeerAppToApp p) {
        this.list.add(p);
    }

    public void remove(PeerAppToApp p) {
        this.list.remove(p);
    }

    public int size() {
        return list.size();
    }

    public boolean peerExistsInList(PeerAppToApp peer) {
        if (peer.getPeerId() == null) return false;
        for (PeerAppToApp p : this.list) {
            if (peer.getPeerId().equals(p.getPeerId())) {
                return true;
            }
        }
        return false;
    }
}
