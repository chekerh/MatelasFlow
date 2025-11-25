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
        // Add initial_stock column to track original stock received
        ensureColumn(conn, "mattress", "initial_stock",
            "ALTER TABLE `mattress` ADD COLUMN `initial_stock` INT NOT NULL DEFAULT 0 AFTER `quantity`");
        // Add quantity_sold column to track total quantity sold
        ensureColumn(conn, "mattress", "quantity_sold",
            "ALTER TABLE `mattress` ADD COLUMN `quantity_sold` INT NOT NULL DEFAULT 0 AFTER `initial_stock`");
        // Make prix column nullable if it exists (prix is only used in transactions, not in inventory)
        if (columnExists(conn, "mattress", "prix")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE `mattress` MODIFY COLUMN `prix` DECIMAL(10,2) NULL COMMENT 'Prix de vente (non utilisé dans l\\'inventaire, seulement dans les transactions)'");
            } catch (SQLException e) {
                // Ignore if modification fails (might already be nullable or other issue)
                System.err.println("Note: Could not modify prix column (may already be nullable): " + e.getMessage());
            }
        }
        // If initial_stock is 0 but quantity > 0, set initial_stock = quantity (for existing data)
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("UPDATE `mattress` SET initial_stock = quantity WHERE initial_stock = 0 AND quantity > 0");
        } catch (SQLException e) {
            // Ignore if column doesn't exist yet
        }
        // Calculate initial quantity_sold from existing sales transactions
        // Update ALL mattresses (set to 0 if no sales) to ensure consistency
        try (Statement stmt = conn.createStatement()) {
            if (columnExists(conn, "mattress", "quantity_sold") && columnExists(conn, "transaction", "type")) {
                stmt.executeUpdate(
                    "UPDATE mattress m " +
                    "SET m.quantity_sold = COALESCE((" +
                    "    SELECT SUM(t.quantity) " +
                    "    FROM transaction t " +
                    "    WHERE t.mattress_id = m.id AND t.type = 'Vente'" +
                    "), 0)"
                );
            }
        } catch (SQLException e) {
            // Ignore if columns don't exist yet or if there are no transactions
        }
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

