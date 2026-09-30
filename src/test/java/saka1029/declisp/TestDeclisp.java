package saka1029.declisp;

import static org.junit.Assert.*;
import static saka1029.declisp.DecLisp.*;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.StreamSupport;

import static saka1029.declisp.Common.*;

import org.junit.Test;

public class TestDeclisp {

    @Test 
    public void testAndOr() {
        Env env = defaultEnv();
        assertEquals(Bool.F, eval(env,"(not T)"));
        assertEquals(Bool.T, eval(env,"(not F)"));
        assertEquals(Bool.F, eval(env,"(! T)"));
        assertEquals(Bool.T, eval(env,"(! F)"));
        assertEquals(Bool.T, eval(env,"(&&)"));
        assertEquals(Bool.T, eval(env,"(&& T)"));
        assertEquals(Bool.T, eval(env,"(&& T T)"));
        assertEquals(Bool.F, eval(env,"(&& T F)"));
        assertEquals(Bool.F, eval(env,"(&& F T)"));
        assertEquals(Bool.F, eval(env,"(&& F F)"));
        assertEquals(Bool.F, eval(env,"(&& 3 F)"));
        assertEquals(Bool.F, eval(env,"(&& 1 2 F)"));
        assertEquals(dec(3), eval(env,"(&& 1 2 3)"));
        assertEquals(list(dec(1), Bool.F, dec(3)), eval(env,"(&& (1 F 3))"));
        assertEquals(Bool.F, eval(env,"(||)"));
        assertEquals(Bool.T, eval(env,"(|| T)"));
        assertEquals(Bool.T, eval(env,"(|| T T)"));
        assertEquals(Bool.T, eval(env,"(|| T F)"));
        assertEquals(Bool.T, eval(env,"(|| F T)"));
        assertEquals(Bool.F, eval(env,"(|| F F)"));
        assertEquals(dec(3), eval(env,"(|| 3 F)"));
        assertEquals(dec(3), eval(env,"(|| F 3 F)"));
        assertEquals(dec(1), eval(env,"(|| 1 2 3)"));
    }

    @Test
    public void testEvalRead() {
        Env env = defaultEnv();
        assertEquals(sym("F"), sym("F"));
        assertEquals(list(dec(0), dec(3)), eval(env, "(list '0 3)"));
        // System.out.println(list(sym("F"), dec(3)));
        // System.out.println(eval(env, "(list 'F 3)"));
        assertEquals(list(Bool.F, dec(3)), eval(env, "(list 'F 3)"));
        assertEquals(dec(2), eval(env, "(if F 1 2)"));
        assertEquals(Nil.NIL, eval(env, "(if F 1)"));
        assertEquals(dec(1), eval(env, "(car '(1 a))"));
        assertEquals(sym("a"), eval(env, "(cdr '(1 . a))"));
        assertEquals(list(dec(1), dec(2)), eval(env, "(cons 1 '(2))"));
        // Cannot cons any and ATOM
        // assertEquals(cons(d(1), d(2)), evalRead("(cons 1 2)", env));
        assertEquals(Bool.F, eval(env, "(not (== 0 0))"));
        assertEquals(sym("a"), eval(env, "((lambda (a) (car a)) '(a b))"));
        assertEquals(dec(6), eval(env, "(+ 1 2 3)"));
        assertEquals(dec(6), eval(env, "(+ 1 2 (+ 1 2))"));
        // System.out.println(print(env, evalRead("(-)")));
        assertEquals(dec(0), eval(env, "(-)"));
        assertEquals(dec(-1), eval(env, "(- 1)"));
        assertEquals(dec(-4), eval(env, "(- 1 2 3)"));
        assertEquals(Bool.T, eval(env, "(== 2 2)"));
        assertEquals(Bool.F, eval(env, "(== 0 2)"));
        env.define(sym("fact"), eval(env, "(lambda (n) (if (<= n 0) 1 (* n (fact (- n 1)))))"));
        assertEquals(dec(1), eval(env, "(fact 0)"));
        assertEquals(dec(1), eval(env, "(fact 1)"));
        assertEquals(dec(2), eval(env, "(fact 2)"));
        assertEquals(dec(6), eval(env, "(fact 3)"));
        assertEquals(sym("fact2"), eval(env, "(define fact2 (lambda (n) (if (<= n 0) 1 (* n (fact (- n 1))))))"));
        assertEquals(dec(1), eval(env, "(fact2 0)"));
        assertEquals(dec(1), eval(env, "(fact2 1)"));
        assertEquals(dec(2), eval(env, "(fact2 2)"));
        assertEquals(dec(6), eval(env, "(fact2 3)"));
        assertEquals(Bool.T, eval(env, "(&&)"));
        assertEquals(dec(3), eval(env, "(&& 2 3)"));
        assertEquals(Bool.F, eval(env, "(&& F 3)"));
        assertEquals(Bool.F, eval(env, "(||)"));
        assertEquals(dec(2), eval(env, "(|| 2 3)"));
        assertEquals(dec(3), eval(env, "(|| F 3)"));
        assertEquals(sym("listx"), eval(env, "(define listx (lambda x x))))"));
        assertEquals(read("(1 2 3)"), eval(env, "(listx 1 2 3)"));
        assertEquals(sym("foo"), eval(env, "(define foo (lambda (first . rest) (listx first rest)))))"));
        assertEquals(read("(1 (2 3))"), eval(env, "(foo 1 2 3)"));
    }

    @Test 
    public void testArithmetic() {
        Env env = defaultEnv();
        assertEquals(dec(0), eval(env, "(+)"));
        assertEquals(dec(2), eval(env, "(+ 2)"));
        assertEquals(dec(3), eval(env, "(+ 1 2)"));
        assertEquals(dec(6), eval(env, "(+ 1 2 3)"));
        assertEquals(dec(0), eval(env, "(-)"));
        assertEquals(dec(-2), eval(env, "(- 2)"));
        assertEquals(dec(-1), eval(env, "(- 1 2)"));
        assertEquals(dec(-4), eval(env, "(- 1 2 3)"));
        assertEquals(dec(1), eval(env, "(*)"));
        assertEquals(dec(2), eval(env, "(* 2)"));
        assertEquals(dec(2), eval(env, "(* 1 2)"));
        assertEquals(dec(8), eval(env, "(* 1 2 4)"));
        assertEquals(dec(1), eval(env, "(/)"));
        assertEquals(dec(0.5), eval(env, "(/ 2)"));
        assertEquals(dec(0.25), eval(env, "(/ 1 4)"));
        assertEquals(dec(4), eval(env, "(/ 24 2 3)"));
        assertEquals(dec(1), eval(env, "(%)"));
        assertEquals(dec(1), eval(env, "(% 2)"));
        assertEquals(dec(1), eval(env, "(% 1 4)"));
        assertEquals(dec(0), eval(env, "(% 24 2 3)"));
        assertEquals(dec(679), eval(env, "(% 1679 1000)"));
    }

    @Test 
    public void testCompare() {
        Env env = defaultEnv();
        try {
            eval(env, "(==)");
            fail();
        } catch (DecLispException x) {
            assertEquals("number of arguments must >= 2 '()'", x.getMessage());
        }
        try {
            eval(env, "(== 1)");
            fail();
        } catch (DecLispException x) {
            assertEquals("number of arguments must >= 2 '(1)'", x.getMessage());
        }
        assertEquals(Bool.F, eval(env, "(== 1 0)"));
        assertEquals(Bool.T, eval(env, "(== 0 0)"));
        assertEquals(Bool.F, eval(env, "(== 0 1)"));
        assertEquals(Bool.F, eval(env, "(= 1 0)"));
        assertEquals(Bool.T, eval(env, "(= 0 0)"));
        assertEquals(Bool.F, eval(env, "(= 0 1)"));
        assertEquals(Bool.T, eval(env, "(!= 1 0)"));
        assertEquals(Bool.F, eval(env, "(!= 0 0)"));
        assertEquals(Bool.T, eval(env, "(!= 0 1)"));
        assertEquals(Bool.F, eval(env, "(< 1 0)"));
        assertEquals(Bool.F, eval(env, "(< 0 0)"));
        assertEquals(Bool.T, eval(env, "(< 0 1)"));
        assertEquals(Bool.F, eval(env, "(<= 1 0)"));
        assertEquals(Bool.T, eval(env, "(<= 0 0)"));
        assertEquals(Bool.T, eval(env, "(<= 0 1)"));
        assertEquals(Bool.T, eval(env, "(> 1 0)"));
        assertEquals(Bool.F, eval(env, "(> 0 0)"));
        assertEquals(Bool.F, eval(env, "(> 0 1)"));
        assertEquals(Bool.T, eval(env, "(>= 1 0)"));
        assertEquals(Bool.T, eval(env, "(>= 0 0)"));
        assertEquals(Bool.F, eval(env, "(>= 0 1)"));
        assertEquals(Bool.T, eval(env, "(== 1 1 1 1)"));
        assertEquals(Bool.F, eval(env, "(== 1 1 2 1)"));
        assertEquals(Bool.T, eval(env, "(<= 0 1 1 2)"));
        assertEquals(Bool.F, eval(env, "(< 0 1 1 2)"));
    }

    @Test 
    public void testCompareBool() {
        Env env = defaultEnv();
        assertEquals(Bool.T, eval(env, "(== T T)"));
        assertEquals(Bool.F, eval(env, "(== T F)"));
        assertEquals(Bool.F, eval(env, "(== F T)"));
        assertEquals(Bool.T, eval(env, "(== F F)"));
        assertEquals(Bool.T, eval(env, "(= T T)"));
        assertEquals(Bool.F, eval(env, "(= T F)"));
        assertEquals(Bool.F, eval(env, "(= F T)"));
        assertEquals(Bool.T, eval(env, "(= F F)"));
        assertEquals(Bool.F, eval(env, "(< T T)"));
        assertEquals(Bool.F, eval(env, "(< T F)"));
        assertEquals(Bool.T, eval(env, "(< F T)"));
        assertEquals(Bool.F, eval(env, "(< F F)"));
        assertEquals(Bool.T, eval(env, "(<= T T)"));
        assertEquals(Bool.F, eval(env, "(<= T F)"));
        assertEquals(Bool.T, eval(env, "(<= F T)"));
        assertEquals(Bool.T, eval(env, "(<= F F)"));
        assertEquals(Bool.F, eval(env, "(> T T)"));
        assertEquals(Bool.T, eval(env, "(> T F)"));
        assertEquals(Bool.F, eval(env, "(> F T)"));
        assertEquals(Bool.F, eval(env, "(> F F)"));
        assertEquals(Bool.T, eval(env, "(>= T T)"));
        assertEquals(Bool.T, eval(env, "(>= T F)"));
        assertEquals(Bool.F, eval(env, "(>= F T)"));
        assertEquals(Bool.T, eval(env, "(>= F F)"));
    }

    @Test 
    public void testLogical() {
        Env env = defaultEnv();
        assertEquals(Bool.T, eval(env, "(and)"));
        assertEquals(Bool.T, eval(env, "(and T)"));
        assertEquals(Bool.F, eval(env, "(and F)"));
        assertEquals(Bool.T, eval(env, "(and T T)"));
        assertEquals(Bool.F, eval(env, "(and T F)"));
        assertEquals(Bool.F, eval(env, "(and F T)"));
        assertEquals(Bool.F, eval(env, "(and F F)"));
        assertEquals(Bool.F, eval(env, "(or)"));
        assertEquals(Bool.T, eval(env, "(or T)"));
        assertEquals(Bool.F, eval(env, "(or F)"));
        assertEquals(Bool.T, eval(env, "(or T T)"));
        assertEquals(Bool.T, eval(env, "(or T F)"));
        assertEquals(Bool.T, eval(env, "(or F T)"));
        assertEquals(Bool.F, eval(env, "(or F F)"));
        assertEquals(Bool.F, eval(env, "(xor)"));
        assertEquals(Bool.T, eval(env, "(xor T)"));
        assertEquals(Bool.F, eval(env, "(xor F)"));
        assertEquals(Bool.F, eval(env, "(xor T T)"));
        assertEquals(Bool.T, eval(env, "(xor T F)"));
        assertEquals(Bool.T, eval(env, "(xor F T)"));
        assertEquals(Bool.F, eval(env, "(xor F F)"));
    }

    @Test 
    public void testGet() {
        Env env = defaultEnv();
        assertEquals(sym("var"), eval(env, "(define var 3)"));
        assertEquals(dec(3), eval(env, "var"));
        try {
            eval(env, "foo");
        } catch (DecLispException x) {
            assertEquals("Env.get(): symbol 'foo' not found", x.getMessage());
        }
    }

    @Test 
    public void testSet() {
        Env env = defaultEnv();
        assertEquals(sym("var"), eval(env, "(define var 3)"));
        assertEquals(dec(6), eval(env, "(set var (+ 1 2 3))"));
        try {
            eval(env, "(set foo (+ 1 2 3))");
        } catch (DecLispException x) {
            assertEquals("Env.set(): symbol 'foo' not found", x.getMessage());
        }
    }

    @Test 
    public void testDefine() {
        Env env = defaultEnv();
        eval(env, "(define var (+ 1 2 3))");
        assertEquals(dec(6), eval(env, "var"));
        eval(env, "(define fact (lambda (n) (if (<= n 0) 1 (* n (fact (- n 1))))))");
        assertEquals(dec(120), eval(env, "(fact 5)"));
        eval(env, "(define (fact2 n) (if (<= n 0) 1 (* n (fact2 (- n 1)))))");
        assertEquals(dec(1), eval(env, "(fact2 0)"));
        assertEquals(dec(1), eval(env, "(fact2 1)"));
        assertEquals(dec(2), eval(env, "(fact2 2)"));
        assertEquals(dec(6), eval(env, "(fact2 3)"));
        assertEquals(dec(24), eval(env, "(fact2 4)"));
        assertEquals(dec(120), eval(env, "(fact2 5)"));
        eval(env, "(define (foo h . t) (list h t))");
        assertEquals(read("(1 (x))"), eval(env, "(foo 1 'x)"));
        eval(env, "(define (bar . r) r)");
        assertEquals(read("(1 x)"), eval(env, "(bar 1 'x)"));
    } 

    @Test 
    public void testMatrixTranspose() {
        Expr[][] matrix = {
            {dec(1), dec(2), dec(3)},
            {dec(4)},
            {dec(5), dec(6)}};
        assertArrayEquals(matrix, matrix(read("((1 2 3) 4 (5 6))")));
        assertArrayEquals(matrix, matrix(read("((1 2 3) (4) (5 6))")));
        try {
            // 行の長さは最大値(=3)または1でなければならない。
            // (5 6)はこの条件を満たさない。
            transpose(matrix(read("((1 2 3) 4 (5 6))")));
            fail();
        } catch (DecLispException x) {
            assertEquals("Illegal matrix [[1, 2, 3], [4], [5, 6]]", x.getMessage());
        }
        // 長さが1だった行は最大値(=3)まで増幅される。
        Expr[][] transposed = {
            {dec(1), dec(4), dec(5)},
            {dec(2), dec(4), dec(6)},
            {dec(3), dec(4), dec(7)}};
        assertArrayEquals(transposed, transpose(matrix(read("((1 2 3) 4 (5 6 7))"))));
    }

    @Test 
    public void testMap() {
        Env env = defaultEnv();
        assertEquals(read("-1"), eval(env, "(- 1)"));
        assertEquals(read("(-1 -2)"), eval(env, "(map - '(1 2))"));
        assertEquals(read("(-1 -2)"), eval(env, "(map - '(1 2))"));
        assertEquals(read("(2 4)"), eval(env, "(map (lambda (x) (+ x x)) '(1 2))"));
        assertEquals(read("(2 4)"), eval(env, "(map (lambda (x) (+ x x)) '(1 2) '(3 4))"));
        assertEquals(read("(-2 -2)"), eval(env, "(map - '(1 2) '(3 4))"));
        assertEquals(read("(-7 -7)"), eval(env, "(map - '(1 2) '(3 4) 5)"));
        assertEquals(read("()"), eval(env, "(map - )"));
        try {
            eval(env, "(map - '(1 2) '() '5)");
        } catch (DecLispException x) {
            assertEquals("Illegal matrix [[1, 2], [], [5]]", x.getMessage());
        }
        assertEquals(read("(F T)"), eval(env, "(map not '(T F))"));
        assertEquals(read("(T F F F)"), eval(env, "(map and '(T T F F) '(T F T F))"));
        assertEquals(read("(T T T F)"), eval(env, "(map or '(T T F F) '(T F T F))"));
        assertEquals(read("(F T T F)"), eval(env, "(map xor '(T T F F) '(T F T F))"));
        assertEquals(read("fact"), eval(env, "(define (fact n) (if (<= n 0) 1 (* n (fact (- n 1)))))"));
        assertEquals(read("(1 1 2 6 24 120)"), eval(env, "(map fact (range 0 6))"));
    }

    @Test 
    public void testAbs() {
        Env env = defaultEnv();
        assertEquals(dec(9), eval(env, "(abs 9)"));
        assertEquals(dec(9), eval(env, "(abs -9)"));
    }

    @Test 
    public void testGcd() {
        Env env = defaultEnv();
        assertEquals(dec(1), eval(env, "(gcd)"));
        assertEquals(dec(9), eval(env, "(gcd 9)"));
        assertEquals(dec(3), eval(env, "(gcd 9 15)"));
        assertEquals(dec(3), eval(env, "(gcd -9 15)"));
        assertEquals(dec(3), eval(env, "(gcd 9 -15)"));
        assertEquals(dec(12), eval(env, "(gcd 120 84 48)"));
    }

    @Test 
    public void testLcm() {
        Env env = defaultEnv();
        assertEquals(dec(1), eval(env, "(lcm)"));
        assertEquals(dec(9), eval(env, "(lcm 9)"));
        assertEquals(dec(45), eval(env, "(lcm 9 15)"));
        assertEquals(dec(45), eval(env, "(lcm -9 15)"));
        assertEquals(dec(45), eval(env, "(lcm 9 -15)"));
        assertEquals(dec(1680), eval(env, "(lcm 120 84 48)"));
    }

    @Test 
    public void testDelta() {
        Env env = defaultEnv();
        assertEquals(dec(5e-9), eval(env, "(delta 5e-9)"));
        assertTrue(bool(eval(env, "(~ 12.1234567890 12.1234567899)")));
        assertEquals(dec(5e-20), eval(env, "(delta 5e-20)"));
        assertEquals(dec(5e-20), eval(env, "(delta)"));
        assertFalse(bool(eval(env, "(~ 12.1234567890 12.1234567899)")));
    }

    @Test 
    public void testConstant() {
        Env env = defaultEnv();
        assertEquals(dec(34), eval(env, "(precision 34)"));
        assertEquals(dec(34), eval(env, "(precision)"));
        assertEquals(dec(bigDec("3.141592653589793238462643383279503")), eval(env, "(pi)"));
        assertEquals(dec(bigDec("2.718281828459045235360287471352662")), eval(env, "(e)"));
        assertEquals(dec(8), eval(env, "(precision 8)"));
        assertEquals(dec(bigDec("3.1415927")), eval(env, "(pi)"));
        assertEquals(dec(bigDec("2.7182818")), eval(env, "(e)"));
    }

    @Test 
    public void testTriangle() {
        
        Env env = defaultEnv();
        eval(env, "(define d30 (/ (pi) 6))");
        eval(env, "(define d45 (/ (pi) 4))");
        eval(env, "(define d90 (/ (pi) 2))");
        assertTrue(bool(eval(env, "(approx (sin 0) 0)")));
        assertTrue(bool(eval(env, "(approx (sin 0) 0)")));
        assertTrue(bool(eval(env, "(approx (sin d90) 1)")));
        assertTrue(bool(eval(env, "(approx (cos 0) 1)")));
        assertTrue(bool(eval(env, "(approx (cos d90) 0)")));
        assertTrue(bool(eval(env, "(approx (tan 0) 0)")));
        assertTrue(bool(eval(env, "(approx (tan d45) 1)")));
        assertTrue(bool(eval(env, "(approx (asin 0.5) d30)")));
        assertTrue(bool(eval(env, "(approx (asin -1) (- d90))")));
        assertTrue(bool(eval(env, "(~ (acos 0) d90)")));
        assertTrue(bool(eval(env, "(~ (atan 1) d45)")));
    }

    @Test 
    public void testLog() {
        Env env = defaultEnv();
        assertEquals(dec(4), eval(env, "(log10 10000)"));
        assertEquals(dec(10), eval(env, "(log2 1024)"));
        assertTrue(bool(eval(env, "(approx (log (pow (e) 7)) 7)")));
        assertTrue(bool(eval(env, "(~ (log (^ (e) 7)) 7)")));
    }

    @Test 
    public void testGamma() {
        Env env = defaultEnv();
        assertTrue(bool(eval(env, "(approx (gamma (/ 2)) (sqrt (pi)))")));
    }

    @Test 
    public void testExp() {
        Env env = defaultEnv();
        assertTrue(bool(eval(env, "(approx (exp 0) 1)")));
        assertTrue(bool(eval(env, "(approx (exp 1) (e))")));
        assertTrue(bool(eval(env, "(approx (exp 2) (pow (e) 2))")));
    }

    @Test 
    public void testRoot() {
        Env env = defaultEnv();
        assertTrue(bool(eval(env, "(approx (sqrt 65536) 256)")));
        assertTrue(bool(eval(env, "(approx (root 4 65536) 16)")));
    }

    @Test 
    public void testRange() {
        Env env = defaultEnv();
        assertEquals(read("(0 1 2)"), eval(env, "(range 3)"));
        assertEquals(read("(0 1 2)"), eval(env, "(range 0 3)"));
        assertEquals(read("(0 -1 -2)"), eval(env, "(range 0 -3)"));
        assertEquals(read("(0 0.5 1 1.5)"), eval(env, "(range 0 2 0.5)"));
        assertEquals(read("()"), eval(env, "(range 0 -2 0.5)"));
        assertEquals(read("(0 -0.2 -0.4 -0.6 -0.8)"), eval(env, "(range 0 -1 -0.2)"));
        try {
            eval(env, "(range 0 -1 0)");
        } catch (DecLispException x) {
            assertEquals("step must != 0", x.getMessage());
        }
        try {
            eval(env, "(range 0 -1 -0.2 0)");
        } catch (DecLispException x) {
            assertEquals("Illegal range argument", x.getMessage());
        }
        assertEquals(read("(0 2 4)"), eval(env, "(map (lambda (n) (* 2 n)) (range 3))"));
    }

    @Test 
    public void testApply() {
        Env env = defaultEnv();
        assertEquals(read("6"), eval(env, "(apply + '(1 2 3))"));
        assertEquals(read("4950"), eval(env, "(apply + (range 100))"));
    }

    @Test 
    public void testHypot() {
        Env env = defaultEnv();
        assertEquals(read("25"), eval(env, "(square 5)"));
        assertEquals(read("5"), eval(env, "(hypot 3 4)"));
    }

    @Test 
    public void testDate() {
        Env env = defaultEnv();
        var d = LocalDate.now();
        int today = d.getYear() * 10000 + d.getMonthValue() * 100 + d.getDayOfMonth();
        assertEquals(dec(today), eval(env, "(today)"));
        assertEquals(dec(-4447), eval(env, "(days 19571029)"));
        try {
            eval(env, "(days 19579999)");
            fail();
        } catch (DecLispException e) {
            assertEquals(DateTimeException.class, e.getCause().getClass());
        }
        assertEquals(dec(0), eval(env, "(days 19700101)"));
        assertEquals(dec(19571029), eval(env, "(date -4447)"));
        assertEquals(dec(19700101), eval(env, "(date 0)"));
        try {
            eval(env, "(date " + Long.MAX_VALUE + ")");
            fail();
        } catch (DecLispException e) {
            assertEquals(DateTimeException.class, e.getCause().getClass());
        }
        assertEquals(sym("THURSDAY"), eval(env, "(week 19700101)"));
        assertEquals(sym("TUESDAY"), eval(env, "(week 19571029)"));
        try {
            eval(env, "(week 19579999)");
            fail();
        } catch (DecLispException e) {
            assertEquals(DateTimeException.class, e.getCause().getClass());
        }
    }

    @Test 
    public void testSolve() {
        Env env = defaultEnv();
        assertEquals(read("((x y) (2 2) (3 1))"), eval(env, """
            (solve
                ( (x (range 4))
                  (y (range 3)) )
                ( (= 4 (+ x y)) )
            )
            """));
        assertEquals(read("((x y) (2 2))"), eval(env, """
            (solve
                ( (x (range 4))
                  (y (range 3)) )
                ( (= 4 (* x y)) )
            )
            """));
        
        try {
            eval(env, """
            (solve
                ( (x (range 4))
                  (x (range 3)) )
                ( (+ x x) )
            )
            """);
            fail();
        } catch (DecLispException x) {
            assertEquals("variable 'x' duplicated", x.getMessage());
        }
    }

    // @Test 
    public void testSendMoreMoney() {
        Env env = defaultEnv();
        assertEquals(read("((s e n d m o r y) (9 5 6 7 1 0 8 2))"), eval(env, """
            (solve
                ( (s (range 1 10))
                  (e (range 10))
                  (n (range 10))
                  (d (range 10))
                  (m (range 1 10))
                  (o (range 10))
                  (r (range 10))
                  (y (range 10)) )
                ( (all-different s e n d m o r y)
                  (= (+ (number s e n d) (number m o r e)) (number m o n e y))  )
            )
            """));
    }

    @Test 
    public void testMinMax() {
        Env env = defaultEnv();
        assertEquals(read("((* x y) (0 0 0) (5 3 2))"), eval(env, """
            (min-max
                (+ x y)
                (x (range 4))
                (y (range 3)))
            """));
        assertEquals(read("((* x y) (1 1 1) (12 4 3))"), eval(env, """
            (min-max
                (* x y)
                (x (range 4 0))
                (y (range 3 0)))
            """));
        try {
            eval(env, """
            (min-max
                (+ x y)
                (x (range 4))
                (x (range 3)))
            """);
            fail();
        } catch (DecLispException x) {
            assertEquals("variables: duplicated variable 'x'", x.getMessage());
        }

    }

    @Test 
    public void testPolynomialAdd() {
        // (x²+2x+1)+(x+1) = x²+3x+2
        Expr[] a = {dec(1), dec(2), dec(1)};
        Expr[] b = {dec(1), dec(1)};
        Expr[] c = {dec(1), dec(3), dec(2)};
        assertArrayEquals(c, POLYNOMIAL_ADD.apply(a, b));
        // ()+(x+1) = x+1
        Expr[] d = {};
        Expr[] e = {dec(1), dec(1)};
        Expr[] f = {dec(1), dec(1)};
        assertArrayEquals(f, POLYNOMIAL_ADD.apply(d, e));
    }

    @Test 
    public void testPolynomialSubtract() {
        Expr[] a = {dec(1), dec(2), dec(1)};
        Expr[] b = {dec(1), dec(1)};
        Expr[] c = {dec(1), dec(1), dec(0)};
        assertArrayEquals(c, POLYNOMIAL_SUBTRACT.apply(a, b));
        Expr[] d = POLYNOMIAL_ADD_UNIT;
        Expr[] e = {dec(1), dec(1)};
        Expr[] f = {dec(-1), dec(-1)};
        assertArrayEquals(f, POLYNOMIAL_SUBTRACT.apply(d, e));
        Expr[] g = {dec(1), dec(0)};
        Expr[] h = {dec(1), dec(1)};
        Expr[] i = {dec(-1)};
        assertArrayEquals(i, POLYNOMIAL_SUBTRACT.apply(g, h));
    }

    @Test 
    public void testPolynomialMultiply() {
        // (x²+2x+1)(x+1) = x³+3x²+3x+1
        Expr[] a = {dec(1), dec(2), dec(1)};
        Expr[] b = {dec(1), dec(1)};
        Expr[] c = {dec(1), dec(3), dec(3), dec(1)};
        assertArrayEquals(c, POLYNOMIAL_MULTIPLY.apply(a, b));
        // 5(x+1) = 5x+5
        Expr[] d = {dec(5)};
        Expr[] e = {dec(1), dec(1)};
        Expr[] f = {dec(5), dec(5)};
        assertArrayEquals(f, POLYNOMIAL_MULTIPLY.apply(d, e));
        // ()(x+1) = ()
        Expr[] g = {};
        Expr[] h = {dec(1), dec(1)};
        Expr[] i = {dec(0)};
        assertArrayEquals(i, POLYNOMIAL_MULTIPLY.apply(g, h));
        // (1)(x²+2x+3) = x²+2x+3
        Expr[] j = POLYNOMIAL_MULTIPLY_UNIT;
        Expr[] k = {dec(1), dec(2), dec(3)};
        Expr[] l = {dec(1), dec(2), dec(3)};
        assertArrayEquals(l, POLYNOMIAL_MULTIPLY.apply(j, k));
    }

    @Test 
    public void testPolyDivide() {
        // x³+3x²+3x+1 / (x+1) = (x²+2x+1)
        Expr[] a = {dec(1), dec(3), dec(3), dec(1)};
        Expr[] b = {dec(1), dec(1)};
        Expr[] c = {dec(1), dec(2), dec(1)};
        Expr[] zero = {dec(0)};
        Expr[][] r = polyDivide(a, b);
        assertArrayEquals(c, r[0]); // 商
        assertArrayEquals(zero, r[1]); // 余り
        // -2x⁴ / (x²-1) = (-2x²-1) 余り -2
        Expr[] d = {dec(-2), dec(0), dec(0), dec(0), dec(0)};
        Expr[] e = {dec(1), dec(0), dec(-1)};
        Expr[] f = {dec(-2), dec(0), dec(-2)};
        Expr[] g = {dec(-2)};
        Expr[][] s = polyDivide(d, e);
        assertArrayEquals(f, s[0]); // 商
        assertArrayEquals(g, s[1]); // 余り
    }

    @Test 
    public void testPolynomial() {
        Env env = defaultEnv();
        assertEquals(read("()"), eval(env, "(p+)"));
        assertEquals(read("(1 1)"), eval(env, "(p+ '(1 1))"));
        assertEquals(read("(1 2 1)"), eval(env, "(p+ '(1 0 0) '(2 0) '(1))"));
        assertEquals(read("()"), eval(env, "(p-)"));
        assertEquals(read("(-1 -1)"), eval(env, "(p- '(1 1))"));
        assertEquals(read("(1 -2 -1)"), eval(env, "(p- '(1 0 0) '(2 0) '(1))"));
        assertEquals(read("()"), eval(env, "(p*)"));
        assertEquals(read("(1 1)"), eval(env, "(p* '(1 1))"));
        assertEquals(read("(1 3 3 1)"), eval(env, "(p* '(1 1) '(1 1) '(1 1))"));
        assertEquals(read("()"), eval(env, "(p/)"));
        assertEquals(read("(0)"), eval(env, "(p/ '(1 1))"));
        assertEquals(read("(1)"), eval(env, "(p/ '(1 1) '(1 1))"));
        assertEquals(read("(1 1)"), eval(env, "(p/ '(1 3 3 1) '(1 1) '(1 1))"));
        assertEquals(read("()"), eval(env, "(p%)"));
        assertEquals(read("(1)"), eval(env, "(p% '(1 1))"));
        assertEquals(read("(0)"), eval(env, "(p% '(1 1) '(1 1))"));
        assertEquals(read("(0)"), eval(env, "(p% '(1 3 3 1) '(1 1) '(1 1))"));
        assertEquals(read("(1 -2)"), eval(env, "(p/ '(1 -1 -6) '(1 1))"));
        assertEquals(read("(-4)"), eval(env, "(p% '(1 -1 -6) '(1 1))"));
        assertEquals(read("(1 -1 -6)"), eval(env, "(p+ (p* '(1 1) '(1 -2)) '(-4))"));
    }

    @Test
    public void testPolyValue() {
        Env env = defaultEnv();
        // x = 2 : (x + 1) = 3
        assertEquals(read("3"), eval(env, "(p= '(1 1) 2)"));
        // x = 2 : (x^2 + 2*x + 1) = 9
        assertEquals(read("9"), eval(env, "(p= '(1 2 1) 2)"));
        // x = 2 : (x^3 + 3*x^2 + 3*x + 1) = 27
        assertEquals(read("27"), eval(env, "(p= '(1 3 3 1) 2)"));
        assertEquals(read("64"), eval(env, "(p= '(1 3 3 1) 3)"));
    }

    static List<Expr> listArgsList(Expr args) {
        return StreamSupport.stream(listArgsIterable(args).spliterator(), false).toList();
    }

    @Test 
    public void testListArgsList() {
        assertEquals(List.of(read("(1 4)"), read("(2 5)"), read("(3 6)")), listArgsList(read("((1 2 3) (4 5 6))")));
        assertEquals(List.of(read("(1 4)"), read("(2 5)")), listArgsList(read("((1 2 3) (4 5))")));
        assertEquals(List.of(), listArgsList(read("(() ())")));
        assertEquals(List.of(), listArgsList(read("(() (3))")));
        // error case 1
        try {
            assertEquals(List.of(read("(1 4)"), read("(2 5)")), listArgsList(read("((1 2 3) (4 5 . 6))")));
            fail();
        } catch (DecLispException x) {
            assertEquals("invalid list element '6'", x.getMessage());
        }
        // error case 2
        try {
            listArgsList(read("((1 2 3) (4 5) .6)"));
            fail();
        } catch (DecLispException x) {
            assertEquals("invalid list element '6'", x.getMessage());
        }
        try {
            listArgsList(read("((1 2) 3)"));
        } catch (DecLispException x) {
            assertEquals("'3' is not a list", x.getMessage());
        }
    }
}
