package nl.tudelft.cs4160.trustchain_android.databaseTest;

import org.junit.Test;

import nl.tudelft.cs4160.trustchain_android.database.TrustChainDBContract;

import static org.junit.Assert.assertEquals;

/**
 * Created by Boning on 12/16/2017.
 */

public class TrustChainDBContractTest {

    @Test
    public void testBlockEntries() {
        assertEquals(TrustChainDBContract.BlockEntry.TABLE_NAME, "block");
        assertEquals(TrustChainDBContract.BlockEntry.COLUMN_NAME_TX, "tx");
        assertEquals(TrustChainDBContract.BlockEntry.COLUMN_NAME_PUBLIC_KEY, "public_key");
        assertEquals(TrustChainDBContract.BlockEntry.COLUMN_NAME_SEQUENCE_NUMBER, "sequence_number");
        assertEquals(TrustChainDBContract.BlockEntry.COLUMN_NAME_LINK_PUBLIC_KEY, "link_public_key");
        assertEquals(TrustChainDBContract.BlockEntry.COLUMN_NAME_LINK_SEQUENCE_NUMBER, "link_sequence_number");
        assertEquals(TrustChainDBContract.BlockEntry.COLUMN_NAME_PREVIOUS_HASH, "previous_hash");
        assertEquals(TrustChainDBContract.BlockEntry.COLUMN_NAME_SIGNATURE, "signature");
        assertEquals(TrustChainDBContract.BlockEntry.COLUMN_NAME_INSERT_TIME, "insert_time");
        assertEquals(TrustChainDBContract.BlockEntry.COLUMN_NAME_BLOCK_HASH, "block_hash");
    }

}
