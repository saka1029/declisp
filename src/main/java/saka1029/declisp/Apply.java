package saka1029.declisp;

public interface Apply extends Expr {
    Expr apply(Expr args, Env env);
    default Expr eval(Env env) {
        throw new DecLispException("cannot apply");
    }
}
