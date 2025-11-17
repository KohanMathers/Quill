package me.kmathers.quill.utils;

import java.util.ArrayList;
import java.util.List;

public class SecurityConfig {
    public enum SecurityMode {
        WHITELIST,
        BLACKLIST
    }

    private SecurityMode mode;
    private List<String> funcs;

    public SecurityConfig(SecurityMode mode) {
        this.mode = mode;
        this.funcs = new ArrayList<>();
    }

    public SecurityMode getMode() {
        return mode;
    }

    public List<String> getFuncs() {
        return funcs;
    }

    public void setMode(SecurityMode mode) {
        this.mode = mode;
    }

    public void setFuncs(List<String> funcs) {
        this.funcs = funcs;
    }

    public void addFunc(String func) {
        if (!(this.funcs.contains(func))) {
            this.funcs.add(func);
        }
    }

    public void removeFunc(String func) {
        if (this.funcs.contains(func)) {
            this.funcs.remove(func);
        }
    }

    public boolean hasPermission(String func) {
        if (this.funcs == null) {
            this.funcs = new ArrayList<>();
        }

        boolean inList = getFuncs().contains(func);
        return (getMode() == SecurityMode.WHITELIST) ? inList : !inList;
    }
}
