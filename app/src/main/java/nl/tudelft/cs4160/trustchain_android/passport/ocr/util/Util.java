package nl.tudelft.cs4160.trustchain_android.passport.ocr.util;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import nl.tudelft.cs4160.trustchain_android.R;

public class Util {

    public static final String FOLDER_DIGITAL_VOTING_PASS = "DigitalVotingPass";

    /**
     * Returns the height of the status bar in pixels
     *
     * @param resources Resources object required to get the height attribute.
     * @return int
     */
    public static int getStatusBarHeight(Resources resources) {
        int result = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * Copies an InputStream into a File.
     * This is used to copy an InputStream from the assets folder to a file in the FileSystem.
     * Creates nay non-existant parent folders to f.
     *
     * @param is InputStream to be copied.
     * @param f  File to copy data to.
     */
    public static void copyAssetsFile(InputStream is, File f) throws IOException {
        if (!f.exists()) {
            if (!f.getParentFile().mkdirs()) { //getParent because otherwise it creates a folder with that filename, we just need the dirs
                Log.e("Util", "Cannot create path!");
            }
        }
        OutputStream os = new FileOutputStream(f, true);

        final int buffer_size = 1024 * 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
            is.close();
            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Method for converting a hexString to a byte array.
     * This method is used for signing transaction hashes (which are in hex).
     */
    public static byte[] hexStringToByteArray(String hStr) {
        if (hStr != null) {
            int len = hStr.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(hStr.charAt(i), 16) << 4)
                        + Character.digit(hStr.charAt(i + 1), 16));
            }
            return data;
        }
        return new byte[0];
    }

    /**
     * Method for converting a byte array to a hexString.
     * This method is used for converting a signed 8-byte array back to a hashString in order to
     * display it readable.
     */
    public static String byteArrayToHexString(byte[] bArray) {
        if (bArray != null) {
            final char[] hexArray = "0123456789ABCDEF".toCharArray();
            char[] hexChars = new char[bArray.length * 2];
            for (int j = 0; j < bArray.length; j++) {
                int v = bArray[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }
        return "";
    }

    public static boolean isOnline(Context ctx) {
        ConnectivityManager cm =
                (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static boolean isNetEnabled(Context ctx) {
        ConnectivityManager cm =
                (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null;
    }
}
