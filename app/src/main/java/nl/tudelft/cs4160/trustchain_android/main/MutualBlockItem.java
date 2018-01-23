package nl.tudelft.cs4160.trustchain_android.main;

import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

/**
 * Class that is used to define a MutualBlock in the recycler view.
 */
public class MutualBlockItem {
    private String peerName;
    private int seqNum;
    private int linkSeqNum;
    private String blockStatus;
    private String transaction;
    private MessageProto.TrustChainBlock block;

    /**
     * Constructor.
     * @param peerName the username of the peer that the user is communicating with.
     * @param blockStatus the status of the block.
     * @param transaction the content(the message) of the block.
     */
    public MutualBlockItem(String peerName, int seqNum, int linkSeqNum, String blockStatus, String transaction, MessageProto.TrustChainBlock block) {
        this.peerName = peerName;
        this.seqNum = seqNum;
        this.linkSeqNum = linkSeqNum;
        this.blockStatus = blockStatus;
        this.transaction = transaction;
        this.block = block;

    }

    /**
     * Check if two MutualBlockItem objects are equal.
     * @param o the other object.
     * @return true if both objects are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MutualBlockItem)) return false;

        MutualBlockItem that = (MutualBlockItem) o;

        if (getSeqNum() != that.getSeqNum()) return false;
        if (getLinkSeqNum() != that.getLinkSeqNum()) return false;
        if (!getPeerName().equals(that.getPeerName())) return false;
        if (!getBlockStatus().equals(that.getBlockStatus())) return false;
        return getTransaction().equals(that.getTransaction());
    }

    /**
     * Generate hash code of a MutualBlockItem object.
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        int result = getPeerName().hashCode();
        result = 31 * result + getSeqNum();
        result = 31 * result + getLinkSeqNum();
        result = 31 * result + getBlockStatus().hashCode();
        result = 31 * result + getTransaction().hashCode();
        return result;
    }


    /**
     * Get the username of the peer that the user is communicating with.
     * @return the username of the peer.
     */
    public String getPeerName() {
        return peerName;
    }

    /**
     * check whether the block is verified or not by both parties.
     * If it is not verified by both parties, it is a halfblock.
     * @return true if the block is verified by both parties.
     */
    public String getBlockStatus() {
        return blockStatus;
    }

    /**
     * Get the content(the message) that is in the block.
     * @return the content that is in the block.
     */
    public String getTransaction() {
        return transaction;
    }

    /**
     *  Get the sequence number of block.
     * @return the sequence number of block
     */
    public int getSeqNum() {
        return seqNum;
    }

    /**
     * Get the linked sequence number of block.
     * @return the linked sequence number of block.
     */
    public int getLinkSeqNum() {
        return linkSeqNum;
    }

    /**
     * Get the complete block
     * @return the block represented by this MutualBlock
     */
    public MessageProto.TrustChainBlock getBlock() {return block;}

}