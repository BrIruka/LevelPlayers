package com.example;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LevelPlayersExpansion extends PlaceholderExpansion {
    private final LevelPlayers plugin;

    public LevelPlayersExpansion(LevelPlayers plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "levelplayers";
    }

    @Override
    public @NotNull String getAuthor() {
        return "OwlStudio";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    @Nullable
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        if (player == null || !player.isOnline()) {
            return "";
        }

        // Плейсхолдер %levelplayers_level%
        if (identifier.equals("level")) {
            int level = plugin.getPlayerLevel((Player) player);
            return plugin.getLevelDisplay(level);
        }

        return null;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }
}