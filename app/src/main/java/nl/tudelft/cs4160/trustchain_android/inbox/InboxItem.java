package nl.tudelft.cs4160.trustchain_android.inbox;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

/**
 * Created by timbu on 08/01/2018.
 */

public class InboxItem implements Serializable {
    private String userName;
    private ArrayList<Integer> halfBlockSequenceNumbers;
    private String address;
    private String publicKey;
    private int port;

    public InboxItem(String userName, ArrayList<Integer> halfBlockSequenceNumbers, String address, String publicKey, int port) {
        this.userName = userName;
        this.halfBlockSequenceNumbers = halfBlockSequenceNumbers;
        this.address = address;
        this.publicKey = publicKey;
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

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public int getAmountUnread() {
        if (halfBlockSequenceNumbers != null) {
            return halfBlockSequenceNumbers.size();
        }
        return 0;
    }

}
