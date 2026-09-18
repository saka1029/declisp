package saka1029.declisp;

public record Symbol(String name) implements Expr {

    public static final Symbol QUOTE = new Symbol("quote");

    @Override
    public final String toString() {
        return name;
    }
}
