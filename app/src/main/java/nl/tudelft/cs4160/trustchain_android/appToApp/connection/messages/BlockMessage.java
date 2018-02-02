package nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages;

import com.google.protobuf.InvalidProtocolBufferException;
import java.net.InetSocketAddress;
import java.util.Map;

import nl.tudelft.cs4160.trustchain_android.Util.ByteArrayConverter;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

/**
 * Created by timbu on 10/01/2018.
 */

public class BlockMessage extends Message {
    final private static String BLOCK_MESSAGE_KEY = "blockMessage";
    final private static String NEW_BLOCK = "newBlock";

    public BlockMessage(String peerId, InetSocketAddress destination, String pubKey, MessageProto.Message message, boolean isNewBlock) {
        super(BLOCK_MESSAGE_ID, peerId, destination, pubKey);
        put(BLOCK_MESSAGE_KEY, ByteArrayConverter.bytesToHexString(message.toByteArray()));
        if(isNewBlock){
            put(NEW_BLOCK,Boolean.toString(isNewBlock));
        }else{
            put(NEW_BLOCK,Boolean.toString(isNewBlock));
        }
    }

    public static Message fromMap(Map map) throws MessageException {
        String peerId = (String) map.get(PEER_ID);
        InetSocketAddress destination = Message.createMapAddress((Map) map.get(DESTINATION));
        String pubKey = (String) map.get(PUB_KEY);
        String messageAsString = (String) map.get(BLOCK_MESSAGE_KEY);
        String boolString = (String)map.get(NEW_BLOCK);
        Boolean isNewBlock = Boolean.valueOf(boolString);
        MessageProto.Message message = null;
        try {
            message = MessageProto.Message.parseFrom(ByteArrayConverter.hexStringToByteArray(messageAsString));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return new BlockMessage(peerId, destination, pubKey, message, isNewBlock);
    }

    public MessageProto.Message getMessageProto() throws MessageException {
        String messageAsString = (String) get(BLOCK_MESSAGE_KEY);
        MessageProto.Message message = null;
        try {
            message = MessageProto.Message.parseFrom(ByteArrayConverter.hexStringToByteArray(messageAsString));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return message;
    }

    public boolean isNewBlock() {
        Object isNewBlock = get(NEW_BLOCK);
        if(isNewBlock == null){
            return true;
        }else{
            return Boolean.valueOf((String)isNewBlock);
        }
    }

    public String toString() {
        return "BlockMessage{" + super.toString() + "}";
    }
}
