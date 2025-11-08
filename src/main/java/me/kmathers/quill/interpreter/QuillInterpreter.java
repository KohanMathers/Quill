package me.kmathers.quill.interpreter;

import me.kmathers.quill.parser.AST.*;
import me.kmathers.quill.Quill;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.ClearEffectsFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.GetGamemodeFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.GetHealthFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.GetHungerFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.GetLocationFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.GetNameFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.GiveEffectFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.GiveFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.HasItemFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.HealFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.IsOnlineFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.IsOpFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.KickFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.KillFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.PlaySoundFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.RemoveEffectFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.RemoveItemFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.SendMessageFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.SendTitleFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.SetFlyingFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.SetGamemodeFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.SetHealthFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.SetHungerFunction;
import me.kmathers.quill.interpreter.BuiltInPlayerFuncs.TeleportFunction;
import me.kmathers.quill.interpreter.BuiltInScopeFuncs.AddToScopeFunction;
import me.kmathers.quill.interpreter.BuiltInScopeFuncs.GetPlayersFunction;
import me.kmathers.quill.interpreter.BuiltInScopeFuncs.GetRegionFunction;
import me.kmathers.quill.interpreter.BuiltInScopeFuncs.InRegionFunction;
import me.kmathers.quill.interpreter.BuiltInScopeFuncs.RemoveFromScopeFunction;
import me.kmathers.quill.interpreter.BuiltInScopeFuncs.SetRegionFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.AbsFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.AppendFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.CancelFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.CeilFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.ContainsFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.DistanceFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.FloorFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.GetOnlinePlayersFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.GetPlayerFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.JoinFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.LenFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.LogFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.PowFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.RandomChoiceFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.RandomFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.RangeFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.RemoveFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.RoundFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.SplitFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.SqrtFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.ToBooleanFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.ToNumberFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.ToStringFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.TriggerCustomFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.TypeOfFunction;
import me.kmathers.quill.interpreter.BuiltInUtilFuncs.WaitFunction;
import me.kmathers.quill.interpreter.BuiltInWorldFuncs.BreakBlockFunction;
import me.kmathers.quill.interpreter.BuiltInWorldFuncs.BroadcastFunction;
import me.kmathers.quill.interpreter.BuiltInWorldFuncs.CreateExplosionFunction;
import me.kmathers.quill.interpreter.BuiltInWorldFuncs.GetTimeFunction;
import me.kmathers.quill.interpreter.BuiltInWorldFuncs.GetWeatherFunction;
import me.kmathers.quill.interpreter.BuiltInWorldFuncs.GetWorldFunction;
import me.kmathers.quill.interpreter.BuiltInWorldFuncs.RemoveEntityFunction;
import me.kmathers.quill.interpreter.BuiltInWorldFuncs.SetBlockFunction;
import me.kmathers.quill.interpreter.BuiltInWorldFuncs.GetBlockFunction;
import me.kmathers.quill.interpreter.BuiltInWorldFuncs.SetTimeFunction;
import me.kmathers.quill.interpreter.BuiltInWorldFuncs.SetWeatherFunction;
import me.kmathers.quill.interpreter.BuiltInWorldFuncs.SpawnEntityFunction;
import me.kmathers.quill.interpreter.BuiltInWorldFuncs.StrikeLightningFunction;
import me.kmathers.quill.interpreter.QuillValue.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tree-walking interpreter for Quill.
 * Executes AST nodes and manages runtime state.
 */
public class QuillInterpreter {
    private ScopeContext globalScope;
    private ScopeContext currentScope;
    private Map<String, BuiltInFunction> builtIns;
    private Map<String, List<EventHandler>> eventHandlers;
    
    private static class ReturnSignal extends RuntimeException {
        final QuillValue value;
        ReturnSignal(QuillValue value) { this.value = value; }
    }
    
    private static class BreakSignal extends RuntimeException {}
    private static class ContinueSignal extends RuntimeException {}
    
    private static Quill plugin = Quill.getPlugin(Quill.class);

    public QuillInterpreter(ScopeContext globalScope) {
        this.globalScope = globalScope;
        this.currentScope = globalScope;
        this.builtIns = new HashMap<>();
        this.eventHandlers = new HashMap<>(); // Now stores lists
        registerBuiltIns();
    }
    
    // === Main Evaluation ===
    
    public void execute(Program program) {
        for (ASTNode statement : program.statements) {
            evaluate(statement);
        }
    }
    
    public QuillValue evaluate(ASTNode node) {
        if (node == null) {
            return NullValue.INSTANCE;
        }
        
        // Literals
        if (node instanceof NumberLiteral) {
            return evaluateNumberLiteral((NumberLiteral) node);
        } else if (node instanceof StringLiteral) {
            return evaluateStringLiteral((StringLiteral) node);
        } else if (node instanceof BooleanLiteral) {
            return evaluateBooleanLiteral((BooleanLiteral) node);
        } else if (node instanceof NullLiteral) {
            return NullValue.INSTANCE;
        } else if (node instanceof ListLiteral) {
            return evaluateListLiteral((ListLiteral) node);
        }
        
        // Identifiers and member access
        else if (node instanceof Identifier) {
            return evaluateIdentifier((Identifier) node);
        } else if (node instanceof MemberExpression) {
            return evaluateMemberExpression((MemberExpression) node);
        }
        
        // Expressions
        else if (node instanceof BinaryExpression) {
            return evaluateBinaryExpression((BinaryExpression) node);
        } else if (node instanceof UnaryExpression) {
            return evaluateUnaryExpression((UnaryExpression) node);
        } else if (node instanceof AssignmentExpression) {
            return evaluateAssignmentExpression((AssignmentExpression) node);
        } else if (node instanceof CallExpression) {
            return evaluateCallExpression((CallExpression) node);
        }
        
        // Statements
        else if (node instanceof VariableDeclaration) {
            return evaluateVariableDeclaration((VariableDeclaration) node);
        } else if (node instanceof FunctionDeclaration) {
            return evaluateFunctionDeclaration((FunctionDeclaration) node);
        } else if (node instanceof ReturnStatement) {
            return evaluateReturnStatement((ReturnStatement) node);
        } else if (node instanceof IfStatement) {
            return evaluateIfStatement((IfStatement) node);
        } else if (node instanceof WhileStatement) {
            return evaluateWhileStatement((WhileStatement) node);
        } else if (node instanceof ForStatement) {
            return evaluateForStatement((ForStatement) node);
        } else if (node instanceof BreakStatement) {
            throw new BreakSignal();
        } else if (node instanceof ContinueStatement) {
            throw new ContinueSignal();
        } else if (node instanceof TryStatement) {
            return evaluateTryStatement((TryStatement) node);
        } else if (node instanceof EventHandler) {
            return evaluateEventHandler((EventHandler) node);
        } else if (node instanceof ScopeCreation) {
            return evaluateScopeCreation((ScopeCreation) node);
        } else if (node instanceof ExpressionStatement) {
            return evaluate(((ExpressionStatement) node).expression);
        }
        
        throw new RuntimeException(plugin.translate("errors.interpreter.unknown-ast", node.getClass().getName()));
    }
    
    // === Literal Evaluation ===
    
    private QuillValue evaluateNumberLiteral(NumberLiteral node) {
        return new NumberValue(node.value);
    }

    @SuppressWarnings("unused")
    private QuillValue evaluateStringLiteral(StringLiteral node) {
        String result = node.value;
        int start = 0;
        while ((start = result.indexOf('{', start)) != -1) {
            int end = result.indexOf('}', start);
            if (end == -1) break;
            
            String expression = result.substring(start + 1, end);
            QuillValue value;
            
            if (expression.contains(".")) {
                String[] parts = expression.split("\\.", 2);
                String objectName = parts[0];
                String propertyName = parts[1];
                
                try {
                    QuillValue object = currentScope.get(objectName);
                    
                    String[] allParts = expression.split("\\.");
                    QuillValue current = currentScope.get(allParts[0]);
                    
                    for (int i = 1; i < allParts.length; i++) {
                        String prop = allParts[i];
                        
                        if (current.isScope()) {
                            ScopeContext scope = current.asScope().getScope();
                            if (prop.equals("players")) {
                                List<QuillValue> players = scope.getPlayers().stream()
                                    .map(PlayerValue::new)
                                    .collect(java.util.stream.Collectors.toList());
                                current = new ListValue(players);
                            } else if (prop.equals("region")) {
                                ScopeContext.Region region = scope.getRegion();
                                if (region == null) {
                                    current = NullValue.INSTANCE;
                                } else {
                                    current = new RegionValue(
                                        region.getX1(), region.getY1(), region.getZ1(),
                                        region.getX2(), region.getY2(), region.getZ2()
                                    );
                                }
                            } else {
                                current = scope.get(prop);
                            }
                        } else if (current.isPlayer()) {
                            Player player = current.asPlayer();
                            switch (prop) {
                                case "name": current = new StringValue(player.getName()); break;
                                case "health": current = new NumberValue(player.getHealth()); break;
                                case "hunger": current = new NumberValue(player.getFoodLevel()); break;
                                case "location": current = new LocationValue(player.getLocation()); break;
                                case "gamemode": current = new StringValue(player.getGameMode().name().toLowerCase()); break;
                                case "flying": current = new BooleanValue(player.isFlying()); break;
                                case "online": current = new BooleanValue(player.isOnline()); break;
                                default:
                                    throw new RuntimeException(plugin.translate("errors.interpreter.unknown-prop", "player", prop));
                            }
                        } else if (current.isLocation()) {
                            org.bukkit.Location loc = current.asLocation();
                            switch (prop) {
                                case "x": current = new NumberValue(loc.getX()); break;
                                case "y": current = new NumberValue(loc.getY()); break;
                                case "z": current = new NumberValue(loc.getZ()); break;
                                case "world": current = new WorldValue(loc.getWorld()); break;
                                default:
                                    throw new RuntimeException(plugin.translate("errors.interpreter.unknown-prop", "location", prop));
                            }
                        } else if (current.isItem()) {
                            org.bukkit.inventory.ItemStack item = current.asItem();
                            switch (prop) {
                                case "type": current = new StringValue(item.getType().name().toLowerCase()); break;
                                case "amount": current = new NumberValue(item.getAmount()); break;
                                default:
                                    throw new RuntimeException(plugin.translate("errors.interpreter.unknown-prop", "item", prop));
                            }
                        } else if (current.isEntity()) {
                            org.bukkit.entity.Entity entity = current.asEntity();
                            switch (prop) {
                                case "type": current = new StringValue(entity.getType().name().toLowerCase()); break;
                                case "location": current = new LocationValue(entity.getLocation()); break;
                                case "alive": current = new BooleanValue(!entity.isDead()); break;
                                default:
                                    throw new RuntimeException(plugin.translate("errors.interpreter.unknown-prop", "entity", prop));
                            }
                        } else if (current.isMap()) {
                            MapValue mapValue = (MapValue) current;
                            current = mapValue.get(prop);
                        } else {
                            throw new RuntimeException(plugin.translate("errors.interpreter.cannot-prop", prop, current.getType()));
                        }
                    }
                    
                    value = current;
                } catch (RuntimeException e) {
                    value = currentScope.get(expression);
                }
            } else {
                value = currentScope.get(expression);
            }
            
            String replacement = value.toString();
            result = result.substring(0, start) + replacement + result.substring(end + 1);
            start += replacement.length();
        }
        return new StringValue(result);
    }
    
    private QuillValue evaluateBooleanLiteral(BooleanLiteral node) {
        return new BooleanValue(node.value);
    }
    
    private QuillValue evaluateListLiteral(ListLiteral node) {
        List<QuillValue> elements = new ArrayList<>();
        for (ASTNode element : node.elements) {
            elements.add(evaluate(element));
        }
        return new ListValue(elements);
    }
    
    // === Identifier and Member Access ===
    
    private QuillValue evaluateIdentifier(Identifier node) {
        return currentScope.get(node.name);
    }
    
    private QuillValue evaluateMemberExpression(MemberExpression node) {
        QuillValue object = evaluate(node.object);
        
        if (object.isScope()) {
            ScopeContext scope = object.asScope().getScope();
            
            if (node.property.equals("players")) {
                List<QuillValue> players = scope.getPlayers().stream()
                    .map(PlayerValue::new)
                    .collect(Collectors.toList());
                return new ListValue(players);
            } else if (node.property.equals("region")) {
                ScopeContext.Region region = scope.getRegion();
                if (region == null) return NullValue.INSTANCE;
                
                return new RegionValue(
                    region.getX1(),
                    region.getY1(),
                    region.getZ1(),
                    region.getX2(),
                    region.getY2(),
                    region.getZ2()
                );
            } else {
                return scope.get(node.property);
            }
        }
        
        if (object.isPlayer()) {
            Player player = object.asPlayer();
            switch (node.property) {
                case "name": return new StringValue(player.getName());
                case "health": return new NumberValue(player.getHealth());
                case "hunger": return new NumberValue(player.getFoodLevel());
                case "location": return new LocationValue(player.getLocation());
                case "gamemode": return new StringValue(player.getGameMode().name().toLowerCase());
                case "flying": return new BooleanValue(player.isFlying());
                case "online": return new BooleanValue(player.isOnline());
                default:
                    throw new RuntimeException(plugin.translate("errors.interpreter.unknown-prop", "player", node.property));
            }
        }
        
        if (object.isLocation()) {
            org.bukkit.Location loc = object.asLocation();
            switch (node.property) {
                case "x": return new NumberValue(loc.getX());
                case "y": return new NumberValue(loc.getY());
                case "z": return new NumberValue(loc.getZ());
                case "world": return new WorldValue(loc.getWorld());
                default:
                    throw new RuntimeException(plugin.translate("errors.interpreter.unknown-prop", "location", node.property));
            }
        }
        
        if (object.isItem()) {
            org.bukkit.inventory.ItemStack item = object.asItem();
            switch (node.property) {
                case "type": return new StringValue(item.getType().name().toLowerCase());
                case "amount": return new NumberValue(item.getAmount());
                default:
                    throw new RuntimeException(plugin.translate("errors.interpreter.unknown-prop", "item", node.property));
            }
        }
        
        if (object.isEntity()) {
            org.bukkit.entity.Entity entity = object.asEntity();
            switch (node.property) {
                case "type": return new StringValue(entity.getType().name().toLowerCase());
                case "location": return new LocationValue(entity.getLocation());
                case "alive": return new BooleanValue(!entity.isDead());
                default:
                    throw new RuntimeException(plugin.translate("errors.interpreter.unknown-prop", "entity", node.property));
            }
        }
        
        if (object.isMap()) {
            MapValue mapValue = (MapValue) object;
            return mapValue.get(node.property);
        }

        throw new RuntimeException(plugin.translate("errors.interpreter.cannot-prop", node.property, object.getType()));
    }
    
    // === Binary Expressions ===
    
    private QuillValue evaluateBinaryExpression(BinaryExpression node) {
        QuillValue left = evaluate(node.left);
        QuillValue right = evaluate(node.right);
        
        switch (node.operator) {
            case "+":
                if (left.isNumber() && right.isNumber()) {
                    return new NumberValue(left.asNumber() + right.asNumber());
                }
                return new StringValue(left.toString() + right.toString());
                
            case "-":
                return new NumberValue(left.asNumber() - right.asNumber());
            case "*":
                return new NumberValue(left.asNumber() * right.asNumber());
            case "/":
                if (right.asNumber() == 0) {
                    throw new RuntimeException(plugin.translate("errors.interpreter.zero-division"));
                }
                return new NumberValue(left.asNumber() / right.asNumber());
            case "%":
                return new NumberValue(left.asNumber() % right.asNumber());
                
            case "==":
                return new BooleanValue(isEqual(left, right));
            case "!=":
                return new BooleanValue(!isEqual(left, right));
            case ">":
                return new BooleanValue(left.asNumber() > right.asNumber());
            case "<":
                return new BooleanValue(left.asNumber() < right.asNumber());
            case ">=":
                return new BooleanValue(left.asNumber() >= right.asNumber());
            case "<=":
                return new BooleanValue(left.asNumber() <= right.asNumber());
                
            case "&&":
                return new BooleanValue(left.isTruthy() && right.isTruthy());
            case "||":
                return new BooleanValue(left.isTruthy() || right.isTruthy());
                
            default:
                throw new RuntimeException(plugin.translate("errors.interpreter.unknown-op", "binary", node.operator));
        }
    }
    
    private boolean isEqual(QuillValue left, QuillValue right) {
        if (left.isNull() && right.isNull()) return true;
        if (left.isNull() || right.isNull()) return false;
        if (left.getType() != right.getType()) return false;
        
        if (left.isNumber()) return left.asNumber() == right.asNumber();
        if (left.isString()) return left.asString().equals(right.asString());
        if (left.isBoolean()) return left.asBoolean() == right.asBoolean();
        
        return left.getValue() == right.getValue();
    }
    
    // === Unary Expressions ===
    
    private QuillValue evaluateUnaryExpression(UnaryExpression node) {
        QuillValue operand = evaluate(node.operand);
        
        switch (node.operator) {
            case "!":
                return new BooleanValue(!operand.isTruthy());
            case "-":
                return new NumberValue(-operand.asNumber());
            default:
                throw new RuntimeException(plugin.translate("errors.interpreter.unknown-op", "unary", node.operator));
        }
    }
    
    // === Assignment ===
    
    private QuillValue evaluateAssignmentExpression(AssignmentExpression node) {
        QuillValue value = evaluate(node.value);
        
        if (node.target instanceof Identifier) {
            String name = ((Identifier) node.target).name;
            currentScope.set(name, value);
            return value;
        } else if (node.target instanceof MemberExpression) {
            MemberExpression member = (MemberExpression) node.target;
            QuillValue object = evaluate(member.object);
            
            if (object.isScope()) {
                ScopeContext scope = object.asScope().getScope();
                scope.set(member.property, value);
                return value;
            }
            
            if (object.isMap()) {
                MapValue mapValue = (MapValue) object;
                mapValue.put(member.property, value);
                return value;
            }
            
            throw new RuntimeException(plugin.translate("errors.interpreter.cannot-assign", object.getType()));
        }
        
        throw new RuntimeException(plugin.translate("errors.interpreter.invalid-assignee"));
    }
    
    // === Function Calls ===
    
    private QuillValue evaluateCallExpression(CallExpression node) {
        if (node.callee instanceof Identifier) {
            String name = ((Identifier) node.callee).name;
            if (builtIns.containsKey(name)) {
                List<QuillValue> args = new ArrayList<>();
                for (ASTNode arg : node.arguments) {
                    args.add(evaluate(arg));
                }
                return builtIns.get(name).call(args, currentScope, this);
            }
        }
        
        QuillValue callee = evaluate(node.callee);
        List<QuillValue> args = new ArrayList<>();
        for (ASTNode arg : node.arguments) {
            args.add(evaluate(arg));
        }
        
        if (node.callee instanceof Identifier) {
            String name = ((Identifier) node.callee).name;
            if (builtIns.containsKey(name)) {
                return builtIns.get(name).call(args, currentScope, this);
            }
        }
        
        if (callee.isFunction()) {
            FunctionValue func = (FunctionValue) callee;
            
            ScopeContext funcScope = new ScopeContext(func.getClosure());
            
            if (args.size() != func.getParameters().size()) {
                throw new RuntimeException(plugin.translate("errors.requires-arguments", "function " + func.getName(), String.valueOf(func.getParameters().size()), "function(...)"));
            }
            
            for (int i = 0; i < args.size(); i++) {
                funcScope.define(func.getParameters().get(i), args.get(i));
            }
            
            ScopeContext previousScope = currentScope;
            currentScope = funcScope;
            
            try {
                if (func.getBody() instanceof me.kmathers.quill.parser.AST.FunctionDeclaration) {
                    FunctionDeclaration funcDecl = (FunctionDeclaration) func.getBody();
                    for (ASTNode statement : funcDecl.body) {
                        evaluate(statement);
                    }
                }
                return NullValue.INSTANCE;
            } catch (ReturnSignal ret) {
                return ret.value;
            } finally {
                currentScope = previousScope;
            }
        }
        
        throw new RuntimeException(plugin.translate("errors.interpreter.not-func", callee.getType()));
    }
    
    // === Statements ===
    
    private QuillValue evaluateVariableDeclaration(VariableDeclaration node) {
        QuillValue value = evaluate(node.value);
        currentScope.define(node.name, value);
        return NullValue.INSTANCE;
    }
    
    private QuillValue evaluateFunctionDeclaration(FunctionDeclaration node) {
        FunctionValue func = new FunctionValue(
            node.name,
            node.parameters,
            node,
            currentScope
        );
        currentScope.define(node.name, func);
        return NullValue.INSTANCE;
    }
    
    private QuillValue evaluateReturnStatement(ReturnStatement node) {
        QuillValue value = node.value != null ? evaluate(node.value) : NullValue.INSTANCE;
        throw new ReturnSignal(value);
    }
    
    private QuillValue evaluateIfStatement(IfStatement node) {
        QuillValue condition = evaluate(node.condition);
        
        if (condition.isTruthy()) {
            for (ASTNode statement : node.thenBranch) {
                evaluate(statement);
            }
        } else if (node.elseBranch != null) {
            for (ASTNode statement : node.elseBranch) {
                evaluate(statement);
            }
        }
        
        return NullValue.INSTANCE;
    }
    
    private QuillValue evaluateWhileStatement(WhileStatement node) {
        try {
            while (evaluate(node.condition).isTruthy()) {
                try {
                    for (ASTNode statement : node.body) {
                        evaluate(statement);
                    }
                } catch (ContinueSignal c) {
                    continue;
                }
            }
        } catch (BreakSignal b) {
            // Break out of loop
        }
        
        return NullValue.INSTANCE;
    }
    

    private QuillValue evaluateForStatement(ForStatement node) {
        QuillValue iterable = evaluate(node.iterable);
        
        if (!iterable.isList()) {
            throw new RuntimeException(plugin.translate("errors.expected", "list", "for-loop iterable", iterable.getType()));
        }
        
        List<QuillValue> items = iterable.asList();
        
        boolean isSubscopeIteration = false;
        ScopeContext subscope = null;
        
        if (node.iterable instanceof MemberExpression) {
            MemberExpression member = (MemberExpression) node.iterable;
            if (member.property.equals("players")) {
                QuillValue obj = evaluate(member.object);
                if (obj.isScope()) {
                    isSubscopeIteration = true;
                    subscope = obj.asScope().getScope();
                }
            }
        }
        
        try {
            boolean firstIteration = true;
            for (QuillValue item : items) {
                ScopeContext previousScope = null;
                if (isSubscopeIteration) {
                    previousScope = currentScope;
                    currentScope = new ScopeContext(subscope);
                }
                
                try {
                    if (firstIteration) {
                        currentScope.define(node.variable, item);
                        firstIteration = false;
                    } else {
                        currentScope.set(node.variable, item);
                    }
                    
                    try {
                        for (ASTNode statement : node.body) {
                            evaluate(statement);
                        }
                    } catch (ContinueSignal c) {
                        continue;
                    }
                } finally {
                    if (isSubscopeIteration) {
                        currentScope = previousScope;
                    }
                }
            }
        } catch (BreakSignal b) {
            // Break out of loop
        }
        
        return NullValue.INSTANCE;
    }

    private QuillValue evaluateTryStatement(TryStatement node) {
        try {
            for (ASTNode statement : node.tryBlock) {
                evaluate(statement);
            }
        } catch (Exception e) {
            ScopeContext catchScope = new ScopeContext(currentScope);
            catchScope.define(node.errorVariable, new StringValue(e.getMessage()));
            
            ScopeContext previousScope = currentScope;
            currentScope = catchScope;
            
            try {
                for (ASTNode statement : node.catchBlock) {
                    evaluate(statement);
                }
            } finally {
                currentScope = previousScope;
            }
        }
        
        return NullValue.INSTANCE;
    }
    
    private QuillValue evaluateEventHandler(EventHandler node) {
        eventHandlers.computeIfAbsent(node.eventName, k -> new ArrayList<>()).add(node);
        return NullValue.INSTANCE;
    }
    
    private QuillValue evaluateScopeCreation(ScopeCreation node) {
        if (node.arguments.size() != 6) {
            throw new RuntimeException(plugin.translate("errors.requires-arguments", "scope_creation()", "6", "(x1, y1, z1, x2, y2, z2)"));
        }
        
        double x1 = evaluate(node.arguments.get(0)).asNumber();
        double y1 = evaluate(node.arguments.get(1)).asNumber();
        double z1 = evaluate(node.arguments.get(2)).asNumber();
        double x2 = evaluate(node.arguments.get(3)).asNumber();
        double y2 = evaluate(node.arguments.get(4)).asNumber();
        double z2 = evaluate(node.arguments.get(5)).asNumber();
        
        String worldName = globalScope.getRegion() != null ? 
            globalScope.getRegion().getWorldName() : "world";
        
        ScopeContext.Region region = new ScopeContext.Region(x1, y1, z1, x2, y2, z2, worldName);
        ScopeContext newScope = new ScopeContext("subscope", currentScope, region);
        
        return new ScopeValue(newScope);
    }
    
    // === Event Handling ===
    
    public void triggerEvent(String eventName, Map<String, QuillValue> eventContext) {
        List<EventHandler> handlers = eventHandlers.get(eventName);
        if (handlers == null || handlers.isEmpty()) return;
        
        for (EventHandler handler : handlers) {
            ScopeContext eventScope = new ScopeContext(globalScope);
            for (Map.Entry<String, QuillValue> entry : eventContext.entrySet()) {
                eventScope.define(entry.getKey(), entry.getValue());
            }
            
            ScopeContext previousScope = currentScope;
            currentScope = eventScope;
            
            try {
                for (ASTNode statement : handler.body) {
                    evaluate(statement);
                }
            } catch (Exception e) {
                // Log error but continue with other handlers
                System.err.println("Error in event handler " + eventName + ": " + e.getMessage());
                e.printStackTrace();
            } finally {
                currentScope = previousScope;
            }
        }
    }
    
    // === Built-in Functions ===
    
    private void registerBuiltIns() {
        // Player Functions
        builtIns.put("teleport", new TeleportFunction());
        builtIns.put("give", new GiveFunction());
        builtIns.put("remove_item", new RemoveItemFunction());
        builtIns.put("set_gamemode", new SetGamemodeFunction());
        builtIns.put("set_health", new SetHealthFunction());
        builtIns.put("set_hunger", new SetHungerFunction());
        builtIns.put("heal", new HealFunction());
        builtIns.put("kill", new KillFunction());
        builtIns.put("sendmessage", new SendMessageFunction());
        builtIns.put("sendtitle", new SendTitleFunction());
        builtIns.put("playsound", new PlaySoundFunction());
        builtIns.put("give_effect", new GiveEffectFunction());
        builtIns.put("remove_effect", new RemoveEffectFunction());
        builtIns.put("clear_effects", new ClearEffectsFunction());
        builtIns.put("set_flying", new SetFlyingFunction());
        builtIns.put("kick", new KickFunction());
        builtIns.put("get_health", new GetHealthFunction());
        builtIns.put("get_hunger", new GetHungerFunction());
        builtIns.put("get_location", new GetLocationFunction());
        builtIns.put("get_gamemode", new GetGamemodeFunction());
        builtIns.put("has_item", new HasItemFunction());
        builtIns.put("get_name", new GetNameFunction());
        builtIns.put("is_online", new IsOnlineFunction());
        builtIns.put("is_op", new IsOpFunction());
        
        // Scope Functions
        builtIns.put("addtoscope", new AddToScopeFunction());
        builtIns.put("removefromscope", new RemoveFromScopeFunction());
        builtIns.put("getplayers", new GetPlayersFunction());
        builtIns.put("in_region", new InRegionFunction());
        builtIns.put("get_region", new GetRegionFunction());
        builtIns.put("set_region", new SetRegionFunction());
        
        // World Functions
        builtIns.put("set_block", new SetBlockFunction());
        builtIns.put("get_block", new GetBlockFunction());
        builtIns.put("break_block", new BreakBlockFunction());
        builtIns.put("spawn_entity", new SpawnEntityFunction());
        builtIns.put("remove_entity", new RemoveEntityFunction());
        builtIns.put("create_explosion", new CreateExplosionFunction());
        builtIns.put("strike_lightning", new StrikeLightningFunction());
        builtIns.put("set_time", new SetTimeFunction());
        builtIns.put("get_time", new GetTimeFunction());
        builtIns.put("set_weather", new SetWeatherFunction());
        builtIns.put("get_weather", new GetWeatherFunction());
        builtIns.put("get_world", new GetWorldFunction());
        builtIns.put("broadcast", new BroadcastFunction());
        
        // Utility Functions
        builtIns.put("cancel", new CancelFunction());
        builtIns.put("wait", new WaitFunction());
        builtIns.put("random", new RandomFunction());
        builtIns.put("round", new RoundFunction());
        builtIns.put("floor", new FloorFunction());
        builtIns.put("ceil", new CeilFunction());
        builtIns.put("abs", new AbsFunction());
        builtIns.put("sqrt", new SqrtFunction());
        builtIns.put("pow", new PowFunction());
        builtIns.put("distance", new DistanceFunction());
        builtIns.put("log", new LogFunction());
        builtIns.put("trigger_custom", new TriggerCustomFunction());
        builtIns.put("get_player", new GetPlayerFunction());
        builtIns.put("get_online_players", new GetOnlinePlayersFunction());
        builtIns.put("log", new LogFunction());
        builtIns.put("len", new LenFunction());
        builtIns.put("append", new AppendFunction());
        builtIns.put("remove", new RemoveFunction());
        builtIns.put("contains", new ContainsFunction());
        builtIns.put("split", new SplitFunction());
        builtIns.put("join", new JoinFunction());
        builtIns.put("to_string", new ToStringFunction());
        builtIns.put("to_number", new ToNumberFunction());
        builtIns.put("to_boolean", new ToBooleanFunction());
        builtIns.put("type_of", new TypeOfFunction());
        builtIns.put("range", new RangeFunction());
        builtIns.put("random_choice", new RandomChoiceFunction());
        
        // Constructor Functions
        builtIns.put("location", new BuiltInConstructorFuncs.LocationFunction());
        builtIns.put("item", new BuiltInConstructorFuncs.ItemFunction());
    }
    
    public interface BuiltInFunction {
        QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter);
    }
}