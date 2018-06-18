package nl.tudelft.cs4160.trustchain_android.Block;


import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityUnitTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libsodium.jni.NaCl;
import org.libsodium.jni.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper;
import nl.tudelft.cs4160.trustchain_android.block.ValidationResult;
import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.main.OverviewConnectionsActivity;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.storage.database.TrustChainDBHelper;
import nl.tudelft.cs4160.trustchain_android.util.ByteArrayConverter;

import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.when;

/**
 * Created by Boning on 12/17/2017.
 */
@RunWith(AndroidJUnit4.class)
public class TrustChainBlockTest extends ActivityUnitTestCase<OverviewConnectionsActivity> {
    public TrustChainBlockTest() {
        super(OverviewConnectionsActivity.class);
    }

    private DualSecret keyPair;
    private DualSecret keyPair2;
    private byte[] transaction = new byte[2];
    private byte[] pubKey;
    private byte[] linkKey = new byte[2];
    private MessageProto.TrustChainBlock genesisBlock;
    private TrustChainDBHelper dbHelper;

    @Before
    public void initialization() {
        NaCl.sodium();
        keyPair = Key.createNewKeyPair();
        keyPair2 = Key.createNewKeyPair();
        dbHelper = mock(TrustChainDBHelper.class);
        when(dbHelper.getMaxSeqNum(keyPair.getPublicKeyPair().toBytes())).thenReturn(2);
        transaction[0] = 12;
        transaction[1] = 42;
        pubKey = keyPair.getPublicKeyPair().toBytes();
        linkKey[0] = 14;
        linkKey[1] = 72;
        genesisBlock = TrustChainBlockHelper.createGenesisBlock(keyPair);
    }

    @Test
    public void testPublicKeyGenesisBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createGenesisBlock(keyPair);
        assertEquals(ByteArrayConverter.bytesToHexString(keyPair.getPublicKeyPair().toBytes()),
                ByteArrayConverter.bytesToHexString(block.getPublicKey().toByteArray()));
    }

    @Test
    public void testGetSequenceNumberGenesisBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createBlock(transaction, null, dbHelper, pubKey, genesisBlock, linkKey);
        assertEquals(0, block.getSequenceNumber());
    }

    @Test
    public void testPublicKeyBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createBlock(transaction, null, dbHelper, pubKey, genesisBlock, linkKey);
        assertEquals(ByteArrayConverter.bytesToHexString(pubKey),
                ByteArrayConverter.bytesToHexString(block.getPublicKey().toByteArray()));
    }

    @Test
    public void testLinkPublicKeyBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createBlock(transaction, null, dbHelper, pubKey, genesisBlock, linkKey);
        assertEquals(ByteArrayConverter.bytesToHexString(keyPair.getPublicKeyPair().toBytes()),
                ByteArrayConverter.bytesToHexString(block.getLinkPublicKey().toByteArray()));
    }

    @Test
    public void testGetSequenceNumberBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createBlock(transaction, null, dbHelper, pubKey, genesisBlock, linkKey);
        assertEquals(0, block.getSequenceNumber());
    }

    @Test
    public void testIsInitializedGenesisBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createGenesisBlock(keyPair);
        assertTrue(block.isInitialized());
    }

    @Test
    public void testGetSameSerializedSizeBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createGenesisBlock(keyPair);
        assertEquals(block.getSerializedSize(), block.getSerializedSize());
    }

    @Test
    public void testGetDiffSerializedSizeBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createGenesisBlock(keyPair);
        assertEquals(block.getSerializedSize(), block.getSerializedSize());
    }

    @Test
    public void testGetDiffHashBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createGenesisBlock(keyPair);
        MessageProto.TrustChainBlock block2 = TrustChainBlockHelper.createGenesisBlock(keyPair2);
        assertNotEquals(block.hashCode(), block2.hashCode());
    }

    @Test
    public void testEqualBlocks() {
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createGenesisBlock(keyPair);
        assertTrue(block.equals(block));
    }

    @Test
    public void testNotEqualBlocks() {
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createGenesisBlock(keyPair);
        MessageProto.TrustChainBlock block2 = TrustChainBlockHelper.createGenesisBlock(keyPair2);
        assertFalse(block.equals(block2));
    }

    @Test
    public void testVerify() {
        DualSecret pair = Key.createNewKeyPair();
        byte[] message = {(byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01};
        byte[] signature = Key.sign(pair.getSigningKey(), message);
        assertTrue(Key.verify(pair.getVerifyKey(), message, signature));
    }


    @After
    public void resetMocks() {
        validateMockitoUsage();
    }

}
