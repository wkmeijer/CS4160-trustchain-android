package nl.tudelft.cs4160.trustchain_android;

import android.bluetooth.BluetoothDevice;

import java.util.Arrays;

import nl.tudelft.cs4160.trustchain_android.Util.ByteArrayConverter;

/**
 * The peer object that is used to identify a TrustChain user.
 * The peer is identified by its unique public key, which is generated.
 * Created by wkmeijer on 20-10-17.
 */
public class Peer {
    private byte[] publicKey;
    private String ipAddress;
    private int port;
    private BluetoothDevice device;
    private String name;

    /**
     * Create a peer with a specific name.
     * @param pubKey
     * @param ip
     * @param port
     * @param name
     */
    public Peer(byte[] pubKey, String ip, int port, String name) {
        this.publicKey = pubKey;
        this.ipAddress = ip;
        this.port = port;
        this.name = name;
    }

    /**
     * If no name is provided, the ip will be used as name.
     * @param pubKey
     * @param ip
     * @param port
     */
    public Peer(byte[] pubKey, String ip, int port) {
        this.publicKey = pubKey;
        this.ipAddress = ip;
        this.port = port;
        this.name = ip;
    }

    public Peer(BluetoothDevice device) {
        this.device = device;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String toString() {
        String res = "<Peer: [";
        res += publicKey + ":" + port + ",PubKey: " + ByteArrayConverter.bytesToHexString(publicKey) + "]>";
        return res;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Peer peer = (Peer) o;

        if (port != peer.port) return false;
        if (!Arrays.equals(publicKey, peer.publicKey)) return false;
        if (!ipAddress.equals(peer.ipAddress)) return false;
        return device != null ? device.equals(peer.device) : peer.device == null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
