package saka1029.declisp;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class TestDecLispException {

    @Test 
    public void testDecLispException() {
        assertEquals(IOException.class, new DecLispException(new IOException()).getCause().getClass());
    }

}
