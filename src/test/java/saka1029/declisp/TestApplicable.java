package saka1029.declisp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static saka1029.declisp.DecLisp.*;

import org.junit.Test;

public class TestApplicable {

    @Test 
    public void TestApply() {
        Env env = defaultEnv();
        try {
            Applicable a = (args, e) -> Nil.NIL;
            a.eval(env);
            fail();
        } catch (DecLispException x) {
            assertEquals("Expr.eval(): cannot eval", x.getMessage());
        }
    }

}
