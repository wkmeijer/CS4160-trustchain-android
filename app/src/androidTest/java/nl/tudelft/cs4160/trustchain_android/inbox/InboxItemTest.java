package nl.tudelft.cs4160.trustchain_android.inbox;


import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.libsodium.jni.NaCl;

import java.util.ArrayList;
import java.util.Arrays;

import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;
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
        assertTrue(Arrays.equals(ii.getPublicKeyPair().toBytes(), publicKey.toBytes()));
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
        assertTrue(Arrays.equals(ii.getPublicKeyPair().toBytes(), pubKeyPair.toBytes()));
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
