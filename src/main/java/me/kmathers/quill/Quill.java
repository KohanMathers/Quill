package me.kmathers.quill;

import me.kmathers.quill.commands.QuillCommands;
import me.kmathers.quill.events.QuillEventBridge;
import me.kmathers.quill.events.QuillInternalListeners;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for Quill.
 */
public class Quill extends JavaPlugin {
    // Version constants
    private static final int CURRENT_CONFIG_VERSION = 1;
    private static final int CURRENT_TRANSLATION_VERSION = 1;
    
    private QuillScriptManager scriptManager;
    private QuillEventBridge eventBridge;
    private FileConfiguration translations;
    private QuillScopeManager scopeManager;
    private List<UUID> flying = new ArrayList<>();

    public boolean editValid = true;

    @Override
    public void onEnable() {
        getLogger().info("Enabling Quill...");

        saveDefaultConfig();
        saveResource("translations.yml", false);

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        File translationsFile = new File(getDataFolder(), "translations.yml");
        translations = YamlConfiguration.loadConfiguration(translationsFile);

        validateConfig();

        getServer().getPluginManager().registerEvents(new QuillInternalListeners(this), this);

        scopeManager = new QuillScopeManager(this, getDataFolder(), getLogger());
        scriptManager = new QuillScriptManager(this, getDataFolder(), getLogger(), scopeManager);
        eventBridge = new QuillEventBridge(scriptManager, this);

        try {
            scopeManager.loadAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        QuillCommands commandHandler = new QuillCommands(this, scriptManager, scopeManager);
        getCommand("quill").setExecutor(commandHandler);
        getCommand("quill").setTabCompleter(commandHandler);
        
        autoLoadScripts();

        getLogger().info(translate("quill.system.state.enabled"));
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
        
        getLogger().info(translate("quill.system.state.disabled"));
    }
    
    /**
     * Auto-load all scripts in the scripts folder.
     */
    private void autoLoadScripts() {
        String[] scripts = scriptManager.listAllScripts();
        
        if (scripts.length == 0) {
            getLogger().info(translate("quill.system.autoload.no-scripts"));
            getLogger().info(translate("quill.system.autoload.no-scripts-hint", scriptManager.getScriptsDirectory().getAbsolutePath()));
            return;
        }
        
        getLogger().info(translate("quill.system.autoload.autoload", scripts.length));
        
        int loaded = 0;
        for (String script : scripts) {
            getLogger().info(translate("quill.system.autoload.loading", script));
            if (scriptManager.loadScript(script)) {
                loaded++;
            } else {
                getLogger().warning(translate("quill.system.autoload.fail", script));
            }
        }
        
        getLogger().info(translate("quill.system.autoload.success", loaded, scripts.length));
        
        if (loaded > 0) {
            getServer().getPluginManager().registerEvents(eventBridge, this);
        }
    }

    public QuillScriptManager getScriptManager() {
        return scriptManager;
    }

    private void validateConfig() {
        handleConfigVersion();
        handleTranslationVersion();
        validateEditorUrl();
    }

    private void handleConfigVersion() {
        int version = getConfig().getInt("config-version", 0);
        
        if (version == CURRENT_CONFIG_VERSION) {
            return;
        }
        
        if (version < CURRENT_CONFIG_VERSION) {
            getLogger().warning(translate("quill.system.config.lower-version"));
            getLogger().info("Migrating config from v" + version + " to v" + CURRENT_CONFIG_VERSION);
            mergeConfig();
            return;
        }
        
        getLogger().warning(translate("quill.system.config.higher-version"));
        backupAndResetConfig(version);
    }

    private void handleTranslationVersion() {
        int version = translations.getInt("translation-version", 0);
        
        if (version == CURRENT_TRANSLATION_VERSION) {
            return;
        }
        
        if (version < CURRENT_TRANSLATION_VERSION) {
            getLogger().info("Updating translations from v" + version + " to v" + CURRENT_TRANSLATION_VERSION);
            mergeTranslations();
            return;
        }
        
        getLogger().warning("Translation file is newer (v" + version + ") than plugin supports (v" + CURRENT_TRANSLATION_VERSION + "). Resetting to defaults.");
        backupAndResetTranslations(version);
    }

    private void backupAndResetConfig(int userVersion) {
        File configFile = new File(getDataFolder(), "config.yml");
        File backupFile = new File(getDataFolder(), "config_v" + userVersion + "_backup.yml");

        try {
            if (configFile.exists()) {
                if (backupFile.exists()) {
                    backupFile.delete();
                }
                java.nio.file.Files.copy(configFile.toPath(), backupFile.toPath());
                getLogger().warning(translate("quill.system.config.usermod-backup"));
            }

            saveResource("config.yml", true);
            reloadConfig();
            getLogger().warning(translate("quill.system.config.config-restore"));
        } catch (IOException e) {
            getLogger().severe("Failed to backup and reset config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void backupAndResetTranslations(int userVersion) {
        File translationsFile = new File(getDataFolder(), "translations.yml");
        File backupFile = new File(getDataFolder(), "translations_v" + userVersion + "_backup.yml");

        try {
            if (translationsFile.exists()) {
                if (backupFile.exists()) {
                    backupFile.delete();
                }
                java.nio.file.Files.copy(translationsFile.toPath(), backupFile.toPath());
                getLogger().info("Backed up translations to: " + backupFile.getName());
            }

            saveResource("translations.yml", true);
            translations = YamlConfiguration.loadConfiguration(translationsFile);
        } catch (IOException e) {
            getLogger().severe("Failed to backup and reset translations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mergeConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        
        if (!configFile.exists()) {
            saveDefaultConfig();
            return;
        }
        
        try {
            FileConfiguration userConfig = YamlConfiguration.loadConfiguration(configFile);
            
            InputStreamReader defaultConfigStream = new InputStreamReader(getResource("config.yml"));
            FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigStream);
            
            userConfig.setDefaults(defaultConfig);
            userConfig.options().copyDefaults(true);
            
            userConfig.set("config-version", CURRENT_CONFIG_VERSION);
            
            userConfig.save(configFile);
            
            reloadConfig();
            
            getLogger().info("Config successfully merged with new defaults");
            
        } catch (Exception e) {
            getLogger().severe("Failed to merge config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mergeTranslations() {
        File translationsFile = new File(getDataFolder(), "translations.yml");
        
        if (!translationsFile.exists()) {
            saveResource("translations.yml", false);
            translations = YamlConfiguration.loadConfiguration(translationsFile);
            return;
        }
        
        try {
            FileConfiguration userTranslations = YamlConfiguration.loadConfiguration(translationsFile);
            
            InputStreamReader defaultTransStream = new InputStreamReader(getResource("translations.yml"));
            FileConfiguration defaultTranslations = YamlConfiguration.loadConfiguration(defaultTransStream);
            
            userTranslations.setDefaults(defaultTranslations);
            userTranslations.options().copyDefaults(true);
            
            userTranslations.set("translation-version", CURRENT_TRANSLATION_VERSION);
            
            userTranslations.save(translationsFile);
            
            translations = userTranslations;
            
            getLogger().info("Translations successfully merged with new defaults");
            
        } catch (Exception e) {
            getLogger().severe("Failed to merge translations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void validateEditorUrl() {
        String url = getConfig().getString("editor.url", "");
        if (!(url.startsWith("https://") || url.startsWith("http://"))) {
            getLogger().warning(translate("quill.system.config.invalid-url"));
            getLogger().warning(translate("quill.system.command-unavailable", "/quill edit"));
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

    public QuillScopeManager getScopeManager() {
        return this.scopeManager;
    }

    public List<UUID> getFlying() {
        return flying;
    }

    public QuillEventBridge getEventBridge() {
        return eventBridge;
    }
}