package me.kmathers.quill.commands;

import me.kmathers.quill.Quill;
import me.kmathers.quill.QuillScopeManager;
import me.kmathers.quill.utils.Result.BooleanResult;
import me.kmathers.quill.utils.SecurityConfig.SecurityMode;
import me.kmathers.quill.utils.SecurityConfig.AccessMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
            if (args.length < 10) {
                sender.sendMessage(Component.text(
                    "Usage: /quill scope create <name> <owner> <x1> <y1> <z1> <x2> <y2> <z2> <whitelist|blacklist> <public|private>",
                    NamedTextColor.RED));
                return true;
            }
            
            if (scopeManager.getScope(args[0]) != null) {
                sender.sendMessage(Component.text(plugin.translate("quill.commands.scope.already-exists", args[0]), NamedTextColor.RED));
                return true;
            }

            try {
                List<Double> boundaries = Arrays.stream(args, 2, 8)
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());
                
                SecurityMode mode = SecurityMode.valueOf(args[8].toUpperCase());
                
                AccessMode access = AccessMode.valueOf(args[9].toUpperCase());

                UUID owner;
                try {
                    owner = UUID.fromString(args[1]);
                } catch (IllegalArgumentException e) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
                    if (player.hasPlayedBefore() || player.isOnline()) {
                        owner = player.getUniqueId();
                    } else {
                        sender.sendMessage(Component.text(
                            plugin.translate("quill.error.scope.no-player", args[1]),
                            NamedTextColor.RED));
                        return true;
                    }
                }
                
                if (scopeManager.createScope(args[0], owner, boundaries, mode, access, null, null) != null) {
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.scope.created", args[0]),
                        NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.global.fail", "create scope: " + args[0]),
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
        public List<String> getPermissions() {
            return List.of("quill.scope.create");
        }

        @Override
        public List<String> getTabCompletions(CommandSender sender, String[] args) {
            return switch (args.length) {
                case 1 -> List.of("<name>");
                case 2 -> Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
                case 3, 4, 5, 6, 7, 8 -> {
                    if (sender instanceof Player player) {
                        var loc = player.getLocation();
                        yield switch (args.length) {
                            case 3 -> List.of(String.valueOf((int) loc.getX()));
                            case 4 -> List.of(String.valueOf((int) loc.getY()));
                            case 5 -> List.of(String.valueOf((int) loc.getZ()));
                            case 6 -> List.of(String.valueOf((int) loc.getX()));
                            case 7 -> List.of(String.valueOf((int) loc.getY()));
                            case 8 -> List.of(String.valueOf((int) loc.getZ()));
                            default -> List.of();
                        };
                    }
                    yield List.of();
                }
                case 9 -> Arrays.asList("whitelist", "blacklist").stream()
                    .filter(m -> m.startsWith(args[8].toLowerCase()))
                    .collect(Collectors.toList());
                case 10 -> Arrays.asList("public", "private").stream()
                    .filter(a -> a.startsWith(args[9].toLowerCase()))
                    .collect(Collectors.toList());
                default -> List.of();
            };
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
            
            String scopeName = args[0];
            var scope = scopeManager.getScope(scopeName);
            
            if (scope == null) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.info.not-found", scopeName),
                    NamedTextColor.RED));
                return true;
            }
            
            if (!sender.isOp() && !sender.hasPermission("quill.scope.delete.any")) {
                if (!(sender instanceof Player player) || !scope.getOwner().equals(player.getUniqueId())) {
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.global.no-permission", "delete this scope"),
                        NamedTextColor.RED));
                    return true;
                }
            }
            
            if (scopeManager.deleteScope(scopeName)) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.deleted", scopeName),
                    NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.global.fail", "delete scope: " + scopeName),
                    NamedTextColor.RED));
            }
            
            return true;
        }
        
        @Override
        public String getName() {
            return "delete";
        }

        @Override
        public List<String> getPermissions() {
            return List.of();
        }

        @Override
        public List<String> getTabCompletions(CommandSender sender, String[] args) {
            if (args.length == 1) {
                List<String> completions = new ArrayList<>();
                boolean canDeleteAny = sender.isOp() || sender.hasPermission("quill.scope.delete.any");

                for (String scopeName : scopeManager.listScopes()) {
                    if (canDeleteAny) {
                        completions.add(scopeName);
                    } else if (sender instanceof Player player) {
                        var scope = scopeManager.getScope(scopeName);
                        if (scope != null && scope.getOwner().equals(player.getUniqueId())) {
                            completions.add(scopeName);
                        }
                    }
                }
                return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            }
            return List.of();
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
            List<String> allScopes = scopeManager.listScopes();
            List<String> visibleScopes = new ArrayList<>();
            
            boolean canSeeAll = sender.isOp() || sender.hasPermission("quill.scope.list.all");
            
            if (canSeeAll) {
                visibleScopes.addAll(allScopes);
            } else {
                UUID senderUUID = null;
                if (sender instanceof Player player) {
                    senderUUID = player.getUniqueId();
                }
                
                for (String scopeName : allScopes) {
                    var scope = scopeManager.getScope(scopeName);
                    
                    if (scope != null && senderUUID != null && scope.getOwner().equals(senderUUID)) {
                        visibleScopes.add(scopeName);
                    }
                }
            }
            
            sender.sendMessage(Component.text(
                "=== " + plugin.translate("quill.commands.scope.list.title") + " ===",
                NamedTextColor.GOLD));
            
            if (visibleScopes.isEmpty()) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.list.none"),
                    NamedTextColor.YELLOW));
            } else {
                for (String name : visibleScopes) {
                    sender.sendMessage(Component.text(name, NamedTextColor.YELLOW));
                }
            }
            
            sender.sendMessage(Component.text("=====================", NamedTextColor.GOLD));
            return true;
        }
        
        @Override
        public String getName() {
            return "list";
        }
        
        @Override
        public List<String> getPermissions() {
            return List.of("quill.scope.list");
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
            
            String scopeName = args[0];
            Map<String, Object> info = scopeManager.scopeInfo(scopeName);
            
            if (info.get("name").equals("scope-not-found")) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.info.not-found", scopeName),
                    NamedTextColor.RED));
                return true;
            }
            
            boolean isOwner = false;
            if (info.get("owner") != null && sender instanceof Player player) {
                isOwner = player.getUniqueId().equals(UUID.fromString(info.get("owner").toString()));
            }
            
            boolean canViewAny = sender.isOp() || sender.hasPermission("quill.scope.info.any");
            
            if (!canViewAny && !isOwner) {
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
                    String.format("%.1f, %.1f, %.1f - %.1f, %.1f, %.1f", 
                        boundaries.get(0), boundaries.get(1), boundaries.get(2),
                        boundaries.get(3), boundaries.get(4), boundaries.get(5)),
                    NamedTextColor.WHITE)));
            sender.sendMessage(Component.text(
                plugin.translate("quill.commands.scope.info.mode") + ": ", NamedTextColor.YELLOW)
                .append(Component.text(info.get("mode").toString(), NamedTextColor.WHITE)));
            sender.sendMessage(Component.text(
                info.get("mode").toString().toLowerCase().equals("whitelist") 
                    ? plugin.translate("quill.commands.scope.info.whitelisted-funcs") + ": "
                    : plugin.translate("quill.commands.scope.info.blacklisted-funcs") + ": ",
                NamedTextColor.YELLOW)
                .append(Component.text(
                    plugin.translate("quill.commands.scope.info.funcs-hint", info.get("name")),
                    NamedTextColor.WHITE)));
            sender.sendMessage(Component.text(
                plugin.translate("quill.commands.scope.info.persistent") + ": ", NamedTextColor.YELLOW)
                .append(Component.text(
                    plugin.translate("quill.commands.scope.info.persistent-hint", info.get("name")),
                    NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("=====================", NamedTextColor.GOLD));
            
            return true;
        }
        
        @Override
        public String getName() {
            return "info";
        }

        @Override
        public List<String> getPermissions() {
            return List.of("quill.scope.info");
        }

        @Override
        public List<String> getTabCompletions(CommandSender sender, String[] args) {
            if (args.length == 1) {
                List<String> completions = new ArrayList<>();
                boolean canViewAny = sender.isOp() || sender.hasPermission("quill.scope.info.any");

                for (String scopeName : scopeManager.listScopes()) {
                    if (canViewAny) {
                        completions.add(scopeName);
                    } else if (sender instanceof Player player) {
                        var scope = scopeManager.getScope(scopeName);
                        if (scope != null && scope.getOwner().equals(player.getUniqueId())) {
                            completions.add(scopeName);
                        }
                    }
                }
                return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            }
            return List.of();
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
            if (args.length < 2) {
                sender.sendMessage(Component.text(
                    "Usage: /quill scope permission <grant|revoke> <name> <function>",
                    NamedTextColor.RED));
                sender.sendMessage(Component.text(
                    "/quill scope permission <list> <name>",
                    NamedTextColor.RED));
                return true;
            }
            
            String action = args[0].toLowerCase();
            String scopeName = args[1];
            String function = args.length > 2 ? args[2] : null;
            
            var scope = scopeManager.getScope(scopeName);
            
            if (scope == null) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.info.not-found", scopeName),
                    NamedTextColor.RED));
                return true;
            }
            
            if (!sender.isOp() && !sender.hasPermission("quill.scope.permission")) {
                if (!(sender instanceof Player player) || !scope.getOwner().equals(player.getUniqueId())) {
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.global.no-permission", "modify this scope's permissions"),
                        NamedTextColor.RED));
                    return true;
                }
            }
            
            BooleanResult result;
            String translationKey;
            
            switch (action) {
                case "grant":
                    if (function == null) {
                        sender.sendMessage(Component.text("Usage: /quill scope permission grant <name> <function>", NamedTextColor.RED));
                        return true;
                    }
                    result = scopeManager.grantFunc(scopeName, function);
                    translationKey = "quill.commands.scope.grant";
                    break;
                case "revoke":
                    if (function == null) {
                        sender.sendMessage(Component.text("Usage: /quill scope permission revoke <name> <function>", NamedTextColor.RED));
                        return true;
                    }
                    result = scopeManager.revokeFunc(scopeName, function);
                    translationKey = "quill.commands.scope.revoke";
                    break;
                case "list":
                    List<String> funcs = scopeManager.getFuncs(scopeName);
                    if (funcs != null) {
                        if (!(funcs.isEmpty())) {
                            sender.sendMessage(Component.text("=== " + plugin.translate("quill.commands.scope.permission.list.title", scopeName, scopeManager.getScope(scopeName).getSecurityMode().equals(SecurityMode.WHITELIST) ? "Whitelisted" : "Blacklisted") + " ===", NamedTextColor.GOLD));
                            for (String func : funcs) {
                                sender.sendMessage(Component.text(func, NamedTextColor.YELLOW));
                            }
                            sender.sendMessage(Component.text("==============================", NamedTextColor.GOLD));
                        } else {
                            sender.sendMessage(Component.text(plugin.translate("quill.commands.scope.permission.list.no-funcs", scopeName, scopeManager.getScope(scopeName).getSecurityMode().equals(SecurityMode.WHITELIST) ? "whitelisted" : "blacklisted"), NamedTextColor.YELLOW));
                        }
                    } else {
                        sender.sendMessage(Component.text(plugin.translate("quill.commands.scope.info.not-found", scopeName), NamedTextColor.RED));
                    }
                    result = null;
                    translationKey = null;
                    break;
                default:
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.global.unknown", action),
                        NamedTextColor.RED));
                    return true;
            }
            
            if (result != null) {
                if (result.success()) {
                    sender.sendMessage(Component.text(
                        plugin.translate(translationKey + ".success", function, scopeName),
                        NamedTextColor.GREEN));
                } else {
                    String errorKey = result.message().orElse("default");
                    sender.sendMessage(Component.text(
                        plugin.translate(translationKey + ".fail." + errorKey, scopeName, function),
                        NamedTextColor.RED));
                }
            }
            
            return true;
        }
        
        @Override
        public String getName() {
            return "permission";
        }
        
        @Override
        public List<String> getPermissions() {
            return List.of();
        }
        
        @Override
        public List<String> getTabCompletions(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return Arrays.asList("grant", "revoke", "list").stream()
                    .filter(a -> a.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            } else if (args.length == 2) {
                List<String> completions = new ArrayList<>();
                boolean canModifyAny = sender.isOp() || sender.hasPermission("quill.scope.permission");

                for (String scopeName : scopeManager.listScopes()) {
                    if (canModifyAny) {
                        completions.add(scopeName);
                    } else if (sender instanceof Player player) {
                        var scope = scopeManager.getScope(scopeName);
                        if (scope != null && scope.getOwner().equals(player.getUniqueId())) {
                            completions.add(scopeName);
                        }
                    }
                }
                return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
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
            if (args.length < 2) {
                sender.sendMessage(Component.text(
                    "Usage: /quill scope persist <add|remove> <name> <variable>",
                    NamedTextColor.RED));
                sender.sendMessage(Component.text("/quill scope persist list <name>", NamedTextColor.RED));
                return true;
            }
            
            String action = args[0].toLowerCase();
            String scopeName = args[1];
            String variable = args.length > 2 ? args[2] : null;
            
            var scope = scopeManager.getScope(scopeName);
            
            if (scope == null) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.info.not-found", scopeName),
                    NamedTextColor.RED));
                return true;
            }
            
            if (!sender.isOp() && !sender.hasPermission("quill.scope.persist.any")) {
                if (!(sender instanceof Player player) || !scope.getOwner().equals(player.getUniqueId())) {
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.global.no-permission", "modify this scope's persistent variables"),
                        NamedTextColor.RED));
                    return true;
                }
            }
            
            BooleanResult result;
            String translationKey;
            
            switch (action) {
                case "add":
                    if (variable == null) {
                        sender.sendMessage(Component.text("Usage: /quill scope persist add <name> <variable>", NamedTextColor.RED)); 
                        return true;
                    }
                    result = scopeManager.addPersistentVar(scopeName, variable);
                    translationKey = "quill.commands.scope.add-persistent";
                    break;
                case "remove":
                    if (variable == null) {
                        sender.sendMessage(Component.text("Usage: /quill scope persist remove <name> <variable>", NamedTextColor.RED)); 
                        return true;
                    }
                    result = scopeManager.removePersistentVar(scopeName, variable);
                    translationKey = "quill.commands.scope.remove-persistent";
                    break;
                case "list":
                    List<String> vars = scopeManager.getPersistentVars(scopeName);
                    if (vars != null) {
                        if (!(vars.isEmpty())) {
                            sender.sendMessage(Component.text("=== " + plugin.translate("quill.commands.scope.persist.list.title", scopeName) + " ===", NamedTextColor.GOLD));
                            for (String var : vars) {
                                sender.sendMessage(Component.text(var, NamedTextColor.YELLOW));
                            }
                            sender.sendMessage(Component.text("==============================", NamedTextColor.GOLD));
                        } else {
                            sender.sendMessage(Component.text(plugin.translate("quill.commands.scope.persist.list.no-vars", scopeName), NamedTextColor.YELLOW));
                        }
                    } else {
                        sender.sendMessage(Component.text(plugin.translate("quill.commands.scope.info.not-found", scopeName), NamedTextColor.RED));
                    }
                    result = null;
                    translationKey = null;
                    break;
                default:
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.global.unknown", action),
                        NamedTextColor.RED));
                    return true;
            }
            
            if (result != null) {
                if (result.success()) {
                    sender.sendMessage(Component.text(
                        plugin.translate(translationKey + ".success", variable, scopeName),
                        NamedTextColor.GREEN));
                } else {
                    String errorKey = result.message().orElse("default");
                    sender.sendMessage(Component.text(
                        plugin.translate(translationKey + ".fail." + errorKey, scopeName, variable),
                        NamedTextColor.RED));
                }
            }
            
            return true;
        }

        @Override
        public String getName() {
            return "persist";
        }
        
        @Override
        public List<String> getPermissions() {
            return List.of("quill.scope.persist");
        }
        
        @Override
        public List<String> getTabCompletions(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return Arrays.asList("add", "remove", "list").stream()
                    .filter(a -> a.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            } else if (args.length == 2) {
                List<String> completions = new ArrayList<>();
                boolean canModifyAny = sender.isOp() || sender.hasPermission("quill.scope.persist.any");

                for (String scopeName : scopeManager.listScopes()) {
                    if (canModifyAny) {
                        completions.add(scopeName);
                    } else if (sender instanceof Player player) {
                        var scope = scopeManager.getScope(scopeName);
                        if (scope != null && scope.getOwner().equals(player.getUniqueId())) {
                            completions.add(scopeName);
                        }
                    }
                }
                return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
            return List.of();
        }
    }

    public static class Mode implements SubCommand {
        private final Quill plugin;
        private final QuillScopeManager scopeManager;
        
        public Mode(Quill plugin, QuillScopeManager scopeManager) {
            this.plugin = plugin;
            this.scopeManager = scopeManager;
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                sender.sendMessage(Component.text(
                    "Usage: /quill scope mode <name> <mode>",
                    NamedTextColor.RED));
                return true;
            }
            
            String scopeName = args[0];
            String modeString = args[1].toLowerCase();

            var scope = scopeManager.getScope(scopeName);
            
            if (scope == null) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.info.not-found", scopeName),
                    NamedTextColor.RED));
                return true;
            }
            
            if (!sender.isOp() && !sender.hasPermission("quill.scope.mode")) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.global.no-permission", "modify this scope's security mode"),
                    NamedTextColor.RED));
                return true;
            }
            
            try {
                SecurityMode.valueOf(modeString.toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(Component.text(plugin.translate("quill.commands.scope.mode.no-mode", modeString)));
                return true;
            }

            BooleanResult result;
            String translationKey;
            
            switch (modeString) {
                case "whitelist":
                    result = scopeManager.setMode(scopeName, modeString);
                    translationKey = "quill.commands.scope.mode";
                    break;
                case "blacklist":
                    result = scopeManager.setMode(scopeName, modeString);
                    translationKey = "quill.commands.scope.mode";
                    break;
                default:
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.scope.mode.unknown", modeString),
                        NamedTextColor.RED));
                    return true;
            }
            
            if (result != null) {
                if (result.success()) {
                    sender.sendMessage(Component.text(
                        plugin.translate(translationKey + ".success", modeString, scopeName),
                        NamedTextColor.GREEN));
                } else {
                    String errorKey = result.message().orElse("default");
                    sender.sendMessage(Component.text(
                        plugin.translate(translationKey + ".fail." + errorKey, scopeName, modeString),
                        NamedTextColor.RED));
                }
            }
            
            return true;
        }


        @Override
        public String getName() {
            return "mode";
        }

        @Override
        public List<String> getPermissions() {
            return List.of("quill.scope.mode");
        }

        @Override
        public List<String> getTabCompletions(CommandSender sender, String[] args) {
            if (args.length == 1) {
                List<String> completions = new ArrayList<>();
                boolean canModifyAny = sender.isOp() || sender.hasPermission("quill.scope.mode");

                for (String scopeName : scopeManager.listScopes()) {
                    if (canModifyAny) {
                        completions.add(scopeName);
                    }
                }
                return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            } else if (args.length == 2) {
                return Arrays.asList("whitelist", "blacklist").stream()
                    .filter(m -> m.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
            return List.of();
        }
    }

    public static class AddPlayer implements SubCommand {
        private final Quill plugin;
        private final QuillScopeManager scopeManager;
        
        public AddPlayer(Quill plugin, QuillScopeManager scopeManager) {
            this.plugin = plugin;
            this.scopeManager = scopeManager;
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                sender.sendMessage(Component.text(
                    "Usage: /quill scope addplayer <scope> <player>",
                    NamedTextColor.RED));
                return true;
            }
            
            String scopeName = args[0];
            String playerName = args[1];
            
            var scope = scopeManager.getScope(scopeName);
            if (scope == null) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.info.not-found", scopeName),
                    NamedTextColor.RED));
                return true;
            }
            
            // CHANGED: Only .any permission can add players (no ownership check)
            if (!sender.isOp() && !sender.hasPermission("quill.scope.players.any")) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.global.no-permission", "add players to scopes"),
                    NamedTextColor.RED));
                return true;
            }
            
            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.error.scope.no-player", playerName),
                    NamedTextColor.RED));
                return true;
            }
            
            BooleanResult result = scopeManager.addPlayer(scopeName, target.getUniqueId());
            
            if (result.success()) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.addplayer.success", target.getName(), scopeName),
                    NamedTextColor.GREEN));
            } else {
                String errorKey = result.message().orElse("default");
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.addplayer.fail." + errorKey, scopeName, target.getName()),
                    NamedTextColor.RED));
            }
            
            return true;
        }
        
        @Override
        public String getName() {
            return "addplayer";
        }

        @Override
        public List<String> getPermissions() {
            return List.of("quill.scope.players.any");
        }

        @Override
        public List<String> getTabCompletions(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return scopeManager.listScopes().stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            } else if (args.length == 2) {
                String scopeName = args[0];
                var scope = scopeManager.getScope(scopeName);
                if (scope != null) {
                    Set<UUID> playersInScope = new HashSet<>(scopeManager.getPlayersInScope(scopeName));
                    return Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !playersInScope.contains(p.getUniqueId()))
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                }
            }
            return List.of();
        }
    }

    public static class RemovePlayer implements SubCommand {
        private final Quill plugin;
        private final QuillScopeManager scopeManager;
        
        public RemovePlayer(Quill plugin, QuillScopeManager scopeManager) {
            this.plugin = plugin;
            this.scopeManager = scopeManager;
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                sender.sendMessage(Component.text(
                    "Usage: /quill scope removeplayer <scope> <player>",
                    NamedTextColor.RED));
                return true;
            }
            
            String scopeName = args[0];
            String playerName = args[1];
            
            var scope = scopeManager.getScope(scopeName);
            if (scope == null) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.info.not-found", scopeName),
                    NamedTextColor.RED));
                return true;
            }
            
            // Check permissions
            if (!sender.isOp() && !sender.hasPermission("quill.scope.players.any")) {
                if (!(sender instanceof Player player) || !scope.getOwner().equals(player.getUniqueId())) {
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.global.no-permission", "modify this scope's players"),
                        NamedTextColor.RED));
                    return true;
                }
            }
            
            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.error.scope.no-player", playerName),
                    NamedTextColor.RED));
                return true;
            }
            
            BooleanResult result = scopeManager.removePlayer(scopeName, target.getUniqueId());
            
            if (result.success()) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.removeplayer.success", target.getName(), scopeName),
                    NamedTextColor.GREEN));
            } else {
                String errorKey = result.message().orElse("default");
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.removeplayer.fail." + errorKey, scopeName, target.getName()),
                    NamedTextColor.RED));
            }
            
            return true;
        }
        
        @Override
        public String getName() {
            return "removeplayer";
        }

        @Override
        public List<String> getPermissions() {
            return List.of("quill.scope.players");
        }

        @Override
        public List<String> getTabCompletions(CommandSender sender, String[] args) {
            if (args.length == 1) {
                List<String> completions = new ArrayList<>();
                boolean canModifyAny = sender.isOp() || sender.hasPermission("quill.scope.players.any");

                for (String scopeName : scopeManager.listScopes()) {
                    if (canModifyAny) {
                        completions.add(scopeName);
                    } else if (sender instanceof Player player) {
                        var scope = scopeManager.getScope(scopeName);
                        if (scope != null && scope.getOwner().equals(player.getUniqueId())) {
                            completions.add(scopeName);
                        }
                    }
                }
                return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            } else if (args.length == 2) {
                String scopeName = args[0];
                var scope = scopeManager.getScope(scopeName);
                if (scope != null) {
                    return scopeManager.getPlayersInScope(scopeName).stream()
                        .map(Bukkit::getOfflinePlayer)
                        .map(OfflinePlayer::getName)
                        .filter(Objects::nonNull)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                }
            }
            return List.of();
        }
    }

    public static class ListPlayers implements SubCommand {
        private final Quill plugin;
        private final QuillScopeManager scopeManager;
        
        public ListPlayers(Quill plugin, QuillScopeManager scopeManager) {
            this.plugin = plugin;
            this.scopeManager = scopeManager;
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 1) {
                sender.sendMessage(Component.text(
                    "Usage: /quill scope listplayers <scope>",
                    NamedTextColor.RED));
                return true;
            }
            
            String scopeName = args[0];
            var scope = scopeManager.getScope(scopeName);
            
            if (scope == null) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.info.not-found", scopeName),
                    NamedTextColor.RED));
                return true;
            }
            
            List<UUID> players = scopeManager.getPlayersInScope(scopeName);
            
            sender.sendMessage(Component.text(
                "=== " + plugin.translate("quill.commands.scope.listplayers.title", scopeName) + " ===",
                NamedTextColor.GOLD));
            
            if (players.isEmpty()) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.listplayers.none", scopeName),
                    NamedTextColor.YELLOW));
            } else {
                for (UUID playerId : players) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
                    String status = player.isOnline() ? " (online)" : "";
                    sender.sendMessage(Component.text(
                        "- " + player.getName() + status,
                        NamedTextColor.YELLOW));
                }
            }
            
            sender.sendMessage(Component.text("==============================", NamedTextColor.GOLD));
            return true;
        }
        
        @Override
        public String getName() {
            return "listplayers";
        }

        @Override
        public List<String> getPermissions() {
            return List.of("quill.scope.players");
        }

        @Override
        public List<String> getTabCompletions(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return scopeManager.listScopes().stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            }
            return List.of();
        }
    }

    public static class Join implements SubCommand {
        private final Quill plugin;
        private final QuillScopeManager scopeManager;
        
        public Join(Quill plugin, QuillScopeManager scopeManager) {
            this.plugin = plugin;
            this.scopeManager = scopeManager;
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.global.player-only"),
                    NamedTextColor.RED));
                return true;
            }
            
            if (args.length < 1) {
                sender.sendMessage(Component.text(
                    "Usage: /quill scope join <scope>",
                    NamedTextColor.RED));
                return true;
            }
            
            String scopeName = args[0];
            var scope = scopeManager.getScope(scopeName);
            
            if (scope == null) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.info.not-found", scopeName),
                    NamedTextColor.RED));
                return true;
            }
            
            if (scope.hasPlayer(player.getUniqueId())) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.join.already-in", scopeName),
                    NamedTextColor.RED));
                return true;
            }
            
            if (scope.hasJoinRequest(player.getUniqueId())) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.join.request-pending", scopeName),
                    NamedTextColor.YELLOW));
                return true;
            }
            
            if (scope.getAccessMode() == AccessMode.PUBLIC) {
                BooleanResult result = scopeManager.addPlayer(scopeName, player.getUniqueId());
                
                if (result.success()) {
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.scope.join.success", scopeName),
                        NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.scope.join.fail"),
                        NamedTextColor.RED));
                }
            } else {
                BooleanResult result = scopeManager.addJoinRequest(scopeName, player.getUniqueId());
                
                if (result.success()) {
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.scope.join.request-sent", scopeName),
                        NamedTextColor.GREEN));
                    
                    Player owner = Bukkit.getPlayer(scope.getOwner());
                    if (owner != null && owner.isOnline()) {
                        owner.sendMessage(Component.text(
                            plugin.translate("quill.scope.requests.new-request", player.getName(), scopeName),
                            NamedTextColor.YELLOW));
                        
                        Component acceptButton = Component.text("[Accept]", NamedTextColor.GREEN)
                            .clickEvent(ClickEvent.runCommand("/quill scope accept " + scopeName + " " + player.getName()));
                        Component denyButton = Component.text("[Deny]", NamedTextColor.RED)
                            .clickEvent(ClickEvent.runCommand("/quill scope deny " + scopeName + " " + player.getName()));
                        
                        owner.sendMessage(acceptButton.append(Component.text(" ")).append(denyButton));
                    }
                } else {
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.scope.join.fail"),
                        NamedTextColor.RED));
                }
            }
            
            return true;
        }
        
        @Override
        public String getName() {
            return "join";
        }
        
        @Override
        public List<String> getPermissions() {
            return List.of("quill.scope.join");
        }
        
        @Override
        public List<String> getTabCompletions(CommandSender sender, String[] args) {
            if (args.length == 1) {
                List<String> completions = new ArrayList<>();
                for (String scopeName : scopeManager.listScopes()) {
                    var scope = scopeManager.getScope(scopeName);
                    if (scope != null && scope.getAccessMode() == AccessMode.PUBLIC) {
                        completions.add(scopeName);
                    }
                }
                return completions;
            }
            return List.of();
        }
    }

    public static class Leave implements SubCommand {
        private final Quill plugin;
        private final QuillScopeManager scopeManager;
        
        public Leave(Quill plugin, QuillScopeManager scopeManager) {
            this.plugin = plugin;
            this.scopeManager = scopeManager;
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.global.player-only"),
                    NamedTextColor.RED));
                return true;
            }
            
            if (args.length < 1) {
                sender.sendMessage(Component.text(
                    "Usage: /quill scope leave <scope>",
                    NamedTextColor.RED));
                return true;
            }
            
            String scopeName = args[0];
            var scope = scopeManager.getScope(scopeName);
            
            if (scope == null) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.info.not-found", scopeName),
                    NamedTextColor.RED));
                return true;
            }
            
            if (!scope.hasPlayer(player.getUniqueId())) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.leave.not-in", scopeName),
                    NamedTextColor.RED));
                return true;
            }
            
            if (scope.getOwner().equals(player.getUniqueId())) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.leave.owner-cannot-leave"),
                    NamedTextColor.RED));
                return true;
            }
            
            BooleanResult result = scopeManager.removePlayer(scopeName, player.getUniqueId());
            
            if (result.success()) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.leave.success", scopeName),
                    NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.leave.fail"),
                    NamedTextColor.RED));
            }
            
            return true;
        }
        
        @Override
        public String getName() {
            return "leave";
        }
        
        @Override
        public List<String> getPermissions() {
            return List.of("quill.scope.leave");
        }
        
        @Override
        public List<String> getTabCompletions(CommandSender sender, String[] args) {
            if (args.length == 1 && sender instanceof Player player) {
                List<String> completions = new ArrayList<>();
                for (String scopeName : scopeManager.listScopes()) {
                    var scope = scopeManager.getScope(scopeName);
                    if (scope != null && scope.hasPlayer(player.getUniqueId()) 
                        && !scope.getOwner().equals(player.getUniqueId())) {
                        completions.add(scopeName);
                    }
                }
                return completions;
            }
            return List.of();
        }
    }

    public static class Accept implements SubCommand {
        private final Quill plugin;
        private final QuillScopeManager scopeManager;
        
        public Accept(Quill plugin, QuillScopeManager scopeManager) {
            this.plugin = plugin;
            this.scopeManager = scopeManager;
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                sender.sendMessage(Component.text(
                    "Usage: /quill scope accept <scope> <player>",
                    NamedTextColor.RED));
                return true;
            }
            
            String scopeName = args[0];
            String playerName = args[1];
            
            var scope = scopeManager.getScope(scopeName);
            if (scope == null) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.info.not-found", scopeName),
                    NamedTextColor.RED));
                return true;
            }
            
            if (!sender.isOp() && !sender.hasPermission("quill.scope.requests.any")) {
                if (!(sender instanceof Player player) || !scope.getOwner().equals(player.getUniqueId())) {
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.global.no-permission", "accept join requests"),
                        NamedTextColor.RED));
                    return true;
                }
            }
            
            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.error.scope.no-player", playerName),
                    NamedTextColor.RED));
                return true;
            }
            
            if (!scope.hasJoinRequest(target.getUniqueId())) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.accept.no-request", target.getName(), scopeName),
                    NamedTextColor.RED));
                return true;
            }
            
            BooleanResult result = scopeManager.addPlayer(scopeName, target.getUniqueId());
            
            if (result.success()) {
                scopeManager.removeJoinRequest(scopeName, target.getUniqueId());
                
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.accept.success", target.getName(), scopeName),
                    NamedTextColor.GREEN));
                
                Player targetPlayer = target.getPlayer();
                if (targetPlayer != null && targetPlayer.isOnline()) {
                    targetPlayer.sendMessage(Component.text(
                        plugin.translate("quill.scope.requests.accepted", scopeName),
                        NamedTextColor.GREEN));
                }
            } else {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.accept.fail"),
                    NamedTextColor.RED));
            }
            
            return true;
        }
        
        @Override
        public String getName() {
            return "accept";
        }
        
        @Override
        public List<String> getPermissions() {
            return List.of("quill.scope.requests");
        }
        
        @Override
        public List<String> getTabCompletions(CommandSender sender, String[] args) {
            if (args.length == 1) {
                List<String> completions = new ArrayList<>();
                if (sender instanceof Player player) {
                    for (String scopeName : scopeManager.listScopes()) {
                        var scope = scopeManager.getScope(scopeName);
                        if (scope != null && scope.getOwner().equals(player.getUniqueId()) 
                            && !scope.getPendingRequests().isEmpty()) {
                            completions.add(scopeName);
                        }
                    }
                }
                return completions;
            } else if (args.length == 2) {
                String scopeName = args[0];
                var scope = scopeManager.getScope(scopeName);
                if (scope != null) {
                    List<String> completions = new ArrayList<>();
                    for (UUID playerId : scope.getPendingRequests().keySet()) {
                        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
                        if (player.getName() != null) {
                            completions.add(player.getName());
                        }
                    }
                    return completions;
                }
            }
            return List.of();
        }
    }

    public static class Deny implements SubCommand {
        private final Quill plugin;
        private final QuillScopeManager scopeManager;
        
        public Deny(Quill plugin, QuillScopeManager scopeManager) {
            this.plugin = plugin;
            this.scopeManager = scopeManager;
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                sender.sendMessage(Component.text(
                    "Usage: /quill scope deny <scope> <player>",
                    NamedTextColor.RED));
                return true;
            }
            
            String scopeName = args[0];
            String playerName = args[1];
            
            var scope = scopeManager.getScope(scopeName);
            if (scope == null) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.info.not-found", scopeName),
                    NamedTextColor.RED));
                return true;
            }
            
            if (!sender.isOp() && !sender.hasPermission("quill.scope.requests.any")) {
                if (!(sender instanceof Player player) || !scope.getOwner().equals(player.getUniqueId())) {
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.global.no-permission", "deny join requests"),
                        NamedTextColor.RED));
                    return true;
                }
            }
            
            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.error.scope.no-player", playerName),
                    NamedTextColor.RED));
                return true;
            }
            
            if (!scope.hasJoinRequest(target.getUniqueId())) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.deny.no-request", target.getName(), scopeName),
                    NamedTextColor.RED));
                return true;
            }
            
            BooleanResult result = scopeManager.removeJoinRequest(scopeName, target.getUniqueId());
            
            if (result.success()) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.deny.success", target.getName(), scopeName),
                    NamedTextColor.GREEN));
                
                Player targetPlayer = target.getPlayer();
                if (targetPlayer != null && targetPlayer.isOnline()) {
                    targetPlayer.sendMessage(Component.text(
                        plugin.translate("quill.scope.requests.denied", scopeName),
                        NamedTextColor.RED));
                }
            } else {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.deny.fail"),
                    NamedTextColor.RED));
            }
            
            return true;
        }
        
        @Override
        public String getName() {
            return "deny";
        }
        
        @Override
        public List<String> getPermissions() {
            return List.of("quill.scope.requests");
        }
        
        @Override
        public List<String> getTabCompletions(CommandSender sender, String[] args) {
            if (args.length == 1) {
                List<String> completions = new ArrayList<>();
                if (sender instanceof Player player) {
                    for (String scopeName : scopeManager.listScopes()) {
                        var scope = scopeManager.getScope(scopeName);
                        if (scope != null && scope.getOwner().equals(player.getUniqueId()) 
                            && !scope.getPendingRequests().isEmpty()) {
                            completions.add(scopeName);
                        }
                    }
                }
                return completions;
            } else if (args.length == 2) {
                String scopeName = args[0];
                var scope = scopeManager.getScope(scopeName);
                if (scope != null) {
                    List<String> completions = new ArrayList<>();
                    for (UUID playerId : scope.getPendingRequests().keySet()) {
                        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
                        if (player.getName() != null) {
                            completions.add(player.getName());
                        }
                    }
                    return completions;
                }
            }
            return List.of();
        }
    }

    public static class Requests implements SubCommand {
        private final Quill plugin;
        private final QuillScopeManager scopeManager;
        
        public Requests(Quill plugin, QuillScopeManager scopeManager) {
            this.plugin = plugin;
            this.scopeManager = scopeManager;
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 1) {
                sender.sendMessage(Component.text(
                    "Usage: /quill scope requests <scope>",
                    NamedTextColor.RED));
                return true;
            }
            
            String scopeName = args[0];
            var scope = scopeManager.getScope(scopeName);
            
            if (scope == null) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.info.not-found", scopeName),
                    NamedTextColor.RED));
                return true;
            }
            
            if (!sender.isOp() && !sender.hasPermission("quill.scope.requests.any")) {
                if (!(sender instanceof Player player) || !scope.getOwner().equals(player.getUniqueId())) {
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.global.no-permission", "view join requests"),
                        NamedTextColor.RED));
                    return true;
                }
            }
            
            Map<UUID, Long> requests = scope.getPendingRequests();
            
            sender.sendMessage(Component.text(
                "=== " + plugin.translate("quill.commands.scope.requests.title", scopeName) + " ===",
                NamedTextColor.GOLD));
            
            if (requests.isEmpty()) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.requests.none", scopeName),
                    NamedTextColor.YELLOW));
            } else {
                for (Map.Entry<UUID, Long> entry : requests.entrySet()) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());
                    String playerName = player.getName() != null ? player.getName() : entry.getKey().toString();
                    
                    Component message = Component.text("- " + playerName, NamedTextColor.YELLOW);
                    
                    if (sender instanceof Player) {
                        Component acceptButton = Component.text(" [Accept]", NamedTextColor.GREEN)
                            .clickEvent(ClickEvent.runCommand("/quill scope accept " + scopeName + " " + playerName));
                        Component denyButton = Component.text(" [Deny]", NamedTextColor.RED)
                            .clickEvent(ClickEvent.runCommand("/quill scope deny " + scopeName + " " + playerName));
                        
                        message = message.append(acceptButton).append(denyButton);
                    }
                    
                    sender.sendMessage(message);
                }
            }
            
            sender.sendMessage(Component.text("==============================", NamedTextColor.GOLD));
            return true;
        }
        
        @Override
        public String getName() {
            return "requests";
        }
        
        @Override
        public List<String> getPermissions() {
            return List.of("quill.scope.requests");
        }
        
        @Override
        public List<String> getTabCompletions(CommandSender sender, String[] args) {
            if (args.length == 1 && sender instanceof Player player) {
                List<String> completions = new ArrayList<>();
                for (String scopeName : scopeManager.listScopes()) {
                    var scope = scopeManager.getScope(scopeName);
                    if (scope != null && scope.getOwner().equals(player.getUniqueId())) {
                        completions.add(scopeName);
                    }
                }
                return completions;
            }
            return List.of();
        }
    }

    public static class Access implements SubCommand {
        private final Quill plugin;
        private final QuillScopeManager scopeManager;
        
        public Access(Quill plugin, QuillScopeManager scopeManager) {
            this.plugin = plugin;
            this.scopeManager = scopeManager;
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                sender.sendMessage(Component.text(
                    "Usage: /quill scope access <scope> <public|private>",
                    NamedTextColor.RED));
                return true;
            }
            
            String scopeName = args[0];
            String accessStr = args[1].toLowerCase();
            
            var scope = scopeManager.getScope(scopeName);
            if (scope == null) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.info.not-found", scopeName),
                    NamedTextColor.RED));
                return true;
            }
            
            if (!sender.isOp() && !sender.hasPermission("quill.scope.access.any")) {
                if (!(sender instanceof Player player) || !scope.getOwner().equals(player.getUniqueId())) {
                    sender.sendMessage(Component.text(
                        plugin.translate("quill.commands.global.no-permission", "change scope access mode"),
                        NamedTextColor.RED));
                    return true;
                }
            }
            
            BooleanResult result = scopeManager.setAccessMode(scopeName, accessStr);
            
            if (result.success()) {
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.access.success", accessStr, scopeName),
                    NamedTextColor.GREEN));
            } else {
                String errorKey = result.message().orElse("default");
                sender.sendMessage(Component.text(
                    plugin.translate("quill.commands.scope.access.fail." + errorKey, scopeName, accessStr),
                    NamedTextColor.RED));
            }
            
            return true;
        }
        
        @Override
        public String getName() {
            return "access";
        }
        
        @Override
        public List<String> getPermissions() {
            return List.of("quill.scope.access");
        }
        
        @Override
        public List<String> getTabCompletions(CommandSender sender, String[] args) {
            if (args.length == 1 && sender instanceof Player player) {
                List<String> completions = new ArrayList<>();
                for (String scopeName : scopeManager.listScopes()) {
                    var scope = scopeManager.getScope(scopeName);
                    if (scope != null && scope.getOwner().equals(player.getUniqueId())) {
                        completions.add(scopeName);
                    }
                }
                return completions;
            } else if (args.length == 2) {
                return Arrays.asList("public", "private");
            }
            return List.of();
        }
    }
}