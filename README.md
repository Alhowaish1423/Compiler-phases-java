# Compiler-phases-java

Lexical Analyzer (Scanner) — the first phase of a compiler.
It reads raw Java source code character by character and breaks it down into meaningful units called tokens. Think of it as the compiler's "reading comprehension" step before any actual understanding of the code can happen.
What it recognizes:
It can identify reserved words like if, while, class, etc., identifiers (variable/method names), integer and float literals, string and character literals, operators (both single like + and compound like +=, ==), punctuation like {}, [], (), and it skips over both single-line (//) and multi-line (/* */) comments.
How it works:
It uses a finite state machine (FSM) — a classic compiler theory concept. It starts in state 0 and transitions between states depending on each character it reads. For example, reading a letter moves it to the identifier state, reading a digit moves it to the number state, and so on. When it hits a character that doesn't fit the current state, it "accepts" whatever it has built so far as a token and resets.
Output:
It writes a formatted table mapping each lexeme (the raw text) to its token type — for example while → WHILE or 123 → INT_LITERAL.
It's a solid foundational compiler project — lexical analysis is typically the first thing covered in compiler design courses, and implementing it with a pushback reader and manual state machine shows a good grasp of the underlying theory.

------------------------------------------------------------------------------------------

Recursive Descent Parser — the second phase of a compiler, which runs after the Lexical Analyzer.
What it does:
It takes the input source code and checks whether it follows the correct grammatical structure of the language. It doesn't care about meaning — only about whether the syntax is valid, like making sure every if has a condition in parentheses, or every variable declaration ends with a semicolon.
How it works:
Each grammar rule has its own method. For example ifStatement(), whileStatement(), forStatement(), block() and so on. The parser reads one token at a time using nextToken(), peeks at the current token with lookhead(), and uses match() to consume an expected token or throw an error if it doesn't match.
What it recognizes:
It supports variable declarations (int, boolean, String, char), assignments, if/else statements, while and for loops, blocks {}, and arithmetic/relational expressions.
Output:
Simply prints either "Correct Syntax" or "Syntax Error" — no tree is built, just validation.
How it connects to your first project:
Your Lexical Analyzer handles what the tokens are. This parser handles how those tokens are arranged. In a full compiler pipeline, the output of the lexer feeds directly into the parser — they're two consecutive stages of the same process.



