package nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.ByteBufferOutputStream;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.ByteBufferinputStream;
import nl.tudelft.cs4160.trustchain_android.bencode.BencodeReadException;
import nl.tudelft.cs4160.trustchain_android.bencode.BencodeReader;
import nl.tudelft.cs4160.trustchain_android.bencode.BencodeWriter;

/**
 * Created by jaap on 5/31/16.
 */
public abstract class Message extends HashMap {
    public final static int INTRODUCTION_REQUEST_ID = 1;
    public final static int INTRODUCTION_RESPONSE_ID = 2;
    public final static int PUNCTURE_REQUEST_ID = 3;
    public final static int PUNCTURE_ID = 4;
    public final static int BLOCK_MESSAGE_ID = 5;
    public final static int CRAWL_REQUEST_ID = 6;

    final protected static String TYPE = "type";
    final protected static String DESTINATION = "destination";

    final protected static String PORT = "port";
    final protected static String ADDRESS = "address";
    final protected static String PEER_ID = "peer_id";
    final protected static String PUB_KEY = "public_key";

    /**
     * Create a message.
     * @param type the message type.
     * @param peerId the unique id of self.
     * @param destination the destination address.
     */
    public Message(int type, String peerId, InetSocketAddress destination, String pubKey) {
        put(TYPE, type);
        put(PEER_ID, peerId);
        put(DESTINATION, createAddressMap(destination));
        put(PUB_KEY, pubKey);
    }

    /**
     * Create a message from a stream.
     * @param stream the stream to create the message from.
     * @return the created message.
     * @throws IOException
     * @throws BencodeReadException
     * @throws MessageException
     */
    public static Message createFromStream(InputStream stream) throws IOException, BencodeReadException, MessageException {
        BencodeReader reader = new BencodeReader(stream);
        Map<String, Object> dict = reader.readDict();
        if (!dict.containsKey(TYPE)) {
            Log.d("App-To-App Log", "Dictionary " + dict + " doesn't contain type");
            throw new MessageException("Invalid message");
        }
        int messageType = (int) (long) dict.get(TYPE);
        switch (messageType) {
            case INTRODUCTION_REQUEST_ID:
                return IntroductionRequest.fromMap(dict);
            case INTRODUCTION_RESPONSE_ID:
                return IntroductionResponse.fromMap(dict);
            case PUNCTURE_ID:
                return Puncture.fromMap(dict);
            case PUNCTURE_REQUEST_ID:
                return PunctureRequest.fromMap(dict);
            case BLOCK_MESSAGE_ID:
                return BlockMessage.fromMap(dict);
            case CRAWL_REQUEST_ID:
                return CrawlRequest.fromMap(dict);
            default:
                throw new MessageException("Unknown message");
        }
    }

    /**
     * Create a message from a {@link ByteBuffer}.
     * @param buffer the buffer to create the message from.
     * @return the message.
     * @throws BencodeReadException
     * @throws IOException
     * @throws MessageException
     */
    public static Message createFromByteBuffer(ByteBuffer buffer) throws BencodeReadException, IOException, MessageException {
        return createFromStream(new ByteBufferinputStream(buffer));
    }

    /**
     * Create a map from an address for sending.
     * @param address the address.
     * @return the map.
     */
    public static Map createAddressMap(InetSocketAddress address) {
        Map map = new HashMap();
        map.put(PORT, (long) address.getPort());
        map.put(ADDRESS, address.getAddress().getHostAddress());
        return map;
    }

    /**
     * Create a map from a peer for sending.
     * @param peer the peer.
     * @return the map.
     */
    public static Map createPeerMap(PeerAppToApp peer) {
        Map map = new HashMap();
        InetSocketAddress address = peer.getAddress();
        map.put(PORT, (long) address.getPort());
        map.put(ADDRESS, address.getAddress().getHostAddress());
        if (peer.getPeerId() != null) map.put(PEER_ID, peer.getPeerId());
        return map;
    }

    /**
     * Create an address from a map.
     * @param map the map.
     * @return the address.
     * @throws MessageException
     */
    public static InetSocketAddress createMapAddress(Map map) throws MessageException {
        if (!map.containsKey(PORT) || !map.containsKey(ADDRESS)) throw new MessageException("Invalid address map");

        int port = (int) (long) map.get(PORT);
        String address = (String) map.get(ADDRESS);
        return new InetSocketAddress(address, port);
    }

    /**
     * Create a peer from a map.
     * @param map the map.
     * @return the peer.
     * @throws MessageException
     */
    public static PeerAppToApp createMapPeer(Map map) throws MessageException {
        if (!map.containsKey(PORT) || !map.containsKey(ADDRESS)) throw new MessageException("Invalid address map");

        int port = (int) (long) map.get(PORT);
        String address = (String) map.get(ADDRESS);
        String peerId = null;
        if (map.containsKey(PEER_ID)) peerId = (String) map.get(PEER_ID);
        return new PeerAppToApp(peerId, new InetSocketAddress(address, port));
    }

    public InetSocketAddress getDestination() throws MessageException {
        return createMapAddress((Map) get(DESTINATION));
    }

    /**
     * Write this message to a stream.
     * @param out the stream to write the message to.
     * @throws IOException
     */
    public void writeToStream(OutputStream out) throws IOException {
        BencodeWriter writer = new BencodeWriter(out);
        writer.write(this);
    }

    /**
     * Write this message to a buffer.
     * @param buffer the buffer to write the message to.
     * @throws IOException
     */
    public void writeToByteBuffer(ByteBuffer buffer) throws IOException {
        buffer.clear();
        writeToStream(new ByteBufferOutputStream(buffer));
    }

    /**
     * Get the peer id associated with this message.
     * @return the id.
     */
    public String getPeerId() {
        return (String) get(PEER_ID);
    }

    public String getPubKey() {
        return (String) get(PUB_KEY);
    }

    public void putPubKey(String pubkey) {
        put(PUB_KEY, pubkey);
    }

    /**
     * Get the type of this message.
     * @return the type.
     */
    public int getType() {
        return (int) get(TYPE);
    }
}
