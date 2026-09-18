package saka1029.declisp;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

interface Expr extends Iterable<Expr> {

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
