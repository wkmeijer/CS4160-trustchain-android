package nl.tudelft.cs4160.trustchain_android.inbox;


import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.libsodium.jni.NaCl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;

import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;
import nl.tudelft.cs4160.trustchain_android.network.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.main.UserConfigurationActivity;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class InboxItemTest {

    private String userName;
    private ArrayList<Integer> halfBlockSequenceNumbers = new ArrayList<>();
    private String address;
    private PublicKeyPair publicKey;
    private int port;
    private InboxItem ii;


    @Rule
    public ActivityTestRule<UserConfigurationActivity> mActivityRule = new ActivityTestRule<>(
            UserConfigurationActivity.class,
            true,
            false);

    @Before
    public void setUp() {
        NaCl.sodium();
        userName = "userName";
        halfBlockSequenceNumbers.add(1);
        halfBlockSequenceNumbers.add(2);
        halfBlockSequenceNumbers.add(3);
        address = "address";
        publicKey = new DualSecret().getPublicKeyPair();
        port = 123;
        Peer peer = new Peer(new InetSocketAddress(address, port),publicKey,userName);
        ii = new InboxItem(peer, halfBlockSequenceNumbers);
    }


    @Test
    public void testConstructorUserName() {
        assertEquals(ii.getPeer().getName(), userName);
    }

    @Test
    public void testConstructorBlocks() {
        assertEquals(ii.getHalfBlocks(), halfBlockSequenceNumbers);
    }

    @Test
    public void testConstructorAddress() {
        assertEquals(ii.getPeer().getIpAddress().getHostAddress(), address);
    }

    @Test
    public void testConstructorPublicKey() {
        assertEquals(ii.getPeer().getPublicKeyPair(), publicKey);
    }

    @Test
    public void testAddHalfblock() {
        int seqBlock = 12;
        ii.addHalfBlocks(seqBlock);
        halfBlockSequenceNumbers.add(seqBlock);
        assertEquals(ii.getHalfBlocks(), halfBlockSequenceNumbers);
    }

    @Test
    public void testEquals() {
        Peer peer = new Peer(new InetSocketAddress(address,port),publicKey,userName);
        InboxItem ii2 = new InboxItem(peer, halfBlockSequenceNumbers);
        assertEquals(ii, ii2);
    }

    @Test
    public void testEqualsFalseUserName() {
        Peer peer = new Peer(new InetSocketAddress(address,port),publicKey,userName + "r");
        InboxItem ii2 = new InboxItem(peer, halfBlockSequenceNumbers);
        assertFalse(ii.equals(ii2));
    }

    @Test
    public void testEqualsFalseAddress() {
        Peer peer = new Peer(new InetSocketAddress(address + "r",port),publicKey,userName);
        InboxItem ii2 = new InboxItem(peer, halfBlockSequenceNumbers);
        assertFalse(ii.equals(ii2));
    }

    @Test
    public void testEqualsFalsePublicKey() {
        Peer peer = new Peer(new InetSocketAddress(address,port),new PublicKeyPair(new byte[] {0x00,0x01,0x02}),userName);
        InboxItem ii2 = new InboxItem(peer, halfBlockSequenceNumbers);
        assertFalse(ii.equals(ii2));
    }

    @Test
    public void testEqualsFalsePort() {
        Peer peer = new Peer(new InetSocketAddress(address,port + 12),publicKey,userName);
        InboxItem ii2 = new InboxItem(peer, halfBlockSequenceNumbers);
        assertFalse(ii.equals(ii2));
    }

    @Test
    public void testEqualsFalseHalfBlockSequenceNumbers() {
        Peer peer = new Peer(new InetSocketAddress(address,port),publicKey,userName);
        ArrayList<Integer> halfBlockSequenceNumbers2 = new ArrayList<>();
        InboxItem ii2 = new InboxItem(peer, halfBlockSequenceNumbers2);
        assertFalse(ii.equals(ii2));
    }

    @Test
    public void testSetUserName() {
        String newUserName = "random";
        ii.getPeer().setName(newUserName);
        assertEquals(ii.getPeer().getName(), newUserName);
    }

    @Test
    public void testSetPublicKey() {
        PublicKeyPair pubKeyPair = new DualSecret().getPublicKeyPair();
        ii.getPeer().setPublicKeyPair(pubKeyPair);
        assertTrue(Arrays.equals(ii.getPeer().getPublicKeyPair().toBytes(), pubKeyPair.toBytes()));
    }

    @Test
    public void testSetBlocks() {
        ArrayList<Integer> halfBlockSequenceNumbers2 = new ArrayList<>();
        halfBlockSequenceNumbers2.add(4);
        ii.setHalfBlocks(halfBlockSequenceNumbers2);
        assertEquals(ii.getHalfBlocks(), halfBlockSequenceNumbers2);
    }

    @Test
    public void testGetAmountUnread() {
        assertEquals(ii.getAmountUnread(), halfBlockSequenceNumbers.size());
    }
    @Test
    public void testGetAmountUnreadNull() {
        Peer peer = new Peer(new InetSocketAddress(address,port), publicKey,userName);
        InboxItem ii2 = new InboxItem(peer, null);
        assertEquals(ii2.getAmountUnread(),0);
    }

}
