package saka1029.declisp;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

public class TestPermutation {

    static void print(int[] array, int r) {
        String result = IntStream.range(0, r)
            .mapToObj(i -> Integer.toString(array[i]))
            .collect(Collectors.joining(", ", "[", "]"));
        System.out.println(result);
    }

    static void permutationBySwap(int n, int r) {
        int[] array = IntStream.range(0, n).toArray();
        new Object() {
            void swap(int i, int j) {
                int t = array[i];
                array[i] = array[j];
                array[j] = t;
            }
            void solve(int index) {
                if (index >= r)
                    print(array, r);
                else
                    for (int i = index; i < n; ++i) {
                        swap(index, i);
                        solve(index + 1);
                        swap(index, i);
                    }
            }
        }.solve(0);
    }

    @Test 
    public void testPermutationBySwap() {
        System.out.println("permutationBySwap(4, 0):");
        permutationBySwap(4, 0);
        System.out.println("permutationBySwap(4, 4):");
        // 結果の順序が他と違う点に注意する。
        permutationBySwap(4, 4);
    }

    static void permutationBySet(int n, int r) {
        int[] array = new int[n];
        Set<Integer> used = new HashSet<>();
        new Object() {
            void solve(int index) {
                if (index >= r)
                    print(array, r);
                else
                    for (int i = 0; i < n; ++i) {
                        if (!used.contains(i)) {
                            used.add(i);
                            array[index] = i;
                            solve(index + 1);
                            used.remove(i);
                        }
                    }
            }
        }.solve(0);
    }

    @Test 
    public void testPermutationBySet() {
        System.out.println("permutationBySet(4, 0):");
        permutationBySet(4, 0);
        System.out.println("permutationBySet(4, 4):");
        permutationBySet(4, 4);
    }

    /**
     * long値の各ビットをintの集合として処理する方式
     * 基本的にはpermutationBySetと同じであるが、
     * ループ内で空振りすることがない点が異なる。
     */
    static void permutationByBitMap(int n, int r) {
        Objects.checkIndex(n, Long.SIZE + 1);   // longを[1...32]の集合として扱うため。
        Objects.checkIndex(r, n + 1);
        int[] array = new int[n];
        new Object () {
            long used = 0;
            void solve(int index) {
                int i;
                long bit;
                if (index >= r)             // r個の組み合わせが見つかった。
                    print(array, r);
                else
                    // Long.numberOfTrailingZeros(rest)はrestにおいて末尾に連続する0ビットの数を数える。
                    // つまりrestにおける最右端の1ビットのビット位置を求める。
                    for (long rest = ~used; (i = Long.numberOfTrailingZeros(rest)) < n; rest &= ~bit) {
                        bit = 1 << i;       // iのビット表現を得る。
                        used |= bit;        // usedにiを追加する。
                        array[index] = i;   // 結果にiを追加する。
                        solve(index + 1);   // indexより後の結果を求める。
                        used &= ~bit;       // usedからiを除外する。
                    }
            }
        }.solve(0);
    }

    @Test 
    public void testPermutationByBitMap() {
        System.out.println("permutationByBitMap(0, 0):");
        permutationByBitMap(0, 0);
        System.out.println("permutationByBitMap(4, 2):");
        permutationByBitMap(4, 2);
        System.out.println("permutationByBitMap(4, 4):");
        permutationByBitMap(4, 4);
    }
}
