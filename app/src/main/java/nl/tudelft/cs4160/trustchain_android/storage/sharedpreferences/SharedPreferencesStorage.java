package nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import static nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.InboxItemStorage.INBOX_ITEM_KEY;

/**
 * The class that holds all the functions necessary to store data locally.
 */
public final class SharedPreferencesStorage {
    public static final String PREFS_NAME = "MyPrefsFile";
    private static Gson gson;

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


    public static <T> T readSharedPreferences(Context context, String key, Class<T> type) throws IOException, ClassNotFoundException {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        if(settings.contains(key)) {
            String object = settings.getString(key, null);
            // when we deal with an InboxItem list serialize it instead of json because of some dumb thing
            // where gson can't deal with ByteStrings.
            if(key.equals(INBOX_ITEM_KEY)) {
                ByteArrayInputStream in = new ByteArrayInputStream(Base64.decode(object,Base64.DEFAULT));
                ObjectInputStream is = new ObjectInputStream(in);
                return (T) is.readObject();
            }
            if (gson == null) {
                gson = new GsonBuilder().create();
            }
            return gson.fromJson(object, type);
        } else {
            return null;
        }
    }

    public static void writeSharedPreferences(Context context, String key, Object value) throws IOException {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        String a = "";

        // when we deal with an InboxItem list serialize it instead of json because of some dumb thing
        // where gson can't deal with ByteStrings.
        if(key.equals(INBOX_ITEM_KEY)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(value);
            a = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
        } else {
            if (gson == null) {
                gson = new GsonBuilder().create();
            }
            a = gson.toJson(value);
        }
        editor.putString(key, a);
        editor.apply();
    }
}
