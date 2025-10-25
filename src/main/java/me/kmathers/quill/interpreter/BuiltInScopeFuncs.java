package me.kmathers.quill.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;

import me.kmathers.quill.interpreter.QuillValue.BooleanValue;
import me.kmathers.quill.interpreter.QuillValue.ListValue;
import me.kmathers.quill.interpreter.QuillValue.PlayerValue;
import me.kmathers.quill.interpreter.QuillValue.RegionValue;
import me.kmathers.quill.interpreter.ScopeContext.Region;

/**
 * Built-in scope functions for Quill.
 */
public class BuiltInScopeFuncs {
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

    public static class RemoveFromScopeFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("removefromscope() requires 2 arguments: removefromscope(player, scope)");
            }
            
            Player player = args.get(0).asPlayer();
            ScopeContext targetScope = args.get(1).asScope().getScope();

            if(targetScope.getPlayers().contains(player)) {
                targetScope.removePlayer(player);
            }

            return new BooleanValue(true);
        }
    }

    public static class GetPlayersFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("getplayers() requires 1 argument: getplayers(scope)");
            }

            ScopeContext targetScope = args.get(0).asScope().getScope();
            Set<Player> players = targetScope.getPlayers();

            List<QuillValue> playerValues = new ArrayList<>();
            for (Player player : players) {
                playerValues.add(new PlayerValue(player));
            }

            return new ListValue(playerValues);
        }
    }

    public static class InRegionFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("in_region() requires 2 arguments: in_region(player, scope) or in_region(location, scope)");
            }
            

            ScopeContext targetScope = args.get(1).asScope().getScope();

            if (args.get(0).isPlayer()) {
                return new BooleanValue(targetScope.isInRegion(args.get(0).asPlayer().getLocation()));
            } else if (args.get(0).isLocation()) {
                return new BooleanValue(targetScope.isInRegion(args.get(0).asLocation()));
            } else {
                throw new RuntimeException("Expected player or location in in_region(), found: " + args.get(0).getType());
            }
        }
    }

    public static class GetRegionFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("get_region() requires 1 argument: get_region(scope)");
            }
            
            ScopeContext targetScope = args.get(0).asScope().getScope();
            Region region = targetScope.getRegion();
            
            if (region == null) {
                return QuillValue.NullValue.INSTANCE;
            }
            
            return new RegionValue(
                region.getX1(), 
                region.getY1(), 
                region.getZ1(),
                region.getX2(), 
                region.getY2(), 
                region.getZ2()
            );
        }
    }

    public static class SetRegionFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 7) {
                throw new RuntimeException("set_region() requires 7 arguments: set_region(scope, x1, y1, z1, x2, y2, z2)");
            }
            
            ScopeContext targetScope = args.get(0).asScope().getScope();
            double x1 = args.get(1).asNumber();
            double y1 = args.get(2).asNumber();
            double z1 = args.get(3).asNumber();
            double x2 = args.get(4).asNumber();
            double y2 = args.get(5).asNumber();
            double z2 = args.get(6).asNumber();
            
            String worldName = targetScope.getRegion() != null ? 
                targetScope.getRegion().getWorldName() : "world";
            
            Region newRegion = new Region(x1, y1, z1, x2, y2, z2, worldName);
            targetScope.setRegion(newRegion);
            
            return new BooleanValue(true);
        }
    }
}
