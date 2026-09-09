package saka1029.declisp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static saka1029.declisp.DecLisp.*;

import org.junit.Test;

/**
 * (1) Listでドット記法は許さない。
 * (2) Consセルはイミュータブルである。
 */
public class TestDeclisp {

    @Test
    public void testEval() {
        Env env = new Env();
        define(env, sym("a"), d(3));
        assertEquals(d(3), eval(sym("a"), env));
        assertEquals(d(3), eval(d(3), env));
        assertEquals(TRUE, eval(TRUE, env));
        assertEquals(FALSE, eval(FALSE, env));
        define(env, sym("+"), (Apply)(a, e) -> {
            Expr evaled = evlis(a, e);
            return d(d(car(evaled)).add(d(car(cdr(evaled)))));
        });
        assertEquals(d(3), eval(list(sym("+"), d(1), d(2)), env));
    }

    static Expr read(String s) { return new Reader(s).read(); }

    @Test
    public void testRead() {
        assertEquals(list(d(1), sym("a")), read("(1 a)"));
        assertEquals(list(d(1), sym("."), sym("a")), read("(1 . a)"));
    }

    @Test 
    public void testPrint() {
        assertEquals("true", print(TRUE));
        assertEquals("false", print(FALSE));
        assertEquals("SYM", print(sym("SYM")));
        assertEquals("3", print(d(3)));
        assertEquals("()", print(NIL));
        assertEquals("(1 3)", print(list(d(1), d(3))));
        try {
            assertEquals("UNKNOWN", print(new Expr(){@Override public String toString() { return "UNKNOWN"; }}));
            fail();
        } catch (DecLispException x) {
            assertEquals("print(): Unknown type 'UNKNOWN'", x.getMessage());
        }
    }

    static Expr evalRead(String s, Env e) { return eval(read(s), e); }

    @Test
    public void evalRead() {
        Env env = defaultEnv();
        assertEquals(d(1), evalRead("(car '(1 a))", env));
        assertEquals(list(sym("."), sym("a")), evalRead("(cdr '(1 . a))", env));
        assertEquals(list(d(1), d(2)), evalRead("(cons 1 '(2))", env));
        // Cannot cons any and ATOM
        // assertEquals(cons(d(1), d(2)), evalRead("(cons 1 2)", env));
        assertEquals(FALSE, evalRead("(not true)", env));
        assertEquals(TRUE, evalRead("(not false)", env));
        assertEquals(FALSE, evalRead("(not (== 0 0))", env));
        assertEquals(sym("a"), evalRead("((lambda (a) (car a)) '(a b))", env));
        assertEquals(d(6), evalRead("(+ 1 2 3)", env));
        assertEquals(d(6), evalRead("(+ 1 2 (+ 1 2))", env));
        assertEquals(d(0), evalRead("(-)", env));
        assertEquals(d(-1), evalRead("(- 1)", env));
        assertEquals(d(-4), evalRead("(- 1 2 3)", env));
        assertEquals(TRUE, evalRead("(== 2 2)", env));
        assertEquals(FALSE, evalRead("(== 0 2)", env));
        define(env, sym("fact"), evalRead("(lambda (n) (if (<= n 0) 1 (* n (fact (- n 1)))))", env));
        assertEquals(d(1), evalRead("(fact 0)", env));
        assertEquals(d(1), evalRead("(fact 1)", env));
        assertEquals(d(2), evalRead("(fact 2)", env));
        assertEquals(d(6), evalRead("(fact 3)", env));
        assertEquals(sym("fact2"), evalRead("(define fact2 (lambda (n) (if (<= n 0) 1 (* n (fact (- n 1))))))", env));
        assertEquals(d(1), evalRead("(fact2 0)", env));
        assertEquals(d(1), evalRead("(fact2 1)", env));
        assertEquals(d(2), evalRead("(fact2 2)", env));
        assertEquals(d(6), evalRead("(fact2 3)", env));
        assertEquals(TRUE, evalRead("(and)", env));
        assertEquals(d(3), evalRead("(and 2 3)", env));
        assertEquals(FALSE, evalRead("(and false 3)", env));
        assertEquals(FALSE, evalRead("(or)", env));
        assertEquals(d(2), evalRead("(or 2 3)", env));
        assertEquals(d(3), evalRead("(or false 3)", env));
    }

    @Test 
    public void testArithmetic() {
        Env env = defaultEnv();
        assertEquals(d(0), evalRead("(+)", env));
        assertEquals(d(2), evalRead("(+ 2)", env));
        assertEquals(d(3), evalRead("(+ 1 2)", env));
        assertEquals(d(6), evalRead("(+ 1 2 3)", env));
        assertEquals(d(0), evalRead("(-)", env));
        assertEquals(d(-2), evalRead("(- 2)", env));
        assertEquals(d(-1), evalRead("(- 1 2)", env));
        assertEquals(d(-4), evalRead("(- 1 2 3)", env));
        assertEquals(d(1), evalRead("(*)", env));
        assertEquals(d(2), evalRead("(* 2)", env));
        assertEquals(d(2), evalRead("(* 1 2)", env));
        assertEquals(d(8), evalRead("(* 1 2 4)", env));
        assertEquals(d(1), evalRead("(/)", env));
        assertEquals(d(0.5), evalRead("(/ 2)", env));
        assertEquals(d(0.25), evalRead("(/ 1 4)", env));
        assertEquals(d(4), evalRead("(/ 24 2 3)", env));
    }

    @Test 
    public void testCompare() {
        Env env = defaultEnv();
        assertEquals(FALSE, evalRead("(== 1 0)", env));
        assertEquals(TRUE, evalRead("(== 0 0)", env));
        assertEquals(FALSE, evalRead("(== 0 1)", env));
        assertEquals(TRUE, evalRead("(!= 1 0)", env));
        assertEquals(FALSE, evalRead("(!= 0 0)", env));
        assertEquals(TRUE, evalRead("(!= 0 1)", env));
        assertEquals(FALSE, evalRead("(< 1 0)", env));
        assertEquals(FALSE, evalRead("(< 0 0)", env));
        assertEquals(TRUE, evalRead("(< 0 1)", env));
        assertEquals(FALSE, evalRead("(<= 1 0)", env));
        assertEquals(TRUE, evalRead("(<= 0 0)", env));
        assertEquals(TRUE, evalRead("(<= 0 1)", env));
        assertEquals(TRUE, evalRead("(> 1 0)", env));
        assertEquals(FALSE, evalRead("(> 0 0)", env));
        assertEquals(FALSE, evalRead("(> 0 1)", env));
        assertEquals(TRUE, evalRead("(>= 1 0)", env));
        assertEquals(TRUE, evalRead("(>= 0 0)", env));
        assertEquals(FALSE, evalRead("(>= 0 1)", env));

    }

}
