package nl.tudelft.cs4160.trustchain_android.appToApp.connectionTest.messagesTest;

import com.google.protobuf.InvalidProtocolBufferException;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.CrawlRequest;

/**
 * Created by Boning on 1/27/2018.
 */

public class CrawlRequestTest {

    CrawlRequest cr;
    Map<String, Object> m;
    Map<String, Object> dest;

    @Before
    public void initialization() throws InvalidProtocolBufferException{
//        InetSocketAddress dest = new InetSocketAddress("111.111.11.11", 11);
//        MessageProto.CrawlRequest mcr = MessageProto.CrawlRequest.parseFrom(ByteArrayConverter.hexStringToByteArray("49204c6f7665204a61766121"));
//        System.out.println(ByteArrayConverter.hexStringToByteArray("49204c6f7665204a61766121").toString());
//        cr = new CrawlRequest("123", dest, "jshjsjgwduhw", mcr);
    }

    @Test
    public void testToString(){
//        assertTrue(cr.toString().length() > 0);
    }
}
