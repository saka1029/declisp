# samples

## send more money (solve)

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

## send more money (fukumen)

```
(fukumen send + more = money)
```