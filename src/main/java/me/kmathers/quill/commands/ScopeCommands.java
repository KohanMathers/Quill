package me.kmathers.quill.commands;

import me.kmathers.quill.Quill;
import me.kmathers.quill.QuillScopeManager;
import me.kmathers.quill.utils.Result.BooleanResult;
import me.kmathers.quill.utils.SecurityConfig.SecurityMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class ScopeCommands {
    
    public static class Create implements SubCommand {
        private final Quill plugin;
        private final QuillScopeManager scopeManager;
        
        public Create(Quill plugin, QuillScopeManager scopeManager) {
            this.plugin = plugin;
            this.scopeManager = scopeManager;
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 9) {
                sender.sendMessage(Component.text(
                    "Usage: /quill scope create <name> <owner> <x1> <y1> <z1> <x2> <y2> <z2> <whitelist|blacklist>",
                    NamedTextColor.RED));
                return true;
            }
            
            try {
                List<Double> boundaries = Arrays.stream(args, 2, 8)
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());
                
                SecurityMode mode = SecurityMode.valueOf(args[8].toUpperCase());
                UUID owner = UUID.fromString(args[1]);
                
                if (scopeManager.createScope(args[0], owner, boundaries, mode, null, null) != null) {
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.scope.created", args[0]),
                        NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.scope.create-fail", args[0]),
                        NamedTextColor.RED));
                }
            } catch (IllegalArgumentException e) {
                sender.sendMessage(Component.text("Invalid arguments: " + e.getMessage(), NamedTextColor.RED));
            }
            
            return true;
        }
        
        @Override
        public String getName() {
            return "create";
        }
        
        @Override
        public String getPermission() {
            return "quill.scope.create";
        }
    }
    
    public static class Delete implements SubCommand {
        private final Quill plugin;
        private final QuillScopeManager scopeManager;
        
        public Delete(Quill plugin, QuillScopeManager scopeManager) {
            this.plugin = plugin;
            this.scopeManager = scopeManager;
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 1) {
                sender.sendMessage(Component.text("Usage: /quill scope delete <name>", NamedTextColor.RED));
                return true;
            }
            
            if (scopeManager.deleteScope(args[0])) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.deleted", args[0]),
                    NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.delete-fail", args[0]),
                    NamedTextColor.RED));
            }
            
            return true;
        }
        
        @Override
        public String getName() {
            return "delete";
        }
        
        @Override
        public String getPermission() {
            return "quill.scope.delete";
        }
    }
    
    public static class ListScopes implements SubCommand {
        private final Quill plugin;
        private final QuillScopeManager scopeManager;
        
        public ListScopes(Quill plugin, QuillScopeManager scopeManager) {
            this.plugin = plugin;
            this.scopeManager = scopeManager;
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            List<String> scopes = scopeManager.listScopes();
            sender.sendMessage(Component.text(
                "=== " + plugin.translate("quill.commands.scope.list.title") + " ===",
                NamedTextColor.GOLD));
            
            for (String name : scopes) {
                sender.sendMessage(Component.text(name, NamedTextColor.YELLOW));
            }
            
            sender.sendMessage(Component.text("=====================", NamedTextColor.GOLD));
            return true;
        }
        
        @Override
        public String getName() {
            return "list";
        }
        
        @Override
        public String getPermission() {
            return "quill.scope.list";
        }
    }
    
    public static class Info implements SubCommand {
        private final Quill plugin;
        private final QuillScopeManager scopeManager;
        
        public Info(Quill plugin, QuillScopeManager scopeManager) {
            this.plugin = plugin;
            this.scopeManager = scopeManager;
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 1) {
                sender.sendMessage(Component.text("Usage: /quill scope info <name>", NamedTextColor.RED));
                return true;
            }
            
            Map<String, Object> info = scopeManager.scopeInfo(args[0]);
            
            if (info.get("name").equals("scope-not-found")) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.info.not-found"),
                    NamedTextColor.RED));
                return true;
            }
            
            boolean isOwner = sender instanceof Player player && 
                player.getUniqueId().equals(UUID.fromString(info.get("owner").toString()));
            
            if (!sender.isOp() && !sender.hasPermission("quill.scope.info.others") && !isOwner) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.global.no-permission", "view this scope's info"),
                    NamedTextColor.RED));
                return true;
            }
            
            @SuppressWarnings("unchecked")
            List<Double> boundaries = (List<Double>) info.get("boundaries");
            
            sender.sendMessage(Component.text(
                "=== " + plugin.translate("quill.commands.scope.info.title", info.get("name")) + " ===",
                NamedTextColor.GOLD));
            sender.sendMessage(Component.text(
                plugin.translate("quill.commands.scope.info.name") + ": ", NamedTextColor.YELLOW)
                .append(Component.text(info.get("name").toString(), NamedTextColor.WHITE)));
            sender.sendMessage(Component.text(
                plugin.translate("quill.commands.scope.info.owner") + ": ", NamedTextColor.YELLOW)
                .append(Component.text(
                    Bukkit.getOfflinePlayer(UUID.fromString(info.get("owner").toString())).getName(),
                    NamedTextColor.WHITE)));
            sender.sendMessage(Component.text(
                plugin.translate("quill.commands.scope.info.boundaries") + ": ", NamedTextColor.YELLOW)
                .append(Component.text(
                    String.format("%s, %s, %s - %s, %s, %s", 
                        boundaries.get(0), boundaries.get(1), boundaries.get(2),
                        boundaries.get(3), boundaries.get(4), boundaries.get(5)),
                    NamedTextColor.WHITE)));
            sender.sendMessage(Component.text(
                plugin.translate("quill.commands.scope.info.mode") + ": ", NamedTextColor.YELLOW)
                .append(Component.text(info.get("mode").toString(), NamedTextColor.WHITE)));
            sender.sendMessage(Component.text(
                info.get("mode").equals("whitelist") 
                    ? plugin.translate("quill.commands.scope.info.whitelisted-funcs") + ": "
                    : plugin.translate("quill.commands.scope.info.blacklisted-funcs"),
                NamedTextColor.YELLOW)
                .append(Component.text(
                    plugin.translate("quill.commands.scope.info.funcs-hint"),
                    NamedTextColor.WHITE)));
            sender.sendMessage(Component.text(
                plugin.translate("quill.commands.scope.info.persistent") + ": ", NamedTextColor.YELLOW)
                .append(Component.text(
                    plugin.translate("quill.commands.scope.info.persistent-hint"),
                    NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("=====================", NamedTextColor.GOLD));
            
            return true;
        }
        
        @Override
        public String getName() {
            return "info";
        }
    }
    
    public static class Permission implements SubCommand {
        private final Quill plugin;
        private final QuillScopeManager scopeManager;
        
        public Permission(Quill plugin, QuillScopeManager scopeManager) {
            this.plugin = plugin;
            this.scopeManager = scopeManager;
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 3) {
                sender.sendMessage(Component.text(
                    "Usage: /quill scope permission <grant|revoke> <name> <function>",
                    NamedTextColor.RED));
                return true;
            }
            
            String action = args[0].toLowerCase();
            String scopeName = args[1];
            String function = args[2];
            
            BooleanResult result;
            String translationKey;
            
            switch (action) {
                case "grant":
                    result = scopeManager.grantFunc(scopeName, function);
                    translationKey = "quill.commands.scope.grant";
                    break;
                case "revoke":
                    result = scopeManager.revokeFunc(scopeName, function);
                    translationKey = "quill.commands.scope.revoke";
                    break;
                default:
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.global.unknown", action),
                        NamedTextColor.RED));
                    return true;
            }
            
            if (result.success()) {
                sender.sendMessage(Component.text(
                    plugin.translate(translationKey + ".success", scopeName, function),
                    NamedTextColor.GREEN));
            } else {
                String errorKey = result.message().orElse("default");
                sender.sendMessage(Component.text(
                    plugin.translate(translationKey + ".fail." + errorKey, scopeName, function),
                    NamedTextColor.RED));
            }
            
            return true;
        }
        
        @Override
        public String getName() {
            return "permission";
        }
        
        @Override
        public String getPermission() {
            return "quill.scope.permission";
        }
        
        @Override
        public List<String> getTabCompletions(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return Arrays.asList("grant", "revoke");
            }
            return List.of();
        }
    }

    public static class Persistent implements SubCommand {
        private final Quill plugin;
        private final QuillScopeManager scopeManager;
        
        public Persistent(Quill plugin, QuillScopeManager scopeManager) {
            this.plugin = plugin;
            this.scopeManager = scopeManager;
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 3) {
                sender.sendMessage(Component.text(
                    "Usage: /quill scope persist <add|remove> <name> <variable>",
                    NamedTextColor.RED));
                return true;
            }
            
            String action = args[0].toLowerCase();
            String scopeName = args[1];
            String variable = args[2];
            
            BooleanResult result;
            String translationKey;
            
            switch (action) {
                case "add":
                    result = scopeManager.addPersistentVar(scopeName, variable);
                    translationKey = "quill.commands.scope.add-persistent";
                    break;
                case "revoke":
                    result = scopeManager.removePersistentVar(scopeName, variable);
                    translationKey = "quill.commands.scope.remove-persistent";
                    break;
                default:
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.global.unknown", action),
                        NamedTextColor.RED));
                    return true;
            }
            
            if (result.success()) {
                sender.sendMessage(Component.text(
                    plugin.translate(translationKey + ".success", scopeName, variable),
                    NamedTextColor.GREEN));
            } else {
                String errorKey = result.message().orElse("default");
                sender.sendMessage(Component.text(
                    plugin.translate(translationKey + ".fail." + errorKey, scopeName, variable),
                    NamedTextColor.RED));
            }
            
            return true;
        }
        
        @Override
        public String getName() {
            return "persist";
        }
        
        @Override
        public String getPermission() {
            return "quill.scope.persist";
        }
        
        @Override
        public List<String> getTabCompletions(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return Arrays.asList("add", "remove");
            }
            return List.of();
        }
    }
}