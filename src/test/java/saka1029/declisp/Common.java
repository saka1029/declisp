package saka1029.declisp;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.Assert.fail;
import static saka1029.declisp.DecLisp.*;

public class Common {

    public static void assertEqualsBigDeciml(BigDecimal expected, BigDecimal actual) {
        if (!equal(expected, actual))
            fail("%s != %s".formatted(expected, actual));
    }

    public static void assertEqualsBigDeciml(BigDecimal[] expected, BigDecimal[] actual) {
        if (!equal(expected, actual))
            fail("%s != %s".formatted(Arrays.toString(expected), Arrays.toString(actual)));
    }

    public static void assertEqualsBigDeciml(BigDecimal[][] expected, BigDecimal[][] actual) {
        if (!equal(expected, actual))
            fail("%s != %s".formatted(Arrays.deepToString(expected), Arrays.deepToString(actual)));
    }

}
