package nl.tudelft.cs4160.trustchain_android.appToAppTest.connectionTest.messagesTest;

/**
 * Created by michiel on 27-1-2018.
 */

import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.IntroductionRequest;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.IntroductionResponse;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Message;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.MessageException;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.IntroductionRequest;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Message;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.MessageException;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Puncture;

import static org.junit.Assert.assertEquals;

/**
 * Created by Michiel on 12/18/2017.
 */

public class IntroductionResponseTest {

    IntroductionResponse req;
    Map<String, Object> m;
    Map<String, Object> dest;

    @Before
    public void initialization(){
        InetSocketAddress dest = new InetSocketAddress("111.111.11.11", 11);
        PeerAppToApp peerAppToApp = new PeerAppToApp("peer", dest);
        req = new IntroductionResponse("123", dest, dest, peerAppToApp, (long) 1, new ArrayList<PeerAppToApp>(), "WIFI", "jshjsjgwduhw");
    }

    @Test
    public void testToString(){
        assertEquals("IntroductionResponse{{public_key=jshjsjgwduhw, network_operator=WIFI, pex=[], connection_type=1, destination={address=111.111.11.11, port=11}, type=2, internal_source={address=111.111.11.11, port=11}, peer_id=123, invitee={address=111.111.11.11, port=11, peer_id=peer}}}",
                req.toString());
    }

    @Test
    public void testGetters() {
        assertEquals("WIFI", req.getNetworkOperator());
        assertEquals((long) 1, req.getConnectionType());
    }

    @Test
    public void testFromMap() throws MessageException {
        m = new HashMap<>();
        m.put("peer_id", "345");
        m.put("connection_type", (long) 1);
        m.put("network_operator", "WIFI");

        dest = new HashMap<>();
        dest.put("address", "222.222.22.22");
        dest.put("port", (long) 22);

        m.put("destination", dest);

        Message message = IntroductionRequest.fromMap(m);
        assertEquals(1, message.getType());
        assertEquals("345", message.getPeerId());
    }

}

