package nl.tudelft.cs4160.trustchain_android.appToApp;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * The peer object. The peer is identified by its unique peer id and keeps track of the last send and receive time.
 * <p/>
 * Created by jaap on 5/19/16.
 */
public class PeerAppToApp implements Serializable {
    public final static boolean INCOMING = true;
    public final static boolean OUTGOING = false;

    final private static int TIMEOUT = 20000;
    private InetSocketAddress address;
    private String peerId;
    private boolean hasReceivedData = false;
    private boolean hasSentData = false;
    private int connectionType;
    private String networkOperator;
    private long lastSendTime;
    private long lastReceiveTime;
    private long creationTime;


    /**
     * Create a peer.
     *
     * @param peerId  its unique id.
     * @param address its address.
     */
    public PeerAppToApp(String peerId, InetSocketAddress address) {
        this.peerId = peerId;
        this.address = address;
        this.lastSendTime = System.currentTimeMillis();
        this.creationTime = System.currentTimeMillis();
    }

    public long getCreationTime() {
        return creationTime;
    }

    public String getNetworkOperator() {
        return networkOperator;
    }

    public void setNetworkOperator(String networkOperator) {
        this.networkOperator = networkOperator;
    }

    public int getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(int connectionType) {
        this.connectionType = connectionType;
    }


    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public boolean hasReceivedData() {
        return hasReceivedData;
    }

    public int getPort() {
        return address.getPort();
    }

    public InetAddress getExternalAddress() {
        return address.getAddress();
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }


    /**
     * Method called when data is sent to this peer.
     */
    public void sentData() {
        hasSentData = true;
        lastSendTime = System.currentTimeMillis();
    }

    /**
     * Method called when data is received.
     *
     * @param buffer the received data.
     */
    public void received(ByteBuffer buffer) {
        hasReceivedData = true;
        lastReceiveTime = System.currentTimeMillis();
    }

    /**
     * Calculates whether this peer is alive: the peer is alive when the peer hasn't send data yet, or when data is received within the
     * timeout after sending data.
     *
     * @return
     */
    public boolean isAlive() {
        if (hasSentData) {
            if (System.currentTimeMillis() - lastSendTime < TIMEOUT) return true;
            return hasReceivedData && lastReceiveTime > lastSendTime;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Peer{" +
                "address=" + address +
                ", peerId='" + peerId + '\'' +
                ", hasReceivedData=" + hasReceivedData +
                ", connectionType=" + connectionType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PeerAppToApp peer = (PeerAppToApp) o;

        if (address != null ? !address.equals(peer.address) : peer.address != null) return false;
        return peerId != null ? peerId.equals(peer.peerId) : peer.peerId == null;

    }

    public boolean isHasSentData() {
        return hasSentData;
    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + (peerId != null ? peerId.hashCode() : 0);
        return result;
    }

    public long getLastSendTime() {
        return lastSendTime;
    }

    public long getLastReceiveTime() {
        return lastReceiveTime;
    }
}
