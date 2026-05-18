package com.budgetfit.dao;

import com.budgetfit.database.DatabaseHelper;
import com.budgetfit.model.Goal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoalDAO {

    public static List<Goal> getGoals(int userId) {
        List<Goal> list = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, name, target_amount, current_amount, target_date FROM goals WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Goal(rs.getInt("id"), rs.getString("name"), rs.getDouble("target_amount"), rs.getDouble("current_amount"), rs.getString("target_date")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void insertGoal(int userId, Goal goal) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO goals (user_id, name, target_amount, current_amount, target_date) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setString(2, goal.getName());
            stmt.setDouble(3, goal.getTargetAmount());
            stmt.setDouble(4, goal.getCurrentAmount());
            stmt.setString(5, goal.getTargetDate());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) goal.idProperty().set(keys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateGoal(int userId, Goal goal) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE goals SET name = ?, target_amount = ?, current_amount = ?, target_date = ? WHERE id = ? AND user_id = ?")) {
            stmt.setString(1, goal.getName());
            stmt.setDouble(2, goal.getTargetAmount());
            stmt.setDouble(3, goal.getCurrentAmount());
            stmt.setString(4, goal.getTargetDate());
            stmt.setInt(5, goal.getId());
            stmt.setInt(6, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteGoal(int userId, int goalId) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM goals WHERE id = ? AND user_id = ?")) {
            stmt.setInt(1, goalId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
