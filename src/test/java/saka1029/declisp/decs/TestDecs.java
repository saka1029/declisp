package saka1029.declisp.decs;

import static org.junit.Assert.assertArrayEquals;

import java.math.BigDecimal;

import org.junit.Test;
import static saka1029.declisp.decs.Decs.*;

public class TestDecs {

    static BigDecimal dec(double value) {
        return BigDecimal.valueOf(value);
    }

    static BigDecimal[] arr(BigDecimal... elements) {
        return elements;
    }

    static BigDecimal[][] mat(BigDecimal[]... elements) {
        return elements;
    }

    @Test 
    public void testArithmetic() {
        assertArrayEquals(arr(dec(0)), arithmetic(mat(arr()), BigDecimal.ZERO, (a, b) -> a.add(b), BigDecimal.class));
    }
}
