package com.example;

import com.example.utils.ColorUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private final LevelPlayers plugin;
    private FileConfiguration langConfig;
    private final Map<String, String> languageFiles = new HashMap<>();

    public LanguageManager(LevelPlayers plugin) {
        this.plugin = plugin;
        setupLanguages();
        loadLanguage();
    }

    private void setupLanguages() {
        languageFiles.put("ENGLISH", "english.yml");
        languageFiles.put("RUSSIAN", "russian.yml");
        languageFiles.put("UKRAINIAN", "ukrainian.yml");
        languageFiles.put("POLISH", "polish.yml");
        languageFiles.put("CHINESE", "chinese.yml");
        languageFiles.put("FRENCH", "french.yml");
        languageFiles.put("SPANISH", "spanish.yml");
    }

    public void loadLanguage() {
        String language = plugin.getConfig().getString("settings.language", "English").toUpperCase();
        String fileName = languageFiles.get(language);

        if (fileName == null) {
            plugin.getLogger().warning("Язык '" + language + "' не найден! Используется английский язык.");
            fileName = "english.yml";
        }

        File langFile = new File(plugin.getDataFolder() + "/lang", fileName);
        if (!langFile.exists()) {
            plugin.saveResource("lang/" + fileName, false);
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);

        // Проверяем обновления в файле ресурсов
        InputStream defaultLangStream = plugin.getResource("lang/" + fileName);
        if (defaultLangStream != null) {
            YamlConfiguration defaultLang = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultLangStream, StandardCharsets.UTF_8));
            langConfig.setDefaults(defaultLang);
        }
    }

    public String getMessage(String path) {
        String message = langConfig.getString(path);
        if (message == null) {
            return "Message not found: " + path;
        }
        return ColorUtils.colorize(message);
    }

    public String getMessageWithPrefix(String path) {
        return getMessage("prefix") + getMessage(path);
    }
}