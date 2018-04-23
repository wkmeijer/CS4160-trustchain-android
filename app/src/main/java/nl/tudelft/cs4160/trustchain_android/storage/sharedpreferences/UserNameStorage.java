package nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences;

import android.content.Context;
import android.util.Base64;

import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;

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

    public static void setNewPeerByPublicKey(Context context, String userName, PublicKeyPair pubKeyPair){
        SharedPreferencesStorage.writeSharedPreferences(context, Base64.encodeToString(pubKeyPair.toBytes(),Base64.DEFAULT), userName);
    }

    public static String getPeerByPublicKey(Context context, PublicKeyPair pubKeyPair){
        return SharedPreferencesStorage.readSharedPreferences(context, Base64.encodeToString(pubKeyPair.toBytes(),Base64.DEFAULT));
    }
}
