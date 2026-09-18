package saka1029.declisp;

public class Bool implements Expr {
    public static final Bool T = new Bool(true);
    public static final Bool F = new Bool(false);

    public final boolean value;

    private Bool(boolean value) {
        this.value = value;
    }

    public static Bool of(boolean value) {
        return value ? T : F;
    }

    @Override
    public Expr eval(Env env) {
        return this;
    }

    @Override
    public final String toString() {
        return value ? "T" : "F";
    }
}
