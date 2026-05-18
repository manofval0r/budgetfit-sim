package com.budgetfit.controllers;

import com.budgetfit.dao.UserDAO;
import com.budgetfit.database.DatabaseHelper;
import com.budgetfit.model.User;
import com.budgetfit.session.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller for the Administrative dashboard.
 * Provides system stats and user management capabilities.
 */
public class AdminController {

    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label churnUsersLabel;
    @FXML private LineChart<String, Number> activityChart;

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> userIdCol;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TableColumn<User, String> lastActiveCol;
    @FXML private TableColumn<User, Void> userActionCol;

    private ObservableList<User> userList;

    @FXML
    public void initialize() {
        loadStats();
        setupUserTable();
    }

    private void setupUserTable() {
        userList = FXCollections.observableArrayList(UserDAO.getAllUsers());
        userTable.setItems(userList);

        userIdCol.setCellValueFactory(d -> d.getValue().idProperty().asObject());
        usernameCol.setCellValueFactory(d -> d.getValue().usernameProperty());
        roleCol.setCellValueFactory(d -> d.getValue().roleProperty());
        lastActiveCol.setCellValueFactory(d -> d.getValue().lastActiveProperty());

        userActionCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("🗑");
            {
                btn.getStyleClass().add("btn-icon");
                btn.setStyle("-fx-text-fill: #ba1a1a;");
                btn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    if (u.getId() == UserSession.getUserId()) {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "You cannot delete your own admin account.");
                        alert.show();
                        return;
                    }
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete user '" + u.getUsername() + "'?", ButtonType.YES, ButtonType.NO);
                    if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                        if (UserDAO.deleteUser(u.getId())) {
                            userList.remove(u);
                            loadStats();
                        }
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btn);
            }
        });
    }

    private void loadStats() {
        totalUsersLabel.setText(String.valueOf(UserDAO.getTotalUsersCount()));
        activeUsersLabel.setText(String.valueOf(UserDAO.getActiveUsersCount()));
        churnUsersLabel.setText(String.valueOf(UserDAO.getChurnUsersCount()));
        loadActivityChart();
    }

    private void loadActivityChart() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            counts.put(date.format(formatter), 0);
        }

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT strftime('%m-%d', log_date) AS day, COUNT(*) " +
                             "FROM system_logs WHERE action = 'login' " +
                             "AND log_date >= date('now', '-6 day') " +
                             "GROUP BY day")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String day = rs.getString("day");
                int count = rs.getInt(2);
                if (counts.containsKey(day)) {
                    counts.put(day, count);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        activityChart.getData().setAll(series);
    }

    @FXML
    private void handleLogout(ActionEvent event) throws Exception {
        UserSession.clear();
        Stage stage = (Stage) totalUsersLabel.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        stage.setScene(new Scene(root, 500, 500));
    }
}
