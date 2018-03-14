package nl.tudelft.cs4160.trustchain_android.SharedPreferences;

import android.content.Context;
import android.util.Log;

import org.libsodium.jni.keys.PublicKey;

/**
 * This class will store the public key and address of the current user locally.
 */
public class PubKeyAndAddressPairStorage {

    private final static String PUBKEY_KEY_PREFIX = "PUBKEY_KEY_PREFIX:";
    private final static String ADDRESS_KEY_PREFIX = "ADDRESS_KEY_PREFIX:";

    /**
     * Store locally public key combined with address.
     * @param context
     * @param pubkey
     * @param address
     */
    public static void addPubkeyAndAddressPair(Context context, PublicKey pubkey, String address) {
        if(pubkey == null || address == null) {
            return;
        }
        Log.d("PubKeyAndAddress", "add " + address + " - " + pubkey.toString());
        SharedPreferencesStorage.writeSharedPreferences(context, PUBKEY_KEY_PREFIX + pubkey, address);
        SharedPreferencesStorage.writeSharedPreferences(context, ADDRESS_KEY_PREFIX + address, pubkey);
    }

    /**
     * retrieve the ip and port based on the pubkey
     * @param context
     * @param pubkey
     * @return
     */
    public static String getAddressByPubkey(Context context, PublicKey pubkey) {
        Log.d("PubKeyAndAddres", "get address of: " + pubkey.toString());
        return SharedPreferencesStorage.readSharedPreferences(context, PUBKEY_KEY_PREFIX + pubkey);
    }

    /**
     * retrieve the pub key based on the ip and port
     * @param context
     * @param address
     * @return
     */
    public static String getPubKeyByAddress(Context context, String address) {
        Log.d("PubKeyAndAddres", "get key of: " + address);
        return SharedPreferencesStorage.readSharedPreferences(context, ADDRESS_KEY_PREFIX + address);
    }
}
