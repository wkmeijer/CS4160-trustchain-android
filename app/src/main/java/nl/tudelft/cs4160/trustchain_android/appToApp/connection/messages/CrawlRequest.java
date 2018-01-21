package nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages;

import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;

import java.net.InetSocketAddress;
import java.util.Map;

import nl.tudelft.cs4160.trustchain_android.Util.ByteArrayConverter;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

import static nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Message.CRAWL_REQUEST;

/**
 * Created by Boning on 1/18/2018.
 */

public class CrawlRequest extends Message{

    final protected static String CRAWLREQUEST = "crawlRequest";

    public CrawlRequest(String peerId, InetSocketAddress destination, String pubKey, MessageProto.CrawlRequest request) {
        super(CRAWL_REQUEST, peerId, destination, pubKey);
        put(CRAWLREQUEST, ByteArrayConverter.bytesToHexString(request.toByteArray()));
    }

    public static Message fromMap(Map map) throws MessageException {
        String peerId = (String) map.get(PEER_ID);
        InetSocketAddress destination = Message.createMapAddress((Map) map.get(DESTINATION));
        String requestAsString = (String) map.get(CRAWLREQUEST);
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
        String requestAsString = (String) get(CRAWLREQUEST);
        MessageProto.CrawlRequest request = null;
        try {
            request = MessageProto.CrawlRequest.parseFrom(ByteArrayConverter.hexStringToByteArray(requestAsString));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return request;
    }

}
