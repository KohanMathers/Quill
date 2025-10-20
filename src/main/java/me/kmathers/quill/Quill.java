package me.kmathers.quill;

import org.bukkit.plugin.java.JavaPlugin;

public class Quill extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("Enabling Quill...");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling Quill...");
    }
}