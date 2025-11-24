package me.kmathers.quill.utils;

import java.util.ArrayList;
import java.util.List;

public class SecurityConfig {
    public enum SecurityMode {
        WHITELIST,
        BLACKLIST
    }

    public enum AccessMode {
        PUBLIC,
        PRIVATE
    }

    private SecurityMode mode;
    private AccessMode access;
    private List<String> funcs;

    public SecurityConfig(SecurityMode mode, AccessMode access) {
        this.mode = mode;
        this.access = access;
        this.funcs = new ArrayList<>();
    }

    public SecurityMode getMode() {
        return mode;
    }

    public List<String> getFuncs() {
        return funcs;
    }

    public AccessMode getAccess() {
        return access;
    }

    public void setMode(SecurityMode mode) {
        this.mode = mode;
    }

    public void setFuncs(List<String> funcs) {
        this.funcs = funcs;
    }

    public void setAccess(AccessMode access) {
        this.access = access;
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
