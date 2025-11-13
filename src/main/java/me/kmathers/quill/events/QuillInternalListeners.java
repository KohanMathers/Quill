package me.kmathers.quill.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.kmathers.quill.Quill;

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
}
