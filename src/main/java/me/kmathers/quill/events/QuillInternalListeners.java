package me.kmathers.quill.events;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.kmathers.quill.Quill;
import me.kmathers.quill.interpreter.QuillInterpreter;
import me.kmathers.quill.interpreter.QuillValue;
import me.kmathers.quill.utils.Scope;

public class QuillInternalListeners implements Listener {
        private final Quill main;

        public QuillInternalListeners(Quill main) {
            this.main = main;
        }
        
        @EventHandler(priority = EventPriority.HIGH)
        public void onPlayerKick(PlayerKickEvent event) {
            if (main.getFlying().contains(event.getPlayer().getUniqueId())) {
                String reasonText = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                    .serialize(event.reason());
                
                if (reasonText.contains("Flying is not enabled on this server")) {
                    event.setCancelled(true);
                }
            }
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            main.getFlying().remove(event.getPlayer().getUniqueId());
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onQuillEvent(QuillEvent event) {
            if (event.isCancelled()) return;
            
            List<String> scopesToCheck = new ArrayList<>();
            scopesToCheck.add("global");
            scopesToCheck.addAll(main.getScopeManager().listScopes());
            
            for (String scopeName : scopesToCheck) {
                List<QuillInterpreter> handlers = main.getScriptManager().getHandlersForScopeAndEvent(
                    scopeName, 
                    event.getEventName()
                );
                
                for (QuillInterpreter interpreter : handlers) {
                    try {
                        if (!scopeName.equals("global")) {
                            Scope scope = main.getScopeManager().getScope(scopeName);
                            if (scope != null) {
                                QuillValue playerValue = event.getContext().get("player");
                                
                                if (playerValue != null && playerValue.isPlayer()) {
                                    Player player = playerValue.asPlayer();
                                    if (!scope.getPlayers().contains(player.getUniqueId())) {
                                        continue;
                                    }
                                }
                            }
                        }
                        
                        interpreter.triggerEvent(event.getEventName(), event.getContext());
                    } catch (Exception e) {
                        main.getLogger().severe("Error triggering " + event.getEventName() + 
                            " in " + scopeName + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
}
