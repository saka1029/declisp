package saka1029.declisp;

import static saka1029.declisp.Common.NO_VALUE;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class Env {
    final Map<Symbol, Expr> map = new HashMap<>();
    final Map<Symbol, Help> help = new HashMap<>();
    final Env prev;
    Consumer<String> out = System.out::print;

    public Env() { this.prev = null; }
    public Env(Env prev) {
        this.prev = prev;
        this.out = prev.out;
    }

    public Symbol define(Symbol key, Expr value) {
        map.put(key, value);
        return key;
    }
    public Symbol define(Symbol key, Expr value,
            VT type, String args, String text) {
        help.put(key, new Help(type, key, args, text));
        return define(key, value);
    }

    public Expr get(Symbol key) {
        for (Env e = this; e != null; e = e.prev) {
            Expr value = e.map.get(key);
            if (value != null)
                return value;
        }
        throw new DecLispException("Env.get(): symbol '%s' not found", key);
    }

    public void out(Consumer<String> out) {
        for (Env env = this; env != null; env = prev)
            env.out = out;
    }

    public Expr print(Object obj) {
        out.accept(obj.toString());
        return NO_VALUE;
    }

    public Expr println(Object obj) {
        out.accept("%s%n".formatted(obj));
        return NO_VALUE;
    }

    public Expr set(Symbol key, Expr value) {
        for (Env e = this; e != null; e = e.prev) {
            if (e.map.containsKey(key)) {
                e.map.put(key, value);
                return value;
            }
        }
        throw new DecLispException("Env.set(): symbol '%s' not found", key);
    }

    public List<Help> sortedHelp() {
        Map<Symbol, Help> all = new HashMap<>();
        new Object() {
            void put(Env e) {
                if (e.prev != null)
                    put(e.prev);
                for (Entry<Symbol, Help> x : e.help.entrySet())
                    all.put(x.getKey(), x.getValue());
            }
        }.put(this);
        return all.values().stream()
            .sorted(Comparator.comparing(h -> h.name().value()))
            .toList();
    }

    @Override
    public String toString() {
        // return map.toString() + (prev == null ? "" : " -> " + prev.toString());
        return map.toString();
    }
}