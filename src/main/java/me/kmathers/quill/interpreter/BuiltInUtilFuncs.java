package me.kmathers.quill.interpreter;

import me.kmathers.quill.Quill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import me.kmathers.quill.interpreter.QuillValue.BooleanValue;
import me.kmathers.quill.interpreter.QuillValue.NumberValue;
import me.kmathers.quill.interpreter.QuillValue.PlayerValue;
import me.kmathers.quill.interpreter.QuillValue.StringValue;
import me.kmathers.quill.interpreter.QuillValue.ListValue;
import me.kmathers.quill.interpreter.QuillValue.MapValue;

/**
 * Built-in utility functions for Quill.
 */
public class BuiltInUtilFuncs {
    private static Random random = new Random();
    private static Quill plugin = Quill.getPlugin(Quill.class);

    public static class CancelFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "cancel()", "cancel(event)"));
            }
            
            if (!args.get(0).isEvent()) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "event", "cancel()", args.get(0).getType()));
            }
            
            org.bukkit.event.Event event = args.get(0).asEvent();
            
            if (event instanceof org.bukkit.event.Cancellable) {
                ((org.bukkit.event.Cancellable) event).setCancelled(true);
                return new BooleanValue(true);
            } else {
                throw new RuntimeException(plugin.translate("quill.error.developer.misc.cannot-cancel", event.getEventName()));
            }
        }
    }

    public static class WaitFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "wait()", "wait(ticks)"));
            }
            
            // TODO: Handle wait
            int ticks = (int) args.get(0).asNumber();
            
            if (ticks < 0) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "non-negative digit", "wait()", String.valueOf(ticks)));
            }
            
            return new BooleanValue(true);
        }
    }

    public static class RandomFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() < 1 || args.size() > 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "random()", "1 or 2", "random(max) or random(min, max)"));
            }

            double min = 0;
            double max = 0;

            if(args.size() == 1) {
                max = args.get(0).asNumber();
            } else {
                min = args.get(0).asNumber();
                max = args.get(1).asNumber();
            }

            double choice = random.nextDouble(min, max);
            return new NumberValue(choice);
        }
    }

    public static class RoundFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1 ) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "round()", "round(number)"));
            }

            return new NumberValue((double) Math.round(args.get(0).asNumber()));
        }
    }

    public static class FloorFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1 ) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "floor()", "floor(number)"));
            }

            return new NumberValue((double) Math.floor(args.get(0).asNumber()));
        }
    }

    public static class CeilFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1 ) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "ceil()", "ceil(number)"));
            }

            return new NumberValue((double) Math.ceil(args.get(0).asNumber()));
        }
    }

    public static class AbsFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1 ) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "abs()", "abs(number)"));
            }

            return new NumberValue((double) Math.abs(args.get(0).asNumber()));
        }
    }

    public static class SqrtFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1 ) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "sqrt()", "sqrt(number)"));
            }

            return new NumberValue((double) Math.sqrt(args.get(0).asNumber()));
        }
    }

    public static class PowFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2 ) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "pow()", "2", "pow(number, exponent)"));
            }

            return new NumberValue((double) Math.pow(args.get(0).asNumber(), args.get(1).asNumber()));
        }
    }

    public static class DistanceFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2 && args.size() != 6) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "distance()", "2 or 6", "distance(location, location) or distance(x1, y1, z1, x2, y2, z2)"));
            }
            Location loc1;
            Location loc2;

            if (args.size() == 2) {
                loc1 = args.get(0).asLocation();
                loc2 = args.get(1).asLocation();
            } else {
                double x1 = args.get(0).asNumber();
                double y1 = args.get(1).asNumber();
                double z1 = args.get(2).asNumber();
                double x2 = args.get(3).asNumber();
                double y2 = args.get(4).asNumber();
                double z2 = args.get(5).asNumber();
                
                World world = BuiltInWorldFuncs.getWorld(scope);

                loc1 = new Location(world, x1, y1, z1);
                loc2 = new Location(world, x2, y2, z2);
            }

            return new NumberValue(loc1.distance(loc2));
        }
    }

    public static class LogFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "log()", "log(message)"));
            }

            String scopeName = scope.getName();
            ScopeContext current = scope;
            while ("anonymous".equals(scopeName) && current.getParent() != null) {
                current = current.getParent();
                scopeName = current.getName();
            }

            Bukkit.getLogger().info("[Quill] (" + scopeName + ") " + args.get(0).toString());

            return new BooleanValue(true);
        }
    }

    public static class TriggerCustomFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "trigger_custom()", "2", "trigger_custom(event_name, data)"));
            }
            
            String eventName = args.get(0).asString();
            QuillValue dataValue = args.get(1);
            
            Bukkit.getLogger().info("Custom event in scope '" + scope.getName() + "' triggered: " + eventName);
            
            try {
                Map<String, QuillValue> context = new HashMap<>();
                
                if (dataValue.isString()) {
                    // Parse JSON string into a map
                    JsonObject jsonData = JsonParser.parseString(dataValue.asString()).getAsJsonObject();
                    for (String key : jsonData.keySet()) {
                        JsonElement value = jsonData.get(key);
                        context.put(key, jsonElementToQuillValue(value));
                        Bukkit.getLogger().info("  " + key + ": " + value.toString());
                    }
                } else if (dataValue.isMap()) {
                    // Direct map conversion
                    context.putAll(dataValue.asMap());
                    for (Map.Entry<String, QuillValue> entry : context.entrySet()) {
                        Bukkit.getLogger().info("  " + entry.getKey() + ": " + entry.getValue().toString());
                    }
                } else {
                    throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "map or JSON string", "trigger_custom()", dataValue.getType()));
                }
                
                // Get the permission scope to pass to event bridge
                me.kmathers.quill.utils.Scope permScope = null;
                if (!scope.getName().equals("global")) {
                    permScope = plugin.getScopeManager().getScope(scope.getName());
                }
                
                // Trigger the event with the context and scope
                plugin.getEventBridge().triggerForAllScripts(eventName, context, permScope);
                
            } catch (Exception e) {
                Bukkit.getLogger().warning("Failed to process custom event data: " + e.getMessage());
                throw new RuntimeException("Failed to trigger custom event: " + e.getMessage());
            }
            
            return new BooleanValue(true);
        }
    }

    public static class GetPlayerFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "get_player()", "get_player(name)"));
            }

            Player player = Bukkit.getPlayer(args.get(0).asString());
            
            if (player == null) {
                return QuillValue.NullValue.INSTANCE;
            }
            
            return new PlayerValue(player);
        }
    }

    public static class GetOnlinePlayersFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 0) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "get_online_players()", "0", "get_online_players()"));
            }

            List<QuillValue> players = Bukkit.getOnlinePlayers().stream().map(PlayerValue::new).collect(Collectors.toList());
            
            return new ListValue(players);
        }
    }

    public static class LenFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "len()", "len(list) or len(string)"));
            }

            if (args.get(0).isList()) {
                return new NumberValue(args.get(0).asList().size());
            } else if (args.get(0).isString()) {
                return new NumberValue(args.get(0).asString().length());
                } else {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "list or string", "len()", args.get(0).getType()));
            }
        }
    }

    public static class AppendFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "append()", "2", "append(list, item)"));
            }

            if (args.get(0).isList()) {
                List<QuillValue> list = args.get(0).asList();
                list.add(args.get(1));
                return new BooleanValue(true);
            } else {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "list", "append()", args.get(0).getType()));
            }
        }
    }

    public static class RemoveFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "remove()", "2", "remove(list, index)"));
            }

            if (args.get(0).isList()) {
                if (args.get(1).isNumber()) {
                    List<QuillValue> list = args.get(0).asList();
                    int index = (int) args.get(1).asNumber();
                    
                    if (index < 0 || index >= list.size()) {
                        throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "index between 0 and " + (list.size()-1), "remove()", String.valueOf(index)));
                    }
                    
                    list.remove(index);
                    return new BooleanValue(true);
                } else {
                    throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "number", "remove()", args.get(1).getType()));
                }
            } else {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "list", "remove()", args.get(0).getType()));
            }
        }
    }

    public static class ContainsFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "contains()", "2", "contains(list, item) or contains(string, substring)"));
            }

            if (args.get(0).isList()) {
                List<QuillValue> list = args.get(0).asList();
                QuillValue searchItem = args.get(1);
                
                for (QuillValue item : list) {
                    if (valuesEqual(item, searchItem)) {
                        return new BooleanValue(true);
                    }
                }
                return new BooleanValue(false);
            } else if (args.get(0).isString()) {
                return new BooleanValue(args.get(0).asString().contains(args.get(1).asString()));
            } else {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "list or string", "contains()", args.get(0).getType()));
            }
        }
        
        private boolean valuesEqual(QuillValue a, QuillValue b) {
            if (a.getType() != b.getType()) return false;
            
            if (a.isNumber()) return a.asNumber() == b.asNumber();
            if (a.isString()) return a.asString().equals(b.asString());
            if (a.isBoolean()) return a.asBoolean() == b.asBoolean();
            if (a.isNull()) return b.isNull();
            
            return a.getValue() == b.getValue();
        }
    }

    public static class SplitFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "split()", "2", "split(string, delimiter)"));
            }

            String str = args.get(0).asString();
            String delimiter = args.get(1).asString();
            
            return new ListValue(
                Arrays.stream(str.split(java.util.regex.Pattern.quote(delimiter)))
                    .map(StringValue::new)
                    .collect(Collectors.toList())
            );
        }
    }

    public static class JoinFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "join()", "2", "join(list, delimiter)"));
            }

            List<QuillValue> list = args.get(0).asList();
            String delimiter = args.get(1).asString();
            
            return new StringValue(
                list.stream()
                    .map(v -> v.toString())
                    .collect(Collectors.joining(delimiter))
            );
        }
    }

    public static class ToStringFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "to_string()", "to_string(value)"));
            }

            return new StringValue(args.get(0).toString());
        }
    }

    public static class ToNumberFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "to_number()", "to_number(value)"));
            }

            QuillValue val = args.get(0);
            
            if (val.isNumber()) {
                return val;
            } else if (val.isString()) {
                try {
                    return new NumberValue(Double.parseDouble(val.asString()));
                } catch (NumberFormatException e) {
                    throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "number parsable string", "to_number()", val.asString()));
                }
            } else if (val.isBoolean()) {
                return new NumberValue(val.asBoolean() ? 1.0 : 0.0);
            } else {
                throw new RuntimeException(plugin.translate("quill.error.developer.misc.cannot-convert", val.getType(), "number"));
            }
        }
    }

    public static class ToBooleanFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "to_boolean()", "to_boolean(value)"));
            }

            return new BooleanValue(args.get(0).isTruthy());
        }
    }

    public static class TypeOfFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "type_of()", "type_of(value)"));
            }

            return new StringValue(args.get(0).getType().toString().toLowerCase());
        }
    }

    public static class RangeFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "range()", "2", "range(start, end)"));
            }

            int start = (int) args.get(0).asNumber();
            int end = (int) args.get(1).asNumber();
            
            List<QuillValue> values = new ArrayList<>();
            if (start <= end) {
                for (int i = start; i < end; i++) {
                    values.add(new NumberValue(i));
                }
            } else {
                for (int i = start; i > end; i--) {
                    values.add(new NumberValue(i));
                }
            }
            
            return new ListValue(values);
        }
    }

    public static class RandomChoiceFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "random_choice()", "random_choice(list)"));
            }

            List<QuillValue> list = args.get(0).asList();
            
            if (list.isEmpty()) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.expected", "non-empty list", "random_choice()", "empty"));
            }
            
            int index = random.nextInt(list.size());
            return list.get(index);
        }
    }

    private static JsonObject convertMapToJson(Map<String, QuillValue> map) {
        JsonObject json = new JsonObject();
        
        for (Map.Entry<String, QuillValue> entry : map.entrySet()) {
            String key = entry.getKey();
            QuillValue value = entry.getValue();
            
            json.add(key, quillValueToJson(value));
        }
        
        return json;
    }
    
    private static JsonElement quillValueToJson(QuillValue value) {
        if (value.isNull()) {
            return com.google.gson.JsonNull.INSTANCE;
        } else if (value.isNumber()) {
            return new com.google.gson.JsonPrimitive(value.asNumber());
        } else if (value.isString()) {
            return new com.google.gson.JsonPrimitive(value.asString());
        } else if (value.isBoolean()) {
            return new com.google.gson.JsonPrimitive(value.asBoolean());
        } else if (value.isList()) {
            com.google.gson.JsonArray array = new com.google.gson.JsonArray();
            for (QuillValue item : value.asList()) {
                array.add(quillValueToJson(item));
            }
            return array;
        } else if (value.isMap()) {
            return convertMapToJson(value.asMap());
        } else if (value.isLocation()) {
            Location loc = value.asLocation();
            JsonObject locJson = new JsonObject();
            locJson.addProperty("x", loc.getX());
            locJson.addProperty("y", loc.getY());
            locJson.addProperty("z", loc.getZ());
            locJson.addProperty("world", loc.getWorld().getName());
            return locJson;
        } else if (value.isPlayer()) {
            return new JsonPrimitive(value.asPlayer().getName());
        } else {
            return new JsonPrimitive(value.toString());
        }
    }

    private static QuillValue jsonElementToQuillValue(JsonElement element) {
        if (element.isJsonNull()) {
            return QuillValue.NullValue.INSTANCE;
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isNumber()) {
                return new NumberValue(primitive.getAsDouble());
            } else if (primitive.isBoolean()) {
                return new BooleanValue(primitive.getAsBoolean());
            } else if (primitive.isString()) {
                return new StringValue(primitive.getAsString());
            }
        } else if (element.isJsonArray()) {
            com.google.gson.JsonArray array = element.getAsJsonArray();
            List<QuillValue> list = new ArrayList<>();
            for (JsonElement item : array) {
                list.add(jsonElementToQuillValue(item));
            }
            return new ListValue(list);
        } else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            Map<String, QuillValue> map = new HashMap<>();
            for (String key : obj.keySet()) {
                map.put(key, jsonElementToQuillValue(obj.get(key)));
            }
            return new MapValue(map);
        }
        return QuillValue.NullValue.INSTANCE;
    }
}
