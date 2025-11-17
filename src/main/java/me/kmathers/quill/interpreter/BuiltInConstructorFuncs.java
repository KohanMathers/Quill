package me.kmathers.quill.interpreter;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;

import org.bukkit.NamespacedKey;

import me.kmathers.quill.Quill;
import me.kmathers.quill.interpreter.QuillValue.ItemValue;
import me.kmathers.quill.interpreter.QuillValue.LocationValue;

import net.kyori.adventure.text.Component;

/**
 * Built-in constructor functions for Quill.
 */
public class BuiltInConstructorFuncs {
    private static Quill plugin = Quill.getPlugin(Quill.class);

    public static class LocationFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 3 && args.size() != 4) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "location()", "3 or 4", "location(x, y, z) or location(x, y, z, world)"));
            }
            
            double x = args.get(0).asNumber();
            double y = args.get(1).asNumber();
            double z = args.get(2).asNumber();
            
            World world;
            if (args.size() == 4) {
                String worldName = args.get(3).asString();
                world = Bukkit.getWorld(worldName);
                if (world == null) {
                    throw new RuntimeException(plugin.translate("quill.error.user.world.world-not-found", worldName));
                }
            } else {
                ScopeContext.Region region = scope.getRegion();
                if (region == null) {
                    throw new RuntimeException(plugin.translate("quill.error.user.world.no-region-defined"));
                }
                world = Bukkit.getWorld(region.getWorldName());
                if (world == null) {
                    throw new RuntimeException(plugin.translate("quill.error.user.world.world-not-found", region.getWorldName()));
                }
            }
            
            return new LocationValue(new Location(world, x, y, z));
        }
    }
    
    public static class ItemFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() < 1 || args.size() > 3) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "item()", "1 to 3", "item(item_id), item(item_id, amount), or item(item_id, amount, metadata)"));
            }
            
            String itemId = args.get(0).asString();
            int amount = args.size() >= 2 ? (int) args.get(1).asNumber() : 1;
            
            ItemStack item = createItemStack(itemId, amount);
            
            if (args.size() == 3) {
                applyMetadata(item, args.get(2));
            }
            
            return new ItemValue(item);
        }
        
        private void applyMetadata(ItemStack item, QuillValue metadataValue) {
            if (!metadataValue.isMap()) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "map/dictionary", "item()", metadataValue.getType()));
            }
            
            var metadata = metadataValue.asMap();
            var itemMeta = item.getItemMeta();
            
            if (itemMeta == null) {
                throw new RuntimeException(plugin.translate("quill.error.user.item.no-metadata-support", item.getType().name()));
            }
            
            if (metadata.containsKey("name")) {
                QuillValue nameValue = metadata.get("name");
                if (nameValue.isString()) {
                    itemMeta.displayName(Component.text(nameValue.asString()));
                }
            }
            
            if (metadata.containsKey("lore")) {
                QuillValue loreValue = metadata.get("lore");
                if (loreValue.isList()) {
                    List<Component> lore = new ArrayList<>();
                    for (QuillValue line : loreValue.asList()) {
                        if (line.isString()) {
                            lore.add(Component.text(line.asString()));
                        }
                    }
                    itemMeta.lore(lore);
                }
            }
            
            if (metadata.containsKey("enchantments")) {
                QuillValue enchValue = metadata.get("enchantments");
                if (enchValue.isMap()) {
                    var enchMap = enchValue.asMap();
                    for (var entry : enchMap.entrySet()) {
                        String enchName = entry.getKey();
                        int level = (int) entry.getValue().asNumber();
                        
                        NamespacedKey key = enchName.contains(":")
                                ? NamespacedKey.fromString(enchName)
                                : NamespacedKey.minecraft(enchName);

                        Enchantment ench = RegistryAccess.registryAccess()
                                .getRegistry(RegistryKey.ENCHANTMENT)
                                .get(key);

                        if (ench != null) {
                            itemMeta.addEnchant(ench, level, true);
                        }
                    }
                }
            }
            
            if (metadata.containsKey("unbreakable")) {
                QuillValue unbreakableValue = metadata.get("unbreakable");
                if (unbreakableValue.isBoolean()) {
                    itemMeta.setUnbreakable(unbreakableValue.asBoolean());
                }
            }
            
            if (metadata.containsKey("custom_model_data")) {
                QuillValue cmdValue = metadata.get("custom_model_data");
                if (cmdValue.isNumber()) {
                    CustomModelDataComponent cmdComponent = itemMeta.getCustomModelDataComponent();
                    cmdComponent.setFloats(List.of((float) cmdValue.asNumber()));
                    itemMeta.setCustomModelDataComponent(cmdComponent);
                }
            }
            
            item.setItemMeta(itemMeta);
        }
        
        private ItemStack createItemStack(String itemId, int amount) {
            if (itemId == null || itemId.isEmpty()) {
                throw new RuntimeException(plugin.translate("quill.error.user.item.empty-item-id"));
            }
            
            Material material;
            
            if (itemId.contains(":")) {
                String[] parts = itemId.split(":", 2);
                String namespace = parts[0];
                String materialName = parts[1].toUpperCase();
                
                if ("minecraft".equalsIgnoreCase(namespace)) {
                    try {
                        material = Material.valueOf(materialName);
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException(plugin.translate("quill.error.user.item.invalid-item-id", itemId));
                    }
                } else {
                    throw new RuntimeException(plugin.translate("quill.error.user.item.custom-namespace-not-supported", namespace));
                }
            } else {
                try {
                    material = Material.valueOf(itemId.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(plugin.translate("quill.error.user.item.invalid-item-id", itemId));
                }
            }
            
            if (amount < 1) {
                throw new RuntimeException(plugin.translate("quill.error.user.item.under-1-item", amount));
            }
            if (amount > 64) {
                amount = Math.min(amount, material.getMaxStackSize());
            }
            
            return new ItemStack(material, amount);
        }
    }
}