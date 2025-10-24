package me.kmathers.quill.interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import me.kmathers.quill.interpreter.QuillValue.BooleanValue;
import me.kmathers.quill.interpreter.QuillValue.NumberValue;
import me.kmathers.quill.interpreter.QuillValue.PlayerValue;
import me.kmathers.quill.interpreter.QuillValue.StringValue;
import me.kmathers.quill.interpreter.QuillValue.ListValue;

/**
 * Built-in utility functions for Quill.
 */
public class BuiltInUtilFuncs {
    private static Random random = new Random();

    public static class CancelFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("cancel() requires 1 argument: cancel(event)");
            }
            
            if (!args.get(0).isEvent()) {
                throw new RuntimeException("cancel() requires an event argument");
            }
            
            org.bukkit.event.Event event = args.get(0).asEvent();
            
            if (event instanceof org.bukkit.event.Cancellable) {
                ((org.bukkit.event.Cancellable) event).setCancelled(true);
                return new BooleanValue(true);
            } else {
                throw new RuntimeException("Event " + event.getEventName() + " is not cancellable");
            }
        }
    }

    public static class WaitFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("wait() requires 1 argument: wait(ticks)");
            }
            
            // TODO: Handle wait
            int ticks = (int) args.get(0).asNumber();
            
            if (ticks < 0) {
                throw new RuntimeException("Wait time must be non-negative, got: " + ticks);
            }
            
            return new BooleanValue(true);
        }
    }

    public static class RandomFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() < 1 || args.size() > 2) {
                throw new RuntimeException("random() requires 1 or 2 arguments: random(max) or random(min, max)");
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
                throw new RuntimeException("round() requires 1 argument: round(number)");
            }

            return new NumberValue((double) Math.round(args.get(0).asNumber()));
        }
    }

    public static class FloorFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1 ) {
                throw new RuntimeException("floor() requires 1 argument: floor(number)");
            }

            return new NumberValue((double) Math.floor(args.get(0).asNumber()));
        }
    }

    public static class CeilFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1 ) {
                throw new RuntimeException("ceil() requires 1 argument: ceil(number)");
            }

            return new NumberValue((double) Math.ceil(args.get(0).asNumber()));
        }
    }

    public static class AbsFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1 ) {
                throw new RuntimeException("abs() requires 1 argument: abs(number)");
            }

            return new NumberValue((double) Math.abs(args.get(0).asNumber()));
        }
    }

    public static class SqrtFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1 ) {
                throw new RuntimeException("sqrt() requires 1 argument: sqrt(number)");
            }

            return new NumberValue((double) Math.sqrt(args.get(0).asNumber()));
        }
    }

    public static class PowFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2 ) {
                throw new RuntimeException("pow() requires 2 arguments: pow(number, exponent)");
            }

            return new NumberValue((double) Math.pow(args.get(0).asNumber(), args.get(1).asNumber()));
        }
    }

    public static class DistanceFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2 && args.size() != 6) {
                throw new RuntimeException("distance() requires 2 or 6 arguments: distance(location, location) or distance(x1, y1, z1, x2, y2, z2)");
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
                throw new RuntimeException("log() requires 1 argument: log(message)");
            }

            Bukkit.getLogger().info("(" + scope.getName() + ") " + args.get(0).toString());

            return new BooleanValue(true);
        }
    }

    public static class TriggerCustomFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("trigger_custom() requires 2 arguments: trigger_custom(event_name, data)");
            }
            
            String eventName = args.get(0).asString();
            QuillValue data = args.get(1);
            
            Bukkit.getLogger().info("Custom event triggered: " + eventName + " with data: " + data.toString());
            
            return new BooleanValue(true);
        }
    }

    public static class GetPlayerFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("get_player() requires 1 argument: get_player(name)");
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
                throw new RuntimeException("get_online_players() requires 0 arguments: get_online_players()");
            }

            List<QuillValue> players = Bukkit.getOnlinePlayers().stream().map(PlayerValue::new).collect(Collectors.toList());
            
            return new ListValue(players);
        }
    }

    public static class LenFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("len() requires 1 argument: len(list) or len(string)");
            }

            if (args.get(0).isList()) {
                return new NumberValue(args.get(0).asList().size());
            } else if (args.get(0).isString()) {
                return new NumberValue(args.get(0).asString().length());
            } else {
                throw new RuntimeException("Expected list or string in len(), found: " + args.get(0).getType());
            }
        }
    }

    public static class AppendFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("append() requires 2 arguments: append(list, item)");
            }

            if (args.get(0).isList()) {
                List<QuillValue> list = args.get(0).asList();
                list.add(args.get(1));
                return new BooleanValue(true);
            } else {
                throw new RuntimeException("Expected list in append(), found: " + args.get(0).getType());
            }
        }
    }

    public static class RemoveFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("remove() requires 2 arguments: remove(list, index)");
            }

            if (args.get(0).isList()) {
                if (args.get(1).isNumber()) {
                    List<QuillValue> list = args.get(0).asList();
                    int index = (int) args.get(1).asNumber();
                    
                    if (index < 0 || index >= list.size()) {
                        throw new RuntimeException("Index out of bounds: " + index + " (list size: " + list.size() + ")");
                    }
                    
                    list.remove(index);
                    return new BooleanValue(true);
                } else {
                    throw new RuntimeException("Expected number in remove(), found: " + args.get(1).getType());
                }
            } else {
                throw new RuntimeException("Expected list in remove(), found: " + args.get(0).getType());
            }
        }
    }

    public static class ContainsFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("contains() requires 2 argument: contains(list, item) or contains(string, substring)");
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
                throw new RuntimeException("Expected list or string in contains(), found: " + args.get(0).getType());
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
                throw new RuntimeException("split() requires 2 arguments: split(string, delimiter)");
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
                throw new RuntimeException("join() requires 2 arguments: join(list, delimiter)");
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
                throw new RuntimeException("to_string() requires 1 argument: to_string(value)");
            }

            return new StringValue(args.get(0).toString());
        }
    }

    public static class ToNumberFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("to_number() requires 1 argument: to_number(value)");
            }

            QuillValue val = args.get(0);
            
            if (val.isNumber()) {
                return val;
            } else if (val.isString()) {
                try {
                    return new NumberValue(Double.parseDouble(val.asString()));
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Cannot convert string '" + val.asString() + "' to number");
                }
            } else if (val.isBoolean()) {
                return new NumberValue(val.asBoolean() ? 1.0 : 0.0);
            } else {
                throw new RuntimeException("Cannot convert " + val.getType() + " to number");
            }
        }
    }

    public static class ToBooleanFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("to_boolean() requires 1 argument: to_boolean(value)");
            }

            return new BooleanValue(args.get(0).isTruthy());
        }
    }

    public static class TypeOfFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("type_of() requires 1 argument: type_of(value)");
            }

            return new StringValue(args.get(0).getType().toString().toLowerCase());
        }
    }

    public static class RangeFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("range() requires 2 arguments: range(start, end)");
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
                throw new RuntimeException("random_choice() requires 1 argument: random_choice(list)");
            }

            List<QuillValue> list = args.get(0).asList();
            
            if (list.isEmpty()) {
                throw new RuntimeException("Cannot choose from empty list");
            }
            
            int index = random.nextInt(list.size());
            return list.get(index);
        }
    }
}