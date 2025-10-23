package me.kmathers.quill.interpreter;

import me.kmathers.quill.parser.AST.*;
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
    private Map<String, EventHandler> eventHandlers;
    
    private static class ReturnSignal extends RuntimeException {
        final QuillValue value;
        ReturnSignal(QuillValue value) { this.value = value; }
    }
    
    private static class BreakSignal extends RuntimeException {}
    private static class ContinueSignal extends RuntimeException {}
    
    public QuillInterpreter(ScopeContext globalScope) {
        this.globalScope = globalScope;
        this.currentScope = globalScope;
        this.builtIns = new HashMap<>();
        this.eventHandlers = new HashMap<>();
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
        
        throw new RuntimeException("Unknown AST node type: " + node.getClass().getName());
    }
    
    // === Literal Evaluation ===
    
    private QuillValue evaluateNumberLiteral(NumberLiteral node) {
        return new NumberValue(node.value);
    }
    
    private QuillValue evaluateStringLiteral(StringLiteral node) {
        String result = node.value;
        int start = 0;
        while ((start = result.indexOf('{', start)) != -1) {
            int end = result.indexOf('}', start);
            if (end == -1) break;
            
            String varName = result.substring(start + 1, end);
            QuillValue value = currentScope.get(varName);
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
                    throw new RuntimeException("Unknown player property: " + node.property);
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
                    throw new RuntimeException("Unknown location property: " + node.property);
            }
        }
        
        if (object.isItem()) {
            org.bukkit.inventory.ItemStack item = object.asItem();
            switch (node.property) {
                case "type": return new StringValue(item.getType().name().toLowerCase());
                case "amount": return new NumberValue(item.getAmount());
                default:
                    throw new RuntimeException("Unknown item property: " + node.property);
            }
        }
        
        if (object.isEntity()) {
            org.bukkit.entity.Entity entity = object.asEntity();
            switch (node.property) {
                case "type": return new StringValue(entity.getType().name().toLowerCase());
                case "location": return new LocationValue(entity.getLocation());
                case "alive": return new BooleanValue(!entity.isDead());
                default:
                    throw new RuntimeException("Unknown entity property: " + node.property);
            }
        }
        
        throw new RuntimeException("Cannot access property '" + node.property + "' on " + object.getType());
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
                    throw new RuntimeException("Division by zero");
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
                throw new RuntimeException("Unknown binary operator: " + node.operator);
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
                throw new RuntimeException("Unknown unary operator: " + node.operator);
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
            
            throw new RuntimeException("Cannot assign to property of " + object.getType());
        }
        
        throw new RuntimeException("Invalid assignment target");
    }
    
    // === Function Calls ===
    
    private QuillValue evaluateCallExpression(CallExpression node) {
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
                throw new RuntimeException("Function " + func.getName() + " expects " + 
                    func.getParameters().size() + " arguments but got " + args.size());
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
        
        throw new RuntimeException("Not a function: " + callee.getType());
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
            throw new RuntimeException("For loop requires a list, got " + iterable.getType());
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
            for (QuillValue item : items) {
                ScopeContext previousScope = null;
                if (isSubscopeIteration) {
                    previousScope = currentScope;
                    currentScope = new ScopeContext(subscope);
                }
                
                try {
                    currentScope.define(node.variable, item);
                    
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
        eventHandlers.put(node.eventName, node);
        return NullValue.INSTANCE;
    }
    
    private QuillValue evaluateScopeCreation(ScopeCreation node) {
        if (node.arguments.size() != 6) {
            throw new RuntimeException("Scope creation requires 6 arguments (x1, y1, z1, x2, y2, z2)");
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
        EventHandler handler = eventHandlers.get(eventName);
        if (handler == null) return;
        
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
        } finally {
            currentScope = previousScope;
        }
    }
    
    // === Built-in Functions ===
    
    private void registerBuiltIns() {
        // TODO: Implement utility and constructor built in functions
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
        builtIns.put("addtoscope", new AddToScopeFunction());
        builtIns.put("removefromscope", new RemoveFromScopeFunction());
        builtIns.put("getplayers", new GetPlayersFunction());
        builtIns.put("in_region", new InRegionFunction());
        builtIns.put("get_region", new GetRegionFunction());
        builtIns.put("set_region", new SetRegionFunction());
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
    }
    
    public interface BuiltInFunction {
        QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter);
    }
}