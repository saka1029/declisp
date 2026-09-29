package saka1029.declisp;

public class DecLispEOFException extends DecLispException {
    public DecLispEOFException(String format, Object... args) {
        super(format, args);
    }
}
