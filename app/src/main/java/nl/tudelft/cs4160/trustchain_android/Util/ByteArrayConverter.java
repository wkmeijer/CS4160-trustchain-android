package nl.tudelft.cs4160.trustchain_android.Util;

import android.util.Log;

import com.google.protobuf.ByteString;

/**
 * Created by timbu on 10/01/2018.
 */

public class ByteArrayConverter {

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String byteStringToString(ByteString byteString) {
        return bytesToHexString(byteString.toByteArray());
    }

    public static String bytesToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private void printByteArray(byte[] message) {
        String printString = "";
        for (byte b : message) {
            printString = printString + b;
        }
        Log.d("testByteString", printString);
    }

}
