package me.kmathers.quill.commands;

import me.kmathers.quill.Quill;
import me.kmathers.quill.QuillScopeManager;
import me.kmathers.quill.QuillScriptManager;
import me.kmathers.quill.utils.Editor;
import me.kmathers.quill.utils.SecurityConfig.SecurityMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Command handler for Quill plugin.
 */
public class QuillCommands implements CommandExecutor, TabCompleter {
    private final Quill plugin;
    private final QuillScriptManager scriptManager;
    private final QuillScopeManager scopeManager;
    
    public QuillCommands(Quill plugin, QuillScriptManager scriptManager, QuillScopeManager scopeManager) {
        this.plugin = plugin;
        this.scriptManager = scriptManager;
        this.scopeManager = scopeManager;
        this.editor = new Editor(plugin);
    }
    
    private Editor editor;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender, null);
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
                createSession(sender, args);
                return true;
            case "help":
                sendHelp(sender, args);
                return true;
            default:
                sender.sendMessage(Component.text(plugin.translate("quill.commands.global.unknown"), NamedTextColor.RED));
                sender.sendMessage(Component.text(plugin.translate("quill.commands.global.unknown-hint"), NamedTextColor.YELLOW));
                return true;
        }
    }
    
    private boolean handleLoad(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text(plugin.translate("quill.commands.usage.filename", "load"), NamedTextColor.RED));
            return true;
        }
        
        if (!sender.hasPermission("quill.script.load")) {
            sender.sendMessage(Component.text(plugin.translate("quill.commands.global.no-permission", "load scripts"), NamedTextColor.RED));
            return true;
        }
        
        String filename = args[1];
        if (!filename.endsWith(".ql") && !filename.endsWith(".quill")) {
            filename += ".ql";
        }
        
        sender.sendMessage(Component.text("Loading script: " + filename, NamedTextColor.YELLOW));
        
        if (scriptManager.loadScript(filename)) {
            sender.sendMessage(Component.text(plugin.translate("quill.commands.global.success", "loaded script: " + filename), NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text(plugin.translate("quill.commands.global.fail", "load script"), NamedTextColor.RED));
        }
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text(plugin.translate("quill.commands.usage.filename", "reload"), NamedTextColor.RED));
            return true;
        }
        
        if (!sender.hasPermission("quill.script.reload")) {
            sender.sendMessage(Component.text(plugin.translate("quill.commands.global.no-permission", "reload scripts"), NamedTextColor.RED));
            return true;
        }
        
        String filename = args[1];
        if (!filename.endsWith(".ql") && !filename.endsWith(".quill")) {
            filename += ".ql";
        }
        
        sender.sendMessage(Component.text("Reloading script: " + filename, NamedTextColor.YELLOW));
        
        if (scriptManager.reloadScript(filename)) {
            sender.sendMessage(Component.text(plugin.translate("quill.commands.global.success", "reloaded script: " + filename), NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text(plugin.translate("quill.commands.global.fail", "reload script"), NamedTextColor.RED));
        }
        
        return true;
    }
    
    private boolean handleUnload(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text(plugin.translate("quill.commands.usage.filename", "unload"), NamedTextColor.RED));
            return true;
        }
        
        if (!sender.hasPermission("quill.script.unload")) {
            sender.sendMessage(Component.text(plugin.translate("quill.commands.global.no-permission", "unload scripts"), NamedTextColor.RED));
            return true;
        }
        
        String filename = args[1];
        if (!filename.endsWith(".ql") && !filename.endsWith(".quill")) {
            filename += ".ql";
        }
        
        scriptManager.unloadScript(filename);
        sender.sendMessage(Component.text(plugin.translate("quill.commands.global.success", "unloaded script: " + filename), NamedTextColor.GREEN));
        
        return true;
    }
    
    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("quill.script.list")) {
            sender.sendMessage(Component.text(plugin.translate("quill.commands.global.no-permission", "list scripts"), NamedTextColor.RED));
            return true;
        }
        
        String[] scripts = scriptManager.listScripts();
        
        sender.sendMessage(Component.text("=== " + plugin.translate("quill.commands.scripts.available") + " ===", NamedTextColor.GOLD));
        
        if (scripts.length == 0) {
            sender.sendMessage(Component.text(plugin.translate("quill.commands.scripts.none"), NamedTextColor.YELLOW));
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
        sender.sendMessage(Component.text("=== " + plugin.translate("quill.commands.info.title") + " ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text(plugin.translate("quill.commands.info.version") + ": ", NamedTextColor.YELLOW).append(Component.text(plugin.getPluginMeta().getVersion(), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text(plugin.translate("quill.commands.info.loaded") + ": ", NamedTextColor.YELLOW).append(Component.text(scriptManager.getAllInterpreters().size(), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text(plugin.translate("quill.commands.info.directory") + ": ", NamedTextColor.YELLOW).append(Component.text(scriptManager.getScriptsDirectory().getAbsolutePath(), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("========================", NamedTextColor.GOLD));
        
        return true;
    }
    
    private void sendHelp(CommandSender sender, String[] args) {
        if (args == null || args.length == 0) {
            sender.sendMessage(Component.text("=== " + plugin.translate("quill.commands.help.title") + " ===", NamedTextColor.GOLD));
            sender.sendMessage(Component.text("/quill load <filename>", NamedTextColor.YELLOW).append(Component.text(" - " + plugin.translate("quill.commands.help.load"), NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("/quill reload <filename>", NamedTextColor.YELLOW).append(Component.text(" - " + plugin.translate("quill.commands.help.reload"), NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("/quill unload <filename>", NamedTextColor.YELLOW).append(Component.text(" - " + plugin.translate("quill.commands.help.unload"), NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("/quill edit <filename>", NamedTextColor.YELLOW).append(Component.text(" - " + plugin.translate("quill.commands.help.edit"), NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("/quill list", NamedTextColor.YELLOW).append(Component.text(" - " + plugin.translate("quill.commands.help.list"), NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("/quill info", NamedTextColor.YELLOW).append(Component.text(" - " + plugin.translate("quill.commands.help.info"), NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("/quill scope <subcommand> [args]", NamedTextColor.YELLOW).append(Component.text(" - Manage scopes (use /quill help scope)", NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("/quill help", NamedTextColor.YELLOW).append(Component.text(" - " + plugin.translate("quill.commands.help.help"), NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("=====================", NamedTextColor.GOLD));
        } else if (args.length >= 1) {
            switch (args[0]) {
                case "scope":
                    sender.sendMessage(Component.text("=== " + plugin.translate("quill.commands.scope.title") + " ===", NamedTextColor.GOLD));
                    sender.sendMessage(Component.text("/quill scope create <name> <owner> <x1> <y1> <z1> <x2> <y2> <z2> <whitelist|blacklist>", NamedTextColor.YELLOW).append(Component.text(" - " + plugin.translate("quill.commands.scope.create"), NamedTextColor.WHITE)));
                    sender.sendMessage(Component.text("/quill scope delete <name>", NamedTextColor.YELLOW).append(Component.text(" - " + plugin.translate("quill.commands.scope.delete"), NamedTextColor.WHITE)));
                    sender.sendMessage(Component.text("/quill scope list", NamedTextColor.YELLOW).append(Component.text(" - " + plugin.translate("quill.commands.scope.list"), NamedTextColor.WHITE)));
                    sender.sendMessage(Component.text("/quill scope info <name>", NamedTextColor.YELLOW).append(Component.text(" - " + plugin.translate("quill.commands.scope.info"), NamedTextColor.WHITE)));
                    sender.sendMessage(Component.text("/quill scope permission <name> <function> <grant|revoke>", NamedTextColor.YELLOW).append(Component.text(" - " + plugin.translate("quill.commands.scope.permission"), NamedTextColor.WHITE)));
                    sender.sendMessage(Component.text("/quill scope persist <name> <variable> <add|remove>", NamedTextColor.YELLOW).append(Component.text(" - " + plugin.translate("quill.commands.scope.persist"), NamedTextColor.WHITE)));
                    sender.sendMessage(Component.text("=====================", NamedTextColor.GOLD));
                    break;
                default:
                    break;
            }
        }
    }

    private void createSession(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text(plugin.translate("quill.commands.usage.filename", "edit"), NamedTextColor.RED));
            return;
        }
        
        if (!sender.hasPermission("quill.script.edit")) {
            sender.sendMessage(Component.text(plugin.translate("quill.commands.global.no-permission", "edit scripts"), NamedTextColor.RED));
            return;
        }
        
        String filename = args[1];
        if (!filename.endsWith(".ql") && !filename.endsWith(".quill")) {
            filename += ".ql";
        }
        if (plugin.editValid) {
            final String finalFilename = filename;
            editor.readFile(filename).thenAccept(fileData -> {
                editor.createSession(fileData).thenAccept(sessionId -> {
                    if (sessionId != null) {
                        String url = plugin.getConfig().getString("editor.url") + sessionId;
                        sender.sendMessage(Component.text(plugin.translate("quill.commands.global.session-created") + " ", NamedTextColor.GREEN)
                            .append(Component.text(url, NamedTextColor.GREEN, TextDecoration.UNDERLINED)
                            .clickEvent(ClickEvent.openUrl(url))));

                        CompletableFuture<String> editsFuture = editor.waitForEdits(sessionId);
                        editsFuture.thenAccept(data -> {
                            editor.writeFile(finalFilename, data, editsFuture);
                            sender.sendMessage(Component.text(plugin.translate("quill.commands.scripts.saved", finalFilename), NamedTextColor.GREEN));
                            editor.deleteSession(sessionId, editsFuture);
                        });
                    } else {
                        plugin.getLogger().log(Level.SEVERE, plugin.translate("quill.commands.global.no-sessionid"));
                        sender.sendMessage(Component.text(plugin.translate("quill.commands.global.fail", "create session"), NamedTextColor.RED));
                    }
                });
            });
        } else {
            sender.sendMessage(Component.text(plugin.translate("quill.commands.global.invalid-url"), NamedTextColor.RED));
        }
    }

    private void handleScope(CommandSender sender, String[] args) {
        switch (args[0]) {
            case "create":
                if(scopeManager.createScope(args[1], UUID.fromString(args[2]), List.of(args[3], args[4], args[5], args[6], args[7], args[8]).stream().map(Double::parseDouble).collect(Collectors.toList()), SecurityMode.valueOf(args[9].toUpperCase()), null, null) != null) {
                    sender.sendMessage(Component.text(plugin.translate("quill.commands.scope.created", args[1]), NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text(plugin.translate("quill.commands.scope.create-fail", args[1]), NamedTextColor.RED));
                }
                break;
            case "delete":
                if(scopeManager.deleteScope(args[1])) {
                    sender.sendMessage(Component.text(plugin.translate("quill.commands.scope.deleted", args[1]), NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text(plugin.translate("quill.commands.scope.delete-fail", args[1]), NamedTextColor.RED));
                }
                break;
            case "list":
                List <String> scopes = scopeManager.listScopes();
                sender.sendMessage(Component.text("=== " + plugin.translate("quill.commands.scope.list.title") + " ===", NamedTextColor.GOLD));
                for (String name : scopes) {
                    sender.sendMessage(Component.text(name, NamedTextColor.YELLOW));
                }
                sender.sendMessage(Component.text("=====================", NamedTextColor.GOLD));
                break;
            case "info":
                if (sender.isOp() || sender.hasPermission("quill.scope.info.others")) {
                    Map<String, Object> info = scopeManager.scopeInfo(args[1]);
                    if (info.get("name").equals("scope-not-found")) {
                    sender.sendMessage(Component.text(plugin.translate("quill.commands.scope.info.not-found"), NamedTextColor.RED));
                    } else {
                        @SuppressWarnings("unchecked")
                        List<Double> boundaries = (List<Double>) info.get("boundaries");

                        sender.sendMessage(Component.text("=== " + plugin.translate("quill.commands.scope.info.title", info.get("name")) + " ===", NamedTextColor.GOLD));
                        sender.sendMessage(Component.text(plugin.translate("quill.commands.scope.info.name") + ": ", NamedTextColor.YELLOW).append(Component.text(info.get("name").toString(), NamedTextColor.WHITE)));
                        sender.sendMessage(Component.text(plugin.translate("quill.commands.scope.info.owner") + ": ", NamedTextColor.YELLOW).append(Component.text(Bukkit.getOfflinePlayer(UUID.fromString(info.get("owner").toString())).getName(), NamedTextColor.WHITE)));
                        sender.sendMessage(Component.text(plugin.translate("quill.commands.scope.info.boundaries") + ": ", NamedTextColor.YELLOW).append(Component.text(String.format("%s, %s, %s - %s, %s, %s", boundaries.get(0), boundaries.get(1), boundaries.get(2), boundaries.get(3), boundaries.get(4), boundaries.get(5)), NamedTextColor.WHITE)));
                        sender.sendMessage(Component.text(plugin.translate("quill.commands.scope.info.mode") + ": ", NamedTextColor.YELLOW).append(Component.text(info.get("mode").toString(), NamedTextColor.WHITE)));
                        sender.sendMessage(Component.text(info.get("mode").equals("whitelist") ? plugin.translate("quill.commands.scope.info.whitelisted-funcs") + ": " : plugin.translate("quill.commands.scope.info.blacklisted-funcs"), NamedTextColor.YELLOW).append(Component.text(plugin.translate("quill.commands.scope.info.funcs-hint"), NamedTextColor.WHITE)));
                        sender.sendMessage(Component.text(plugin.translate("quill.commands.scope.info.persistent") + ": ", NamedTextColor.YELLOW).append(Component.text(plugin.translate("quill.commands.scope.info.persistent-hint"), NamedTextColor.WHITE)));
                        sender.sendMessage(Component.text("=====================", NamedTextColor.GOLD));
                    }
                } else {
                    sender.sendMessage(Component.text(plugin.translate("quill.commands.global.no-permission", "view this scope's info"), NamedTextColor.RED));
                }
                break;
            default:
                sender.sendMessage(plugin.translate("quill.commands.global.unknown"));
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("load", "reload", "unload", "edit", "list", "info", "help");
            String partial = args[0].toLowerCase();
            
            for (String subcommand : subcommands) {
                if (subcommand.startsWith(partial)) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            String subcommand = args[0].toLowerCase();
            
            if (subcommand.equals("load") || subcommand.equals("reload") || subcommand.equals("unload") || subcommand.equals("edit")) {
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