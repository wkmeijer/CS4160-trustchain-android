package nl.tudelft.cs4160.trustchain_android.network.peer;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.validateMockitoUsage;

/**
 * Created by Boning on 12/17/2017.
 */

public class PeerHandlerTest {
    private PeerHandler peerHandler;
    private ArrayList<Peer> originalIpList;
    private ArrayList<Peer> expectedIpList;
    InetSocketAddress rnadomInet = new InetSocketAddress(200);
    private String randomHashIdName = "randomHashIdName";

    @Before
    public void initialization() {
        Peer peer1 = new Peer("peer1", rnadomInet);
        Peer peer2 = new Peer("peer2", rnadomInet);
        Peer peer3 = new Peer("peer3", rnadomInet);

        originalIpList = new ArrayList<>();
        originalIpList.add(peer1);
        originalIpList.add(peer2);
        originalIpList.add(peer3);
        originalIpList.add(peer1);

        expectedIpList = new ArrayList<>();
        expectedIpList.add(peer1);
        expectedIpList.add(peer2);
        expectedIpList.add(peer3);

        peerHandler = new PeerHandler(randomHashIdName);
        peerHandler.setPeerList(originalIpList);
    }

    @Test
    public void removeDuplicatesTest() {
        peerHandler.removeDuplicates();
        ArrayList<Peer> newIPPeerList = peerHandler.getPeerList();
        boolean failed = false;

        for (Peer peer : newIPPeerList) {
            if (!expectedIpList.remove(peer)) {
                failed = true;
                break;
            }
        }
        if (expectedIpList.size() != 0) failed = false;
        assertFalse(failed);
    }

    @Test
    public void peerExistsInListTest() {
        Peer peer4 = new Peer("peer4", rnadomInet);
        assertTrue(peerHandler.peerExistsInList(originalIpList.get(0)));
        assertFalse(peerHandler.peerExistsInList(peer4));
    }

    @Test
    public void testCertainMethods() {
        peerHandler = new PeerHandler("name");
        Peer peer = new Peer("peer", rnadomInet);
        peerHandler.add(peer);
        assertEquals(1, peerHandler.size());
        peerHandler.remove(peer);
        assertEquals(0, peerHandler.size());
    }

    @Test
    public void testWanVoteNull() {
        assertEquals(peerHandler.getWanVote().getAddress(), null);
    }

    @Test
    public void testSetPeerlist() {
        assertEquals(expectedIpList.size(),peerHandler.getPeerList().size());
    }

    @Test
    public void testPeerlistAdd() {
        int size = peerHandler.getPeerList().size();
        Peer peer2 = new Peer("peer2", rnadomInet);
        peerHandler.add(peer2);
        assertEquals(peerHandler.getPeerList().size(), size + 1);
    }


    @Test
    public void testPeerListGetHash() {
        assertEquals(peerHandler.getHashId(), randomHashIdName);
    }

    @Test
    public void testRemoveAPeers() {
        int size = peerHandler.getPeerList().size();
        peerHandler.remove(originalIpList.get(0));
        assertEquals(peerHandler.getPeerList().size(), size - 1);
    }

    @Test
    public void testExistsIn() {
        assertTrue(peerHandler.peerExistsInList(originalIpList.get(0)));
    }

    @Test
    public void testNotExistsIn() {
        assertFalse(peerHandler.peerExistsInList(new Peer("peerA??", new InetSocketAddress(202))));
    }

    @Test
    public void testAdd() {
        Peer randomPeer = new Peer("peerA??", new InetSocketAddress(202));
        peerHandler.add(randomPeer);
        assertTrue(peerHandler.peerExistsInList(randomPeer));
    }

    @Test
    public void testAddPeerAlreadyInList() {
        int size = peerHandler.getPeerList().size();
        peerHandler.addPeer(originalIpList.get(0).getPeerId(), originalIpList.get(0).getAddress());
        assertEquals(peerHandler.getPeerList().size(), size);
    }

    @Test
    public void testEligiblePeer() {
        Peer peer = peerHandler.getEligiblePeer(originalIpList.get(0));
        assertNotEquals(peer.toString(), originalIpList.get(0).toString());
    }

    @After
    public void resetMocks() {
        validateMockitoUsage();
    }
}
