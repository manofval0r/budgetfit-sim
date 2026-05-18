package com.budgetfit.dao;

import com.budgetfit.database.DatabaseHelper;
import com.budgetfit.model.Investment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Data Access Object (DAO) managing user investment portfolios.
 * Enforces per-user tracking and provides market simulation mechanics.
 */
public class InvestmentDAO {

    /**
     * Retrieves all investment holdings for a specific user.
     */
    public static List<Investment> getInvestments(int userId) {
        List<Investment> list = new ArrayList<>();
        String sql = "SELECT id, symbol, shares, avg_price, current_price FROM investments WHERE user_id = ? ORDER BY symbol ASC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Investment(
                            rs.getInt("id"),
                            userId,
                            rs.getString("symbol"),
                            rs.getDouble("shares"),
                            rs.getDouble("avg_price"),
                            rs.getDouble("current_price")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Buys shares of an investment asset, updating cost basis or inserting a new record.
     */
    public static boolean buyInvestment(int userId, String symbol, double shares, double price) {
        if (shares <= 0 || price <= 0) return false;
        String checkSql = "SELECT id, shares, avg_price FROM investments WHERE user_id = ? AND symbol = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, userId);
            checkStmt.setString(2, symbol.toUpperCase());

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    double oldShares = rs.getDouble("shares");
                    double oldAvg = rs.getDouble("avg_price");

                    double newShares = oldShares + shares;
                    double newAvg = ((oldShares * oldAvg) + (shares * price)) / newShares;

                    try (PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE investments SET shares = ?, avg_price = ?, current_price = ?, last_updated = CURRENT_TIMESTAMP WHERE id = ?")) {
                        updateStmt.setDouble(1, newShares);
                        updateStmt.setDouble(2, newAvg);
                        updateStmt.setDouble(3, price);
                        updateStmt.setInt(4, id);
                        return updateStmt.executeUpdate() > 0;
                    }
                } else {
                    try (PreparedStatement insertStmt = conn.prepareStatement(
                            "INSERT INTO investments (user_id, symbol, shares, avg_price, current_price) VALUES (?, ?, ?, ?, ?)")) {
                        insertStmt.setInt(1, userId);
                        insertStmt.setString(2, symbol.toUpperCase());
                        insertStmt.setDouble(3, shares);
                        insertStmt.setDouble(4, price);
                        insertStmt.setDouble(5, price);
                        return insertStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Sells shares of an investment asset.
     */
    public static boolean sellInvestment(int userId, String symbol, double sharesToSell) {
        if (sharesToSell <= 0) return false;
        String checkSql = "SELECT id, shares FROM investments WHERE user_id = ? AND symbol = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, userId);
            checkStmt.setString(2, symbol.toUpperCase());

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    double currentShares = rs.getDouble("shares");

                    if (sharesToSell >= currentShares) {
                        try (PreparedStatement delStmt = conn.prepareStatement("DELETE FROM investments WHERE id = ?")) {
                            delStmt.setInt(1, id);
                            return delStmt.executeUpdate() > 0;
                        }
                    } else {
                        try (PreparedStatement updateStmt = conn.prepareStatement(
                                "UPDATE investments SET shares = ?, last_updated = CURRENT_TIMESTAMP WHERE id = ?")) {
                            updateStmt.setDouble(1, currentShares - sharesToSell);
                            updateStmt.setInt(2, id);
                            return updateStmt.executeUpdate() > 0;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Simulates dynamic market price fluctuations (+/- 5%) across a user's holdings.
     */
    public static void simulateMarketFluctuations(int userId) {
        List<Investment> investments = getInvestments(userId);
        if (investments.isEmpty()) return;

        Random random = new Random();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE investments SET current_price = ?, last_updated = CURRENT_TIMESTAMP WHERE id = ?")) {

            for (Investment inv : investments) {
                // Random fluctuation between -5% and +5%
                double factor = 0.95 + (0.10 * random.nextDouble());
                double newPrice = Math.max(0.01, inv.getCurrentPrice() * factor);

                stmt.setDouble(1, newPrice);
                stmt.setInt(2, inv.getId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
