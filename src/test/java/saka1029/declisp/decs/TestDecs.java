package saka1029.declisp.decs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Test;
import static saka1029.declisp.decs.Decs.*;

public class TestDecs {

    static BigDecimal dec(double value) {
        return BigDecimal.valueOf(value);
    }

    static BigDecimal[] arr(double... elements) {
        BigDecimal[] result = new BigDecimal[elements.length];
        for (int i = 0; i < elements.length; ++i)
            result[i] = BigDecimal.valueOf(elements[i]);
        return result;
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

    static void assertEq(BigDecimal[] expected, BigDecimal[] actual) {
        if (!eq(expected, actual))
            fail("%s != %s".formatted(Arrays.toString(expected), Arrays.toString(actual)));
    }

    @Test 
    public void testArithmetic() {
        assertEq(arr(), arithmetic(mat(arr()), BigDecimal.ZERO, (a, b) -> a.add(b), BigDecimal.class));
        assertEq(arr(1, 2), arithmetic(mat(arr(1, 2)), BigDecimal.ZERO, (a, b) -> a.add(b), BigDecimal.class));
        assertEq(arr(4, 5), arithmetic(mat(arr(1, 2), arr(3)), BigDecimal.ZERO, (a, b) -> a.add(b), BigDecimal.class));
        assertEq(arr(5, 7, 9), arithmetic(mat(arr(1, 2, 3), arr(4, 5, 6)), BigDecimal.ZERO, (a, b) -> a.add(b), BigDecimal.class));
        assertEq(arr(12, 14, 16), arithmetic(mat(arr(1, 2, 3), arr(4, 5, 6), arr(7)), BigDecimal.ZERO, (a, b) -> a.add(b), BigDecimal.class));
        try {
            arithmetic(mat(arr(1, 2, 3), arr(4, 5)), BigDecimal.ZERO, (a, b) -> a.add(b), BigDecimal.class);
            fail();
        } catch (DecsException e) {
            assertEquals("illegal row length 2", e.getMessage());
        }
    }

    @Test 
    public void testPolynomialAdd() {
        assertEq(arr(), polynomial(mat(arr()), Decs::polynomialAdd, BigDecimal.class));
        assertEq(arr(1, 2), polynomial(mat(arr(1, 2)), Decs::polynomialAdd, BigDecimal.class));
        assertEq(arr(1, 5), polynomial(mat(arr(1, 2), arr(3)), Decs::polynomialAdd, BigDecimal.class));
        assertEq(arr(5, 7, 9), polynomial(mat(arr(1, 2, 3), arr(4, 5, 6)), Decs::polynomialAdd, BigDecimal.class));
        assertEq(arr(5, 7, 16), polynomial(mat(arr(1, 2, 3), arr(4, 5, 6), arr(7)), Decs::polynomialAdd, BigDecimal.class));
        assertEq(arr(1,6,8), polynomial(mat(arr(1, 2, 3), arr(4, 5)), Decs::polynomialAdd, BigDecimal.class));
    }
}
