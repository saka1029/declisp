package saka1029.declisp;

public record Cons(Expr car, Expr cdr) implements Expr {

    public Cons(Expr car, Expr cdr) {
        if (!cdr.equals(Nil.NIL) && !(cdr instanceof Cons))
            throw new DecLispException("cons: cannot cons '%s' and '%s'", car, cdr);
        this.car = car;
        this.cdr = cdr;
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        if (cdr instanceof Cons cdr && car.equals(Symbol.QUOTE)) // && cdr.cdr.equals(NIL))
            return sb.append("'").append(cdr.car).toString();
        sb.append("(").append(car.toString());
        for (Expr c : cdr())
            sb.append(" ").append(c.toString());
        return sb.append(")").toString();
    }
}
