package saka1029.declisp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static saka1029.declisp.Bool.*;

public class TestBool {

    @Test 
    public void testBool() {
        Env env = new Env();
        assertEquals("T", T.toString());
        assertEquals("F", F.toString());
        assertEquals(T, Bool.of(true));
        assertEquals(F, Bool.of(false));
        assertEquals(T, T.eval(env));
        assertEquals(F, F.eval(env));
    }

}
