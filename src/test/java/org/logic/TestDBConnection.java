package org.logic;

import java.sql.*;

public class TestDBConnection {
    private static final String DB_URL = "jdbc:sqlite:game.db";

    public static void main(String[] args) {
        try {
            // Явная загрузка драйвера (на всякий случай)
            Class.forName("org.sqlite.JDBC");

            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                System.out.println("Подключение к БД успешно!");


                Statement stmt = conn.createStatement();
                stmt.execute("CREATE TABLE IF NOT EXISTS test_table (id INTEGER PRIMARY KEY, name TEXT)");

                stmt.execute("INSERT INTO test_table (name) VALUES ('Test Entry')");

                ResultSet rs = stmt.executeQuery("SELECT * FROM test_table");
                while (rs.next()) {
                    System.out.println("ID: " + rs.getInt("id") + ", Name: " + rs.getString("name"));
                }

                System.out.println("Тест пройден: БД работает!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Драйвер не найден: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Ошибка БД: " + e.getMessage());
        }
    }
}