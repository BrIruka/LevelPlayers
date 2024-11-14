package com.example;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class LevelCommand implements CommandExecutor {
    private final LevelPlayers plugin;
    public LevelCommand(LevelPlayers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getMessageWithPrefix("usage.set"));
            return true;
        }

        // Обработка подкоманд give и take
        if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("take"))) {
            boolean isGive = args[0].equalsIgnoreCase("give");
            String permission = isGive ? "levelplayers.give" : "levelplayers.take";

            if (!sender.hasPermission(permission)) {
                sender.sendMessage(plugin.getMessageWithPrefix("no-permission"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.getMessageWithPrefix("player-not-found"));
                return true;
            }

            int currentLevel = plugin.getPlayerLevel(target);
            int newLevel = isGive ? currentLevel + 1 : currentLevel - 1;
            int maxLevel = plugin.getMaxLevel();

            if (isGive && newLevel > maxLevel) {
                sender.sendMessage(plugin.getMessageWithPrefix("max-level-reached"));
                return true;
            }

            if (!isGive && newLevel < 1) {
                sender.sendMessage(plugin.getMessageWithPrefix("min-level-reached"));
                return true;
            }

            plugin.setPlayerLevel(target, newLevel);
            String messageKey = isGive ? "level-increased" : "level-decreased";
            sender.sendMessage(plugin.getMessageWithPrefix(messageKey)
                    .replace("%player%", target.getName())
                    .replace("%level%", String.valueOf(newLevel)));
            target.sendMessage(plugin.getMessageWithPrefix(messageKey + "-target")
                    .replace("%level%", String.valueOf(newLevel)));
            return true;
        }

        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
            // Показываем меню помощи
            sender.sendMessage(plugin.getMessageWithPrefix("help.header")
                    .replace("%version%", plugin.getDescription().getVersion()));

            sender.sendMessage(plugin.getMessageWithPrefix("help.commands.help"));
            sender.sendMessage(plugin.getMessageWithPrefix("help.commands.set"));
            sender.sendMessage(plugin.getMessageWithPrefix("help.commands.give"));
            sender.sendMessage(plugin.getMessageWithPrefix("help.commands.take"));
            sender.sendMessage(plugin.getMessageWithPrefix("help.commands.reload"));

            sender.sendMessage(plugin.getMessageWithPrefix("help.footer"));
            return true;
        }

        // Существующая логика для /level <player> <level>
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