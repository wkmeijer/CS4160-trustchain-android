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
    private final static String TAG = "PubKAndAddressPairStrg";

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
        Log.i(TAG, "add " + address + " - " + pubkey.toString());
        SharedPreferencesStorage.writeSharedPreferences(context, PUBKEY_KEY_PREFIX + pubkey.toString(), address);
        SharedPreferencesStorage.writeSharedPreferences(context, ADDRESS_KEY_PREFIX + address, pubkey.toString());
    }

    /**
     * retrieve the ip and port based on the pubkey
     * @param context
     * @param pubkey
     * @return
     */
    public static String getAddressByPubkey(Context context, PublicKey pubkey) {
        Log.i(TAG, "get address of: " + pubkey.toString());
        return SharedPreferencesStorage.readSharedPreferences(context, PUBKEY_KEY_PREFIX + pubkey.toString());
    }

    /**
     * retrieve the pub key based on the ip and port
     * @param context
     * @param address
     * @return
     */
    public static PublicKey getPubKeyByAddress(Context context, String address) {
        Log.i(TAG, "get key of: " + address);
        String pubKey =  SharedPreferencesStorage.readSharedPreferences(context, ADDRESS_KEY_PREFIX + address);
        return new PublicKey(pubKey);
    }
}
