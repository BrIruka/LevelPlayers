package com.example;

import com.example.database.DatabaseManager;
import com.example.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LevelPlayers extends JavaPlugin {
    private static LevelPlayers instance;
    private Map<UUID, Integer> playerLevels;
    private DatabaseManager databaseManager;
    private boolean useDatabase;
    private File playerDataFile;
    private FileConfiguration playerData;
    private FileConfiguration config;
    private int maxLevel;
    private String defaultFormat;
    private LanguageManager languageManager;

    @Override
    public void onEnable() {
        instance = this;
        playerLevels = new HashMap<>();

        saveDefaultConfig();
        config = getConfig();

        useDatabase = config.getBoolean("database.enabled", true);

        if (useDatabase) {
            databaseManager = new DatabaseManager(this);
            playerLevels = databaseManager.getAllPlayerLevels();
        } else {
            loadPlayerData(); // Старый метод загрузки из файла
        }

        File langFolder = new File(getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        languageManager = new LanguageManager(this);
        loadLevelSettings();
        
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
        Bukkit.getConsoleSender().sendMessage("§6║ §7Author: §fIrukaMine");
        Bukkit.getConsoleSender().sendMessage("§6║ §7Status: §aEnabled");
        Bukkit.getConsoleSender().sendMessage("§6╚════════════════════════════════════");

        // Регистрируем команды
        getCommand("level").setExecutor(new LevelCommand(this));
        getCommand("level").setTabCompleter(new LevelCommandTabCompleter(this));

        getCommand("levelplayers").setExecutor(new LevelPlayersCommand(this));
        getCommand("levelplayers").setTabCompleter(new LevelPlayersCommandTabCompleter());
    }

    @Override
    public void onDisable() {
        if (useDatabase && databaseManager != null) {
            databaseManager.close();
        } else {
            savePlayerData(); // Старый метод сохранения в файл
        }
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

    private void loadPlayerData() {
        playerDataFile = new File(getDataFolder(), "playerdata.yml");
        if (!playerDataFile.exists()) {
            saveResource("playerdata.yml", false);
        }
        playerData = YamlConfiguration.loadConfiguration(playerDataFile);

        // Загружаем данные игроков
        if (playerData.contains("players")) {
            for (String uuidString : playerData.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                int level = playerData.getInt("players." + uuidString);
                playerLevels.put(uuid, level);
            }
        }
    }

    private void savePlayerData() {
        try {
            // Очищаем старые данные
            playerData.set("players", null);

            // Сохраняем текущие данные
            for (Map.Entry<UUID, Integer> entry : playerLevels.entrySet()) {
                playerData.set("players." + entry.getKey().toString(), entry.getValue());
            }

            // Сохраняем файл
            playerData.save(playerDataFile);
        } catch (IOException e) {
            getLogger().severe("Не удалось сохранить данные игроков!");
            e.printStackTrace();
        }
    }

    private void loadLevelSettings() {
        maxLevel = config.getInt("level-settings.max-level", 5);
        defaultFormat = config.getString("level-settings.format", "&7Уровень &6%level%");
    }


    public static LevelPlayers getInstance() {
        return instance;
    }

    public String getLevelDisplay(int level) {
        String format = config.getString("levels." + level);

        if (format == null) {
            format = defaultFormat.replace("%level%", String.valueOf(level));
        }

        return ColorUtils.colorize(format);
    }

    public int getPlayerLevel(Player player) {
        return playerLevels.getOrDefault(player.getUniqueId(), 1);
    }

    public void setPlayerLevel(Player player, int level) {
        if (player == null) return;

        if (level >= 1 && level <= maxLevel) {
            UUID uuid = player.getUniqueId();
            playerLevels.put(uuid, level);

            if (useDatabase) {
                if (databaseManager != null) {
                    // Асинхронное сохранение в БД
                    Bukkit.getScheduler().runTaskAsynchronously(this, () ->
                            databaseManager.setPlayerLevel(uuid, level));
                } else {
                    getLogger().warning("DatabaseManager is null but database storage is enabled!");
                }
            } else {
                savePlayerData();
            }
        }
    }

    public String getMessage(String path) {
        return languageManager.getMessage(path);
    }

    public String getMessageWithPrefix(String path) {
        return languageManager.getMessageWithPrefix(path);
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    // Метод для перезагрузки плагина
    public void reloadPlugin() {
        // Сохраняем текущие данные перед перезагрузкой
        if (useDatabase && databaseManager != null) {
            databaseManager.close();
        } else {
            savePlayerData();
        }

        // Перезагружаем конфиг
        reloadConfig();
        config = getConfig();

        // Проверяем новый режим хранения
        boolean newDatabaseMode = config.getBoolean("database.enabled", true);

        // Если режим хранения изменился
        if (newDatabaseMode != useDatabase) {
            useDatabase = newDatabaseMode;
            if (useDatabase) {
                databaseManager = new DatabaseManager(this);
                playerLevels = databaseManager.getAllPlayerLevels();
            } else {
                if (databaseManager != null) {
                    databaseManager.close();
                    databaseManager = null;
                }
                loadPlayerData();
            }
        }

        languageManager.loadLanguage();
        loadLevelSettings();

        // Перерегистрируем PlaceholderAPI расширение
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new LevelPlayersExpansion(this).register();
        }

        // Отправляем сообщение в консоль
        Bukkit.getConsoleSender().sendMessage("§6[LevelPlayers] §fПлагин перезагружен!");
    }

    public boolean isUsingDatabase() {
        return useDatabase && databaseManager != null;
    }
}
