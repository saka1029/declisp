package saka1029.declisp;

import java.util.Arrays;

public class CodePointBuffer {
    int[] buffer;
    int next = 0;

    public CodePointBuffer() { this.buffer = new int[64]; }

    public void append(int cp) {
        if (next >= buffer.length)
            buffer = Arrays.copyOf(buffer, buffer.length * 2);
        buffer[next++] = cp;
    }

    int pop() { return buffer[--next]; }

    public void clear() { next = 0; }

    public void clearButLast() {
        int last = pop();
        next = 0;
        append(last);
    }

    public String stringButLast() {
        int last = pop();
        String s = new String(buffer, 0, next);
        next = 0;
        append(last);
        return s;
    }
}