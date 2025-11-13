package me.kmathers.quill.parser;

import me.kmathers.quill.Quill;
import me.kmathers.quill.lexer.QuillLexer.Token;
import me.kmathers.quill.lexer.QuillLexer.TokenType;
import me.kmathers.quill.parser.AST.*;

import java.util.ArrayList;
import java.util.List;

public class QuillParser {
    private List<Token> tokens;
    private int position;
    
    public QuillParser(List<Token> tokens) {
        this.tokens = tokens;
        this.position = 0;
    }
    
    // === Helper Methods ===
    
    private Token current() {
        if (position >= tokens.size()) {
            return tokens.get(tokens.size() - 1); // Return EOF
        }
        return tokens.get(position);
    }
    
    @SuppressWarnings("unused")
    private Token peek(int offset) {
        int pos = position + offset;
        if (pos >= tokens.size()) {
            return tokens.get(tokens.size() - 1);
        }
        return tokens.get(pos);
    }
    
    private Token consume(TokenType expected) throws ParseException {
        Token token = current();
        if (token.kind != expected) {
            throw new ParseException(Quill.getPlugin(Quill.class).translate("quill.error.runtime.parser.expected", expected, token.kind, token.line));
        }
        position++;
        return token;
    }
    
    private boolean check(TokenType type) {
        return current().kind == type;
    }
    
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                return true;
            }
        }
        return false;
    }
    
    // === Main Parse Method ===
    
    public Program parse() throws ParseException {
        Program program = new Program(1, 1);
        program.statements = new ArrayList<>();
        
        while (!check(TokenType.EOF)) {
            program.statements.add(parseStatement());
        }
        
        return program;
    }
    
    // === Statement Parsing ===
    
    private ASTNode parseStatement() throws ParseException {
        if (check(TokenType.Let) || check(TokenType.Const)) {
            return parseVariableDeclaration();
        } else if (check(TokenType.Func) || check(TokenType.Function)) {
            return parseFunctionDeclaration();
        } else if (check(TokenType.Return)) {
            return parseReturnStatement();
        } else if (check(TokenType.If)) {
            return parseIfStatement();
        } else if (check(TokenType.While)) {
            return parseWhileStatement();
        } else if (check(TokenType.For)) {
            return parseForStatement();
        } else if (check(TokenType.Break)) {
            Token token = consume(TokenType.Break);
            consumeOptionalSemicolon();
            return new BreakStatement(token.line, token.column);
        } else if (check(TokenType.Continue)) {
            Token token = consume(TokenType.Continue);
            consumeOptionalSemicolon();
            return new ContinueStatement(token.line, token.column);
        } else if (check(TokenType.Try)) {
            return parseTryStatement();
        } else if (check(TokenType.OnEvent)) {
            return parseEventHandler();
        } else {
            // Expression statement
            ASTNode expr = parseExpression();
            consumeOptionalSemicolon();
            return new ExpressionStatement(expr, expr.line, expr.column);
        }
    }
    
    private void consumeOptionalSemicolon() {
        if (check(TokenType.Semicolon)) {
            position++;
        }
    }
    
    private VariableDeclaration parseVariableDeclaration() throws ParseException {
        Token keyword = current();
        boolean isConst = keyword.kind == TokenType.Const;
        position++; // consume 'let' or 'const'
        
        Token name = consume(TokenType.Identifier);
        consume(TokenType.Equals);
        ASTNode value = parseExpression();
        consumeOptionalSemicolon();
        
        return new VariableDeclaration(name.value, value, isConst, keyword.line, keyword.column);
    }
    
    private FunctionDeclaration parseFunctionDeclaration() throws ParseException {
        Token funcToken = current();
        position++; // consume 'func' or 'function'
        
        Token name = consume(TokenType.Identifier);
        consume(TokenType.OpenParen);
        
        List<String> parameters = new ArrayList<>();
        if (!check(TokenType.CloseParen)) {
            do {
                if (check(TokenType.Comma)) {
                    position++;
                }
                Token param = consume(TokenType.Identifier);
                parameters.add(param.value);
            } while (check(TokenType.Comma));
        }
        
        consume(TokenType.CloseParen);
        List<ASTNode> body = parseBlock();
        
        return new FunctionDeclaration(name.value, parameters, body, funcToken.line, funcToken.column);
    }
    
    private ReturnStatement parseReturnStatement() throws ParseException {
        Token returnToken = consume(TokenType.Return);
        
        ASTNode value = null;
        if (!check(TokenType.Semicolon) && !check(TokenType.CloseBrace)) {
            value = parseExpression();
        }
        
        consumeOptionalSemicolon();
        return new ReturnStatement(value, returnToken.line, returnToken.column);
    }
    
    private IfStatement parseIfStatement() throws ParseException {
        Token ifToken = consume(TokenType.If);
        ASTNode condition = parseExpression();
        List<ASTNode> thenBranch = parseBlock();
        
        List<ASTNode> elseBranch = null;
        if (check(TokenType.Else)) {
            position++;
            if (check(TokenType.If)) {
                // else if
                elseBranch = new ArrayList<>();
                elseBranch.add(parseIfStatement());
            } else {
                elseBranch = parseBlock();
            }
        }
        
        return new IfStatement(condition, thenBranch, elseBranch, ifToken.line, ifToken.column);
    }
    
    private WhileStatement parseWhileStatement() throws ParseException {
        Token whileToken = consume(TokenType.While);
        ASTNode condition = parseExpression();
        List<ASTNode> body = parseBlock();
        
        return new WhileStatement(condition, body, whileToken.line, whileToken.column);
    }
    
    private ForStatement parseForStatement() throws ParseException {
        Token forToken = consume(TokenType.For);
        Token variable = consume(TokenType.Identifier);
        consume(TokenType.In);
        ASTNode iterable = parseExpression();
        List<ASTNode> body = parseBlock();
        
        return new ForStatement(variable.value, iterable, body, forToken.line, forToken.column);
    }
    
    private TryStatement parseTryStatement() throws ParseException {
        Token tryToken = consume(TokenType.Try);
        List<ASTNode> tryBlock = parseBlock();
        
        consume(TokenType.Catch);
        Token errorVar = consume(TokenType.Identifier);
        List<ASTNode> catchBlock = parseBlock();
        
        return new TryStatement(tryBlock, errorVar.value, catchBlock, tryToken.line, tryToken.column);
    }
    
    private EventHandler parseEventHandler() throws ParseException {
        Token onEventToken = consume(TokenType.OnEvent);
        consume(TokenType.OpenParen);
        Token eventName = consume(TokenType.Identifier);
        consume(TokenType.CloseParen);
        List<ASTNode> body = parseBlock();
        
        return new EventHandler(eventName.value, body, onEventToken.line, onEventToken.column);
    }
    
    private List<ASTNode> parseBlock() throws ParseException {
        consume(TokenType.OpenBrace);
        List<ASTNode> statements = new ArrayList<>();
        
        while (!check(TokenType.CloseBrace) && !check(TokenType.EOF)) {
            statements.add(parseStatement());
        }
        
        consume(TokenType.CloseBrace);
        return statements;
    }
    
    // === Expression Parsing ===
    
    public ASTNode parseExpression() throws ParseException {
        return parseAssignment();
    }
    
    private ASTNode parseAssignment() throws ParseException {
        ASTNode expr = parseLogicalOr();
        
        if (check(TokenType.Equals)) {
            Token equals = current();
            position++;
            ASTNode value = parseAssignment();
            return new AssignmentExpression(expr, value, equals.line, equals.column);
        }
        
        return expr;
    }
    
    private ASTNode parseLogicalOr() throws ParseException {
        ASTNode left = parseLogicalAnd();
        
        while (check(TokenType.Or)) {
            Token op = current();
            position++;
            ASTNode right = parseLogicalAnd();
            left = new BinaryExpression(left, op.value, right, op.line, op.column);
        }
        
        return left;
    }
    
    private ASTNode parseLogicalAnd() throws ParseException {
        ASTNode left = parseEquality();
        
        while (check(TokenType.And)) {
            Token op = current();
            position++;
            ASTNode right = parseEquality();
            left = new BinaryExpression(left, op.value, right, op.line, op.column);
        }
        
        return left;
    }
    
    private ASTNode parseEquality() throws ParseException {
        ASTNode left = parseComparison();
        
        while (match(TokenType.EqualsEquals, TokenType.BangEquals)) {
            Token op = current();
            position++;
            ASTNode right = parseComparison();
            left = new BinaryExpression(left, op.value, right, op.line, op.column);
        }
        
        return left;
    }
    
    private ASTNode parseComparison() throws ParseException {
        ASTNode left = parseAdditive();
        
        while (match(TokenType.Greater, TokenType.GreaterEquals, TokenType.Less, TokenType.LessEquals)) {
            Token op = current();
            position++;
            ASTNode right = parseAdditive();
            left = new BinaryExpression(left, op.value, right, op.line, op.column);
        }
        
        return left;
    }
    
    private ASTNode parseAdditive() throws ParseException {
        ASTNode left = parseMultiplicative();
        
        while (match(TokenType.Plus, TokenType.Minus)) {
            Token op = current();
            position++;
            ASTNode right = parseMultiplicative();
            left = new BinaryExpression(left, op.value, right, op.line, op.column);
        }
        
        return left;
    }
    
    private ASTNode parseMultiplicative() throws ParseException {
        ASTNode left = parseUnary();
        
        while (match(TokenType.Star, TokenType.Slash, TokenType.Percent)) {
            Token op = current();
            position++;
            ASTNode right = parseUnary();
            left = new BinaryExpression(left, op.value, right, op.line, op.column);
        }
        
        return left;
    }
    
    private ASTNode parseUnary() throws ParseException {
        if (match(TokenType.Bang, TokenType.Minus)) {
            Token op = current();
            position++;
            ASTNode operand = parseUnary();
            return new UnaryExpression(op.value, operand, op.line, op.column);
        }
        
        return parsePostfix();
    }
    
    private ASTNode parsePostfix() throws ParseException {
        ASTNode expr = parsePrimary();
        
        while (true) {
            if (check(TokenType.Dot)) {
                Token dot = current();
                position++;
                Token property = consume(TokenType.Identifier);
                expr = new MemberExpression(expr, property.value, dot.line, dot.column);
            } else if (check(TokenType.OpenParen)) {
                Token openParen = current();
                position++;
                List<ASTNode> args = new ArrayList<>();
                
                if (!check(TokenType.CloseParen)) {
                    do {
                        if (check(TokenType.Comma)) {
                            position++;
                        }
                        args.add(parseExpression());
                    } while (check(TokenType.Comma));
                }
                
                consume(TokenType.CloseParen);
                expr = new CallExpression(expr, args, openParen.line, openParen.column);
            } else {
                break;
            }
        }
        
        return expr;
    }
    
    private ASTNode parsePrimary() throws ParseException {
        Token token = current();
        
        switch (token.kind) {
            case Number:
                position++;
                return new NumberLiteral(Double.parseDouble(token.value), token.line, token.column);
                
            case StringLiteral:
                position++;
                return new StringLiteral(token.value, token.line, token.column);
                
            case True:
                position++;
                return new BooleanLiteral(true, token.line, token.column);
                
            case False:
                position++;
                return new BooleanLiteral(false, token.line, token.column);
                
            case Null:
                position++;
                return new NullLiteral(token.line, token.column);
                
            case Identifier:
                position++;
                return new Identifier(token.value, token.line, token.column);
                
            case OpenParen:
                position++;
                ASTNode expr = parseExpression();
                consume(TokenType.CloseParen);
                return expr;
                
            case OpenBracket:
                return parseListLiteral();

            case OpenBrace:
                return parseMapLiteral();

            case New:
                return parseNewExpression();
                
            default:
                throw new ParseException("Unexpected token: " + token.kind + " at line " + token.line);
        }
    }
    
    private ListLiteral parseListLiteral() throws ParseException {
        Token openBracket = consume(TokenType.OpenBracket);
        List<ASTNode> elements = new ArrayList<>();
        
        if (!check(TokenType.CloseBracket)) {
            do {
                if (check(TokenType.Comma)) {
                    position++;
                }
                elements.add(parseExpression());
            } while (check(TokenType.Comma));
        }
        
        consume(TokenType.CloseBracket);
        return new ListLiteral(elements, openBracket.line, openBracket.column);
    }
    
    private ASTNode parseNewExpression() throws ParseException {
        Token newToken = consume(TokenType.New);
        consume(TokenType.Scope);
        consume(TokenType.OpenParen);
        
        List<ASTNode> args = new ArrayList<>();
        if (!check(TokenType.CloseParen)) {
            do {
                if (check(TokenType.Comma)) {
                    position++;
                }
                args.add(parseExpression());
            } while (check(TokenType.Comma));
        }
        
        consume(TokenType.CloseParen);
        return new ScopeCreation(args, newToken.line, newToken.column);
    }
    
    private MapLiteral parseMapLiteral() throws ParseException {
        Token openBrace = consume(TokenType.OpenBrace);
        List<MapLiteral.MapEntry> entries = new ArrayList<>();
        
        if (!check(TokenType.CloseBrace)) {
            do {
                if (check(TokenType.Comma)) {
                    position++;
                }
                
                String key;
                if (check(TokenType.Identifier)) {
                    key = current().value;
                    position++;
                } else if (check(TokenType.StringLiteral)) {
                    key = current().value;
                    position++;
                } else {
                    throw new ParseException(Quill.getPlugin(Quill.class).translate("quill.error.runtime.parser.expected", "identifier or string as map key", current().kind, current().line));
                }
                
                consume(TokenType.Colon);
                ASTNode value = parseExpression();
                entries.add(new MapLiteral.MapEntry(key, value));
                
            } while (check(TokenType.Comma));
        }
        
        consume(TokenType.CloseBrace);
        return new MapLiteral(entries, openBrace.line, openBrace.column);
    }

    // === Exception Class ===
    
    public static class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }
    }
}