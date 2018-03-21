package nl.tudelft.cs4160.trustchain_android.SharedPreferences;

import android.content.Context;

import org.libsodium.jni.keys.PublicKey;

import nl.tudelft.cs4160.trustchain_android.Util.ByteArrayConverter;

/**
 * This class will store the chosen username of the user locally.
 */
public class UserNameStorage {

    static String userNameStorage = "userNameStorage";

    public static void setUserName(Context context, String userName) {
        SharedPreferencesStorage.writeSharedPreferences(context, userNameStorage, userName);
    }

    public static String getUserName(Context context) {
        return SharedPreferencesStorage.readSharedPreferences(context, userNameStorage);
    }

    public static void setNewPeerByPublicKey(Context context, String userName, PublicKey publicKey){
        SharedPreferencesStorage.writeSharedPreferences(context, publicKey.toString(), userName);
    }

    public static String getPeerByPublicKey(Context context, PublicKey publicKey){
        return SharedPreferencesStorage.readSharedPreferences(context, publicKey.toString());
    }
}
