package saka1029.declisp;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static saka1029.declisp.DecLisp.*;

import java.io.IOException;
import java.math.BigDecimal;

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
        Expr e = dec(8);
        assertEquals(Dec.class, cast(e, Dec.class).getClass());
        try {
            cast(e, Symbol.class);
            fail();
        } catch (DecLispException x) {
        }
    }

    @Test 
    public void testDec() {
        assertEquals(dec(2), dec(2.0));
        assertNotEquals(dec(2), dec(2.3));
    }

    @Test 
    public void testEnv() {
        Env env = new Env();
        define(env, sym("A"), dec(3));
        assertEquals(dec(3), get(env, sym("A")));
        set(env, sym("A"), dec(7));
        assertEquals(dec(7), get(env, sym("A")));
        try {
            assertEquals(dec(3), get(env, sym("F")));
            fail();
        } catch (DecLispException x) {
        }
        try {
            set(env, sym("F"), dec(999));
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
        define(env, sym("a"), dec(3));
        assertEquals(list(), eval(NIL, env));
        assertEquals(dec(3), eval(sym("a"), env));
        assertEquals(dec(3), eval(dec(3), env));
        assertEquals(T, eval(T, env));
        assertEquals(F, eval(F, env));
        define(env, sym("+"), (Apply)(a, e) -> {
            Expr evaled = evlis(a, e);
            return dec(dec(car(evaled)).add(dec(car(cdr(evaled)))));
        });
        assertEquals(dec(3), eval(list(sym("+"), dec(1), dec(2)), env));
        assertEquals(list(dec(3), dec(1)), eval(list(sym("a"), dec(1)), env));
        try {
            eval(new Expr(){@Override public String toString() { return "UNKNOWN"; }}, env);
            fail();
        } catch (DecLispException x) {
            assertEquals("eval(): Unknown type 'print: unknown type 'UNKNOWN''", x.getMessage());
        }
        assertEquals(list(T, dec(3)), eval(list(T, dec(3)), env));
    }

    @Test 
    public void testEvalAndOr() {
        Env env = defaultEnv();
        assertEquals(F, evalRead("(not T)", env));
        assertEquals(T, evalRead("(not F)", env));
        assertEquals(T, evalRead("(&&)", env));
        assertEquals(T, evalRead("(&& T)", env));
        assertEquals(T, evalRead("(&& T T)", env));
        assertEquals(F, evalRead("(&& T F)", env));
        assertEquals(F, evalRead("(&& F T)", env));
        assertEquals(F, evalRead("(&& F F)", env));
        assertEquals(F, evalRead("(||)", env));
        assertEquals(T, evalRead("(|| T)", env));
        assertEquals(T, evalRead("(|| T T)", env));
        assertEquals(T, evalRead("(|| T F)", env));
        assertEquals(T, evalRead("(|| F T)", env));
        assertEquals(F, evalRead("(|| F F)", env));

    }

    static Expr read(String s) { return new Reader(s).read(); }

    @Test
    public void testReadHokke() {
        /*
        𩸽
        UTF-8 Encoding:	0xF0 0xA9 0xB8 0xBD
        UTF-16 Encoding: 0xD867 0xDE3D
        UTF-32 Encoding: 0x00029E3D (171581)
         */
        assertEquals(sym("𩸽"), read("𩸽"));
    }

    @Test
    public void testReadNoSpaces() {
        assertEquals(list(dec(12), sym("𩸽")), read("(12𩸽)"));
    }

    @Test
    public void testReadLongSymbol() {
        String longName = "A".repeat(90);
        assertEquals(sym(longName), read(longName));
    }

    @Test
    public void testRead() {
        assertEquals(dec(1), read("+1"));
        assertEquals(dec(-1), read("-1"));
        assertEquals(list(dec(1), sym("a")), read("(1 a)"));
        assertEquals(list(dec(1), sym("."), sym("a")), read("(1 . a)"));
        assertEquals(sym("AB"), read("AB"));
        assertEquals(dec(12.34), read("12.34"));
        assertEquals(dec(1234), read("12.34e2"));
        assertEquals(dec(1234), read("12.34E2"));
        assertEquals(dec(0.1234), read("12.34e-2"));
        assertEquals(dec(1234), read("12.34e+2"));
        assertEquals(sym(","), read(","));
        try {
            read("😀");
            fail();
        } catch (DecLispException x) {
            // System.out.println(x.getMessage());
            // assertEquals("Reader.read(): ", x.getMessage());
        }
    }

    @Test 
    public void testPrint() {
        assertEquals("true", print(T));
        assertEquals("false", print(F));
        assertEquals("SYM", print(sym("SYM")));
        assertEquals("3", print(dec(3)));
        assertEquals("()", print(NIL));
        assertEquals("'(1)", print(list(QUOTE, list(dec(1)))));
        assertEquals("'a", print(list(QUOTE, sym("a"))));
        assertEquals("(quote)", print(list(QUOTE)));
    }

    @Test
    public void testCons() {
        assertEquals("(1)", print(cons(dec(1), NIL)));
        assertEquals("(1 2)", print(cons(dec(1), cons(dec(2), NIL))));
        try {
            print(cons(dec(1), dec(2)));
            fail();
        } catch (DecLispException x) {
            assertEquals("cons: cannot cons '1' and '2'", x.getMessage());
        }
        assertEquals("print: unknown type 'UNKNOWN'", print(new Expr(){@Override public String toString() { return "UNKNOWN"; }}));
    }

    static Expr evalRead(String s, Env e) { return eval(read(s), e); }

    @Test
    public void evalRead() {
        Env env = defaultEnv();
        assertEquals(dec(2), evalRead("(if F 1 2)", env));
        assertEquals(NIL, evalRead("(if F 1)", env));
        assertEquals(dec(1), evalRead("(car '(1 a))", env));
        assertEquals(list(sym("."), sym("a")), evalRead("(cdr '(1 . a))", env));
        assertEquals(list(dec(1), dec(2)), evalRead("(cons 1 '(2))", env));
        // Cannot cons any and ATOM
        // assertEquals(cons(d(1), d(2)), evalRead("(cons 1 2)", env));
        assertEquals(F, evalRead("(not (== 0 0))", env));
        assertEquals(sym("a"), evalRead("((lambda (a) (car a)) '(a b))", env));
        assertEquals(dec(6), evalRead("(+ 1 2 3)", env));
        assertEquals(dec(6), evalRead("(+ 1 2 (+ 1 2))", env));
        // System.out.println(print(evalRead("(-)", env)));
        assertEquals(dec(0), evalRead("(-)", env));
        assertEquals(dec(-1), evalRead("(- 1)", env));
        assertEquals(dec(-4), evalRead("(- 1 2 3)", env));
        assertEquals(T, evalRead("(== 2 2)", env));
        assertEquals(F, evalRead("(== 0 2)", env));
        define(env, sym("fact"), evalRead("(lambda (n) (if (<= n 0) 1 (* n (fact (- n 1)))))", env));
        assertEquals(dec(1), evalRead("(fact 0)", env));
        assertEquals(dec(1), evalRead("(fact 1)", env));
        assertEquals(dec(2), evalRead("(fact 2)", env));
        assertEquals(dec(6), evalRead("(fact 3)", env));
        assertEquals(sym("fact2"), evalRead("(define fact2 (lambda (n) (if (<= n 0) 1 (* n (fact (- n 1))))))", env));
        assertEquals(dec(1), evalRead("(fact2 0)", env));
        assertEquals(dec(1), evalRead("(fact2 1)", env));
        assertEquals(dec(2), evalRead("(fact2 2)", env));
        assertEquals(dec(6), evalRead("(fact2 3)", env));
        assertEquals(T, evalRead("(&&)", env));
        assertEquals(dec(3), evalRead("(&& 2 3)", env));
        assertEquals(F, evalRead("(&& F 3)", env));
        assertEquals(F, evalRead("(||)", env));
        assertEquals(dec(2), evalRead("(|| 2 3)", env));
        assertEquals(dec(3), evalRead("(|| F 3)", env));
    }

    @Test 
    public void testArithmetic() {
        Env env = defaultEnv();
        assertEquals(dec(0), evalRead("(+)", env));
        assertEquals(dec(2), evalRead("(+ 2)", env));
        assertEquals(dec(3), evalRead("(+ 1 2)", env));
        assertEquals(dec(6), evalRead("(+ 1 2 3)", env));
        assertEquals(dec(0), evalRead("(-)", env));
        assertEquals(dec(-2), evalRead("(- 2)", env));
        assertEquals(dec(-1), evalRead("(- 1 2)", env));
        assertEquals(dec(-4), evalRead("(- 1 2 3)", env));
        assertEquals(dec(1), evalRead("(*)", env));
        assertEquals(dec(2), evalRead("(* 2)", env));
        assertEquals(dec(2), evalRead("(* 1 2)", env));
        assertEquals(dec(8), evalRead("(* 1 2 4)", env));
        assertEquals(dec(1), evalRead("(/)", env));
        assertEquals(dec(0.5), evalRead("(/ 2)", env));
        assertEquals(dec(0.25), evalRead("(/ 1 4)", env));
        assertEquals(dec(4), evalRead("(/ 24 2 3)", env));
        assertEquals(read("(9 10 11)"), evalRead("(+ (1 2 3) 8)) ", env));
        assertEquals(read("(9 10 11)"), evalRead("(+ 8 (1 2 3))) ", env));
        assertEquals(read("(0.2 0.1 0.05)"), evalRead("(/ (5 10 20))) ", env));
        assertEquals(read("(2.5 5 10)"), evalRead("(/ (5 10 20) 2)) ", env));
        assertEquals(read("(16 18 20)"), evalRead("(+ (1 2 3) (4) 5 (6 (- 10 3) 8)) ", env));
    }

    @Test 
    public void testCompare() {
        Env env = defaultEnv();
        assertEquals(F, evalRead("(== 1 0)", env));
        assertEquals(T, evalRead("(== 0 0)", env));
        assertEquals(F, evalRead("(== 0 1)", env));
        assertEquals(T, evalRead("(!= 1 0)", env));
        assertEquals(F, evalRead("(!= 0 0)", env));
        assertEquals(T, evalRead("(!= 0 1)", env));
        assertEquals(F, evalRead("(< 1 0)", env));
        assertEquals(F, evalRead("(< 0 0)", env));
        assertEquals(T, evalRead("(< 0 1)", env));
        assertEquals(F, evalRead("(<= 1 0)", env));
        assertEquals(T, evalRead("(<= 0 0)", env));
        assertEquals(T, evalRead("(<= 0 1)", env));
        assertEquals(T, evalRead("(> 1 0)", env));
        assertEquals(F, evalRead("(> 0 0)", env));
        assertEquals(F, evalRead("(> 0 1)", env));
        assertEquals(T, evalRead("(>= 1 0)", env));
        assertEquals(T, evalRead("(>= 0 0)", env));
        assertEquals(F, evalRead("(>= 0 1)", env));
    }

    @Test 
    public void testStringBuilder() {
        String hokke = "𩸽";
        StringBuilder sb = new StringBuilder();
        sb.appendCodePoint(Character.codePointAt(hokke, 0));
        // System.out.println(sb);
        assertEquals(hokke, sb.toString());
    }

    @Test 
    public void testLetter() {
        assertTrue(Character.isLetter('漢'));
        assertTrue(Character.isLetter('あ'));
        assertTrue(Character.isLetter('ぁ'));
        assertTrue(Character.isLetter(Character.codePointAt("𩸽", 0)));
        assertFalse(Character.isLetter("𩸽".charAt(0)));
        assertFalse(Character.isLetter(Character.codePointAt("😀", 0)));
        assertFalse(Character.isLetter('１'));
        assertFalse(Character.isLetter('／'));
    }

    @Test 
    public void testString() {
        BigDecimal b = new BigDecimal(65535);
        assertEquals("ffff", b.toBigInteger().toString(16));
    }

    @Test 
    public void testEvalLogicalAndOr() {
        Env env = defaultEnv();
        assertEquals(list(T, F, F, F), evalRead("(and (T T F F) (T F T F))", env));
        assertEquals(list(T, T, T, F), evalRead("(or (T T F F) (T F T F))", env));
        assertEquals(list(F, T, T, F), evalRead("(xor (T T F F) (T F T F))", env));
    }

    @Test 
    public void testConverter() {
        assertArrayEquals(new BigDecimal[][] {
            {BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3)},
            {BigDecimal.valueOf(4)},
            {BigDecimal.valueOf(5), BigDecimal.valueOf(6), BigDecimal.valueOf(7)},
        }, DEC_CONV.matrix(read("((1 2 3) (4) (5 6 7))")));
        assertArrayEquals(new BigDecimal[][] {
            {BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3)},
            {BigDecimal.valueOf(4)},
            {BigDecimal.valueOf(5), BigDecimal.valueOf(6), BigDecimal.valueOf(7)},
        }, DEC_CONV.matrix(read("((1 2 3) 4 (5 6 7))")));

    }
}
