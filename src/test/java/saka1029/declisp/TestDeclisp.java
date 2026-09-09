package saka1029.declisp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import static saka1029.declisp.DecLisp.*;

import java.io.IOException;

import org.junit.Test;

import saka1029.declisp.DecLisp.Apply;
import saka1029.declisp.DecLisp.Dec;
import saka1029.declisp.DecLisp.DecLispException;
import saka1029.declisp.DecLisp.Env;
import saka1029.declisp.DecLisp.Expr;
import saka1029.declisp.DecLisp.Reader;
import saka1029.declisp.DecLisp.Symbol;

/**
 * (1) Listでドット記法は許さない。
 * (2) Consセルはイミュータブルである。
 */
public class TestDeclisp {

    @Test 
    public void testDecLispException() {
        assertEquals(IOException.class, new DecLispException(new IOException()).getCause().getClass());
    }

    @Test 
    public void testCast() {
        Expr e = d(8);
        assertEquals(Dec.class, cast(e, Dec.class).getClass());
        try {
            cast(e, Symbol.class);
            fail();
        } catch (DecLispException x) {
        }
    }

    @Test 
    public void testDec() {
        assertEquals(d(2), d(2.0));
        assertNotEquals(d(2), d(2.3));
    }

    @Test 
    public void testEnv() {
        Env env = new Env();
        define(env, sym("A"), d(3));
        assertEquals(d(3), get(env, sym("A")));
        set(env, sym("A"), d(7));
        assertEquals(d(7), get(env, sym("A")));
        try {
            assertEquals(d(3), get(env, sym("F")));
            fail();
        } catch (DecLispException x) {
        }
        try {
            set(env, sym("F"), d(999));
            fail();
        } catch (DecLispException x) {
        }
    }

    @Test
    public void testReader() {
        java.io.Reader r = new java.io.Reader() {
            @Override public void close() throws IOException { }
            @Override public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException();
            }
        };
        try {
            Reader reader = new Reader(r);
            reader.read();
            fail();
        } catch (DecLispException x) {
            assertEquals(IOException.class, x.getCause().getClass());
        }
        try {
            read("(a b");
            fail();
        } catch (DecLispException x) {
            assertEquals("Reader.list(): Unexpected EOF", x.getMessage());
        }
    }

    @Test
    public void testEval() {
        Env env = new Env();
        define(env, sym("a"), d(3));
        assertEquals(list(), eval(NIL, env));
        assertEquals(d(3), eval(sym("a"), env));
        assertEquals(d(3), eval(d(3), env));
        assertEquals(TRUE, eval(TRUE, env));
        assertEquals(FALSE, eval(FALSE, env));
        define(env, sym("+"), (Apply)(a, e) -> {
            Expr evaled = evlis(a, e);
            return d(d(car(evaled)).add(d(car(cdr(evaled)))));
        });
        assertEquals(d(3), eval(list(sym("+"), d(1), d(2)), env));
        try {
            eval(list(sym("a"), d(1)), env);
            fail();
        } catch (DecLispException x) {
            assertEquals("eval(): Cannot apply '3' to '(1)'", x.getMessage());
        }
        try {
            eval(new Expr(){@Override public String toString() { return "UNKNOWN"; }}, env);
            fail();
        } catch (DecLispException x) {
            assertEquals("eval(): Unknown type 'Unknown type 'UNKNOWN''", x.getMessage());
        }
    }

    static Expr read(String s) { return new Reader(s).read(); }

    @Test
    public void testRead() {
        assertEquals(d(1), read("+1"));
        assertEquals(d(-1), read("-1"));
        assertEquals(list(d(1), sym("a")), read("(1 a)"));
        assertEquals(list(d(1), sym("."), sym("a")), read("(1 . a)"));
        assertEquals(sym("AB"), read("AB"));
        assertEquals(d(12.34), read("12.34"));
        assertEquals(d(1234), read("12.34e2"));
        assertEquals(d(1234), read("12.34E2"));
        assertEquals(d(0.1234), read("12.34e-2"));
        assertEquals(d(1234), read("12.34e+2"));
        assertEquals(sym(","), read(","));
        assertEquals(sym("😀"), read("😀"));
    }

    @Test 
    public void testPrint() {
        assertEquals("true", print(TRUE));
        assertEquals("false", print(FALSE));
        assertEquals("SYM", print(sym("SYM")));
        assertEquals("3", print(d(3)));
        assertEquals("()", print(NIL));
        assertEquals("'(1)", print(list(QUOTE, list(d(1)))));
        assertEquals("'a", print(list(QUOTE, sym("a"))));
        assertEquals("(quote)", print(list(QUOTE)));
    }

    @Test
    public void testCons() {
        assertEquals("(1)", print(cons(d(1), NIL)));
        assertEquals("(1 2)", print(cons(d(1), cons(d(2), NIL))));
        try {
            print(cons(d(1), d(2)));
            fail();
        } catch (DecLispException x) {
            assertEquals("cons: cannot cons '1' and '2'", x.getMessage());
        }
        assertEquals("Unknown type 'UNKNOWN'", print(new Expr(){@Override public String toString() { return "UNKNOWN"; }}));
    }

    static Expr evalRead(String s, Env e) { return eval(read(s), e); }

    @Test
    public void evalRead() {
        Env env = defaultEnv();
        assertEquals(d(2), evalRead("(if false 1 2)", env));
        assertEquals(NIL, evalRead("(if false 1)", env));
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

    @Test 
    public void testAdd() {
        Env env = defaultEnv();
        evalRead("(add (1 2 3) (4) 5 (6 (- 10 3) 8)) ", env);
    }

}
