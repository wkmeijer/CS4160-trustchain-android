package nl.tudelft.cs4160.trustchain_android.crypto;

import org.libsodium.jni.Sodium;
import org.libsodium.jni.crypto.Point;
import org.libsodium.jni.crypto.Random;
import org.libsodium.jni.keys.PrivateKey;
import org.libsodium.jni.keys.PublicKey;
import org.libsodium.jni.keys.VerifyKey;

import static org.libsodium.jni.crypto.Util.checkLength;

public class DualSecret {
    private byte[] privateKey;
    private byte[] publicKey;

    private byte[] signKey;
    private byte[] verifyKey;

    private byte[] signSeed;

    public DualSecret() {
        publicKey = new byte[Sodium.crypto_box_curve25519xsalsa20poly1305_publickeybytes()];
        privateKey = new byte[Sodium.crypto_box_curve25519xsalsa20poly1305_secretkeybytes()];
        Sodium.crypto_box_curve25519xsalsa20poly1305_keypair(publicKey, privateKey);

        signSeed = new Random().randomBytes(Sodium.crypto_sign_ed25519_seedbytes());
        verifyKey = new byte[Sodium.crypto_sign_ed25519_publickeybytes()];
        signKey = new byte[Sodium.crypto_sign_ed25519_secretkeybytes()];
        Sodium.crypto_sign_ed25519_seed_keypair(verifyKey, signKey, signSeed);
    }

    public DualSecret(byte[] secretKey, byte[] signSeed) {
        checkLength(secretKey, Sodium.crypto_box_curve25519xsalsa20poly1305_secretkeybytes());
        this.privateKey = secretKey;

        Point point = new Point();
        this.publicKey = point.mult(privateKey).toBytes();
        checkLength(publicKey, Sodium.crypto_box_curve25519xsalsa20poly1305_publickeybytes());

        this.signSeed = signSeed;

        this.verifyKey = new byte[Sodium.crypto_sign_ed25519_publickeybytes()];
        this.signKey = new byte[Sodium.crypto_sign_ed25519_secretkeybytes()];
        Sodium.crypto_sign_seed_keypair(verifyKey, signKey, signSeed);
    }

    public PublicKeyPair getPublicKeyPair() {
        return new PublicKeyPair(getPublicKey(), getVerifyKey());
    }

    public PrivateKey getPrivateKey() {
        return new PrivateKey(privateKey);
    }

    public PublicKey getPublicKey() {
        return new PublicKey(publicKey);
    }

    public VerifyKey getVerifyKey() {
        return new VerifyKey(verifyKey);
    }

    public byte[] getSignSeed() {
        return signSeed;
    }

    public SigningKey getSigningKey() {
        return new SigningKey(signKey);
    }
}
