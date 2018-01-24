package nl.tudelft.cs4160.trustchain_android.appToAppTest.connectionTest.messagesTest;

import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Message;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.MessageException;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Puncture;

import static org.junit.Assert.assertEquals;

/**
 * Created by Boning on 12/18/2017.
 */

public class PunctureTest {

    Puncture p;
    InetSocketAddress dest;
    InetSocketAddress source;
    Map<String, Object> m;
    Map<String, Object> sourceAddressMap = new HashMap<>();
    Map<String, Object> destAddressMap = new HashMap<>();

    @Before
    public void initialization(){
        source = new InetSocketAddress("111.111.11.11", 11);
        dest = new InetSocketAddress("222.222.22.22", 22);
        m = new HashMap<>();
        m.put("peer_id", "34");

        sourceAddressMap.put("address", "111.111.11.11");
        sourceAddressMap.put("port", (long) 11);
        m.put("source", sourceAddressMap);

        destAddressMap.put("address", "222.222.22.22");
        destAddressMap.put("port", (long) 22);
        m.put("destination", destAddressMap);
    }

    @Test
    public void testToString(){
        p = new Puncture("123", dest, source, "hhdgwhfdgw");
        assertEquals("Puncture{{public_key=hhdgwhfdgw, destination={address=222.222.22.22, port=22}, source={address=111.111.11.11, port=11}, type=4, peer_id=123}}",
                p.toString());
    }

    @Test
    public void testFromMap() throws MessageException {
        Message message = Puncture.fromMap(m);
        assertEquals(4, message.getType());
        assertEquals("34", message.getPeerId());
        assertEquals(dest, message.getDestination());
    }

}
