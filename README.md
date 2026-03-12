# Compiler-phases-java

This is a Lexical Analyzer (Scanner) — the first phase of a compiler.
It reads raw Java source code character by character and breaks it down into meaningful units called tokens. Think of it as the compiler's "reading comprehension" step before any actual understanding of the code can happen.
What it recognizes:
It can identify reserved words like if, while, class, etc., identifiers (variable/method names), integer and float literals, string and character literals, operators (both single like + and compound like +=, ==), punctuation like {}, [], (), and it skips over both single-line (//) and multi-line (/* */) comments.
How it works:
It uses a finite state machine (FSM) — a classic compiler theory concept. It starts in state 0 and transitions between states depending on each character it reads. For example, reading a letter moves it to the identifier state, reading a digit moves it to the number state, and so on. When it hits a character that doesn't fit the current state, it "accepts" whatever it has built so far as a token and resets.
Output:
It writes a formatted table mapping each lexeme (the raw text) to its token type — for example while → WHILE or 123 → INT_LITERAL.
It's a solid foundational compiler project — lexical analysis is typically the first thing covered in compiler design courses, and implementing it with a pushback reader and manual state machine shows a good grasp of the underlying theory.

