package saka1029.declisp;

import static saka1029.declisp.Common.*;

import java.math.BigDecimal;

public class Dec implements Expr {
    public final BigDecimal value;

    public Dec(BigDecimal value) {
        this.value = value;
    }

    @Override
    public Expr eval(Env env) {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Dec r && r.value.compareTo(value) == 0;
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString().replaceFirst("\\.0$", "");
    }

    @Override
    public int compareTo(Expr arg0) {
        return value.compareTo(dec(arg0));
    }
}
