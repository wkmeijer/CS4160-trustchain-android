package nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages;

import com.google.protobuf.InvalidProtocolBufferException;

import java.net.InetSocketAddress;
import java.util.Map;

import nl.tudelft.cs4160.trustchain_android.Util.ByteArrayConverter;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

/**
 * Created by Boning on 1/18/2018.
 */

public class CrawlRequest extends Message{

    final private static String CRAWL_REQUEST_KEY = "crawlRequest";

    public CrawlRequest(String peerId, InetSocketAddress destination, String pubKey, MessageProto.CrawlRequest request) {
        super(CRAWL_REQUEST_ID, peerId, destination, pubKey);
        put(CRAWL_REQUEST_KEY, ByteArrayConverter.bytesToHexString(request.toByteArray()));
    }

    public static Message fromMap(Map map) throws MessageException {
        String peerId = (String) map.get(PEER_ID);
        InetSocketAddress destination = Message.createMapAddress((Map) map.get(DESTINATION));
        String requestAsString = (String) map.get(CRAWL_REQUEST_KEY);
        String pubKey = (String) map.get(PUB_KEY);
        MessageProto.CrawlRequest request = null;

        try {
            request = MessageProto.CrawlRequest.parseFrom(ByteArrayConverter.hexStringToByteArray(requestAsString));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return new CrawlRequest(peerId, destination, pubKey, request);
    }

    public MessageProto.CrawlRequest getCrawlRequest() throws MessageException {
        String requestAsString = (String) get(CRAWL_REQUEST_KEY);
        MessageProto.CrawlRequest request = null;
        try {
            request = MessageProto.CrawlRequest.parseFrom(ByteArrayConverter.hexStringToByteArray(requestAsString));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return request;
    }

}
