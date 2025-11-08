package me.kmathers.quill.utils;

import java.util.List;
import java.util.UUID;

import me.kmathers.quill.Quill;
import me.kmathers.quill.utils.SecurityConfig.SecurityMode;

public class Scope {
    private String name;
    private UUID owner;
    private List<Double> boundaries;
    private SecurityConfig config;
    private Quill plugin;

    public Scope(String name, UUID owner, List<Double> boundaries, SecurityMode mode) {
        this.name = name;
        this.owner = owner;
        this.boundaries = boundaries;
        this.config = new SecurityConfig(mode);
        this.plugin = Quill.getPlugin(Quill.class);
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public List<Double> getBoundaries() {
        return boundaries;
    }

    public SecurityMode getSecurityMode() {
        return config.getMode();
    }

    public List<String> getFuncs() {
        return config.getFuncs();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwner(UUID owner)  {
        this.owner = owner;
    }

    public void setBoundaries(List<Double> boundaries){
        if (boundaries.size() != 6) {
            throw new RuntimeException(plugin.translate("errors.scope.wrong-boundary-list-size"));
        } else {
            this.boundaries = boundaries;
        }
    }

    public void setSecurityMode(SecurityMode mode) {
        this.config.setMode(mode);
    }

    public void setFuncs(List<String> funcs) {
        this.config.setFuncs(funcs);
    }

    public void setBoundary(String boundary, Double coord) {
        String key = boundary.toLowerCase();
        List<String> validModes = List.of("x1", "y1", "z1", "x2", "y2", "z2");

        int index = validModes.indexOf(key);
        if (index == -1) {
            throw new RuntimeException(plugin.translate("errors.value.expected", "one of []'x1', 'y1', 'z1', 'x2', 'y2', 'z2']",  boundary));
        }

        this.boundaries.set(index, coord);
    }

    public void addFunc(String func) {
        this.config.addFunc(func);
    }

    public void removeFunc(String func) {
        this.config.removeFunc(func);
    }

    public boolean hasPermission(String func) {
        return config.hasPermission(func);
    }
}
