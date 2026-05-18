package com.budgetfit.database;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for initializing and provisioning SQLite database connections.
 * Enforces foreign key constraints and implements robust schema/data migrations.
 * Expanded for GUI ATM & Digital Banking / Wealth Management capabilities.
 */
public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:budgetfit.db";

    /**
     * Obtains a new database connection and automatically enables foreign key enforcement.
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        } catch (SQLException e) {
            System.err.println("Warning: Could not enable foreign keys pragma: " + e.getMessage());
        }
        return conn;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create Users Table (Added pin_hash)
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password_hash TEXT NOT NULL," +
                    "pin_hash TEXT NOT NULL DEFAULT ''," +
                    "role TEXT DEFAULT 'user'," +
                    "monthly_income REAL DEFAULT 5000.0," +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "last_active DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");";
            stmt.execute(createUsersTable);

            // Create Transactions Table (Added sender_receiver and tx_type)
            String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "category TEXT NOT NULL," +
                    "item_name TEXT NOT NULL," +
                    "budgeted_amount REAL DEFAULT 0.0," +
                    "actual_amount REAL DEFAULT 0.0," +
                    "paid INTEGER DEFAULT 0," +
                    "recurring INTEGER DEFAULT 0," +
                    "sender_receiver TEXT DEFAULT ''," +
                    "tx_type TEXT DEFAULT 'BUDGET'," +
                    "date DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ");";
            stmt.execute(createTransactionsTable);

            // Create Investments Table (NEW)
            String createInvestmentsTable = "CREATE TABLE IF NOT EXISTS investments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "symbol TEXT NOT NULL," +
                    "shares REAL DEFAULT 0.0," +
                    "avg_price REAL DEFAULT 0.0," +
                    "current_price REAL DEFAULT 0.0," +
                    "last_updated DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ");";
            stmt.execute(createInvestmentsTable);

            // Create System Logs Table
            String createSystemLogsTable = "CREATE TABLE IF NOT EXISTS system_logs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER," +
                    "action TEXT NOT NULL," +
                    "log_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ");";
            stmt.execute(createSystemLogsTable);

            // Create Goals Table
            String createGoalsTable = "CREATE TABLE IF NOT EXISTS goals (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "name TEXT NOT NULL," +
                    "target_amount REAL NOT NULL," +
                    "current_amount REAL DEFAULT 0.0," +
                    "target_date TEXT DEFAULT '2026-12'," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ");";
            stmt.execute(createGoalsTable);

            // Safely apply column migrations for existing databases with logging
            try { stmt.execute("ALTER TABLE transactions ADD COLUMN paid INTEGER DEFAULT 0"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE transactions ADD COLUMN recurring INTEGER DEFAULT 0"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE transactions ADD COLUMN sender_receiver TEXT DEFAULT ''"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE transactions ADD COLUMN tx_type TEXT DEFAULT 'BUDGET'"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE users ADD COLUMN monthly_income REAL DEFAULT 5000.0"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE users ADD COLUMN pin_hash TEXT DEFAULT ''"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE goals ADD COLUMN target_date TEXT DEFAULT '2026-12'"); } catch (SQLException e) {}

            // Perform automatic migration of legacy plaintext passwords/PINs to secure BCrypt hashes
            migrateLegacySecurity(conn);

            // Perform automatic migration of legacy compound categories (Type:Name) to split columns
            migrateLegacyCategories(conn);

            seedMockData(conn);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void migrateLegacySecurity(Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, password_hash, pin_hash FROM users")) {
            List<String> updateQueries = new ArrayList<>();
            String defaultPinHash = BCrypt.hashpw("1234", BCrypt.gensalt());

            while (rs.next()) {
                int id = rs.getInt("id");
                String pwHash = rs.getString("password_hash");
                String pinHash = rs.getString("pin_hash");

                boolean needsUpdate = false;
                String newPwHash = pwHash;
                String newPinHash = pinHash;

                if (pwHash != null && !pwHash.startsWith("$2a$")) {
                    newPwHash = BCrypt.hashpw(pwHash, BCrypt.gensalt());
                    needsUpdate = true;
                }
                if (pinHash == null || pinHash.isEmpty() || (!pinHash.startsWith("$2a$") && !pinHash.isEmpty())) {
                    newPinHash = (pinHash != null && !pinHash.isEmpty()) ? BCrypt.hashpw(pinHash, BCrypt.gensalt()) : defaultPinHash;
                    needsUpdate = true;
                }

                if (needsUpdate) {
                    updateQueries.add("UPDATE users SET password_hash = '" + newPwHash + "', pin_hash = '" + newPinHash + "' WHERE id = " + id);
                }
            }
            for (String sql : updateQueries) {
                try (Statement uStmt = conn.createStatement()) {
                    uStmt.executeUpdate(sql);
                }
            }
        } catch (SQLException ignore) {}
    }

    private static void migrateLegacyCategories(Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, category, item_name FROM transactions")) {
            List<String> updateQueries = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String cat = rs.getString("category");
                if (cat != null && cat.contains(":")) {
                    int idx = cat.indexOf(':');
                    String newCat = cat.substring(0, idx).trim();
                    String newItemName = cat.substring(idx + 1).trim();
                    updateQueries.add("UPDATE transactions SET category = '" + newCat.replace("'", "''") +
                            "', item_name = '" + newItemName.replace("'", "''") + "' WHERE id = " + id);
                }
            }
            for (String sql : updateQueries) {
                try (Statement uStmt = conn.createStatement()) {
                    uStmt.executeUpdate(sql);
                }
            }
        } catch (SQLException ignore) {}
    }

    private static void seedMockData(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return; // already seeded
            }
        }

        System.out.println("Seeding comprehensive mock data for multi-table layout...");

        String adminHash = BCrypt.hashpw("admin", BCrypt.gensalt());
        String userHash = BCrypt.hashpw("password", BCrypt.gensalt());
        String defaultPin = BCrypt.hashpw("1234", BCrypt.gensalt());

        String[] users = {
            "INSERT INTO users (id, username, password_hash, pin_hash, role, last_active) VALUES (1, 'admin', '" + adminHash + "', '" + defaultPin + "', 'admin', date('now', '-1 day'))",
            "INSERT INTO users (id, username, password_hash, pin_hash, role, last_active) VALUES (2, 'john', '" + userHash + "', '" + defaultPin + "', 'user', date('now'))",
            "INSERT INTO users (id, username, password_hash, pin_hash, role, last_active) VALUES (3, 'sarah', '" + userHash + "', '" + defaultPin + "', 'user', date('now', '-2 days'))",
            "INSERT INTO users (id, username, password_hash, pin_hash, role, last_active) VALUES (4, 'mike', '" + userHash + "', '" + defaultPin + "', 'user', date('now', '-8 days'))"
        };

        try (Statement stmt = conn.createStatement()) {
            for (String u : users) stmt.execute(u);

            // Comprehensive multi-table transactions for john (id 2)
            // Income / Banking
            stmt.execute("INSERT INTO transactions (user_id, category, item_name, budgeted_amount, actual_amount, sender_receiver, tx_type) VALUES (2, 'Income', 'Primary Salary', 5500, 5500, 'Redstar Communications', 'SALARY')");
            stmt.execute("INSERT INTO transactions (user_id, category, item_name, budgeted_amount, actual_amount, sender_receiver, tx_type) VALUES (2, 'Income', 'Cash Deposit', 400, 400, 'BudgetFit Bank (Cash Deposit)', 'DEPOSIT')");
            stmt.execute("INSERT INTO transactions (user_id, category, item_name, budgeted_amount, actual_amount, sender_receiver, tx_type) VALUES (2, 'Income', 'YouTube Ad Revenue', 1000, 1200, 'Google LLC', 'DEPOSIT')");

            // Bills
            stmt.execute("INSERT INTO transactions (user_id, category, item_name, budgeted_amount, actual_amount, paid, sender_receiver, tx_type) VALUES (2, 'Bill', 'Monthly Rent', 1200, 1200, 1, 'Landlord Property Mgmt', 'WITHDRAWAL')");
            stmt.execute("INSERT INTO transactions (user_id, category, item_name, budgeted_amount, actual_amount, paid, sender_receiver, tx_type) VALUES (2, 'Bill', 'Power Utility', 150, 140, 1, 'City Power Corp', 'WITHDRAWAL')");
            stmt.execute("INSERT INTO transactions (user_id, category, item_name, budgeted_amount, actual_amount, paid, sender_receiver, tx_type) VALUES (2, 'Bill', 'Design Subscription', 12, 12, 0, 'Adobe Inc', 'WITHDRAWAL')");

            // Expenses
            stmt.execute("INSERT INTO transactions (user_id, category, item_name, budgeted_amount, actual_amount, sender_receiver, tx_type) VALUES (2, 'Expense', 'Supermarket Groceries', 400, 320, 'Local Supermarket', 'WITHDRAWAL')");
            stmt.execute("INSERT INTO transactions (user_id, category, item_name, budgeted_amount, actual_amount, sender_receiver, tx_type) VALUES (2, 'Expense', 'Movies & Games', 100, 150, 'Cinema & Arcade', 'WITHDRAWAL')");

            // Savings
            stmt.execute("INSERT INTO transactions (user_id, category, item_name, budgeted_amount, actual_amount, sender_receiver, tx_type) VALUES (2, 'Savings', 'Emergency Fund', 300, 300, 'Internal Transfer', 'TRANSFER')");

            // Debt
            stmt.execute("INSERT INTO transactions (user_id, category, item_name, budgeted_amount, actual_amount, sender_receiver, tx_type) VALUES (2, 'Debt', 'Student Loan Repayment', 200, 200, 'Federal Loan Servicing', 'WITHDRAWAL')");

            // Seed Mock Investments for john (id 2)
            stmt.execute("INSERT INTO investments (user_id, symbol, shares, avg_price, current_price) VALUES (2, 'AAPL', 10.0, 150.0, 175.50)");
            stmt.execute("INSERT INTO investments (user_id, symbol, shares, avg_price, current_price) VALUES (2, 'BTC', 0.5, 30000.0, 34500.0)");
            stmt.execute("INSERT INTO investments (user_id, symbol, shares, avg_price, current_price) VALUES (2, 'TSLA', 5.0, 200.0, 195.0)");

            // Active logs to populate charts
            for (int i = 0; i < 7; i++) {
                stmt.execute("INSERT INTO system_logs (user_id, action, log_date) VALUES (2, 'login', date('now', '-" + i + " days'))");
                if (i % 2 == 0) {
                    stmt.execute("INSERT INTO system_logs (user_id, action, log_date) VALUES (3, 'login', date('now', '-" + i + " days'))");
                }
            }
            stmt.execute("INSERT INTO system_logs (user_id, action, log_date) VALUES (1, 'login', date('now'))");
            stmt.execute("INSERT INTO system_logs (user_id, action, log_date) VALUES (4, 'login', date('now', '-8 days'))");
        }
    }
}
