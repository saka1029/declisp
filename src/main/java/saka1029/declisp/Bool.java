package saka1029.declisp;

import static saka1029.declisp.Common.*;

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

    @Override
    public int compareTo(Expr arg0) {
        return Boolean.compare(value, bool(arg0));
    }
}
