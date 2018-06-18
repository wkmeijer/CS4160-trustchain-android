package nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences;

import android.content.Context;

/**
 * This class will store the IP of the bootstrap locally.
 */
public class BootstrapIPStorage {

    static String bootstrapIPStorage = "bootstrapIPStorage";

    /**
     *
     * @param context
     * @param ip
     */
    public static void setIP(Context context, String ip) {
        if(ip == null) {
            return;
        }
        SharedPreferencesStorage.writeSharedPreferences(context, bootstrapIPStorage, ip);
    }

    public static String getIP(Context context) {
        return SharedPreferencesStorage.readSharedPreferences(context, bootstrapIPStorage);
    }
}
