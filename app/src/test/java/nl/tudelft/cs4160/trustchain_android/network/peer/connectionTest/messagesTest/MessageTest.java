package nl.tudelft.cs4160.trustchain_android.network.peer.connectionTest.messagesTest;

import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import nl.tudelft.cs4160.trustchain_android.network.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.network.peer.connection.messages.Message;
import nl.tudelft.cs4160.trustchain_android.network.peer.connection.messages.MessageException;

import static org.junit.Assert.assertEquals;

public class MessageTest {

    InetSocketAddress dest;
    InetSocketAddress source;

    Peer peer1;
    Peer peer2;

    @Before
    public void initialization() {
        source = new InetSocketAddress("111.111.11.11", 11);
        dest = new InetSocketAddress("222.222.22.22", 22);
        peer1 = new Peer("123", new InetSocketAddress(33));
    }

    @Test
    public void testCreateAddressMap() {
        Map m = Message.createAddressMap(source);
        assertEquals(m.get("port"), (long) 11);
        assertEquals("111.111.11.11", m.get("address"));
    }

    @Test
    public void testCreatePeerMap() {
        Map m = Message.createPeerMap(peer1);
        assertEquals(m.get("port"), (long) 33);
        assertEquals("123", m.get("peer_id"));
    }

    @Test
    public void testCreateMapAddress() throws MessageException {
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("address", "444.444.44.44");
        addressMap.put("port", (long) 44);
        InetSocketAddress socketAddress = Message.createMapAddress(addressMap);
        assertEquals(44, socketAddress.getPort());
    }

    @Test
    public void testCreateMapPeer() throws MessageException{
        Map<String, Object> m = new HashMap<>();
        m.put("address", "555.555.55.55");
        m.put("port", (long) 55);
        m.put("peer_id", "567");
        Peer peer = Message.createMapPeer(m);
        assertEquals("567", peer.getPeerId());
        assertEquals(55, peer.getPort());
    }
}
