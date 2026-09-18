package saka1029.declisp;

import java.lang.reflect.Array;
import java.math.BigDecimal;

interface Converter<T> {
    Class<T> clazz();
    Expr single(T t);
    T single(Expr e);

    @SuppressWarnings("unchecked")
    default T[] array(int size) {
        return (T[])Array.newInstance(clazz(), size);
    }

    @SuppressWarnings("unchecked")
    default T[][] matrix(int size) {
        return (T[][]) Array.newInstance(clazz(), size, 0);
    }

    default boolean isInstance(Expr e) { return clazz().isInstance(e); }

    default T[] array(Expr e) {
        T[] result = e.stream().map(x -> single(x)).toArray(x -> array(x));
        if (result.length == 0) {
            result = array(1);
            result[0] = single(e);
        }
        return result;
    }
    default T[][] matrix(Expr e) {
        return e.stream().map(x -> array(x)).toArray(x -> matrix(x));
    }

    public static final Converter<BigDecimal> DEC = new Converter<>() {
        @Override public Class<BigDecimal> clazz() { return BigDecimal.class; }
        @Override public Expr single(BigDecimal t) { return new Dec(t); }
        @Override public BigDecimal single(Expr e) { return e.cast(Dec.class).value; }
    };
    public static final Converter<Boolean> BOOL = new Converter<>() {
        @Override public Class<Boolean> clazz() { return Boolean.class; }
        @Override public Expr single(Boolean t) { return Bool.of(t); }
        @Override public Boolean single(Expr e) { return e.cast(Bool.class).value; }
    };
}