package util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database Connection Utility
 * Singleton pattern for managing database connections
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;
    private String url;
    private String username;
    private String password;
    private String driver;

    // Private constructor for singleton pattern
    private DatabaseConnection() {
        loadProperties();
    }

    /**
     * Load database configuration from properties file
     */
    private void loadProperties() {
        try {
            Properties props = new Properties();
            // First try loading from classpath
            InputStream input = getClass().getClassLoader().getResourceAsStream("config/db.properties");

            if (input == null) {
                // Try loading from file system as fallback
                try {
                    input = new java.io.FileInputStream("src/config/db.properties");
                } catch (java.io.FileNotFoundException e) {
                    // Ignore, will use defaults
                }
            }

            if (input == null) {
                // Default values if properties file not found
                System.out.println("[INFO] Using default database configuration (properties file not found)");
                this.url = "jdbc:mysql://localhost:3306/bus_booking_db";
                this.username = "root";
                this.password = "TUsar@2006";
                this.driver = "com.mysql.cj.jdbc.Driver";
            } else {
                props.load(input);
                this.url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/bus_booking_db");
                this.username = props.getProperty("db.username", "root");
                this.password = props.getProperty("db.password", "");
                this.driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
                input.close();
            }

            this.url = getEnvOrDefault("BUS_DB_URL", this.url);
            this.username = getEnvOrDefault("BUS_DB_USERNAME", this.username);
            this.password = getEnvOrDefault("BUS_DB_PASSWORD", this.password);
            this.driver = getEnvOrDefault("BUS_DB_DRIVER", this.driver);

            // Load JDBC driver
            Class.forName(driver);

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load database properties: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] JDBC Driver not found: " + e.getMessage());
        }
    }

    private String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

    /**
     * Get singleton instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Get database connection
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, username, password);
            connection.setAutoCommit(true);
        }
        return connection;
    }

    /**
     * Get a new connection (for transactions)
     */
    public Connection getNewConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Close the connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to close connection: " + e.getMessage());
        }
    }

    /**
     * Test database connection
     */
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Database connection test failed: " + e.getMessage());
        }
        return false;
    }
}
