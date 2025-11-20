package com.warehouse.model;

import com.warehouse.config.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DBUtil {
    private static final Logger logger = LoggerFactory.getLogger(DBUtil.class);
    private static final String URL = ConfigManager.get("db.url", "jdbc:mysql://localhost:3306/warehouse_db");
    private static final String USER = ConfigManager.get("db.user", "root");
    private static final String PASSWORD = ConfigManager.get("db.password", "");
    
    private static HikariDataSource dataSource;
    
    static {
        initializeConnectionPool();
        SchemaMigrator.runMigrations(URL, USER, PASSWORD);
    }
    
    /**
     * Initializes HikariCP connection pool for better performance and connection management
     */
    private static void initializeConnectionPool() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL);
            config.setUsername(USER);
            config.setPassword(PASSWORD);
            
            // Connection pool settings
            config.setMaximumPoolSize(10); // Maximum 10 connections
            config.setMinimumIdle(2); // Keep at least 2 idle connections
            config.setConnectionTimeout(30000); // 30 seconds timeout
            config.setIdleTimeout(600000); // 10 minutes idle timeout
            config.setMaxLifetime(1800000); // 30 minutes max lifetime
            config.setLeakDetectionThreshold(60000); // Detect connection leaks
            
            // MySQL-specific settings
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            
            dataSource = new HikariDataSource(config);
            logger.info("Connection pool initialized successfully: {} connections available", config.getMaximumPoolSize());
        } catch (Exception e) {
            logger.error("Failed to initialize connection pool, falling back to direct connections", e);
            dataSource = null;
        }
    }
    
    /**
     * Gets a connection from the pool (or creates a direct connection if pool unavailable)
     */
    public static Connection getConnection() throws SQLException {
        try {
            if (dataSource != null && !dataSource.isClosed()) {
                Connection conn = dataSource.getConnection();
                logger.debug("Connection obtained from pool");
                return conn;
            }
        } catch (SQLException e) {
            logger.warn("Failed to get connection from pool: {}, falling back to direct connection", e.getMessage());
        }
        
        // Fallback to direct connection if pool is unavailable
        try {
            Connection conn = java.sql.DriverManager.getConnection(URL, USER, PASSWORD);
            logger.debug("Direct connection created (pool unavailable)");
            return conn;
        } catch (SQLException e) {
            logger.error("Failed to create database connection: {}", e.getMessage(), e);
            throw new SQLException("Impossible de se connecter à la base de données. Vérifiez que XAMPP est démarré et que MySQL fonctionne.", e);
        }
    }
    
    /**
     * Tests the database connection
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean isValid = conn != null && !conn.isClosed();
            if (isValid) {
                logger.info("Database connection test successful");
            }
            return isValid;
        } catch (SQLException e) {
            logger.error("Database connection test failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Closes a connection (returns it to the pool if using HikariCP)
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close(); // HikariCP connections are returned to pool on close()
                logger.debug("Connection closed (returned to pool)");
            } catch (SQLException e) {
                logger.error("Error closing connection: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Shuts down the connection pool (called on application exit)
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Connection pool shut down");
        }
    }
} 