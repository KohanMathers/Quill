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
    
    public Scope createScope(String name, UUID owner, List<Double> boundaries, SecurityMode mode, List<String> funcs, Map<String, Object> persistentVars) {
        if(boundaries.get(0) > boundaries.get(3)) {
            double temp = boundaries.get(0);
            boundaries.set(0, boundaries.get(3));
            boundaries.set(3, temp);
        }
        if(boundaries.get(1) > boundaries.get(4)) {
            double temp = boundaries.get(1);
            boundaries.set(1, boundaries.get(4));
            boundaries.set(4, temp);
        }
        if(boundaries.get(2) > boundaries.get(5)) {
            double temp = boundaries.get(2);
            boundaries.set(2, boundaries.get(5));
            boundaries.set(5, temp);
        }
        
        Scope scope = new Scope(name, owner, boundaries, mode);
        scope.setFuncs(funcs != null ? funcs : new ArrayList<>());
        scope.setPersistentVars(persistentVars != null ? persistentVars : new HashMap<>());
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
            if (funcs == null) {
                funcs = new ArrayList<>();
            }
            
            Map<String, Object> persistentVars = new HashMap<>();
            if (config.contains("persistent")) {
                var section = config.getConfigurationSection("persistent");
                if (section != null) {
                    persistentVars = section.getValues(false);
                }
            }
            
            Scope scope = new Scope(name, owner, boundaries, mode);
            scope.setFuncs(funcs);
            scope.setPersistentVars(persistentVars);
            scopes.put(scope.getName(), scope);
            
            logger.info(plugin.translate("quill.scope-manager.status.loaded-success", name));
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
            
            List<String> funcs = scope.getFuncs();
            config.set("funcs", funcs != null ? funcs : new ArrayList<>());
            
            Map<String, Object> persistentVars = scope.getPersistentVars();
            if (persistentVars == null) {
                persistentVars = new HashMap<>();
            }
            
            if (!persistentVars.isEmpty()) {
                for (Map.Entry<String, Object> entry : persistentVars.entrySet()) {
                    config.set("persistent." + entry.getKey(), entry.getValue());
                }
            } else {
                config.set("persistent", new HashMap<>());
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
        if (scopesDir.listFiles() != null) {
            for (File file : scopesDir.listFiles()) {
                if (file.getName().endsWith(".yml")) {
                    total++;
                    Scope scope = loadScope(file.getName());
                    if (scope != null) {
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
        File scopeFile = new File(scopesDir, filename + ".yml");
        
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
                    return BooleanResult.fail("already-granted");
                } else {
                    targetScope.addFunc(func);
                    saveScope(targetScope, targetScope.getName() + ".yml");
                    return BooleanResult.ok();
                }
            } else {
                if(!targetScope.hasPermission(func)) {
                    targetScope.removeFunc(func);
                    saveScope(targetScope, targetScope.getName() + ".yml");
                    return BooleanResult.ok();
                } else {
                    return BooleanResult.fail("already-granted");
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
                    return BooleanResult.fail("not-granted");
                }
            } else {
                if(targetScope.hasPermission(func)) {
                    targetScope.addFunc(func);
                    saveScope(targetScope, targetScope.getName() + ".yml");
                    return BooleanResult.ok();
                } else {
                    return BooleanResult.fail("already-revoked");
                }
            }
        } else {
            return BooleanResult.fail("scope-not-found");
        }
    }

    public List<String> getFuncs(String scope) {
        if(scopes.containsKey(scope)) {
            Scope targetScope = scopes.get(scope);
            return targetScope.getFuncs();
        } else {
            return new ArrayList<>();
        }
    }

    public List<String> getPersistentVars(String scope) {
        if(scopes.containsKey(scope)) {
            Scope targetScope = scopes.get(scope);
            Map<String, Object> vars = targetScope.getPersistentVars();
            return vars != null ? vars.keySet().stream().toList() : new ArrayList<>();
        } else {
            return new ArrayList<>();
        }
    }

    public BooleanResult addPersistentVar(String scope, String var) {
        if(scopes.containsKey(scope)) {
            Scope targetScope = scopes.get(scope);
            if(targetScope.hasPersistentVar(var)) {
                return BooleanResult.fail("already-exists");
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
                return BooleanResult.fail("does-not-exist");
            }
        } else {
            return BooleanResult.fail("scope-not-found");
        }
    }

    public Scope getScope(String scope) {
        if (scopes.containsKey(scope)) {
            return scopes.get(scope);
        } else {
            return null;
        }
    }
}