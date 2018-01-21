package nl.tudelft.cs4160.trustchain_android.Util;

import org.libsodium.jni.Sodium;
import org.libsodium.jni.crypto.Point;
import org.libsodium.jni.keys.PrivateKey;
import org.libsodium.jni.keys.PublicKey;
import org.libsodium.jni.keys.VerifyKey;

import static org.libsodium.jni.crypto.Util.checkLength;
import static org.libsodium.jni.crypto.Util.zeros;

public class DualKey {
    private byte[] privateKey;
    private byte[] publicKey;

    private byte[] signPrivateKey;
    private byte[] signPublicKey;

    private byte[] signSeed;

    public DualKey() {
        publicKey = new byte[Sodium.crypto_box_curve25519xsalsa20poly1305_publickeybytes()];
        privateKey = new byte[Sodium.crypto_box_curve25519xsalsa20poly1305_secretkeybytes()];
        Sodium.crypto_box_curve25519xsalsa20poly1305_keypair(publicKey, privateKey);

        signSeed = zeros(Sodium.crypto_sign_ed25519_seedbytes());
        signPublicKey = new byte[Sodium.crypto_sign_ed25519_publickeybytes()];
        signPrivateKey = new byte[Sodium.crypto_sign_ed25519_secretkeybytes()];
        Sodium.crypto_sign_ed25519_seed_keypair(signPublicKey, signPrivateKey, signSeed);
    }

    public DualKey(byte[] secretKey, byte[] signSeed) {
        checkLength(secretKey, Sodium.crypto_box_curve25519xsalsa20poly1305_secretkeybytes());
        this.privateKey = secretKey;

        Point point = new Point();
        this.publicKey = point.mult(privateKey).toBytes();
        checkLength(publicKey, Sodium.crypto_box_curve25519xsalsa20poly1305_publickeybytes());

        this.signSeed = signSeed;

        this.signPublicKey = new byte[Sodium.crypto_sign_ed25519_publickeybytes()];
        this.signPrivateKey = new byte[Sodium.crypto_sign_ed25519_secretkeybytes()];
        Sodium.crypto_sign_seed_keypair(signPublicKey, signPrivateKey, signSeed);
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
        return new VerifyKey(signPublicKey);
    }

    public SigningKey getSigningKey() {
        return new SigningKey(signPrivateKey);
    }
}
