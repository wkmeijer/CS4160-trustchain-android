package nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.Map;

import nl.tudelft.cs4160.trustchain_android.Util.ByteArrayConverter;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

/**
 * Created by timbu on 10/01/2018.
 */

public class BlockMessage extends Message {
    final protected static String BLOCKMESSAGE = "blockMessage";

    public BlockMessage(String peerId, InetSocketAddress destination, String pubKey, MessageProto.Message message) {
        super(BLOCK_MESSAGE, peerId, destination, pubKey);
        put(BLOCKMESSAGE, ByteArrayConverter.bytesToHexString(message.toByteArray()));
    }

    public static Message fromMap(Map map) throws MessageException {
        String peerId = (String) map.get(PEER_ID);
        InetSocketAddress destination = Message.createMapAddress((Map) map.get(DESTINATION));
        String pubKey = (String) map.get(PUB_KEY);
        String messageAsString = (String) map.get(BLOCKMESSAGE);
        MessageProto.Message message = null;
        try {
            message = MessageProto.Message.parseFrom(ByteArrayConverter.hexStringToByteArray(messageAsString));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return new BlockMessage(peerId, destination, pubKey, message);
    }

    public MessageProto.Message getMessageProto() throws MessageException {
        String messageAsString = (String) get(BLOCKMESSAGE);
        MessageProto.Message message = null;
        try {
            message = MessageProto.Message.parseFrom(ByteArrayConverter.hexStringToByteArray(messageAsString));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return message;
    }

    public String toString() {
        return "BlockMessage{" + super.toString() + "}";
    }
}
