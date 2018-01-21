package nl.tudelft.cs4160.trustchain_android.Util;

import org.libsodium.jni.Sodium;
import org.libsodium.jni.keys.PublicKey;
import org.libsodium.jni.keys.VerifyKey;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class PublicKeyPair {
    private static final String checkString = "LibNaCLPk:";
    PublicKey publicKey;
    VerifyKey verifyKey;

    public PublicKeyPair(PublicKey publicKey, VerifyKey verifyKey) {
        this.publicKey = publicKey;
        this.verifyKey = verifyKey;
    }

    public PublicKeyPair(byte[] bytes) {
        byte[] check = checkString.getBytes();
        int pkLength = Sodium.crypto_box_curve25519xsalsa20poly1305_publickeybytes();
        int vkLength = Sodium.crypto_sign_ed25519_publickeybytes();
        if (bytes.length != check.length + pkLength + vkLength) {
            throw new RuntimeException("Key does not match format " + checkString + " + " + pkLength + " bytes + " + vkLength + " bytes (was " + bytes.length + ").");
        }

        byte[] pk = Arrays.copyOfRange(bytes, check.length, check.length + pkLength);
        byte[] vk = Arrays.copyOfRange(bytes, check.length + pkLength, check.length + pkLength + vkLength);

        this.publicKey = new PublicKey(pk);
        this.verifyKey = new VerifyKey(vk);
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public VerifyKey getVerifyKey() {
        return verifyKey;
    }

    public byte[] toBytes() {
        try {
            ByteArrayOutputStream export = new ByteArrayOutputStream();
            export.write(checkString.getBytes());
            export.write(getPublicKey().toBytes());
            export.write(getVerifyKey().toBytes());

            return export.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
