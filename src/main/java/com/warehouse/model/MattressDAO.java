package com.warehouse.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MattressDAO {
    private static final Logger logger = LoggerFactory.getLogger(MattressDAO.class);
    private static volatile boolean SORT_ORDER_SUPPORTED = true;
    private static String lastError = null;
    
    /**
     * Get the last error message from add/update operations
     */
    public static String getLastError() {
        return lastError;
    }

    public static List<Mattress> getAllMattresses() {
        boolean trySortOrder = SORT_ORDER_SUPPORTED;
        while (true) {
            try {
                return fetchMattresses(trySortOrder);
            } catch (SQLException e) {
                if (trySortOrder && isUnknownColumn(e, "sort_order")) {
                    SORT_ORDER_SUPPORTED = false;
                    trySortOrder = false;
                    logger.warn("Colonne sort_order absente – la persistance de l'ordre est désactivée tant que le script update_schema.sql n'est pas exécuté.");
                    continue;
                }
                logger.error("Error fetching mattresses", e);
                return new ArrayList<>();
            }
        }
    }

    private static List<Mattress> fetchMattresses(boolean includeSortOrder) throws SQLException {
        List<Mattress> mattresses = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection()) {
            // Check if columns exist
            boolean hasInitialStock = columnExists(conn, "mattress", "initial_stock");
            boolean hasQuantitySold = columnExists(conn, "mattress", "quantity_sold");
            
            // Build SELECT clause based on available columns
            StringBuilder selectClause = new StringBuilder("SELECT id, type AS mattress_type, size AS mattress_size, reference AS mattress_reference, quantity, ");
            if (hasInitialStock) {
                selectClause.append("initial_stock, ");
            } else {
                selectClause.append("quantity AS initial_stock, ");
            }
            if (hasQuantitySold) {
                selectClause.append("quantity_sold, ");
            } else {
                selectClause.append("0 AS quantity_sold, ");
            }
            selectClause.append("unit_price");
            if (includeSortOrder) {
                selectClause.append(", sort_order");
            }
            selectClause.append(" FROM mattress");
            if (includeSortOrder) {
                selectClause.append(" ORDER BY sort_order ASC, id ASC");
            } else {
                selectClause.append(" ORDER BY id ASC");
            }
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectClause.toString())) {
                while (rs.next()) {
                    mattresses.add(new Mattress(
                        rs.getInt("id"),
                        rs.getString("mattress_type"),
                        rs.getString("mattress_size"),
                        rs.getString("mattress_reference"),
                        rs.getInt("quantity"),
                        rs.getInt("initial_stock"),
                        rs.getInt("quantity_sold"),
                        rs.getDouble("unit_price"),
                        includeSortOrder ? rs.getInt("sort_order") : rs.getInt("id")
                    ));
                }
            }
        }
        return mattresses;
    }
    
    private static boolean columnExists(Connection conn, String table, String column) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getColumns(conn.getCatalog(), null, table, column)) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean addMattress(Mattress mattress) {
        lastError = null;
        try (Connection conn = DBUtil.getConnection()) {
            // Check if columns exist
            boolean hasInitialStock = columnExists(conn, "mattress", "initial_stock");
            boolean hasQuantitySold = columnExists(conn, "mattress", "quantity_sold");
            boolean hasPrix = columnExists(conn, "mattress", "prix");
            boolean hasSortOrder = SORT_ORDER_SUPPORTED && columnExists(conn, "mattress", "sort_order");
            
            // Build SQL based on available columns
            // Note: prix (sale price) is NOT used in inventory, but we include it as NULL if column exists
            //       (prix is only used in transactions, not in inventory - only unit_price is used here)
            StringBuilder sql = new StringBuilder("INSERT INTO mattress (type, size, reference, quantity, ");
            if (hasInitialStock) {
                sql.append("initial_stock, ");
            }
            if (hasQuantitySold) {
                sql.append("quantity_sold, ");
            }
            sql.append("unit_price");
            // Include prix as NULL if column exists (required for old schema compatibility)
            if (hasPrix) {
                sql.append(", prix");
            }
            if (hasSortOrder) {
                sql.append(", sort_order");
            }
            sql.append(") VALUES (?, ?, ?, ?, ");
            if (hasInitialStock) {
                sql.append("?, ");
            }
            if (hasQuantitySold) {
                sql.append("?, ");
            }
            sql.append("?");
            // Include NULL for prix if column exists
            if (hasPrix) {
                sql.append(", NULL");
            }
            if (hasSortOrder) {
                sql.append(", ?");
            }
            sql.append(")");
            
            logger.debug("Executing SQL: {}", sql.toString());
            
            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                int paramIndex = 1;
                stmt.setString(paramIndex++, mattress.getType());
                stmt.setString(paramIndex++, mattress.getSize());
                stmt.setString(paramIndex++, mattress.getReference());
                stmt.setInt(paramIndex++, mattress.getQuantity());
                if (hasInitialStock) {
                    stmt.setInt(paramIndex++, mattress.getInitialStock() > 0 ? mattress.getInitialStock() : mattress.getQuantity());
                }
                if (hasQuantitySold) {
                    stmt.setInt(paramIndex++, mattress.getQuantitySold());
                }
                stmt.setDouble(paramIndex++, mattress.getUnitPrice());
                // Note: prix is NOT set here - it's set to NULL in SQL (prix is only used in transactions)
                if (hasSortOrder) {
                    int sortOrder = 0;
                    try {
                        sortOrder = getNextSortOrder(conn, "mattress");
                    } catch (SQLException e) {
                        // If getNextSortOrder fails (e.g., column doesn't exist), disable sort_order support
                        if (isUnknownColumn(e, "sort_order")) {
                            SORT_ORDER_SUPPORTED = false;
                            logger.warn("sort_order column not available, disabling sort order support");
                        }
                        // Use 0 as default sort order if we can't get the next one
                        sortOrder = 0;
                    }
                    stmt.setInt(paramIndex++, sortOrder);
                }
                
                int rowsAffected = stmt.executeUpdate();
                boolean success = rowsAffected > 0;
                if (success) {
                    lastError = null;
                    logger.info("Mattress added successfully: type={}, size={}, reference={}, quantity={}, initialStock={}, quantitySold={}", 
                        mattress.getType(), mattress.getSize(), mattress.getReference(), mattress.getQuantity(), 
                        mattress.getInitialStock(), mattress.getQuantitySold());
                } else {
                    lastError = "Aucune ligne affectée par l'insertion.";
                    logger.warn("Failed to add mattress: no rows affected");
                }
                return success;
            }
        } catch (SQLException e) {
            String errorMsg = e.getMessage();
            lastError = errorMsg != null ? errorMsg : "Erreur SQL inconnue";
            logger.error("Error adding mattress: SQL State={}, Error Code={}, Message={}", 
                e.getSQLState(), e.getErrorCode(), errorMsg, e);
            
            // Check if it's an unknown column error for sort_order
            if (isUnknownColumn(e, "sort_order")) {
                SORT_ORDER_SUPPORTED = false;
                logger.warn("sort_order column not found, disabling sort order support and retrying");
                // Retry without sort_order
                return addMattressWithoutSortOrder(mattress);
            }
            
            // Check for common database errors and provide helpful messages
            if (errorMsg != null) {
                if (errorMsg.contains("Field 'prix' doesn't have a default value") || 
                    (errorMsg.contains("prix") && errorMsg.contains("doesn't have a default value"))) {
                    lastError = "Erreur: La colonne 'prix' n'a pas de valeur par défaut. " +
                               "Le système va automatiquement rendre cette colonne nullable au prochain démarrage. " +
                               "Redémarrez l'application pour appliquer la correction automatique.";
                    logger.error("Missing 'prix' default value - SchemaMigrator should fix this on next startup");
                    // Try to fix it now
                    try {
                        try (Connection fixConn = DBUtil.getConnection()) {
                            try (Statement fixStmt = fixConn.createStatement()) {
                                fixStmt.executeUpdate("ALTER TABLE `mattress` MODIFY COLUMN `prix` DECIMAL(10,2) NULL");
                                logger.info("Fixed: Made prix column nullable");
                            }
                        }
                    } catch (SQLException fixException) {
                        logger.warn("Could not auto-fix prix column: {}", fixException.getMessage());
                    }
                } else if (errorMsg.contains("Connection") || errorMsg.contains("Communications link failure")) {
                    lastError = "Impossible de se connecter à la base de données. Vérifiez que XAMPP MySQL est démarré.";
                } else if (errorMsg.contains("Unknown database")) {
                    lastError = "Base de données 'warehouse_db' introuvable. Créez-la d'abord.";
                } else if (errorMsg.contains("Table") && errorMsg.contains("doesn't exist")) {
                    lastError = "Table 'mattress' introuvable. Exécutez le script de création de schéma.";
                } else if (errorMsg.contains("Access denied")) {
                    lastError = "Accès refusé à la base de données. Vérifiez les identifiants (utilisateur/mot de passe).";
                } else if (errorMsg.contains("Duplicate entry")) {
                    lastError = "Une entrée avec cette référence existe déjà.";
                }
            }
            
            return false;
        }
    }
    
    /**
     * Helper method to retry adding mattress without sort_order column
     */
    private static boolean addMattressWithoutSortOrder(Mattress mattress) {
        lastError = null;
        try (Connection conn = DBUtil.getConnection()) {
            boolean hasInitialStock = columnExists(conn, "mattress", "initial_stock");
            boolean hasQuantitySold = columnExists(conn, "mattress", "quantity_sold");
            boolean hasPrix = columnExists(conn, "mattress", "prix");
            
            // Note: prix (sale price) is NOT used in inventory, but we include it as NULL if column exists
            StringBuilder sql = new StringBuilder("INSERT INTO mattress (type, size, reference, quantity, ");
            if (hasInitialStock) {
                sql.append("initial_stock, ");
            }
            if (hasQuantitySold) {
                sql.append("quantity_sold, ");
            }
            sql.append("unit_price");
            if (hasPrix) {
                sql.append(", prix");
            }
            sql.append(") VALUES (?, ?, ?, ?, ");
            if (hasInitialStock) {
                sql.append("?, ");
            }
            if (hasQuantitySold) {
                sql.append("?, ");
            }
            sql.append("?");
            if (hasPrix) {
                sql.append(", NULL");
            }
            sql.append(")");
            
            logger.debug("Retrying SQL (without sort_order): {}", sql.toString());
            
            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                int paramIndex = 1;
                stmt.setString(paramIndex++, mattress.getType());
                stmt.setString(paramIndex++, mattress.getSize());
                stmt.setString(paramIndex++, mattress.getReference());
                stmt.setInt(paramIndex++, mattress.getQuantity());
                if (hasInitialStock) {
                    stmt.setInt(paramIndex++, mattress.getInitialStock() > 0 ? mattress.getInitialStock() : mattress.getQuantity());
                }
                if (hasQuantitySold) {
                    stmt.setInt(paramIndex++, mattress.getQuantitySold());
                }
                stmt.setDouble(paramIndex++, mattress.getUnitPrice());
                
                int rowsAffected = stmt.executeUpdate();
                boolean success = rowsAffected > 0;
                if (success) {
                    lastError = null;
                    logger.info("Mattress added successfully (without sort_order): type={}, size={}, reference={}, quantity={}", 
                        mattress.getType(), mattress.getSize(), mattress.getReference(), mattress.getQuantity());
                } else {
                    lastError = "Aucune ligne affectée par l'insertion (retry).";
                }
                return success;
            }
        } catch (SQLException retryException) {
            String errorMsg = retryException.getMessage();
            lastError = errorMsg != null ? errorMsg : "Erreur SQL lors de la nouvelle tentative";
            logger.error("Error adding mattress (retry without sort_order): SQL State={}, Error Code={}, Message={}", 
                retryException.getSQLState(), retryException.getErrorCode(), errorMsg, retryException);
            return false;
        }
    }

    public static boolean updateMattress(Mattress mattress) {
        try (Connection conn = DBUtil.getConnection()) {
            // Check if columns exist
            boolean hasInitialStock = columnExists(conn, "mattress", "initial_stock");
            boolean hasQuantitySold = columnExists(conn, "mattress", "quantity_sold");
            
            // Build SQL based on available columns
            StringBuilder sql = new StringBuilder("UPDATE mattress SET type=?, size=?, reference=?, quantity=?, ");
            if (hasInitialStock) {
                sql.append("initial_stock=?, ");
            }
            if (hasQuantitySold) {
                sql.append("quantity_sold=?, ");
            }
            sql.append("unit_price=? WHERE id=?");
            
            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                int paramIndex = 1;
                stmt.setString(paramIndex++, mattress.getType());
                stmt.setString(paramIndex++, mattress.getSize());
                stmt.setString(paramIndex++, mattress.getReference());
                stmt.setInt(paramIndex++, mattress.getQuantity());
                if (hasInitialStock) {
                    stmt.setInt(paramIndex++, mattress.getInitialStock());
                }
                if (hasQuantitySold) {
                    stmt.setInt(paramIndex++, mattress.getQuantitySold());
                }
                stmt.setDouble(paramIndex++, mattress.getUnitPrice());
                stmt.setInt(paramIndex, mattress.getId());
                
                boolean success = stmt.executeUpdate() > 0;
                if (success) {
                    logger.info("Mattress updated successfully: id={}, type={}, quantity={}, initialStock={}, quantitySold={}", 
                        mattress.getId(), mattress.getType(), mattress.getQuantity(), 
                        mattress.getInitialStock(), mattress.getQuantitySold());
                } else {
                    logger.warn("Failed to update mattress: id={}, no rows affected", mattress.getId());
                }
                return success;
            }
        } catch (SQLException e) {
            logger.error("Error updating mattress: id={}, error={}", mattress.getId(), e.getMessage(), e);
            return false;
        }
    }

    public static boolean deleteMattress(int id) {
        String sql = "DELETE FROM mattress WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                logger.info("Mattress deleted successfully: id={}", id);
            } else {
                logger.warn("Failed to delete mattress: id={}, no rows affected", id);
            }
            return success;
        } catch (SQLException e) {
            logger.error("Error deleting mattress: id={}, error={}", id, e.getMessage(), e);
            return false;
        }
    }

    public static boolean decreaseQuantity(int mattressId, int amount) {
        String sql = "UPDATE mattress SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, amount);
            stmt.setInt(2, mattressId);
            stmt.setInt(3, amount);
            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                logger.debug("Decreased mattress quantity: id={}, amount={}", mattressId, amount);
            }
            return success;
        } catch (SQLException e) {
            logger.error("Error decreasing mattress quantity: id={}, amount={}", mattressId, amount, e);
            return false;
        }
    }

    public static boolean increaseQuantity(int mattressId, int amount) {
        String sql = "UPDATE mattress SET quantity = quantity + ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, amount);
            stmt.setInt(2, mattressId);
            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                logger.debug("Increased mattress quantity: id={}, amount={}", mattressId, amount);
            }
            return success;
        } catch (SQLException e) {
            logger.error("Error increasing mattress quantity: id={}, amount={}", mattressId, amount, e);
            return false;
        }
    }

    public static Mattress getMattressById(int id) {
        String sql = "SELECT * FROM mattress WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    boolean hasInitialStock = columnExists(conn, "mattress", "initial_stock");
                    int initialStock = hasInitialStock ? rs.getInt("initial_stock") : rs.getInt("quantity");
                    boolean hasQuantitySold = columnExists(conn, "mattress", "quantity_sold");
                    int quantitySold = hasQuantitySold ? rs.getInt("quantity_sold") : 0;
                    return new Mattress(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("size"),
                        rs.getString("reference"),
                        rs.getInt("quantity"),
                        initialStock,
                        quantitySold,
                        rs.getDouble("unit_price"),
                        rs.getInt("sort_order")
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching mattress by ID: id={}", id, e);
        }
        return null;
    }

    public static void updateSortOrder(List<Mattress> orderedMattresses) {
        if (!SORT_ORDER_SUPPORTED) {
            return;
        }
        String sql = "UPDATE mattress SET sort_order=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (int index = 0; index < orderedMattresses.size(); index++) {
                Mattress mattress = orderedMattresses.get(index);
                stmt.setInt(1, index + 1);
                stmt.setInt(2, mattress.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
            conn.commit();
            logger.debug("Updated sort order for {} mattresses", orderedMattresses.size());
        } catch (SQLException e) {
            logger.error("Error updating mattress sort order", e);
        }
    }

    private static int getNextSortOrder(Connection conn, String tableName) throws SQLException {
        if (!SORT_ORDER_SUPPORTED) {
            return 1;
        }
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COALESCE(MAX(sort_order), 0) + 1 AS next_order FROM " + tableName);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("next_order");
            }
        }
        return 1;
    }

    private static boolean isUnknownColumn(SQLException e, String columnName) {
        String message = e.getMessage();
        return message != null && message.contains("Unknown column '" + columnName + "'");
    }

    public static boolean isSortOrderSupported() {
        return SORT_ORDER_SUPPORTED;
    }
} 