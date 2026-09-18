package saka1029.declisp;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

interface Expr extends Iterable<Expr> {

    Expr eval(Env env);

    default <T> T cast(Class<T> cls) {
        if (cls.isInstance(this))
            return cls.cast(this);
        else
            throw new DecLispException("cast: cannot cast '%s' to '%s'", this, cls.getSimpleName());
    }

    default Expr car() { return cast(Cons.class).car(); }
    default Expr cdr() { return cast(Cons.class).cdr(); }

    public static Expr list(Expr... list) {
        Expr r = Nil.NIL;
        for (int i = list.length - 1; i >= 0; --i)
            r = new Cons(list[i], r);
        return r;

    }

    default Expr evlis(Env env) {
        return list(this.stream()
            .map(e -> e.eval(env))
            .toArray(Expr[]::new));
    }

    default void pairlis(Expr args, Env env) {
        for (Expr p : this) {
            env.define(p.cast(Symbol.class), args.car());
            args = args.cdr();
        }
    }

    default Stream<Expr> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    default Iterator<Expr> iterator() {
        return new Iterator<>() {
            Expr expr = Expr.this;

            @Override
            public boolean hasNext() {
                return expr instanceof Cons;
            }

            @Override
            public Expr next() {
                if (expr instanceof Cons c) {
                    expr = c.cdr();
                    return c.car();
                } else
                    throw new NoSuchElementException();
            }
        };
    }
}
