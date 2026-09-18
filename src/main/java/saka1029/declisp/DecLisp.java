package saka1029.declisp;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.IntPredicate;
import java.util.stream.Stream;

public class DecLisp {

    private DecLisp(){}

    public static MathContext MC = MathContext.DECIMAL128;


    public static Symbol sym(String name) { return new Symbol(name);}
    public static Symbol sym(Expr e) { return (Symbol)e;}

    public static Expr cons(Expr a, Expr b) { return new Cons(a, b); }

    public static BigDecimal dec(Expr e) { return e.cast(Dec.class).value; }
    public static Dec dec(BigDecimal v) { return new Dec(v); }
    public static Dec dec(double v) { return new Dec(BigDecimal.valueOf(v)); }

    public static boolean bool(Expr e) { return e.cast(Bool.class).value;}
    public static Bool bool(boolean b) { return b ? Bool.T : Bool.F; }

    // public static Expr eval(Expr e, Env env) {
    //     return switch (e) {
    //         case Symbol s -> env.get(s);
    //         case Bool b -> b;
    //         case Dec d -> d;
    //         case Nil n -> n;
    //         case Cons c -> {
    //             Expr head = eval(c.car(), env);
    //             if (head instanceof Apply app)
    //                 yield app.apply(c.cdr(), env);
    //             else if (head instanceof Dec)   // リストの先頭が数字ならevlisする
    //                 yield cons(head, c.cdr().evlis(env));
    //             else if (head instanceof Bool)   // リストの先頭が真偽値ならevlisする
    //                 yield cons(head, c.cdr().evlis(env));
    //             else
    //                 throw new DecLispException("eval(): Cannot apply '%s' to '%s'", head, c.cdr());
    //         }
    //         default -> throw new DecLispException("eval(): Unknown type '%s'", e);
    //     };
    // }

    public static Expr list(Expr... list) {
        Expr r = Nil.NIL;
        for (int i = list.length - 1; i >= 0; --i)
            r = cons(list[i], r);
        return r;
    }

    public static Expr list(List<Expr> list) {
        Expr r = Nil.NIL;
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
        public static final Expr EOF = new Expr() {public Expr eval(Env env) {return null;};};

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
            return DecLisp.list(Symbol.QUOTE, read());
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
                case "T" -> Bool.T;
                case "F" -> Bool.F;
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

    public static <T> int matrix(Expr evaled, List<List<T>> mat, Converter<T> conv) {
        int maxRowSize = 0;
        for (Expr c : evaled) {
            List<T> row = c.stream().map(d -> conv.single(d)).toList();
            if (row.isEmpty())
                row = List.of(conv.single(c));
            // System.out.println(row);
            maxRowSize = Math.max(maxRowSize, row.size());
            mat.add(row);
        }
        return maxRowSize;
    }

    public static <T> Expr arithmetic(Expr evaled, T unit, BinaryOperator<T> op, Converter<T> conv) {
        if (evaled.equals(Nil.NIL))
            return conv.single(unit);
        T[][] mat = conv.matrix(evaled);
        int maxRowSize = Stream.of(mat).mapToInt(row -> row.length).max().getAsInt();
        // System.out.println(maxRowSize);
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
        if (maxRowSize == 1)
            return conv.single(result[0]);
        Expr r = Nil.NIL;
        for (int i = maxRowSize - 1; i >= 0; --i)
            r = cons(conv.single(result[i]), r);
        return r;
    }

    static Bool compare(Expr args, IntPredicate predicate) {
        return bool(predicate.test(dec(args.car()).compareTo(dec(args.cdr().car()))));
    }

    public static Expr progn(Expr body, Env env) {
        Expr r = Nil.NIL;
        for (Expr c : body)
            r = c.eval(env);
        return r;
    }

    public static Env defaultEnv() {
        Env env = new Env();
        env.define(Symbol.QUOTE, (Apply) (a, e) -> a.car());
        env.define(sym("lambda"), (Apply) (a, e) -> {
            Expr parms = a.car(), body = a.cdr();
            return (Apply)(aa, ee) -> {
                Env n = new Env(e);
                parms.pairlis(aa.evlis(ee), n);
                return progn(body, n);
            };
        });
        env.define(sym("if"), (Apply) (a, e) -> {
            boolean p = bool(a.car().eval(e));
            if (p)
                return a.cdr().car().eval(e);
            else if (!a.cdr().cdr().equals(Nil.NIL))
                return a.cdr().cdr().car().eval(e);
            else
                return Nil.NIL;
        });
        env.define(sym("define"), (Apply) (a, e) -> e.define(sym(a.car()), a.cdr().car().eval(e)));
        // conditional AND
        env.define(sym("&&"), (Apply) (a, e) -> {
            Expr last = Bool.T;
            for (Expr c : a)
                if ((last = c.eval(e)).equals(Bool.F))
                    return last;
            return last;
        });
        // conditional OR
        env.define(sym("||"), (Apply) (a, e) -> {
            Expr last = Bool.F;
            for (Expr c : a)
                if (!(last = c.eval(e)).equals(Bool.F))
                    return last;
            return last;
        });
        env.define(sym("car"), (Proc) a -> a.car().car());
        env.define(sym("cdr"), (Proc) a -> a.car().cdr());
        env.define(sym("cons"), (Proc) a -> cons(a.car(), a.cdr().car()));
        env.define(sym("not"), (Proc) a -> a.car().equals(Bool.F) ? Bool.T : Bool.F);
        env.define(sym("!"), (Proc) a -> a.car().equals(Bool.F) ? Bool.T : Bool.F);
        env.define(sym("+"), (Proc) a -> arithmetic(a, BigDecimal.ZERO, (x, y) -> x.add(y, MC), Converter.DEC));
        env.define(sym("-"), (Proc) a -> arithmetic(a, BigDecimal.ZERO, (x, y) -> x.subtract(y, MC), Converter.DEC));
        env.define(sym("*"), (Proc) a -> arithmetic(a, BigDecimal.ONE, (x, y) -> x.multiply(y, MC), Converter.DEC));
        env.define(sym("/"), (Proc) a -> arithmetic(a, BigDecimal.ONE, (x, y) -> x.divide(y, MC), Converter.DEC));
        env.define(sym("and"), (Proc) a -> arithmetic(a, true, (x, y) -> x & y, Converter.BOOL));
        env.define(sym("or"), (Proc) a -> arithmetic(a, false, (x, y) -> x | y, Converter.BOOL));
        env.define(sym("xor"), (Proc) a -> arithmetic(a, false, (x, y) -> x ^ y, Converter.BOOL));
        env.define(sym("=="), (Proc) a -> compare(a, x -> x == 0));
        env.define(sym("!="), (Proc) a -> compare(a, x -> x != 0));
        env.define(sym("<"), (Proc) a -> compare(a, x -> x < 0));
        env.define(sym("<="), (Proc) a -> compare(a, x -> x <= 0));
        env.define(sym(">"), (Proc) a -> compare(a, x -> x > 0));
        env.define(sym(">="), (Proc) a -> compare(a, x -> x >= 0));
        return env;
    }
}
