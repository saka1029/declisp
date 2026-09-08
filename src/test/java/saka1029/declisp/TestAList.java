package saka1029.declisp;

import static java.util.stream.Collectors.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import org.junit.Test;

public class TestAList {
    public static class AListException extends RuntimeException {
        public AListException(String format, Object... args) {
            super(format.formatted(args));
        }
    }
    public static class KV {
        Symbol k; Expr v; KV prev;
        public KV(Symbol k, Expr v, KV prev) { this.k = k; this.v = v; this.prev = prev; }
    }
    public static class Env {
        KV kv;
        public Env() { this.kv = null; }
        public Env(Env p) { this.kv = p.kv; }
    }
    public static void define(Env env, Symbol k, Expr v) { env.kv = new KV(k, v, env.kv); }
    public static Expr get(Env env, Symbol k) {
        for (KV x = env.kv; x != null; x = x.prev)
            if (x.k.equals(k))
                return x.v;
        throw new AListException("get: '%s' not defined", print(k));
    }
    public static void set(Env env, Symbol k, Expr v) {
        for (KV x = env.kv; x != null; x = x.prev)
            if (x.k.equals(k))
                x.v = v;
        throw new AListException("set: '%s' not defined", print(k));
    }

    interface Expr {}
    public record Bool(boolean value) implements Expr {}
    public record Dec(BigDecimal value) implements Expr {
        @Override public final boolean equals(Object r) {
            return r instanceof Dec d && value.compareTo(d.value) == 0;
        }
        @Override public final String toString() {
            return value.toString().replaceFirst("\\.0$", "");
        }
    }
    public record Symbol(String value) implements Expr {}
    public record List(Expr... elements) implements Expr {
        @Override
        public final boolean equals(Object r) {
            return r instanceof List l && Arrays.equals(l.elements, elements);
        }
    }
    public interface Apply extends Expr { Expr apply(Expr args, Env env); }
    public interface Proc extends Apply {
        Expr apply(Expr evaled);
        default Expr apply(Expr args, Env env) {
            return apply(evlis(args, env));
        }
    }

    public static final Bool TRUE = new Bool(true);
    public static final Bool FALSE = new Bool(false);
    public static Bool b(boolean v) { return v ? TRUE : FALSE; }
    public static boolean b(Expr e) { return cast(e, Bool.class).value; }
    public static Dec d(double v) { return new Dec(BigDecimal.valueOf(v)); }
    public static Dec d(BigDecimal v) { return new Dec(v); }
    public static BigDecimal d(Expr e) { return cast(e, Dec.class).value; }
    public static Symbol s(String v) { return new Symbol(v); }
    public static String s(Expr e) { return cast(e, Symbol.class).value; }
    public static Symbol QUOTE = s("quote");
    public static List list(Expr... elements) { return new List(elements); }
    public static Expr[] array(Expr e) { return cast(e, List.class).elements; }
    public static <T> T cast(Expr e, Class<T> c) {
        if (c.isInstance(e))
            return c.cast(e);
        else
            throw new AListException("cannot cast '%s' to '%s'",
                print(e), c.getSimpleName());
    }

    public static Expr car(Expr e) { return cast(e, List.class).elements[0]; }
    public static Expr cdr(Expr e) {
        Expr[] es = cast(e, List.class).elements;
        return new List(Arrays.copyOfRange(es, 1, es.length));
    }
    public static Stream<Expr> stream(Expr e) {
        return Stream.of(cast(e, List.class).elements);
    }
    public static BigDecimal[] darray(Expr e) {
        return stream(e).map(x->d(x)).toArray(BigDecimal[]::new);
    }
    public static List cons(Expr a, Expr b) {
        Expr[] be = cast(b, List.class).elements;
        int blen = be.length;
        Expr[] r = new Expr[blen + 1];
        r[0] = a;
        System.arraycopy(be, 0, r, 1, blen);
        return new List(r);
    }

    public static String print(Expr e) {
        return switch (e) {
            case Bool b -> Boolean.toString(b.value);
            case Dec d -> d.toString();
            case Symbol s -> s.value;
            case List l -> Stream.of(l.elements).map(x -> print(x)).collect(joining(" ", "(", ")"));
            default -> Objects.toString(e);
        };
    }

    public static Expr eval(Expr e, Env env) {
        return switch (e) {
            case Bool b -> b;
            case Dec d -> d;
            case Symbol s -> get(env, s);
            case List l -> cast(eval(car(l), env),Apply.class).apply(cdr(l), env);
            default -> throw new AListException("eval: unknown type %s " + e);
        };
    }

    public static Expr evlis(Expr list, Env env) {
        return new List(stream(list)
            .map(e -> eval(e, env))
            .toArray(Expr[]::new));
    }
    static <T> T arithmetic(T[] args, T start, BinaryOperator<T> operator) {
        T prev = null;
        for (int i = 0, len = args.length; i < len; ++i) {
            T value = args[i];
            if (i == 1)
                start = prev;
            start = operator.apply(start, value);
            prev = value;
        }
        return start;
    }

    // interface IntBinaryOperator { int apply(int a, int b); }
    // static Int intArithmetic(Expr args, int start, IntBinaryOperator operator) {
    //     Expr[] v = array(args);
    //     int prev = 0;
    //     for (int i = 0, len = v.length; i < len; ++i) {
    //         int value = i(v[i]);
    //         if (i == 1)
    //             start = prev;
    //         start = operator.apply(start, value);
    //         prev = value;
    //     }
    //     return i(start);
    // }

    public static Env environment() {
        Env env = new Env();
        define(env, QUOTE, (Apply) (a, e) -> car(a));
        define(env, s("car"), (Proc) a -> car(car(a)));
        define(env, s("cdr"), (Proc) a -> cdr(car((a))));
        define(env, s("cons"), (Proc) a -> cons(car(a), car(cdr(a))));
        define(env, s("+"), (Proc) a -> d(arithmetic(darray(a), BigDecimal.ZERO, (x, y) -> x.add(y))));
        define(env, s("-"), (Proc) a -> d(arithmetic(darray(a), BigDecimal.ZERO, (x, y) -> x.subtract(y))));
        define(env, s("*"), (Proc) a -> d(arithmetic(darray(a), BigDecimal.ONE, (x, y) -> x.multiply(y))));
        define(env, s("/"), (Proc) a -> d(arithmetic(darray(a), BigDecimal.ONE, (x, y) -> x.divide(y, MathContext.DECIMAL128))));
        return env;
    }

    @Test 
    public void testCarCdr() {
        assertEquals(d(0), car(list(d(0), list(d(1)))));
        assertEquals(list(list(d(1))), cdr(list(d(0), list(d(1)))));
        try {
            assertEquals(s("a"), car(s("a")));
            fail();
        } catch (AListException e) {
            assertEquals("cannot cast 'a' to 'List'", e.getMessage());
        }
    }

    @Test 
    public void testCons() {
        assertEquals(list(d(0), d(1)), cons(d(0), list(d(1))));
        try {
            assertEquals(list(d(0), d(1)), cons(d(0), d(1)));
            fail();
        } catch (AListException e) {
            assertEquals("cannot cast '1' to 'List'", e.getMessage());
        }
    }

    @Test 
    public void testPrint() {
        assertEquals("true", print(TRUE));
        assertEquals("false", print(FALSE));
        assertEquals("123", print(d(123)));
        assertEquals("abc", print(s("abc")));
        assertEquals("(+ 1 2)", print(list(s("+"), d(1), d(2))));
        assertEquals("(1 (true false) 2)", print(list(d(1), list(TRUE, FALSE), d(2))));
    }

    @Test 
    public void testEval() {
        Env env = environment();
        assertEquals(s("a"), eval(list(QUOTE, s("a")), env));
        assertEquals(s("a"), eval(list(s("car"), list(QUOTE, list(s("a"), s("b")))), env));
        assertEquals(list(s("b")), eval(list(s("cdr"), list(QUOTE, list(s("a"), s("b")))), env));
        assertEquals(list(d(1), d(2), d(3)), eval(list(s("cons"), d(1), list(QUOTE, list(d(2), d(3)))), env));
        assertEquals(d(0), eval(list(s("+")), env));
        assertEquals(d(2), eval(list(s("+"), d(2)), env));
        assertEquals(d(3), eval(list(s("+"), d(1), d(2)), env));
        assertEquals(d(6), eval(list(s("+"), d(1), d(2), d(3)), env));
        assertEquals(d(1), eval(list(s("/")), env));
        assertEquals(d(0.5), eval(list(s("/"), d(2)), env));
        assertEquals(d(2), eval(list(s("/"), d(4), d(2)), env));
        assertEquals(d(4), eval(list(s("/"), d(24), d(2), d(3)), env));
    }
}