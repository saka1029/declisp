package saka1029.declisp;

import static org.junit.Assert.*;
import static saka1029.declisp.DecLisp.*;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
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
        assertEquals(list(dec(1), Bool.F, dec(3)), eval(env,"(&& '(1 F 3))"));
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
        assertEquals(list(Bool.F, dec(3)), eval(env, "(list 'F 3)"));
        assertEquals(list(dec(3), Bool.F), eval(env, "(reverse '(F 3))"));
        assertEquals(list(Bool.F, dec(3), sym("a")), eval(env, "(append '(F 3) '(a))"));
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
        try {
            eval(env, "(/ 1 0)");
            fail();
        } catch (DecLispException x) {
            assertEquals("division by 0", x.getMessage());
        }
        assertEquals(dec(1), eval(env, "(%)"));
        assertEquals(dec(1), eval(env, "(% 2)"));
        assertEquals(dec(1), eval(env, "(% 1 4)"));
        assertEquals(dec(0), eval(env, "(% 24 2 3)"));
        assertEquals(dec(679), eval(env, "(% 1679 1000)"));
        try {
            eval(env, "(% 1 0)");
            fail();
        } catch (DecLispException x) {
            assertEquals("division by 0", x.getMessage());
        }
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
    public void testHelp() {
        Env env = defaultEnv();
        env.define(sym("aaaa"), Nil.NIL, VT.special, "arg", "text");
        env.define(sym("aaab"), Nil.NIL, VT.procedure, "arg", "text");
        env.define(sym("ccca"), Nil.NIL, VT.procedure, "arg", "text");
        StringBuilder sb = new StringBuilder();
        env.out = s -> sb.append(s);
        eval(env, "(help aaa)");
        assertEquals("special (aaaa arg) : text%nprocedure (aaab arg) : text%n".formatted(), sb.toString());
        sb.setLength(0);
        eval(env, "(help)");
        assertTrue(sb.toString().contains("quote"));
    }

    @Test 
    public void testMap() {
        Env env = defaultEnv();
        assertEquals(read("-1"), eval(env, "(- 1)"));
        assertEquals(read("()"), eval(env, "(map - '())"));
        assertEquals(read("()"), eval(env, "(map - )"));
        assertEquals(read("(-1 -2)"), eval(env, "(map - '(1 2))"));
        assertEquals(read("(-1 -2)"), eval(env, "(map - '(1 2))"));
        assertEquals(read("(2 4)"), eval(env, "(map (lambda (x) (+ x x)) '(1 2))"));
        assertEquals(read("(2 4)"), eval(env, "(map (lambda (x) (+ x x)) '(1 2) '(3 4))"));
        assertEquals(read("(-2 -2)"), eval(env, "(map - '(1 2) '(3 4))"));
        try {
            eval(env, "(map - '(1 2) '(3 4) 5)");
            fail();
        } catch (DecLispException x) {
            assertEquals("'5' is not a list", x.getMessage());
        }
        try {
            eval(env, "(map - '(1 2) '() '5)");
            fail();
        } catch (DecLispException x) {
            assertEquals("'5' is not a list", x.getMessage());
        }
        assertEquals(read("(F T)"), eval(env, "(map not '(T F))"));
        assertEquals(read("(T F F F)"), eval(env, "(map and '(T T F F) '(T F T F))"));
        assertEquals(read("(T T T F)"), eval(env, "(map or '(T T F F) '(T F T F))"));
        assertEquals(read("(F T T F)"), eval(env, "(map xor '(T T F F) '(T F T F))"));
        assertEquals(read("fact"), eval(env, "(define (fact n) (if (<= n 0) 1 (* n (fact (- n 1)))))"));
        assertEquals(read("(1 1 2 6 24 120)"), eval(env, "(map fact (range 0 5))"));
    }

    @Test 
    public void testFilter() {
        Env env = defaultEnv();
        assertEquals(read("()"), eval(env, "(filter odd '())"));
        assertEquals(read("(1 3 5)"), eval(env, "(filter odd (range 6))"));
        assertEquals(read("(2 4 6)"), eval(env, "(filter even (range 6))"));
    }

    @Test 
    public void testReduce() {
        Env env = defaultEnv();
        assertEquals(dec(15), eval(env, "(reduce 0 + (range 5))"));
        assertEquals(dec(0), eval(env, "(reduce 0 + ())"));
        assertEquals(dec(3), eval(env, "(reduce 3 + ())"));
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
    public void testEvenOdd() {
        Env env = defaultEnv();
        assertEquals(Bool.T, eval(env, "(even 0)"));
        assertEquals(Bool.F, eval(env, "(even 1)"));
        assertEquals(Bool.F, eval(env, "(odd 0)"));
        assertEquals(Bool.T, eval(env, "(odd 1)"));
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
        assertEquals(read("(1 2 3)"), eval(env, "(range 3)"));
        assertEquals(read("(0 1 2 3)"), eval(env, "(range 0 3)"));
        assertEquals(read("(0 -1 -2 -3)"), eval(env, "(range 0 -3)"));
        assertEquals(read("(0 0.5 1 1.5 2)"), eval(env, "(range 0 2 0.5)"));
        assertEquals(read("()"), eval(env, "(range 0 -2 0.5)"));
        assertEquals(read("(0 -0.2 -0.4 -0.6 -0.8 -1)"), eval(env, "(range 0 -1 -0.2)"));
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
        assertEquals(read("(2 4 6)"), eval(env, "(map (lambda (n) (* 2 n)) (range 3))"));
    }

    @Test 
    public void testApply() {
        Env env = defaultEnv();
        assertEquals(read("6"), eval(env, "(apply + '(1 2 3))"));
        assertEquals(read("5050"), eval(env, "(apply + (range 100))"));
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
    public void testSolveJava() {
        Env env = defaultEnv();
        List<String> out = new ArrayList<>();
        env.out(s -> out.add(s.trim()));
        eval(env, """
            (solve-java
                (var x (range 0 3))
                (var y (range 0 2))
                (rule x + y == 4)
            )
            """);
        assertEquals(List.of("x,y", "2,2", "3,1"), out);
        out.clear();
        eval(env, """
            (solve-java
                (var x (range 0 3))
                (var y (range 0 2))
                (rule x * y == 4)
            )
            """);
        assertEquals(List.of("x,y", "2,2"), out);
        out.clear();
        eval(env, """
            (solve-java
                (var a b (range 1 9))
                (var c (range 0 9))
                (rule all-different a b c)
                (rule number(a,b,c)+number(b,a,c)==number(c,a,c,a))
            )
            """);
        assertEquals(List.of("a,b,c", "2,9,1"), out);
    }

    @Test 
    public void testSolve() {
        Env env = defaultEnv();
        assertEquals(read("((x y) (2 2) (3 1))"),
            eval(env, """
                (solve
                    (var x (range 3))
                    (var y (range 2))
                    (rule (== 4 (+ x y)))
                )
                """));
        assertFalse(env.map.containsKey(sym("x")));
        assertEquals(read("((x y) (1 1) (1 2) (2 1) (3 2))"),
            eval(env, """
                (solve
                    (var x (range 3))
                    (var y (range 2))
                    (rule (isPrime (+ x y)))
                )
                """));
        assertEquals(read("((a b c) (2 9 1))"),
            eval(env, """
                (solve
                    (var a b c (range 9))
                    (rule (= (+ (number a b c) (number b a c)) (number c a c a)))
                )
                """));
        assertEquals(read("((a b c) (2 9 1))"),
            eval(env, """
                (solve
                    (var a b c (range 9))
                    (rule (= (+ (num a b c) (num b a c)) (num c a c a)))
                    (rule all-different a b c)
                    (code (num . x) (apply number x))
                )
                """));
        assertFalse(env.map.containsKey(sym("num")));
    }

    @Test 
    public void testSendMoreMoney() {
        Env env = defaultEnv();
        eval(env, """
            (solve-java
                (var s m (range 1 9))
                (var e n d o r y (range 0 9))
                (rule all-different s e n d m o r y)
                (rule number(s, e, n, d) + number(m, o, r, e) == number(m, o, n, e, y))
            )
            """);
    }

    @Test 
    public void testMinMax() {
        Env env = defaultEnv();
        assertEquals(read("((* x y) (0 0 0) (5 3 2))"), eval(env, """
            (min-max
                (+ x y)
                (x (range 0 3))
                (y (range 0 2)))
            """));
        assertEquals(read("((* x y) (1 1 1) (12 4 3))"), eval(env, """
            (min-max
                (* x y)
                (x (range 4 1))
                (y (range 3 1)))
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
    public void testPrimes() {
        Env env = defaultEnv();
        assertEquals(read("(2 3 5 7 11 13 17 19 23 29 31 37 41 43 47)"), eval(env, "(primes 50)"));
    }

    @Test 
    public void testIsPrime() {
        Env env = defaultEnv();
        assertEquals(Bool.F, eval(env, "(isPrime -2)"));
        assertEquals(Bool.F, eval(env, "(isPrime -1)"));
        assertEquals(Bool.F, eval(env, "(isPrime 0)"));
        assertEquals(Bool.F, eval(env, "(isPrime 1)"));
        assertEquals(Bool.T, eval(env, "(isPrime 2)"));
        assertEquals(Bool.T, eval(env, "(isPrime 3)"));
        assertEquals(Bool.F, eval(env, "(isPrime 4)"));
        assertEquals(Bool.T, eval(env, "(isPrime 5)"));
        assertEquals(Bool.F, eval(env, "(isPrime 6)"));
        assertEquals(Bool.T, eval(env, "(isPrime 7)"));
        assertEquals(Bool.F, eval(env, "(isPrime 8)"));
        assertEquals(Bool.F, eval(env, "(isPrime 9)"));
        assertEquals(Bool.F, eval(env, "(isPrime 10)"));
        assertEquals(Bool.T, eval(env, "(isPrime 11)"));
    }

    @Test 
    public void testFactor() {
        Env env = defaultEnv();
        assertEquals(read("(7 11 13)"), eval(env, "(factor 1001)"));
        try {
            eval(env, "(factor 0)");
            fail();
        } catch (DecLispException x) {
            assertEquals("Cannot factor zero", x.getMessage());
        }
    }

    @Test 
    public void testDivisor() {
        Env env = defaultEnv();
        assertEquals(read("(1)"), eval(env, "(divisor 1)"));
        assertEquals(read("(1 3)"), eval(env, "(divisor 3)"));
        assertEquals(read("(1 2 3 6)"), eval(env, "(divisor 6)"));
    }

    @Test 
    public void testP() {
        Env env = defaultEnv();
        assertEquals(read("1"), eval(env, "(P 1 1)"));
        assertEquals(read("2"), eval(env, "(P 2 1)"));
        assertEquals(read("2"), eval(env, "(P 2 2)"));
        assertEquals(read("3"), eval(env, "(P 3 1)"));
        assertEquals(read("6"), eval(env, "(P 3 2)"));
        assertEquals(read("6"), eval(env, "(P 3 3)"));
    }

    @Test 
    public void testPermutation() {
        Env env = defaultEnv();
        env.define(sym("*LIST*"), Nil.NIL);
        eval(env, "(permutation 1 1 (lambda (x) (set *LIST* (cons x *LIST*))))");
        assertEquals(read("((0))"), eval(env, "(reverse *LIST*)"));
        env.define(sym("*LIST*"), Nil.NIL);
        eval(env, "(permutation 2 1 (lambda (x) (set *LIST* (cons x *LIST*))))");
        assertEquals(read("((0) (1))"), eval(env, "(reverse *LIST*)"));
        env.define(sym("*LIST*"), Nil.NIL);
        eval(env, "(permutation 2 2 (lambda (x) (set *LIST* (cons x *LIST*))))");
        assertEquals(read("((0 1) (1 0))"), eval(env, "(reverse *LIST*)"));
        env.define(sym("*LIST*"), Nil.NIL);
        eval(env, "(permutation 3 1 (lambda (x) (set *LIST* (cons x *LIST*))))");
        assertEquals(read("((0) (1) (2))"), eval(env, "(reverse *LIST*)"));
        env.define(sym("*LIST*"), Nil.NIL);
        eval(env, "(permutation 3 2 (lambda (x) (set *LIST* (cons x *LIST*))))");
        assertEquals(read("((0 1) (0 2) (1 0) (1 2) (2 0) (2 1))"), eval(env, "(reverse *LIST*)"));
    }

    @Test 
    public void testCombination() {
        Env env = defaultEnv();
        env.define(sym("*LIST*"), Nil.NIL);
        eval(env, "(combination 1 1 (lambda (x) (set *LIST* (cons x *LIST*))))");
        assertEquals(read("((0))"), eval(env, "(reverse *LIST*)"));
        env.define(sym("*LIST*"), Nil.NIL);
        eval(env, "(combination 2 1 (lambda (x) (set *LIST* (cons x *LIST*))))");
        assertEquals(read("((0) (1))"), eval(env, "(reverse *LIST*)"));
        env.define(sym("*LIST*"), Nil.NIL);
        eval(env, "(combination 2 2 (lambda (x) (set *LIST* (cons x *LIST*))))");
        assertEquals(read("((0 1))"), eval(env, "(reverse *LIST*)"));
        env.define(sym("*LIST*"), Nil.NIL);
        eval(env, "(combination 3 1 (lambda (x) (set *LIST* (cons x *LIST*))))");
        assertEquals(read("((0) (1) (2))"), eval(env, "(reverse *LIST*)"));
        env.define(sym("*LIST*"), Nil.NIL);
        eval(env, "(combination 3 2 (lambda (x) (set *LIST* (cons x *LIST*))))");
        assertEquals(read("((0 1) (0 2) (1 2))"), eval(env, "(reverse *LIST*)"));
    }

    @Test 
    public void testC() {
        Env env = defaultEnv();
        assertEquals(read("1"), eval(env, "(C 1 1)"));
        assertEquals(read("2"), eval(env, "(C 2 1)"));
        assertEquals(read("1"), eval(env, "(C 2 2)"));
        assertEquals(read("3"), eval(env, "(C 3 1)"));
        assertEquals(read("3"), eval(env, "(C 3 2)"));
        assertEquals(read("1"), eval(env, "(C 3 3)"));

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
        assertEquals(read("(0)"), eval(env, "(p+)"));
        assertEquals(read("(1 1)"), eval(env, "(p+ '(1 1))"));
        assertEquals(read("(1 2 1)"), eval(env, "(p+ '(1 0 0) '(2 0) '(1))"));
        assertEquals(read("(0)"), eval(env, "(p-)"));
        assertEquals(read("(-1 -1)"), eval(env, "(p- '(1 1))"));
        assertEquals(read("(1 -2 -1)"), eval(env, "(p- '(1 0 0) '(2 0) '(1))"));
        assertEquals(read("(1)"), eval(env, "(p*)"));
        assertEquals(read("(1 1)"), eval(env, "(p* '(1 1))"));
        assertEquals(read("(1 3 3 1)"), eval(env, "(p* '(1 1) '(1 1) '(1 1))"));
        assertEquals(read("(1)"), eval(env, "(p/)"));
        assertEquals(read("(0)"), eval(env, "(p/ '(1 1))"));
        assertEquals(read("(1)"), eval(env, "(p/ '(1 1) '(1 1))"));
        assertEquals(read("(1 1)"), eval(env, "(p/ '(1 3 3 1) '(1 1) '(1 1))"));
        assertEquals(read("(1)"), eval(env, "(p%)"));
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
        // error case 3
        try {
            listArgsList(read("((1 2) 3)"));
        } catch (DecLispException x) {
            assertEquals("'3' is not a list", x.getMessage());
        }
    }

    @Test 
    public void testFukumen() {
        Env env = defaultEnv();
        List<String> out = new ArrayList<>();
        env.out(s -> out.add(s));
        eval(env, "(fukumen abc+bac=caca)");
        String nl = System.lineSeparator();
        assertEquals(List.of("a,b,c" + nl, "2,9,1" + nl), out);
    }

    @Test 
    public void testAt() {
        Env env = defaultEnv();
        assertEquals(dec(1), eval(env, "(at '(0 1 2) 1)"));
        assertEquals(list(dec(1), dec(2)), eval(env, "(at '(0 1 2) '(1 2))"));
        try {
            eval(env, "(at '(0 1 2) -1)");
            fail();
        } catch (DecLispException x) {
            assertEquals("index '-1' out of bounds", x.getMessage());
        }
        try {
            eval(env, "(at '(0 1 2) 3)");
            fail();
        } catch (DecLispException x) {
            assertEquals("index '3' out of bounds", x.getMessage());
        }
    }
}
