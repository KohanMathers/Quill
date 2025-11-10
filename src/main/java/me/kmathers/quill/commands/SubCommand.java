package me.kmathers.quill.commands;

import org.bukkit.command.CommandSender;
import java.util.List;

public interface SubCommand {
    boolean execute(CommandSender sender, String[] args);
    
    String getName();
    
    default List<String> getTabCompletions(CommandSender sender, String[] args) {
        return List.of();
    }
    
    default String getPermission() {
        return null;
    }
    
    default String getHelpText() {
        return "No help available";
    }
}