package nl.tudelft.cs4160.trustchain_android.qr;

import android.util.Base64;

import com.google.protobuf.ByteString;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.libsodium.jni.Sodium;

import java.util.Arrays;

import nl.tudelft.cs4160.trustchain_android.Util.DualKey;
import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper;
import nl.tudelft.cs4160.trustchain_android.database.TrustChainDBHelper;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.qr.exception.InvalidDualKeyException;
import nl.tudelft.cs4160.trustchain_android.qr.exception.QRWalletImportException;
import nl.tudelft.cs4160.trustchain_android.qr.models.QRTransaction;
import nl.tudelft.cs4160.trustchain_android.qr.models.QRWallet;

public class TrustChainBlockFactory {
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<QRTransaction> transactionAdapter = moshi.adapter(QRTransaction.class);

    public MessageProto.TrustChainBlock createBlock(QRWallet wallet, TrustChainDBHelper helper, DualKey ownKeyPair) throws QRWalletImportException {
        byte[] myPublicKey = ownKeyPair.getPublicKeyPair().toBytes();

        // Similar to tribler logic.
        // We are likely mis-interpreting their logic and/or their logic is wrong
        // This is part of a POC for one way transfer identities,
        // Dont take this as a reference point for TX.
        // At the time of writing there is no TX api.

        wallet.transaction.totalUp = wallet.transaction.up;
        wallet.transaction.totalDown = wallet.transaction.down;

        QRTransaction tx;
        try {
            ByteString tx_data = helper.getLatestBlock(myPublicKey).getTransaction();
            String tx_string = tx_data.toStringUtf8();
            tx = transactionAdapter.fromJson( tx_string);

            wallet.transaction.totalUp += tx.totalUp;
            wallet.transaction.totalDown += tx.totalDown;
        } catch (Exception e) {

        }


        String transactionString = transactionAdapter.toJson(wallet.transaction);
        DualKey walletKeyPair = getKeyPairFromWallet(wallet);

        MessageProto.TrustChainBlock identityHalfBlock = reconstructTemporaryIdentityHalfBlock(wallet);

        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createBlock(transactionString.getBytes(), helper, myPublicKey, identityHalfBlock, walletKeyPair.getPublicKeyPair().toBytes());

        block = TrustChainBlockHelper.sign(block, ownKeyPair.getSigningKey());

        return block;
    }

    public MessageProto.TrustChainBlock reconstructTemporaryIdentityHalfBlock(QRWallet wallet) throws InvalidDualKeyException {
        String transactionString = transactionAdapter.toJson(wallet.transaction);

        DualKey walletKeyPair = getKeyPairFromWallet(wallet);

        MessageProto.TrustChainBlock block = MessageProto.TrustChainBlock.newBuilder().
                setTransaction(ByteString.copyFromUtf8(transactionString))
                .setPublicKey(ByteString.copyFrom(walletKeyPair.getPublicKeyPair().toBytes()))
                .setSequenceNumber(wallet.block.sequenceNumber)
                .setPreviousHash(ByteString.copyFrom(Base64.decode(wallet.block.blockHashBase64, Base64.DEFAULT)))
                .setLinkPublicKey(ByteString.copyFrom(walletKeyPair.getPublicKeyPair().toBytes()))
                .build();
        MessageProto.TrustChainBlock signedBlock = TrustChainBlockHelper.sign(block, walletKeyPair.getSigningKey());
        return signedBlock;
    }


    private DualKey getKeyPairFromWallet(QRWallet wallet) throws InvalidDualKeyException {
        byte[] keyBytes = Base64.decode(wallet.privateKeyBase64, Base64.DEFAULT);
        return readKeyPair(keyBytes);
    }

    private DualKey readKeyPair(byte[] message) throws InvalidDualKeyException {
        String check = "LibNaCLSK:";
        byte[] expectedCheckByteArray = check.getBytes();
        byte[] checkByteArray = Arrays.copyOfRange(message, 0, expectedCheckByteArray.length);

        if (!(Arrays.equals(expectedCheckByteArray, checkByteArray))) {
            throw new InvalidDualKeyException("Private key does not match expected format");
        }

        int pkLength = Sodium.crypto_box_curve25519xsalsa20poly1305_secretkeybytes();
        int seedLength = Sodium.crypto_sign_ed25519_seedbytes();

        int expectedLength = expectedCheckByteArray.length + pkLength + seedLength;
        if (message.length != expectedLength) {
            throw new InvalidDualKeyException("Expected key length " + expectedLength + " but got " + message.length);
        }

        byte[] pk = Arrays.copyOfRange(message, expectedCheckByteArray.length, expectedCheckByteArray.length + pkLength); // first group is pk
        byte[] signSeed = Arrays.copyOfRange(message, expectedCheckByteArray.length + pkLength, expectedCheckByteArray.length + pkLength + seedLength); // second group is seed
        return new DualKey(pk, signSeed);
    }
}
