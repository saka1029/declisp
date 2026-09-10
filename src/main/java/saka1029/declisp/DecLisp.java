package saka1029.declisp;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BinaryOperator;
import java.util.function.IntPredicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DecLisp {

    private DecLisp(){}

    public static class DecLispException extends RuntimeException {
        public DecLispException(String format, Object... args) {
            super(format.formatted(args));
        }
        public DecLispException(Throwable cause) {
            super(cause);
        }
    }

    public interface Expr extends Iterable<Expr> {
        default Stream<Expr> stream() {
            return StreamSupport.stream(spliterator(), false);
        }
        @Override default Iterator<Expr> iterator() {
            return new Iterator<>() {
                Expr expr = Expr.this;

                @Override
                public boolean hasNext() {
                    return expr instanceof Cons;
                }

                @Override
                public Expr next() {
                    if (expr instanceof Cons c) {
                        expr = c.cdr;
                        return c.car;
                    } else
                        throw new NoSuchElementException();
                }
            };
        }
    }
    public static Expr car(Expr e) { return cast(e, Cons.class).car; }
    public static Expr cdr(Expr e) { return cast(e, Cons.class).cdr; }

    public record Symbol(String name) implements Expr {
        @Override public final String toString() { return name; }
    }
    public static final Symbol QUOTE = new Symbol("quote");
    public static Symbol sym(String name) { return new Symbol(name);}
    public static Symbol sym(Expr e) { return (Symbol)e;}

    public static class KeyValue {
        KeyValue prev; Symbol key; Expr value;
        public KeyValue(KeyValue prev, Symbol key, Expr value) {
            this.prev = prev; this.key = key; this.value = value;
        }
    }
    public static class Env {
        KeyValue kv;
        public Env() { this.kv = null; }
        public Env(Env e) { this.kv = e.kv; }
    }
    public static Symbol define(Env e, Symbol key, Expr value) {
        e.kv = new KeyValue(e.kv, key, value);
        return key;
    }
    public static Expr get(Env env, Symbol key) {
        for (KeyValue kv = env.kv; kv != null; kv = kv.prev)
            if (kv.key.equals(key))
                return kv.value;
        throw new DecLispException("get(): Not found %s", key);
    }
    public static Expr set(Env env, Symbol key, Expr value) {
        for (KeyValue kv = env.kv; kv != null; kv = kv.prev)
            if (kv.key.equals(key))
                return kv.value = value;
        throw new DecLispException("set(): Not found %s", key);
    }

    public static <T> T cast(Expr e, Class<T> cls) {
        if (cls.isInstance(e))
            return cls.cast(e);
        else
            throw new DecLispException("cast: cannot cast '%s' to '%s'",
                print(e), cls.getSimpleName());
    }

    public record Cons(Expr car, Expr cdr) implements Expr {
        public Cons(Expr car, Expr cdr) {
            if (!cdr.equals(NIL) && !(cdr instanceof Cons))
                throw new DecLispException("cons: cannot cons '%s' and '%s'",
                    print(car), print(cdr));
            this.car = car;
            this.cdr = cdr;
        }
        @Override public final String toString() { return printCons(this); }
    }
    public static Expr cons(Expr a, Expr b) { return new Cons(a, b); }
    static String printCons(Cons cons) {
        StringBuilder sb = new StringBuilder();
        if (cons.cdr instanceof Cons cdr && cons.car.equals(QUOTE)) // && cdr.cdr.equals(NIL))
            return sb.append("'").append(print(cdr.car)).toString();
        sb.append("(").append(print(cons.car));
        for (Expr c : cons.cdr)
            sb.append(" ").append(print(c));
        return sb.append(")").toString();
    }

    public static class Nil implements Expr { private Nil() {} }
    public static Expr NIL = new Nil() { @Override public String toString() { return "()"; }};

    public record Dec(BigDecimal value) implements Expr {
        @Override public final boolean equals(Object r) {
            return r instanceof Dec d && value.compareTo(d.value) == 0;
        }
        @Override public final String toString() {
            return value.toString().replaceFirst("\\.0$", "");
        }
    }
    public static BigDecimal d(Expr e) { return cast(e, Dec.class).value; }
    public static Dec d(BigDecimal v) { return new Dec(v); }
    public static Dec d(double v) { return new Dec(BigDecimal.valueOf(v)); }

    public record Bool(boolean value) implements Expr {
        @Override public final String toString() { return "" + value; }
    }
    public static boolean b(Expr e) { return cast(e, Bool.class).value();}
    public static Bool b(boolean b) { return b ? TRUE : FALSE; }
    public static final Bool TRUE = new Bool(true);
    public static final Bool FALSE = new Bool(false);

    public static String print(Expr e) {
        return switch (e) {
            case Symbol s -> s.toString();
            case Bool b -> b.toString();
            case Dec d -> d.toString();
            case Nil n -> n.toString();
            case Cons c -> c.toString();
            default -> "print: unknown type '%s'".formatted(e);
        };
    }

    public interface Apply extends Expr {
        Expr apply(Expr args, Env env);
    }
    public interface Proc extends Apply {
        Expr apply(Expr evaled);
        default Expr apply(Expr args, Env env) {
            return apply(evlis(args, env));
        }
    }

    public static Expr eval(Expr e, Env env) {
        return switch (e) {
            case Symbol s -> get(env, s);
            case Bool b -> b;
            case Dec d -> d;
            case Nil n -> n;
            case Cons c -> {
                Expr head = eval(c.car, env);
                if (head instanceof Apply app)
                    yield app.apply(c.cdr, env);
                else if (head instanceof Dec)   // リストの先頭が数字ならevlisする
                    yield cons(head, evlis(c.cdr, env));
                else if (head instanceof Bool)   // リストの先頭が真偽値ならevlisする
                    yield cons(head, evlis(c.cdr, env));
                else
                    throw new DecLispException("eval(): Cannot apply '%s' to '%s'", print(head), print(c.cdr));
            }
            default -> throw new DecLispException("eval(): Unknown type '%s'", print(e));
        };
    }

    public static Expr list(Expr... list) {
        Expr r = NIL;
        for (int i = list.length - 1; i >= 0; --i)
            r = cons(list[i], r);
        return r;
    }

    public static Expr list(List<Expr> list) {
        Expr r = NIL;
        for (int i = list.size() - 1; i >= 0; --i)
            r = cons(list.get(i), r);
        return r;
    }

    public static class CodePointBuffer {
        int[] buffer;
        int next = 0;
        public CodePointBuffer() { this.buffer = new int[64]; }
        public void append(int cp) {
            if (next >= buffer.length)
                buffer = Arrays.copyOf(buffer, buffer.length * 2);
            buffer[next++] = cp;
        }
        int pop() { return buffer[--next]; }
        public void clear() { next = 0; }
        public void clearButLast() {
            int last = pop();
            next = 0;
            append(last);
        }
        public String stringButLast() {
            int last = pop();
            String s = new String(buffer, 0, next);
            next = 0;
            append(last);
            return s;
        }
    }

    public static class Reader {
        public static final Expr EOF = new Expr() {};

        final java.io.Reader reader;
        final CodePointBuffer buffer = new CodePointBuffer();
        int ch; // code point (not char)

        public Reader(java.io.Reader reader) {
            this.reader = reader;
            this.ch = get();
        }

        public Reader(String source) {
            this(new StringReader(source));
        }

        int readCodePoint() throws IOException {
            int hi = reader.read();
            if (hi == -1)
                return -1;
            if (!Character.isHighSurrogate((char)hi))
                return hi;
            int lo = reader.read();
            if (lo == -1)
                return -1;
            if (!Character.isLowSurrogate((char)lo))
                throw new IOException("readCodePoint: invalid surrogate pair");
            return Character.toCodePoint((char)hi, (char)lo);
        }

        int get() {
            try {
                ch = readCodePoint();
                buffer.append(ch);    // ch == EOFの時もappendする
                return ch;
            } catch (IOException e) {
                throw new DecLispException(e);
            }
        }

        int clearGet() {
            buffer.clear();
            return get();
        }

        void spaces() {
            while (Character.isWhitespace(ch))
                get();
            buffer.clearButLast();
        }

        Expr list() {
            clearGet();     // skip '('
            List<Expr> list = new ArrayList<>();
            while (true) {
                spaces();
                if (ch == ')') {
                    clearGet();  // skip ')'
                    return DecLisp.list(list);
                // no dot pair
                }
                Expr e = read();
                if (e == EOF)
                    throw new DecLispException("Reader.list(): Unexpected EOF");
                list.addLast(e);
            }
        }

        Expr quote() {
            clearGet();  // skip '\''
            return DecLisp.list(DecLisp.QUOTE, read());
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
            if (ch == '.') {
                get();
                while (isDigit(ch))
                    get();
            }
            if (ch == 'E' || ch == 'e') {
                get();
                if (ch == '+' || ch == '-')
                    get();
                while (isDigit(ch))
                    get();
            }
            return new Dec(new BigDecimal(buffer.stringButLast()));
        }

        static boolean isSymbolFirst(int ch) {
            return switch (ch) {
                case -1, '(', ')' -> false;
                case '!', '#', '$', '%', '&', '|', '@', '=', '^',
                    '+', '-', '*', '/', ';', ':', ',', '.',
                    '_', '<', '>' -> true;
                default -> Character.isLetter(ch);
            };
        }

        static boolean isSymbolRest(int ch) {
            return isSymbolFirst(ch) || isDigit(ch);
        }

        Expr symbol() {
            while (isSymbolRest(ch))
                get();
            String value = buffer.stringButLast();
            return switch (value) {
                case "true" -> DecLisp.TRUE;
                case "false" -> DecLisp.FALSE;
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
                throw new DecLispException("Reader.read(): Unexpected character '0x%x'", ch);
        }
    }


    public static Expr arithmet(Expr evaled, BigDecimal unit, BinaryOperator<BigDecimal> op) {
        if (evaled.equals(NIL))
            return d(unit);
        List<List<BigDecimal>> mat = new ArrayList<>();
        int maxRowSize = 0;
        for (Expr c : evaled) {
            List<BigDecimal> row = new ArrayList<>();
            for (Expr f = c; f instanceof Cons d; f = d.cdr) {
                row.add(d(d.car));
            }
            if (row.isEmpty())
                row.add(d(c));
            // System.out.println(row);
            maxRowSize = Math.max(maxRowSize, row.size());
            mat.add(row);
        }
        // System.out.println(maxRowSize);
        BigDecimal[] result = new BigDecimal[maxRowSize];
        Arrays.fill(result, unit);
        for (int c = 0; c < maxRowSize; ++c) {
            BigDecimal prev = null;
            for (int r = 0, rmax = mat.size(); r < rmax; ++r) {
                BigDecimal adder = mat.get(r).get(c >= mat.get(r).size() ? 0 : c);
                if (r == 1)
                    result[c] = prev;
                result[c] = op.apply(result[c], adder);
                prev = adder;
            }
        }
        if (maxRowSize == 1)
            return d(result[0]);
        Expr r = NIL;
        for (int i = maxRowSize - 1; i >= 0; --i)
            r = cons(d(result[i]), r);
        // System.out.println("r=" + print(r));
        return r;
    }

    static Bool compare(Expr args, IntPredicate predicate) {
        return b(predicate.test(d(car(args)).compareTo(d(car(cdr(args))))));
    }

    public static Expr evlis(Expr args, Env env) {
        return list(args.stream()
            .map(e ->eval(e, env))
            .toArray(Expr[]::new));
    }

    public static void pairlis(Expr parms, Expr args, Env env) {
        for (Expr c : parms) {
            define(env, (Symbol)c, car(args));
            args = cdr(args);
        }
    }

    public static Expr progn(Expr body, Env env) {
        Expr r = NIL;
        for (Expr c : body)
            r = eval(c, env);
        return r;
    }

    public static Env defaultEnv() {
        Env env = new Env();
        define(env, QUOTE, (Apply)(a, e) -> (car((a))));
        define(env, sym("lambda"), (Apply)(a, e) -> {
            Expr parms = car(a), body = cdr(a);
            return (Apply)(aa, ee) -> {
                Env n = new Env(e);
                pairlis(parms, evlis(aa, ee), n);
                return progn(body, n);
            };
        });
        define(env, sym("if"), (Apply)(a, e) -> {
            boolean p = b(eval(car(a), e));
            if (p)
                return eval(car(cdr(a)), e);
            else if (!cdr(cdr(a)).equals(NIL))
                return eval(car(cdr(cdr(a))), e);
            else
                return NIL;
        });
        define(env, sym("define"), (Apply)(a, e) -> define(e, sym(car(a)), eval(car(cdr(a)), e)));
        define(env, sym("and"), (Apply)(a, e) -> {
            Expr last = TRUE;
            for (Expr c : a)
                if ((last = eval(c, e)).equals(FALSE))
                    return last;
            return last;
        });
        define(env, sym("or"), (Apply)(a, e) -> {
            Expr last = FALSE;
            for (Expr c : a)
                if (!(last = eval(c, e)).equals(FALSE))
                    return last;
            return last;
        });
        define(env, sym("car"), (Proc) a -> car(car(a)));
        define(env, sym("cdr"), (Proc) a -> cdr(car(a)));
        define(env, sym("cons"), (Proc) a -> cons(car(a), car(cdr(a))));
        define(env, sym("not"), (Proc) a -> car(a).equals(FALSE) ? TRUE : FALSE);
        define(env, sym("+"), (Proc) a -> arithmet(a, BigDecimal.ZERO, (x, y) -> x.add(y)));
        define(env, sym("-"), (Proc) a -> arithmet(a, BigDecimal.ZERO, (x, y) -> x.subtract(y)));
        define(env, sym("*"), (Proc) a -> arithmet(a, BigDecimal.ONE, (x, y) -> x.multiply(y)));
        define(env, sym("/"), (Proc) a -> arithmet(a, BigDecimal.ONE, (x, y) -> x.divide(y, MathContext.DECIMAL128)));
        define(env, sym("=="), (Proc) a -> compare(a, x -> x == 0));
        define(env, sym("!="), (Proc) a -> compare(a, x -> x != 0));
        define(env, sym("<"), (Proc) a -> compare(a, x -> x < 0));
        define(env, sym("<="), (Proc) a -> compare(a, x -> x <= 0));
        define(env, sym(">"), (Proc) a -> compare(a, x -> x > 0));
        define(env, sym(">="), (Proc) a -> compare(a, x -> x >= 0));
        return env;
    }
}
