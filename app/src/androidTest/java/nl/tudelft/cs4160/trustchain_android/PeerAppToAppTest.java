package nl.tudelft.cs4160.trustchain_android;

import android.content.Context;
import android.location.Address;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class PeerAppToAppTest extends TestCase {
    String id1;
    String id2;
    InetSocketAddress address;

    @Override
    protected void setUp() {
        // not sure if the setUp works correctly
        address = mock(InetSocketAddress.class);
    }

    @Test
    public void testEqual() {
        PeerAppToApp peer1 = new PeerAppToApp(id1, address);
        PeerAppToApp peer2 = new PeerAppToApp(id1, address);
        assertTrue(peer1.equals(peer2));
    }

    @Test
    public void testNotEqual() {
        PeerAppToApp peer1 = new PeerAppToApp("RANDOM", address);
        PeerAppToApp peer2 = new PeerAppToApp("MORERANDOM", address);
        assertFalse(peer1.equals(peer2));
    }
}
