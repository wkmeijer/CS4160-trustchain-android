package nl.tudelft.cs4160.trustchain_android.network.peer;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import nl.tudelft.cs4160.trustchain_android.network.WanVote;

public class PeerHandler {
    private ArrayList<Peer> peerList;
    private List<Peer> connectedPeersList = new ArrayList<>();
    private List<Peer> incomingPeersList = new ArrayList<>();
    private PeerListener peerListener;
    public String hashId;
    private WanVote wanVote;
    private final String TAG = this.getClass().toString();

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
            Peer p1 = peerList.get(i);
            for (int j = 0; j < peerList.size(); j++) {
                Peer p2 = peerList.get(j);
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
        for (Peer peer : new ArrayList<>(peerList)) {
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
    public synchronized void add(Peer p) {
        this.peerList.add(p);
    }

    /**
     * Remove a peer from the list.
     * Synchronized is to make sure this happens thread safe.
     *
     * @param p
     */
    public synchronized void remove(Peer p) {
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
    public synchronized boolean peerExistsInList(Peer peer) {
        if (peer.getPeerId() == null) return false;
        for (Peer p : this.peerList) {
            if (peer.getPeerId().equals(p.getPeerId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add a inboxItem to the peerlist.
     * Synchronized is to make sure this happens thread safe.
     * @param peerId   the inboxItem's id.
     * @param address  the inboxItem's address.
     * @return the added inboxItem.
     */
    public synchronized Peer addPeer(String peerId, InetSocketAddress address) {
        if (hashId.equals(peerId)) {
            Log.i(TAG, "Not adding self");
            Peer self = null;
            for (Peer p : peerList) {
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
        for (Peer peer : peerList) {
            if (peer.getPeerId() != null && peer.getPeerId().equals(peerId)) return peer;
            if (peer.getAddress().equals(address)) return peer;
        }
        final Peer peer = new Peer(peerId, address);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public synchronized void run() {
                    peerList.add(peer);
                    splitPeerList();
                peerListener.updateConnectedPeers();
                peerListener.updateIncomingPeers();
            }
        });
        return peer;
    }

    /**
     * Split the inboxItem list between incoming and outgoing peers.
     * Synchronized is to make sure this happens thread safe.
     */
    public synchronized void splitPeerList() {
        List<Peer> newIncoming = new ArrayList<>();
        List<Peer> newOutgoing = new ArrayList<>();
        for (Peer peer : peerList) {
            if (peer.hasReceivedData()) {
                newIncoming.add(peer);
            } else {
                newOutgoing.add(peer);
            }
        }
        if (!newIncoming.equals(connectedPeersList)) {
            connectedPeersList.clear();
            connectedPeersList.addAll(newIncoming);
        }
        if (!newOutgoing.equals(incomingPeersList)) {
            incomingPeersList.clear();
            incomingPeersList.addAll(newOutgoing);
        }
    }


    /**
     * Pick a random eligible inboxItem/invitee for sending an introduction request to.
     * The bootstrap is always an eligible peer, even it is is timed out.
     * Synchronized is to make sure this happens thread safe.
     * @param excludePeer inboxItem to which the invitee is sent.
     * @return the eligible inboxItem if any, else null.
     */
    public synchronized Peer getEligiblePeer(Peer excludePeer) {
        List<Peer> eligiblePeers = new ArrayList<>();
        for (Peer p : peerList) {
            if (p.isAlive() && !p.equals(excludePeer) || p.isBootstrap()) {
                eligiblePeers.add(p);
            }
        }
        if (eligiblePeers.size() == 0) {
            Log.d(TAG, "No eligible peers!");
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
    public synchronized Peer getOrMakePeer(String id, InetSocketAddress address) {
        if (id != null) {
            for (Peer peer : peerList) {
                if (id.equals(peer.getPeerId())) {
                    if (!address.equals(peer.getAddress())) {
                        Log.i(TAG, "Peer address differs from known address | address: " + address.toString() + " | peer.getAddress(): " + peer.getAddress().toString() + " | id: " + id + " | hashid: " + hashId);
                        peer.setAddress(address);
                        removeDuplicates();
                    }
                    return peer;
                }
            }
        }
        for (Peer peer : peerList) {
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

    public List<Peer> getConnectedPeersList() {
        return connectedPeersList;
    }

    public List<Peer> getIncomingPeersList() {
        return incomingPeersList;
    }

    /**
     * Get the peer list
     * Synchronized is to make sure this happens thread safe.
     * @return
     */
    public synchronized ArrayList<Peer> getPeerList() {
        return peerList;
    }

    /**
     * Set the peer list.
     * Synchronized is to make sure this happens thread safe.
     * @param peerList
     */
    public synchronized void setPeerList(ArrayList<Peer> peerList) {
        this.peerList = peerList;
        removeDuplicates();
    }
}