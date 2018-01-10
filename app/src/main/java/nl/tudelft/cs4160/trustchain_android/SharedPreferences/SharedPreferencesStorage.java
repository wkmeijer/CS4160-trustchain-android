package nl.tudelft.cs4160.trustchain_android.SharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * The class that holds all the functions necessary to store data locally.
 */
public final class SharedPreferencesStorage {
    public static final String PREFS_NAME = "MyPrefsFile";

    /**
     * Returns the string value that should be stored under some given key.
     * @param context
     * @param key
     * @return
     */
    public static String readSharedPreferences(Context context, String key) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);

        if (settings.contains(key)) {
            String object = settings.getString(key, null);
            return object;
        } else {
            return null;
        }
    }

    /**
     * Stores a given String value under a given key.
     * @param context
     * @param key
     * @param value
     */
    public static void writeSharedPreferences(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Gets all locally stored values.
     * @param context
     * @return
     */
    public static Map<String, ?> getAll(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getAll();
    }
}
