package me.kmathers.quill.interpreter;

import me.kmathers.quill.Quill;
import me.kmathers.quill.interpreter.QuillValue.*;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Set;

/**
 * Built-in player functions for Quill.
 */
public class BuiltInPlayerFuncs {
    private static Quill plugin = Quill.getPlugin(Quill.class);
    // === Movement ===

    public static class TeleportFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2 && args.size() != 4) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "teleport()", "2 or 4", "teleport(player, x, y, z) or teleport(player, location)"));
            }
            
            Player player = args.get(0).asPlayer();
            
            if (args.size() == 2) {
                Location loc = args.get(1).asLocation();
                player.teleport(loc);
            } else {
                double x = args.get(1).asNumber();
                double y = args.get(2).asNumber();
                double z = args.get(3).asNumber();
                Location loc = new Location(player.getWorld(), x, y, z);
                player.teleport(loc);
            }
            
            return new BooleanValue(true);
        }
    }

    // === Inventory Management ===

    public static class GiveFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() < 2 || args.size() > 3) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "give()", "2 or 3", "give(player, item_id) or give(player, item_id, amount) or give(player, item)"));
            }
            
            Player player = args.get(0).asPlayer();
            ItemStack item = null;
            int amount = 1;
            String itemId = null;

            if (args.size() == 2) {
                QuillValue second = args.get(1);
                if (second.isString()) {
                    itemId = second.asString();
                } else if (second.isItem()) {
                    item = second.asItem();
                } else {
                    throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "string or item", "give()", second.getType()));
                }
            } else {
                itemId = args.get(1).asString();
                amount = (int) args.get(2).asNumber();
            }
            
            if (item == null) {
                item = createItemStack(itemId, amount);
            }

            player.give(item);

            return new BooleanValue(true);
        }
    }

    public static class RemoveItemFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() < 2 || args.size() > 3) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "remove_item()", "2 or 3", "remove_item(player, item_id) or remove_item(player, item_id, amount)"));
            }
            
            Player player = args.get(0).asPlayer();
            int amount = 1;
            String itemId = null;
            Material itemMaterial = null;
            ItemStack item = null;

            if (args.size() == 2) {
                QuillValue second = args.get(1);
                if (second.isString()) {
                    itemId = second.asString();
                } else {
                    throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "string", "remove_item()", second.getType()));
                }
            } else {
                itemId = args.get(1).asString();
                amount = (int) Math.floor(args.get(2).asNumber());
            }

            itemMaterial = Material.matchMaterial(itemId);
            item = createItemStack(itemId, amount);

            if(player.getInventory().contains(itemMaterial)) {
                player.getInventory().remove(item);
            } else {
                return new NumberValue(0);
            }
            
            return new NumberValue(amount);
        }
    }

    // === Player State ===

    public static class SetGamemodeFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "set_gamemode()", "2", "set_gamemode(player, mode)"));
            }
            
            Player player = args.get(0).asPlayer();
            String gamemode = args.get(1).asString();

            Set<String> validModes = Set.of("adventure", "creative", "spectator", "survival");

            if (!validModes.contains(gamemode.toLowerCase())) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "one of ['adventure', 'creative', 'spectator', 'survival']", "set_gamemode()", gamemode));
            }

            player.setGameMode(GameMode.valueOf(gamemode.toUpperCase()));
            
            return new BooleanValue(true);
        }
    }

    public static class SetHealthFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "set_health()", "2", "set_health(player, health)"));
            }
            
            Player player = args.get(0).asPlayer();

            double health = args.get(1).asNumber();

            if (health < 0 || health > 20) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "digit between 0 and 20", "set_health()", args.get(1).asString()));
            }

            player.setHealth(health);

            return new BooleanValue(true);
        }
    }

    public static class SetHungerFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "set_hunger()", "2", "set_hunger(player, hunger)"));
            }
            
            Player player = args.get(0).asPlayer();

            double hunger = args.get(1).asNumber();

            if (hunger < 0 || hunger > 20) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "digit between 0 and 20", "set_hunger()", args.get(1).asString()));
            }

            player.setFoodLevel((int) hunger);

            return new BooleanValue(true);
        }
    }

    public static class HealFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "heal()", "heal(player)"));
            }
            
            Player player = args.get(0).asPlayer();
            AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);

            player.heal(maxHealth.getValue());

            return new BooleanValue(true);
        }
    }

    public static class KillFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "kill()", "kill(player)"));
            }
            
            Player player = args.get(0).asPlayer();

            player.setHealth(0);

            return new BooleanValue(true);
        }
    }

    // === Communication ===

    public static class SendMessageFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "sendmessage()", "2", "sendmessage(player, message)"));
            }
            
            Player player = args.get(0).asPlayer();
            String message = args.get(1).asString();

            player.sendMessage(message);
            
            return new BooleanValue(true);
        }
    }

    public static class SendTitleFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 6) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "sendtitle()", "6", "sendmessage(player, title, subtitle, fade_in, stay, fade_out)"));
            }
            
            Player player = args.get(0).asPlayer();
            String title = args.get(1).asString();
            String subtitle = args.get(2).asString();
            int fade_in = (int) args.get(3).asNumber();
            int stay = (int) args.get(4).asNumber();
            int fade_out = (int) args.get(5).asNumber();

            Title playerTitle = Title.title(Component.text(title), Component.text(subtitle), fade_in, stay, fade_out);

            player.showTitle(playerTitle);

            return new BooleanValue(true);
        }
    }

    public static class PlaySoundFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 4) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "playsound()", "4", "playsound(player, sound, volume, pitch)"));
            }
            
            Player player = args.get(0).asPlayer();
            String soundName = args.get(1).asString();
            double volume = args.get(2).asNumber();
            double pitch = args.get(3).asNumber();

            if (volume < 0) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "positive digit volume", "playsound()", String.valueOf(volume)));
            }

            if (pitch < 0 || pitch > 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "digit between 0 and 2", "playsound()", String.valueOf(pitch)));
            }

            Key soundKey;
            if (soundName.contains(":")) {
                soundKey = Key.key(soundName);
            } else {
                soundKey = Key.key("minecraft", soundName);
            }

            try {
                Sound sound = Sound.sound(soundKey, Sound.Source.MASTER, (float) volume, (float) pitch);
                player.playSound(sound);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(plugin.translate("quill.error.user.misc.invalid-sound", soundName));
            }

            return new BooleanValue(true);
        }
    }

    // === Effects ===

    public static class GiveEffectFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 4) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "give_effect()", "4", "give_effect(player, effect, duration, amplifier)"));
            }

            Player player = args.get(0).asPlayer();
            String effectString = args.get(1).asString().toLowerCase();
            int duration = (int) args.get(2).asNumber();
            int amplifier = (int) args.get(3).asNumber();

            NamespacedKey key = effectString.contains(":")
                    ? NamespacedKey.fromString(effectString)
                    : NamespacedKey.minecraft(effectString);

            PotionEffectType effectType = Registry.EFFECT.get(key);
            if (effectType == null) {
                throw new RuntimeException(plugin.translate("quill.error.user.misc.invalid-potion", effectString));
            }

            player.addPotionEffect(new PotionEffect(effectType, duration, amplifier));
            return new BooleanValue(true);
        }
    }

    public static class RemoveEffectFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "remove_effect()", "2", "remove_effect(player, effect)"));
            }

            Player player = args.get(0).asPlayer();
            String effectString = args.get(1).asString().toLowerCase();

            NamespacedKey key = effectString.contains(":")
                    ? NamespacedKey.fromString(effectString)
                    : NamespacedKey.minecraft(effectString);

            PotionEffectType effectType = Registry.EFFECT.get(key);
            if (effectType == null) {
                throw new RuntimeException("Invalid potion effect: " + effectString);
            }

            player.removePotionEffect(effectType);
            return new BooleanValue(true);
        }
    }

    public static class ClearEffectsFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "clear_effects()", "clear_effects(player)"));
            }
            
            Player player = args.get(0).asPlayer();

            player.clearActivePotionEffects();

            return new BooleanValue(true);
        }
    }

    // === Player Management ===

    public static class SetFlyingFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "set_flying()", "2", "set_flying(player, flying)"));
            }
            
            Player player = args.get(0).asPlayer();
            boolean flying = args.get(1).asBoolean();

            player.setFlying(flying);

            return new BooleanValue(true);
        }
    }

    public static class KickFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "kick()", "2", "kick(player, reason)"));
            }
            
            Player player = args.get(0).asPlayer();
            String reason = args.get(1).asString();

            player.kick(Component.text(reason));

            return new BooleanValue(true);
        }
    }

    // === Getters ===

    public static class GetHealthFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "get_health()", "get_health(player)"));
            }
            
            Player player = args.get(0).asPlayer();

            double health = player.getHealth();

            return new NumberValue(health);
        }
    }

    public static class GetHungerFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "get_hunger()", "get_hunger(player)"));
            }
            
            Player player = args.get(0).asPlayer();

            double hunger = player.getFoodLevel();

            return new NumberValue(hunger);
        }
    }

    public static class GetLocationFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "get_location()", "get_location(player)"));
            }
            
            Player player = args.get(0).asPlayer();

            Location loc = player.getLocation();

            return new LocationValue(loc);
        }
    }

    public static class GetGamemodeFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "get_gamemode()", "get_gamemode(player)"));
            }
            
            Player player = args.get(0).asPlayer();

            String gamemode = player.getGameMode().toString();

            return new StringValue(gamemode);
        }
    }

    public static class HasItemFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() < 2 || args.size() > 3) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "has_item()", "2 or 3", "has_item(player, item_id) or has_item(player, item_id, amount)"));
            }
            
            Player player = args.get(0).asPlayer();
            int amount = 1;
            String itemId = null;
            Material itemMaterial = null;

            if (args.size() == 2) {
                QuillValue second = args.get(1);
                if (second.isString()) {
                    itemId = second.asString();
                } else {
                    throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "string", "has_item()", args.get(1).getType()));
                }
            } else {
                itemId = args.get(1).asString();
                amount = (int) Math.floor(args.get(2).asNumber());
            }

            itemMaterial = Material.matchMaterial(itemId);

            if(player.getInventory().contains(itemMaterial, amount)) {
                return new BooleanValue(true);
            } else {
                return new BooleanValue(false);
            }
        }
    }    

    public static class GetNameFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "get_name()", "get_name(player)"));
            }
            
            Player player = args.get(0).asPlayer();

            return new StringValue(player.getName());
        }
    }    

    public static class IsOnlineFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "is_online()", "is_online(player)"));
            }
            
            Player player = args.get(0).asPlayer();

            return new BooleanValue(player.isOnline());
        }
    }

    public static class IsOpFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "is_op()", "is_op(player)"));
            }
            
            Player player = args.get(0).asPlayer();

            return new BooleanValue(player.isOp());
        }
    }

    // === Helpers ===
    private static ItemStack createItemStack(String itemId, int amount) {
        if (itemId == null || itemId.isEmpty()) {
            throw new RuntimeException(plugin.translate("quill.error.user.item.empty-item-id"));
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
            throw new RuntimeException(plugin.translate("quill.error.user.item.invalid-item-id", itemId));
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