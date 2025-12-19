package org.logic;

import org.model.*;

import java.sql.*;
import java.util.*;

public class DatabaseManager implements PlayerRepository, ScenarioRepository {
    private static final String DB_URL = "jdbc:sqlite:game.db"; // Файл БД в корне проекта

    public DatabaseManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Statement stmt = conn.createStatement();

            stmt.execute("CREATE TABLE IF NOT EXISTS campaigns (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT, " +
                    "description TEXT, " +
                    "start_scenario_id TEXT, " +
                    "faction TEXT, " +
                    "start_stats TEXT" +  // "strength:15,intelligence:10"
                    ")");

            if (!campaignExists("knight")) {
                addCampaign("knight", "Рыцарь и принцесса", "Вы рыцарь, спасающий принцессу от дракона.", "castle", "Knight", "strength:15,stealth:5");
                addCampaign("engineer", "Космический инженер", "Вы инженер на корабле с инопланетянином.", "spaceship", "Engineer", "intelligence:15,strength:5");
            }


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


            stmt.execute("CREATE TABLE IF NOT EXISTS scenarios (" +
                    "id TEXT PRIMARY KEY, " +
                    "description TEXT" +
                    ")");


            stmt.execute("CREATE TABLE IF NOT EXISTS choices (" +
                    "scenario_id TEXT, " +
                    "choice_index INTEGER, " +
                    "text TEXT, " +
                    "next_scenario_id TEXT, " +
                    "effect TEXT, " +
                    "PRIMARY KEY (scenario_id, choice_index)" +
                    ")");

            // knight
            if (!scenarioExists("castle")) {
                addScenario("castle", "Вы стоите у ворот замка. Принцесса похищена драконом. Что делать?");
                addChoice("castle", 0, "Идти в лес на поиски", "forest_knight", "add:health_potion");
                addChoice("castle", 1, "Подняться в горы", "mountains", "check:strength");
            }
            if (!scenarioExists("forest_knight")) {
                addScenario("forest_knight", "В лесу вы встречаете разбойников. Они требуют золото.");
                addChoice("forest_knight", 0, "Сразиться", "cave", "hp-15");
                addChoice("forest_knight", 1, "Убежать уйти", "cave", "check:stealth");
            }
            if (!scenarioExists("mountains")) {
                addScenario("mountains", "В горах вы находите пещеру дракона.");
                addChoice("mountains", 0, "Войти", "cave", null);
                addChoice("mountains", 1, "Вернуться в замок", "castle", "heal:20");
            }
            if (!scenarioExists("cave")) {
                addScenario("cave", "Вы в пещере. Дракон спит. Принцесса в клетке.");
                addChoice("cave", 0, "Атаковать дракона", "end_knight", "check:strength");
                addChoice("cave", 1, "Украсть принцессу тихо", "end_knight", "check:stealth");
            }
            if (!scenarioExists("end_knight")) {
                addScenario("end_knight", "Вы спасли принцессу! Победа! (Конец кампании)");
            }

            // engineer
            if (!scenarioExists("spaceship")) {
                addScenario("spaceship", "Вы на космическом корабле. Слышна тревога — инопланетянин на борту.");
                addChoice("spaceship", 0, "Идти в машинный зал", "engine_room", "add:sword");
                addChoice("spaceship", 1, "Проверить каюты", "alien_encounter", "check:intelligence");
            }
            if (!scenarioExists("engine_room")) {
                addScenario("engine_room", "В машинном зале поломка. Нужно починить.");
                addChoice("engine_room", 0, "Починить быстро", "alien_encounter", "check:intelligence");
                addChoice("engine_room", 1, "Вызвать помощь", "end_engineer", "hp-10");
            }
            if (!scenarioExists("alien_encounter")) {
                addScenario("alien_encounter", "Вы встречаете инопланетянина. Он агрессивен.");
                addChoice("alien_encounter", 0, "Сразиться", "end_engineer", "check:strength");
                addChoice("alien_encounter", 1, "Перехитрить", "end_engineer", "check:intelligence");
            }
            if (!scenarioExists("end_engineer")) {
                addScenario("end_engineer", "Вы справились с угрозой! Корабль спасён. (Конец кампании)");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка инициализации БД: " + e.getMessage());
        }
    }


    private boolean scenarioExists(String id) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM scenarios WHERE id = ?")) {
            pstmt.setString(1, id);
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }


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
                player.setCurrentScenarioId(rs.getString("current_scenario_id"));
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

    public Map<String, Integer> stringToMap(String str) {
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

    @Override
    public List<String> getAllScenarioIds() {  // NEW: Получаем список ID сценариев
        List<String> ids = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id FROM scenarios ORDER BY id")) {
            while (rs.next()) {
                ids.add(rs.getString("id"));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения списка сценариев: " + e.getMessage());
        }
        return ids;
    }

    private boolean campaignExists(String id) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM campaigns WHERE id = ?")) {
            pstmt.setString(1, id);
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    public void addCampaign(String id, String name, String description, String startScenarioId, String faction, String startStats) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO campaigns (id, name, description, start_scenario_id, faction, start_stats) VALUES (?, ?, ?, ?, ?, ?)")) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, description);
            pstmt.setString(4, startScenarioId);
            pstmt.setString(5, faction);
            pstmt.setString(6, startStats);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка добавления кампании: " + e.getMessage());
        }
    }

    public List<Campaign> getAllCampaigns() {  // Новый класс Campaign (ниже)
        List<Campaign> campaigns = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM campaigns")) {
            while (rs.next()) {
                campaigns.add(new Campaign(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("start_scenario_id"),
                        rs.getString("faction"),
                        rs.getString("start_stats")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения кампаний: " + e.getMessage());
        }
        return campaigns;
    }

    public Campaign getCampaignById(String id) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM campaigns WHERE id = ?")) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Campaign(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("start_scenario_id"),
                        rs.getString("faction"),
                        rs.getString("start_stats")
                );
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения кампании: " + e.getMessage());
        }
        return null;
    }
}