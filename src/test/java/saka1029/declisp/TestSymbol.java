package saka1029.declisp;

import static org.junit.Assert.*;
import static saka1029.declisp.Common.*;

import org.junit.Test;

public class TestSymbol {

    @Test 
    public void testSym() {
        assertEquals("name", sym(sym("name")));
    }
}
