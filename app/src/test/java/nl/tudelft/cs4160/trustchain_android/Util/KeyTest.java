package nl.tudelft.cs4160.trustchain_android.Util;

import org.junit.Before;
import org.junit.Test;

import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class KeyTest  {
    DualSecret kp;
    DualSecret kp2;
    byte[] ba1;

    @Before
    public void initialization() {
        byte[] byteArray = {0, 2};
        ba1 = byteArray;
        kp = Key.createNewKeyPair();
        kp2 = Key.createNewKeyPair();
    }

    @Test
    public void testSignAndVerfiy() {
        byte[] signature = Key.sign(kp.getSigningKey(), ba1);
        assertTrue(Key.verify(kp.getVerifyKey(), ba1, signature));
    }

    @Test
    public void testSignAndVerfiyInvalidPublicKey() {
        byte[] signature = Key.sign(kp.getSigningKey(), ba1);
        assertFalse(Key.verify(kp2.getVerifyKey(), ba1, signature));
    }

}
