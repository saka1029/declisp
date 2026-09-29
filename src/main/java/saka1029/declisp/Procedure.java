package saka1029.declisp;

public interface Procedure extends Applicable {

    Expr apply(Expr evaled);

    default Expr apply(Expr args, Env env) {
        return apply(args.evlis(env));
    }
}

