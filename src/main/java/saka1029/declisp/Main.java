package saka1029.declisp;

import static saka1029.declisp.Common.*;
import static saka1029.declisp.DecLisp.defaultEnv;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.jline.reader.CompletingParsedLine;
import org.jline.reader.EOFError;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.SyntaxError;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp.Capability;

public class Main {

    static class SimpleDecListParser extends DefaultParser {

        static class MyParsedLine implements CompletingParsedLine {
            final String word;
            public MyParsedLine(String word) { this.word = word; }
            @Override public String word() { return word; }
            @Override public int wordCursor() { return 0; }
            @Override public int wordIndex() { return 0; }
            @Override public List<String> words() { return List.of(); }
            @Override public String line() { return word; }
            @Override public int cursor() { return 0; }
            @Override public CharSequence escape(CharSequence candidate, boolean complete) { return null; }
            @Override public int rawWordCursor() { return 0; }
            @Override public int rawWordLength() { return 0; }
        }

        @Override
        public ParsedLine parse(String line, int cursor, ParseContext context) throws SyntaxError {
            try {
                new Reader(line).read();
                var parsedLine = super.parse(line, cursor, context);
                // System.out.println(r.words());
                return parsedLine;
                // return new MyParsedLine(line);
            } catch (DecLispEOFException x) {
                throw new EOFError(0, 0, x.getMessage());
            }
        }
    }

    static final String ERROR_COLOR  = "\u001b[00;91m";
    static final String ERROR_COLOR_END  = "\u001b[00m";
    static final String PROMPT = "\u001b[00;92mdecl> \u001b[00m";
    static final Symbol LAST_RESULT = sym("$");

    public static void main(String[] args) {
        try (Terminal terminal = TerminalBuilder.builder().system(true).build();) {
            Env env = defaultEnv();
            // Completer completer = new StringsCompleter(
            //     env.sortedHelp().stream()
            //         .map(h -> h.name.value())
            //         .toArray(String[]::new));
            LineReader reader = LineReaderBuilder.builder()
                .parser(new SimpleDecListParser())
                // .completer(completer)
                .terminal(terminal)
                .variable(LineReader.EDIT_AND_EXECUTE_COMMAND, "vi")    // 有効でない
                .variable(LineReader.EDITING_MODE, "vi")                // 有効でない
                .variable(LineReader.HISTORY_FILE, Paths.get(System.getProperty("user.home"), ".decl_history"))
                .variable(LineReader.HISTORY_SIZE, 1000)
                .variable(LineReader.HISTORY_FILE_SIZE, 2000)
                .variable(LineReader.SECONDARY_PROMPT_PATTERN, "%M%P > ")
                .build();


            while (true) {
                try {
                    String line = reader.readLine(PROMPT);
                    if ("exit".equalsIgnoreCase(line))
                        break;
                    Expr evaled = eval(env, line);
                    env.define(LAST_RESULT, evaled);
                    terminal.puts(Capability.orig_pair);
                    terminal.writer().println(evaled);
                    terminal.flush();
                } catch (UserInterruptException uie) {
                    terminal.writer().println("Ctrl-C entered");
                } catch (DecLispException x) {
                    // terminal.puts(Capability.set_a_foreground, 1);   // IOErrorになる。
                    terminal.writer().print(ERROR_COLOR);
                    terminal.writer().println(x.getMessage());
                    terminal.writer().print(ERROR_COLOR_END);
                    terminal.flush();
                }
            }
        } catch (EndOfFileException e) {
            System.out.println("EOF entered");
        } catch (IOException e) {
            System.err.println("Error creating terminal: " + e.getMessage());
        }
    }
}
