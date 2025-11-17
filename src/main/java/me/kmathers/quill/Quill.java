package me.kmathers.quill;

import me.kmathers.quill.commands.QuillCommands;
import me.kmathers.quill.events.QuillEventBridge;
import me.kmathers.quill.events.QuillInternalListeners;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
            getLogger().warning(translate("quill.system.config.lower-version"));
            mergeConfig();
        } else if (version > 1) {
            getLogger().warning(translate("quill.system.config.higher-version"));

            File configFile = new File(getDataFolder(), "config.yml");
            File backupFile = new File(getDataFolder(), "config_broken_backup.yml");

            if (configFile.exists()) {
                if (backupFile.exists()) {
                    backupFile.delete();
                }
                if (configFile.renameTo(backupFile)) {
                    getLogger().warning(translate("quill.system.config.usermod-backup"));
                } else {
                    getLogger().warning(translate("quill.system.config.usermod-fail"));
                }
            }

            saveResource("config.yml", true);
            reloadConfig();
            getLogger().warning(translate("quill.system.config.config-restore"));
        }

        int transVersion = getConfig().getInt("translation-version", 0);
        if (transVersion < 1) {
            mergeTranslations();
        }

        String url = getConfig().getString("editor.url", "");
        if (!(url.startsWith("https://") || url.startsWith("http://"))) {
            getLogger().warning(translate("quill.system.config.invalid-url"));
            getLogger().warning(translate("quill.system.command-unavailable", "/quill edit"));
            editValid = false;
        }
    }

    /**
     * Merge user translations with new default translations.
     */
    private void mergeTranslations() {
        File translationsFile = new File(getDataFolder(), "translations.yml");
        File oldTranslationsFile = new File(getDataFolder(), "translations_old.yml");
        
        if (!translationsFile.exists()) {
            saveResource("translations.yml", false);
            return;
        }
        
        try {
            if (oldTranslationsFile.exists()) {
                oldTranslationsFile.delete();
            }
            
            if (!translationsFile.renameTo(oldTranslationsFile)) {
                getLogger().warning(translate("quill.system.translations-rename-fail"));
                return;
            }
            
            saveResource("translations.yml", true);
            
            FileConfiguration oldTranslations = YamlConfiguration.loadConfiguration(oldTranslationsFile);
            FileConfiguration newTranslations = YamlConfiguration.loadConfiguration(translationsFile);
            
            mergeConfigurationRecursive(oldTranslations, newTranslations, "");
            
            newTranslations.save(translationsFile);
            
            if (oldTranslationsFile.exists()) {
                oldTranslationsFile.delete();
            }
        } catch (Exception e) {
            getLogger().warning("Failed to merge translations: " + e.getMessage());
            e.printStackTrace();
            
            if (oldTranslationsFile.exists() && !translationsFile.exists()) {
                oldTranslationsFile.renameTo(translationsFile);
            }
        }
    }

    /**
     * Merge user config with new default config.
     */
    private void mergeConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        File oldConfigFile = new File(getDataFolder(), "config_old.yml");
        File tempConfigFile = new File(getDataFolder(), "config_new.yml");
        
        if (!configFile.exists()) {
            saveDefaultConfig();
            return;
        }
        
        try {
            if (oldConfigFile.exists()) {
                oldConfigFile.delete();
            }
            
            if (!configFile.renameTo(oldConfigFile)) {
                getLogger().warning(translate("quill.system.config-rename-fail"));
                return;
            }
            
            saveResource("config.yml", true);
            if (!configFile.renameTo(tempConfigFile)) {
                getLogger().warning(translate("quill.system.config-temp-fail"));
                oldConfigFile.renameTo(configFile);
                return;
            }
            
            FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldConfigFile);
            
            List<String> newConfigLines = readFileLines(tempConfigFile);
            
            List<String> mergedLines = mergeConfigLines(newConfigLines, oldConfig);
            
            writeFileLines(configFile, mergedLines);
            
            if (oldConfigFile.exists()) {
                oldConfigFile.delete();
            }
            if (tempConfigFile.exists()) {
                tempConfigFile.delete();
            }            
        } catch (Exception e) {
            getLogger().warning("Failed to merge config: " + e.getMessage());
            e.printStackTrace();
            
            if (oldConfigFile.exists() && !configFile.exists()) {
                oldConfigFile.renameTo(configFile);
            }
            if (tempConfigFile.exists()) {
                tempConfigFile.delete();
            }
        }
    }

    /**
     * Read all lines from a file.
     */
    private List<String> readFileLines(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * Write lines to a file.
     */
    private void writeFileLines(File file, List<String> lines) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i < lines.size(); i++) {
                writer.write(lines.get(i));
                if (i < lines.size() - 1) {
                    writer.newLine();
                }
            }
        }
    }

    /**
     * Merge config lines, replacing values from old config while preserving structure and comments.
     */
    private List<String> mergeConfigLines(List<String> newLines, FileConfiguration oldConfig) {
        List<String> result = new ArrayList<>();
        List<String> currentPath = new ArrayList<>();
        
        for (String line : newLines) {
            String trimmed = line.trim();
            
            if (trimmed.startsWith("#") || trimmed.isEmpty()) {
                result.add(line);
                continue;
            }
            
            String[] parts = line.split(":", 2);
            if (parts.length < 1) {
                result.add(line);
                continue;
            }
            
            String keyPart = parts[0];
            int indent = keyPart.length() - keyPart.trim().length();
            String key = keyPart.trim();
            
            int pathLevel = indent / 2;
            while (currentPath.size() > pathLevel) {
                currentPath.remove(currentPath.size() - 1);
            }
            
            String fullPath = buildPath(currentPath, key);
            
            if (parts.length == 2) {
                String value = parts[1].trim();
                
                if (!value.isEmpty()) {
                    if (!fullPath.equals("config-version") && !fullPath.equals("translation-version") && oldConfig.contains(fullPath)) {
                        Object oldValue = oldConfig.get(fullPath);
                        
                        if (!oldConfig.isConfigurationSection(fullPath)) {
                            String newLine = keyPart + ": " + formatConfigValue(oldValue);
                            result.add(newLine);
                            continue;
                        }
                    }
                }
                
                if (value.isEmpty()) {
                    currentPath.add(key);
                }
            } else {
                currentPath.add(key);
            }
            
            result.add(line);
        }
        
        return result;
    }

    /**
     * Build a dot-notation path from path components.
     */
    private String buildPath(List<String> pathComponents, String key) {
        StringBuilder sb = new StringBuilder();
        for (String component : pathComponents) {
            if (sb.length() > 0) {
                sb.append(".");
            }
            sb.append(component);
        }
        if (sb.length() > 0) {
            sb.append(".");
        }
        sb.append(key);
        return sb.toString();
    }

    /**
     * Format a config value for YAML output.
     */
    private String formatConfigValue(Object value) {
        if (value == null) {
            return "null";
        }
        
        if (value instanceof String) {
            String str = (String) value;
            if (str.contains(":") || str.contains("#") || str.contains("'") || 
                str.contains("\"") || str.startsWith(" ") || str.endsWith(" ")) {
                return "\"" + str.replace("\"", "\\\"") + "\"";
            }
            return str;
        }
        
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) {
                return "[]";
            }
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(formatConfigValue(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        
        return value.toString();
    }

    private void mergeConfigurationRecursive(FileConfiguration oldConfig, FileConfiguration newConfig, String path) {
        for (String key : newConfig.getKeys(false)) {
            String currentPath = path.isEmpty() ? key : path + "." + key;
            
            if (oldConfig.contains(currentPath)) {
                Object oldValue = oldConfig.get(currentPath);
                @SuppressWarnings("unused")
                Object newValue = newConfig.get(currentPath);
                
                if (oldConfig.isConfigurationSection(currentPath) && 
                    newConfig.isConfigurationSection(currentPath)) {
                    mergeConfigurationRecursive(oldConfig, newConfig, currentPath);
                } 
                else if (oldValue instanceof String) {
                    newConfig.set(currentPath, oldValue);
                }
            }
            else if (newConfig.isConfigurationSection(currentPath)) {
                mergeConfigurationRecursive(oldConfig, newConfig, currentPath);
            }
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