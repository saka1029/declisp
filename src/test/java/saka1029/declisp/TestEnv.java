package saka1029.declisp;

import static org.junit.Assert.*;
import static saka1029.declisp.Common.*;

import org.junit.Test;

public class TestEnv {

    @Test 
    public void testEnv() {
        Env env = new Env();
        env.define(sym("A"), dec(3));
        assertEquals(dec(3), env.get(sym("A")));
        env.set(sym("A"), dec(7));
        assertEquals(dec(7), env.get(sym("A")));
        assertEquals("{}", new Env(env).toString());
        try {
            assertEquals(dec(3), env.get(sym("F")));
            fail();
        } catch (DecLispException x) {
        }
        try {
            env.set(sym("F"), dec(999));
            fail();
        } catch (DecLispException x) {
        }
    }

}
