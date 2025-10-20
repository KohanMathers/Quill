package me.kmathers.quill.interpreter;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.World;

import java.util.List;

public abstract class QuillValue {
    
    public enum ValueType {
        NUMBER,
        STRING,
        BOOLEAN,
        NULL,
        PLAYER,
        LOCATION,
        ITEM,
        SCOPE,
        LIST,
        ENTITY,
        FUNCTION,
        WORLD
    }
    
    public abstract ValueType getType();
    public abstract Object getValue();
    
    // === Type Checking ===
    
    public boolean isNumber() { return getType() == ValueType.NUMBER; }
    public boolean isString() { return getType() == ValueType.STRING; }
    public boolean isBoolean() { return getType() == ValueType.BOOLEAN; }
    public boolean isNull() { return getType() == ValueType.NULL; }
    public boolean isPlayer() { return getType() == ValueType.PLAYER; }
    public boolean isLocation() { return getType() == ValueType.LOCATION; }
    public boolean isItem() { return getType() == ValueType.ITEM; }
    public boolean isScope() { return getType() == ValueType.SCOPE; }
    public boolean isList() { return getType() == ValueType.LIST; }
    public boolean isEntity() { return getType() == ValueType.ENTITY; }
    public boolean isFunction() { return getType() == ValueType.FUNCTION; }
    public boolean isWorld() { return getType() == ValueType.WORLD; }
    
    // === Type Conversion (with runtime checks) ===
    
    public double asNumber() {
        if (!isNumber()) {
            throw new RuntimeException("Expected number but got " + getType());
        }
        return (double) getValue();
    }
    
    public String asString() {
        if (!isString()) {
            throw new RuntimeException("Expected string but got " + getType());
        }
        return (String) getValue();
    }
    
    public boolean asBoolean() {
        if (!isBoolean()) {
            throw new RuntimeException("Expected boolean but got " + getType());
        }
        return (boolean) getValue();
    }
    
    public Player asPlayer() {
        if (!isPlayer()) {
            throw new RuntimeException("Expected player but got " + getType());
        }
        return (Player) getValue();
    }
    
    public Location asLocation() {
        if (!isLocation()) {
            throw new RuntimeException("Expected location but got " + getType());
        }
        return (Location) getValue();
    }
    
    public ItemStack asItem() {
        if (!isItem()) {
            throw new RuntimeException("Expected item but got " + getType());
        }
        return (ItemStack) getValue();
    }
    
    public ScopeValue asScope() {
        if (!isScope()) {
            throw new RuntimeException("Expected scope but got " + getType());
        }
        return (ScopeValue) this;
    }
    
    @SuppressWarnings("unchecked")
    public List<QuillValue> asList() {
        if (!isList()) {
            throw new RuntimeException("Expected list but got " + getType());
        }
        return (List<QuillValue>) getValue();
    }
    
    public Entity asEntity() {
        if (!isEntity()) {
            throw new RuntimeException("Expected entity but got " + getType());
        }
        return (Entity) getValue();
    }
    
    public World asWorld() {
        if (!isWorld()) {
            throw new RuntimeException("Expected world but got " + getType());
        }
        return (World) getValue();
    }
    
    // === Truthiness ===
    
    public boolean isTruthy() {
        if (isNull()) return false;
        if (isBoolean()) return asBoolean();
        if (isNumber()) return asNumber() != 0;
        if (isString()) return !asString().isEmpty();
        if (isList()) return !asList().isEmpty();
        return true;
    }
    
    // === String Representation ===
    
    @Override
    public String toString() {
        if (isNull()) return "null";
        if (isString()) return asString();
        return String.valueOf(getValue());
    }
    
    // === Concrete Value Types ===
    
    public static class NumberValue extends QuillValue {
        private final double value;
        
        public NumberValue(double value) {
            this.value = value;
        }
        
        @Override
        public ValueType getType() { return ValueType.NUMBER; }
        
        @Override
        public Object getValue() { return value; }
        
        @Override
        public String toString() { 
            if (value == Math.floor(value)) {
                return String.valueOf((long) value);
            }
            return String.valueOf(value);
        }
    }
    
    public static class StringValue extends QuillValue {
        private final String value;
        
        public StringValue(String value) {
            this.value = value;
        }
        
        @Override
        public ValueType getType() { return ValueType.STRING; }
        
        @Override
        public Object getValue() { return value; }
    }
    
    public static class BooleanValue extends QuillValue {
        private final boolean value;
        
        public BooleanValue(boolean value) {
            this.value = value;
        }
        
        @Override
        public ValueType getType() { return ValueType.BOOLEAN; }
        
        @Override
        public Object getValue() { return value; }
        
        @Override
        public String toString() { return String.valueOf(value); }
    }
    
    public static class NullValue extends QuillValue {
        public static final NullValue INSTANCE = new NullValue();
        
        private NullValue() {}
        
        @Override
        public ValueType getType() { return ValueType.NULL; }
        
        @Override
        public Object getValue() { return null; }
        
        @Override
        public String toString() { return "null"; }
    }
    
    public static class PlayerValue extends QuillValue {
        private final Player player;
        
        public PlayerValue(Player player) {
            this.player = player;
        }
        
        @Override
        public ValueType getType() { return ValueType.PLAYER; }
        
        @Override
        public Object getValue() { return player; }
        
        @Override
        public String toString() { return "Player(" + player.getName() + ")"; }
    }
    
    public static class LocationValue extends QuillValue {
        private final Location location;
        
        public LocationValue(Location location) {
            this.location = location;
        }
        
        @Override
        public ValueType getType() { return ValueType.LOCATION; }
        
        @Override
        public Object getValue() { return location; }
        
        @Override
        public String toString() { 
            return String.format("Location(%.1f, %.1f, %.1f)", 
                location.getX(), location.getY(), location.getZ());
        }
    }
    
    public static class ItemValue extends QuillValue {
        private final ItemStack item;
        
        public ItemValue(ItemStack item) {
            this.item = item;
        }
        
        @Override
        public ValueType getType() { return ValueType.ITEM; }
        
        @Override
        public Object getValue() { return item; }
        
        @Override
        public String toString() { 
            return "Item(" + item.getType() + " x" + item.getAmount() + ")";
        }
    }
    
    public static class ScopeValue extends QuillValue {
        private final ScopeContext scope;
        
        public ScopeValue(ScopeContext scope) {
            this.scope = scope;
        }
        
        @Override
        public ValueType getType() { return ValueType.SCOPE; }
        
        @Override
        public Object getValue() { return scope; }
        
        public ScopeContext getScope() { return scope; }
        
        @Override
        public String toString() { return "Scope(" + scope.getName() + ")"; }
    }
    
    public static class ListValue extends QuillValue {
        private final List<QuillValue> elements;
        
        public ListValue(List<QuillValue> elements) {
            this.elements = elements;
        }
        
        @Override
        public ValueType getType() { return ValueType.LIST; }
        
        @Override
        public Object getValue() { return elements; }
        
        @Override
        public String toString() { 
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < elements.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(elements.get(i).toString());
            }
            sb.append("]");
            return sb.toString();
        }
    }
    
    public static class EntityValue extends QuillValue {
        private final Entity entity;
        
        public EntityValue(Entity entity) {
            this.entity = entity;
        }
        
        @Override
        public ValueType getType() { return ValueType.ENTITY; }
        
        @Override
        public Object getValue() { return entity; }
        
        @Override
        public String toString() { 
            return "Entity(" + entity.getType() + ")";
        }
    }
    
    public static class FunctionValue extends QuillValue {
        private final String name;
        private final List<String> parameters;
        private final me.kmathers.quill.parser.AST.ASTNode body;
        private final ScopeContext closure;
        
        public FunctionValue(String name, List<String> parameters, 
                           me.kmathers.quill.parser.AST.ASTNode body, 
                           ScopeContext closure) {
            this.name = name;
            this.parameters = parameters;
            this.body = body;
            this.closure = closure;
        }
        
        @Override
        public ValueType getType() { return ValueType.FUNCTION; }
        
        @Override
        public Object getValue() { return this; }
        
        public String getName() { return name; }
        public List<String> getParameters() { return parameters; }
        public me.kmathers.quill.parser.AST.ASTNode getBody() { return body; }
        public ScopeContext getClosure() { return closure; }
        
        @Override
        public String toString() { return "Function(" + name + ")"; }
    }
    
    public static class WorldValue extends QuillValue {
        private final World world;
        
        public WorldValue(World world) {
            this.world = world;
        }
        
        @Override
        public ValueType getType() { return ValueType.WORLD; }
        
        @Override
        public Object getValue() { return world; }
        
        @Override
        public String toString() { return "World(" + world.getName() + ")"; }
    }
}