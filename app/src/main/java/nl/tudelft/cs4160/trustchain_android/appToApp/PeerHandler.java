package nl.tudelft.cs4160.trustchain_android.appToApp;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import nl.tudelft.cs4160.trustchain_android.SharedPreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.PeerListener;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.WanVote;
import nl.tudelft.cs4160.trustchain_android.main.OverviewConnectionsActivity;
import nl.tudelft.cs4160.trustchain_android.main.PeerListAdapter;

/**
 * Created by timbu on 02/12/2017.
 */

public class PeerHandler {
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

    public void add(PeerAppToApp p) {
        this.peerList.add(p);
    }

    public void remove(PeerAppToApp p) {
        this.peerList.remove(p);
    }

    public int size() {
        return peerList.size();
    }

    public boolean peerExistsInList(PeerAppToApp peer) {
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
     *
     * @param peerId   the inboxItem's id.
     * @param address  the inboxItem's address.
     * @param incoming whether the inboxItem is an incoming inboxItem.
     * @return the added inboxItem.
     */
    public synchronized PeerAppToApp addPeer(String peerId, InetSocketAddress address, boolean incoming) {
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
                peerList.add(peer);
                splitPeerList();
                peerListener.updateIncomingPeers();
                peerListener.updateOutgoingPeers();
                Log.d("App-To-App Log", "Added " + peer);
            }
        });
        return peer;
    }

        /**
     * Split the inboxItem list between incoming and outgoing peers.
     */
    public void splitPeerList() {
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
     *
     * @param excludePeer inboxItem to which the invitee is sent.
     * @return the eligible inboxItem if any, else null.
     */
    public PeerAppToApp getEligiblePeer(PeerAppToApp excludePeer) {
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
    }

    /**
     * Resolve a inboxItem id or address to a inboxItem, else create a new one.
     *
     * @param id       the inboxItem's unique id.
     * @param address  the inboxItem's address.
     * @param incoming boolean indicator whether the inboxItem is incoming.
     * @return the resolved or create inboxItem.
     */
    public PeerAppToApp getOrMakePeer(String id, InetSocketAddress address, boolean incoming) {
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
        return peerList;
    }

    public void setPeerList(ArrayList<PeerAppToApp> peerList) {
        this.peerList = peerList;
    }
}