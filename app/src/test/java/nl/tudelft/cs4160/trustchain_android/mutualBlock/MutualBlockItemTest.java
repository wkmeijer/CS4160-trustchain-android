package nl.tudelft.cs4160.trustchain_android.mutualBlock;

import junit.framework.TestCase;


import org.junit.Test;

import nl.tudelft.cs4160.trustchain_android.main.MutualBlockItem;

import static org.junit.Assert.assertNotEquals;

public class MutualBlockItemTest extends TestCase {

    String peerName = "peer";
    int seqNum = 1;
    int linkSeqNum = 2;
    String blockStatus = "Test Verified";
    String transaction = "Test Transaction";
    String testString = "Not Same";

    @Test
    public void testTwoIdenticalObjectsShouldBeEqual() {
        MutualBlockItem m1 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, transaction);
        MutualBlockItem m2 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, transaction);
        assertEquals(m1, m2);
    }

    @Test
    public void testTwoIdenticalMutualBlockShouldHaveSameHashCode() {
        MutualBlockItem m1 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, transaction);
        MutualBlockItem m2 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, transaction);
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    public void testTwoDifferentObjectsShouldNotBeEqual() {
        MutualBlockItem m1 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, transaction);
        MutualBlockItem m2 = new MutualBlockItem(testString, 0, 0, testString, testString);
        assertFalse(m1.hashCode() == m2.hashCode());
    }


    @Test
    public void testDifferntPeerName() {
        MutualBlockItem m1 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, transaction);
        MutualBlockItem m2 = new MutualBlockItem(testString, seqNum, linkSeqNum, blockStatus, transaction);
        assertNotEquals(m1, m2);
    }

    @Test
    public void testDifferentSeqNum() {
        MutualBlockItem m1 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, transaction);
        MutualBlockItem m2 = new MutualBlockItem(peerName, 5, linkSeqNum, blockStatus, transaction);
        assertNotEquals(m1, m2);
    }

    @Test
    public void testDifferentLinkSeqNum() {
        MutualBlockItem m1 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, transaction);
        MutualBlockItem m2 = new MutualBlockItem(peerName, seqNum, 99, blockStatus, transaction);
        assertNotEquals(m1, m2);
    }

    @Test
    public void testDifferentBlockStatus() {
        MutualBlockItem m1 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, transaction);
        MutualBlockItem m2 = new MutualBlockItem(peerName, seqNum, linkSeqNum, testString, transaction);
        assertNotEquals(m1, m2);
    }

    @Test
    public void testDifferentTransaction() {
        MutualBlockItem m1 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, transaction);
        MutualBlockItem m2 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, testString);
        assertNotEquals(m1, m2);
    }

    @Test
    public void testDifferentPeerNameAndSeqNum() {
        MutualBlockItem m1 = new MutualBlockItem(testString, 0, linkSeqNum, blockStatus, transaction);
        MutualBlockItem m2 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, transaction);
        assertNotEquals(m1, m2);
    }

    @Test
    public void testDifferentPeerNameAndLinkSeqNum() {
        MutualBlockItem m1 = new MutualBlockItem(testString, seqNum, 0, blockStatus, transaction);
        MutualBlockItem m2 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, transaction);
        assertNotEquals(m1, m2);
    }

    @Test
    public void testDifferentPeerNameAndBlockStatus() {
        MutualBlockItem m1 = new MutualBlockItem(testString, seqNum, linkSeqNum, testString, transaction);
        MutualBlockItem m2 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, transaction);
        assertNotEquals(m1, m2);
    }

    @Test
    public void testDifferentPeerNameAndTransAction() {
        MutualBlockItem m1 = new MutualBlockItem(testString, seqNum, linkSeqNum, blockStatus, testString);
        MutualBlockItem m2 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, transaction);
        assertNotEquals(m1, m2);
    }

    @Test
    public void testDifferentSeqNumAndLinkSeqNum() {
        MutualBlockItem m1 = new MutualBlockItem(testString, 0, 0, blockStatus, transaction);
        MutualBlockItem m2 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, transaction);
        assertNotEquals(m1, m2);
    }

    @Test
    public void testDifferentSeqNumAndBlockStatus() {
        MutualBlockItem m1 = new MutualBlockItem(testString, 0, linkSeqNum, testString, transaction);
        MutualBlockItem m2 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, transaction);
        assertNotEquals(m1, m2);
    }

    @Test
    public void testDifferentLinkSeqNumAndBlockStatus() {
        MutualBlockItem m1 = new MutualBlockItem(testString, seqNum, 0, testString, transaction);
        MutualBlockItem m2 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, transaction);
        assertNotEquals(m1, m2);
    }

    @Test
    public void testDifferentLinkSeqNumAndTransAction() {
        MutualBlockItem m1 = new MutualBlockItem(testString, seqNum, 0, blockStatus, testString);
        MutualBlockItem m2 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, transaction);
        assertNotEquals(m1, m2);
    }

    @Test
    public void testDifferentSeqNumAndTransAction() {
        MutualBlockItem m1 = new MutualBlockItem(testString, 0, linkSeqNum, blockStatus, testString);
        MutualBlockItem m2 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, transaction);
        assertNotEquals(m1, m2);
    }

    @Test
    public void testDifferentBlockStatusAndTransAction() {
        MutualBlockItem m1 = new MutualBlockItem(testString, 0, linkSeqNum, testString, testString);
        MutualBlockItem m2 = new MutualBlockItem(peerName, seqNum, linkSeqNum, blockStatus, transaction);
        assertNotEquals(m1, m2);
    }

}