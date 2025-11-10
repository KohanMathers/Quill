package me.kmathers.quill.commands;

import java.util.*;

public class CommandRegistry {
    private final Map<String, SubCommand> commands = new HashMap<>();
    
    public void register(SubCommand command) {
        commands.put(command.getName().toLowerCase(), command);
    }
    
    public Optional<SubCommand> getCommand(String name) {
        return Optional.ofNullable(commands.get(name.toLowerCase()));
    }
    
    public Set<String> getCommandNames() {
        return commands.keySet();
    }
    
    public Collection<SubCommand> getCommands() {
        return commands.values();
    }
}