package saka1029.declisp;

public record Symbol(String value) implements Expr {

    @Override
    public Expr eval(Env env) {
        return env.get(this);
    }

    @Override
    public final String toString() {
        return value;
    }
}
