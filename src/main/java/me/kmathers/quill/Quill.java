package me.kmathers.quill;

import me.kmathers.quill.commands.QuillCommands;
import me.kmathers.quill.events.QuillEventBridge;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for Quill.
 */
public class Quill extends JavaPlugin {
    private QuillScriptManager scriptManager;
    private QuillEventBridge eventBridge;
    
    @Override
    public void onEnable() {
        getLogger().info("Enabling Quill...");
        
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        
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
                
                var interpreter = scriptManager.getInterpreter(script);
                if (interpreter != null) {
                    eventBridge = new QuillEventBridge(interpreter);
                    getServer().getPluginManager().registerEvents(eventBridge, this);
                }
            } else {
                getLogger().warning("Failed to load: " + script);
            }
        }
        
        getLogger().info("Successfully loaded " + loaded + "/" + scripts.length + " script(s)");
    }
    
    /**
     * Get the script manager.
     */
    public QuillScriptManager getScriptManager() {
        return scriptManager;
    }
}