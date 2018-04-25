
package nl.tudelft.cs4160.trustchain_android.peersummary.mutualblock;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.ArrayList;

public class MutualBlockAdapterTest extends TestCase {


    @Test
    public void testSimpleInitializationWithEmptyList() {
        ArrayList<MutualBlockItem> list = new ArrayList<>();
        MutualBlockAdapter mbA = new MutualBlockAdapter(null, list);
        assertEquals(0, mbA.getItemCount());
    }

//    @Test
//    public void testSimpleInitializationWithOneItemInList() {
//        ArrayList<MutualBlockItem> list = new ArrayList<>();
//        list.add(new MutualBlockItem("peer", 0, 0, "test", "test2"));
//        MutualBlockAdapter mbA = new MutualBlockAdapter(null, list);
//        assertEquals(1, mbA.getItemCount());
//    }

}