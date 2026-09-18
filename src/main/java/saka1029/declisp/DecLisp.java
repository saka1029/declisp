package saka1029.declisp;

import java.math.BigDecimal;
import java.math.MathContext;
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

    public static Expr list(Expr... list) { return Cons.list(list); }
    public static Expr list(List<Expr> list) { return Cons.list(list); }

    public static BigDecimal dec(Expr e) { return e.cast(Dec.class).value; }
    public static Dec dec(BigDecimal v) { return new Dec(v); }
    public static Dec dec(double v) { return new Dec(BigDecimal.valueOf(v)); }

    public static boolean bool(Expr e) { return e.cast(Bool.class).value;}
    public static Bool bool(boolean b) { return b ? Bool.T : Bool.F; }

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
