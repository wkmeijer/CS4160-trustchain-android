package nl.tudelft.cs4160.trustchain_android.Util;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.Assert.assertEquals;
import static nl.tudelft.cs4160.trustchain_android.Util.Util.ellipsize;
import static org.junit.Assert.*;

/**
 * Created by meijer on 10-11-17.
 */
@RunWith(JUnit4.class)
public class UtilTest {
    String longString;
    String longStringFirst6;
    String shortString;
    String shortStringFirst6;

    @Before
    public void initialization() {
        longString = "0002SDVnnuisdvbhhduifvbsdiuvndskjvcxnvjkdsvusidvbsdjovcnvjkcxv0002SDVnnuisdvbhhduifvbsdiuvndskjvcxnvjkdsvusidvbsdjovcnvjkcxv0002SDVnnuisdvbhhduifvbsdiuvndskjvcxnvjkdsvusidvbsdjovcnvjkcxv";
        longStringFirst6 = "0(..)v";
        shortString = "viudnfvidufdvn84938enfivdnidvn";
        shortStringFirst6 = "v(..)n";
    }

    @Test
    public void ellipsizeTest() throws Exception {
        String input = "12345678910";
        String expected = "1(..)0";
        Assert.assertEquals(expected,ellipsize(input,5));
    }

    @Test
    public void ellipsizeTest2() throws Exception {
        String input = "12345678910";
        String expected = "12(..)10";
        Assert.assertEquals(expected,ellipsize(input,8));
    }

    @Test
    public void ellipsizeTest3() throws Exception {
        String input = "12345678910";
        Assert.assertEquals(input,ellipsize(input,11));
    }

    @Test
    public void ellipsizeTest4() throws Exception {
        String input = "12345678910";
        String expected = "1(..)0";
        Assert.assertEquals(expected,ellipsize(input,6));
    }

    @Test
    public void ellipsizeTest5() throws Exception {
        String input = "12";
        String expected = "12";
        Assert.assertEquals(expected,ellipsize(input,5));
    }
    @Test
    public void testEllipsizeShort() {
        String a = Util.ellipsize(shortString, 6);
        assertEquals(a, shortStringFirst6);
    }

    @Test
    public void testEllipsizeLong() {
        assertEquals(Util.ellipsize(longString, 6), longStringFirst6);
    }

    @Test
    public void testEllipsizeFull() {
        assertEquals(Util.ellipsize(shortString, 1000), shortString);
    }

}