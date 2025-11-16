package me.kmathers.quill.interpreter;

import me.kmathers.quill.Quill;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Represents a scope context in Quill.
 * Handles variable storage, subscope management, and player tracking.
 */
public class ScopeContext {
    private static Quill plugin = Quill.getPlugin(Quill.class);
    private final String name;
    private final ScopeContext parent;
    private final Map<String, QuillValue> variables;
    private final Set<String> consts;
    private final Map<String, ScopeContext> subscopes;
    private final Set<Player> players;
    private Region region;
    
    // Root scopes
    public ScopeContext(String name, Region region) {
        this.name = name;
        this.parent = null;
        this.variables = new HashMap<>();
        this.consts = new HashSet<>();
        this.subscopes = new HashMap<>();
        this.players = new HashSet<>();
        this.region = region;
    }
    
    // Subscopes
    public ScopeContext(String name, ScopeContext parent, Region region) {
        this.name = name;
        this.parent = parent;
        this.variables = new HashMap<>();
        this.consts = new HashSet<>();
        this.subscopes = new HashMap<>();
        this.players = new HashSet<>();
        this.region = region;
    }
    
    // Nested execution (functions etc)
    public ScopeContext(ScopeContext parent) {
        this.name = "anonymous";
        this.parent = parent;
        this.variables = new HashMap<>();
        this.consts = new HashSet<>();
        this.subscopes = new HashMap<>();
        this.players = new HashSet<>();
        this.region = parent != null ? parent.region : null;
    }
    
    // === Variable Management ===
    
    /**
     * Define a new variable in this scope.
     * Throws if variable already exists in this scope.
     */
    public void define(String name, QuillValue value) {
        if (variables.containsKey(name)) {
            throw new RuntimeException(plugin.translate("quill.error.user.scope.already-defined", name));
        }
        variables.put(name, value);
    }
    
    public void defineConst(String name, QuillValue value) {
        if (variables.containsKey(name)) {
            throw new RuntimeException(plugin.translate("quill.error.user.scope.already-defined", name));
        }
        variables.put(name, value);
        consts.add(name);
    }

    private boolean isConst(String name) {
        if (consts.contains(name)) {
            return true;
        }
        if (parent != null && parent.has(name)) {
            return parent.isConst(name);
        }
        return false;
    }

    /**
     * Set a variable's value.
     * Looks up the scope chain to find where the variable is defined.
     * If not found anywhere, defines it in the current scope.
     */
    public void set(String name, QuillValue value) {
        if (consts.contains(name)) {
            throw new RuntimeException(plugin.translate("quill.error.user.scope.cannot-const", name));
        }

        if (variables.containsKey(name)) {
            variables.put(name, value);
            return;
        }
        
        if (parent != null) {
            try {
                parent.get(name);
                if (parent.isConst(name)) {
                    throw new RuntimeException(plugin.translate("quill.error.user.scope.cannot-const", name));
                }
                parent.set(name, value);
                return;
            } catch (RuntimeException e) {
                // If a const error, re-throw
                if (e.getMessage().contains(plugin.translate("quill.error.user.scope.cannot-const", name))) {
                    throw e;
                }
                // Variable doesn't exist in parent chain, define it here
            }
        }
        
        variables.put(name, value);
    }
    
    /**
     * Get a variable's value.
     * Looks up the scope chain until found.
     */
    public QuillValue get(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        }
        
        if (parent != null) {
            return parent.get(name);
        }
        
    throw new RuntimeException(plugin.translate("quill.error.user.scope.undefined", name));
    }
    
    /**
     * Check if a variable exists in this scope or parent scopes.
     */
    public boolean has(String name) {
        if (variables.containsKey(name)) {
            return true;
        }
        if (parent != null) {
            return parent.has(name);
        }
        return false;
    }
    
    // === Subscope Management ===
    
    /**
     * Register a subscope with a name.
     */
    public void registerSubscope(String name, ScopeContext subscope) {
        subscopes.put(name, subscope);
    }
    
    /**
     * Get a subscope by name.
     */
    public ScopeContext getSubscope(String name) {
        return subscopes.get(name);
    }
    
    /**
     * Check if a subscope exists.
     */
    public boolean hasSubscope(String name) {
        return subscopes.containsKey(name);
    }
    
    // === Player Management ===
    
    /**
     * Add a player to this scope's player list.
     */
    public void addPlayer(Player player) {
        players.add(player);
    }
    
    /**
     * Remove a player from this scope's player list.
     */
    public void removePlayer(Player player) {
        players.remove(player);
    }
    
    /**
     * Check if a player is in this scope's player list.
     */
    public boolean hasPlayer(Player player) {
        return players.contains(player);
    }
    
    /**
     * Get all players in this scope.
     */
    public Set<Player> getPlayers() {
        return new HashSet<>(players);
    }
    
    // === Region Management ===
    
    /**
     * Get this scope's physical region.
     */
    public Region getRegion() {
        return region;
    }
    
    /**
     * Set this scope's physical region.
     */
    public void setRegion(Region region) {
        this.region = region;
    }
    
    /**
     * Check if a location is within this scope's region.
     */
    public boolean isInRegion(Location location) {
        if (region == null) return false;
        return region.contains(location);
    }
    
    /**
     * Check if a player is within this scope's region.
     */
    public boolean isPlayerInRegion(Player player) {
        if (region == null) return false;
        return region.contains(player.getLocation());
    }
    
    // === Scope Hierarchy ===
    
    /**
     * Get the parent scope.
     */
    public ScopeContext getParent() {
        return parent;
    }
    
    /**
     * Get the root scope (topmost parent).
     */
    public ScopeContext getRoot() {
        ScopeContext current = this;
        while (current.parent != null) {
            current = current.parent;
        }
        return current;
    }
    
    /**
     * Check if this is a root scope (no parent).
     */
    public boolean isRoot() {
        return parent == null;
    }
    
    // === Getters ===
    
    public String getName() {
        return name;
    }
    
    public Map<String, QuillValue> getVariables() {
        return new HashMap<>(variables);
    }
    
    // === Debugging ===
    
    @Override
    public String toString() {
        return "Scope(" + name + ", vars=" + variables.size() + ", players=" + players.size() + ")";
    }
    
    /**
     * Represents a 3D rectangular region in the world.
     */
    public static class Region {
        private final double x1, y1, z1;
        private final double x2, y2, z2;
        private final String worldName;
        
        public Region(double x1, double y1, double z1, double x2, double y2, double z2, String worldName) {
            this.x1 = Math.min(x1, x2);
            this.y1 = Math.min(y1, y2);
            this.z1 = Math.min(z1, z2);
            this.x2 = Math.max(x1, x2);
            this.y2 = Math.max(y1, y2);
            this.z2 = Math.max(z1, z2);
            this.worldName = worldName;
        }
        
        /**
         * Check if a location is within this region.
         */
        public boolean contains(Location loc) {
            if (worldName != null && !worldName.equals(loc.getWorld().getName())) {
                return false;
            }
            
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();
            
            return x >= x1 && x <= x2 &&
                   y >= y1 && y <= y2 &&
                   z >= z1 && z <= z2;
        }
        
        /**
         * Check if this region is fully contained within another region.
         */
        public boolean isWithin(Region other) {
            if (worldName != null && !worldName.equals(other.worldName)) {
                return false;
            }
            
            return x1 >= other.x1 && x2 <= other.x2 &&
                   y1 >= other.y1 && y2 <= other.y2 &&
                   z1 >= other.z1 && z2 <= other.z2;
        }
        
        /**
         * Check if this region overlaps with another region.
         */
        public boolean overlaps(Region other) {
            if (worldName != null && !worldName.equals(other.worldName)) {
                return false;
            }
            
            return x1 <= other.x2 && x2 >= other.x1 &&
                   y1 <= other.y2 && y2 >= other.y1 &&
                   z1 <= other.z2 && z2 >= other.z1;
        }
        
        public double getX1() { return x1; }
        public double getY1() { return y1; }
        public double getZ1() { return z1; }
        public double getX2() { return x2; }
        public double getY2() { return y2; }
        public double getZ2() { return z2; }
        public String getWorldName() { return worldName; }
        
        @Override
        public String toString() {
            return String.format("Region(%.1f,%.1f,%.1f to %.1f,%.1f,%.1f)", 
                x1, y1, z1, x2, y2, z2);
        }
    }
}