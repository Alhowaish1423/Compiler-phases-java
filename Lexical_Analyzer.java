import java.io.*;
import java.util.*;

// Lexical Analyzer - converts source code into tokens using a Finite State Machine (FSM)
public class LexicalAnalyzer {

    private PushbackReader reader; // allows us to "unread" a character if we go too far
    private BufferedWriter writer;
    private StringBuilder lexeme; // builds up the current token character by character
    private int currentState;     // tracks which FSM state we're in

    // all Java reserved words - used to tell keywords apart from identifiers
    private static final Set<String> reservedWords = new HashSet<>(Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while"
    ));

    public LexicalAnalyzer(String inputFile, String outputFile) throws IOException {
        reader = new PushbackReader(new FileReader(inputFile));
        writer = new BufferedWriter(new FileWriter(outputFile));
        lexeme = new StringBuilder();
        currentState = 0; // start state
    }

    public void tokenize() throws IOException {
        int charCode;
        char ch;

        writer.write(String.format("%-20s %s%n", "[Lexemes]", "[Tokens]"));
        writer.write("----------------------------------------\n");

        while ((charCode = reader.read()) != -1) {
            ch = (char) charCode;

            // dispatch to the correct state handler
            switch (currentState) {
                case 0:
                    handleState0(ch);
                    break;
                case 1:
                    handleState1(ch);
                    break;
                case 3:
                    handleState3(ch);
                    break;
                case 4:
                    handleState4(ch);
                    break;
                case 5:
                    handleState5(ch);
                    break;
                case 6:
                    handleState6(ch);
                    break;
                case 7:
                    handleState7(ch);
                    break;
                case 9:
                    handleState9(ch);
                    break;
                case 10:
                    handleState10(ch);
                    break;
                default:
                    error();
                    currentState = 0;
                    break;
            }
        }

        // Check if any remaining lexeme to process
        if (lexeme.length() > 0) {
            processLexeme();
        }

        writer.close();
        reader.close();
    }

    // start state - decides what kind of token we're starting
    private void handleState0(char ch) throws IOException {
        if (Character.isLetter(ch) || ch == '_' || ch == '$') {
            lexeme.append(ch);
            currentState = 1; // go to identifier state
        } else if (Character.isDigit(ch)) {
            lexeme.append(ch);
            currentState = 3; // go to number state
        } else if (ch == '"') {
            lexeme.append(ch);
            currentState = 5; // go to string literal state
        } else if (ch == '\'') {
            lexeme.append(ch);
            currentState = 6; // go to char literal state
        } else if (ch == '/') {
            lexeme.append(ch);
            currentState = 7; // might be division or a comment
        } else if (isOperator(ch)) {
            lexeme.append(ch);
            processOperator(ch);
            currentState = 0;
        } else if (isPunctuation(ch)) {
            lexeme.append(ch);
            processPunctuation(ch);
            currentState = 0;
        } else if (Character.isWhitespace(ch)) {
            currentState = 0; // skip whitespace
        } else {
            error();
        }
    }

    // state 1 - reading an identifier or keyword
    private void handleState1(char ch) throws IOException {
        if (Character.isLetterOrDigit(ch) || ch == '_' || ch =='$') {
            lexeme.append(ch);
            currentState = 1;
        } else {
            reader.unread(ch); // put back the character that ended the identifier
            processLexeme();
            currentState = 0;
        }
    }

    // state 3 - reading an integer, transitions to float state if we see a dot
    private void handleState3(char ch) throws IOException {
        if (Character.isDigit(ch)) {
            lexeme.append(ch);
            currentState = 3;
        } else if (ch == '.') {
            lexeme.append(ch);
            currentState = 4; // switch to float state
        } else {
            reader.unread(ch);
            processLexeme();
            currentState = 0;
        }
    }

    // state 4 - reading the decimal part of a float
    private void handleState4(char ch) throws IOException {
        if (Character.isDigit(ch)) {
            lexeme.append(ch);
            currentState = 4;
        } else {
            reader.unread(ch);
            processLexeme();
            currentState = 0;
        }
    }

    // state 5 - inside a string literal, handles escape sequences
    private void handleState5(char ch) throws IOException {
        lexeme.append(ch);
        if (ch == '"') {
            processLexeme(); // closing quote found
            currentState = 0;
        } else if (ch == '\\') {
            currentState = 9; // next char is an escape sequence
        } else {
            currentState = 5;
        }
    }

    // state 6 - inside a char literal, handles escape sequences
    private void handleState6(char ch) throws IOException {
        lexeme.append(ch);
        if (ch == '\'') {
            processLexeme(); // closing quote found
            currentState = 0;
        } else if (ch == '\\') {
            currentState = 10; // next char is an escape sequence
        } else {
            currentState = 6;
        }
    }

    // state 7 - after reading '/', check if it's a comment or just division
    private void handleState7(char ch) throws IOException {
        if (ch == '/') {
            lexeme.setLength(0);
            skipSingleLineComment();
            currentState = 0;
        } else if (ch == '*') {
            lexeme.setLength(0);
            skipMultiLineComment();
            currentState = 0;
        } else {
            reader.unread(ch);
            processOperator('/'); // just a division operator
            currentState = 0;
        }
    }

    // state 9 - escaped character inside a string, go back to string state
    private void handleState9(char ch) throws IOException {
        lexeme.append(ch);
        currentState = 5;
    }

    // state 10 - escaped character inside a char literal, go back to char state
    private void handleState10(char ch) throws IOException {
        lexeme.append(ch);
        currentState = 6;
    }

    // classifies the completed lexeme and writes it to the output file
    private void processLexeme() throws IOException {
        String lex = lexeme.toString(); 
        lexeme.setLength(0);

        if (reservedWords.contains(lex)) {
            writer.write(String.format("%-20s %s%n", lex, lex.toUpperCase()));
        } else if (lex.matches("\\d+")) {
            writer.write(String.format("%-20s %s%n", lex, "INT_LITERAL"));
        } else if (lex.matches("\\d+\\.\\d+")) { //  \d+ . \d+
            writer.write(String.format("%-20s %s%n", lex, "FLOAT_LITERAL"));
        } else if (lex.startsWith("\"") && lex.endsWith("\"")) {
            writer.write(String.format("%-20s %s%n", lex, "STRING_LITERAL"));
        } else if (lex.startsWith("'") && lex.endsWith("'")) {
            writer.write(String.format("%-20s %s%n", lex, "CHAR_LITERAL"));
        } else if (lex.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            writer.write(String.format("%-20s %s%n", lex, "ID"));
        } else {
            writer.write(String.format("%-20s %s%n", lex, "UNKNOWN_TOKEN"));
        }
    }

    // checks if the operator is compound (e.g. ++ or >=), otherwise treats it as single
    private void processOperator(char ch) throws IOException {
        String op = String.valueOf(ch); // covert it to string + -> "+" 
        int nextChar = reader.read();

        if (nextChar != -1) {
            char nextCh = (char) nextChar;
            String combinedOp = op + nextCh;

            if (isCombinedOperator(combinedOp)) {
                op = combinedOp;
            } else {
                reader.unread(nextCh);
            }
        }

        writer.write(String.format("%-20s %s%n", op, getOperatorToken(op)));
        lexeme.setLength(0);
    }

    private void processPunctuation(char ch) throws IOException {
        writer.write(String.format("%-20s %s%n", ch, getPunctuationToken(ch)));
        lexeme.setLength(0);
    }

    // skip everything until end of line
    private void skipSingleLineComment() throws IOException {
        int charCode;
        while ((charCode = reader.read()) != -1) {
            if ((char) charCode == '\n') {  // '\n' == enter ( next line)
                return;
            }
        }
    }

    // skip everything until we find the closing */
    private void skipMultiLineComment() throws IOException {
        int charCode;
        while ((charCode = reader.read()) != -1) {
            if ((char) charCode == '*') {
                if ((charCode = reader.read()) != -1 && (char) charCode == '/') {
                    return;
                }
            }
        }
    }

    private void error() throws IOException {
        writer.write(String.format("%-20s %s%n", lexeme.toString(), "UNRECOGNIZED_TOKEN"));
        lexeme.setLength(0);
        currentState = 0;
    }

    private boolean isOperator(char ch) {
        return "+-*/%=!<>|&".indexOf(ch) != -1;
    }

    private boolean isCombinedOperator(String op) {
        return Arrays.asList("++", "--", "+=", "-=", "*=", "/=", "%=", "==", "!=", "<=", ">=", "&&", "||").contains(op);
    }

    private String getOperatorToken(String op) {
        switch (op) {
            case "+":return "ADD_OP";
            case "-":return "SUB_OP";
            case "*":return "MUL_OP";
            case "/":return "DIV_OP";
            case "%":return "MOD_OP";
            case "=":return "ASSIGN_OP";
            case "++":return "INC_OP";
            case "--":return "DEC_OP";
            case "+=":return "ADD_ASSIGN_OP";
            case "-=":return "SUB_ASSIGN_OP";
            case "*=":return "MUL_ASSIGN_OP";
            case "/=":return "DIV_ASSIGN_OP";
            case "%=":return "MOD_ASSIGN_OP";
            case "==":return "EQ_OP";
            case "!=":return "NE_OP";
            case "<":return "LT_OP";
            case "<=":return "LE_OP";
            case ">":return "GT_OP";
            case ">=":return "GE_OP";
            case "&&":return "AND_OP";
            case "||":return "OR_OP";
            case "!":return "NOT_OP";
            default: return "UNKNOWN_OP";
        }
    }

    private boolean isPunctuation(char ch) {
        return "(){}[];,:.?".indexOf(ch) != -1;
    }

    private String getPunctuationToken(char ch) {
        switch (ch) {
            case '(':return "LEFT_PAREN";
            case ')':return "RIGHT_PAREN";
            case '{':return "LEFT_CURLY";
            case '}':return "RIGHT_CURLY";
            case '[':return "LEFT_SQUARE";
            case ']':return "RIGHT_SQUARE";
            case ';':return "SEMI_COLON";
            case ',':return "COMMA";
            case ':':return "COLON";
            case '.':return "DOT";
            case '?':return "QUESTION_MARK";
            default: return "UNKNOWN_PUNCT";
        }
    }

    public static void main(String[] args) {
        try {
            LexicalAnalyzer analyzer = new LexicalAnalyzer("input.txt", "output.txt");
            analyzer.tokenize();
            System.out.println("Tokens have been identified successfully!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}