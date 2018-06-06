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
    private byte[] transaction;
    private String transactionFormat;
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
        this.transaction = block.getTransaction().getUnformatted().toByteArray();
        this.transactionFormat = block.getTransaction().getFormat();
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
     * Get the content (the message or file bytes) that is in the block.
     * @return the content that is in the block.
     */
    public byte[] getTransaction() {
        return transaction;
    }

    /**
     * Get the format of the transaction in the block,
     * i.e., whether a file is enclosed and what its format is.
     * @return the format of the transaction bytes.
     */
    public String getTransactionFormat() {
        return transactionFormat;
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