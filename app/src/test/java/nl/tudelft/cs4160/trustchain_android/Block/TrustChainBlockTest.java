package nl.tudelft.cs4160.trustchain_android.Block;


import android.content.Context;
import android.util.Log;

import com.google.protobuf.CodedOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import nl.tudelft.cs4160.trustchain_android.Peer;
import nl.tudelft.cs4160.trustchain_android.Util.Key;
import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlock;
import nl.tudelft.cs4160.trustchain_android.database.TrustChainDBHelper;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

/**
 * Created by Boning on 12/17/2017.
 */
public class TrustChainBlockTest {
    private KeyPair keyPair;
    private KeyPair keyPair2;
    private byte[] transaction = new byte[2];
    private byte[] pubKey = new byte[2];
    private byte[] linkKey = new byte[2];
    private MessageProto.TrustChainBlock genesisBlock;
    private TrustChainDBHelper dbHelper;

    @Before
    public void initialization() {
        keyPair = Key.createNewKeyPair();
        keyPair2 = Key.createNewKeyPair();
        dbHelper = mock(TrustChainDBHelper.class);
        when(dbHelper.getMaxSeqNum(keyPair.getPublic().getEncoded())).thenReturn(0);
        transaction[0] = 12;
        transaction[1] = 42;
        pubKey[0] = 2;
        pubKey[1] = 4;
        linkKey[0] = 14;
        linkKey[1] = 72;
        genesisBlock = TrustChainBlock.createGenesisBlock(keyPair);
    }

    @Test
    public void publicKeyGenesisBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlock.createGenesisBlock(keyPair);
        assertEquals(bytesToHex(keyPair.getPublic().getEncoded()), bytesToHex(block.getPublicKey().toByteArray()));
    }

    @Test
    public void getSequenceNumberGenesisBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlock.createBlock(transaction, dbHelper, pubKey, genesisBlock, linkKey);
        assertEquals(0, block.getSequenceNumber());
    }

    @Test
    public void publicKeyBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlock.createBlock(transaction, dbHelper, pubKey, genesisBlock, linkKey);
        assertEquals(bytesToHex(pubKey), bytesToHex(block.getPublicKey().toByteArray()));
    }

    @Test
    public void linkPublicKeyBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlock.createBlock(transaction, dbHelper, pubKey, genesisBlock, linkKey);
        assertEquals(bytesToHex(keyPair.getPublic().getEncoded()), bytesToHex(block.getLinkPublicKey().toByteArray()));
    }

    @Test
    public void getSequenceNumberBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlock.createBlock(transaction, dbHelper, pubKey, genesisBlock, linkKey);
        assertEquals(0, block.getSequenceNumber());
    }

    @Test
    public void isInitializedGenesisBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlock.createGenesisBlock(keyPair);
        assertTrue(block.isInitialized());
    }

    @Test
    public void getSameSerializedSizeBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlock.createGenesisBlock(keyPair);
        assertEquals(block.getSerializedSize(), block.getSerializedSize());
    }

    @Test
    public void getDiffSerializedSizeBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlock.createGenesisBlock(keyPair);
        assertEquals(block.getSerializedSize(), block.getSerializedSize());
    }

    @Test
    public void getDiffHashBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlock.createGenesisBlock(keyPair);
        MessageProto.TrustChainBlock block2 = TrustChainBlock.createGenesisBlock(keyPair2);
        assertNotEquals(block.hashCode(), block2.hashCode());
    }

    @Test
    public void equalBlocks() {
        MessageProto.TrustChainBlock block = TrustChainBlock.createGenesisBlock(keyPair);
        assertTrue(block.equals(block));
    }

    @Test
    public void notEqualBlocks() {
        MessageProto.TrustChainBlock block = TrustChainBlock.createGenesisBlock(keyPair);
        MessageProto.TrustChainBlock block2 = TrustChainBlock.createGenesisBlock(keyPair2);
        assertFalse(block.equals(block2));
    }

    @After
    public void resetMocks(){
        validateMockitoUsage();
    }

}
