package me.kmathers.quill.commands;

import me.kmathers.quill.Quill;
import me.kmathers.quill.QuillScriptManager;
import me.kmathers.quill.utils.Editor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * Command handler for Quill plugin.
 */
public class QuillCommands implements CommandExecutor, TabCompleter {
    private final Quill plugin;
    private final QuillScriptManager scriptManager;
    
    public QuillCommands(Quill plugin, QuillScriptManager scriptManager) {
        this.plugin = plugin;
        this.scriptManager = scriptManager;
        this.editor = new Editor(plugin);
    }
    
    private Editor editor;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subcommand = args[0].toLowerCase();
        
        switch (subcommand) {
            case "load":
                return handleLoad(sender, args);
            case "reload":
                return handleReload(sender, args);
            case "unload":
                return handleUnload(sender, args);
            case "list":
                return handleList(sender);
            case "info":
                return handleInfo(sender);
            case "edit":
                createSession(sender);
                return true;
            case "help":
                sendHelp(sender);
                return true;
            default:
                sender.sendMessage(Component.text("Unknown subcommand: " + subcommand, NamedTextColor.RED));
                sender.sendMessage(Component.text("Use /quill help for available commands", NamedTextColor.YELLOW));
                return true;
        }
    }
    
    private boolean handleLoad(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /quill load <filename>", NamedTextColor.RED));
            return true;
        }
        
        if (!sender.hasPermission("quill.script.load")) {
            sender.sendMessage(Component.text("You don't have permission to load scripts", NamedTextColor.RED));
            return true;
        }
        
        String filename = args[1];
        if (!filename.endsWith(".ql") && !filename.endsWith(".quill")) {
            filename += ".ql";
        }
        
        sender.sendMessage(Component.text("Loading script: " + filename, NamedTextColor.YELLOW));
        
        if (scriptManager.loadScript(filename)) {
            sender.sendMessage(Component.text("Successfully loaded script: " + filename, NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Failed to load script. Check console for errors.", NamedTextColor.RED));
        }
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /quill reload <filename>", NamedTextColor.RED));
            return true;
        }
        
        if (!sender.hasPermission("quill.script.reload")) {
            sender.sendMessage(Component.text("You don't have permission to reload scripts", NamedTextColor.RED));
            return true;
        }
        
        String filename = args[1];
        if (!filename.endsWith(".ql") && !filename.endsWith(".quill")) {
            filename += ".ql";
        }
        
        sender.sendMessage(Component.text("Reloading script: " + filename, NamedTextColor.YELLOW));
        
        if (scriptManager.reloadScript(filename)) {
            sender.sendMessage(Component.text("Successfully reloaded script: " + filename, NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Failed to reload script. Check console for errors.", NamedTextColor.RED));
        }
        
        return true;
    }
    
    private boolean handleUnload(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /quill unload <filename>", NamedTextColor.RED));
            return true;
        }
        
        if (!sender.hasPermission("quill.script.unload")) {
            sender.sendMessage(Component.text("You don't have permission to unload scripts", NamedTextColor.RED));
            return true;
        }
        
        String filename = args[1];
        if (!filename.endsWith(".ql") && !filename.endsWith(".quill")) {
            filename += ".ql";
        }
        
        scriptManager.unloadScript(filename);
        sender.sendMessage(Component.text("Unloaded script: " + filename, NamedTextColor.GREEN));
        
        return true;
    }
    
    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("quill.script.list")) {
            sender.sendMessage(Component.text("You don't have permission to list scripts", NamedTextColor.RED));
            return true;
        }
        
        String[] scripts = scriptManager.listScripts();
        
        sender.sendMessage(Component.text("=== Available Quill Scripts ===", NamedTextColor.GOLD));
        
        if (scripts.length == 0) {
            sender.sendMessage(Component.text("No scripts found in scripts folder", NamedTextColor.YELLOW));
        } else {
            for (String script : scripts) {
                boolean loaded = scriptManager.getInterpreter(script) != null;
                Component status = loaded ? Component.text("[LOADED]", NamedTextColor.GREEN) : Component.text("[UNLOADED]", NamedTextColor.GRAY);
                sender.sendMessage(status.append(Component.text(" " + script, NamedTextColor.WHITE)));
            }
        }
        
        sender.sendMessage(Component.text("==============================", NamedTextColor.GOLD));
        
        return true;
    }
    
    private boolean handleInfo(CommandSender sender) {
        sender.sendMessage(Component.text("=== Quill Plugin Info ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Version: ", NamedTextColor.YELLOW).append(Component.text(plugin.getPluginMeta().getVersion(), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Scripts Loaded: ", NamedTextColor.YELLOW).append(Component.text(scriptManager.getAllInterpreters().size(), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Scripts Directory: ", NamedTextColor.YELLOW).append(Component.text(scriptManager.getScriptsDirectory().getAbsolutePath(), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("========================", NamedTextColor.GOLD));
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== Quill Commands ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/quill load <filename>", NamedTextColor.YELLOW).append(Component.text(" - Load a script", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/quill reload <filename>", NamedTextColor.YELLOW).append(Component.text(" - Reload a script", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/quill unload <filename>", NamedTextColor.YELLOW).append(Component.text(" - Unload a script", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/quill list", NamedTextColor.YELLOW).append(Component.text(" - List all scripts", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/quill info", NamedTextColor.YELLOW).append(Component.text(" - Show plugin info", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/quill help", NamedTextColor.YELLOW).append(Component.text(" - Show this help", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("=====================", NamedTextColor.GOLD));
    }
    
    private void createSession(CommandSender sender) {
        if (plugin.editValid) {
            editor.createSession().thenAccept(sessionId -> {
                if (sessionId != null) {
                    String url = plugin.getConfig().getString("editor.url") + sessionId;
                    sender.sendMessage(Component.text("Editor session created! ", NamedTextColor.GREEN)
                        .append(Component.text(url, NamedTextColor.GREEN, TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl(url))));
                } else {
                    plugin.getLogger().log(Level.SEVERE, "createSession() did not return a session id!");
                    sender.sendMessage(Component.text("Failed to create session, please check console for errors.", NamedTextColor.RED));
                }
            });
        } else {
            sender.sendMessage(Component.text("Cannot open editor, editor url is not valid! Ask an admin to check the config.", NamedTextColor.RED));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("load", "reload", "unload", "list", "info", "help");
            String partial = args[0].toLowerCase();
            
            for (String subcommand : subcommands) {
                if (subcommand.startsWith(partial)) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            String subcommand = args[0].toLowerCase();
            
            if (subcommand.equals("load") || subcommand.equals("reload") || subcommand.equals("unload")) {
                String[] scripts = scriptManager.listScripts();
                String partial = args[1].toLowerCase();
                
                for (String script : scripts) {
                    if (script.toLowerCase().startsWith(partial)) {
                        completions.add(script);
                    }
                }
            }
        }
        
        return completions;
    }
}