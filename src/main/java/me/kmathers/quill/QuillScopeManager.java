package me.kmathers.quill;

import me.kmathers.quill.utils.Scope;
import me.kmathers.quill.utils.SecurityConfig.SecurityMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Manages loading and saving of Quill scopes.
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
     * Load a scope from a YAML file.
     */
    public Scope loadScope(String filename) {
        File scopeFile = new File(scopesDir, filename);
        
        if (!scopeFile.exists()) {
            logger.severe(plugin.translate("scope-manager.file-not-found", filename));
            return null;
        }
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(scopeFile);

            String name = config.getString("name");
            String ownerStr = config.getString("owner");
            List<Double> boundaries = config.getDoubleList("boundaries");
            String modeStr = config.getString("mode", "whitelist");

            if (name == null || ownerStr == null || boundaries == null || boundaries.size() != 6) {
                logger.severe(plugin.translate("scope-manager.invalid-format", filename));
                return null;
            }
            
            UUID owner = UUID.fromString(ownerStr);
            
            SecurityMode mode;
            try {
                mode = SecurityMode.valueOf(modeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warning(plugin.translate("scope-manager.invalid-mode", modeStr));
                mode = SecurityMode.WHITELIST;
            }
            
            List<String> funcs = config.getStringList("funcs");
            
            Map<String, Object> persistentVars = new HashMap<>();
            if (config.contains("persistent")) {
                persistentVars = config.getConfigurationSection("persistent").getValues(false);
            }
            
            Scope scope = createScope(name, owner, boundaries, mode, funcs, persistentVars);
            logger.info(plugin.translate("scope-manager.loaded-success", name, filename));
            return scope;
            
        } catch (IllegalArgumentException e) {
            logger.severe(plugin.translate("scope-manager.invalid-uuid", filename));
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            logger.severe(plugin.translate("scope-manager.read-fail", filename));
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Save a scope to a YAML file.
     */
    public boolean saveScope(Scope scope, String filename) {
        File scopeFile = new File(scopesDir, filename);
        
        try {
            FileConfiguration config = new YamlConfiguration();
            
            config.set("name", scope.getName());
            config.set("owner", scope.getOwner().toString());
            config.set("boundaries", scope.getBoundaries());
            config.set("mode", scope.getSecurityMode().toString().toLowerCase());
            config.set("funcs", scope.getFuncs());
            
            Map<String, Object> persistentVars = scope.getPersistentVars();
            if (persistentVars != null && !persistentVars.isEmpty()) {
                for (Map.Entry<String, Object> entry : persistentVars.entrySet()) {
                    config.set("persistent." + entry.getKey(), entry.getValue());
                }
            }
            
            config.save(scopeFile);
            logger.info(plugin.translate("scope-manager.saved-success", scope.getName(), filename));
            return true;
            
        } catch (Exception e) {
            logger.severe(plugin.translate("scope-manager.write-fail", filename));
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Scope> loadAll() {
        int loaded = 0;
        int total = 0;
        Map<String, Scope> scopes = new HashMap<>();
        for (File file : scopesDir.listFiles()) {
            if (file.getName().endsWith(".yml")) {
                total++;
                Scope scope = loadScope(file.getName());
                if (scope != null) {
                    scopes.put(scope.getName(), scope);
                    loaded++;
                }
            }
        }
        logger.info(plugin.translate("scope-manager.loaded", loaded, total));
        return scopes;
    }
}