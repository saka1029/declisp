package saka1029.declisp.decs;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Arrays;
// import java.util.function.BinaryOperator;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

public class Decs {

    private Decs() {}

    @SuppressWarnings("unchecked")
    static <T> T[] array(Class<T> clazz, int size) {
        return (T[])Array.newInstance(clazz, 1);
    }

    @SafeVarargs
    static <T> T[] array(T... e) {
        return e;
    }

    public static BigDecimal[] polinomialAdd(BigDecimal[] left, BigDecimal[] right) {
        if (left.length < right.length) {
            BigDecimal[] t = left; left = right; right = t;
        }
        int ll = left.length, rl = right.length;
        BigDecimal[] result = left.clone();
        for (int i = ll - 1, j = rl - 1; j >= 0; --i, --j)
            result[i] = result[i].add(right[j]);
        return result;
    }

    public static BigDecimal[]  polinomialMult(BigDecimal[] left, BigDecimal[] right) {
        int ll = left.length, rl = right.length;
        BigDecimal[] result = new BigDecimal[ll + rl - 1];
        Arrays.fill(result, BigDecimal.ZERO);
        for (int i = 0; i < ll; ++i)
            for (int j = 0, k = i; j < rl; ++j, ++k)
                result[k] = result[k].add(left[i].multiply(right[j]));
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
