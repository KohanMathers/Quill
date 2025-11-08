package me.kmathers.quill;

import me.kmathers.quill.interpreter.QuillInterpreter;
import me.kmathers.quill.interpreter.ScopeContext;
import me.kmathers.quill.lexer.QuillLexer;
import me.kmathers.quill.parser.AST.Program;
import me.kmathers.quill.utils.Scope;
import me.kmathers.quill.utils.SecurityConfig.SecurityMode;
import me.kmathers.quill.parser.QuillParser;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Manages loading and savinh of Quill scopes.
 */
public class QuillScopeManager {
    private Quill plugin;
    private final File scopesDir;
    private final Logger logger;
    
    public QuillScopeManager(Quill plugin, File dataFolder, Logger logger) {
        this.plugin = plugin;
        this.scopesDir = new File(dataFolder, "scopes");
        this.logger = logger;
        
        if (!scopesDir.exists()) {
            scopesDir.mkdirs();
        }
    }
    
    /**
     * Creates a new scope in memory
     */
    public Scope createScope(String name, UUID owner, List<Double> boundaries, SecurityMode mode, List<String> funcs, Map<String, Object> persistentVars) {
        Scope scope = new Scope(name, owner, boundaries, mode);
        scope.setFuncs(funcs);
        scope.setPersistentVars(persistentVars);
        return scope;
    }

    /**
     * Load a scope from a file.
     */
    public boolean loadScope(String filename) {
        File scopeFile = new File(scopesDir, filename);
        
        if (!scopeFile.exists()) {
            logger.severe(plugin.translate("scope-manager.file-not-found", filename));
            return false;
        }
        
        try {
            String sourceCode = Files.readString(scopeFile.toPath());
            return true;
        } catch (IOException e) {
            logger.severe(plugin.translate("scope-manager.read-fail", filename));
            e.printStackTrace();
            return false;
        }
    }
}