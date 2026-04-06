package dao;

import model.User;
import util.DatabaseConnection;

import java.sql.*;

/**
 * User Data Access Object
 * Handles all database operations for user accounts
 */
public class UserDAO {
    private Connection connection;

    public UserDAO() {
        try {
            this.connection = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            this.connection = null;
        }
    }

    private void checkConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DatabaseConnection.getInstance().getConnection();
        }
    }

    /**
     * Create the users table if it doesn't exist (Migration)
     */
    public void ensureUserTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "user_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        try {
            checkConnection();
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            System.err.println("Error creating users table: " + e.getMessage());
        }
    }

    /**
     * Register a new user
     */
    public boolean registerUser(String name, String email, String password) {
        String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
        try {
            checkConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setString(3, hashPassword(password));
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Authenticate a user
     */
    public User loginUser(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try {
            checkConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, email);
                stmt.setString(2, hashPassword(password));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        User user = new User();
                        user.setUserId(rs.getInt("user_id"));
                        user.setName(rs.getString("name"));
                        user.setEmail(rs.getString("email"));
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
        }
        return null;
    }

    /**
     * SHA-256 password hashing with salt
     * Matches the logic in server.js
     */
    private String hashPassword(String password) {
        try {
            String salt = "bus_salt_2026";
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((password + salt).getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error hashing password", ex);
        }
    }
}
