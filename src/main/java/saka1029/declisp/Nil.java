package saka1029.declisp;

public class Nil implements Expr{
    public static final Expr NIL = new Nil();

    @Override 
    public String toString() {
        return "()";
    }
}
