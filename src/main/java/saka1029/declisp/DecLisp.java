package saka1029.declisp;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.IntPredicate;

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

    public interface Expr{}

    public record Symbol(String name) implements Expr {}
    public static final Symbol QUOTE = new Symbol("quote");

    public static class KeyValue {
        KeyValue prev; Symbol key; Expr value;
        public KeyValue(KeyValue prev, Symbol key, Expr value) {
            this.prev = prev; this.key = key; this.value = value;
        }
    }
    public static class Env {
        KeyValue kv;
        public Env(KeyValue kv) { this.kv = kv; }
        public Env() { this(null); }
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

    }
    public static class Nil implements Expr { private Nil() {} }
    public static Expr NIL = new Nil();
    // public record Int(int value) implements Expr {}
    public record Dec(BigDecimal value) implements Expr {
        @Override public final boolean equals(Object r) {
            return r instanceof Dec d && value.compareTo(d.value) == 0;
        }
    }
    public record Bool(boolean value) implements Expr {}
    public static final Bool TRUE = new Bool(true);
    public static final Bool FALSE = new Bool(false);

    static String printCons(Cons cons) {
        StringBuilder sb = new StringBuilder();
        if (cons.cdr instanceof Cons cdr && cons.car.equals(QUOTE)) // && cdr.cdr.equals(NIL))
            return sb.append("'").append(print(cdr.car)).toString();
        sb.append("(").append(print(cons.car));
        Expr e;
        for (e = cons.cdr; e instanceof Cons c; e = c.cdr)
            sb.append(" ").append(print(c.car));
        // if (!e.equals(NIL))
        //     sb.append(" . ").append(print(e));
        return sb.append(")").toString();
    }

    public static String print(Expr e) {
        return switch (e) {
            case Symbol s -> s.name;
            case Bool b -> "" + b.value;
            // case Int i -> "" + i.value;
            case Dec d -> d.value.toString().replaceFirst("\\.0$", "");
            case Nil n -> "()";
            case Cons c -> printCons(c);
            default -> "Unknown type '%s'".formatted(e);
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
            // case Int i -> i;
            case Dec d -> d;
            case Nil n -> n;
            case Cons c -> {
                Expr head = eval(c.car, env);
                if (head instanceof Apply app)
                    yield app.apply(c.cdr, env);
                else
                    throw new DecLispException("eval(): Cannot apply '%s' to '%s'", print(head), print(c.cdr));
            }
            default -> throw new DecLispException("eval(): Unknown type '%s'", print(e));
        };
    }

    public static Expr list(Expr... list) {
        Expr r = NIL;
        for (int i = list.length - 1; i >= 0; --i)
            r = new Cons(list[i], r);
        return r;
    }

    public static Expr list(Expr dot, List<Expr> list) {
        Expr r = dot;
        for (int i = list.size() - 1; i >= 0; --i)
            r = new Cons(list.get(i), r);
        return r;
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
                throw new DecLispException(e);
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
            List<Expr> list = new ArrayList<>();
            while (true) {
                spaces();
                if (ch == ')') {
                    getClear();  // skip ')'
                    return DecLisp.list(NIL, list);
                // no dot pair
                // } else if (ch == '.') {
                //     getClear();  // skip '.'
                //     Expr result = DecLisp.list(read(), list);
                //     spaces();
                //     if (ch != ')')
                //         throw new DecLispException("Reader.list(): ')' expected");
                //     getClear();  // skip ')'
                //     return result;
                }
                Expr e = read();
                if (e == EOF)
                    throw new DecLispException("Reader.list(): Unexpected EOF");
                list.addLast(e);
            }
        }

        Expr quote() {
            getClear();  // skip '\''
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
            return new Dec(new BigDecimal(buffer.substring(0, buffer.length() - 1)));
        }

        // Int integer() {
        //     while (isDigit(ch))
        //         get();
        //     return new Int(Integer.parseInt(buffer.substring(0, buffer.length() - 1)));
        // }

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
                throw new DecLispException("Reader.read(): Unexpected character '%c'", (char)ch);
        }
    }

    public static Expr cons(Expr a, Expr b) { return new Cons(a, b); }
    public static Symbol sym(String name) { return new Symbol(name);}
    public static Symbol sym(Expr e) { return (Symbol)e;}
    public static Expr car(Expr e) { return cast(e, Cons.class).car; }
    public static Expr cdr(Expr e) { return cast(e, Cons.class).cdr; }
    // public static Int i(int value) { return new Int(value);}
    // public static int i(Expr e) { return ((Int)e).value();}
    public static boolean b(Expr e) { return cast(e, Bool.class).value();}
    public static Bool b(boolean b) { return b ? TRUE : FALSE; }
    public static BigDecimal d(Expr e) { return cast(e, Dec.class).value; }
    public static Dec d(BigDecimal v) { return new Dec(v); }
    public static Dec d(double v) { return new Dec(BigDecimal.valueOf(v)); }

    public interface IntBinaryPredicate {
        boolean test(int a, int b);
    }

    static Dec arithmetic(Expr args, BigDecimal start, BinaryOperator<BigDecimal> operator) {
        BigDecimal prev = BigDecimal.ZERO;
        int i = 0;
        for (Expr a = args; a instanceof Cons c; a = c.cdr) {
            BigDecimal value = d(c.car);
            if (i == 1)
                start = prev;
            start = operator.apply(start, value);
            prev = value;
            ++i;
        }
        return d(start);
    }

    // static Int arithmetic(Expr args, int start, IntBinaryOperator operator) {
    //     int count = 0, prev = 0;
    //     for (Expr a = args; a instanceof Cons c; a = c.cdr) {
    //         int value = i(c.car);
    //         if (count == 1)
    //             start = prev;
    //         start = operator.applyAsInt(start, value);
    //         count++;
    //         prev = value;
    //     }
    //     return i(start);
    // }

    static Bool compare(Expr args, IntPredicate predicate) {
        return b(predicate.test(d(car(args)).compareTo(d(car(cdr(args))))));
    }
    // static Bool compare(Expr args, BiPredicate<BigDecimal, BigDecimal> operator) {
    //     return operator.test(d(car(args)), d(car(cdr(args)))) ? TRUE : FALSE;
    // }

    public static Expr evlis(Expr args, Env env) {
        List<Expr> list = new ArrayList<>();
        for (Expr e = args; e instanceof Cons c; e = c.cdr)
            list.add(eval(c.car, env));
        return list(NIL, list);
    }

    public static void pairlis(Expr parms, Expr args, Env env) {
        for (Expr p = parms; p instanceof Cons c; p = c.cdr, args = cdr(args))
            define(env, (Symbol)c.car, car(args));
    }

    public static Expr progn(Expr body, Env env) {
        Expr r = NIL;
        for (Expr b = body; b instanceof Cons c; b = c.cdr)
            r = eval(c.car, env);
        return r;
    }

    public static Env defaultEnv() {
        Env env = new Env();
        define(env, QUOTE, (Apply)(a, e) -> (car((a))));
        define(env, sym("lambda"), (Apply)(a, e) -> {
            Expr parms = car(a), body = cdr(a);
            return (Apply)(aa, ee) -> {
                Env n = new Env(e.kv);
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
            for (Expr x = a; x instanceof Cons c; x = c.cdr)
                if ((last = eval(c.car, e)).equals(FALSE))
                    return last;
            return last;
        });
        define(env, sym("or"), (Apply)(a, e) -> {
            Expr last = FALSE;
            for (Expr x = a; x instanceof Cons c; x = c.cdr)
                if (!(last = eval(c.car, e)).equals(FALSE))
                    return last;
            return last;
        });
        define(env, sym("car"), (Proc) a -> car(car(a)));
        define(env, sym("cdr"), (Proc) a -> cdr(car(a)));
        define(env, sym("cons"), (Proc) a -> cons(car(a), car(cdr(a))));
        define(env, sym("not"), (Proc) a -> car(a).equals(FALSE) ? TRUE : FALSE);
        define(env, sym("+"), (Proc) a -> arithmetic(a, BigDecimal.ZERO, (x, y) -> x.add(y)));
        define(env, sym("-"), (Proc) a -> arithmetic(a, BigDecimal.ZERO, (x, y) -> x.subtract(y)));
        define(env, sym("*"), (Proc) a -> arithmetic(a, BigDecimal.ONE, (x, y) -> x.multiply(y)));
        define(env, sym("/"), (Proc) a -> arithmetic(a, BigDecimal.ONE, (x, y) -> x.divide(y, MathContext.DECIMAL128)));
        define(env, sym("=="), (Proc) a -> compare(a, x -> x == 0));
        define(env, sym("!="), (Proc) a -> compare(a, x -> x != 0));
        define(env, sym("<"), (Proc) a -> compare(a, x -> x < 0));
        define(env, sym("<="), (Proc) a -> compare(a, x -> x <= 0));
        define(env, sym(">"), (Proc) a -> compare(a, x -> x > 0));
        define(env, sym(">="), (Proc) a -> compare(a, x -> x >= 0));
        return env;
    }
}
