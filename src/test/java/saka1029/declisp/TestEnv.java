package saka1029.declisp;

import static org.junit.Assert.*;
import static saka1029.declisp.Common.*;

import java.util.ArrayList;
import java.util.List;

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

    @Test 
    public void testSortedHelp() {
        Env env = new Env();
        env.define(sym("z"), Nil.NIL, VT.procedure, "args", "text");
        env.define(sym("a"), Nil.NIL, VT.procedure, "args", "text");
        env.define(sym("b"), Nil.NIL, VT.procedure, "args", "text");
        assertEquals(List.of(
            new Help(VT.procedure, sym("a"), "args", "text"),
            new Help(VT.procedure, sym("b"), "args", "text"),
            new Help(VT.procedure, sym("z"), "args", "text")),
            env.sortedHelp());
    }
}
