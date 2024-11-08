package com.example;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LevelCommand implements CommandExecutor {
    private final LevelPlayers plugin;

    public LevelCommand(LevelPlayers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(getMessage("usage.set"));
            return true;
        }
    
        if (!sender.hasPermission("levelplayers.set")) {
            sender.sendMessage(getMessage("no-permission"));
            return true;
        }
    
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(getMessage("player-not-found"));
            return true;
        }
    
        try {
            int level = Integer.parseInt(args[1]);
            int maxLevel = plugin.getMaxLevel();
            
            if (level < 1 || level > maxLevel) {
                sender.sendMessage(getMessage("invalid-level")
                    .replace("%max%", String.valueOf(maxLevel)));
                return true;
            }
    
            plugin.setPlayerLevel(target, level);
            sender.sendMessage(getMessage("level-set")
                    .replace("%player%", target.getName())
                    .replace("%level%", String.valueOf(level)));
            target.sendMessage(getMessage("level-set-target")
                    .replace("%level%", String.valueOf(level)));
        } catch (NumberFormatException e) {
            sender.sendMessage(getMessage("invalid-number"));
        }
        return true;
    }

    private String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("messages.prefix", "&8[&6LevelPlayers&8] ") + 
            plugin.getConfig().getString("messages." + path, "&cСообщение не найдено: " + path));
    }
}