package nl.tudelft.cs4160.trustchain_android.appToAppTest;


import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;
import nl.tudelft.cs4160.trustchain_android.appToApp.PeerHandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.validateMockitoUsage;

/**
 * Created by Boning on 12/17/2017.
 */

public class PeerHandlerTest {
    private PeerHandler peerHandler;
    private ArrayList<PeerAppToApp> originalIpList;
    private ArrayList<PeerAppToApp> expectedIpList;
    InetSocketAddress rnadomInet = new InetSocketAddress(200);
    private String randomHashIdName = "randomHashIdName";

    @Before
    public void initialization() {
        PeerAppToApp peer1 = new PeerAppToApp("peer1", rnadomInet);
        PeerAppToApp peer2 = new PeerAppToApp("peer2", rnadomInet);
        PeerAppToApp peer3 = new PeerAppToApp("peer3", rnadomInet);

        originalIpList = new ArrayList<PeerAppToApp>();
        originalIpList.add(peer1);
        originalIpList.add(peer2);
        originalIpList.add(peer3);
        originalIpList.add(peer1);

        expectedIpList = new ArrayList<PeerAppToApp>();
        expectedIpList.add(peer1);
        expectedIpList.add(peer2);
        expectedIpList.add(peer3);

        peerHandler = new PeerHandler(randomHashIdName);
        peerHandler.setPeerList(originalIpList);
    }

    @Test
    public void removeDuplicatesTest() {
        peerHandler.removeDuplicates();
        ArrayList<PeerAppToApp> newIPPeerList = peerHandler.getPeerList();
        boolean failed = false;

        for (PeerAppToApp peer : newIPPeerList) {
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
        PeerAppToApp peer4 = new PeerAppToApp("peer4", rnadomInet);
        assertTrue(peerHandler.peerExistsInList(originalIpList.get(0)));
        assertFalse(peerHandler.peerExistsInList(peer4));
    }

    @Test
    public void testCertainMethods() {
        peerHandler = new PeerHandler("name");
        PeerAppToApp peer = new PeerAppToApp("peer", rnadomInet);
        peerHandler.add(peer);
        assertEquals(1, peerHandler.size());
        peerHandler.remove(peer);
        assertEquals(0, peerHandler.size());
    }

    @Test
    public void testWanVoteNull() {
        assertEquals(
                peerHandler.getWanVote().getAddress(), null);
    }

    @Test
    public void testSetPeerlist() {

        PeerAppToApp peer1 = new PeerAppToApp("peer1", rnadomInet);
        PeerAppToApp peer2 = new PeerAppToApp("peer2", rnadomInet);
        PeerAppToApp peer3 = new PeerAppToApp("peer3", rnadomInet);
        ArrayList<PeerAppToApp> list = new ArrayList<PeerAppToApp>();
        list.add(peer1);
        list.add(peer2);
        list.add(peer3);
        list.add(peer1);

        peerHandler.setPeerList(list);
        assertEquals(
                peerHandler.getPeerList().toString(), list.toString());
    }

    @Test
    public void testPeerlistAdd() {
        int size = peerHandler.getPeerList().size();
        PeerAppToApp peer2 = new PeerAppToApp("peer2", rnadomInet);
        peerHandler.add(peer2);
        assertEquals(
                peerHandler.getPeerList().size(), size + 1);
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
        assertFalse(peerHandler.peerExistsInList(new PeerAppToApp("peerA??", new InetSocketAddress(202))));
    }
    @Test
    public void testAdd() {
        PeerAppToApp randomPeer = new PeerAppToApp("peerA??", new InetSocketAddress(202));
        peerHandler.add(randomPeer);
        assertTrue(peerHandler.peerExistsInList(randomPeer));
    }
    @Test
    public void testAddPeerAlreadyInList() {
        int size = peerHandler.getPeerList().size();
        peerHandler.addPeer(originalIpList.get(0).getPeerId(), originalIpList.get(0).getAddress(), true);
        assertEquals(peerHandler.getPeerList().size(), size);
    }

    @Test
    public void testEligiblePeer() {
        PeerAppToApp peer = peerHandler.getEligiblePeer(originalIpList.get(0));
        assertNotEquals(peer.toString(), originalIpList.get(0).toString());
    }

    @After
    public void resetMocks() {
        validateMockitoUsage();
    }
}
