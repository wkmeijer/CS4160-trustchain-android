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

public class PeerHandler {
    private ArrayList<PeerAppToApp> peerList;
    private List<PeerAppToApp> incomingList = new ArrayList<>();
    private List<PeerAppToApp> outgoingList = new ArrayList<>();
    private PeerListener peerListener;
    public String hashId;
    private WanVote wanVote;
    final String TAG = this.getClass().toString();

    /**
     * Peer handler constructor.
     *
     * @param hashId
     */
    public PeerHandler(String hashId) {
        this.peerList = new ArrayList<>();
        this.hashId = hashId;
        this.wanVote = new WanVote();
    }

    public void setPeerListener(PeerListener peerListener) {
        this.peerListener = peerListener;
    }

    /**
     * Remove duplicate peers from the peerlist.
     */
    public synchronized void removeDuplicates() {
        for (int i = 0; i < peerList.size(); i++) {
            PeerAppToApp p1 = peerList.get(i);
            for (int j = 0; j < peerList.size(); j++) {
                PeerAppToApp p2 = peerList.get(j);
                if (j != i && p1.getPeerId() != null && p1.getPeerId().equals(p2.getPeerId())) {
                    peerList.remove(p2);
                }
            }
        }
    }

    /**
     * Remove all inactive peers.
     */
    public synchronized void removeDeadPeers() {
        for (PeerAppToApp peer : new ArrayList<>(peerList)) {
            if (peer.canBeRemoved()) {
                peerList.remove(peer);
            }
        }
    }

    /**
     * Add peer to the list.
     * Synchronized is to make sure this happens thread safe.
     *
     * @param p
     */
    public synchronized void add(PeerAppToApp p) {
        this.peerList.add(p);
    }

    /**
     * Remove a peer from the list.
     * Synchronized is to make sure this happens thread safe.
     *
     * @param p
     */
    public synchronized void remove(PeerAppToApp p) {
        this.peerList.remove(p);
    }

    /**
     * Get the amount of peers.
     * Lock is to make sure this happens thread safe.
     *
     * @return
     */
    public synchronized int size() {
        return peerList.size();
    }

    /**
     * Check if a peer exists in the list.
     * Synchronized is to make sure this happens thread safe.
     *
     * @param peer
     * @return
     */
    public synchronized boolean peerExistsInList(PeerAppToApp peer) {
        if (peer.getPeerId() == null) return false;
        for (PeerAppToApp p : this.peerList) {
            if (peer.getPeerId().equals(p.getPeerId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add a inboxItem to the inboxItem list.
     * Synchronized is to make sure this happens thread safe.
     * @param peerId   the inboxItem's id.
     * @param address  the inboxItem's address.
     * @return the added inboxItem.
     */
    public synchronized PeerAppToApp addPeer(String peerId, InetSocketAddress address) {
        if (hashId.equals(peerId)) {
            Log.i(TAG, "Not adding self");
            PeerAppToApp self = null;
            for (PeerAppToApp p : peerList) {
                if (p.getAddress().equals(wanVote.getAddress()))
                    self = p;
            }
            if (self != null) {
                peerList.remove(self);
                Log.i(TAG, "Removed self");
            }
            return null;
        }
        if (wanVote.getAddress() != null && wanVote.getAddress().equals(address)) {
            Log.i(TAG, "Not adding inboxItem with same address as wanVote");
            return null;
        }
        for (PeerAppToApp peer : peerList) {
            if (peer.getPeerId() != null && peer.getPeerId().equals(peerId)) return peer;
            if (peer.getAddress().equals(address)) return peer;
        }
        final PeerAppToApp peer = new PeerAppToApp(peerId, address);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public synchronized void run() {
                    peerList.add(peer);
                    splitPeerList();
                    Log.i(TAG, "Added " + peer + " Peerlist now has size: " + peerList.size());
                peerListener.updateIncomingPeers();
                peerListener.updateOutgoingPeers();
            }
        });
        return peer;
    }

    /**
     * Split the inboxItem list between incoming and outgoing peers.
     * Synchronized is to make sure this happens thread safe.
     */
    public synchronized void splitPeerList() {
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
    }


    /**
     * Pick a random eligible inboxItem/invitee for sending an introduction request to.
     * Synchronized is to make sure this happens thread safe.
     * @param excludePeer inboxItem to which the invitee is sent.
     * @return the eligible inboxItem if any, else null.
     */
    public synchronized PeerAppToApp getEligiblePeer(PeerAppToApp excludePeer) {
        List<PeerAppToApp> eligiblePeers = new ArrayList<>();
        for (PeerAppToApp p : peerList) {
            if (p.isAlive() && !p.equals(excludePeer)) {
                eligiblePeers.add(p);
            }
        }
        if (eligiblePeers.size() == 0) {
            Log.d(TAG, "No elegible peers!");
            return null;
        }
        Random random = new Random();
        return eligiblePeers.get(random.nextInt(eligiblePeers.size()));
    }

    /**
     * Resolve a inboxItem id or address to a inboxItem, else create a new one.
     *
     * @param id       the inboxItem's unique id.
     * @param address  the inboxItem's address.
     * @return the resolved or create inboxItem.
     */
    public synchronized PeerAppToApp getOrMakePeer(String id, InetSocketAddress address) {
        if (id != null) {
            for (PeerAppToApp peer : peerList) {
                if (id.equals(peer.getPeerId())) {
                    if (!address.equals(peer.getAddress())) {
                        Log.i(TAG, "Peer address differs from known address | address: " + address.toString() + " peer.getAddress(): " + peer.getAddress().toString());
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
        return addPeer(id, address);
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

    /**
     * Get the peer list
     * Synchronized is to make sure this happens thread safe.
     * @return
     */
    public synchronized ArrayList<PeerAppToApp> getPeerList() {
        return peerList;
    }

    /**
     * Set the peer list.
     * Synchronized is to make sure this happens thread safe.
     * @param peerList
     */
    public synchronized void setPeerList(ArrayList<PeerAppToApp> peerList) {
        this.peerList = peerList;
    }
}