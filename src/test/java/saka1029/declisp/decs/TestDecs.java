package saka1029.declisp.decs;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

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

    static boolean eq(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) == 0;
    }

    static boolean eq(BigDecimal[] a, BigDecimal[] b) {
        int len = a.length;
        if (b.length != len)
            return false;
        for (int i = 0; i < len; ++i)
            if (!eq(a[i], b[i]))
                return false;
        return true;
    }

    @Test 
    public void testArithmetic() {
        assertTrue(eq(arr(dec(0)), arithmetic(mat(arr()), BigDecimal.ZERO, (a, b) -> a.add(b), BigDecimal.class)));
    }
}
