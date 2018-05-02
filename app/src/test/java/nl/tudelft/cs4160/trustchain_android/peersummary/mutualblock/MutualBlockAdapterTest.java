
package nl.tudelft.cs4160.trustchain_android.peersummary.mutualblock;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.ArrayList;

import nl.tudelft.cs4160.trustchain_android.block.ValidationResult;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

public class MutualBlockAdapterTest extends TestCase {


    @Test
    public void testSimpleInitializationWithEmptyList() {
        ArrayList<MutualBlockItem> list = new ArrayList<>();
        MutualBlockAdapter mbA = new MutualBlockAdapter(null, list);
        assertEquals(0, mbA.getItemCount());
    }

    @Test
    public void testSimpleInitializationWithOneItemInList() {
        ArrayList<MutualBlockItem> list = new ArrayList<>();
        list.add(new MutualBlockItem("peerTest",
                MessageProto.TrustChainBlock.newBuilder().build(),
                ValidationResult.INVALID));
        MutualBlockAdapter mbA = new MutualBlockAdapter(null, list);
        assertEquals(1, mbA.getItemCount());
    }

}