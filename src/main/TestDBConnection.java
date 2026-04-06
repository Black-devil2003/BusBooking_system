package main;

import util.DatabaseConnection;

public class TestDBConnection {
    public static void main(String[] args) {
        System.out.println("Testing Database Connection...");
        DatabaseConnection db = DatabaseConnection.getInstance();
        boolean isConnected = db.testConnection();
        
        if (isConnected) {
            System.out.println("SUCCESS: Database connection established successfully!");
        } else {
            System.out.println("FAILED: Could not connect to the database. Please check your config/db.properties or default credentials.");
        }
    }
}
