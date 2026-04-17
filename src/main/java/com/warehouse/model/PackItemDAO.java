package com.warehouse.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for pack_items table
 */
public class PackItemDAO {
    
    /**
     * Get all pack items for a transaction
     */
    public static List<PackItem> getPackItemsByTransactionId(int transactionId) {
        List<PackItem> items = new ArrayList<>();
        String sql = "SELECT pi.id, pi.transaction_id, pi.mattress_id, pi.quantity, " +
                     "m.size AS mattress_size, m.reference AS mattress_reference, " +
                     "CONCAT(COALESCE(m.size, ''), ' - ', COALESCE(m.reference, 'Réf inconnue')) AS mattress_name " +
                     "FROM pack_items pi " +
                     "LEFT JOIN mattress m ON m.id = pi.mattress_id " +
                     "WHERE pi.transaction_id = ? " +
                     "ORDER BY pi.id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transactionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PackItem item = new PackItem(
                        rs.getInt("id"),
                        rs.getInt("transaction_id"),
                        rs.getInt("mattress_id"),
                        rs.getInt("quantity")
                    );
                    item.setMattressName(rs.getString("mattress_name"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    
    /**
     * Add pack items for a transaction
     * Also updates inventory for each mattress in the pack (fallback if triggers don't work)
     */
    public static boolean addPackItems(int transactionId, List<PackItem> packItems) {
        if (packItems == null || packItems.isEmpty()) {
            return true; // No items to add, consider it successful
        }
        
        String sql = "INSERT INTO pack_items (transaction_id, mattress_id, quantity) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Insert pack items
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    for (PackItem item : packItems) {
                        stmt.setInt(1, transactionId);
                        stmt.setInt(2, item.getMattressId());
                        stmt.setInt(3, item.getQuantity());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
                
                // Verify transaction type is Pack and update inventory manually (fallback)
                // This ensures inventory is updated even if triggers fail
                String checkTypeSql = "SELECT type FROM transaction WHERE id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkTypeSql)) {
                    checkStmt.setInt(1, transactionId);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            String transactionType = rs.getString("type");
                            if ("Pack".equals(transactionType)) {
                                // Update inventory for each mattress in the pack
                                for (PackItem item : packItems) {
                                    // Reduce quantity
                                    String updateQuantitySql = "UPDATE mattress SET quantity = quantity - ? WHERE id = ?";
                                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuantitySql)) {
                                        updateStmt.setInt(1, item.getQuantity());
                                        updateStmt.setInt(2, item.getMattressId());
                                        int updated = updateStmt.executeUpdate();
                                        if (updated == 0) {
                                            System.err.println("Warning: Failed to update quantity for mattress ID: " + item.getMattressId());
                                        }
                                    }
                                    
                                    // Update quantity_sold
                                    String updateSoldSql = "UPDATE mattress SET quantity_sold = quantity_sold + ? WHERE id = ?";
                                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSoldSql)) {
                                        updateStmt.setInt(1, item.getQuantity());
                                        updateStmt.setInt(2, item.getMattressId());
                                        updateStmt.executeUpdate();
                                    }
                                }
                            }
                        }
                    }
                }
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete all pack items for a transaction
     */
    public static boolean deletePackItemsByTransactionId(int transactionId) {
        String sql = "DELETE FROM pack_items WHERE transaction_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transactionId);
            return stmt.executeUpdate() >= 0; // >= 0 because if no items exist, it's still successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update pack items for a transaction (delete old ones and add new ones)
     */
    public static boolean updatePackItems(int transactionId, List<PackItem> packItems) {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Delete existing items
                if (!deletePackItemsByTransactionId(transactionId)) {
                    conn.rollback();
                    return false;
                }
                // Add new items
                if (!addPackItems(transactionId, packItems)) {
                    conn.rollback();
                    return false;
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

