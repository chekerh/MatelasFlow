package com.warehouse.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private static volatile boolean SORT_ORDER_SUPPORTED = true;
    private static volatile boolean EXPECTED_RETURN_SUPPORTED = true;

    public static List<Transaction> getAllTransactions() {
        boolean trySort = SORT_ORDER_SUPPORTED;
        boolean tryExpected = EXPECTED_RETURN_SUPPORTED;

        while (true) {
            try {
                return fetchTransactions(trySort, tryExpected);
            } catch (SQLException e) {
                if (trySort && isUnknownColumn(e, "sort_order")) {
                    SORT_ORDER_SUPPORTED = false;
                    trySort = false;
                    System.err.println("[TransactionDAO] Colonne sort_order absente – la persistance de l'ordre est désactivée tant que update_schema.sql n'est pas appliqué.");
                    continue;
                }
                if (tryExpected && isUnknownColumn(e, "expected_return_date")) {
                    EXPECTED_RETURN_SUPPORTED = false;
                    tryExpected = false;
                    System.err.println("[TransactionDAO] Colonne expected_return_date absente – les retours prévus seront ignorés tant que le schéma n'est pas mis à jour.");
                    continue;
                }
                e.printStackTrace();
                return new ArrayList<>();
            }
        }
    }

    private static List<Transaction> fetchTransactions(boolean includeSortOrder, boolean includeExpectedReturn) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String expectedColumn = includeExpectedReturn
            ? "t.expected_return_date AS expected_return_date"
            : "NULL AS expected_return_date";
        String sortColumn = includeSortOrder
            ? "t.sort_order AS sort_order"
            : "t.id AS sort_order";
        String orderClause = includeSortOrder ? "ORDER BY t.sort_order ASC, t.date DESC" : "ORDER BY t.date DESC";
        String sql = "SELECT t.id, t.date, t.mattress_id, t.quantity, t.type, t.store_owner_id, " +
            "t.user_id, t.prix, t.notes, " + expectedColumn + ", " + sortColumn + ", " +
            "m.type AS mattress_name, s.name AS store_owner_name " +
            "FROM transaction t " +
            "LEFT JOIN mattress m ON m.id = t.mattress_id " +
            "LEFT JOIN store_owner s ON s.id = t.store_owner_id " +
            orderClause;
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
                    rs.getDate("expected_return_date") != null ? rs.getDate("expected_return_date").toLocalDate() : null,
                    rs.getInt("sort_order"),
                    rs.getString("mattress_name"),
                    rs.getString("store_owner_name")
                ));
            }
        }
        return transactions;
    }

    public static boolean addTransaction(Transaction transaction) {
        boolean includeSort = SORT_ORDER_SUPPORTED;
        boolean includeExpected = EXPECTED_RETURN_SUPPORTED;

        String sql;
        if (includeExpected && includeSort) {
            sql = "INSERT INTO transaction (date, mattress_id, quantity, type, store_owner_id, user_id, prix, notes, expected_return_date, sort_order) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else if (includeExpected) {
            sql = "INSERT INTO transaction (date, mattress_id, quantity, type, store_owner_id, user_id, prix, notes, expected_return_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else if (includeSort) {
            sql = "INSERT INTO transaction (date, mattress_id, quantity, type, store_owner_id, user_id, prix, notes, sort_order) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO transaction (date, mattress_id, quantity, type, store_owner_id, user_id, prix, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        }

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

            int paramIndex = 9;
            if (includeExpected) {
                if (transaction.getExpectedReturnDate() != null) {
                    stmt.setDate(paramIndex, java.sql.Date.valueOf(transaction.getExpectedReturnDate()));
                } else {
                    stmt.setNull(paramIndex, Types.DATE);
                }
                paramIndex++;
            }
            if (includeSort) {
                stmt.setInt(paramIndex, getNextSortOrder(conn, "transaction"));
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (includeSort && isUnknownColumn(e, "sort_order")) {
                SORT_ORDER_SUPPORTED = false;
                System.err.println("[TransactionDAO] Échec lors de l'écriture de sort_order – nouvelle tentative sans la colonne.");
                return addTransaction(transaction);
            }
            if (includeExpected && isUnknownColumn(e, "expected_return_date")) {
                EXPECTED_RETURN_SUPPORTED = false;
                System.err.println("[TransactionDAO] Échec lors de l'écriture de expected_return_date – nouvelle tentative sans la colonne.");
                return addTransaction(transaction);
            }
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateTransaction(Transaction transaction) {
        boolean includeExpected = EXPECTED_RETURN_SUPPORTED;
        String sql = includeExpected
            ? "UPDATE transaction SET date=?, mattress_id=?, quantity=?, type=?, store_owner_id=?, user_id=?, prix=?, notes=?, expected_return_date=? WHERE id=?"
            : "UPDATE transaction SET date=?, mattress_id=?, quantity=?, type=?, store_owner_id=?, user_id=?, prix=?, notes=? WHERE id=?";
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
            if (includeExpected) {
                if (transaction.getExpectedReturnDate() != null) {
                    stmt.setDate(9, java.sql.Date.valueOf(transaction.getExpectedReturnDate()));
                } else {
                    stmt.setNull(9, Types.DATE);
                }
                stmt.setInt(10, transaction.getId());
            } else {
                stmt.setInt(9, transaction.getId());
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (includeExpected && isUnknownColumn(e, "expected_return_date")) {
                EXPECTED_RETURN_SUPPORTED = false;
                System.err.println("[TransactionDAO] expected_return_date absent lors de la mise à jour – nouvelle tentative sans la colonne.");
                return updateTransaction(transaction);
            }
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteTransaction(int id) {
        String sql = "DELETE FROM transaction WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void updateSortOrder(List<Transaction> transactions) {
        if (!SORT_ORDER_SUPPORTED) {
            return;
        }
        String sql = "UPDATE transaction SET sort_order=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (int i = 0; i < transactions.size(); i++) {
                stmt.setInt(1, i + 1);
                stmt.setInt(2, transactions.get(i).getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
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