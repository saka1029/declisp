package saka1029.declisp;

public class Env {
    KeyValue kv;
    public Env() { this.kv = null; }
    public Env(Env e) { this.kv = e.kv; }
    public Symbol define(Symbol key, Expr value) {
        kv = new KeyValue(kv, key, value);
        return key;
    }

    public Expr get(Symbol key) {
        for (KeyValue x = kv; x != null; x = x.prev)
            if (x.key.equals(key))
                return x.value;
        throw new DecLispException("get(): Not found %s", key);
    }

    public Expr set(Symbol key, Expr value) {
        for (KeyValue x = kv; x != null; x = x.prev)
            if (x.key.equals(key))
                return x.value = value;
        throw new DecLispException("set(): Not found %s", key);
    }
}

class KeyValue {
    KeyValue prev; Symbol key; Expr value;
    public KeyValue(KeyValue prev, Symbol key, Expr value) {
        this.prev = prev; this.key = key; this.value = value;
    }
}
