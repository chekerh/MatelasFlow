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
            int rowsAffected = stmt.executeUpdate();
            System.out.println("DEBUG DAO: Transaction ajoutée, lignes affectées: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("ERREUR DAO addTransaction: " + e.getMessage());
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
} 