package saka1029.declisp.decs;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Arrays;
// import java.util.function.BinaryOperator;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

public class Decs {

    private Decs() {}

    // static BigDecimal unary(BigDecimal[] left, BigDecimal unit, BinaryOperator<BigDecimal> operator) {
    //     BigDecimal result = unit, prev = null;
    //     int i = 0;
    //     for (BigDecimal e : left) {
    //         if (i == 1)
    //             result = prev;
    //         result = operator.apply(result, e);
    //         prev = e;
    //         ++i;
    //     }
    //     return result;
    // }

    // public static BigDecimal add(BigDecimal[] left) {
    //     return unary(left, BigDecimal.ZERO, BigDecimal::add);
    // }

    public static BigDecimal[] add(BigDecimal[] left, BigDecimal[] right) {
        if (left.length < right.length) {
            BigDecimal[] t = left; left = right; right = t;
        }
        int ll = left.length, rl = right.length;
        BigDecimal[] result = left.clone();
        for (int i = ll - 1, j = rl - 1; j >= 0; --i, --j)
            result[i] = result[i].add(right[j]);
        return result;
    }

    public static BigDecimal[]  mult(BigDecimal[] left, BigDecimal[] right) {
        int ll = left.length, rl = right.length;
        BigDecimal[] result = new BigDecimal[ll + rl - 1];
        Arrays.fill(result, BigDecimal.ZERO);
        for (int i = 0; i < ll; ++i)
            for (int j = 0, k = i; j < rl; ++j, ++k)
                result[k] = result[k].add(left[i].multiply(right[j]));
        return result;
    };

    interface Constructor<T> {
        Class<T> clazz();

        default T[] array(T arg) {
            T[] result = array(1);
            result[0] = arg;
            return result;
        }

        @SuppressWarnings("unchecked")
        default T[] array(int size) {
            return (T[])Array.newInstance(clazz(), size);
        }
    }

    public static <T> T[] arithmetic(T[][] mat, T unit, BinaryOperator<T> op, Constructor<T> conv) {
        if (mat.length == 0)
            return conv.array(unit);
        int maxRowSize = Stream.of(mat).mapToInt(row -> row.length).max().getAsInt();
        T[] result = conv.array(maxRowSize);
        Arrays.fill(result, unit);
        for (int c = 0; c < maxRowSize; ++c) {
            T prev = null;
            for (int r = 0, rmax = mat.length; r < rmax; ++r) {
                T adder = mat[r][c >= mat[r].length ? 0 : c];
                if (r == 1)
                    result[c] = prev;
                result[c] = op.apply(result[c], adder);
                prev = adder;
            }
        }
        return result;
    }

}
