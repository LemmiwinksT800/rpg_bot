package org.logic;

import org.model.*;

import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:game.db"; // Файл БД в корне проекта

    public DatabaseManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Statement stmt = conn.createStatement();

            // Таблица для игроков
            stmt.execute("CREATE TABLE IF NOT EXISTS players (" +
                            "id TEXT PRIMARY KEY, " +
                            "name TEXT, " +
                            "health INTEGER, " +
                            "max_health INTEGER, " +
                            "level INTEGER, " +
                            "faction TEXT, " +  // Для фракции (позже добавим)
                            "current_scenario_id TEXT, " +
                            "stats TEXT, " +    // Храним как JSON-строку"
                            "inventory TEXT" +  // Храним IDs предметов через запятую, напр. "sword,health_potion"
                    ")");

            // Таблица для сценариев
            stmt.execute("CREATE TABLE IF NOT EXISTS scenarios (" +
                    "id TEXT PRIMARY KEY, " +
                    "description TEXT" +
                    ")");

            // Таблица для выборов (choices) в сценариях
            stmt.execute("CREATE TABLE IF NOT EXISTS choices (" +
                    "scenario_id TEXT, " +
                    "choice_index INTEGER, " +  // Порядок выбора (0,1,2...)
                    "text TEXT, " +
                    "next_scenario_id TEXT, " +
                    "effect TEXT, " +
                    "PRIMARY KEY (scenario_id, choice_index)" +
                    ")");

            // Добавляем дефолтные сценарии, если их нет (замена хардкода из ScenarioGen)
            if (!scenarioExists("start")) {
                addScenario("start", "Вы стоите на перепутье. Перед вами три пути...");
                addChoice("start", 0, "Исследовать лес", "forest", null);
                addChoice("start", 1, "Пойти в город", "city", null);
                addChoice("start", 2, "Проверить снаряжение", "end", null);

                addScenario("forest", "Вы встретили голодного волка! Он рычит...");
                addChoice("forest", 0, "Атаковать", "end", "hp-15");
                addChoice("forest", 1, "Убежать", "end", null);
                addChoice("forest", 2, "Использовать зелье", "end", "stealth-check");

                addScenario("end", "Конец приключения.");
                // Добавлять свои сценарии сюда, Леша не путай ScenarioGen пережиток ))
            }
        } catch (SQLException e) {
            System.err.println("Ошибка инициализации БД: " + e.getMessage());
        }
    }

    // Проверка существования сценария
    private boolean scenarioExists(String id) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM scenarios WHERE id = ?")) {
            pstmt.setString(1, id);
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    // Добавление сценария
    public void addScenario(String id, String description) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO scenarios (id, description) VALUES (?, ?)")) {
            pstmt.setString(1, id);
            pstmt.setString(2, description);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка добавления сценария: " + e.getMessage());
        }
    }

    // Добавление выбора
    public void addChoice(String scenarioId, int choiceIndex, String text, String nextScenarioId, String effect) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO choices (scenario_id, choice_index, text, next_scenario_id, effect) VALUES (?, ?, ?, ?, ?)")) {
            pstmt.setString(1, scenarioId);
            pstmt.setInt(2, choiceIndex);
            pstmt.setString(3, text);
            pstmt.setString(4, nextScenarioId);
            pstmt.setString(5, effect);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка добавления выбора: " + e.getMessage());
        }
    }

    // Получение сценария по ID
    public Scenario getScenario(String id) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Получаем описание
            PreparedStatement pstmt = conn.prepareStatement("SELECT description FROM scenarios WHERE id = ?");
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) return null;
            String description = rs.getString("description");

            // Получаем choices
            List<Choice> choices = new ArrayList<>();
            pstmt = conn.prepareStatement("SELECT text, next_scenario_id, effect FROM choices WHERE scenario_id = ? ORDER BY choice_index");
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                choices.add(new Choice(rs.getString("text"), rs.getString("next_scenario_id"), rs.getString("effect")));
            }

            return new Scenario(id, description, choices);
        } catch (SQLException e) {
            System.err.println("Ошибка получения сценария: " + e.getMessage());
            return null;
        }
    }


    public void savePlayer(String playerId, Player player, String currentScenarioId) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO players (id, name, health, max_health, level, faction, current_scenario_id, stats, inventory) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            pstmt.setString(1, playerId);
            pstmt.setString(2, player.getName());
            pstmt.setInt(3, player.getHealth());
            pstmt.setInt(4, player.getMaxHealth());
            pstmt.setInt(5, player.getLevel());
            pstmt.setString(6, player.getFaction());  // Будет null пока
            pstmt.setString(7, currentScenarioId);
            pstmt.setString(8, mapToString(player.getStats()));  // Конверт Map в строку, напр. "stealth:10,strength:10"
            pstmt.setString(9, String.join(",", player.getInventory().stream().map(Item::getId).toList()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка сохранения игрока: " + e.getMessage());
        }
    }


    public Player loadPlayer(String playerId) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM players WHERE id = ?")) {
            pstmt.setString(1, playerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Player player = new Player(rs.getString("name"));
                player.setHealth(rs.getInt("health"));
                player.setMaxHealth(rs.getInt("max_health"));
                player.setLevel(rs.getInt("level"));
                player.setFaction(rs.getString("faction"));
                // Stats: парсим строку обратно в Map
                player.setStats(stringToMap(rs.getString("stats")));
                // Inventory: парсим и добавляем предметы
                String invStr = rs.getString("inventory");
                if (invStr != null && !invStr.isEmpty()) {
                    Arrays.stream(invStr.split(",")).forEach(id -> {
                        Item item = Item.findById(id);  // Нужно добавить в Item статический метод findById
                        if (item != null) player.addItem(item);
                    });
                }
                return player;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки игрока: " + e.getMessage());
        }
        return null;
    }

    // Вспомогательные: Map <-> String
    private String mapToString(Map<String, Integer> map) {
        if (map == null || map.isEmpty()) return "";
        return map.entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(java.util.stream.Collectors.joining(","));
    }

    private Map<String, Integer> stringToMap(String str) {
        Map<String, Integer> map = new HashMap<>();
        if (str == null || str.isEmpty()) return map;
        Arrays.stream(str.split(",")).forEach(s -> {
            String[] parts = s.split(":");
            if (parts.length == 2) {
                map.put(parts[0], Integer.parseInt(parts[1]));
            }
        });
        return map;
    }
}