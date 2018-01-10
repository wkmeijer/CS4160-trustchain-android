package nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages;

import java.net.InetSocketAddress;
import java.util.Map;

import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

/**
 * Created by timbu on 10/01/2018.
 */

public class BlockMessage extends Message {
    final protected static String BLOCKMESSAGE = "blockMessage";

    public BlockMessage(String peerId, InetSocketAddress destination, String pubKey, MessageProto.Message message) {
        super(PUNCTURE, peerId, destination, pubKey);
        put(BLOCKMESSAGE, message);
    }

    public static Message fromMap(Map map) throws MessageException {
        String peerId = (String) map.get(PEER_ID);
        InetSocketAddress destination = Message.createMapAddress((Map) map.get(DESTINATION));
        String pubKey = (String) map.get(PUB_KEY);
        MessageProto.Message message = (MessageProto.Message) map.get(BLOCKMESSAGE);
        return new BlockMessage(peerId, destination, pubKey, message);
    }

    public MessageProto.Message getMessageProto() throws MessageException {
        Object o = get(BLOCKMESSAGE);
        return (MessageProto.Message) o;
    }


    public String toString() {
        return "BlockMessage{" + super.toString() + "}";
    }
}
