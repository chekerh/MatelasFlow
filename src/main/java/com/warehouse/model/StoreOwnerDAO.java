package com.warehouse.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StoreOwnerDAO {
    private static final Logger logger = LoggerFactory.getLogger(StoreOwnerDAO.class);
    public static List<StoreOwner> getAllStoreOwners() {
        List<StoreOwner> owners = new ArrayList<>();
        String sql = "SELECT * FROM store_owner";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                owners.add(new StoreOwner(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("contact")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all store owners", e);
        }
        return owners;
    }

    public static boolean addStoreOwner(StoreOwner owner) {
        String sql = "INSERT INTO store_owner (name, contact) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, owner.getName());
            stmt.setString(2, owner.getContact());
            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                logger.info("Store owner added successfully: name={}", owner.getName());
            }
            return success;
        } catch (SQLException e) {
            logger.error("Error adding store owner: name={}", owner.getName(), e);
            return false;
        }
    }

    public static boolean updateStoreOwner(StoreOwner owner) {
        String sql = "UPDATE store_owner SET name=?, contact=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, owner.getName());
            stmt.setString(2, owner.getContact());
            stmt.setInt(3, owner.getId());
            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                logger.info("Store owner updated successfully: id={}, name={}", owner.getId(), owner.getName());
            }
            return success;
        } catch (SQLException e) {
            logger.error("Error updating store owner: id={}, name={}", owner.getId(), owner.getName(), e);
            return false;
        }
    }

    public static boolean deleteStoreOwner(int id) {
        String sql = "DELETE FROM store_owner WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                logger.info("Store owner deleted successfully: id={}", id);
            }
            return success;
        } catch (SQLException e) {
            logger.error("Error deleting store owner: id={}", id, e);
            return false;
        }
    }

    public static StoreOwner getStoreOwnerById(Integer id) {
        if (id == null) {
            return null;
        }
        String sql = "SELECT * FROM store_owner WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    logger.debug("Store owner found by ID: id={}, name={}", id, rs.getString("name"));
                    return new StoreOwner(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("contact")
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching store owner by ID: id={}", id, e);
        }
        return null;
    }
} 