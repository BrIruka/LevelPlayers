package com.example;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LevelPlayersCommand implements CommandExecutor {
    private final LevelPlayers plugin;

    public LevelPlayersCommand(LevelPlayers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(plugin.getMessageWithPrefix("usage.reload"));
            return true;
        }

        if (!sender.hasPermission("levelplayers.reload")) {
            sender.sendMessage(plugin.getMessageWithPrefix("no-permission"));
            return true;
        }

        // Перезагружаем конфиг
        plugin.reloadConfig();
        plugin.reloadPlugin();
        sender.sendMessage(plugin.getMessageWithPrefix("reload-success"));

        return true;
    }
}