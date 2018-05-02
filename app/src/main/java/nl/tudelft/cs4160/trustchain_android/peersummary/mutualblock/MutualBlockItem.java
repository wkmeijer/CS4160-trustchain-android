package nl.tudelft.cs4160.trustchain_android.peersummary.mutualblock;

import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

/**
 * Class that is used to define a MutualBlock in the recycler view.
 */
public class MutualBlockItem {
    private String peerName;
    private int seqNum;
    private int linkSeqNum;
    private int validationResult;
    private String transaction;
    private MessageProto.TrustChainBlock block;

    /**
     * Constructor.
     * @param peerName the username of the peer that the user is communicating with.
     * @param block The block.
     * @param validationResult result of the validation of this block.
     */
    public MutualBlockItem(String peerName,  MessageProto.TrustChainBlock block, int validationResult) {
        this.peerName = peerName;
        this.seqNum = block.getSequenceNumber();
        this.linkSeqNum = block.getLinkSequenceNumber();
        this.transaction = block.getTransaction().getUnformatted().toStringUtf8();
        this.validationResult = validationResult;
        this.block = block;

    }


    /**
     * Get the username of the peer that the user is communicating with.
     * @return the username of the peer.
     */
    public String getPeerName() {
        return peerName;
    }

    /**
     * Get the validation result
     * @return
     */
    public int getValidationResult() { return validationResult; }


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