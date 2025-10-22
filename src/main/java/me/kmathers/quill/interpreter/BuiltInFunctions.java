package me.kmathers.quill.interpreter;

import me.kmathers.quill.interpreter.QuillValue.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.List;
/**
 * Built-in functions for Quill.
 * These connect Quill scripts to Minecraft's Bukkit API.
 */
public class BuiltInFunctions {
    // === Scope Functions ===
    
    public static class AddToScopeFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("addtoscope() requires 2 arguments: addtoscope(player, scope)");
            }
            
            Player player = args.get(0).asPlayer();
            ScopeContext targetScope = args.get(1).asScope().getScope();
            
            targetScope.addPlayer(player);

            return new BooleanValue(true);
        }
    }

    // === Helper Methods ===
    public static ItemStack createItemStack(String itemId, int amount) {
        if (itemId == null || itemId.isEmpty()) {
            throw new RuntimeException("Invalid item_id: cannot be null or empty");
        }
        
        String materialName = itemId;
        if (itemId.contains(":")) {
            String[] parts = itemId.split(":", 2);
            // TODO: Implement handling for other namespaces
            materialName = parts[1].toUpperCase();
        } else {
            materialName = itemId.toUpperCase();
        }
        
        Material material;
        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid item_id: '" + itemId + "' is not a valid Minecraft item");
        }
        
        if (amount < 1) {
            throw new RuntimeException("Item amount must be at least 1, got: " + amount);
        }
        if (amount > 64) {
            amount = Math.min(amount, material.getMaxStackSize());
        }
        
        return new ItemStack(material, amount);
    }
}