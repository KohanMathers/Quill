package me.kmathers.quill.events;

import me.kmathers.quill.QuillScriptManager;
import me.kmathers.quill.interpreter.QuillInterpreter;
import me.kmathers.quill.interpreter.QuillValue;
import me.kmathers.quill.interpreter.QuillValue.*;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.TimeSkipEvent;

import io.papermc.paper.event.player.AsyncChatEvent;

import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Bridges Minecraft events to Quill event handlers.
 * Triggers events for ALL loaded scripts.
 */
public class QuillEventBridge implements Listener {
    private final QuillScriptManager scriptManager;
    
    public QuillEventBridge(QuillScriptManager scriptManager) {
        this.scriptManager = scriptManager;
    }
    
    private void triggerForAllScripts(String eventName, Map<String, QuillValue> context) {
        for (QuillInterpreter interpreter : scriptManager.getAllInterpreters().values()) {
            interpreter.triggerEvent(eventName, context);
        }
    }
    
    // === Player Events ===
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Map<String, QuillValue> context = new HashMap<>();
        context.put("player", new PlayerValue(event.getPlayer()));
        triggerForAllScripts("PlayerJoin", context);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Map<String, QuillValue> context = new HashMap<>();
        context.put("player", new PlayerValue(event.getPlayer()));
        triggerForAllScripts("PlayerQuit", context);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncChatEvent event) {
        Map<String, QuillValue> context = new HashMap<>();
        context.put("player", new PlayerValue(event.getPlayer()));
        
        Map<String, QuillValue> chatData = new HashMap<>();
        chatData.put("message", new StringValue(event.message().toString()));
        context.put("chat", new MapValue(chatData));
        
        triggerForAllScripts("PlayerChat", context);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return; // Only trigger on actual movement
        }
        
        Map<String, QuillValue> context = new HashMap<>();
        context.put("player", new PlayerValue(event.getPlayer()));
        
        Map<String, QuillValue> moveData = new HashMap<>();
        moveData.put("from", new LocationValue(event.getFrom()));
        moveData.put("to", new LocationValue(event.getTo()));
        context.put("move", new MapValue(moveData));
        context.put("event", new EventValue(event));
        
        triggerForAllScripts("PlayerMove", context);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.Player)) {
            return;
        }
        
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getEntity();
        
        Map<String, QuillValue> context = new HashMap<>();
        
        Map<String, QuillValue> damageData = new HashMap<>();
        damageData.put("target", new PlayerValue(player));
        damageData.put("amount", new NumberValue(event.getDamage()));
        damageData.put("cause", new StringValue(event.getCause().name().toLowerCase()));
        
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageByEntity = (EntityDamageByEntityEvent) event;
            damageData.put("source", new EntityValue(damageByEntity.getDamager()));
        } else {
            damageData.put("source", NullValue.INSTANCE);
        }
        
        context.put("damage", new MapValue(damageData));
        context.put("event", new EventValue(event));
        
        triggerForAllScripts("PlayerDamage", context);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Map<String, QuillValue> context = new HashMap<>();
        context.put("player", new PlayerValue(event.getEntity()));
        
        Map<String, QuillValue> deathData = new HashMap<>();
        deathData.put("killer", event.getEntity().getKiller() != null ? 
            new PlayerValue(event.getEntity().getKiller()) : NullValue.INSTANCE);
        deathData.put("cause", event.getEntity().getLastDamageCause() != null ?
            new StringValue(event.getEntity().getLastDamageCause().getCause().name().toLowerCase()) :
            new StringValue("unknown"));
        context.put("death", new MapValue(deathData));
        
        triggerForAllScripts("PlayerDeath", context);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Map<String, QuillValue> context = new HashMap<>();
        context.put("player", new PlayerValue(event.getPlayer()));
        triggerForAllScripts("PlayerRespawn", context);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Map<String, QuillValue> context = new HashMap<>();
        context.put("player", new PlayerValue(event.getPlayer()));
        
        Map<String, QuillValue> interactData = new HashMap<>();
        interactData.put("block", event.getClickedBlock() != null ?
            new LocationValue(event.getClickedBlock().getLocation()) : NullValue.INSTANCE);
        interactData.put("item", event.getItem() != null ?
            new ItemValue(event.getItem()) : NullValue.INSTANCE);
        context.put("interact", new MapValue(interactData));
        context.put("event", new EventValue(event));
        
        triggerForAllScripts("PlayerInteract", context);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Map<String, QuillValue> context = new HashMap<>();
        context.put("player", new PlayerValue(event.getPlayer()));
        context.put("item", new ItemValue(event.getItemDrop().getItemStack()));
        context.put("event", new EventValue(event));
        triggerForAllScripts("PlayerDropItem", context);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            Map<String, QuillValue> context = new HashMap<>();
            context.put("player", new PlayerValue(player));
            context.put("item", new ItemValue(event.getItem().getItemStack()));
            context.put("event", new EventValue(event));
            triggerForAllScripts("PlayerPickupItem", context);
        }    
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Map<String, QuillValue> context = new HashMap<>();
        context.put("player", new PlayerValue(event.getPlayer()));
        
        Map<String, QuillValue> teleportData = new HashMap<>();
        teleportData.put("from", new LocationValue(event.getFrom()));
        teleportData.put("to", new LocationValue(event.getTo()));
        context.put("teleport", new MapValue(teleportData));
        context.put("event", new EventValue(event));
        
        triggerForAllScripts("PlayerTeleport", context);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Map<String, QuillValue> context = new HashMap<>();
        context.put("player", new PlayerValue(event.getPlayer()));
        
        Map<String, QuillValue> gamemodeData = new HashMap<>();
        gamemodeData.put("old", new StringValue(event.getPlayer().getGameMode().name().toLowerCase()));
        gamemodeData.put("new", new StringValue(event.getNewGameMode().name().toLowerCase()));
        context.put("gamemode", new MapValue(gamemodeData));
        context.put("event", new EventValue(event));
        
        triggerForAllScripts("PlayerGameModeChange", context);
    }
    
    // === Block Events ===
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Map<String, QuillValue> context = new HashMap<>();
        context.put("player", new PlayerValue(event.getPlayer()));
        
        Map<String, QuillValue> blockData = new HashMap<>();
        blockData.put("type", new StringValue(event.getBlock().getType().name().toLowerCase()));
        blockData.put("location", new LocationValue(event.getBlock().getLocation()));
        context.put("block", new MapValue(blockData));
        context.put("event", new EventValue(event));
        
        triggerForAllScripts("BlockBreak", context);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Map<String, QuillValue> context = new HashMap<>();
        context.put("player", new PlayerValue(event.getPlayer()));
        
        Map<String, QuillValue> blockData = new HashMap<>();
        blockData.put("type", new StringValue(event.getBlock().getType().name().toLowerCase()));
        blockData.put("location", new LocationValue(event.getBlock().getLocation()));
        context.put("block", new MapValue(blockData));
        context.put("event", new EventValue(event));
        
        triggerForAllScripts("BlockPlace", context);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        
        Map<String, QuillValue> context = new HashMap<>();
        context.put("player", new PlayerValue(event.getPlayer()));
        
        Map<String, QuillValue> blockData = new HashMap<>();
        blockData.put("type", new StringValue(event.getClickedBlock().getType().name().toLowerCase()));
        blockData.put("location", new LocationValue(event.getClickedBlock().getLocation()));
        context.put("block", new MapValue(blockData));
        context.put("event", new EventValue(event));
        
        triggerForAllScripts("BlockInteract", context);
    }
    
    // === Entity Events ===
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntitySpawn(EntitySpawnEvent event) {
        Map<String, QuillValue> context = new HashMap<>();
        context.put("entity", new EntityValue(event.getEntity()));
        
        Map<String, QuillValue> entityData = new HashMap<>();
        entityData.put("type", new StringValue(event.getEntity().getType().name().toLowerCase()));
        entityData.put("location", new LocationValue(event.getLocation()));
        context.put("entity", new MapValue(entityData));
        
        triggerForAllScripts("EntitySpawn", context);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        Map<String, QuillValue> context = new HashMap<>();
        context.put("entity", new EntityValue(event.getEntity()));
        
        Map<String, QuillValue> entityData = new HashMap<>();
        entityData.put("killer", event.getEntity().getKiller() != null ?
            new PlayerValue(event.getEntity().getKiller()) : NullValue.INSTANCE);
        context.put("entity", new MapValue(entityData));
        
        triggerForAllScripts("EntityDeath", context);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.Player) {
            return; // Handled by PlayerDamage
        }
        
        Map<String, QuillValue> context = new HashMap<>();
        context.put("entity", new EntityValue(event.getEntity()));
        
        Map<String, QuillValue> damageData = new HashMap<>();
        damageData.put("amount", new NumberValue(event.getDamage()));
        
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageByEntity = (EntityDamageByEntityEvent) event;
            damageData.put("source", new EntityValue(damageByEntity.getDamager()));
        } else {
            damageData.put("source", NullValue.INSTANCE);
        }
        
        context.put("damage", new MapValue(damageData));
        context.put("event", new EventValue(event));
        
        triggerForAllScripts("EntityDamage", context);
    }
    
    // === World Events ===
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onTimeChange(TimeSkipEvent event) {
        Map<String, QuillValue> context = new HashMap<>();
        context.put("world", new WorldValue(event.getWorld()));
        
        Map<String, QuillValue> timeData = new HashMap<>();
        timeData.put("old", new NumberValue(event.getWorld().getTime() - event.getSkipAmount()));
        timeData.put("new", new NumberValue(event.getWorld().getTime()));
        context.put("time", new MapValue(timeData));
        
        triggerForAllScripts("TimeChange", context);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onWeatherChange(WeatherChangeEvent event) {
        Map<String, QuillValue> context = new HashMap<>();
        context.put("world", new WorldValue(event.getWorld()));
        
        Map<String, QuillValue> weatherData = new HashMap<>();
        String newWeather = event.toWeatherState() ? "rain" : "clear";
        weatherData.put("new", new StringValue(newWeather));
        context.put("weather", new MapValue(weatherData));
        
        triggerForAllScripts("WeatherChange", context);
    }
}