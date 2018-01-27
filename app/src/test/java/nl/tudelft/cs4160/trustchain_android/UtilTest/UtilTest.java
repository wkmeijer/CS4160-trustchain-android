package nl.tudelft.cs4160.trustchain_android.UtilTest;

import com.google.protobuf.ByteString;

import org.junit.Before;
import org.junit.Test;

import nl.tudelft.cs4160.trustchain_android.Util.ByteArrayConverter;
import nl.tudelft.cs4160.trustchain_android.Util.Util;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Boning on 12/17/2017.
 */

public class UtilTest {
    String longString;
    String longStringFirst6;
    String shortString;
    String shortStringFirst6;

    @Before
    public void initialization() {
        longString = "0002SDVnnuisdvbhhduifvbsdiuvndskjvcxnvjkdsvusidvbsdjovcnvjkcxv0002SDVnnuisdvbhhduifvbsdiuvndskjvcxnvjkdsvusidvbsdjovcnvjkcxv0002SDVnnuisdvbhhduifvbsdiuvndskjvcxnvjkdsvusidvbsdjovcnvjkcxv";
        longStringFirst6 = "0(..)v";
        shortString = "viudnfvidufdvn84938enfivdnidvn";
        shortStringFirst6 = "v(..)n";
    }

    @Test
    public void testEllipsizeShort() {
        String a = Util.ellipsize(shortString, 6);
        assertEquals(a, shortStringFirst6);
    }

    @Test
    public void testEllipsizeLong() {
        assertEquals(Util.ellipsize(longString, 6), longStringFirst6);
    }

    @Test
    public void testEllipsizeFull() {
        assertEquals(Util.ellipsize(shortString, 1000), shortString);
    }


}
