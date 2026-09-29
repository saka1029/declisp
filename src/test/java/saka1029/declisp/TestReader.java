package saka1029.declisp;

import static org.junit.Assert.*;
import static saka1029.declisp.Common.*;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

public class TestReader {

    @Test
    public void testReader() {
        java.io.Reader r = new java.io.Reader() {
            @Override public void close() throws IOException { }
            @Override public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException();
            }
        };
        try {
            Reader reader = new Reader(r);
            reader.read();
            fail();
        } catch (DecLispException x) {
            assertEquals(IOException.class, x.getCause().getClass());
        }
        try {
            new Reader("(a b").read();
            fail();
        } catch (DecLispEOFException x) {
            assertEquals("Reader.parseList(): Unexpected EOF", x.getMessage());
        }
    }

    @Test
    public void testRead() {
        assertEquals(dec(1), read("+1"));
        assertEquals(dec(-1), read("-1"));
        assertEquals(list(dec(1), sym("a")), read("(1 a)"));
        assertEquals(cons(dec(1), sym("a")), read("(1 . a)"));
        assertEquals(sym("AB"), read("AB"));
        assertEquals(dec(12.34), read("12.34"));
        assertEquals(dec(1234), read("12.34e2"));
        assertEquals(dec(1234), read("12.34E2"));
        assertEquals(dec(0.1234), read("12.34e-2"));
        assertEquals(dec(1234), read("12.34e+2"));
        assertEquals(sym(","), read(","));
        try {
            read("😀");
            fail();
        } catch (DecLispException x) {
            // System.out.println(x.getMessage());
            // assertEquals("Reader.read(): ", x.getMessage());
        }
    }

    @Test 
    public void testReadCodePoint() {
        String s = "𩸽";
        char highSurrogate = s.charAt(0);
        char lowSurrogate = s.charAt(1);
        assertTrue(Character.isHighSurrogate(highSurrogate));
        assertTrue(Character.isLowSurrogate(lowSurrogate));
        assertEquals(Reader.EOF, new Reader("%c".formatted(highSurrogate)).read());
        try {
            // high surrogateの後にlow surrogateが続かない場合。
            new Reader("%ca".formatted(highSurrogate)).read();
            fail();
        } catch (DecLispException x) {
            assertEquals("Reader.readCodePoint(): invalid surrogate pair", x.getMessage());
        }
    }

    @Test
    public void testParseList() {
        try {
            new Reader("( 1 . x x").read();
            fail();
        } catch (DecLispException x) {
            assertEquals("Reader.parseList(): ')' expected", x.getMessage());
        }
    }

    @Test
    public void testReadNoSpaces() {
        assertEquals(list(dec(12), sym("𩸽")), read("(12𩸽)"));
    }

    @Test
    public void testReadHokke() {
        /*
        𩸽
        UTF-8 Encoding:	0xF0 0xA9 0xB8 0xBD
        UTF-16 Encoding: 0xD867 0xDE3D
        UTF-32 Encoding: 0x00029E3D (171581)
         */
        assertEquals(sym("𩸽"), read("𩸽"));
    }

    @Test
    public void testSplit() {
        String line = "(abc de )s";
        String splitter = "(?<=[()])|(?=[()])|\\s+";
        String[] words = line.split(splitter);
        System.out.println(Arrays.toString(words));
    }
}
