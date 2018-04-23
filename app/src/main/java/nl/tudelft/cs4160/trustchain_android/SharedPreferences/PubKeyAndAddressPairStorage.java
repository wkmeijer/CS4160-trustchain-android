package nl.tudelft.cs4160.trustchain_android.SharedPreferences;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;

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
     * @param pubKeyPair
     * @param address
     */
    public static void addPubkeyAndAddressPair(Context context, PublicKeyPair pubKeyPair, String address) {
        if(pubKeyPair == null || address == null) {
            return;
        }
        Log.i(TAG, "add " + address + " - " + pubKeyPair.toString());
        SharedPreferencesStorage.writeSharedPreferences(context, PUBKEY_KEY_PREFIX + Base64.encodeToString(pubKeyPair.toBytes(), Base64.DEFAULT), address);
        SharedPreferencesStorage.writeSharedPreferences(context, ADDRESS_KEY_PREFIX + address, Base64.encodeToString(pubKeyPair.toBytes(), Base64.DEFAULT));
    }

    /**
     * retrieve the ip and port based on the pubkey
     * @param context
     * @param pubKeyPair
     * @return
     */
    public static String getAddressByPubkey(Context context, PublicKeyPair pubKeyPair) {
        Log.i(TAG, "get address of: " + pubKeyPair.toString());
        return SharedPreferencesStorage.readSharedPreferences(context, PUBKEY_KEY_PREFIX + Base64.encodeToString(pubKeyPair.toBytes(), Base64.DEFAULT));
    }

    /**
     * retrieve the pub key based on the ip and port
     * @param context
     * @param address
     * @return
     */
    public static PublicKeyPair getPubKeyByAddress(Context context, String address) {
        Log.i(TAG, "get key of: " + address);
        String pubKeyPair = SharedPreferencesStorage.readSharedPreferences(context, ADDRESS_KEY_PREFIX + address);
        return new PublicKeyPair(Base64.decode(pubKeyPair, Base64.DEFAULT));
    }
}
