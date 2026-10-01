package saka1029.declisp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static saka1029.declisp.Common.*;;

public class TestHelp {

    @Test 
    public void testToString() {
        Help variable = new Help(VT.variable, sym("variable"), "args", "text");
        assertEquals("variable variable", variable.toString());
        Help special = new Help(VT.special, sym("special"), "args", "text");
        assertEquals("special (special args) : text", special.toString());
        Help procedure = new Help(VT.procedure, sym("procedure"), "args", "text");
        assertEquals("procedure (procedure args) : text", procedure.toString());
        Help noArgs = new Help(VT.procedure, sym("noArgs"), "", "text");
        assertEquals("procedure (noArgs) : text", noArgs.toString());
    }

}
