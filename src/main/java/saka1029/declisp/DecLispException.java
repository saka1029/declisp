package saka1029.declisp;

public class DecLispException extends RuntimeException {
    public DecLispException(String format, Object... args) {
        super(format.formatted(args));
    }
    public DecLispException(Throwable cause) {
        super(cause);
    }
}