package nl.tudelft.cs4160.trustchain_android.peer;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;
import nl.tudelft.cs4160.trustchain_android.network.WanVote;

public class PeerHandler {
    private ArrayList<Peer> peerList;
    private List<Peer> activePeersList = new ArrayList<>();
    private List<Peer> newPeersList = new ArrayList<>();
    private PeerListener peerListener;
    public PublicKeyPair publicKeyPair;
    String name;
    private WanVote wanVote;
    private final String TAG = this.getClass().toString();

    /**
     * Peer handler constructor.
     *
     * @param publicKeyPair the public keys of this device
     * @param name the username chosen for this device
     */
    public PeerHandler(PublicKeyPair publicKeyPair, String name) {
        this.peerList = new ArrayList<>();
        this.publicKeyPair = publicKeyPair;
        this.name = name;
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
                if (j != i && p1.getPublicKeyPair() != null && p1.getPublicKeyPair().equals(p2.getPublicKeyPair())) {
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
        if (peer.getPublicKeyPair() == null) return false;
        for (Peer p : this.peerList) {
            if (peer.getPublicKeyPair().equals(p.getPublicKeyPair())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add a peer to the peerlist.
     * Synchronized is to make sure this happens thread safe.
     * @param address  the peer's address.
     * @param peerPublicKeyPair the peer's public keys
     * @param name   the peer's name.
     * @return the added peer.
     */
    public synchronized Peer addPeer(InetSocketAddress address, PublicKeyPair peerPublicKeyPair, String name) {
        if (publicKeyPair.equals(peerPublicKeyPair)) {
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
            Log.i(TAG, "Not adding peer with same address as this device");
            return null;
        }
        for (Peer peer : peerList) {
            if (peer.getPublicKeyPair() != null && peer.getPublicKeyPair().equals(peerPublicKeyPair)) return peer;
            if (peer.getAddress().equals(address)) return peer;
        }
        final Peer peer = new Peer(address, peerPublicKeyPair, name);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public synchronized void run() {
                    peerList.add(peer);
                    splitPeerList();
                peerListener.updateActivePeers();
                peerListener.updateNewPeers();
            }
        });
        return peer;
    }

    /**
     * Split the peer list between connected and incoming peers.
     * Synchronized is to make sure this happens thread safe.
     */
    public synchronized void splitPeerList() {
        List<Peer> newConnected = new ArrayList<>();
        List<Peer> newIncoming = new ArrayList<>();
        for (Peer peer : peerList) {
            if (peer.isReceivedFrom()) {
                newConnected.add(peer);
            } else {
                newIncoming.add(peer);
            }
        }
        if (!newConnected.equals(activePeersList)) {
            activePeersList.clear();
            activePeersList.addAll(newConnected);
        }
        if (!newIncoming.equals(newPeersList)) {
            newPeersList.clear();
            newPeersList.addAll(newIncoming);
        }
    }


    /**
     * Pick a random eligible inboxItem/invitee for sending an introduction request to.
     * The bootstrap is always an eligible peer, even it is is timed out.
     * Synchronized is to make sure this happens thread safe.
     * @param excludePeers a list of peers to exclude, can be null
     * @return the eligible inboxItem if any, else null.
     */
    public synchronized Peer getEligiblePeer(List<Peer> excludePeers) {
        List<Peer> eligiblePeers = new ArrayList<>();
        for (Peer p : peerList) {
            boolean excluded = false;
            for(Peer e : excludePeers) {
                if(e.equals(p)) {
                    excluded = true;
                    break;
                }
            }
            if (p.isAlive() && !excluded || p.isBootstrap()) {
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
     * Resolve a public key or address to a peer, else create a new one.
     *
     * @param address  the peer's address.
     * @param publicKeyPair the peer's public keys
     * @param name   the peer's name.
     * @return the resolved or created peer.
     */
    public synchronized Peer getOrMakePeer(InetSocketAddress address, PublicKeyPair publicKeyPair, String name) {
        if (publicKeyPair != null) {
            for (Peer peer : peerList) {
                if (publicKeyPair.equals(peer.getPublicKeyPair())) {
                    if (!address.equals(peer.getAddress())) {
                        Log.i(TAG, "Peer address differs from known address | address: " + address.toString() + " | peer.getAddress(): " + peer.getAddress().toString() + " | peer's public keys: " + publicKeyPair + " | this device's public keys: " + this.publicKeyPair);
                        peer.setAddress(address);
                        peer.setName(name);
                        removeDuplicates();
                    }
                    return peer;
                }
            }
        }
        for (Peer peer : peerList) {
            if (peer.getAddress().equals(address)) {
                if (publicKeyPair != null) peer.setPublicKeyPair(publicKeyPair);
                peer.setName(name);
                return peer;
            }
        }
        return addPeer(address, publicKeyPair, name);
    }

    public PublicKeyPair getPublicKeyPair() {
        return publicKeyPair;
    }

    public String getName() {
        return name;
    }

    public WanVote getWanVote() {
        return wanVote;
    }

    public List<Peer> getactivePeersList() {
        return activePeersList;
    }

    public List<Peer> getnewPeersList() {
        return newPeersList;
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