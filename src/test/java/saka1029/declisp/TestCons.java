package saka1029.declisp;

import static org.junit.Assert.*;
import static saka1029.declisp.DecLisp.*;

import java.util.NoSuchElementException;

import static saka1029.declisp.Common.*;

import org.junit.Test;

public class TestCons {

    @Test
    public void testCons() {
        assertEquals("(1)", cons(dec(1), Nil.NIL).toString());
        assertEquals("(1 2)", cons(dec(1), cons(dec(2), Nil.NIL)).toString());
        assertEquals("(1 . 2)", cons(dec(1), dec(2)).toString());
        var iter = read("(1 2)").iterator();
        assertTrue(iter.hasNext()); assertEquals(dec(1), iter.next());
        assertTrue(iter.hasNext()); assertEquals(dec(2), iter.next());
        assertFalse(iter.hasNext());
        try {
            iter.next();
            fail();
        } catch (NoSuchElementException x) {
            assertEquals(null, x.getMessage());
        }
    }

    @Test 
    public void testEval() {
        Env env = defaultEnv();
        env.define(sym("a"), Nil.NIL);
        try {
            eval(env, "(car (a 1))");
            fail();
        } catch (DecLispException x) {
            assertEquals("cannot apply '()' to '(1)'", x.getMessage());
        }

    }

}
