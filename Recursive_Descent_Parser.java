import java.io.*;

// Recursive Descent Parser - checks if the input code follows correct syntax rules
public class RecursiveDescentParser_CPCS_302_PP2 {
    static String input;
    static int pos = 0;
    static String token; // current token being looked at

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("input.txt"));
        StringBuilder code = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
            code.append(line).append(" ");
        br.close();

        // clean up extra whitespace from the input
        input = code.toString().replaceAll("\\s+", " ").trim();
        nextToken();

        try {
            while (!lookhead().equals("")) {
                statement();
            }
            System.out.println("Correct Syntax");
        } catch (Exception e) {
            System.out.println("Syntax Error");
        }
    }

    // reads the next token from the input
    static void nextToken() {
        skipWhitespace();
        if (pos >= input.length()) {
            token = "";
            return;
        }

        char c = input.charAt(pos);

        if (Character.isLetter(c)) {
            int start = pos;
            while (pos < input.length() && Character.isLetterOrDigit(input.charAt(pos)))
                pos++;
            token = input.substring(start, pos);
        } else if (Character.isDigit(c)) {
            token = String.valueOf(c);
            pos++;
        } else {
            // check for two-character operators like ==, !=, <=, >=
            if (pos + 1 < input.length()) {
                String two = input.substring(pos, pos + 2);
                if (two.equals("==") || two.equals("!=") || two.equals("<=") || two.equals(">=")) {
                    token = two;
                    pos += 2;
                    return;
                }
            }
            token = String.valueOf(c);
            pos++;
        }
    }

    static void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos)))
            pos++;
    }

    // returns the current token without consuming it
    static String lookhead() {
        return token;
    }

    // consumes the current token if it matches what we expect, otherwise throws an error
    static void match(String expected) {
        if (lookhead().equals(expected)) {
            nextToken();
        } else {
            error();
        }
    }

    static void error() {
        throw new RuntimeException("Syntax Error");
    }

    // decides which statement rule to apply based on the current token
    static void statement() {
        switch (lookhead()) {
            case "int": case "boolean": case "String": case "char":
                declaration(); break;
            case "if":
                ifStatement(); break;
            case "while":
                whileStatement(); break;
            case "for":
                forStatement(); break;
            case "{":
                block(); break;
            default:
                if (isIdentifier(lookhead())) {
                    assignment();
                } else error();
        }
    }

    // e.g. int x; or int x = 5;
    static void declaration() {
        type();
        identifier();
        decl();
    }

    // handles the optional assignment part in a declaration
    static void decl() {
        if (lookhead().equals("=")) {
            match("=");
            expr();
            match(";");
        } else {
            match(";");
        }
    }

    // e.g. x = 5;
    static void assignment() {
        identifier();
        match("=");
        expr();
        match(";");
    }

    // e.g. if (x > 0) { ... } else { ... }
    static void ifStatement() {
        match("if");
        match("(");
        expr();
        match(")");
        statement();
        elseStatement();
    }

    // optional else branch
    static void elseStatement() {
        if (lookhead().equals("else")) {
            match("else");
            statement();
        }
    }

    // e.g. while (x > 0) { ... }
    static void whileStatement() {
        match("while");
        match("(");
        expr();
        match(")");
        statement();
    }

    // e.g. for (int i = 0; i < 10; i + 1) { ... }
    static void forStatement() {
        match("for");
        match("(");
        declaration();
        expr();
        match(";");
        expr();
        match(")");
        statement();
    }

    // a block of statements wrapped in { }
    static void block() {
        match("{");
        while (!lookhead().equals("}")) {
            statement();
        }
        match("}");
    }

    // expression with optional relational operator (e.g. x + 1 > 0)
    static void expr() {
        simpleExpr();
        if (isRelop(lookhead())) {
            relop();
            simpleExpr();
        }
    }

    // handles + and - operations
    static void simpleExpr() {
        term();
        while (lookhead().equals("+") || lookhead().equals("-")) {
            match(lookhead());
            term();
        }
    }

    // handles * and / operations
    static void term() {
        factor();
        while (lookhead().equals("*") || lookhead().equals("/")) {
            match(lookhead());
            factor();
        }
    }

    // the smallest unit - a number, identifier, or a parenthesized expression
    static void factor() {
        if (lookhead().equals("(")) {
            match("(");
            expr();
            match(")");
        } else if (isIdentifier(lookhead()) || isNumber(lookhead())) {
            match(lookhead());
        } else {
            error();
        }
    }

    static void type() {
        if (lookhead().equals("int") || lookhead().equals("boolean") ||
            lookhead().equals("String") || lookhead().equals("char")) {
            match(lookhead());
        } else error();
    }

    static void identifier() {
        if (isIdentifier(lookhead())) {
            match(lookhead());
        } else error();
    }

    static void relop() {
        if (isRelop(lookhead())) {
            match(lookhead());
        } else error();
    }

    // identifiers are single lowercase letters in this parser
    static boolean isIdentifier(String token) {
        return token.matches("[a-z]");
    }

    // numbers are single digits in this parser
    static boolean isNumber(String token) {
        return token.matches("[0-9]");
    }

    static boolean isRelop(String token) {
        return token.equals("<") || token.equals(">") ||
               token.equals("<=") || token.equals(">=") ||
               token.equals("==") || token.equals("!=");
    }
}