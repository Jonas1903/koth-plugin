package com.koth.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class MessageUtils {
    private final FileConfiguration config;
    private final LegacyComponentSerializer serializer;
    
    public MessageUtils(FileConfiguration config) {
        this.config = config;
        this.serializer = LegacyComponentSerializer.legacyAmpersand();
    }
    
    public void sendMessage(CommandSender sender, String key, String... replacements) {
        String message = getMessage(key, replacements);
        sender.sendMessage(serializer.deserialize(message));
    }
    
    public String getMessage(String key, String... replacements) {
        String prefix = config.getString("messages.prefix", "&7[&6KOTH&7]&r ");
        String message = config.getString("messages." + key, "");
        
        // Apply replacements
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        
        return prefix + message;
    }
    
    public Component getComponent(String key, String... replacements) {
        return serializer.deserialize(getMessage(key, replacements));
    }
    
    public String getRawMessage(String key, String... replacements) {
        String message = config.getString("messages." + key, "");
        
        // Apply replacements
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        
        return message;
    }
    
    public String formatTime(int seconds) {
        if (seconds >= 60) {
            int minutes = seconds / 60;
            int secs = seconds % 60;
            if (secs > 0) {
                return minutes + " minute" + (minutes != 1 ? "s" : "") + " " + secs + " second" + (secs != 1 ? "s" : "");
            }
            return minutes + " minute" + (minutes != 1 ? "s" : "");
        }
        return seconds + " second" + (seconds != 1 ? "s" : "");
    }
}
