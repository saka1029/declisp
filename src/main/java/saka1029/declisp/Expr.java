package saka1029.declisp;

import static saka1029.declisp.Common.*;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Expr extends Iterable<Expr>, Comparable<Expr> {

    default Expr eval(Env env) {
        throw new DecLispException("cannot eval", this);
    }

    default boolean isNil() {
        return this.equals(Nil.NIL);
    }

    default boolean isList() {
        return isNil() || this instanceof Cons;
    }

    default <T> T cast(Class<T> cls) {
        if (cls.isInstance(this))
            return cls.cast(this);
        else
            throw new DecLispException("type '%s' expected but '%s' appeared", cls.getSimpleName(), this);
    }

    default Expr evlis(Env env) {
        // System.out.println(this);
        return list(this.stream()
            .map(e -> e.eval(env))
            .toArray(Expr[]::new));
    }

    default void pairlis(Expr args, Env env) {
        Expr a;
        for (a = this; a instanceof Cons c; a = c.cdr(), args = cdr(args))
            env.define(symbol(car(a)), car(args));
        if (a instanceof Symbol sym)
            env.define(sym, args);
    }

    default Expr[] array() {
        return stream().toArray(Expr[]::new);
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
                if (expr instanceof Cons)
                    return true;
                else if (expr instanceof Nil)
                    return false;
                else
                    throw new DecLispException("invalid list element '%s'", expr);
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

    @Override
    default int compareTo(Expr arg0) {
        throw new DecLispException("cannot compare '%s' to '%s'", this, arg0);
    }
}
