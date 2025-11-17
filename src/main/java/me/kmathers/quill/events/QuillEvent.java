package me.kmathers.quill.events;

import me.kmathers.quill.interpreter.QuillValue;
import me.kmathers.quill.utils.Scope;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Map;

public class QuillEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    
    private final String eventName;
    private final Map<String, QuillValue> context;
    private final Scope scope;
    private boolean cancelled = false;
    
    public QuillEvent(String eventName, Map<String, QuillValue> context, Scope scope) {
        super(true);
        this.eventName = eventName;
        this.context = context;
        this.scope = scope;
    }
    
    public String getEventName() { return eventName; }
    public Map<String, QuillValue> getContext() { return context; }
    public Scope getScope() { return scope; }
    
    @Override
    public boolean isCancelled() { return cancelled; }
    
    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    
    @Override
    public HandlerList getHandlers() { return handlers; }
    
    public static HandlerList getHandlerList() { return handlers; }
}