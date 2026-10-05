package saka1029.declisp;

import static saka1029.declisp.Common.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class Reader {
    public static final Expr EOF = new Expr() {};

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
            throw new DecLispException("Reader.readCodePoint(): invalid surrogate pair");
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

    Expr parseList() {
        clearGet();     // skip '('
        List<Expr> list = new ArrayList<>();
        while (true) {
            spaces();
            if (ch == ')') {
                clearGet(); // skip ')'
                return list(list);
            // no dot pair
            } else if (ch == '.') {
                clearGet(); // skip '.'
                list.add(read());
                spaces();
                if (ch != ')')
                    throw new DecLispException("Reader.parseList(): ')' expected");
                clearGet(); // skip ')'
                return listDot(list);
            }
            Expr e = read();
            if (e == EOF)
                throw new DecLispEOFException("Reader.parseList(): Unexpected EOF");
            list.addLast(e);
        }
    }

    Expr parseQuote() {
        clearGet();  // skip '\''
        return list(QUOTE, read());
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
    Dec parseDecimal() {
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
        return new Dec(bigDec(buffer.stringButLast()));
    }

    static boolean isSymbolFirst(int ch) {
        return switch (ch) {
            case -1, '(', ')' -> false;
            case '!', '#', '$', '%', '&', '|', '@', '=', '^',
                '~', '+', '-', '*', '/', ';', ':', ',', '.',
                '_', '<', '>', '{', '}', '[', ']' -> true;
            default -> Character.isLetter(ch);
        };
    }

    static boolean isSymbolRest(int ch) {
        return isSymbolFirst(ch) || isDigit(ch);
    }

    Expr parseSymbol() {
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
            return parseList();
        else if (ch == '\'')
            return parseQuote();
        else if (ch == '+')
            return isDigit(get()) ? parseDecimal() : parseSymbol();
        else if (ch == '-')
            return isDigit(get()) ? parseDecimal() : parseSymbol();
        else if (isDigit(ch))
            return parseDecimal();
        else if (isSymbolFirst(ch))
            return parseSymbol();
        else 
            throw new DecLispException("Reader.read(): Unexpected character '0x%x'", ch);
    }
}