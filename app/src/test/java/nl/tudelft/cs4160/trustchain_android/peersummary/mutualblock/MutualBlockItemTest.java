package nl.tudelft.cs4160.trustchain_android.peersummary.mutualblock;


import com.google.protobuf.ByteString;

import org.junit.Before;
import org.junit.Test;

import nl.tudelft.cs4160.trustchain_android.block.ValidationResult;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class MutualBlockItemTest {

    private String peerName = "peer";
    private int seqNum = 1;
    private int linkSeqNum = 2;
    private String transaction = "Test Transaction";
    private MessageProto.TrustChainBlock block;


    @Before
    public void init() {
        block = MessageProto.TrustChainBlock.newBuilder()
                .setLinkSequenceNumber(linkSeqNum)
                .setSequenceNumber(seqNum)
                .setTransaction(
                        MessageProto.TrustChainBlock.Transaction
                                .newBuilder()
                                .setUnformatted(ByteString.copyFrom(transaction.getBytes())).build()
                ).build();
    }

    @Test
    public void testGetPeerName() {
        MutualBlockItem mbi = new MutualBlockItem(peerName, block, ValidationResult.INVALID);
        assertEquals(mbi.getPeerName(), peerName);
    }


    @Test
    public void testGetBlock() {
        MutualBlockItem mbi = new MutualBlockItem(peerName, block, ValidationResult.INVALID);
        assertEquals(mbi.getBlock(), block);
    }

    @Test
    public void testGetValidationResult() {
        MutualBlockItem mbi = new MutualBlockItem(peerName, block, ValidationResult.INVALID);
        assertEquals(mbi.getValidationResult(), ValidationResult.INVALID);
    }

    @Test
    public void testLinkSeqNum() {
        MutualBlockItem mbi = new MutualBlockItem(peerName, block, ValidationResult.INVALID);
        assertEquals(mbi.getLinkSeqNum(), linkSeqNum);
    }


    @Test
    public void testSeqNum() {
        MutualBlockItem mbi = new MutualBlockItem(peerName, block, ValidationResult.INVALID);
        assertEquals(mbi.getSeqNum(), seqNum);
    }



}