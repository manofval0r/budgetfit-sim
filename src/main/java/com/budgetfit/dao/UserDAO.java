package com.budgetfit.dao;

import com.budgetfit.database.DatabaseHelper;
import com.budgetfit.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) providing clean abstractions for database interactions
 * concerning user accounts, authentication, PIN verification, and user metadata.
 */
public class UserDAO {

    /**
     * Authenticates a user by username and password against securely stored BCrypt hashes.
     * @param username the username to verify
     * @param password the plaintext password to verify
     * @return a populated User object if authentication is successful, null otherwise
     */
    public static User authenticate(String username, String password) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, username, password_hash, pin_hash, role, monthly_income, last_active FROM users WHERE username = ?")) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    boolean matched = false;
                    if (storedHash != null && storedHash.startsWith("$2a$")) {
                        matched = BCrypt.checkpw(password, storedHash);
                    } else if (storedHash != null) {
                        matched = storedHash.equals(password);
                    }

                    if (matched) {
                        int id = rs.getInt("id");
                        String pinHash = rs.getString("pin_hash");
                        String role = rs.getString("role");
                        double income = rs.getDouble("monthly_income");
                        String lastActive = rs.getString("last_active");
                        return new User(id, username, role, lastActive, income > 0 ? income : 5000.0, pinHash);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Verifies a 4-digit PIN for Step-Up ATM authorization.
     */
    public static boolean verifyPin(int userId, String pin) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT pin_hash FROM users WHERE id = ?")) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("pin_hash");
                    if (storedHash != null && storedHash.startsWith("$2a$")) {
                        return BCrypt.checkpw(pin, storedHash);
                    } else if (storedHash != null) {
                        return storedHash.equals(pin);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if a specific username is already registered.
     */
    public static boolean usernameExists(String username) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Registers a new user account with securely hashed password and PIN.
     */
    public static boolean registerUser(String username, String password, String pin, String role) {
        String hashedPw = BCrypt.hashpw(password, BCrypt.gensalt());
        String hashedPin = BCrypt.hashpw(pin, BCrypt.gensalt());

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO users (username, password_hash, pin_hash, role) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPw);
            stmt.setString(3, hashedPin);
            stmt.setString(4, role == null ? "user" : role);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Records a login event, updating the user's last_active timestamp and writing an audit log.
     */
    public static void recordLogin(int userId) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(
                     "UPDATE users SET last_active = CURRENT_TIMESTAMP WHERE id = ?");
             PreparedStatement logStmt = conn.prepareStatement(
                     "INSERT INTO system_logs (user_id, action) VALUES (?, ?)")) {

            updateStmt.setInt(1, userId);
            updateStmt.executeUpdate();

            logStmt.setInt(1, userId);
            logStmt.setString(2, "login");
            logStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the persisted monthly income base for a user.
     */
    public static double getMonthlyIncome(int userId) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT monthly_income FROM users WHERE id = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double val = rs.getDouble("monthly_income");
                    return val > 0 ? val : 5000.0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 5000.0;
    }

    /**
     * Updates the persisted monthly income base for a user.
     */
    public static void updateMonthlyIncome(int userId, double income) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE users SET monthly_income = ? WHERE id = ?")) {
            stmt.setDouble(1, income);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all registered user accounts for administrative management.
     */
    public static List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, username, pin_hash, role, last_active, monthly_income FROM users ORDER BY id");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("last_active"),
                        rs.getDouble("monthly_income"),
                        rs.getString("pin_hash")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Deletes a user account and all references to prevent dangling constraints.
     */
    public static boolean deleteUser(int userId) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement delLogs = conn.prepareStatement("DELETE FROM system_logs WHERE user_id = ?");
                 PreparedStatement delTrans = conn.prepareStatement("DELETE FROM transactions WHERE user_id = ?");
                 PreparedStatement delInv = conn.prepareStatement("DELETE FROM investments WHERE user_id = ?");
                 PreparedStatement delGoals = conn.prepareStatement("DELETE FROM goals WHERE user_id = ?");
                 PreparedStatement delUser = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {

                delLogs.setInt(1, userId); delLogs.executeUpdate();
                delTrans.setInt(1, userId); delTrans.executeUpdate();
                delInv.setInt(1, userId); delInv.executeUpdate();
                delGoals.setInt(1, userId); delGoals.executeUpdate();

                delUser.setInt(1, userId);
                int rows = delUser.executeUpdate();

                conn.commit();
                return rows > 0;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getTotalUsersCount() {
        return queryCount("SELECT COUNT(*) FROM users");
    }

    public static int getActiveUsersCount() {
        return queryCount("SELECT COUNT(*) FROM users WHERE date(last_active) = date('now')");
    }

    public static int getChurnUsersCount() {
        return queryCount("SELECT COUNT(*) FROM users WHERE date(last_active) <= date('now', '-7 day')");
    }

    private static int queryCount(String sql) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
