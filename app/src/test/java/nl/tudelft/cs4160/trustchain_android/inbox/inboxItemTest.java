package nl.tudelft.cs4160.trustchain_android.inbox;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;

public class inboxItemTest extends TestCase {
    String userName;
    ArrayList<Integer> halfBlockSequenceNumbers = new ArrayList<>();
    String address;
    PublicKeyPair publicKey;
    int port;
    InboxItem ii;

    @Before
    public void setUp() {
        userName = "userName";
        halfBlockSequenceNumbers.add(1);
        halfBlockSequenceNumbers.add(2);
        halfBlockSequenceNumbers.add(3);
        address = "address";
        publicKey = new DualSecret().getPublicKeyPair();
        port = 123;
        ii = new InboxItem(userName, halfBlockSequenceNumbers, address, publicKey, port);
    }

    @Test
    public void testConstructorUserName() {
        assertEquals(ii.getUserName(), userName);
    }

    @Test
    public void testConstructorBlocks() {
        assertEquals(ii.getHalfBlocks(), halfBlockSequenceNumbers);
    }

    @Test
    public void testConstructorAddress() {
        assertEquals(ii.getAddress(), address);
    }

    @Test
    public void testConstructorPublicKey() {
        assertEquals(ii.getPublicKeyPair(), publicKey);
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
        InboxItem ii2 = new InboxItem(userName, halfBlockSequenceNumbers, address, publicKey, port);
        assertEquals(ii, ii2);
    }

    @Test
    public void testEqualsFalseUserName() {
        InboxItem ii2 = new InboxItem(userName + "r", halfBlockSequenceNumbers, address, publicKey, port);
        assertFalse(ii.equals(ii2));
    }

    @Test
    public void testEqualsFalseAddress() {
        InboxItem ii2 = new InboxItem(userName, halfBlockSequenceNumbers, address + "r", publicKey, port);
        assertFalse(ii.equals(ii2));
    }

    @Test
    public void testEqualsFalsePublicKey() {
        InboxItem ii2 = new InboxItem(userName, halfBlockSequenceNumbers, address, new PublicKeyPair(new byte[] {0x00,0x01,0x02}), port);
        assertFalse(ii.equals(ii2));
    }

    @Test
    public void testEqualsFalsePort() {
        InboxItem ii2 = new InboxItem(userName, halfBlockSequenceNumbers, address, publicKey, port + 12);
        assertFalse(ii.equals(ii2));
    }

    @Test
    public void testEqualsFalseHalfBlockSequenceNumbers() {
        ArrayList<Integer> halfBlockSequenceNumbers2 = new ArrayList<>();
        InboxItem ii2 = new InboxItem(userName, halfBlockSequenceNumbers2, address, publicKey, port);
        assertFalse(ii.equals(ii2));
    }

    @Test
    public void testSetUserName() {
        String newUserName = "random";
        ii.setUserName(newUserName);
        assertEquals(ii.getUserName(), newUserName);
    }

    @Test
    public void testSetAddress() {
        String string = "testSetAddress";
        ii.setAddress(string);
        assertEquals(ii.getAddress(), string);
    }

    @Test
    public void testSetPublicKey() {
        PublicKeyPair pubKeyPair = new DualSecret().getPublicKeyPair();
        ii.setPublicKeyPair(pubKeyPair);
        assertEquals(ii.getPublicKeyPair(), pubKeyPair);
    }

    @Test
    public void testSetPort() {
        int port = 14;
        ii.setPort(port);
        assertEquals(ii.getPort(), port);
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
        InboxItem ii2 = new InboxItem(userName, null, address, publicKey, port);
        assertEquals(ii2.getAmountUnread(),0);
    }


}
