package nl.tudelft.cs4160.trustchain_android.SharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

/**
 * Created by timbu on 18/12/2017.
 */

public final class SharedPreferencesStorage {
    public static final String PREFS_NAME = "MyPrefsFile";
    private static Gson gson;

    public static String readSharedPreferences(Context context, String key) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);

        if (settings.contains(key)) {
            String object = settings.getString(key, null);
            return object;
        } else {
            return null;
        }
    }

    public static void writeSharedPreferences(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void writeSharedPreferences(Context context, String key, int value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static Map<String, ?> getAll(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getAll();
    }


    public static <T> T readSharedPreferences(Context context, String key, Class<T> type) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        if(settings.contains(key)) {
            String object = settings.getString(key, null);
            if (gson == null) {
                gson = new GsonBuilder().create();
            }
            return gson.fromJson(object, type);
        }else{
            return null;
        }
    }

    public static void writeSharedPreferences(Context context, String key, Object value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        if (gson == null) {
            gson = new GsonBuilder().create();
        }
        String a = gson.toJson(value);
        editor.putString(key, a);
        editor.apply();
    }
}
