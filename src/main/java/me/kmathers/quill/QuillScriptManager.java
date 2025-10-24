package me.kmathers.quill;

import me.kmathers.quill.interpreter.QuillInterpreter;
import me.kmathers.quill.interpreter.ScopeContext;
import me.kmathers.quill.lexer.QuillLexer;
import me.kmathers.quill.parser.AST.Program;
import me.kmathers.quill.parser.QuillParser;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manages loading and execution of Quill scripts.
 */
public class QuillScriptManager {
    private final File scriptsDir;
    private final Logger logger;
    private final Map<String, QuillInterpreter> activeScripts;
    
    public QuillScriptManager(File dataFolder, Logger logger) {
        this.scriptsDir = new File(dataFolder, "scripts");
        this.logger = logger;
        this.activeScripts = new HashMap<>();
        
        if (!scriptsDir.exists()) {
            scriptsDir.mkdirs();
        }
    }
    
    /**
     * Load and execute a script from a file.
     */
    public boolean loadScript(String filename) {
        File scriptFile = new File(scriptsDir, filename);
        
        if (!scriptFile.exists()) {
            logger.severe("Script file not found: " + filename);
            return false;
        }
        
        try {
            String sourceCode = Files.readString(scriptFile.toPath());
            return executeScript(filename, sourceCode);
        } catch (IOException e) {
            logger.severe("Failed to read script file: " + filename);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Execute a script from source code.
     */
    public boolean executeScript(String name, String sourceCode) {
        try {
            QuillLexer lexer = new QuillLexer(sourceCode);
            var tokens = lexer.tokenize();
            
            logger.info("Tokenized " + name + " (" + tokens.size() + " tokens)");
            
            QuillParser parser = new QuillParser(tokens);
            Program ast = parser.parse();
            
            logger.info("Parsed " + name + " (" + ast.statements.size() + " statements)");
            
            ScopeContext.Region defaultRegion = new ScopeContext.Region(
                -1000000, -64, -1000000,
                1000000, 320, 1000000,
                Bukkit.getWorlds().get(0).getName()
            );
            ScopeContext globalScope = new ScopeContext("global", defaultRegion);
            
            QuillInterpreter interpreter = new QuillInterpreter(globalScope);
            interpreter.execute(ast);
            
            activeScripts.put(name, interpreter);
            
            logger.info("Successfully executed script: " + name);
            return true;
            
        } catch (QuillLexer.LexerException e) {
            logger.severe("Lexer error in " + name + ": " + e.getMessage());
            return false;
        } catch (QuillParser.ParseException e) {
            logger.severe("Parser error in " + name + ": " + e.getMessage());
            return false;
        } catch (Exception e) {
            logger.severe("Runtime error in " + name + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Reload a script.
     */
    public boolean reloadScript(String filename) {
        unloadScript(filename);
        return loadScript(filename);
    }
    
    /**
     * Unload a script.
     */
    public void unloadScript(String name) {
        activeScripts.remove(name);
        logger.info("Unloaded script: " + name);
    }
    
    /**
     * Get an active interpreter by name.
     */
    public QuillInterpreter getInterpreter(String name) {
        return activeScripts.get(name);
    }
    
    /**
     * Get all active interpreters.
     */
    public Map<String, QuillInterpreter> getAllInterpreters() {
        return new HashMap<>(activeScripts);
    }
    
    /**
     * Unload all scripts.
     */
    public void unloadAll() {
        activeScripts.clear();
        logger.info("Unloaded all scripts");
    }
    
    /**
     * Get the scripts directory.
     */
    public File getScriptsDirectory() {
        return scriptsDir;
    }
    
    /**
     * List all available script files.
     */
    public String[] listScripts() {
        File[] files = scriptsDir.listFiles((dir, name) -> name.endsWith(".ql") || name.endsWith(".quill"));
        if (files == null) {
            return new String[0];
        }
        
        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            names[i] = files[i].getName();
        }
        return names;
    }
}