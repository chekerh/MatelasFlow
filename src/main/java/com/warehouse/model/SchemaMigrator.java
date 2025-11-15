package com.warehouse.model;

import java.sql.*;

/**
 * Performs lightweight, code-driven schema migrations so the app
 * can work even if SQL scripts were not run manually.
 */
public final class SchemaMigrator {
    private SchemaMigrator() {}

    public static void runMigrations(String url, String user, String password) {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            ensureMattressReferenceAndPricing(conn);
            ensureMattressSortOrder(conn);
            ensureTransactionEnhancements(conn);
        } catch (SQLException e) {
            System.err.println("Impossible d'exécuter les migrations automatiques: " + e.getMessage());
        }
    }

    private static void ensureMattressReferenceAndPricing(Connection conn) throws SQLException {
        if (columnExists(conn, "mattress", "brand")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE `mattress` CHANGE COLUMN `brand` `reference` VARCHAR(100) NULL");
            } catch (SQLException e) {
                if (!e.getMessage().contains("Unknown column 'brand'")) {
                    throw e;
                }
            }
        }
        ensureColumn(conn, "mattress", "unit_price",
            "ALTER TABLE `mattress` ADD COLUMN `unit_price` DECIMAL(10,2) NOT NULL DEFAULT 0 AFTER `quantity`");
    }

    private static void ensureMattressSortOrder(Connection conn) throws SQLException {
        ensureColumn(conn, "mattress", "sort_order",
            "ALTER TABLE `mattress` ADD COLUMN `sort_order` INT NOT NULL DEFAULT 0 AFTER `prix`");
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("UPDATE `mattress` SET sort_order = id WHERE sort_order IS NULL OR sort_order = 0");
        }
    }

    private static void ensureTransactionEnhancements(Connection conn) throws SQLException {
        ensureColumn(conn, "transaction", "expected_return_date",
            "ALTER TABLE `transaction` ADD COLUMN `expected_return_date` DATE AFTER `prix`");
        ensureColumn(conn, "transaction", "sort_order",
            "ALTER TABLE `transaction` ADD COLUMN `sort_order` INT NOT NULL DEFAULT 0 AFTER `expected_return_date`");
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("UPDATE `transaction` SET sort_order = id WHERE sort_order IS NULL OR sort_order = 0");
        }
    }

    private static void ensureColumn(Connection conn, String table, String column, String alterSql) throws SQLException {
        if (columnExists(conn, table, column)) {
            return;
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(alterSql);
        }
    }

    private static boolean columnExists(Connection conn, String table, String column) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        return hasColumn(metaData, conn.getCatalog(), table, column)
            || hasColumn(metaData, conn.getCatalog(), table.toUpperCase(), column.toUpperCase())
            || hasColumn(metaData, conn.getCatalog(), table.toLowerCase(), column.toLowerCase());
    }

    private static boolean hasColumn(DatabaseMetaData metaData, String catalog, String table, String column) throws SQLException {
        try (ResultSet rs = metaData.getColumns(catalog, null, table, column)) {
            return rs.next();
        }
    }
}

