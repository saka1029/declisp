package saka1029.declisp.decs;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

public class Decs {

    private Decs() {}

    public static MathContext MC = MathContext.DECIMAL128;

    public static BigDecimal add(BigDecimal a, BigDecimal b) { return a.add(b, MC); }
    public static BigDecimal multiply(BigDecimal a, BigDecimal b) { return a.multiply(b, MC); }
    public static BigDecimal divide(BigDecimal a, BigDecimal b) { return a.divide(b, MC); }
    public static boolean equal(BigDecimal a, BigDecimal b) { return a.compareTo(b) == 0; }
    public static boolean equal(BigDecimal[] a, BigDecimal[] b) {
        int length = a.length;
        if (b.length != length)
            return false;
        for (int i = 0; i < length; ++i)
            if (!equal(a[i], b[i]))
                return false;
        return true;
    }
    public static BigDecimal dec(double value) { return BigDecimal.valueOf(value); }
    public static BigDecimal[] decs(double... values) {
        int length = values.length;
        BigDecimal[] result = new BigDecimal[length];
        for (int i = 0; i < length; ++i)
            result[i] = BigDecimal.valueOf(values[i]);
        return result;
    }
    public static BigDecimal[][] matrix(BigDecimal[]... decs) {
        return decs;
    }

    public static class DecsException extends RuntimeException {
        public DecsException(String format, Object... args) {
            super(format.formatted(args));
        }
        public DecsException(Throwable t) {
            super(t);
        }

    }
    @SuppressWarnings("unchecked")
    static <T> T[] array(Class<T> clazz, int size) {
        return (T[])Array.newInstance(clazz, size);
    }

    @SafeVarargs
    static <T> T[] array(T... e) {
        return e;
    }

    public static BigDecimal[] polynomialAdd(BigDecimal[] left, BigDecimal[] right) {
        if (left.length < right.length) {
            BigDecimal[] t = left; left = right; right = t;
        }
        int ll = left.length, rl = right.length;
        BigDecimal[] result = left.clone();
        for (int i = ll - 1, j = rl - 1; j >= 0; --i, --j)
            result[i] = add(result[i], right[j]);
        return result;
    }

    public static BigDecimal[]  polynomialMult(BigDecimal[] left, BigDecimal[] right) {
        int ll = left.length, rl = right.length;
        BigDecimal[] result = new BigDecimal[ll + rl - 1];
        Arrays.fill(result, BigDecimal.ZERO);
        for (int i = 0; i < ll; ++i)
            for (int j = 0, k = i; j < rl; ++j, ++k)
                result[k] = add(result[k], multiply(left[i], right[j]));
        return result;
    };

    public static <T> T[] polynomial(T[][] mat, BinaryOperator<T[]> operator, Class<T> clazz) {
        if (mat.length == 0)
            return array();
        T[] result = mat[0];
        for (int i = 1, len = mat.length; i < len; ++i)
            result = operator.apply(result, mat[i]);
        return result;
    }

    public static <T> T[] arithmetic(T[][] mat, T unit, BinaryOperator<T> operator, Class<T> clazz) {
        if (mat.length == 0)
            return array(unit);
        int maxRowSize = Stream.of(mat).mapToInt(row -> row.length).max().getAsInt();
        for (T[] e : mat) {
            int length = e.length;
            if (length != 1 && length != maxRowSize)
                throw new DecsException("illegal row length %d", length);
        }
        T[] result = array(clazz, maxRowSize);
        Arrays.fill(result, unit);
        for (int c = 0; c < maxRowSize; ++c) {
            T prev = null;
            for (int r = 0, rmax = mat.length; r < rmax; ++r) {
                T adder = mat[r][c >= mat[r].length ? 0 : c];
                if (r == 1)
                    result[c] = prev;
                result[c] = operator.apply(result[c], adder);
                prev = adder;
            }
        }
        return result;
    }
}
