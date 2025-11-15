package com.warehouse.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MattressDAO {
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
                    System.err.println("[MattressDAO] Colonne sort_order absente – la persistance de l'ordre est désactivée tant que le script update_schema.sql n'est pas exécuté.");
                    continue;
                }
                e.printStackTrace();
                return new ArrayList<>();
            }
        }
    }

    private static List<Mattress> fetchMattresses(boolean includeSortOrder) throws SQLException {
        List<Mattress> mattresses = new ArrayList<>();
        String selectClause = includeSortOrder
            ? "SELECT id, type AS mattress_type, size AS mattress_size, reference AS mattress_reference, quantity, unit_price, prix, sort_order FROM mattress ORDER BY sort_order ASC, id ASC"
            : "SELECT id, type AS mattress_type, size AS mattress_size, reference AS mattress_reference, quantity, unit_price, prix FROM mattress ORDER BY id ASC";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectClause)) {
            while (rs.next()) {
                mattresses.add(new Mattress(
                    rs.getInt("id"),
                    rs.getString("mattress_type"),
                    rs.getString("mattress_size"),
                    rs.getString("mattress_reference"),
                    rs.getInt("quantity"),
                    rs.getDouble("unit_price"),
                    rs.getDouble("prix"),
                    includeSortOrder ? rs.getInt("sort_order") : rs.getInt("id")
                ));
            }
        }
        return mattresses;
    }

    public static boolean addMattress(Mattress mattress) {
        String sql = "INSERT INTO mattress (type, size, reference, quantity, unit_price, prix, sort_order) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mattress.getType());
            stmt.setString(2, mattress.getSize());
            stmt.setString(3, mattress.getReference());
            stmt.setInt(4, mattress.getQuantity());
            stmt.setDouble(5, mattress.getUnitPrice());
            stmt.setDouble(6, mattress.getSalePrice());
            stmt.setInt(7, getNextSortOrder(conn, "mattress"));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateMattress(Mattress mattress) {
        String sql = "UPDATE mattress SET type=?, size=?, reference=?, quantity=?, unit_price=?, prix=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mattress.getType());
            stmt.setString(2, mattress.getSize());
            stmt.setString(3, mattress.getReference());
            stmt.setInt(4, mattress.getQuantity());
            stmt.setDouble(5, mattress.getUnitPrice());
            stmt.setDouble(6, mattress.getSalePrice());
            stmt.setInt(7, mattress.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteMattress(int id) {
        String sql = "DELETE FROM mattress WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
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
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean increaseQuantity(int mattressId, int amount) {
        String sql = "UPDATE mattress SET quantity = quantity + ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, amount);
            stmt.setInt(2, mattressId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
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
                    return new Mattress(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("size"),
                        rs.getString("reference"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price"),
                        rs.getDouble("prix"),
                        rs.getInt("sort_order")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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