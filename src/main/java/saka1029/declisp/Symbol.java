package saka1029.declisp;

public record Symbol(String name) implements Expr {

    public static final Symbol QUOTE = new Symbol("quote");

    @Override
    public Expr eval(Env env) {
        return env.get(this);
    }

    @Override
    public final String toString() {
        return name;
    }
}
