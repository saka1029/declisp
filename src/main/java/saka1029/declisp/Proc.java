package saka1029.declisp;

public interface Proc extends Apply {

    Expr apply(Expr evaled);

    default Expr apply(Expr args, Env env) {
        return apply(args.evlis(env));
    }
}

