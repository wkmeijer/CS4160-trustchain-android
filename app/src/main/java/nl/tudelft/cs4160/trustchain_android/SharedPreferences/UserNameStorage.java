package nl.tudelft.cs4160.trustchain_android.SharedPreferences;

import android.content.Context;

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

    public static void setNewPeerByPublicKey(Context context, String userName, String publicKey){
        SharedPreferencesStorage.writeSharedPreferences(context, publicKey, userName);
    }

    public static String getPeerByPublicKey(Context context, byte[] publicKey) {
        return getPeerByPublicKey(context, ByteArrayConverter.bytesToHexString(publicKey));
    }

    public static String getPeerByPublicKey(Context context, String publickey){
        return SharedPreferencesStorage.readSharedPreferences(context, publickey);
    }
}
