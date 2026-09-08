package saka1029.declisp;

import static java.util.stream.Collectors.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import org.junit.Test;

public class TestAList {
    public static class AListException extends RuntimeException {
        public AListException(String format, Object... args) { super(format.formatted(args)); }
        public AListException(Throwable cause) { super(cause); }
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
    public interface ProcArray extends Proc {
        Expr apply(Expr[] evaled);
        default Expr apply(Expr evaled) {
            return apply(array(evaled));
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
    public static Symbol s(Expr e) { return cast(e, Symbol.class); }
    public static Symbol QUOTE = s("quote");
    public static Symbol LAMBDA = s("lambda");
    public static List NIL = new List();
    public static List list(Expr... elements) { return new List(elements); }
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
    public static Expr[] array(Expr e) { return cast(e, List.class).elements; }
    public static Stream<Expr> stream(Expr e) { return Stream.of(cast(e, List.class).elements); }
    public static BigDecimal[] darray(Expr e) { return stream(e).map(x->d(x)).toArray(BigDecimal[]::new); }
    public static List cons(Expr a, Expr b) {
        Expr[] be = cast(b, List.class).elements;
        int blen = be.length;
        Expr[] r = new Expr[blen + 1];
        r[0] = a;
        System.arraycopy(be, 0, r, 1, blen);
        return new List(r);
    }

    public static String printList(List list) {
        Expr[] v = array(list);
        if (v.length == 2 && v[0].equals(QUOTE))
            return "'" + print(v[1]);
        return Stream.of(list.elements)
            .map(x -> print(x)).collect(joining(" ", "(", ")"));
    }

    public static String print(Expr e) {
        return switch (e) {
            case Bool b -> Boolean.toString(b.value);
            case Dec d -> d.toString();
            case Symbol s -> s.value;
            case List l -> printList(l);
            default -> Objects.toString(e);
        };
    }

    public static Expr eval(Expr e, Env env) {
        return switch (e) {
            case Bool b -> b;
            case Dec d -> d;
            case Symbol s -> get(env, s);
            case List l -> cast(eval(car(l), env), Apply.class).apply(cdr(l), env);
            default -> throw new AListException("eval: unknown type %s " + e);
        };
    }

    public static class Reader {
        public static final Expr EOF = new Expr() {};

        final java.io.Reader reader;
        final StringBuilder buffer = new StringBuilder();
        int ch;

        public Reader(java.io.Reader reader) {
            this.reader = reader;
            this.ch = get();
        }

        public Reader(String source) {
            this(new StringReader(source));
        }

        int get() {
            try {
                ch = reader.read();
                buffer.append((char)ch);    // ch == EOFの時もappendする
                return ch;
            } catch (IOException e) {
                throw new AListException(e);
            }
        }

        int getClear() {
            buffer.setLength(0);
            return get();
        }

        void spaces() {
            while (Character.isWhitespace(ch))
                get();
            buffer.delete(0, buffer.length() - 1);
        }

        Expr list() {
            getClear();     // skip '('
            java.util.List<Expr> list = new ArrayList<>();
            while (true) {
                spaces();
                if (ch == ')') {
                    getClear();  // skip ')'
                    return new List(list.toArray(Expr[]::new));
                // no DOT notation
                // } else if (ch == '.') {
                //     getClear();  // skip '.'
                //     Expr result = DecLisp.list(read(), list);
                //     spaces();
                //     if (ch != ')')
                //         throw new RuntimeException("Reader.list(): ')' expected");
                //     getClear();  // skip ')'
                //     return result;
                }
                Expr e = read();
                if (e == EOF)
                    throw new AListException("Reader.list(): Unexpected EOF");
                list.addLast(e);
            }
        }

        Expr quote() {
            getClear();  // skip '\''
            return new List(QUOTE, read());
        }

        static boolean isDigit(int ch) {
            return ch >= '0' && ch <= '9';
        }

        /**
         * 開始文字は
         * '+' D
         * '+' '.'
         * '-' D
         * '-' '.'
         * Digit
         * '.' D
         * BigDecimalString:
         *     [ '+' | '-' ] ( Digits [ '.' [ Digits ]] | '.' Digits ) [ ('e'|'E') [ '+' | '-'] Digits ]
         * Digits: Digit { Digit }
        */
        Dec decimal() {
            while (isDigit(ch))
                get();
            if (ch == '.')
                do {
                    get();
                } while (isDigit(ch));
            if (ch == 'E' || ch == 'e') {
                get();
                if (ch == '-' || ch == '+')
                    get();
                while (isDigit(ch))
                    get();
            }
            return new Dec(new BigDecimal(buffer.substring(0, buffer.length() - 1)));
        }

        static boolean isSymbolFirst(int ch) {
            return switch (ch) {
                case -1, '(', ')' -> false;
                default -> !Character.isWhitespace(ch) && !isDigit(ch);
            };
        }

        static boolean isSymbolRest(int ch) {
            return isSymbolFirst(ch) || isDigit(ch);
        }

        Expr symbol() {
            while (isSymbolRest(ch))
                get();
            String value = buffer.substring(0, buffer.length() - 1);
            return switch (value) {
                case "true" -> TRUE;
                case "false" -> FALSE;
                default -> new Symbol(value);
            };
        }

        public Expr read() {
            spaces();
            if (ch == -1)
                return EOF;
            else if (ch == '(')
                return list();
            else if (ch == '\'')
                return quote();
            else if (ch == '+')
                return isDigit(get()) ? decimal() : new Symbol("+");
            else if (ch == '-')
                return isDigit(get()) ? decimal() : new Symbol("-");
            else if (isDigit(ch))
                return decimal();
            else if (isSymbolFirst(ch))
                return symbol();
            else 
                throw new AListException("Reader.read(): Unexpected character '%c'", (char)ch);
        }
    }

    public static Expr evlis(Expr list, Env env) {
        return new List(stream(list)
            .map(e -> eval(e, env))
            .toArray(Expr[]::new));
    }
    public static void pairlis(Expr parms, Expr args, Env e) {
        Expr[] p = array(parms), a = array(args);
        for (int i = 0, len = p.length; i < len; ++i)
            define(e, s(p[i]), a[i]);
    }
    public static Expr progn(Expr body, Env e) {
        Expr r = NIL;
        Expr[] v = array(body);
        for (int i = 0, len = v.length; i < len; ++i)
            r = eval(v[i], e);
        return r;
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

    public static Env environment() {
        Env env = new Env();
        define(env, QUOTE, (Apply) (a, e) -> car(a));
        // (define SYMBOL VALUE)
        // (define (SYMBOL ARGS...) BODY) -> (define SYMBOL (lambda (ARGS...) BODY))
        define(env, s("define"), (Apply) (a, e) -> {
            if (car(a) instanceof Symbol s)
                define(e, s, eval(car(cdr(a)), e));
            else
                define(e, s(car(car(a))), eval(cons(LAMBDA, cons(cdr(car(a)), cdr(a))), e));
            return car(a);
        });
        define(env, s("if"), (Apply) (a, e) -> {
            Expr[] v = array(a);
            if (b(eval(v[0], e)))
                return eval(v[1], e);
            else if (v.length >=3)
                return eval(v[2], e);
            else
                return NIL;
        });
        define(env, LAMBDA, (Apply) (a, e) -> {
            Expr parms = car(a), body = cdr(a);
            return (Apply) (aa, ee) -> {
                Env n = new Env(e);
                pairlis(parms, evlis(aa, ee), n);
                return progn(body, n);
            };
        });
        define(env, s("car"), (ProcArray) a -> car(a[0]));
        define(env, s("cdr"), (ProcArray) a -> cdr(a[0]));
        define(env, s("cons"), (ProcArray) a -> cons(a[0], a[1]));
        define(env, s("list"), (Proc) a -> a);
        define(env, s("+"), (Proc) a -> d(arithmetic(darray(a), BigDecimal.ZERO, (x, y) -> x.add(y))));
        define(env, s("-"), (Proc) a -> d(arithmetic(darray(a), BigDecimal.ZERO, (x, y) -> x.subtract(y))));
        define(env, s("*"), (Proc) a -> d(arithmetic(darray(a), BigDecimal.ONE, (x, y) -> x.multiply(y))));
        define(env, s("/"), (Proc) a -> d(arithmetic(darray(a), BigDecimal.ONE, (x, y) -> x.divide(y, MathContext.DECIMAL128))));
        define(env, s("=="), (ProcArray) a -> b(d(a[0]).compareTo(d(a[1])) == 0));
        define(env, s("!="), (ProcArray) a -> b(d(a[0]).compareTo(d(a[1])) != 0));
        define(env, s("<"), (ProcArray) a -> b(d(a[0]).compareTo(d(a[1])) < 0));
        define(env, s("<="), (ProcArray) a -> b(d(a[0]).compareTo(d(a[1])) <= 0));
        define(env, s(">"), (ProcArray) a -> b(d(a[0]).compareTo(d(a[1])) > 0));
        define(env, s(">="), (ProcArray) a -> b(d(a[0]).compareTo(d(a[1])) >= 0));
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
        assertEquals("'abc", print(list(QUOTE, s("abc"))));
        assertEquals("'(1 2)", print(list(QUOTE, list(d(1), d(2)))));
    }

    @Test 
    public void testIf() {
        Env env = environment();
        assertEquals(d(1), eval(list(s("if"), TRUE, d(1), d(0)), env));
        assertEquals(d(0), eval(list(s("if"), FALSE, d(1), d(0)), env));
    }

    @Test 
    public void testEval() {
        Env env = environment();
        assertEquals(s("a"), eval(list(QUOTE, s("a")), env));
        assertEquals(s("a"), eval(list(s("car"), list(QUOTE, list(s("a"), s("b")))), env));
        assertEquals(list(d(0), d(1), d(2)), eval(list(s("list"), d(0), d(1), d(2)), env));
        assertEquals(list(s("b")), eval(list(s("cdr"), list(QUOTE, list(s("a"), s("b")))), env));
        assertEquals(list(d(1), d(2), d(3)), eval(list(s("cons"), d(1), list(QUOTE, list(d(2), d(3)))), env));
        assertEquals(d(0), eval(list(s("+")), env));
        assertEquals(d(2), eval(list(s("+"), d(2)), env));
        assertEquals(d(3), eval(list(s("+"), d(1), d(2)), env));
        assertEquals(d(10), eval(list(s("+"), d(1), list(s("+"), d(2), d(3)), d(4)), env));
        assertEquals(d(1), eval(list(s("/")), env));
        assertEquals(d(0.5), eval(list(s("/"), d(2)), env));
        assertEquals(d(2), eval(list(s("/"), d(4), d(2)), env));
        assertEquals(d(4), eval(list(s("/"), d(24), d(2), d(3)), env));
    }

    @Test 
    public void testCompare() {
        Env env = environment();
        assertEquals(FALSE, eval(list(s("<="), d(1), d(0)), env));
        assertEquals(TRUE, eval(list(s("<="), d(0), d(0)), env));
        assertEquals(TRUE, eval(list(s("<="), d(0), d(1)), env));
        assertEquals(TRUE, eval(list(s(">"), d(1), d(0)), env));
        assertEquals(FALSE, eval(list(s(">"), d(0), d(0)), env));
        assertEquals(FALSE, eval(list(s(">"), d(0), d(1)), env));
    }

    @Test 
    public void testDefine() {
        Env env = environment();
        assertEquals(s("A"), eval(list(s("define"), s("A"), list(s("+"), d(100), d(23))), env));
        assertEquals(d(123), eval(s("A"), env));
        assertEquals(s("fact"), eval(list(s("define"), s("fact"),
            list(LAMBDA, list(s("n")),
                list(s("if"), list(s("<="), s("n"), d(0)),
                    d(1),
                    list(s("*"), s("n"), list(s("fact"), list(s("-"), s("n"), d(1))))))), env));
        assertEquals(d(1), eval(list(s("fact"), d(0)), env));
        assertEquals(d(1), eval(list(s("fact"), d(1)), env));
        assertEquals(d(2), eval(list(s("fact"), d(2)), env));
        assertEquals(d(6), eval(list(s("fact"), d(3)), env));
        assertEquals(d(24), eval(list(s("fact"), d(4)), env));
        assertEquals(s("F"), eval(list(s("define"), s("F"),
            list(LAMBDA, list(s("n")), list(s("cons"), s("A"), list(QUOTE, list())))), env));
        assertEquals(list(d(123)), eval(list(s("F"), d(0)), env));
    }

    static Expr read(String s) {
        return new Reader(s).read();
    }

    @Test 
    public void testRead() {
        Env env = environment();
        assertEquals(d(12), read("12"));
        assertEquals(d(12.34), read("12.34"));
        assertEquals(d(12000), read("12e+3"));
        assertEquals(d(12000), read("12e3"));
        assertEquals(d(0.1234), read("12.34E-2"));
        assertEquals(list(s("a"), d(1)), read("(a 1)"));
        assertEquals(list(s("a"), s("."), d(1)), read("(a . 1)"));
        assertEquals(list(s("a.1")), read("(a.1)"));
        assertEquals(list(s("quote"), s("a")), read("'a"));
        assertEquals("'a", print(read("'a")));
        assertEquals(s("fact"), eval(read("""
            (define fact (lambda (n)
                (if (<= n 0)
                    1
                    (* n (fact (- n 1))))))
            """), env));
        assertEquals(d(1), eval(read("(fact 0)"), env));
        assertEquals(d(1), eval(read("(fact 1)"), env));
        assertEquals(d(2), eval(read("(fact 2)"), env));
        assertEquals(d(6), eval(read("(fact 3)"), env));
        assertEquals(d(24), eval(read("(fact 4)"), env));
        assertEquals(d(120), eval(read("(fact 5)"), env));
        assertEquals(d(720), eval(read("(fact 6)"), env));
        assertEquals(d(5040), eval(read("(fact 7)"), env));
    }

    @Test 
    public void testDefineRead() {
        Env env = environment();
        assertEquals(read("V"), eval(read("(define V (- 3 1))"), env));
        assertEquals(read("2"), eval(read(" V "), env));
        assertEquals(read("(add x y)"), eval(read("(define (add x y) (+ x y))"), env));
        assertEquals(read("3"), eval(read("(add 1 V)"), env));
    }
}