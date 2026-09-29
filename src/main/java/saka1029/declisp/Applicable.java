package saka1029.declisp;

public interface Applicable extends Expr {

    Expr apply(Expr args, Env env);

}
