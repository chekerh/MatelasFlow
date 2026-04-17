package com.warehouse.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private static final Logger logger = LoggerFactory.getLogger(TransactionDAO.class);
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
                    logger.warn("Colonne sort_order absente – la persistance de l'ordre est désactivée tant que update_schema.sql n'est pas appliqué.");
                    continue;
                }
                if (tryExpected && isUnknownColumn(e, "expected_return_date")) {
                    EXPECTED_RETURN_SUPPORTED = false;
                    tryExpected = false;
                    logger.warn("Colonne expected_return_date absente – les retours prévus seront ignorés tant que le schéma n'est pas mis à jour.");
                    continue;
                }
                logger.error("Error fetching transactions", e);
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
            "m.type AS mattress_type, m.size AS mattress_size, m.reference AS mattress_reference, " +
            "CONCAT(COALESCE(m.size, ''), ' - ', COALESCE(m.reference, 'Réf inconnue')) AS mattress_name, " +
            "s.name AS store_owner_name " +
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

    public static int addTransactionAndGetId(Transaction transaction) {
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

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    // Get generated ID
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            int transactionId = rs.getInt(1);
                            // NOTE: Inventory update is handled by database trigger 'update_stock_after_transaction'
                            conn.commit();
                            logger.info("Transaction added successfully: id={}, type={}, mattressId={}, quantity={}", 
                                transactionId, transaction.getType(), transaction.getMattressId(), transaction.getQuantity());
                            return transactionId;
                        }
                    }
                }
                conn.rollback();
                logger.warn("Failed to add transaction: no rows affected");
                return -1;
            } catch (SQLException e) {
                conn.rollback();
                if (includeSort && isUnknownColumn(e, "sort_order")) {
                    SORT_ORDER_SUPPORTED = false;
                    System.err.println("[TransactionDAO] Échec lors de l'écriture de sort_order – nouvelle tentative sans la colonne.");
                    return addTransactionAndGetId(transaction);
                }
                if (includeExpected && isUnknownColumn(e, "expected_return_date")) {
                    EXPECTED_RETURN_SUPPORTED = false;
                    System.err.println("[TransactionDAO] Échec lors de l'écriture de expected_return_date – nouvelle tentative sans la colonne.");
                    return addTransactionAndGetId(transaction);
                }
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    public static boolean addTransaction(Transaction transaction) {
        return addTransactionAndGetId(transaction) > 0;
    }
    
    /**
     * Updates inventory based on transaction type
     * Called internally to ensure inventory stays in sync
     * Note: Pack transactions are handled by database triggers on pack_items
     */
    private static void updateInventoryForTransaction(Connection conn, Transaction transaction, boolean isEdit) throws SQLException {
        String type = transaction.getType();
        int mattressId = transaction.getMattressId();
        int quantity = transaction.getQuantity();
        
        if ("Vente".equals(type) || "Prêt".equals(type) || "Transfert".equals(type)) {
            // Decrease stock for sales, loans, transfers
            String sql = "UPDATE mattress SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, quantity);
                stmt.setInt(2, mattressId);
                stmt.setInt(3, quantity);
                int updated = stmt.executeUpdate();
                if (updated == 0) {
                    logger.error("Insufficient stock for mattress ID: {}, requested quantity: {}", mattressId, quantity);
                    throw new SQLException("Stock insuffisant pour le matelas ID: " + mattressId);
                }
                logger.debug("Inventory updated: mattressId={}, quantity change={}, type={}", mattressId, -quantity, type);
            }
            // Update quantity_sold for sales
            if ("Vente".equals(type)) {
                String soldSql = "UPDATE mattress SET quantity_sold = quantity_sold + ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(soldSql)) {
                    stmt.setInt(1, quantity);
                    stmt.setInt(2, mattressId);
                    stmt.executeUpdate();
                    logger.debug("Quantity sold updated: mattressId={}, quantity added={}", mattressId, quantity);
                }
            }
        } else if ("Pack".equals(type)) {
            // Pack transactions are handled by database triggers on pack_items
            // This method is called for consistency but pack inventory is managed by triggers
            logger.debug("Pack transaction inventory handled by database triggers");
        } else if ("retour".equals(type) || "Réception".equals(type)) {
            // Increase stock for returns, receptions
            String sql = "UPDATE mattress SET quantity = quantity + ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, quantity);
                stmt.setInt(2, mattressId);
                stmt.executeUpdate();
            }
            // Update initial_stock for receptions
            if ("Réception".equals(type)) {
                String initialSql = "UPDATE mattress SET initial_stock = initial_stock + ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(initialSql)) {
                    stmt.setInt(1, quantity);
                    stmt.setInt(2, mattressId);
                    stmt.executeUpdate();
                    logger.debug("Initial stock updated: mattressId={}, quantity added={}", mattressId, quantity);
                }
            }
        }
    }

    public static boolean updateTransaction(Transaction transaction) {
        // First, get the old transaction to revert its inventory effect
        Transaction oldTransaction = getTransactionById(transaction.getId());
        if (oldTransaction == null) {
            return false;
        }
        
        boolean includeExpected = EXPECTED_RETURN_SUPPORTED;
        String sql = includeExpected
            ? "UPDATE transaction SET date=?, mattress_id=?, quantity=?, type=?, store_owner_id=?, user_id=?, prix=?, notes=?, expected_return_date=? WHERE id=?"
            : "UPDATE transaction SET date=?, mattress_id=?, quantity=?, type=?, store_owner_id=?, user_id=?, prix=?, notes=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement stmt = conn.prepareStatement(sql);
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
                
                boolean success = stmt.executeUpdate() > 0;
                stmt.close();
                
                if (success) {
                    // Revert old transaction's inventory effect
                    revertInventoryForTransaction(conn, oldTransaction);
                    // Apply new transaction's inventory effect
                    updateInventoryForTransaction(conn, transaction, true);
                    conn.commit();
                    logger.info("Transaction updated successfully: id={}, type={}, mattressId={}, quantity={}", 
                        transaction.getId(), transaction.getType(), transaction.getMattressId(), transaction.getQuantity());
                    return true;
                } else {
                    conn.rollback();
                    logger.warn("Failed to update transaction: id={}, no rows affected", transaction.getId());
                    return false;
                }
            } catch (SQLException e) {
                conn.rollback();
                if (includeExpected && isUnknownColumn(e, "expected_return_date")) {
                    EXPECTED_RETURN_SUPPORTED = false;
                    System.err.println("[TransactionDAO] expected_return_date absent lors de la mise à jour – nouvelle tentative sans la colonne.");
                    return updateTransaction(transaction);
                }
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Reverts inventory changes for a transaction (used when editing/deleting)
     * For Pack transactions, reverts inventory for all pack items
     */
    private static void revertInventoryForTransaction(Connection conn, Transaction transaction) throws SQLException {
        String type = transaction.getType();
        int mattressId = transaction.getMattressId();
        int quantity = transaction.getQuantity();
        
        // Reverse the effect: if it decreased stock, increase it back, and vice versa
        if ("Vente".equals(type) || "Prêt".equals(type) || "Transfert".equals(type)) {
            // Was decreased, so increase back
            String sql = "UPDATE mattress SET quantity = quantity + ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, quantity);
                stmt.setInt(2, mattressId);
                stmt.executeUpdate();
            }
            // Revert quantity_sold for sales
            if ("Vente".equals(type)) {
                String soldSql = "UPDATE mattress SET quantity_sold = quantity_sold - ? WHERE id = ? AND quantity_sold >= ?";
                try (PreparedStatement stmt = conn.prepareStatement(soldSql)) {
                    stmt.setInt(1, quantity);
                    stmt.setInt(2, mattressId);
                    stmt.setInt(3, quantity);
                    int updated = stmt.executeUpdate();
                    if (updated == 0) {
                        logger.warn("Cannot revert quantity_sold for mattress ID: {} - quantity_sold would become negative", mattressId);
                    } else {
                        logger.debug("Quantity sold reverted: mattressId={}, quantity removed={}", mattressId, quantity);
                    }
                }
            }
        } else if ("Pack".equals(type)) {
            // For Pack transactions, revert inventory for each pack item
            String packItemsSql = "SELECT mattress_id, quantity FROM pack_items WHERE transaction_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(packItemsSql)) {
                stmt.setInt(1, transaction.getId());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int packMattressId = rs.getInt("mattress_id");
                        int packQuantity = rs.getInt("quantity");
                        
                        // Revert quantity (increase back)
                        String revertSql = "UPDATE mattress SET quantity = quantity + ? WHERE id = ?";
                        try (PreparedStatement revertStmt = conn.prepareStatement(revertSql)) {
                            revertStmt.setInt(1, packQuantity);
                            revertStmt.setInt(2, packMattressId);
                            revertStmt.executeUpdate();
                        }
                        
                        // Revert quantity_sold (decrease)
                        String soldSql = "UPDATE mattress SET quantity_sold = quantity_sold - ? WHERE id = ? AND quantity_sold >= ?";
                        try (PreparedStatement soldStmt = conn.prepareStatement(soldSql)) {
                            soldStmt.setInt(1, packQuantity);
                            soldStmt.setInt(2, packMattressId);
                            soldStmt.setInt(3, packQuantity);
                            int updated = soldStmt.executeUpdate();
                            if (updated == 0) {
                                logger.warn("Cannot revert quantity_sold for pack mattress ID: {} - quantity_sold would become negative", packMattressId);
                            } else {
                                logger.debug("Pack quantity sold reverted: mattressId={}, quantity removed={}", packMattressId, packQuantity);
                            }
                        }
                    }
                }
            }
            logger.debug("Pack transaction inventory reverted for transaction ID: {}", transaction.getId());
        } else if ("retour".equals(type) || "Réception".equals(type)) {
            // Was increased, so decrease back (with safety check)
            String sql = "UPDATE mattress SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, quantity);
                stmt.setInt(2, mattressId);
                stmt.setInt(3, quantity);
                int updated = stmt.executeUpdate();
                if (updated == 0) {
                    logger.warn("Cannot revert inventory for mattress ID: {} - insufficient stock to revert quantity: {}", mattressId, quantity);
                    // Note: We still proceed, but log the warning. The stock might have been partially used.
                }
            }
            // Revert initial_stock for receptions
            if ("Réception".equals(type)) {
                String initialSql = "UPDATE mattress SET initial_stock = initial_stock - ? WHERE id = ? AND initial_stock >= ?";
                try (PreparedStatement stmt = conn.prepareStatement(initialSql)) {
                    stmt.setInt(1, quantity);
                    stmt.setInt(2, mattressId);
                    stmt.setInt(3, quantity);
                    int updated = stmt.executeUpdate();
                    if (updated == 0) {
                        logger.warn("Cannot revert initial_stock for mattress ID: {} - initial_stock would become negative", mattressId);
                    } else {
                        logger.debug("Initial stock reverted: mattressId={}, quantity removed={}", mattressId, quantity);
                    }
                }
            }
        }
    }
    
    /**
     * Gets a transaction by ID (for edit operations)
     */
    private static Transaction getTransactionById(int id) {
        String sql = "SELECT t.id, t.date, t.mattress_id, t.quantity, t.type, t.store_owner_id, " +
            "t.user_id, t.prix, t.notes, t.expected_return_date, t.sort_order, " +
            "CONCAT(COALESCE(m.size, ''), ' - ', COALESCE(m.reference, 'Réf inconnue')) AS mattress_name, " +
            "s.name AS store_owner_name " +
            "FROM transaction t " +
            "LEFT JOIN mattress m ON m.id = t.mattress_id " +
            "LEFT JOIN store_owner s ON s.id = t.store_owner_id " +
            "WHERE t.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Transaction(
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
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean deleteTransaction(int id) {
        // First, get the transaction to revert its inventory effect
        Transaction transaction = getTransactionById(id);
        if (transaction == null) {
            return false;
        }
        
        String sql = "DELETE FROM transaction WHERE id=?";
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, id);
                boolean success = stmt.executeUpdate() > 0;
                stmt.close();
                
                if (success) {
                    // Revert inventory changes
                    revertInventoryForTransaction(conn, transaction);
                    conn.commit();
                    logger.info("Transaction deleted successfully: id={}, type={}, mattressId={}, quantity={}", 
                        id, transaction.getType(), transaction.getMattressId(), transaction.getQuantity());
                    return true;
                } else {
                    conn.rollback();
                    logger.warn("Failed to delete transaction: id={}, no rows affected", id);
                    return false;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
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