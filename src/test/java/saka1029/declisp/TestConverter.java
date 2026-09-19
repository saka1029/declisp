package saka1029.declisp;

import static org.junit.Assert.assertEquals;
import static saka1029.declisp.DecLisp.*;

import java.math.BigDecimal;

import org.junit.Test;
import static saka1029.declisp.Common.*;

public class TestConverter {

    @Test 
    public void testConverterDEC() {
        assertEquals(BigDecimal.class, Converter.DEC.clazz());
        assertEquals(new Dec(bdec(123)), Converter.DEC.single(bdec(123)));
        assertEqualsBigDeciml(bdec(123), Converter.DEC.single(dec(123)));
        assertEqualsBigDeciml(new BigDecimal[3], Converter.DEC.array(3));
        assertEqualsBigDeciml(new BigDecimal[3][0], Converter.DEC.matrix(3));
        assertEqualsBigDeciml(bdecs(3, 4), Converter.DEC.array(list(dec(3), dec(4))));
        assertEqualsBigDeciml(new BigDecimal[][] {bdecs(1, 2), bdecs(3, 4)}, Converter.DEC.matrix(list(list(dec(1), dec(2)), list(dec(3), dec(4)))));
    }

}
