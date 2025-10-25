package me.kmathers.quill.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuillLexer {
    private String source;
    private int position;
    private int line;
    private int column;

    public QuillLexer(String source) {
        this.source = source;
        this.position = 0;
        this.line = 1;
        this.column = 1;
    }

    public static class Token {
        public String value;
        public TokenType kind;
        public int line;
        public int column;
        
        public Token(String value, TokenType kind, int line, int column) {
            this.value = value;
            this.kind = kind;
            this.line = line;
            this.column = column;
        }
    }

    public enum TokenType {
        // Literals
        Number,
        StringLiteral,
        True,
        False,
        Null,
        
        // Identifiers
        Identifier,
        
        // Keywords
        Let,
        Func,
        Return,
        If,
        Else,
        For,
        In,
        While,
        Break,
        Continue,
        New,
        Scope,
        OnEvent,
        Try,
        Catch,
        Import,
        Const,
        Function,
        
        // Operators
        Plus,           // +
        Minus,          // -
        Star,           // *
        Slash,          // /
        Percent,        // %
        Equals,         // =
        EqualsEquals,   // ==
        BangEquals,     // !=
        Greater,        // >
        Less,           // <
        GreaterEquals,  // >=
        LessEquals,     // <=
        And,            // &&
        Or,             // ||
        Bang,           // !
        
        // Delimiters
        OpenParen,      // (
        CloseParen,     // )
        OpenBrace,      // {
        CloseBrace,     // }
        OpenBracket,    // [
        CloseBracket,   // ]
        Comma,          // ,
        Dot,            // .
        Semicolon,      // ;
        Colon,          // :
        
        // Special
        EOF
    }

    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
        Map.entry("let", TokenType.Let),
        Map.entry("const", TokenType.Const),
        Map.entry("func", TokenType.Func),
        Map.entry("function", TokenType.Function),
        Map.entry("return", TokenType.Return),
        Map.entry("if", TokenType.If),
        Map.entry("else", TokenType.Else),
        Map.entry("for", TokenType.For),
        Map.entry("in", TokenType.In),
        Map.entry("while", TokenType.While),
        Map.entry("break", TokenType.Break),
        Map.entry("continue", TokenType.Continue),
        Map.entry("new", TokenType.New),
        Map.entry("Scope", TokenType.Scope),
        Map.entry("OnEvent", TokenType.OnEvent),
        Map.entry("try", TokenType.Try),
        Map.entry("catch", TokenType.Catch),
        Map.entry("import", TokenType.Import),
        Map.entry("true", TokenType.True),
        Map.entry("false", TokenType.False),
        Map.entry("null", TokenType.Null)
    );

    private Token createToken(String value, TokenType kind) {
        return new Token(value, kind, this.line, this.column);
    }

    private boolean isAlpha(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private boolean isDigit(char c) {
        return Character.isDigit(c);
    }

    private boolean isSkippable(char c) {
        return c == ' ' || c == '\n' || c == '\t' || c == '\r';
    }

    private char peek(int offset) {
        int pos = position + offset;
        if (pos >= source.length()) {
            return '\0';
        }
        return source.charAt(pos);
    }

    private char current() {
        if (position >= source.length()) {
            return '\0';
        }
        return source.charAt(position);
    }

    private void advance() {
        if (position < source.length()) {
            if (source.charAt(position) == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            position++;
        }
    }

    private String processStringLiteral() throws LexerException {
        char quote = current();
        advance(); // Skip opening quote
        
        StringBuilder sb = new StringBuilder();
        
        while (position < source.length() && current() != quote) {
            char c = current();
            
            if (c == '\n' || c == '\r') {
                throw new LexerException("Unterminated string literal: newline found before closing quote");
            }
            
            if (c == '\\') {
                advance();
                if (position < source.length()) {
                    char escapeChar = current();
                    switch (escapeChar) {
                        case 'n':
                            sb.append('\n');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case '\\':
                        case '\'':
                        case '"':
                            sb.append(escapeChar);
                            break;
                        default:
                            sb.append(escapeChar);
                            break;
                    }
                    advance();
                }
            } else {
                sb.append(c);
                advance();
            }
        }
        
        if (position >= source.length()) {
            throw new LexerException("Unterminated string literal: reached end of file before closing quote");
        }
        
        advance(); // Skip closing quote
        return sb.toString();
    }

    public List<Token> tokenize() throws LexerException {
        List<Token> tokens = new ArrayList<>();
        
        while (position < source.length()) {
            char c = current();
            
            // Handle comments
            if (c == '/' && peek(1) == '/') {
                // Single-line comment
                advance();
                advance();
                while (position < source.length() && current() != '\n') {
                    advance();
                }
                if (position < source.length()) {
                    advance();
                }
                continue;
            }
            
            if (c == '/' && peek(1) == '*') {
                // Multi-line comment
                advance();
                advance();
                while (position + 1 < source.length() && !(current() == '*' && peek(1) == '/')) {
                    advance();
                }
                if (position + 1 < source.length()) {
                    advance();
                    advance();
                } else {
                    throw new LexerException("Unterminated multi-line comment");
                }
                continue;
            }
            
            // Handle single-character tokens
            switch (c) {
                case '(':
                    tokens.add(createToken("(", TokenType.OpenParen));
                    advance();
                    break;
                case ')':
                    tokens.add(createToken(")", TokenType.CloseParen));
                    advance();
                    break;
                case '{':
                    tokens.add(createToken("{", TokenType.OpenBrace));
                    advance();
                    break;
                case '}':
                    tokens.add(createToken("}", TokenType.CloseBrace));
                    advance();
                    break;
                case '[':
                    tokens.add(createToken("[", TokenType.OpenBracket));
                    advance();
                    break;
                case ']':
                    tokens.add(createToken("]", TokenType.CloseBracket));
                    advance();
                    break;
                case ',':
                    tokens.add(createToken(",", TokenType.Comma));
                    advance();
                    break;
                case '.':
                    tokens.add(createToken(".", TokenType.Dot));
                    advance();
                    break;
                case ';':
                    tokens.add(createToken(";", TokenType.Semicolon));
                    advance();
                    break;
                case ':':
                    tokens.add(createToken(":", TokenType.Colon));
                    advance();
                    break;
                case '=':
                    if (peek(1) == '=') {
                        tokens.add(createToken("==", TokenType.EqualsEquals));
                        advance();
                        advance();
                    } else {
                        tokens.add(createToken("=", TokenType.Equals));
                        advance();
                    }
                    break;
                case '!':
                    if (peek(1) == '=') {
                        tokens.add(createToken("!=", TokenType.BangEquals));
                        advance();
                        advance();
                    } else {
                        tokens.add(createToken("!", TokenType.Bang));
                        advance();
                    }
                    break;
                case '>':
                    if (peek(1) == '=') {
                        tokens.add(createToken(">=", TokenType.GreaterEquals));
                        advance();
                        advance();
                    } else {
                        tokens.add(createToken(">", TokenType.Greater));
                        advance();
                    }
                    break;
                case '<':
                    if (peek(1) == '=') {
                        tokens.add(createToken("<=", TokenType.LessEquals));
                        advance();
                        advance();
                    } else {
                        tokens.add(createToken("<", TokenType.Less));
                        advance();
                    }
                    break;
                case '&':
                    if (peek(1) == '&') {
                        tokens.add(createToken("&&", TokenType.And));
                        advance();
                        advance();
                    } else {
                        throw new LexerException("Unrecognised character in source: " + c);
                    }
                    break;
                case '|':
                    if (peek(1) == '|') {
                        tokens.add(createToken("||", TokenType.Or));
                        advance();
                        advance();
                    } else {
                        throw new LexerException("Unrecognised character in source: " + c);
                    }
                    break;
                case '+':
                    tokens.add(createToken("+", TokenType.Plus));
                    advance();
                    break;
                case '-':
                    // Check if it's a negative number
                    if (isDigit(peek(1))) {
                        tokens.add(processNumber());
                    } else {
                        tokens.add(createToken("-", TokenType.Minus));
                        advance();
                    }
                    break;
                case '*':
                    tokens.add(createToken("*", TokenType.Star));
                    advance();
                    break;
                case '/':
                    tokens.add(createToken("/", TokenType.Slash));
                    advance();
                    break;
                case '%':
                    tokens.add(createToken("%", TokenType.Percent));
                    advance();
                    break;
                case '"':
                case '\'':
                    String str = processStringLiteral();
                    tokens.add(createToken(str, TokenType.StringLiteral));
                    break;
                default:
                    if (isDigit(c)) {
                        tokens.add(processNumber());
                    } else if (isAlpha(c)) {
                        tokens.add(processIdentifier());
                    } else if (isSkippable(c)) {
                        advance();
                    } else {
                        throw new LexerException("Unrecognised character in source: " + c);
                    }
                    break;
            }
        }
        
        tokens.add(createToken("EndOfFile", TokenType.EOF));
        return tokens;
    }

    private Token processNumber() {
        int startColumn = column;
        StringBuilder sb = new StringBuilder();
        
        if (current() == '-') {
            sb.append(current());
            advance();
        }
        
        boolean hasDecimal = false;
        while (position < source.length() && (isDigit(current()) || current() == '.')) {
            if (current() == '.') {
                if (hasDecimal) {
                    break;
                }
                hasDecimal = true;
            }
            sb.append(current());
            advance();
        }
        
        return new Token(sb.toString(), TokenType.Number, line, startColumn);
    }

    private Token processIdentifier() {
        int startColumn = column;
        StringBuilder sb = new StringBuilder();
        
        while (position < source.length() && (isAlpha(current()) || isDigit(current()))) {
            sb.append(current());
            advance();
        }
        
        String identifier = sb.toString();
        TokenType type = KEYWORDS.getOrDefault(identifier, TokenType.Identifier);
        return new Token(identifier, type, line, startColumn);
    }

    public static class LexerException extends Exception {
        public LexerException(String message) {
            super(message);
        }
    }
}