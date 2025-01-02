package com.example.database;

import com.example.LevelPlayers;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {
    private final LevelPlayers plugin;
    private final File databaseFile;
    private Connection connection;

    public DatabaseManager(LevelPlayers plugin) {
        this.plugin = plugin;
        this.databaseFile = new File(plugin.getDataFolder(), "database.db");
        setupDatabase();
    }

    private void setupDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            getConnection();
            createTables();
            plugin.getLogger().info("Успешное подключение к SQLite!");
        } catch (Exception e) {
            plugin.getLogger().severe("Не удалось подключиться к SQLite!");
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            connection.setAutoCommit(true);
        }
        return connection;
    }

    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS player_levels ("
                + "uuid TEXT PRIMARY KEY,"
                + "level INTEGER NOT NULL DEFAULT 1,"
                + "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ");";

        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Не удалось создать таблицы!");
            e.printStackTrace();
        }
    }

    public synchronized void setPlayerLevel(UUID uuid, int level) {
        String sql = "INSERT OR REPLACE INTO player_levels (uuid, level) VALUES (?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setInt(2, level);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при сохранении уровня игрока: " + uuid, e);
            // Пробуем переподключиться при ошибке
            try {
                connection.close();
                connection = null;
                getConnection();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Не удалось переподключиться к базе данных", ex);
            }
        }
    }

    public synchronized int getPlayerLevel(UUID uuid) {
        String sql = "SELECT level FROM player_levels WHERE uuid = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("level");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при получении уровня игрока: " + uuid, e);
            // Пробуем переподключиться при ошибке
            try {
                connection.close();
                connection = null;
                getConnection();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Не удалось переподключиться к базе данных", ex);
            }
        }
        return 1;
    }

    public synchronized Map<UUID, Integer> getAllPlayerLevels() {
        Map<UUID, Integer> levels = new HashMap<>();
        String sql = "SELECT uuid, level FROM player_levels";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                int level = rs.getInt("level");
                levels.put(uuid, level);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при загрузке уровней игроков", e);
            // Пробуем переподключиться при ошибке
            try {
                connection.close();
                connection = null;
                getConnection();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Не удалось переподключиться к базе данных", ex);
            }
        }
        return levels;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при закрытии соединения с базой данных", e);
        }
    }
}