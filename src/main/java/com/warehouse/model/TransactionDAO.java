package com.warehouse.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    public static List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transaction";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                transactions.add(new Transaction(
                    rs.getInt("id"),
                    rs.getTimestamp("date").toLocalDateTime(),
                    rs.getInt("mattress_id"),
                    rs.getInt("quantity"),
                    rs.getString("type"),
                    rs.getObject("store_owner_id") != null ? rs.getInt("store_owner_id") : null,
                    rs.getInt("user_id"),
                    rs.getDouble("prix"),
                    rs.getString("notes"),
                    rs.getDate("expected_return_date") != null ? rs.getDate("expected_return_date").toLocalDate() : null
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public static boolean addTransaction(Transaction transaction) {
        String sql = "INSERT INTO transaction (date, mattress_id, quantity, type, store_owner_id, user_id, prix, notes, expected_return_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(transaction.getDate()));
            stmt.setInt(2, transaction.getMattressId());
            stmt.setInt(3, transaction.getQuantity());
            stmt.setString(4, transaction.getType());
            if (transaction.getStoreOwnerId() != null) {
                stmt.setInt(5, transaction.getStoreOwnerId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setInt(6, transaction.getUserId());
            stmt.setDouble(7, transaction.getPrix());
            stmt.setString(8, transaction.getNotes());
            if (transaction.getExpectedReturnDate() != null) {
                stmt.setDate(9, java.sql.Date.valueOf(transaction.getExpectedReturnDate()));
            } else {
                stmt.setNull(9, Types.DATE);
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateTransaction(Transaction transaction) {
        String sql = "UPDATE transaction SET date=?, mattress_id=?, quantity=?, type=?, store_owner_id=?, user_id=?, prix=?, notes=?, expected_return_date=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(transaction.getDate()));
            stmt.setInt(2, transaction.getMattressId());
            stmt.setInt(3, transaction.getQuantity());
            stmt.setString(4, transaction.getType());
            if (transaction.getStoreOwnerId() != null) {
                stmt.setInt(5, transaction.getStoreOwnerId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setInt(6, transaction.getUserId());
            stmt.setDouble(7, transaction.getPrix());
            stmt.setString(8, transaction.getNotes());
            if (transaction.getExpectedReturnDate() != null) {
                stmt.setDate(9, java.sql.Date.valueOf(transaction.getExpectedReturnDate()));
            } else {
                stmt.setNull(9, Types.DATE);
            }
            stmt.setInt(10, transaction.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if a mattress has any transactions associated with it
     * @param mattressId The ID of the mattress to check
     * @return true if the mattress has transactions, false otherwise
     */
    public static boolean hasTransactions(int mattressId) {
        String sql = "SELECT COUNT(*) as count FROM transaction WHERE mattress_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mattressId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get the count of transactions for a specific mattress
     * @param mattressId The ID of the mattress
     * @return The number of transactions
     */
    public static int getTransactionCountForMattress(int mattressId) {
        String sql = "SELECT COUNT(*) as count FROM transaction WHERE mattress_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mattressId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Get active loans (Prêt) for a specific mattress
     * @param mattressId The ID of the mattress
     * @return List of active loan transactions
     */
    public static List<Transaction> getActiveLoansForMattress(int mattressId) {
        List<Transaction> activeLoans = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE mattress_id = ? AND type = 'Prêt' ORDER BY date DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mattressId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                activeLoans.add(new Transaction(
                    rs.getInt("id"),
                    rs.getTimestamp("date").toLocalDateTime(),
                    rs.getInt("mattress_id"),
                    rs.getInt("quantity"),
                    rs.getString("type"),
                    rs.getObject("store_owner_id") != null ? rs.getInt("store_owner_id") : null,
                    rs.getInt("user_id"),
                    rs.getDouble("prix"),
                    rs.getString("notes"),
                    rs.getDate("expected_return_date") != null ? rs.getDate("expected_return_date").toLocalDate() : null
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return activeLoans;
    }
    
    /**
     * Get store owners with active loans for a specific mattress
     * @param mattressId The ID of the mattress
     * @return List of store owner IDs with active loans
     */
    public static List<Integer> getStoreOwnersWithActiveLoans(int mattressId) {
        List<Integer> storeOwnerIds = new ArrayList<>();
        String sql = "SELECT DISTINCT store_owner_id FROM transaction WHERE mattress_id = ? AND type = 'Prêt' AND store_owner_id IS NOT NULL";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mattressId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                storeOwnerIds.add(rs.getInt("store_owner_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return storeOwnerIds;
    }
    
    /**
     * Get all active loans (not yet returned)
     * @return List of active loan transactions
     */
    public static List<Transaction> getAllActiveLoans() {
        List<Transaction> activeLoans = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE type = 'Prêt' ORDER BY date DESC";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                activeLoans.add(new Transaction(
                    rs.getInt("id"),
                    rs.getTimestamp("date").toLocalDateTime(),
                    rs.getInt("mattress_id"),
                    rs.getInt("quantity"),
                    rs.getString("type"),
                    rs.getObject("store_owner_id") != null ? rs.getInt("store_owner_id") : null,
                    rs.getInt("user_id"),
                    rs.getDouble("prix"),
                    rs.getString("notes"),
                    rs.getDate("expected_return_date") != null ? rs.getDate("expected_return_date").toLocalDate() : null
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return activeLoans;
    }
    
    /**
     * Get the count of transactions for a specific store owner
     * @param storeOwnerId The ID of the store owner
     * @return The number of transactions
     */
    public static int getTransactionCountForStoreOwner(int storeOwnerId) {
        String sql = "SELECT COUNT(*) as count FROM transaction WHERE store_owner_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, storeOwnerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Get the count of transactions for a specific user
     * @param userId The ID of the user
     * @return The number of transactions
     */
    public static int getTransactionCountForUser(int userId) {
        String sql = "SELECT COUNT(*) as count FROM transaction WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}