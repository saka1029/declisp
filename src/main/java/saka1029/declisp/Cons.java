package saka1029.declisp;

public record Cons(Expr car, Expr cdr) implements Expr {

    public Cons(Expr car, Expr cdr) {
        if (!cdr.equals(Nil.NIL) && !(cdr instanceof Cons))
            throw new DecLispException("cons: cannot cons '%s' and '%s'", car, cdr);
        this.car = car;
        this.cdr = cdr;
    }

    public static Expr list(Expr... list) {
        Expr r = Nil.NIL;
        for (int i = list.length - 1; i >= 0; --i)
            r = new Cons(list[i], r);
        return r;
    }

    @Override
    public Expr eval(Env env) {
        Expr head = car.eval(env);
        if (head instanceof Apply app)
            return app.apply(cdr, env);
        else if (head instanceof Dec)   // リストの先頭が数字ならevlisする
            return new Cons(head, cdr.evlis(env));
        else if (head instanceof Bool)   // リストの先頭が真偽値ならevlisする
            return new Cons(head, cdr.evlis(env));
        else
            throw new DecLispException("eval(): Cannot apply '%s' to '%s'", head, cdr);
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        if (cdr instanceof Cons cdr && car.equals(Symbol.QUOTE)) // && cdr.cdr.equals(NIL))
            return sb.append("'").append(cdr.car).toString();
        sb.append("(").append(car.toString());
        for (Expr c : cdr())
            sb.append(" ").append(c.toString());
        return sb.append(")").toString();
    }
}
