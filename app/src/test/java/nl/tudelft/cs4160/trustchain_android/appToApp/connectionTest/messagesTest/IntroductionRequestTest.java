<<<<<<< HEAD:app/src/test/java/nl/tudelft/cs4160/trustchain_android/appToAppTest/connectionTest/messagesTest/IntroductionRequestTest.java
package nl.tudelft.cs4160.trustchain_android.appToAppTest.connectionTest.messagesTest;
=======
package nl.tudelft.cs4160.trustchain_android.appToApp.connectionTest.messagesTest;
>>>>>>> 29aa341a74003f041d63668cec12d1f42177436b:app/src/test/java/nl/tudelft/cs4160/trustchain_android/appToApp/connectionTest/messagesTest/IntroductionRequestTest.java

import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.IntroductionRequest;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Message;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.MessageException;

import static junit.framework.Assert.assertTrue;
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
        assertTrue(req.toString().length() > 0);
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
