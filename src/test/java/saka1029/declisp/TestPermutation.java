package saka1029.declisp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.junit.Test;

public class TestPermutation {

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
                    callback.accept(Arrays.copyOfRange(array, 0, r));
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
        permutationBySwap(4, 4, a -> System.out.println(Arrays.toString(a)));
    }

    static void permutationBySet(int n, int r, Consumer<int[]> callback) {
        int[] array = new int[n];
        Set<Integer> used = new HashSet<>();
        new Object() {
            void solve(int index) {
                if (index >= r)
                    callback.accept(Arrays.copyOfRange(array, 0, r));
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
        permutationBySet(4, 4, a -> System.out.println(Arrays.toString(a)));
    }

}
