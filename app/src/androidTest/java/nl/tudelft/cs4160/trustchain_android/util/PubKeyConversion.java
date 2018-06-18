package nl.tudelft.cs4160.trustchain_android.util;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libsodium.jni.keys.PublicKey;

import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;


@RunWith(AndroidJUnit4.class)
public class PubKeyConversion {

    @Test
    public void testConversion1() throws Exception {
        DualSecret keyPair = Key.createNewKeyPair();
        PublicKey pubKey = keyPair.getPublicKey();
        byte[] keyBytes = pubKey.toBytes();
        Assert.assertEquals(pubKey.toString(), new PublicKey(keyBytes).toString());
    }
}
