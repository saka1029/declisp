package saka1029.declisp.decs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Test;
import static saka1029.declisp.decs.Decs.*;

public class TestDecs {

    static void assertEq(BigDecimal[] expected, BigDecimal[] actual) {
        if (!equal(expected, actual))
            fail("%s != %s".formatted(Arrays.toString(expected), Arrays.toString(actual)));
    }

    @Test 
    public void testArithmetic() {
        assertEq(decs(), arithmetic(matrix(decs()), BigDecimal.ZERO, Decs::add, BigDecimal.class));
        assertEq(decs(1, 2), arithmetic(matrix(decs(1, 2)), BigDecimal.ZERO, Decs::add, BigDecimal.class));
        assertEq(decs(4, 5), arithmetic(matrix(decs(1, 2), decs(3)), BigDecimal.ZERO, Decs::add, BigDecimal.class));
        assertEq(decs(5, 7, 9), arithmetic(matrix(decs(1, 2, 3), decs(4, 5, 6)), BigDecimal.ZERO, Decs::add, BigDecimal.class));
        assertEq(decs(12, 14, 16), arithmetic(matrix(decs(1, 2, 3), decs(4, 5, 6), decs(7)), BigDecimal.ZERO, Decs::add, BigDecimal.class));
        try {
            arithmetic(matrix(decs(1, 2, 3), decs(4, 5)), BigDecimal.ZERO, Decs::add, BigDecimal.class);
            fail();
        } catch (DecsException e) {
            assertEquals("illegal row length 2", e.getMessage());
        }
    }

    @Test 
    public void testPolynomialAdd() {
        assertEq(decs(), polynomial(matrix(decs()), Decs::polynomialAdd, BigDecimal.class));
        assertEq(decs(1, 2), polynomial(matrix(decs(1, 2)), Decs::polynomialAdd, BigDecimal.class));
        assertEq(decs(1, 5), polynomial(matrix(decs(1, 2), decs(3)), Decs::polynomialAdd, BigDecimal.class));
        assertEq(decs(5, 7, 9), polynomial(matrix(decs(1, 2, 3), decs(4, 5, 6)), Decs::polynomialAdd, BigDecimal.class));
        assertEq(decs(5, 7, 16), polynomial(matrix(decs(1, 2, 3), decs(4, 5, 6), decs(7)), Decs::polynomialAdd, BigDecimal.class));
        assertEq(decs(1,6,8), polynomial(matrix(decs(1, 2, 3), decs(4, 5)), Decs::polynomialAdd, BigDecimal.class));
    }
}
