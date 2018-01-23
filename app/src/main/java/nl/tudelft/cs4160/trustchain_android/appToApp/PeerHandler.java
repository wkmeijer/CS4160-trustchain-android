package nl.tudelft.cs4160.trustchain_android.appToApp;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import nl.tudelft.cs4160.trustchain_android.appToApp.connection.PeerListener;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.WanVote;

/**
 * Created by timbu on 02/12/2017.
 */
public class PeerHandler {
    private final ReentrantLock peerListLock = new ReentrantLock();
    private ArrayList<PeerAppToApp> peerList;
    private List<PeerAppToApp> incomingList = new ArrayList<>();
    private List<PeerAppToApp> outgoingList = new ArrayList<>();
    private PeerListener peerListener;
    public String hashId;
    private WanVote wanVote;

    public PeerHandler(ArrayList<PeerAppToApp> list, String hashId) {
        this.peerList = list;
        this.hashId = hashId;
        this.wanVote = new WanVote();
    }

    public PeerHandler(String hashId) {
        this.peerList = new ArrayList<>();
        this.hashId = hashId;
        this.wanVote = new WanVote();
    }

    public void setPeerListener(PeerListener peerListener){
        this.peerListener = peerListener;
    }

    /**
     * Remove duplicate peers from the peerlist.
     */
    public void removeDuplicates() {
        peerListLock.lock();
        try {
            for (int i = 0; i < peerList.size(); i++) {
                PeerAppToApp p1 = peerList.get(i);
                for (int j = 0; j < peerList.size(); j++) {
                    PeerAppToApp p2 = peerList.get(j);
                    if (j != i && p1.getPeerId() != null && p1.getPeerId().equals(p2.getPeerId())) {
                        peerList.remove(p2);
                    }
                }
            }
        } finally {
            peerListLock.unlock();
        }
    }

    public void removeDeadPeers() {
        peerListLock.lock();
        try {
            for (PeerAppToApp peer : new ArrayList<>(peerList)) {
                if (peer.canBeRemoved()) {
                    peerList.remove(peer);
                }
            }
        } finally {
            peerListLock.unlock();
        }
    }

    public void add(PeerAppToApp p) {
        peerListLock.lock();
        try {
            this.peerList.add(p);
        } finally {
            peerListLock.unlock();
        }
    }

    public void remove(PeerAppToApp p) {
        peerListLock.lock();
        try {
            this.peerList.remove(p);
        } finally {
            peerListLock.unlock();
        }
    }

    public int size() {
        peerListLock.lock();
        try {
            return peerList.size();
        } finally {
            peerListLock.unlock();
        }
    }

    public boolean peerExistsInList(PeerAppToApp peer) {
        peerListLock.lock();
        try {
            if (peer.getPeerId() == null) return false;
            for (PeerAppToApp p : this.peerList) {
                if (peer.getPeerId().equals(p.getPeerId())) {
                    return true;
                }
            }
            return false;
        } finally {
            peerListLock.unlock();
        }
    }


    /**
     * Add a inboxItem to the inboxItem list.
     *
     * @param peerId   the inboxItem's id.
     * @param address  the inboxItem's address.
     * @param incoming whether the inboxItem is an incoming inboxItem.
     * @return the added inboxItem.
     */
    public synchronized PeerAppToApp addPeer(String peerId, InetSocketAddress address, boolean incoming) {
        peerListLock.lock();
        try {
            if (hashId.equals(peerId)) {
                Log.d("App-To-App Log", "Not adding self");
                PeerAppToApp self = null;
                for (PeerAppToApp p : peerList) {
                    if (p.getAddress().equals(wanVote.getAddress()))
                        self = p;
                }
                if (self != null) {
                    peerList.remove(self);
                    Log.d("App-To-App Log", "Removed self");
                }
                return null;
            }
            if (wanVote.getAddress() != null && wanVote.getAddress().equals(address)) {
                Log.d("App-To-App Log", "Not adding inboxItem with same address as wanVote");
                return null;
            }
            for (PeerAppToApp peer : peerList) {
                if (peer.getPeerId() != null && peer.getPeerId().equals(peerId)) return peer;
                if (peer.getAddress().equals(address)) return peer;
            }
            final PeerAppToApp peer = new PeerAppToApp(peerId, address);

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    peerListLock.lock();
                    try {
                        peerList.add(peer);
                        splitPeerList();
                        Log.d("App-To-App Log", "Added " + peer);
                    } finally {
                        peerListLock.unlock();
                    }
                    peerListener.updateIncomingPeers();
                    peerListener.updateOutgoingPeers();
                }
            });
            return peer;
        } finally {
            peerListLock.unlock();
        }
    }

        /**
     * Split the inboxItem list between incoming and outgoing peers.
     */
    public void splitPeerList() {
        peerListLock.lock();
        try {
            List<PeerAppToApp> newIncoming = new ArrayList<>();
            List<PeerAppToApp> newOutgoing = new ArrayList<>();
            for (PeerAppToApp peer : peerList) {
                if (peer.hasReceivedData()) {
                    newIncoming.add(peer);
                } else {
                    newOutgoing.add(peer);
                }
            }
            if (!newIncoming.equals(incomingList)) {
                incomingList.clear();
                incomingList.addAll(newIncoming);
            }
            if (!newOutgoing.equals(outgoingList)) {
                outgoingList.clear();
                outgoingList.addAll(newOutgoing);
            }
        } finally {
            peerListLock.unlock();
        }
    }


    /**
     * Pick a random eligible inboxItem/invitee for sending an introduction request to.
     *
     * @param excludePeer inboxItem to which the invitee is sent.
     * @return the eligible inboxItem if any, else null.
     */
    public PeerAppToApp getEligiblePeer(PeerAppToApp excludePeer) {
        peerListLock.lock();
        try {
            List<PeerAppToApp> eligiblePeers = new ArrayList<>();
            for (PeerAppToApp p : peerList) {
                if (p.isAlive() && !p.equals(excludePeer)) {
                    eligiblePeers.add(p);
                }
            }
            if (eligiblePeers.size() == 0) {
                Log.d("App-To-App Log", "No elegible peers!");
                return null;
            }
            Random random = new Random();
            return eligiblePeers.get(random.nextInt(eligiblePeers.size()));
        } finally {
            peerListLock.unlock();
        }
    }

    /**
     * Resolve a inboxItem id or address to a inboxItem, else create a new one.
     *
     * @param id       the inboxItem's unique id.
     * @param address  the inboxItem's address.
     * @param incoming boolean indicator whether the inboxItem is incoming.
     * @return the resolved or create inboxItem.
     */
    synchronized public PeerAppToApp getOrMakePeer(String id, InetSocketAddress address, boolean incoming) {
        peerListLock.lock();
        try {
            if (id != null) {
                for (PeerAppToApp peer : peerList) {
                    if (id.equals(peer.getPeerId())) {
                        if (!address.equals(peer.getAddress())) {
                            Log.d("App-To-App Log", "Peer address differs from known address");
                            peer.setAddress(address);
                            removeDuplicates();
                        }
                        return peer;
                    }
                }
            }
            for (PeerAppToApp peer : peerList) {
                if (peer.getAddress().equals(address)) {
                    if (id != null) peer.setPeerId(id);
                    return peer;
                }
            }
            return addPeer(id, address, incoming);
        } finally {
            peerListLock.unlock();
        }
    }

    public String getHashId() {
        return hashId;
    }

    public WanVote getWanVote() {
        return wanVote;
    }

    public List<PeerAppToApp> getIncomingList() {
        return incomingList;
    }

    public List<PeerAppToApp> getOutgoingList() {
        return outgoingList;
    }

    public ArrayList<PeerAppToApp> getPeerList() {
        peerListLock.lock();
        try {
            return peerList;
        } finally {
            peerListLock.unlock();
        }
    }

    public void setPeerList(ArrayList<PeerAppToApp> peerList) {
        peerListLock.lock();
        try {
            this.peerList = peerList;
        } finally {
            peerListLock.unlock();
        }
    }
}