package com.example;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LevelPlayers extends JavaPlugin {
    private static LevelPlayers instance;
    private Map<UUID, Integer> playerLevels;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        instance = this;
        playerLevels = new HashMap<>();
        
        // Сохраняем конфиг по умолчанию
        saveDefaultConfig();
        config = getConfig();
        
        // Проверяем версию сервера
        String version = Bukkit.getBukkitVersion().split("-")[0];
        if (!isVersionSupported(version)) {
            getLogger().warning("Эта версия сервера (" + version + ") может быть не полностью совместима!");
            getLogger().warning("Рекомендуемые версии: 1.18.x - 1.21.x");
        }

        // Регистрируем PlaceholderAPI расширение
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new LevelPlayersExpansion(this).register();
            getLogger().info("PlaceholderAPI найден и подключен!");
        } else {
            getLogger().warning("PlaceholderAPI не найден! Плейсхолдеры работать не будут!");
        }
        
        // Красивое сообщение о запуске
        Bukkit.getConsoleSender().sendMessage("§6╔════════════════════════════════════");
        Bukkit.getConsoleSender().sendMessage("§6║ §fLevelPlayers §7v" + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage("§6║ §7Author: §fOwlStudio");
        Bukkit.getConsoleSender().sendMessage("§6║ §7Status: §aEnabled");
        Bukkit.getConsoleSender().sendMessage("§6╚════════════════════════════════════");

        // Регистрируем команды
        getCommand("level").setExecutor(new LevelCommand(this));
        getCommand("levelplayers").setExecutor(new LevelPlayersCommand(this));
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("§6╔════════════════════════════════════");
        Bukkit.getConsoleSender().sendMessage("§6║ §fLevelPlayers §7v" + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage("§6║ §7Status: §cDisabled");
        Bukkit.getConsoleSender().sendMessage("§6╚════════════════════════════════════");
    }

    private boolean isVersionSupported(String version) {
        String[] versionParts = version.split("\\.");
        int major = Integer.parseInt(versionParts[0]);
        int minor = Integer.parseInt(versionParts[1]);
        return major == 1 && minor >= 18 && minor <= 21;
    }

    public static LevelPlayers getInstance() {
        return instance;
    }

    public String getLevelDisplay(int level) {
        return config.getString("levels." + level, "&7Уровень &6" + level);
    }

    public int getPlayerLevel(Player player) {
        return playerLevels.getOrDefault(player.getUniqueId(), 1);
    }

    public void setPlayerLevel(Player player, int level) {
        if (level >= 1 && level <= 5) {
            playerLevels.put(player.getUniqueId(), level);
        }
    }

    public String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("messages." + path, "Сообщение не найдено: " + path));
    }

    public String getMessageWithPrefix(String path) {
        return ChatColor.translateAlternateColorCodes('&',
            getConfig().getString("messages.prefix", "&8[&6LevelPlayers&8] ") +
            getConfig().getString("messages." + path, "Сообщение не найдено: " + path));
    }
    // Добавляем метод для перезагрузки плагина
    public void reloadPlugin() {
        // Перезагружаем конфиг
        reloadConfig();
        config = getConfig();
        
        // Перерегистрируем PlaceholderAPI расширение
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new LevelPlayersExpansion(this).register();
        }
        
        // Отправляем сообщение в консоль
        Bukkit.getConsoleSender().sendMessage("§6[LevelPlayers] §fПлагин перезагружен!");
    }
}
