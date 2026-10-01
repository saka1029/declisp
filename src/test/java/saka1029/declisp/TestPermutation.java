package saka1029.declisp;

import java.util.Arrays;
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
    public void testPermutation() {
        permutationBySwap(4, 4, a -> System.out.println(Arrays.toString(a)));
    }

    static void permutationByBitmap(int n, int r, Consumer<int[]> callback) {
        int[] array = new int[n];
        long available = Long.SIZE ? -1L : (1L << n) -1L;
        long rest =0;
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

}
