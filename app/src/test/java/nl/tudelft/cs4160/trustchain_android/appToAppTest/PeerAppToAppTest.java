package nl.tudelft.cs4160.trustchain_android.appToAppTest;

import android.util.Log;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

public class PeerAppToAppTest extends TestCase {
    String id1;
    String id2;
    InetSocketAddress address;

    @Before
    public void setUp() {
        address = new InetSocketAddress(11);
        id1 = "123";
        id2 = "24";
    }

    @Test
    public void testEqual() {
        PeerAppToApp peer1 = new PeerAppToApp(id1, address);
        PeerAppToApp peer2 = new PeerAppToApp(id1, address);
        assertTrue(peer1.equals(peer2));
    }

    @Test
    public void testNotEqual() {
        PeerAppToApp peer1 = new PeerAppToApp(id1, address);
        PeerAppToApp peer2 = new PeerAppToApp(id2, address);
        assertFalse(peer1.equals(peer2));
    }

    @Test
    public void testCreationTime(){
        PeerAppToApp peer1 = new PeerAppToApp(id1, address);
        try {
            TimeUnit.SECONDS.sleep(1);
        }catch (Exception e){
            e.printStackTrace();
        }
        PeerAppToApp peer2 = new PeerAppToApp(id2, address);
        assertFalse(peer1.getCreationTime() == peer2.getCreationTime());
        assertTrue(peer1.getCreationTime() < System.currentTimeMillis());
    }

    @Test
    public void testNetworkOperator(){
        PeerAppToApp peer1 = new PeerAppToApp(id1, address);
        peer1.setNetworkOperator("NoVODAFONE");
        assertTrue(peer1.getNetworkOperator().equals("NoVODAFONE"));
    }

    @Test
    public void testHasReceivedData(){
        PeerAppToApp peer1 = new PeerAppToApp("firstPEER", address);
        ByteBuffer buf = ByteBuffer.allocate(100);
        assertFalse(peer1.hasReceivedData());
        peer1.received(buf);
        assertTrue(peer1.hasReceivedData());
    }

    @Test
    public void testToString(){
        PeerAppToApp peer1 = new PeerAppToApp("firstPEER", address);
        peer1.setConnectionType(1);
        assertEquals("Peer{" + "address=" + address + ", peerId='" + "firstPEER" + '\'' +
                        ", hasReceivedData=" + false + ", connectionType=" + 1 + '}'
                ,peer1.toString());
    }

    @Test
    public void testChangeParameters() {
        PeerAppToApp peer1 = new PeerAppToApp("firstPEER", address);
        peer1.setConnectionType(1);
        assertEquals(1, peer1.getConnectionType());
        peer1.setPeerId("PEER");
        assertEquals("PEER", peer1.getPeerId());
        peer1.setAddress(new InetSocketAddress("host", 11));
        assertEquals(new InetSocketAddress("host", 11), peer1.getAddress());
        assertNull(peer1.getExternalAddress());
    }

    @Test
    public void testSendData(){
        PeerAppToApp peer1 = new PeerAppToApp("firstPEER", address);
        assertTrue(peer1.isAlive());
        long lastSendTime = peer1.getLastSendTime();
        peer1.sentData();
        assertNotSame(lastSendTime, peer1.getLastSendTime());
        assertTrue(peer1.isAlive());
    }

    @Test
    public void testReceiveData(){
        PeerAppToApp peer1 = new PeerAppToApp("firstPEER", address);
        ByteBuffer buf = ByteBuffer.allocate(100);
        long lastReceivedTime = peer1.getLastReceiveTime();
        peer1.received(buf);
        assertNotSame(lastReceivedTime, peer1.getLastReceiveTime());
    }

    @Test
    public void testHashCode() {
        PeerAppToApp peer1 = new PeerAppToApp("firstPEER", address);
        assertEquals(132867431, peer1.hashCode());
    }
}
