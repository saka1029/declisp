package saka1029.declisp;

import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.junit.Test;

public class TestPermutation {

    interface Perm {
        void run(int n, int r, Consumer<int[]> callback);
    }

    static int[][] permutation(Perm body, int n, int r) {
        List<int[]> result = new ArrayList<>();
        body.run(n, r, a -> result.add(Arrays.copyOfRange(a, 0, r)));
        return result.stream().toArray(int[][]::new);
    }

    static void permutationBySwap(int n, int r, Consumer<int[]> callback) {
        int[] array = IntStream.range(0, n).toArray();
        new Object() {
            void swap(int i, int j) {
                int t = array[i];
                array[i] = array[j];
                array[j] = t;
            }
            void solve(int index) {
                if (index >= r)
                    callback.accept(array);
                else
                    for (int i = index; i < n; ++i) {
                        swap(index, i);
                        solve(index + 1);
                        swap(index, i);
                    }
            }
        }.solve(0);
    }

    static final int[][] PERM_4_0 = new int[][] {{}};
    static final int[][] PERM_4_1 = new int[][] {{0}, {1}, {2}, {3}};
    static final int[][] PERM_4_3 = new int[][] {
        {0, 1, 2}, {0, 1, 3}, {0, 2, 1}, {0, 2, 3}, {0, 3, 1}, {0, 3, 2},
        {1, 0, 2}, {1, 0, 3}, {1, 2, 0}, {1, 2, 3}, {1, 3, 0}, {1, 3, 2},
        {2, 0, 1}, {2, 0, 3}, {2, 1, 0}, {2, 1, 3}, {2, 3, 0}, {2, 3, 1},
        {3, 0, 1}, {3, 0, 2}, {3, 1, 0}, {3, 1, 2}, {3, 2, 0}, {3, 2, 1}};
    static final int[][] SWAP_PERM_4_3 = new int[][] {
        {0, 1, 2}, {0, 1, 3}, {0, 2, 1}, {0, 2, 3}, {0, 3, 2}, {0, 3, 1},
        {1, 0, 2}, {1, 0, 3}, {1, 2, 0}, {1, 2, 3}, {1, 3, 2}, {1, 3, 0},
        {2, 1, 0}, {2, 1, 3}, {2, 0, 1}, {2, 0, 3}, {2, 3, 0}, {2, 3, 1},
        {3, 1, 2}, {3, 1, 0}, {3, 2, 1}, {3, 2, 0}, {3, 0, 2}, {3, 0, 1}};
    static final int[][] PERM_64_1 = IntStream.range(0, 64)
        .mapToObj(i -> new int[]{i})
        .toArray(int[][]::new);

    @Test 
    public void testPermutationBySwap() {
        assertArrayEquals(PERM_4_0, permutation(TestPermutation::permutationBySwap, 4, 0));
        assertArrayEquals(PERM_4_1, permutation(TestPermutation::permutationBySwap, 4, 1));
        assertArrayEquals(SWAP_PERM_4_3, permutation(TestPermutation::permutationBySwap, 4, 3));
        assertArrayEquals(PERM_64_1, permutation(TestPermutation::permutationBySwap, 64, 1));
    }

    static void permutationBySet(int n, int r, Consumer<int[]> callback) {
        if (n < 0) throw new IndexOutOfBoundsException("must 0 <= n");
        if (r < 0 || r > n) throw new IndexOutOfBoundsException("must 0 <= r <= n");
        int[] array = new int[n];
        Set<Integer> used = new HashSet<>();
        new Object() {
            void solve(int index) {
                if (index >= r)
                    callback.accept(array);
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
        assertArrayEquals(PERM_4_0, permutation(TestPermutation::permutationBySet, 4, 0));
        assertArrayEquals(PERM_4_1, permutation(TestPermutation::permutationBySet, 4, 1));
        assertArrayEquals(PERM_4_3, permutation(TestPermutation::permutationBySet, 4, 3));
        assertArrayEquals(PERM_64_1, permutation(TestPermutation::permutationBySet, 64, 1));
    }

    /**
     * long値の各ビットをintの集合として処理する方式
     * 基本的にはpermutationBySetと同じであるが、
     * ループ内で空振りすることがない点が異なる。
     */
    static void permutationByBitMap(int n, int r, Consumer<int[]> callback) {
        if (n < 0 || n > Long.SIZE) throw new IndexOutOfBoundsException("must 0 <= n <= 64");
        if (r < 0 || r > n) throw new IndexOutOfBoundsException("must 0 <= r <= n");
        Objects.checkIndex(n, Long.SIZE + 1);   // longを[1...32]の集合として扱うため。
        Objects.checkIndex(r, n + 1);
        int[] array = new int[n];
        new Object () {
            long used = 0;
            void solve(int index) {
                int i;
                long bit;
                if (index >= r)             // r個の組み合わせが見つかった。
                    callback.accept(array);
                else
                    // Long.numberOfTrailingZeros(rest)はrestにおいて末尾に連続する0ビットの数を数える。
                    // つまりrestにおける最右端の1ビットのビット位置を求める。
                    for (long rest = ~used; (i = Long.numberOfTrailingZeros(rest)) < n; rest &= ~bit) {
                        bit = 1L << i;       // iのビット表現を得る。
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
        assertArrayEquals(PERM_4_0, permutation(TestPermutation::permutationByBitMap, 4, 0));
        assertArrayEquals(PERM_4_1, permutation(TestPermutation::permutationByBitMap, 4, 1));
        assertArrayEquals(PERM_4_3, permutation(TestPermutation::permutationByBitMap, 4, 3));
        assertArrayEquals(PERM_64_1, permutation(TestPermutation::permutationByBitMap, 64, 1));
    }

    static void permutationByBitSet(int n, int r, Consumer<int[]> callback) {
        if (n < 0) throw new IndexOutOfBoundsException("must 0 <= n");
        if (r < 0 || r > n) throw new IndexOutOfBoundsException("must 0 <= r <= n");
        int[] array = new int[n];
        new Object () {
            BitSet used = new BitSet(n);
            void solve(int index) {
                int i;
                if (index >= r)
                    callback.accept(array);
                else
                    for (BitSet loop = (BitSet)used.clone(); (i = loop.nextClearBit(0)) < n; loop.set(i)) {
                        used.set(i);
                        array[index] = i;
                        solve(index + 1);
                        used.clear(i);
                    }
            }
        }.solve(0);
    }

    @Test 
    public void testPermutationByBitSet() {
        assertArrayEquals(PERM_4_0, permutation(TestPermutation::permutationByBitSet, 4, 0));
        assertArrayEquals(PERM_4_1, permutation(TestPermutation::permutationByBitSet, 4, 1));
        assertArrayEquals(PERM_4_3, permutation(TestPermutation::permutationByBitSet, 4, 3));
        assertArrayEquals(PERM_64_1, permutation(TestPermutation::permutationByBitSet, 64, 1));
    }
}
