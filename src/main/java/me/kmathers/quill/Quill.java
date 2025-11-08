package me.kmathers.quill;

import me.kmathers.quill.commands.QuillCommands;
import me.kmathers.quill.events.QuillEventBridge;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for Quill.
 */
public class Quill extends JavaPlugin {
    private QuillScriptManager scriptManager;
    private QuillEventBridge eventBridge;
    private FileConfiguration translations;

    public boolean editValid = true;

    @Override
    public void onEnable() {
        getLogger().info("Enabling Quill...");
    
        saveDefaultConfig();
        saveResource("translations.yml", false);

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        validateConfig();

        File translationsFile = new File(getDataFolder(), "translations.yml");
        translations = YamlConfiguration.loadConfiguration(translationsFile);

        scriptManager = new QuillScriptManager(this, getDataFolder(), getLogger());
        
        QuillCommands commandHandler = new QuillCommands(this, scriptManager);
        getCommand("quill").setExecutor(commandHandler);
        getCommand("quill").setTabCompleter(commandHandler);
        
        autoLoadScripts();

        getLogger().info(translate("system.enabled"));
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
        
        getLogger().info(translate("system.disabled"));
    }
    
    /**
     * Auto-load all scripts in the scripts folder.
     */
    private void autoLoadScripts() {
        String[] scripts = scriptManager.listScripts();
        
        if (scripts.length == 0) {
            getLogger().info(translate("autoload.no-scripts"));
            getLogger().info(translate("autoload.no-scripts-hint", scriptManager.getScriptsDirectory().getAbsolutePath()));
            return;
        }
        
        getLogger().info(translate("system.autoload.autoload", scripts.length));
        
        int loaded = 0;
        for (String script : scripts) {
            getLogger().info(translate("system.autoload.loading", script));
            if (scriptManager.loadScript(script)) {
                loaded++;
            } else {
                getLogger().warning(translate("system.autoload.fail", script));
            }
        }
        
        getLogger().info(translate("system.autoload.success", loaded, scripts.length));
        
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
            getLogger().warning(translate("system.config.lower-version"));
        } else if (version > 1) {
            getLogger().warning(translate("system.config.higher-version"));

            File configFile = new File(getDataFolder(), "config.yml");
            File backupFile = new File(getDataFolder(), "config_broken_backup.yml");

            if (configFile.exists()) {
                if (backupFile.exists()) {
                    backupFile.delete();
                }
                if (configFile.renameTo(backupFile)) {
                    getLogger().warning(translate("system.config.usermod-backup"));
                } else {
                    getLogger().warning(translate("system.config.usermod-fail"));
                }
            }

            saveResource("config.yml", true);
            reloadConfig();
            getLogger().warning(translate("system.config.config-restore"));
        }

        String url = getConfig().getString("editor.url", "");
        if (!(url.startsWith("https://") || url.startsWith("http://"))) {
            getLogger().warning(translate("system.config.invalid-url"));
            getLogger().warning(translate("system.command-unavailable", "/quill edit"));
            editValid = false;
        }
    }

    public String translate(String path, Object... args) {
        String msg = translations.getString(path);
        if (msg == null) return path;

        for (int i = 0; i < args.length; i++) {
            msg = msg.replace("{" + i + "}", args[i].toString());
        }
        return msg;
    }
}