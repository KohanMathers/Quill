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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private final QuillScopeManager scopeManager;

    public QuillScriptManager(Quill plugin, File dataFolder, Logger logger, QuillScopeManager scopeManager) {
        this.plugin = plugin;
        this.scriptsDir = new File(dataFolder, "scripts");
        this.logger = logger;
        this.activeScripts = new HashMap<>();
        this.scopeManager = scopeManager;
        
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

            String scope = "global";
            File parent = scriptFile.getParentFile();
            if (!parent.equals(scriptsDir)) {
                scope = parent.getName();
            }

            return executeScript(filename, sourceCode, scope);
        } catch (IOException e) {
            logger.severe(plugin.translate("quill.script-manager.file.read-fail", filename));
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Execute a script from source code.
     */
    public boolean executeScript(String name, String sourceCode, String scopeName) {
        try {
            QuillLexer lexer = new QuillLexer(sourceCode);
            var tokens = lexer.tokenize();
            
            logger.info(plugin.translate("quill.script-manager.status.tokenized-count", name, tokens.size()));
            
            QuillParser parser = new QuillParser(tokens);
            Program ast = parser.parse();
            
            logger.info(plugin.translate("quill.script-manager.status.parsed-count", name, ast.statements.size()));
            
            QuillInterpreter interpreter;

            if(scopeName.equals("global")) {
                ScopeContext.Region defaultRegion = new ScopeContext.Region(
                    -1000000, -64, -1000000,
                    1000000, 320, 1000000,
                    Bukkit.getWorlds().get(0).getName()
                );
                ScopeContext globalScope = new ScopeContext("global", defaultRegion);
                
                interpreter = new QuillInterpreter(globalScope, scopeManager);
                interpreter.execute(ast);
            } else {
                List<Double> boundaries = (scopeManager.getScope(scopeName) != null ? scopeManager.getScope(scopeName).getBoundaries() : null);
                if (boundaries != null) {
                    ScopeContext.Region targetRegion = new ScopeContext.Region(boundaries.get(0), boundaries.get(1), boundaries.get(2), boundaries.get(3), boundaries.get(4), boundaries.get(5), Bukkit.getWorlds().get(0).getName());
                    ScopeContext targetScope = new ScopeContext(scopeName, targetRegion);
                    interpreter = new QuillInterpreter(targetScope, scopeManager);
                    interpreter.execute(ast);
                } else {
                    logger.warning("quill.script-manager.file.invalid-boundaries");
                    return false;
                }
            }
            
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
    public String[] listAllScripts() {
        List<String> results = new ArrayList<>();
        collectScriptsRecursive(scriptsDir, results, "");
        return results.toArray(new String[0]);
    }

    private void collectScriptsRecursive(File dir, List<String> results, String relativePath) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                collectScriptsRecursive(f, results,
                    relativePath.isEmpty() ? f.getName() : relativePath + "/" + f.getName()
                );
            } else if (f.getName().endsWith(".ql") || f.getName().endsWith(".quill")) {
                String path = relativePath.isEmpty()
                    ? f.getName()
                    : relativePath + "/" + f.getName();
                results.add(path);
            }
        }
    }
}