package me.kmathers.quill.interpreter;

import me.kmathers.quill.Quill;

import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.kmathers.quill.interpreter.QuillValue.BooleanValue;
import me.kmathers.quill.interpreter.QuillValue.EntityValue;
import me.kmathers.quill.interpreter.QuillValue.NumberValue;
import me.kmathers.quill.interpreter.QuillValue.StringValue;
import me.kmathers.quill.interpreter.QuillValue.WorldValue;

/**
 * Built-in world functions for Quill.
 */
public class BuiltInWorldFuncs {
    private static Quill plugin = Quill.getPlugin(Quill.class);
    public static class SetBlockFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2 && args.size() != 4) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "set_block()", "2 or 4", "set_block(location, block_id) or set_block(x, y, z, block_id)"));
            }

            Location loc;
            Material mat;

            if (args.size() == 2) {
                loc = args.get(0).asLocation();
                mat = parseMaterial(args.get(1).asString());
            } else {
                double x = args.get(0).asNumber();
                double y = args.get(1).asNumber();
                double z = args.get(2).asNumber();
                
                World world = getWorld(scope);

                loc = new Location(world, x, y, z);
                mat = parseMaterial(args.get(3).asString());
            }

            loc.getBlock().setType(mat);
            return new BooleanValue(true);
        }
    }

    public static class GetBlockFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1 && args.size() != 3) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "get_block()", "1 or 3", "get_block(location) or get_block(x, y, z)"));
            }

            Location loc;

            if (args.size() == 1) {
                loc = args.get(0).asLocation();
            } else {
                double x = args.get(0).asNumber();
                double y = args.get(1).asNumber();
                double z = args.get(2).asNumber();
                
                World world = getWorld(scope);

                loc = new Location(world, x, y, z);
            }

            Block block = loc.getBlock();
            Material type = block.getType();

            return new StringValue(type.toString());
        }
    }

    public static class BreakBlockFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1 && args.size() != 3) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "break_block()", "1 or 3", "break_block(location) or break_block(x, y, z)"));
            }

            Location loc;

            if (args.size() == 1) {
                loc = args.get(0).asLocation();
            } else {
                double x = args.get(0).asNumber();
                double y = args.get(1).asNumber();
                double z = args.get(2).asNumber();
                
                World world = getWorld(scope);

                loc = new Location(world, x, y, z);
            }

            World world = getWorld(scope);
            Block block = loc.getBlock();
            Material type = block.getType();

            if (type != Material.AIR) {
                world.dropItemNaturally(loc, new ItemStack(type));
                block.setType(Material.AIR);
            }

            loc.getBlock().setType(Material.AIR);
            return new BooleanValue(true);
        }
    }

    public static class SpawnEntityFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2 && args.size() != 4) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "spawn_entity()", "2 or 4", "spawn_entity(entity_type, location) or set_block(entity_type, x, y, z)"));
            }

            Location loc;
            EntityType entType;
            World world = getWorld(scope);
            
            if (args.size() == 2) {
                loc = args.get(1).asLocation();
                entType = parseEntityType(args.get(0).asString());
            } else {
                double x = args.get(1).asNumber();
                double y = args.get(2).asNumber();
                double z = args.get(3).asNumber();
                
                loc = new Location(world, x, y, z);
                entType = parseEntityType(args.get(0).asString());
            }

            Entity entity = world.spawnEntity(loc, entType);
            return new EntityValue(entity);
        }
    }

    public static class RemoveEntityFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "remove_entity()", "remove_entity(entity)"));
            }

            Entity entity = args.get(0).asEntity();

            entity.remove();

            return new EntityValue(entity);
        }
    }

    public static class CreateExplosionFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (!(args.size() >= 2 && args.size() <= 5)) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "create_explosion()", "between 2 and 5", "create_explosion(location, power)\ncreate_explosion(location, power, fire)\ncreate_explosion(x, y, z, power)\ncreate_explosion(x, y, z, power, fire)"));
            }

            Location loc = null;
            double power = 0;
            boolean fire = false;

            World world = getWorld(scope);

            if (args.size() == 2) {
                loc = args.get(0).asLocation();
                power = validateExplosionPower(args.get(1).asNumber());
                fire = false;
            } else if (args.size() == 3) {
                loc = args.get(0).asLocation();
                power = validateExplosionPower(args.get(1).asNumber());
                fire = args.get(2).asBoolean();
            } else if (args.size() == 4) {
                double x = args.get(0).asNumber();
                double y = args.get(1).asNumber();
                double z = args.get(2).asNumber();

                loc = new Location(world, x, y, z);
                power = validateExplosionPower(args.get(3).asNumber());
                fire = false;
            }  else if (args.size() == 5) {
                double x = args.get(0).asNumber();
                double y = args.get(1).asNumber();
                double z = args.get(2).asNumber();

                loc = new Location(world, x, y, z);
                power = validateExplosionPower(args.get(3).asNumber());
                fire = args.get(4).asBoolean();
            }

            world.createExplosion(loc, (float) power, fire);

            return new BooleanValue(true);
        }
    }

    public static class StrikeLightningFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1 && args.size() != 3) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "strike_lightning()", "1 or 3", "strike_lightning(location) or strike_lightning(x, y, z)"));
            }

            Location loc;

            if (args.size() == 1) {
                loc = args.get(0).asLocation();
            } else {
                double x = args.get(0).asNumber();
                double y = args.get(1).asNumber();
                double z = args.get(2).asNumber();
                
                World world = getWorld(scope);

                loc = new Location(world, x, y, z);
            }

            World world = getWorld(scope);

            world.strikeLightning(loc);

            return new BooleanValue(true);
        }
    }

    public static class SetTimeFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "set_time()", "2", "set_time(world, time)"));
            }

            World world = args.get(0).asWorld();
            int time = (int) args.get(1).asNumber();

            if(time < 0 || time > 24000) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "digit between 0 and 24000", "set_time()", String.valueOf(time)));
            }

            world.setTime(time);

            return new BooleanValue(true);
        }
    }

    public static class GetTimeFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "get_time()", "get_time(world)"));
            }

            World world = args.get(0).asWorld();

            return new NumberValue(world.getTime());
        }
    }

    public static class SetWeatherFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 3) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "set_weather()", "3", "set_weather(world, weather, duration)"));
            }

            World world = args.get(0).asWorld();
            String weather = args.get(1).asString();
            int duration = (int) args.get(2).asNumber();

            Set<String> validWeather = Set.of("clear", "rain", "thunder");

            if (!validWeather.contains(weather.toLowerCase())) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "one of ['clear', 'rain', 'thunder']", "set_weather()", weather));
            }

            switch (weather.toLowerCase()) {
                case "clear" -> {
                    world.setStorm(false);
                    world.setThundering(false);
                    world.setWeatherDuration(duration);
                }
                case "rain" -> {
                    world.setStorm(true);
                    world.setThundering(false);
                    world.setWeatherDuration(duration);
                }
                case "thunder" -> {
                    world.setStorm(true);
                    world.setThundering(true);
                    world.setWeatherDuration(duration);
                }
            }

            return new BooleanValue(true);
        }
    }

    public static class GetWeatherFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "get_weather()", "get_weather(world)"));
            }

            World world = args.get(0).asWorld();

            String weather;
            if (world.isThundering()) {
                weather = "thunder";
            } else if (world.hasStorm()) {
                weather = "rain";
            } else {
                weather = "clear";
            }

            return new StringValue(weather);
        }
    }

    public static class GetWorldFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "get_world()", "get_world(world)"));
            }

            String worldString = args.get(0).asString();

            World world = Bukkit.getWorld(worldString);

            return new WorldValue(world);
        }
    }

    public static class BroadcastFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "broadcast()", "broadcast(message)"));
            }

            String message = args.get(0).asString();
            Set<Player> players = scope.getPlayers();

            for(Player player : players) {
                player.sendMessage(message);
            }

            return new BooleanValue(true);
        }
    }

    // === Helpers ===
    private static Material parseMaterial(String matString) {
        if (matString.contains(":")) {
            String[] parts = matString.split(":");
            matString = parts[1];
        }

        try {
            return Material.valueOf(matString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "valid material", "parseMaterial()", matString));
        }
    }

    private static EntityType parseEntityType(String entString) {
        if (entString.contains(":")) {
            String[] parts = entString.split(":");
            entString = parts[1];
        }

        try {
            return EntityType.valueOf(entString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "valid entity type", "parseEntityType()", entString));
        }
    }

    public static World getWorld(ScopeContext scope) {
        ScopeContext.Region region = scope.getRegion();
        if (region == null) {
            throw new RuntimeException(plugin.translate("quill.error.user.world.no-region-defined"));
        }
                
        World world = org.bukkit.Bukkit.getWorld(region.getWorldName());
        if (world == null) {
            throw new RuntimeException(plugin.translate("quill.error.user.world.world-not-found", region.getWorldName()));
        }
        return world;
    }

    private static double validateExplosionPower(double power) {
        if (power < 0 || power > 10) {
            throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "digit between 0 and 10", "create_explosion()", String.valueOf(power)));
        }
        return power;
    }
}