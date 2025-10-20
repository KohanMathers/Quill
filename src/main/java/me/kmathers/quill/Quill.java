package me.kmathers.quill;

import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import me.kmathers.quill.lexer.QuillLexer;
import me.kmathers.quill.lexer.QuillLexer.Token;
import me.kmathers.quill.parser.AST.ASTNode;
import me.kmathers.quill.parser.AST.Program;
import me.kmathers.quill.parser.QuillParser;

public class Quill extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("Enabling Quill...");
        getLogger().info("===========Quill Tests===========");
        getLogger().info("              Lexer");
        getLogger().info("---------------------------------");
        testLexer();
        getLogger().info("---------------------------------");
        getLogger().info("              Parser");
        getLogger().info("---------------------------------");
        testParser();
        getLogger().info("=================================");
    }

    private void testLexer() {
        try {
            String sourceCode = "let x = 27;";
            QuillLexer lexer = new QuillLexer(sourceCode);
            List<Token> tokens = lexer.tokenize();
            for (Token token: tokens) {
                getLogger().info(token.value + "| " + token.kind.toString() + " | " + token.line + ", " + token.column);
            }
        } catch (QuillLexer.LexerException e) {
            getLogger().severe("Lexer error: " + e.getMessage());
            return;
        }
    }

    private void testParser() {
        try {
            String sourceCode = "let x = 27;";
            QuillLexer lexer = new QuillLexer(sourceCode);
            List<Token> tokens = lexer.tokenize();
            QuillParser parser = new QuillParser(tokens);
            Program ast = parser.parse();
            List<ASTNode> statements = ast.statements;
            for (ASTNode statement: statements) {
                getLogger().info(statement.line + ", " + statement.column);
            }
        } catch (QuillLexer.LexerException e) {
            getLogger().severe("Lexer error: " + e.getMessage());
            return;
        } catch (QuillParser.ParseException e) {
            getLogger().severe("Parser error: " + e.getMessage());
            return;
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling Quill...");
    }
}