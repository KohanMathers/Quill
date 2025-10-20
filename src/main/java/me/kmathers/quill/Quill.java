package me.kmathers.quill;

import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import me.kmathers.quill.lexer.QuillLexer;
import me.kmathers.quill.lexer.QuillLexer.Token;

public class Quill extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("Enabling Quill...");
        getLogger().info("========Quill Lexer Test========");
        test();
        getLogger().info("================================");
    }

    private void test() {
        try {
            String sourceCode = "let x = 27;";
            QuillLexer lexer = new QuillLexer(sourceCode);
            List<Token> tokens = lexer.tokenize();
            for (Token token: tokens) {
                getLogger().info(token.kind.toString());
            }
        } catch (QuillLexer.LexerException e) {
            getLogger().severe("Lexer error: " + e.getMessage());
            return;
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling Quill...");
    }
}