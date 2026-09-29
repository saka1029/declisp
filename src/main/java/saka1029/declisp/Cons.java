package saka1029.declisp;

import static saka1029.declisp.Common.*;

/**
 * イミュータブルである。(car, cdrの更新はできない)
 * 点対はある。
 * 
 * @param car 任意のExprが指定可。
 * @param cdr 任意のExprが指定可。
 */
public record Cons(Expr car, Expr cdr) implements Expr {

    public Cons(Expr car, Expr cdr) {
        this.car = car;
        this.cdr = cdr;
    }

    @Override
    public Expr eval(Env env) {
        Expr head = car.eval(env);
        if (head instanceof Applicable app)
            return app.apply(cdr, env);
        else if (head instanceof Dec)   // リストの先頭が数字ならevlisする
            return new Cons(head, cdr.evlis(env));
        else if (head instanceof Bool)   // リストの先頭が真偽値ならevlisする
            return new Cons(head, cdr.evlis(env));
        else
            throw new DecLispException("Cons.eval(): Cannot apply '%s' to '%s'", head, cdr);
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        if (cdr instanceof Cons cdr && car.equals(QUOTE)) // && cdr.cdr.equals(NIL))
            return sb.append("'").append(cdr.car).toString();
        sb.append("(").append(car.toString());
        Expr e;
        for (e = cdr(); e instanceof Cons c; e = c.cdr)
            sb.append(" ").append(c.car);
        if (!(e instanceof Nil))
            sb.append(" . ").append(e);
        return sb.append(")").toString();
    }
}
