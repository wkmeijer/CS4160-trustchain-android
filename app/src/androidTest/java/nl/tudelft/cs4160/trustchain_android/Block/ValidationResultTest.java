package nl.tudelft.cs4160.trustchain_android;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.block.ValidationResult;

import static org.junit.Assert.assertEquals;

/**
 * Created by Boning on 12/16/2017.
 */

public class ValidationResultTest {

    @Test
    public void testConstructor() {
        ValidationResult vr = new ValidationResult();
        assertEquals(ValidationResult.VALID, vr.getStatus());
        assertEquals("<ValidationResult: VALID>", vr.toString());
    }

    @Test
    public void testPartial() {
        ValidationResult vr = new ValidationResult();
        vr.setPartial();
        assertEquals(ValidationResult.PARTIAL, vr.getStatus());
        assertEquals("<ValidationResult: PARTIAL>", vr.toString());
    }

    @Test
    public void testPartialNext() {
        ValidationResult vr = new ValidationResult();
        vr.setPartialNext();
        assertEquals(ValidationResult.PARTIAL_NEXT, vr.getStatus());
        assertEquals("<ValidationResult: PARTIAL_NEXT>", vr.toString());
    }

    @Test
    public void testPartialPrevious() {
        ValidationResult vr = new ValidationResult();
        vr.setPartialPrevious();
        assertEquals(ValidationResult.PARTIAL_PREVIOUS, vr.getStatus());
        assertEquals("<ValidationResult: PARTIAL_PREVIOUS>", vr.toString());
    }

    @Test
    public void testNoInfo() {
        ValidationResult vr = new ValidationResult();
        vr.setNoInfo();
        assertEquals(ValidationResult.NO_INFO, vr.getStatus());
        assertEquals("<ValidationResult: NO_INFO>", vr.toString());
    }

    @Test
    public void testInvalid() {
        ValidationResult vr = new ValidationResult();
        vr.setInvalid();
        assertEquals(ValidationResult.INVALID, vr.getStatus());
        assertEquals("<ValidationResult: INVALID>", vr.toString());
    }

    @Test
    public void testGetterSetter() {
        ValidationResult vr = new ValidationResult();
        vr.setStatus(3);
        assertEquals(ValidationResult.PARTIAL_PREVIOUS, vr.getStatus());
        vr.setStatus(4);
        assertEquals(ValidationResult.NO_INFO, vr.getStatus());
    }

    @Test
    public void testSetErrors() {
        ValidationResult vr = new ValidationResult();
        List<String> errors = new ArrayList<>();
        String test = "test";
        errors.add(test);
        vr.setErrors(errors);
        assertEquals(test, vr.getErrors().get(0));
    }
}