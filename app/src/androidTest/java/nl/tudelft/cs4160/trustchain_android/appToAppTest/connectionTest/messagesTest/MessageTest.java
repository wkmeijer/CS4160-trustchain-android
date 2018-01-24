package nl.tudelft.cs4160.trustchain_android.appToAppTest.connectionTest.messagesTest;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Message;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.MessageException;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Puncture;
import nl.tudelft.cs4160.trustchain_android.bencode.BencodeReadException;

import static org.junit.Assert.assertEquals;

/**
 * Created by Boning on 12/18/2017.
 */

public class MessageTest {

    InetSocketAddress dest;
    InetSocketAddress source;

    PeerAppToApp peer1;
    PeerAppToApp peer2;

    @Before
    public void initialization() {
        source = new InetSocketAddress("111.111.11.11", 11);
        dest = new InetSocketAddress("222.222.22.22", 22);
        peer1 = new PeerAppToApp("123", new InetSocketAddress(33));
    }

    @Test
    public void testCreateAddressMap() {
        Map<String, Object> m = Message.createAddressMap(source);
        assertEquals(m.get("port"), (long) 11);
        assertEquals("111.111.11.11", m.get("address"));
    }

    @Test
    public void testCreatePeerMap() {
        Map<String, Object> m = Message.createPeerMap(peer1);
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
        PeerAppToApp peer = Message.createMapPeer(m);
        assertEquals("567", peer.getPeerId());
        assertEquals(55, peer.getPort());
    }
}
