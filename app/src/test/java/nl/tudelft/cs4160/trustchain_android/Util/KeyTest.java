package nl.tudelft.cs4160.trustchain_android.Util;

import org.junit.Before;
import org.junit.Test;

import java.security.KeyPair;

import nl.tudelft.cs4160.trustchain_android.Util.Key;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Boning on 12/17/2017.
 */
public class KeyTest  {
    KeyPair kp;
    KeyPair kp2;
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
        byte[] signature = Key.sign(kp.getPrivate(), ba1);
        assertTrue(Key.verify(kp.getPublic(), ba1, signature));
    }

    @Test
    public void testSignAndVerfiyInvalidPublicKey() {
        byte[] signature = Key.sign(kp.getPrivate(), ba1);
        assertFalse(Key.verify(kp2.getPublic(), ba1, signature));
    }

}
