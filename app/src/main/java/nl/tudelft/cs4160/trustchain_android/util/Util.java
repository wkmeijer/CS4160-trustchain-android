package nl.tudelft.cs4160.trustchain_android.util;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

public class Util {

    /**
     * Read a file from storage
     * @param context The context.
     * @param fileName The file to be read.
     * @return The content of the file
     */
    public static String readFile(Context context, String fileName) {
        File file = context.getFileStreamPath(fileName);
        if(file == null || !file.exists()) {
            return null;
        } else {
            try {
                StringBuilder text = new StringBuilder();
                FileInputStream fis = context.openFileInput(fileName);
                BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(fis)));

                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
                return text.toString();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Write to a file
     * @param context The context
     * @param fileName File to be written
     * @param data The data to be written to the file
     * @return True if successful, false if not
     */
    public static boolean writeToFile(Context context, String fileName, String data) {
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Create a ellipsized string
     * @param input - the string to be ellipsized
     * @param maxLength - The maximum length the result string can be, minimum should be 6
     * @return An ellipsized string of the input
     */
    public static String ellipsize(String input, int maxLength) {
        String ellip = "(..)";
        if (input == null || input.length() <= maxLength
                || input.length() < ellip.length()) {
            return input;
        }
        if (maxLength < ellip.length()+2) {
            return input.substring(0,1).concat(ellip).concat(input.substring(input.length()-1,input.length()));
        }
        return input.substring(0, (maxLength - ellip.length())/2)
                .concat(ellip)
                .concat(input.substring(input.length() - (maxLength - ellip.length())/2,input.length()));
    }

    public static String readableSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * Returns a nice string representation indicating how long ago this peer was last seen.
     * @param msSinceLastMessage
     * @return a string representation of last seen
     */
    public static String timeToString(long msSinceLastMessage) {
        // display seconds
        if(msSinceLastMessage < 60000) {
            return " " + ((int) Math.floor(msSinceLastMessage / 1000.0)) + "s";
        }

        // display minutes
        if(msSinceLastMessage < 3600000) {
            int seconds = ((int) Math.floor((msSinceLastMessage / 1000.0)));
            int minutes = ((int) Math.floor(seconds /60.0));
            seconds = seconds % 60;
            return " " + minutes + "m" + seconds + "s";
        }

        // display hours
        if(msSinceLastMessage < 86400000) {
            int minutes = ((int) Math.floor(msSinceLastMessage /60000.0));
            int hours = ((int) Math.floor(minutes / 60.0));
            minutes = minutes % 60;
            return " " + hours + "h" + minutes + "m";
        }

        // default: more than 1 day, display nothing, getting a time since last message that is this
        // high will almost always happen cause of some error. In any other cases, the app has been
        // closed for such a long time that the information isn't useful anymore.
        return "";
    }
}
