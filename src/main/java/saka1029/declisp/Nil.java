package saka1029.declisp;

public class Nil implements Expr{
    public static final Expr NIL = new Nil();

    @Override
    public Expr eval(Env env) {
        return this;
    }

    @Override 
    public String toString() {
        return "()";
    }
}
