package saka1029.declisp;

import static saka1029.declisp.Common.*;

public record Closure( Expr parms, Expr body, Env env) implements Procedure {

    @Override
    public Expr apply(Expr evaled) {
        Env newEnv = new Env(env);
        parms.pairlis(evaled, newEnv);
        return progn(body, newEnv);
    }

    @Override
    public final String toString() {
        return cons(sym("closure"), cons(parms, body)).toString();
    }

}
