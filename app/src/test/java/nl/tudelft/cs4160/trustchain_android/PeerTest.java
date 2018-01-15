package nl.tudelft.cs4160.trustchain_android;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class PeerTest extends TestCase {
    String id1;
    String id2;
    int port;

    @Before
    public void setUp() {
        port = 11;
        id1 = "123";
        id2 = "24";
    }

    @Test
    public void testEqual() {
        Peer peer1 = new Peer(null, id1, port, "TestName");
        assertTrue(peer1.equals(peer1));
    }

    @Test
    public void testNotEqual() {
        Peer peer1 = new Peer(null, id1, port);
        Peer peer2 = new Peer(null, id2, port);
        assertFalse(peer1.equals(peer2));
    }

    @Test
    public void testToString(){
        Peer peer1 = new Peer(new byte[] {0x00}, id1, port);
        assertEquals("<Peer: [" + peer1.getPublicKey() + ":" + peer1.getPort() + ",PubKey: " + bytesToHex(peer1.getPublicKey()) + "]>"
                ,peer1.toString());
    }

    @Test
    public void testDevice() {
        Peer peer1 = new Peer(new byte[] {0x00}, id1, port);
        assertNull(peer1.getDevice());
    }

    @Test
    public void testFunctions() {
        Peer peer1 = new Peer(null, id1, port);
        byte[] pubkey = new byte[]{0x00};
        peer1.setPublicKey(pubkey);
        peer1.setIpAddress("111.111.11.11");
        peer1.setPort(12);
        peer1.setName("testName");
        assertEquals("testName", peer1.getName());
        assertEquals(12, peer1.getPort());
        assertEquals("111.111.11.11", peer1.getIpAddress());
        assertEquals(pubkey, peer1.getPublicKey());
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
