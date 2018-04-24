package nl.tudelft.cs4160.trustchain_android.util;

import com.google.protobuf.ByteString;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

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

    String hexString1;
    String hexString2;

    ByteString byteString1;
    ByteString byteString2;

    @Before
    public void initialization() {
        s1 = "0002";
        s2 = "0001";

        byte[] byteArray = {0, 2};
        ba1 = byteArray;
        ba3 = byteArray;
        byteString1 = ByteString.copyFrom(byteArray);
        byte[] byteArray2 = {0, 1};
        ba2 = byteArray2;
        byteString2 = ByteString.copyFrom(byteArray2);

        hexString1 = "0002";
        hexString2 = "0001";
    }

    @Test
    public void testByteStringToStringSame() {
        assertEquals(ByteArrayConverter.bytesToHexString(ba1), ByteArrayConverter.bytesToHexString(ba3));
    }

    @Test
    public void testByteStringToStringDifferent() {
        assertFalse(ByteArrayConverter.bytesToHexString(ba1).equals(ByteArrayConverter.bytesToHexString(ba2)));
    }

    @Test
    public void testBytesToHexString() {
        assertEquals("0002",ByteArrayConverter.bytesToHexString(ba1));
    }

    @Test
    public void testByteStringToString() {
        String output1 = ByteArrayConverter.byteStringToString(byteString1);
        assertEquals(output1, "0002");
    }

    @Test
    public void testHexStringToByteArray() {
        byte[] output1 = ByteArrayConverter.hexStringToByteArray(hexString1);
        assertTrue(Arrays.equals(output1, ba1));
    }
}
