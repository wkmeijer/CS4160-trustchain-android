package nl.tudelft.cs4160.trustchain_android.appToAppTest.connectionTest.messagesTest;

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
 * Created by Boning on 12/18/2017.
 */

public class IntroductionRequestTest {

    IntroductionRequest req;
    Map<String, Object> m;
    Map<String, Object> dest;

    @Before
    public void initialization(){
        InetSocketAddress dest = new InetSocketAddress("111.111.11.11", 11);
        req = new IntroductionRequest("123", dest, (long) 1, "WIFI", "jshjsjgwduhw");
    }

    @Test
    public void testToString(){
        assertEquals("IntroductionRequest{{public_key=jshjsjgwduhw, network_operator=WIFI, connection_type=1, destination={address=111.111.11.11, port=11}, type=1, peer_id=123}}",
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
