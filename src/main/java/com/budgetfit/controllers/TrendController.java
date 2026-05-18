package com.budgetfit.controllers;

import com.budgetfit.dao.TransactionDAO;
import com.budgetfit.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class TrendController {

    @FXML private BarChart<String, Number> trendChart;
    @FXML private LineChart<String, Number> balanceLineChart;
    @FXML private PieChart categoryPieChart;
    @FXML private VBox categoryLegend;

    @FXML private Label avgSavingsLabel;
    @FXML private Label topExpenseLabel;
    @FXML private Label netWorthDeltaLabel;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    @FXML
    public void initialize() {
        loadTrendData();
        loadCategoryBreakdown();
    }

    private void loadTrendData() {
        Map<String, Double[]> data = TransactionDAO.getMonthlySummaries(UserSession.getUserId());
        
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");

        XYChart.Series<String, Number> balanceSeries = new XYChart.Series<>();
        balanceSeries.setName("Net Balance");
        
        double totalSavings = 0;
        int months = 0;
        double firstBalance = 0;
        double lastBalance = 0;
        int i = 0;

        // TreeMap is sorted by key (month string)
        for (Map.Entry<String, Double[]> entry : data.entrySet()) {
            double inc = entry.getValue()[0];
            double exp = entry.getValue()[1];
            double balance = inc - exp;

            incomeSeries.getData().add(new XYChart.Data<>(entry.getKey(), inc));
            expenseSeries.getData().add(new XYChart.Data<>(entry.getKey(), exp));
            balanceSeries.getData().add(new XYChart.Data<>(entry.getKey(), balance));

            totalSavings += balance;
            months++;
            if (i == 0) firstBalance = balance;
            lastBalance = balance;
            i++;
        }
        
        trendChart.getData().addAll(incomeSeries, expenseSeries);
        balanceLineChart.getData().add(balanceSeries);

        // Update Summaries
        if (months > 0) {
            avgSavingsLabel.setText(currencyFormat.format(totalSavings / months));
            double delta = totalSavings; // Cumulative savings is delta net worth
            netWorthDeltaLabel.setText((delta >= 0 ? "+ " : "- ") + currencyFormat.format(Math.abs(delta)));
        }
    }

    private void loadCategoryBreakdown() {
        Map<String, Double> breakdown = TransactionDAO.getCategoryBreakdown(UserSession.getUserId());
        String topCat = "None";
        double maxVal = 0;

        for (Map.Entry<String, Double> entry : breakdown.entrySet()) {
            PieChart.Data data = new PieChart.Data(entry.getKey(), entry.getValue());
            categoryPieChart.getData().add(data);
            
            if (entry.getValue() > maxVal) {
                maxVal = entry.getValue();
                topCat = entry.getKey();
            }

            // Simple legend
            Label legendItem = new Label(String.format("● %s: %s", entry.getKey(), currencyFormat.format(entry.getValue())));
            legendItem.setStyle("-fx-font-size: 12px; -fx-text-fill: #717977; -fx-font-weight: 600;");
            categoryLegend.getChildren().add(legendItem);
        }
        topExpenseLabel.setText(topCat);
    }

    @FXML
    private void handleBack(ActionEvent event) throws Exception {
        Stage stage = (Stage) trendChart.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard.fxml"));
        stage.setScene(new Scene(root, 1100, 850));
    }
}
