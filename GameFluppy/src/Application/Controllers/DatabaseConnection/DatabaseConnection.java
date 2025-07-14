package Application.Controllers.DatabaseConnection;

import java.sql.*;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/flappybird";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "ashik"; // Add your password if needed

    public DatabaseConnection() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS scores (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "player_name VARCHAR(50) NOT NULL, " +
                    "score INT NOT NULL, " +
                    "game_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            System.err.println("Database initialization failed:");
            e.printStackTrace();
        }
    }

    public void saveScore(String playerName, int score) {
        String sql = "INSERT INTO scores (player_name, score) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, playerName);
            pstmt.setInt(2, score);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to save score:");
            e.printStackTrace();
        }
    }

    public int getHighestScore() {
        String sql = "SELECT MAX(score) as max_score FROM scores";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("max_score");
            }
        } catch (SQLException e) {
            System.err.println("Failed to get high score:");
            e.printStackTrace();
        }

        return 0;
    }
}