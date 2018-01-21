package nl.tudelft.cs4160.trustchain_android.Util;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import org.libsodium.jni.NaCl;
import org.libsodium.jni.Sodium;
import org.libsodium.jni.keys.PrivateKey;
import org.libsodium.jni.keys.VerifyKey;

/**
 * Manages key operations.
 */
public class Key {

    static {
        NaCl.sodium();
    }

    private final static String TAG = "KEY";

    public final static String DEFAULT_PRIVATE_KEY_FILE = "private.key";
    public final static String DEFAULT_SIGN_KEY_FILE = "sign.key";


    public static DualKey ensureKeysExist(Context context) {
        try {
            DualKey dualKey = loadKeys(context);
            return dualKey;
        } catch (Exception e) {
            Log.e(TAG, "Keys could not be found", e);
            return createAndSaveKeys(context);
        }
    }

    public static DualKey createAndSaveKeys(Context context) {
        DualKey kp = Key.createNewKeyPair();
        saveKeyPair(context, kp);
        return kp;
    }

    public static void saveKeyPair(Context context, DualKey kp) {
        Key.saveKey(context, Key.DEFAULT_PRIVATE_KEY_FILE, kp.getPrivateKey().toBytes());
        Key.saveKey(context, Key.DEFAULT_SIGN_KEY_FILE, kp.getSigningKey().toBytes());
    }

    /**
     * Creates a new curve25519 KeyPair.
     *
     * @return KeyPair.
     */
    public static DualKey createNewKeyPair() {
        return new DualKey();
    }

    /**
     * Sign a message using the given signing key.
     *
     * @param signingKey The signing key
     * @param data       The message
     * @return The signature
     */
    public static byte[] sign(SigningKey signingKey, byte[] data) {
        byte[] signature = new byte[Sodium.crypto_sign_bytes()];
        Sodium.crypto_sign_ed25519_detached(signature, new int[] { signature.length }, data, data.length, signingKey.toBytes());
        return signature;
    }

    /**
     * Verify a signature
     *
     * @param verifyKey  The verify key of the signer.
     * @param message    The message that was signed.
     * @param signature  The signature.
     * @return True if this a correct signature, false if not.
     */
    public static boolean verify(VerifyKey verifyKey, byte[] message, byte[] signature) {
        return Sodium.crypto_sign_ed25519_verify_detached(signature, message, message.length, verifyKey.toBytes()) == 0;
    }

    public static PublicKeyPair getPublicKeyPairFromBytes(byte[] rawKeypair) {
        return new PublicKeyPair(rawKeypair);
    }

    /**
     * Load a private key from the given file
     *
     * @param context The context (needed to read the file)
     * @param file    The file
     * @return The private key
     */
    public static PrivateKey loadPrivateKey(Context context, String file) {
        String key = Util.readFile(context, file);
        Log.i(TAG, "PRIVATE FROM FILE: " + key);
        return loadPrivateKey(key);
    }

    /**
     * Load a private key from a base64 encoded string
     *
     * @param key The base64 encoded key
     * @return The private key
     */
    public static PrivateKey loadPrivateKey(String key) {
        byte[] rawKey = Base64.decode(key, Base64.DEFAULT);
        return getPrivateKeyFromBytes(rawKey);
    }

    public static PrivateKey getPrivateKeyFromBytes(byte[] rawKey) {
        return new PrivateKey(rawKey);
    }

    public static SigningKey loadSigningKey(Context context, String file) {
        String contents = Util.readFile(context, file);
        Log.i(TAG, "SIGNING KEY FROM FILE: " + contents);
        return loadSigningKey(contents);
    }

    public static SigningKey loadSigningKey(String b64) {
        byte[] signingKey = Base64.decode(b64, Base64.DEFAULT);
        return new SigningKey(signingKey);
    }

    /**
     * Load public and private keys from the standard files.
     *
     * @param context The context (needed to read the files)
     * @return A KeyPair with the private and public key.
     */
    public static DualKey loadKeys(Context context) {
        try {
            PrivateKey privateKey = Key.loadPrivateKey(context, Key.DEFAULT_PRIVATE_KEY_FILE);
            SigningKey signingKey = Key.loadSigningKey(context, Key.DEFAULT_SIGN_KEY_FILE);
            return new DualKey(privateKey.toBytes(), signingKey.toBytes());
        } catch (Throwable t) { return null; }
    }

    /**
     * Write a key to storage
     *
     * @param context Context (needed to write to the file)
     * @param file    The file to write to
     * @param key     The key to be written
     * @return True if successful, false if not
     */
    public static boolean saveKey(Context context, String file, byte[] key) {
        return Util.writeToFile(context, file, Base64.encodeToString(key, Base64.DEFAULT));
    }


}
