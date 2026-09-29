package saka1029.declisp;

import java.math.MathContext;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class Common {
    private Common() {}

    public static MathContext MC = MathContext.DECIMAL128;
    public static int precision() { return MC.getPrecision(); }
    public static int precision(int p) { MC = new MathContext(p); return p;}
    public static BigDecimal DELTA = BigDecimal.valueOf(0.5e-8);
    public static BigDecimal delta() { return DELTA; }
    public static BigDecimal delta(BigDecimal delta) { return DELTA = delta; }
    public static final Symbol QUOTE = sym("quote");
    public static final Symbol LAMBDA = sym("lambda");

    public static Symbol sym(String name) { return new Symbol(name);}
    public static String sym(Expr e) { return e.cast(Symbol.class).value(); }
    public static Symbol symbol(Expr e) { return e.cast(Symbol.class); }


    public static Expr car(Expr e) { return e.cast(Cons.class).car(); }
    public static Expr cdr(Expr e) { return e.cast(Cons.class).cdr(); }
    public static Expr cons(Expr a, Expr b) { return new Cons(a, b); }

    public static Expr list(Expr... list) {
        Expr r = Nil.NIL;
        for (int i = list.length - 1; i >= 0; --i)
            r = new Cons(list[i], r);
        return r;
    }

    public static Expr list(List<Expr> list) {
        Expr r = Nil.NIL;
        for (int i = list.size() - 1; i >= 0; --i)
            r = new Cons(list.get(i), r);
        return r;
    }

    /**
     * 点対のリストを作る。
     * listDot(List.of(1, 2, 3)) -> (1 2 . 3)
     * @param list
     * @return
     */
    public static Expr listDot(List<Expr> list) {
        int i = list.size() - 1;
        Expr r = list.get(i--);
        while (i >= 0)
            r = new Cons(list.get(i--), r);
        return r;
    }

    public static int toInt(BigDecimal d) {
        try {
            return d.intValueExact();
        } catch (ArithmeticException x) {
            throw new DecLispException(x);
        }
    }
    public static long toLong(BigDecimal d) {
        try {
            return d.longValueExact();
        } catch (ArithmeticException x) {
            throw new DecLispException(x);
        }
    }
    public static BigInteger bigInt(BigDecimal d) { return d.toBigInteger(); }
    public static BigInteger bigInt(Expr e) { return dec(e).toBigInteger(); }

    public static BigDecimal bigDec(String s) { return new BigDecimal(s); }
    public static BigDecimal bigDec(BigInteger d) { return new BigDecimal(d); }
    public static BigDecimal dec(Expr e) { return e.cast(Dec.class).value; }
    public static Dec dec(BigDecimal v) { return new Dec(v); }
    public static Dec dec(BigInteger v) { return new Dec(new BigDecimal(v)); }
    public static Dec dec(double v) { return new Dec(BigDecimal.valueOf(v)); }

    public static boolean bool(Expr e) { return !e.equals(Bool.F); }
    public static Bool bool(boolean b) { return b ? Bool.T : Bool.F; }

    public static Procedure proc(Expr e) { return e.cast(Procedure.class); }

    public static Expr read(String input) { return new Reader(input).read(); }
    public static Expr eval(Env env, String input) { return new Reader(input).read().eval(env); }
}
