package nl.tudelft.cs4160.trustchain_android.UtilTest;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import nl.tudelft.cs4160.trustchain_android.Util.ByteArrayConverter;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Boning on 1/27/2018.
 */

public class ByteArrayConverterTest {

    byte[] ba1;
    byte[] ba2;
    byte[] ba3;

    String s1;
    String s2;
    String s3;

    @Before
    public void initialization() {
        ba1 = "Test string 1".getBytes();
        ba2 = "Test string 2".getBytes();
        ba3 = "Test string 1".getBytes();

        s1 = "Test string 1";
        s2 = "Test string 2";
        s3 = "Test string 1";
    }

    @Test
    public void testByteStringToStringSame() {
        assertEquals(ByteArrayConverter.bytesToHexString(ba1),ByteArrayConverter.bytesToHexString(ba3));
    }

    @Test
    public void testByteStringToStringDifferent() {
        assertFalse(ByteArrayConverter.bytesToHexString(ba1).equals(ByteArrayConverter.bytesToHexString(ba2)));
    }

    @Test
    public void testHexStringToByteArray() {
        //byte[] output1 = ByteArrayConverter.hexStringToByteArray(s1);
        //byte[] output2 = ByteArrayConverter.hexStringToByteArray(s2);
        //assertFalse(Arrays.equals(output1, output2));
        //assertTrue(Arrays.equals(ByteArrayConverter.hexStringToByteArray(s1), ByteArrayConverter.hexStringToByteArray(s3)));
    }
}
