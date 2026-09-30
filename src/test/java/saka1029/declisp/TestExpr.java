package saka1029.declisp;

import static org.junit.Assert.*;
import static saka1029.declisp.Common.*;

import org.junit.Test;

public class TestExpr {

    @Test 
    public void testCast() {
        Expr e = dec(8);
        assertEquals(Dec.class, e.cast(Dec.class).getClass());
        try {
            sym(e);
            fail();
        } catch (DecLispException x) {
        }
        assertEquals(dec(1), car(cons(dec(1), dec(2))));
        assertEquals(dec(2), cdr(cons(dec(1), dec(2))));
        try {
            car(dec(2));
            fail();
        } catch (DecLispException x) {
            assertEquals("type 'Cons' expected but '2' appeared", x.getMessage());
        }
        try {
            cdr(dec(2));
            fail();
        } catch (DecLispException x) {
            assertEquals("type 'Cons' expected but '2' appeared", x.getMessage());
        }
    }

    @Test 
    public void testPrint() {
        assertEquals("T", Bool.T.toString());
        assertEquals("F", Bool.F.toString());
        assertEquals("SYM", sym("SYM").toString());
        assertEquals("3", dec(3).toString());
        assertEquals("()", Nil.NIL.toString());
        assertEquals("'(1)", list(QUOTE, list(dec(1))).toString());
        assertEquals("'a", list(QUOTE, sym("a")).toString());
        assertEquals("(quote)", list(QUOTE).toString());
    }

    @Test
    public void testEval() {
        Env env = new Env();
        env.define(sym("a"), dec(3));
        assertEquals(list(), Nil.NIL.eval(env));
        assertEquals(dec(3), sym("a").eval(env));
        assertEquals(dec(3), dec(3).eval(env));
        assertEquals(Bool.T, Bool.T.eval(env));
        assertEquals(Bool.F, Bool.F.eval(env));
        env.define(sym("+"), (Applicable)(a, e) -> {
            Expr evaled = a.evlis(e);
            return dec(dec(car(evaled)).add(dec(car(cdr(evaled)))));
        });
        assertEquals(dec(3), list(sym("+"), dec(1), dec(2)).eval(env));
        env.define(sym("list"), (Procedure) a -> a);
        assertEquals(list(dec(3), dec(1)), list(sym("list"), sym("a"), dec(1)).eval(env));
        assertEquals(list(Bool.T, dec(3)), list(sym("list"), Bool.T, dec(3)).eval(env));
        try {
            new Expr() {
                @Override
                public String toString() {
                    return "ExprObject";
                }
            }.eval(env);
            fail();
        } catch (DecLispException x) {
            assertEquals("cannot eval", x.getMessage());
        }
    }

}
