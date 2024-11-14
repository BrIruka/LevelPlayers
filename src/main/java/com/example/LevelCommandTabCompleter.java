package com.example;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LevelCommandTabCompleter implements TabCompleter {
    private final LevelPlayers plugin;

    public LevelCommandTabCompleter(LevelPlayers plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Добавляем команду help
            if ("help".startsWith(args[0].toLowerCase())) {
                completions.add("help");
            }
            // Добавляем подкоманды give и take
            if (sender.hasPermission("levelplayers.give") && "give".startsWith(args[0].toLowerCase())) {
                completions.add("give");
            }
            if (sender.hasPermission("levelplayers.take") && "take".startsWith(args[0].toLowerCase())) {
                completions.add("take");
            }
            // Если у игрока есть право на установку уровня, показываем список игроков
            if (sender.hasPermission("levelplayers.set")) {
                String partialPlayer = args[0].toLowerCase();
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(partialPlayer))
                        .collect(Collectors.toList()));
            }
        }
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("take")) {
                // Для give и take показываем только список игроков
                String partialPlayer = args[1].toLowerCase();
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(partialPlayer))
                        .collect(Collectors.toList()));
            } else {
                // Для обычной установки уровня показываем доступные уровни
                String partialLevel = args[1].toLowerCase();
                for (int i = 1; i <= plugin.getMaxLevel(); i++) {
                    String level = String.valueOf(i);
                    if (level.startsWith(partialLevel)) {
                        completions.add(level);
                    }
                }
            }
        }

        return completions;
    }
}