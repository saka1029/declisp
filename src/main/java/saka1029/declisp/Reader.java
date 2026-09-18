package saka1029.declisp;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Reader {
    public static final Expr EOF = new Expr() {public Expr eval(Env env) {return null;};};

    final java.io.Reader reader;
    final CodePointBuffer buffer = new CodePointBuffer();
    int ch; // code point (not char)

    public Reader(java.io.Reader reader) {
        this.reader = reader;
        this.ch = get();
    }

    public Reader(String source) {
        this(new StringReader(source));
    }

    int readCodePoint() throws IOException {
        int hi = reader.read();
        if (hi == -1)
            return -1;
        if (!Character.isHighSurrogate((char)hi))
            return hi;
        int lo = reader.read();
        if (lo == -1)
            return -1;
        if (!Character.isLowSurrogate((char)lo))
            throw new IOException("readCodePoint: invalid surrogate pair");
        return Character.toCodePoint((char)hi, (char)lo);
    }

    int get() {
        try {
            ch = readCodePoint();
            buffer.append(ch);    // ch == EOFの時もappendする
            return ch;
        } catch (IOException e) {
            throw new DecLispException(e);
        }
    }

    int clearGet() {
        buffer.clear();
        return get();
    }

    void spaces() {
        while (Character.isWhitespace(ch))
            get();
        buffer.clearButLast();
    }

    Expr list() {
        clearGet();     // skip '('
        List<Expr> list = new ArrayList<>();
        while (true) {
            spaces();
            if (ch == ')') {
                clearGet();  // skip ')'
                return DecLisp.list(list);
            // no dot pair
            }
            Expr e = read();
            if (e == EOF)
                throw new DecLispException("Reader.list(): Unexpected EOF");
            list.addLast(e);
        }
    }

    Expr quote() {
        clearGet();  // skip '\''
        return DecLisp.list(Symbol.QUOTE, read());
    }

    static boolean isDigit(int ch) {
        return ch >= '0' && ch <= '9';
    }

    /**
     * 開始文字は
     * '+' D
     * '+' '.'
     * '-' D
     * '-' '.'
     * Digit
     * '.' D
     * BigDecimalString:
     *     [ '+' | '-' ] ( Digits [ '.' [ Digits ]] | '.' Digits ) [ ('e'|'E') [ '+' | '-'] Digits ]
     * Digits: Digit { Digit }
    */
    Dec decimal() {
        while (isDigit(ch))
            get();
        if (ch == '.') {
            get();
            while (isDigit(ch))
                get();
        }
        if (ch == 'E' || ch == 'e') {
            get();
            if (ch == '+' || ch == '-')
                get();
            while (isDigit(ch))
                get();
        }
        return new Dec(new BigDecimal(buffer.stringButLast()));
    }

    static boolean isSymbolFirst(int ch) {
        return switch (ch) {
            case -1, '(', ')' -> false;
            case '!', '#', '$', '%', '&', '|', '@', '=', '^',
                '+', '-', '*', '/', ';', ':', ',', '.',
                '_', '<', '>' -> true;
            default -> Character.isLetter(ch);
        };
    }

    static boolean isSymbolRest(int ch) {
        return isSymbolFirst(ch) || isDigit(ch);
    }

    Expr symbol() {
        while (isSymbolRest(ch))
            get();
        String value = buffer.stringButLast();
        return switch (value) {
            case "T" -> Bool.T;
            case "F" -> Bool.F;
            default -> new Symbol(value);
        };
    }

    public Expr read() {
        spaces();
        if (ch == -1)
            return EOF;
        else if (ch == '(')
            return list();
        else if (ch == '\'')
            return quote();
        else if (ch == '+')
            return isDigit(get()) ? decimal() : new Symbol("+");
        else if (ch == '-')
            return isDigit(get()) ? decimal() : new Symbol("-");
        else if (isDigit(ch))
            return decimal();
        else if (isSymbolFirst(ch))
            return symbol();
        else 
            throw new DecLispException("Reader.read(): Unexpected character '0x%x'", ch);
    }
}