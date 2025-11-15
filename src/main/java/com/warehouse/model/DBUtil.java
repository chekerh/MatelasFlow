package com.warehouse.model;

import com.warehouse.config.ConfigManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

public class DBUtil {
    private static final String URL = ConfigManager.get("db.url", "jdbc:mysql://localhost:3306/warehouse_db");
    private static final String USER = ConfigManager.get("db.user", "root");
    private static final String PASSWORD = ConfigManager.get("db.password", "");
    private static final AtomicBoolean LOGGED = new AtomicBoolean(false);

    static {
        SchemaMigrator.runMigrations(URL, USER, PASSWORD);
    }
    
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            if (LOGGED.compareAndSet(false, true)) {
                System.out.println("[DBUtil] Connected to " + URL + " as " + USER);
            }
            return conn;
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données: " + e.getMessage());
            throw new SQLException("Impossible de se connecter à la base de données. Vérifiez que XAMPP est démarré et que MySQL fonctionne.", e);
        }
    }
    
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Test de connexion échoué: " + e.getMessage());
            return false;
        }
    }
    
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
            }
        }
    }
} 