package nl.tudelft.cs4160.trustchain_android.peer;

import com.google.protobuf.ByteString;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;
import nl.tudelft.cs4160.trustchain_android.main.OverviewConnectionsActivity;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

/**
 * The peer object that is used to find other connected peers in the network.
 * The peer is identified by its unique public key pair, and keeps track of the last sent and received time.
 */
public class Peer implements Serializable {
    final private static int TIMEOUT = 15000;
    final private static int REMOVE_TIMEOUT = 25000;
    private MessageProto.Peer protoPeer;
    private long lastSentTime = -1;
    private long lastReceiveTime = -1;
    private long creationTime;

    /**
     * Create a peer, for consistency protocolbuffers is used for this too, instead of simply serializing this.
     * @param address the address where the peer is located
     * @param publicKeyPair the public key pair of the peer
     * @param name  the nickname of the peer
     */
    public Peer(InetSocketAddress address, PublicKeyPair publicKeyPair, String name) {
        ByteString ipAddress = null;
        int port = 0;
        ByteString publicKey = ByteString.EMPTY;
        if(address != null && address.getAddress() != null) {
            ipAddress = ByteString.copyFrom(address.getAddress().getAddress());
            port = address.getPort();
        }
        if(publicKeyPair != null) {
            publicKey = ByteString.copyFrom(publicKeyPair.toBytes());
        }
        if(name == null) {
            name = "";
        }

        protoPeer = MessageProto.Peer.newBuilder()
                .setAddress(ipAddress)
                .setPort(port)
                .setPublicKey(publicKey)
                .setName(name)
                .build();

        creationTime = System.currentTimeMillis();
    }

    /**
     * Create a local peer from a received protocolbuffer format peer.
     * @param protoPeer
     */
    public Peer(MessageProto.Peer protoPeer) {
        this.protoPeer = protoPeer;
        creationTime = System.currentTimeMillis();
    }

    /**
     * Method called when data is sent to this peer.
     */
    public void sentData() {
        lastSentTime = System.currentTimeMillis();
    }

    /**
     * Method called when data is received from this peer.
     */
    public void receivedData() {
        lastReceiveTime = System.currentTimeMillis();
    }

    /**
     * If a peer has sent data, but the last time it has sent is longer ago than the remove timeout, it can be removed.
     * If we are trying to connect to a peer, but we haven't gotten a response within the given timeout, it can be removed.
     * Never remove the bootstrap peer.
     * @return
     */
    public boolean canBeRemoved() {
        if(isBootstrap()) {
            return false;
        }
        if (isReceivedFrom()) {
            return System.currentTimeMillis() - lastReceiveTime > REMOVE_TIMEOUT;
        }
        if (isSentTo()) {
            return System.currentTimeMillis() - creationTime > REMOVE_TIMEOUT;
        }
        return false;
    }

    /**
     * Calculates whether this peer is alive: the peer is alive when the peer hasn't send data yet, or when data is received within the
     * timeout after sending data.
     *
     * @return
     */
    public boolean isAlive() {
        if (isReceivedFrom()) {
            return System.currentTimeMillis() - lastReceiveTime < TIMEOUT;
        }
        return true;
    }

    /**
     * Checks if this peer is the bootstrap address.
     * @return
     */
    public boolean isBootstrap() {
        return OverviewConnectionsActivity.CONNECTABLE_ADDRESS.equals(getIpAddress().getHostAddress());
    }

    /**
     * If the lastReceivedTime set is set to some time we have received something
     * @return
     */
    public boolean isReceivedFrom() {
        return getLastReceivedTime() != -1;
    }

    /**
     * If the lastSentTime set is set to some time we have sent something
     * @return
     */
    public boolean isSentTo() {
        return getLastSentTime() != -1;
    }

    public MessageProto.Peer getProtoPeer() {
        return protoPeer;
    }

    public long getLastSentTime() {
        return lastSentTime;
    }

    public long getLastReceivedTime() {
        return lastReceiveTime;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public InetSocketAddress getAddress() {
        return new InetSocketAddress(getIpAddress(),getPort());
    }

    public void setAddress(InetSocketAddress address) {
        ByteString ipAddress = ByteString.EMPTY;
        int port = 0;
        if(address != null) {
            ipAddress = ByteString.copyFrom(address.getAddress().getAddress());
            port = address.getPort();
        }

        protoPeer = protoPeer.toBuilder().setAddress(ipAddress).setPort(port).build();
    }

    public InetAddress getIpAddress() {
        try {
            return InetAddress.getByAddress(protoPeer.getAddress().toByteArray());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getPort() {
        return protoPeer.getPort();
    }

    public PublicKeyPair getPublicKeyPair() {
        try {
            return new PublicKeyPair(protoPeer.getPublicKey().toByteArray());
        } catch(Exception e) {
            // return null when formatted wrong
            return null;
        }
    }

    public void setPublicKeyPair(PublicKeyPair publicKeyPair) {
        protoPeer = protoPeer.toBuilder().setPublicKey(ByteString.copyFrom(publicKeyPair.toBytes())).build();
    }

    public String getName() {
        return protoPeer.getName();
    }

    public void setName(String name) {
        protoPeer = protoPeer.toBuilder().setName(name).build();
    }

    public int getConnectionType() {
        return protoPeer.getConnectionType();
    }

    public void setConnectionType(int connectionType) {
        protoPeer = protoPeer.toBuilder().setConnectionType(connectionType).build();
    }

    @Override
    public String toString() {
        return "Peer{" +
                "address=" + getAddress() +
                ", name='" + getName() + '\'' +
                ", isReceivedFrom=" + isReceivedFrom() +
                ", connectionType=" + getConnectionType() +
                ", publicKeyPair=" + getPublicKeyPair() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Peer peer = (Peer) o;

        if (getAddress() != null ? !getAddress().equals(peer.getAddress()) : peer.getAddress() != null) return false;
        return getPublicKeyPair() != null ? getPublicKeyPair().equals(peer.getPublicKeyPair()) : peer.getPublicKeyPair() == null;

    }

    @Override
    public int hashCode() {
        int result = getAddress() != null ? getAddress().hashCode() : 0;
        result = 31 * result + (getPublicKeyPair() != null ? getPublicKeyPair().hashCode() : 0);
        return result;
    }
}
