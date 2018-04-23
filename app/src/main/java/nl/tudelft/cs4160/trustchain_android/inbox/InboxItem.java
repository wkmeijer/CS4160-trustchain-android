package nl.tudelft.cs4160.trustchain_android.inbox;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;

import nl.tudelft.cs4160.trustchain_android.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;

public class InboxItem implements Serializable {
    private String userName;
    private ArrayList<Integer> halfBlockSequenceNumbers;
    private String address;
    // byte array because libsodium PublicKey is not serializable
    private byte[] publicKeyPair;
    private int port;

    /**
     * Inbox item constructor
     * @param userName the username
     * @param halfBlockSequenceNumbers the list of the sequence numbers of the unread blocks
     * @param address ip address
     * @param publicKeyPair
     * @param port
     */
    public InboxItem(String userName, ArrayList<Integer> halfBlockSequenceNumbers, String address, PublicKeyPair publicKeyPair, int port) {
        this.userName = userName;
        this.halfBlockSequenceNumbers = halfBlockSequenceNumbers;
        this.address = address;
        this.publicKeyPair = publicKeyPair.toBytes();
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ArrayList<Integer> getHalfBlocks() {
        return halfBlockSequenceNumbers;
    }

    public void addHalfBlocks(Integer block) {
        halfBlockSequenceNumbers.add(block);
    }

    public void setHalfBlocks(ArrayList<Integer> halfBlocks) {
        this.halfBlockSequenceNumbers = halfBlocks;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public PublicKeyPair getPublicKeyPair() {
        return new PublicKeyPair(publicKeyPair);
    }

    public void setPublicKeyPair(PublicKeyPair publicKeyPair) {
        this.publicKeyPair = publicKeyPair.toBytes();
    }

    /**
     * return the amount of unread blocks
     * @return
     */
    public int getAmountUnread() {
        if (halfBlockSequenceNumbers != null) {
            return halfBlockSequenceNumbers.size();
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InboxItem inboxItem = (InboxItem) o;

        if (port != inboxItem.port) return false;
        if (userName != null ? !userName.equals(inboxItem.userName) : inboxItem.userName != null)
            return false;
        if (halfBlockSequenceNumbers != null ? !halfBlockSequenceNumbers.equals(inboxItem.halfBlockSequenceNumbers) : inboxItem.halfBlockSequenceNumbers != null)
            return false;
        if (address != null ? !address.equals(inboxItem.address) : inboxItem.address != null)
            return false;
        return publicKeyPair != null ? Arrays.equals(publicKeyPair, inboxItem.publicKeyPair) : inboxItem.publicKeyPair == null;
    }

    public Peer getPeerAppToApp(){
       return new Peer(userName, new InetSocketAddress(address,port));
    }

}
