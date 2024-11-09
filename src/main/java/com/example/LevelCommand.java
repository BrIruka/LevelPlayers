package com.example;

import org.bukkit.Bukkit;
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
            sender.sendMessage(plugin.getMessageWithPrefix("usage.set"));
            return true;
        }
    
        if (!sender.hasPermission("levelplayers.set")) {
            sender.sendMessage(plugin.getMessageWithPrefix("no-permission"));
            return true;
        }
    
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getMessageWithPrefix("player-not-found"));
            return true;
        }
    
        try {
            int level = Integer.parseInt(args[1]);
            int maxLevel = plugin.getMaxLevel();
            
            if (level < 1 || level > maxLevel) {
                sender.sendMessage(plugin.getMessageWithPrefix("invalid-level")
                    .replace("%max%", String.valueOf(maxLevel)));
                return true;
            }
    
            plugin.setPlayerLevel(target, level);
            sender.sendMessage(plugin.getMessageWithPrefix("level-set")
                    .replace("%player%", target.getName())
                    .replace("%level%", String.valueOf(level)));
            target.sendMessage(plugin.getMessageWithPrefix("level-set-target")
                    .replace("%level%", String.valueOf(level)));
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getMessageWithPrefix("invalid-number"));
        }
        return true;
    }
}