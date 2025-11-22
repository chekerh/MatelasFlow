package com.warehouse.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MattressDAO {
    private static final Logger logger = LoggerFactory.getLogger(MattressDAO.class);
    private static volatile boolean SORT_ORDER_SUPPORTED = true;

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
            // Note: prix (sale price) removed from mattress - price is set per transaction
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
                    // Prix (sale price) is now 0.0 - price is set per transaction, not per mattress
                    mattresses.add(new Mattress(
                        rs.getInt("id"),
                        rs.getString("mattress_type"),
                        rs.getString("mattress_size"),
                        rs.getString("mattress_reference"),
                        rs.getInt("quantity"),
                        rs.getInt("initial_stock"),
                        rs.getInt("quantity_sold"),
                        rs.getDouble("unit_price"),
                        0.0, // salePrice removed - price is set per transaction
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
        try (Connection conn = DBUtil.getConnection()) {
            // Check if columns exist
            boolean hasInitialStock = columnExists(conn, "mattress", "initial_stock");
            boolean hasQuantitySold = columnExists(conn, "mattress", "quantity_sold");
            
            // Build SQL based on available columns
            // Note: prix (sale price) removed - price is set per transaction, not per mattress
            StringBuilder sql = new StringBuilder("INSERT INTO mattress (type, size, reference, quantity, ");
            if (hasInitialStock) {
                sql.append("initial_stock, ");
            }
            if (hasQuantitySold) {
                sql.append("quantity_sold, ");
            }
            sql.append("unit_price, sort_order) VALUES (?, ?, ?, ?, ");
            if (hasInitialStock) {
                sql.append("?, ");
            }
            if (hasQuantitySold) {
                sql.append("?, ");
            }
            sql.append("?, ?)");
            
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
                stmt.setInt(paramIndex, getNextSortOrder(conn, "mattress"));
                
                boolean success = stmt.executeUpdate() > 0;
                if (success) {
                    logger.info("Mattress added successfully: type={}, size={}, reference={}, quantity={}, initialStock={}, quantitySold={}", 
                        mattress.getType(), mattress.getSize(), mattress.getReference(), mattress.getQuantity(), 
                        mattress.getInitialStock(), mattress.getQuantitySold());
                } else {
                    logger.warn("Failed to add mattress: no rows affected");
                }
                return success;
            }
        } catch (SQLException e) {
            logger.error("Error adding mattress: {}", e.getMessage(), e);
            return false;
        }
    }

    public static boolean updateMattress(Mattress mattress) {
        try (Connection conn = DBUtil.getConnection()) {
            // Check if columns exist
            boolean hasInitialStock = columnExists(conn, "mattress", "initial_stock");
            boolean hasQuantitySold = columnExists(conn, "mattress", "quantity_sold");
            
            // Build SQL based on available columns
            // Note: prix (sale price) removed - price is set per transaction, not per mattress
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
                // Note: prix is no longer updated - price is set per transaction
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
                    boolean hasQuantitySold = columnExists(conn, "mattress", "quantity_sold");
                    int initialStock = hasInitialStock ? rs.getInt("initial_stock") : rs.getInt("quantity");
                    int quantitySold = hasQuantitySold ? rs.getInt("quantity_sold") : 0;
                    // Prix (sale price) removed - price is set per transaction, not per mattress
                    return new Mattress(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("size"),
                        rs.getString("reference"),
                        rs.getInt("quantity"),
                        initialStock,
                        quantitySold,
                        rs.getDouble("unit_price"),
                        0.0, // salePrice - price is set per transaction
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