package com.warehouse.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    public static User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getString("role")
                );
            }
        } catch (SQLException e) {
            logger.error("Error finding user by username: {}", username, e);
        }
        return null;
    }

    public static java.util.List<User> getAllUsers() {
        java.util.List<User> users = new java.util.ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all users", e);
        }
        return users;
    }

    public static boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole());
            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                logger.info("User added successfully: username={}, role={}", user.getUsername(), user.getRole());
            }
            return success;
        } catch (SQLException e) {
            logger.error("Error adding user: username={}", user.getUsername(), e);
            return false;
        }
    }

    public static boolean updateUser(User user) {
        String sql = "UPDATE users SET username=?, password_hash=?, role=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole());
            stmt.setInt(4, user.getId());
            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                logger.info("User updated successfully: id={}, username={}", user.getId(), user.getUsername());
            }
            return success;
        } catch (SQLException e) {
            logger.error("Error updating user: id={}, username={}", user.getId(), user.getUsername(), e);
            return false;
        }
    }

    public static boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                logger.info("User deleted successfully: id={}", id);
            }
            return success;
        } catch (SQLException e) {
            logger.error("Error deleting user: id={}", id, e);
            return false;
        }
    }
} 