package me.kmathers.quill;

import me.kmathers.quill.commands.QuillCommands;
import me.kmathers.quill.events.QuillEventBridge;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for Quill.
 */
public class Quill extends JavaPlugin {
    private QuillScriptManager scriptManager;
    private QuillEventBridge eventBridge;

    public boolean editValid = true;

    @Override
    public void onEnable() {
        getLogger().info("Enabling Quill...");
    
        saveDefaultConfig();

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        validateConfig();

        scriptManager = new QuillScriptManager(getDataFolder(), getLogger());
        
        QuillCommands commandHandler = new QuillCommands(this, scriptManager);
        getCommand("quill").setExecutor(commandHandler);
        getCommand("quill").setTabCompleter(commandHandler);
        
        autoLoadScripts();

        getLogger().info("Quill enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Disabling Quill...");
        
        if (scriptManager != null) {
            scriptManager.unloadAll();
        }

        if (eventBridge != null) {
            // Event handlers are automatically unregistered when plugin disables
        }
        
        getLogger().info("Quill disabled successfully!");
    }
    
    /**
     * Auto-load all scripts in the scripts folder.
     */
    private void autoLoadScripts() {
        String[] scripts = scriptManager.listScripts();
        
        if (scripts.length == 0) {
            getLogger().info("No scripts found to auto-load");
            getLogger().info("Place .ql or .quill files in: " + 
                scriptManager.getScriptsDirectory().getAbsolutePath());
            return;
        }
        
        getLogger().info("Auto-loading " + scripts.length + " script(s)...");
        
        int loaded = 0;
        for (String script : scripts) {
            getLogger().info("Loading: " + script);
            if (scriptManager.loadScript(script)) {
                loaded++;
            } else {
                getLogger().warning("Failed to load: " + script);
            }
        }
        
        getLogger().info("Successfully loaded " + loaded + "/" + scripts.length + " script(s)");
        
        if (loaded > 0) {
            eventBridge = new QuillEventBridge(scriptManager, this);
            getServer().getPluginManager().registerEvents(eventBridge, this);
        }
    }
    
    /**
     * Get the script manager.
     */
    public QuillScriptManager getScriptManager() {
        return scriptManager;
    }

    /**
     * Validate the config
     */

    private void validateConfig() {
        int version = getConfig().getInt("config-version", 0);
        if (version < 1) {
            getLogger().warning("Config version is lower than current plugin version; some features may have changed.");
        } else if (version > 1) {
            getLogger().warning("User changed the config version! For safety, reverting config to stable version...");

            File configFile = new File(getDataFolder(), "config.yml");
            File backupFile = new File(getDataFolder(), "config_broken_backup.yml");

            if (configFile.exists()) {
                if (backupFile.exists()) {
                    backupFile.delete();
                }
                if (configFile.renameTo(backupFile)) {
                    getLogger().warning("Backed up user-modified config to config_broken_backup.yml");
                } else {
                    getLogger().warning("Failed to back up config; attempting to overwrite anyway.");
                }
            }

            saveResource("config.yml", true);
            reloadConfig();
            getLogger().warning("Default config has been restored. Please do not change the config version manually!");
        }

        String url = getConfig().getString("editor.url", "");
        if (!(url.startsWith("https://") || url.startsWith("http://"))) {
            getLogger().warning("Invalid editor URL: must start with http:// or https://");
            getLogger().warning("/quill edit will NOT work!");
            editValid = false;
        }
    }
}