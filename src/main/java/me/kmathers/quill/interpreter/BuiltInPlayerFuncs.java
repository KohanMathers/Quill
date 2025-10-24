package me.kmathers.quill.interpreter;

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
    // === Movement ===

    public static class TeleportFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2 && args.size() != 4) {
                throw new RuntimeException("teleport() requires 2 or 4 arguments: teleport(player, x, y, z) or teleport(player, location)");
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
                throw new RuntimeException("give() requires 2 or 3 arguments:\n- give(player, item_id)\n- give(player, item_id, amount)\n- give(player, item)");
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
                    throw new RuntimeException("Expected string or item in give(), found: " + second.getType());
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
                throw new RuntimeException("remove_item() requires 2 or 3 arguments:remove_item(player, item_id) or remove_item(player, item_id, amount)");
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
                    throw new RuntimeException("Expected string or item in remove_item(), found: " + second.getType());
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
                throw new RuntimeException("set_gamemode() requires 2 arguments:set_gamemode(player, mode)");
            }
            
            Player player = args.get(0).asPlayer();
            String gamemode = args.get(1).asString();

            Set<String> validModes = Set.of("adventure", "creative", "spectator", "survival");

            if (!validModes.contains(gamemode.toLowerCase())) {
                throw new RuntimeException("Expected one of ['adventure', 'creative', 'spectator', 'survival'] in set_gamemode(), found: " + gamemode);
            }

            player.setGameMode(GameMode.valueOf(gamemode.toUpperCase()));
            
            return new BooleanValue(true);
        }
    }

    public static class SetHealthFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("set_health() requires 2 arguments:set_health(player, health)");
            }
            
            Player player = args.get(0).asPlayer();

            double health = args.get(1).asNumber();

            if (health < 0 || health > 20) {
                throw new RuntimeException("Expected digit between 0 and 20 in set_health(), found: " + args.get(1).asString());
            }

            player.setHealth(health);

            return new BooleanValue(true);
        }
    }

    public static class SetHungerFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("set_hunger() requires 2 arguments:set_hunger(player, hunger)");
            }
            
            Player player = args.get(0).asPlayer();

            double hunger = args.get(1).asNumber();

            if (hunger < 0 || hunger > 20) {
                throw new RuntimeException("Expected digit between 0 and 20 in set_hunger(), found: " + args.get(1).asString());
            }

            player.setFoodLevel((int) hunger);

            return new BooleanValue(true);
        }
    }

    public static class HealFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("heal() requires 1 argument: heal(player)");
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
                throw new RuntimeException("kill() requires 1 argument: kill(player)");
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
                throw new RuntimeException("sendmessage() requires 2 arguments:sendmessage(player, message)");
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
                throw new RuntimeException("sendtitle() requires 6 arguments:sendmessage(player, title, subtitle, fade_in, stay, fade_out)");
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
                throw new RuntimeException("playsound() requires 4 arguments:playsound(player, sound, volume, pitch)");
            }
            
            Player player = args.get(0).asPlayer();
            String soundName = args.get(1).asString();
            double volume = args.get(2).asNumber();
            double pitch = args.get(3).asNumber();

            if (volume < 0 || volume > 1) {
                throw new RuntimeException("Expected digit between 0 and 1 in playsound(), found: " + args.get(2).asString());
            }

            if (pitch < 0 || pitch > 1) {
                throw new RuntimeException("Expected digit between 0 and 1 in playsound(), found: " + args.get(3).asString());
            }

            if (!soundName.contains(":")) {
                soundName = "minecraft:" + soundName;
            }

            try {
                Sound sound = Sound.sound(Key.key("minecraft", soundName), Sound.Source.MASTER, (float) volume, (float) pitch);
                player.playSound(sound);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Expected valid sound in playsound(), found: " + soundName);
            }

            return new BooleanValue(true);
        }
    }

    // === Effects ===

    public static class GiveEffectFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 4) {
                throw new RuntimeException("give_effect() requires 4 arguments: give_effect(player, effect, duration, amplifier)");
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
                throw new RuntimeException("Invalid potion effect: " + effectString);
            }

            player.addPotionEffect(new PotionEffect(effectType, duration, amplifier));
            return new BooleanValue(true);
        }
    }

    public static class RemoveEffectFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("remove_effect() requires 2 arguments: remove_effect(player, effect)");
            }

            Player player = args.get(0).asPlayer();
            String effectString = args.get(1).asString().toLowerCase();

            NamespacedKey key = effectString.contains(":")
                    ? NamespacedKey.fromString(effectString)
                    : NamespacedKey.minecraft(effectString);

            PotionEffectType effectType = Registry.EFFECT.get(key);
            if (effectType == null) {
                throw new RuntimeException("Expected valid potion effect in remove_effect, found: " + effectString);
            }

            player.removePotionEffect(effectType);
            return new BooleanValue(true);
        }
    }

    public static class ClearEffectsFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("heal() requires 1 argument: heal(player)");
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
                throw new RuntimeException("set_flying() requires 2 arguments: set_flying(player, flying)");
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
                throw new RuntimeException("kick() requires 2 arguments: kick(player, reason)");
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
                throw new RuntimeException("get_health() requires 1 argument: get_health(player)");
            }
            
            Player player = args.get(0).asPlayer();

            double health = player.getHealth();

            return new NumberValue(health);
        }
    }

    public static class GetHungerFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("get_hunger() requires 1 argument: get_hunger(player)");
            }
            
            Player player = args.get(0).asPlayer();

            double hunger = player.getFoodLevel();

            return new NumberValue(hunger);
        }
    }

    public static class GetLocationFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("get_location() requires 1 argument: get_location(player)");
            }
            
            Player player = args.get(0).asPlayer();

            Location loc = player.getLocation();

            return new LocationValue(loc);
        }
    }

    public static class GetGamemodeFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("get_gamemode() requires 1 argument: get_gamemode(player)");
            }
            
            Player player = args.get(0).asPlayer();

            String gamemode = player.getGameMode().toString();

            return new StringValue(gamemode);
        }
    }

    public static class HasItemFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() < 2 || args.size() > 3) {
                throw new RuntimeException("has_item() requires 2 or 3 arguments:has_item(player, item_id) or has_item(player, item_id, amount)");
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
                    throw new RuntimeException("Expected string in has_item(), found: " + args.get(1).getType());
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
                throw new RuntimeException("get_name() requires 1 argument: get_name(player)");
            }
            
            Player player = args.get(0).asPlayer();

            return new StringValue(player.getName());
        }
    }    

    public static class IsOnlineFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("is_online() requires 1 argument: is_online(player)");
            }
            
            Player player = args.get(0).asPlayer();

            return new BooleanValue(player.isOnline());
        }
    }

    public static class IsOpFunction implements QuillInterpreter.BuiltInFunction {
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("is_op() requires 1 argument: is_op(player)");
            }
            
            Player player = args.get(0).asPlayer();

            return new BooleanValue(player.isOp());
        }
    }

    // === Helpers ===
    private static ItemStack createItemStack(String itemId, int amount) {
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
