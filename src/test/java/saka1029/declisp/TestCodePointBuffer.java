package saka1029.declisp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestCodePointBuffer {

    @Test 
    public void testStringButLast() {
        CodePointBuffer cp = new CodePointBuffer();
        for (int i = 0; i < 100; ++i)
            cp.append('A');
        assertEquals("A".repeat(99), cp.stringButLast());
    }

    @Test 
    public void testPop() {
        CodePointBuffer cp = new CodePointBuffer();
        for (int i = 0; i < 100; ++i)
            cp.append('A');
        cp.pop();
        assertEquals("A".repeat(98), cp.stringButLast());
    }

    @Test 
    public void testClearButLast() {
        CodePointBuffer cp = new CodePointBuffer();
        for (int i = 0; i < 100; ++i)
            cp.append('A');
        cp.clearButLast();
        assertEquals("A".repeat(0), cp.stringButLast());
    }

    @Test 
    public void testClear() {
        CodePointBuffer cp = new CodePointBuffer();
        for (int i = 0; i < 100; ++i)
            cp.append('A');
        cp.clear();
        cp.append('B');
        cp.append('B');
        assertEquals("B".repeat(1), cp.stringButLast());
    }
}
