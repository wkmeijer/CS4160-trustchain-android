package nl.tudelft.cs4160.trustchain_android.inbox;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

/**
 * Created by timbu on 08/01/2018.
 */

public class InboxItem implements Serializable{
    private String userName;
    private ArrayList<MessageProto.TrustChainBlock> halfBlocks;
    private String address;
    private String publicKey;

    public InboxItem(String userName, ArrayList<MessageProto.TrustChainBlock> halfBlocks, String address, String publicKey) {
        this.userName = userName;
        this.halfBlocks = halfBlocks;
        this.address = address;
        this.publicKey = publicKey;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ArrayList<MessageProto.TrustChainBlock> getHalfBlocks() {
        return halfBlocks;
    }
    public void addHalfBlocks(MessageProto.TrustChainBlock block) {
        halfBlocks.add(block);
    }

    public void setHalfBlocks(ArrayList<MessageProto.TrustChainBlock> halfBlocks) {
        this.halfBlocks = halfBlocks;
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
        if (halfBlocks != null) {
            return halfBlocks.size();
        }
        return 0;
    }

}
