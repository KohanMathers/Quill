package me.kmathers.quill.interpreter;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import me.kmathers.quill.interpreter.QuillValue.ItemValue;
import me.kmathers.quill.interpreter.QuillValue.LocationValue;

/**
 * Built-in constructor functions for Quill.
 */
public class BuiltInConstructorFuncs {
    
    public static class LocationFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 3 && args.size() != 4) {
                throw new RuntimeException("location() requires 3 or 4 arguments: location(x, y, z) or location(x, y, z, world)");
            }
            
            double x = args.get(0).asNumber();
            double y = args.get(1).asNumber();
            double z = args.get(2).asNumber();
            
            World world;
            if (args.size() == 4) {
                String worldName = args.get(3).asString();
                world = Bukkit.getWorld(worldName);
                if (world == null) {
                    throw new RuntimeException("World not found: " + worldName);
                }
            } else {
                ScopeContext.Region region = scope.getRegion();
                if (region == null) {
                    throw new RuntimeException("No region defined in scope - cannot determine world for location");
                }
                world = Bukkit.getWorld(region.getWorldName());
                if (world == null) {
                    throw new RuntimeException("World '" + region.getWorldName() + "' not found");
                }
            }
            
            return new LocationValue(new Location(world, x, y, z));
        }
    }
    
    public static class ItemFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() < 1 || args.size() > 3) {
                throw new RuntimeException("item() requires 1 to 3 arguments: item(item_id), item(item_id, amount), or item(item_id, amount, metadata)");
            }
            
            String itemId = args.get(0).asString();
            int amount = args.size() >= 2 ? (int) args.get(1).asNumber() : 1;
            // TODO metadata (args.get(2))
            
            ItemStack item = createItemStack(itemId, amount);
            return new ItemValue(item);
        }
        
        private ItemStack createItemStack(String itemId, int amount) {
            if (itemId == null || itemId.isEmpty()) {
                throw new RuntimeException("Invalid item_id: cannot be null or empty");
            }
            
            String materialName = itemId;
            if (itemId.contains(":")) {
                String[] parts = itemId.split(":", 2);
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
}