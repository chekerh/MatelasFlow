package com.warehouse.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MattressDAO {
    public static List<Mattress> getAllMattresses() {
        List<Mattress> mattresses = new ArrayList<>();
        String sql = "SELECT * FROM mattress";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                mattresses.add(new Mattress(
                    rs.getInt("id"),
                    rs.getString("type"),
                    rs.getString("size"),
                    rs.getString("brand"),
                    rs.getInt("quantity"),
                    rs.getDouble("prix")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mattresses;
    }

    public static boolean addMattress(Mattress mattress) {
        String sql = "INSERT INTO mattress (type, size, brand, quantity, prix) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mattress.getType());
            stmt.setString(2, mattress.getSize());
            stmt.setString(3, mattress.getBrand());
            stmt.setInt(4, mattress.getQuantity());
            stmt.setDouble(5, mattress.getPrix());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateMattress(Mattress mattress) {
        String sql = "UPDATE mattress SET type=?, size=?, brand=?, quantity=?, prix=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mattress.getType());
            stmt.setString(2, mattress.getSize());
            stmt.setString(3, mattress.getBrand());
            stmt.setInt(4, mattress.getQuantity());
            stmt.setDouble(5, mattress.getPrix());
            stmt.setInt(6, mattress.getId());
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
        for (Mattress m : getAllMattresses()) {
            if (m.getId() == id) return m;
        }
        return null;
    }
} 