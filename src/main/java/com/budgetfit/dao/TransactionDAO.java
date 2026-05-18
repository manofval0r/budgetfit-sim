package com.budgetfit.dao;

import com.budgetfit.database.DatabaseHelper;
import com.budgetfit.model.TransactionEntry;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Data Access Object (DAO) managing transactions persistence.
 * Guarantees data isolation by explicitly scoping updates and deletions by user_id.
 * Supports targeted month/period retrieval and banking ledger queries.
 */
public class TransactionDAO {

    /**
     * Retrieves all distinct year-month strings present for a user's transactions.
     */
    public static List<String> getAvailableMonths(int userId) {
        List<String> months = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT DISTINCT strftime('%Y-%m', date) AS month_str FROM transactions WHERE user_id = ? ORDER BY month_str DESC")) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String m = rs.getString("month_str");
                    if (m != null && !m.isEmpty()) {
                        months.add(m);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String currentMonth = new SimpleDateFormat("yyyy-MM").format(new Date());
        if (!months.contains(currentMonth)) {
            months.add(0, currentMonth);
        }
        return months;
    }

    /**
     * Loads user transactions filtered by a specific year-month string.
     */
    public static List<TransactionEntry> loadTransactions(int userId, String monthStr) {
        List<TransactionEntry> list = new ArrayList<>();
        boolean filterByMonth = monthStr != null && !monthStr.equalsIgnoreCase("All Time") && !monthStr.isEmpty();

        String sql = "SELECT id, category, item_name, budgeted_amount, actual_amount, paid, recurring, sender_receiver, tx_type, date FROM transactions WHERE user_id = ?";
        if (filterByMonth) {
            sql += " AND strftime('%Y-%m', date) = ?";
        }
        sql += " ORDER BY id ASC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            if (filterByMonth) {
                stmt.setString(2, monthStr);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String type = rs.getString("category");
                    String itemName = rs.getString("item_name");
                    double budget = rs.getDouble("budgeted_amount");
                    double actual = rs.getDouble("actual_amount");
                    boolean paid = rs.getInt("paid") == 1;
                    boolean recurring = rs.getInt("recurring") == 1;
                    String senderReceiver = rs.getString("sender_receiver");
                    String txType = rs.getString("tx_type");
                    String dateStr = rs.getString("date");

                    if (type != null && type.contains(":")) {
                        int idx = type.indexOf(':');
                        itemName = type.substring(idx + 1).trim();
                        type = type.substring(0, idx).trim();
                    }

                    list.add(new TransactionEntry(id, type, itemName, budget, actual, paid, recurring, senderReceiver, txType, dateStr, false));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Loads the complete banking ledger ordered by date descending.
     */
    public static List<TransactionEntry> loadBankingLedger(int userId) {
        List<TransactionEntry> list = new ArrayList<>();
        String sql = "SELECT id, category, item_name, budgeted_amount, actual_amount, paid, recurring, sender_receiver, tx_type, date FROM transactions WHERE user_id = ? ORDER BY date DESC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new TransactionEntry(
                            rs.getInt("id"),
                            rs.getString("category"),
                            rs.getString("item_name"),
                            rs.getDouble("budgeted_amount"),
                            rs.getDouble("actual_amount"),
                            rs.getInt("paid") == 1,
                            rs.getInt("recurring") == 1,
                            rs.getString("sender_receiver"),
                            rs.getString("tx_type"),
                            rs.getString("date"),
                            false
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Inserts a banking transaction (ATM Deposit, Withdrawal, Transfer).
     */
    public static boolean insertBankingTransaction(int userId, String category, String itemName, double amount, String senderReceiver, String txType) {
        String sql = "INSERT INTO transactions (user_id, category, item_name, budgeted_amount, actual_amount, paid, recurring, sender_receiver, tx_type) VALUES (?, ?, ?, ?, ?, 1, 0, ?, ?)";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, category);
            stmt.setString(3, itemName);
            stmt.setDouble(4, amount); // Store in budgeted
            stmt.setDouble(5, amount); // Store in actual
            stmt.setString(6, senderReceiver == null ? "" : senderReceiver);
            stmt.setString(7, txType == null ? "BUDGET" : txType);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Inserts a new transaction entry into the database for a target period.
     */
    public static void insertTransaction(int userId, TransactionEntry entry, String targetMonthStr) {
        boolean customDate = targetMonthStr != null && !targetMonthStr.equalsIgnoreCase("All Time") && !targetMonthStr.isEmpty();
        String sql = "INSERT INTO transactions (user_id, category, item_name, budgeted_amount, actual_amount, paid, recurring, sender_receiver, tx_type";
        if (customDate) { sql += ", date"; }
        sql += ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?";
        if (customDate) { sql += ", ?"; }
        sql += ")";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, userId);
            stmt.setString(2, entry.getType());
            stmt.setString(3, entry.getCategory().trim());
            stmt.setDouble(4, entry.getBudgetedAmount());
            stmt.setDouble(5, entry.getActualAmount());
            stmt.setInt(6, entry.isPaid() ? 1 : 0);
            stmt.setInt(7, entry.isRecurring() ? 1 : 0);
            stmt.setString(8, entry.getSenderReceiver());
            stmt.setString(9, entry.getTxType());

            if (customDate) {
                stmt.setString(10, targetMonthStr + "-01 12:00:00");
            }

            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    entry.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates an existing transaction entry, guarded strictly by user_id.
     */
    public static void updateTransaction(int userId, TransactionEntry entry) {
        if (entry.getId() <= 0) return;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE transactions SET category = ?, item_name = ?, budgeted_amount = ?, actual_amount = ?, paid = ?, recurring = ?, sender_receiver = ?, tx_type = ? WHERE id = ? AND user_id = ?")) {

            stmt.setString(1, entry.getType());
            stmt.setString(2, entry.getCategory().trim());
            stmt.setDouble(3, entry.getBudgetedAmount());
            stmt.setDouble(4, entry.getActualAmount());
            stmt.setInt(5, entry.isPaid() ? 1 : 0);
            stmt.setInt(6, entry.isRecurring() ? 1 : 0);
            stmt.setString(7, entry.getSenderReceiver());
            stmt.setString(8, entry.getTxType());
            stmt.setInt(9, entry.getId());
            stmt.setInt(10, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a transaction entry, guarded strictly by user_id.
     */
    public static void deleteTransaction(int userId, int transactionId) {
        if (transactionId <= 0) return;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM transactions WHERE id = ? AND user_id = ?")) {

            stmt.setInt(1, transactionId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clones all transactions marked as 'recurring' from any previous record into the specified target month.
     */
    public static void copyRecurringTransactions(int userId, String targetMonthStr) {
        if (targetMonthStr == null || targetMonthStr.equalsIgnoreCase("All Time")) return;

        String selectSql = "SELECT category, item_name, budgeted_amount, actual_amount, paid, sender_receiver, tx_type FROM transactions " +
                           "WHERE user_id = ? AND recurring = 1 GROUP BY category, item_name";
        
        String checkSql = "SELECT COUNT(*) FROM transactions WHERE user_id = ? AND category = ? AND item_name = ? AND strftime('%Y-%m', date) = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            selectStmt.setInt(1, userId);
            try (ResultSet rs = selectStmt.executeQuery()) {
                while (rs.next()) {
                    String cat = rs.getString("category");
                    String item = rs.getString("item_name");
                    
                    checkStmt.setInt(1, userId);
                    checkStmt.setString(2, cat);
                    checkStmt.setString(3, item);
                    checkStmt.setString(4, targetMonthStr);
                    
                    try (ResultSet checkRs = checkStmt.executeQuery()) {
                        if (checkRs.next() && checkRs.getInt(1) == 0) {
                            TransactionEntry entry = new TransactionEntry(-1, cat, item, rs.getDouble("budgeted_amount"), 0.0, false, true, rs.getString("sender_receiver"), rs.getString("tx_type"), "", false);
                            insertTransaction(userId, entry, targetMonthStr);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Double[]> getMonthlySummaries(int userId) {
        Map<String, Double[]> map = new java.util.TreeMap<>();
        String sql = "SELECT strftime('%Y-%m', date) as month, " +
                     "SUM(CASE WHEN category = 'Income' THEN actual_amount ELSE 0 END) as income, " +
                     "SUM(CASE WHEN category != 'Income' AND category != 'Savings' THEN actual_amount ELSE 0 END) as expenses " +
                     "FROM transactions WHERE user_id = ? " +
                     "GROUP BY month ORDER BY month DESC LIMIT 6";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("month"), new Double[]{rs.getDouble("income"), rs.getDouble("expenses")});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static Map<String, Double> getCategoryBreakdown(int userId) {
        Map<String, Double> map = new java.util.HashMap<>();
        String sql = "SELECT item_name, SUM(actual_amount) as total FROM transactions " +
                     "WHERE user_id = ? AND category != 'Income' AND actual_amount > 0 " +
                     "GROUP BY item_name ORDER BY total DESC LIMIT 10";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("item_name"), rs.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
}
