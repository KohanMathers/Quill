package me.kmathers.quill;

import me.kmathers.quill.utils.Scope;
import me.kmathers.quill.utils.Result.BooleanResult;
import me.kmathers.quill.utils.SecurityConfig.SecurityMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
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
    private Map<String, Scope> scopes;
    
    public QuillScopeManager(Quill plugin, File dataFolder, Logger logger) {
        this.plugin = plugin;
        this.scopesDir = new File(dataFolder, "scopes");
        this.logger = logger;
        this.scopes = new HashMap<>();
        
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
        scopes.put(name, scope);
        saveScope(scope, name + ".yml");
        return scope;
    }

    /**
     * Load a scope from a YAML file.
     */
    public Scope loadScope(String filename) {
        File scopeFile = new File(scopesDir, filename);
        
        if (!scopeFile.exists()) {
            logger.severe(plugin.translate("quill.scope-manager.file.file-not-found", filename));
            return null;
        }
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(scopeFile);

            String name = config.getString("name");
            String ownerStr = config.getString("owner");
            List<Double> boundaries = config.getDoubleList("boundaries");
            String modeStr = config.getString("mode", "whitelist");

            if (name == null || ownerStr == null || boundaries == null || boundaries.size() != 6) {
                logger.severe(plugin.translate("quill.scope-manager.format.invalid-format", filename));
                return null;
            }
            
            UUID owner;
            try {
                owner = UUID.fromString(ownerStr);
            } catch (IllegalArgumentException e) {
                logger.severe(plugin.translate("quill.scope-manager.format.invalid-owner-uuid", ownerStr));
                e.printStackTrace();
                return null;
            }
            
            SecurityMode mode;
            try {
                mode = SecurityMode.valueOf(modeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warning(plugin.translate("quill.scope-manager.format.invalid-mode", modeStr));
                mode = SecurityMode.WHITELIST;
            }
            
            List<String> funcs = config.getStringList("funcs");
            
            Map<String, Object> persistentVars = new HashMap<>();
            if (config.contains("persistent")) {
                var section = config.getConfigurationSection("persistent");
                if (section != null) {
                    persistentVars = section.getValues(false);
                }
            }
            
            Scope scope = createScope(name, owner, boundaries, mode, funcs, persistentVars);
            logger.info(plugin.translate("quill.scope-manager.status.loaded-success", name));
            scopes.put(scope.getName(), scope);
            return scope;
            
        } catch (Exception e) {
            logger.severe(plugin.translate("quill.scope-manager.file.read-fail", filename));
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
            logger.info(plugin.translate("quill.scope-manager.file.saved-success", scope.getName(), filename));
            return true;
            
        } catch (Exception e) {
            logger.severe(plugin.translate("quill.scope-manager.file.write-fail", filename));
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Scope> loadAll() throws Exception {
        int loaded = 0;
        int total = 0;
        Map<String, Scope> scopes = new HashMap<>();
        if (scopesDir.listFiles() != null) {
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
            logger.info(plugin.translate("quill.scope-manager.file.loaded", loaded, total));
            return scopes;
        } else {
            throw new Exception(plugin.translate("quill.scope-manager.file.read-fail"));
        }
    }

    public boolean deleteScope(String filename) {
        File scopeFile = new File(scopesDir, filename);
        
        if (!scopeFile.exists()) {
            logger.severe(plugin.translate("quill.scope-manager.file.file-not-found", filename));
            return false;
        }
        
        try {
            if (scopes.containsKey(filename)) {
                scopes.remove(filename);
            }
            return scopeFile.delete();
        } catch (Exception e) {
            logger.severe(plugin.translate("quill.scope-manager.file.delete-fail", filename));
            e.printStackTrace();
            return false;
        }
    }

    public List<String> listScopes() {
        List<String> loaded = new ArrayList<>();
        for (Map.Entry<String, Scope> scope : scopes.entrySet()) {
            loaded.add(scope.getValue().getName());
        }
        return loaded;
    }

    public Map<String, Object> scopeInfo(String scope) {
        Map<String, Object> info = new HashMap<>();
        if(scopes.containsKey(scope)) {
            Scope targetScope = scopes.get(scope);
            info.put("name", targetScope.getName());
            info.put("owner", targetScope.getOwner());
            info.put("boundaries", targetScope.getBoundaries());
            info.put("mode", targetScope.getSecurityMode());
            info.put("funcs", targetScope.getFuncs());
            info.put("persistent", targetScope.getPersistentVars());
        } else {
            info.put("name", "scope-not-found");
        }
        return info;
    }

    public BooleanResult grantFunc(String scope, String func) {
        if(scopes.containsKey(scope)) {
            Scope targetScope = scopes.get(scope);
            if(targetScope.getSecurityMode().equals(SecurityMode.WHITELIST)) {
                if(targetScope.hasPermission(func)) {
                    return BooleanResult.fail("already-inherits");
                } else {
                    targetScope.addFunc(func);
                    saveScope(targetScope, targetScope.getName() + ".yml");
                    return BooleanResult.ok();
                }
            } else {
                if(targetScope.hasPermission(func)) {
                    targetScope.removeFunc(func);
                    saveScope(targetScope, targetScope.getName() + ".yml");
                    return BooleanResult.ok();
                } else {
                    return BooleanResult.fail("does-not-inherit");
                }
            }
        } else {
            return BooleanResult.fail("scope-not-found");
        }
    }

    public BooleanResult revokeFunc(String scope, String func) {
        if(scopes.containsKey(scope)) {
            Scope targetScope = scopes.get(scope);
            if(targetScope.getSecurityMode().equals(SecurityMode.WHITELIST)) {
                if(targetScope.hasPermission(func)) {
                    targetScope.removeFunc(func);
                    saveScope(targetScope, targetScope.getName() + ".yml");
                    return BooleanResult.ok();
                } else {
                    return BooleanResult.fail("does-not-inherit");
                }
            } else {
                if(targetScope.hasPermission(func)) {
                    return BooleanResult.fail("already-inherits");
                } else {
                    targetScope.addFunc(func);
                    saveScope(targetScope, targetScope.getName() + ".yml");
                    return BooleanResult.ok();
                }
            }
        } else {
            return BooleanResult.fail("scope-not-found");
        }
    }

    public BooleanResult addPersistentVar(String scope, String var) {
        if(scopes.containsKey(scope)) {
            Scope targetScope = scopes.get(scope);
                if(targetScope.hasPersistentVar(var)) {
                    return BooleanResult.fail("already-inherits");
                } else {
                    targetScope.addPersistentVar(var);
                    saveScope(targetScope, targetScope.getName() + ".yml");
                    return BooleanResult.ok();
                }
            } else {
            return BooleanResult.fail("scope-not-found");
        }
    }

    public BooleanResult removePersistentVar(String scope, String var) {
        if(scopes.containsKey(scope)) {
            Scope targetScope = scopes.get(scope);
                if(targetScope.hasPersistentVar(var)) {
                    targetScope.removePersistentVar(var);
                    saveScope(targetScope, targetScope.getName() + ".yml");
                    return BooleanResult.ok();
                } else {
                    return BooleanResult.fail("does-not-inherit");
                }
            } else {
            return BooleanResult.fail("scope-not-found");
        }
    }
}