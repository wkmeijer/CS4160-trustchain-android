package nl.tudelft.cs4160.trustchain_android.main;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.List;

import nl.tudelft.cs4160.trustchain_android.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.util.Util;

import static org.junit.Assert.assertEquals;

public class PeerListAdapterTest {
    @Mock
    Context context;
    int resource = 0;
    @Mock
    List<Peer> peerList;
    @Mock
    CoordinatorLayout coordinatorLayout;

    PeerListAdapter pla = new PeerListAdapter(context, resource, peerList, coordinatorLayout);

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void timeToStringTestSeconds1() {
        long time = 1000;
        String expected = " 1s";
        assertEquals(expected, Util.timeToString(time));
    }

    @Test
    public void timeToStringTestSeconds2() {
        long time = 2000;
        String expected = " 2s";
        assertEquals(expected, Util.timeToString(time));
    }

    @Test
    public void timeToStringTestSeconds3() {
        long time = 0;
        String expected = " 0s";
        assertEquals(expected, Util.timeToString(time));
    }

    @Test
    public void timeToStringTestHours1() {
        long time = 3600000;
        String expected = " 1h0m";
        assertEquals(expected, Util.timeToString(time));
    }

    @Test
    public void timeToStringTestHours2() {
        long time = 3800000;
        String expected = " 1h3m";
        assertEquals(expected, Util.timeToString(time));
    }

    @Test
    public void timeToStringTestHours3() {
        long time = 3599999;
        String expected = " 59m59s";
        assertEquals(expected, Util.timeToString(time));
    }

    @Test
    public void timeToStringMinutes1() {
        long time = 200000;
        String expected = " 3m20s";
        assertEquals(expected, Util.timeToString(time));
    }

    @Test
    public void timeToStringMinutes2() {
        long time = 60000;
        String expected = " 1m0s";
        assertEquals(expected, Util.timeToString(time));
    }

    @Test
    public void timeToStringMinutes3() {
        long time = 60001;
        String expected = " 1m0s";
        assertEquals(expected, Util.timeToString(time));
    }

    @Test
    public void timeToStringMinutes4() {
        long time = 59999;
        String expected = " 59s";
        assertEquals(expected, Util.timeToString(time));
    }

    @Test
    public void timeToStringTestDays() {
        long time = 999999999;
        String expected = "";
        assertEquals(expected, Util.timeToString(time));
    }
}