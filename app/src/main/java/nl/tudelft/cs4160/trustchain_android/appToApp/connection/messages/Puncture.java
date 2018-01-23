package nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Created by jaap on 5/31/16.
 */
public class Puncture extends Message {
    final private static String SOURCE = "source";

    public Puncture(String peerId, InetSocketAddress destination, InetSocketAddress source, String pubKey) {
        super(PUNCTURE_ID, peerId, destination, pubKey);
        put(SOURCE, createAddressMap(source));
    }

    public static Message fromMap(Map map) throws MessageException {
        String peerId = (String) map.get(PEER_ID);
        InetSocketAddress source = Message.createMapAddress((Map) map.get(SOURCE));
        InetSocketAddress destination = Message.createMapAddress((Map) map.get(DESTINATION));
        String pubKey = (String) map.get(PUB_KEY);
        return new Puncture(peerId, destination, source, pubKey);
    }

    public String toString() {
        return "Puncture{" + super.toString() + "}";
    }
}
