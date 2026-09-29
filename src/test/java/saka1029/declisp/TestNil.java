package saka1029.declisp;

import static org.junit.Assert.*;
import static saka1029.declisp.Common.*;

import org.junit.Test;

public class TestNil {

    @Test 
    public void testNil() {
        try {
            Nil.NIL.compareTo(dec(1));
        } catch (DecLispException x) {
            assertEquals("cannot compare '()' to '1'", x.getMessage());
        }
    }

}
