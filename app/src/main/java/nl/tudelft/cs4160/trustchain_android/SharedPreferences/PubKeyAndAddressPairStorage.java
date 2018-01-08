package nl.tudelft.cs4160.trustchain_android.SharedPreferences;

import android.content.Context;

/**
 * This class will store the public key and address of the current user locally.
 */
public class PubKeyAndAddressPairStorage {

    private final static String PUBKEY_KEY_PREFIX = "PUBKEY_KEY_PREFIX:";
    private final static String ADDRESS_KEY_PREFIX = "ADDRESS_KEY_PREFIX:";

    public static void addPubkeyAndAddressPair(Context context, String pubkey, String address) {
        if(pubkey == null || address == null) {
            return;
        }
        SharedPreferencesStorage.writeSharedPreferences(context, PUBKEY_KEY_PREFIX + pubkey, address);
        SharedPreferencesStorage.writeSharedPreferences(context, ADDRESS_KEY_PREFIX + address, pubkey);
    }

    public static String getAddressByPubkey(Context context, String pubkey) {
        return SharedPreferencesStorage.readSharedPreferences(context, PUBKEY_KEY_PREFIX + pubkey);
    }

    public static String getPubKeyByAddress(Context context, String address) {
        return SharedPreferencesStorage.readSharedPreferences(context, ADDRESS_KEY_PREFIX + address);
    }
}
