package saka1029.declisp;

public class Bool implements Expr {
    public final boolean value;
    public static final Bool T = new Bool(true);
    public static final Bool F = new Bool(false);

    private Bool(boolean value) {
        this.value = value;
    }

    @Override
    public final String toString() {
        return value ? "T" : "F";
    }
}
