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
    private Quill plugin;
    private final File scriptsDir;
    private final Logger logger;
    private final Map<String, QuillInterpreter> activeScripts;
    
    public QuillScriptManager(Quill plugin, File dataFolder, Logger logger) {
        this.plugin = plugin;
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
            logger.severe(plugin.translate("quill.script-manager.file.file-not-found", filename));
            return false;
        }
        
        try {
            String sourceCode = Files.readString(scriptFile.toPath());
            return executeScript(filename, sourceCode);
        } catch (IOException e) {
            logger.severe(plugin.translate("quill.script-manager.file.read-fail", filename));
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
            
            logger.info(plugin.translate("quill.script-manager.status.tokenized-count", name, tokens.size()));
            
            QuillParser parser = new QuillParser(tokens);
            Program ast = parser.parse();
            
            logger.info(plugin.translate("quill.script-manager.status.parsed-count", name, ast.statements.size()));
            
            ScopeContext.Region defaultRegion = new ScopeContext.Region(
                -1000000, -64, -1000000,
                1000000, 320, 1000000,
                Bukkit.getWorlds().get(0).getName()
            );
            ScopeContext globalScope = new ScopeContext("global", defaultRegion);
            
            QuillInterpreter interpreter = new QuillInterpreter(globalScope);
            interpreter.execute(ast);
            
            activeScripts.put(name, interpreter);
            
            logger.info(plugin.translate("quill.script-manager.status.execute-success", name));
            return true;
            
        } catch (QuillLexer.LexerException e) {
            logger.severe(plugin.translate("quill.script-manager.error.lexer-error", name, e.getMessage()));
            return false;
        } catch (QuillParser.ParseException e) {
            logger.severe(plugin.translate("quill.script-manager.error.parser-error", name, e.getMessage()));
            return false;
        } catch (Exception e) {
            logger.severe(plugin.translate("quill.script-manager.error.runtime-error", name, e.getMessage()));
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
        logger.info(plugin.translate("quill.script-manager.status.unloaded", name));
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
        logger.info(plugin.translate("quill.script-manager.status.unloaded-all"));
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