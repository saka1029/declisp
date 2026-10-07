# samples

## send more money

### solve-java

Javaコンパイラを使用したsolver
制約(rule)はJavaの文法で記述する点に注意する。

```
(solve-java
    (var s m (range 1 9))
    (var e n d o r y (range 0 9))
    (rule number(s,e,n,d) + number(m,o,r,e) == number(m,o,n,e,y))
    (rule all-different s e n d m o r y)
)
```

任意のJavaコードを記述する例。
code内はJavaの文法で記述する点に注意する。

```
(solve-java
    (var s m (range 1 9))
    (var e n d o r y (range 0 9))
    (rule n(s,e,n,d) + n(m,o,r,e) == n(m,o,n,e,y))
    (rule all-different s e n d m o r y)
    (code static int n(int... ds) {)
    (code   int r = 0;)
    (code   for (int i : ds))
    (code     r = 10 * r + i;)
    (code   return r;)
    (code })
)
```

## solve

Javaコンパイラを使用しないsolverで、
制約式(rule)は参照コード(code)にはdeclispの式が記述できる。
ただし遅い。

```
(solve
    (var s m (range 9))
    (var e n d o r y (range 0 9))
    (rule all-different s e n d m o r y)
    (rule (= (+ (num s e n d) (num m o r e)) (num m o n e y)))
    (code (num . x) (apply number x))
)
```


### fukumen

fukumenは覆面算に特化したsolverで、Javaコンパイラを使用している。

```
(fukumen send + more = money)
```