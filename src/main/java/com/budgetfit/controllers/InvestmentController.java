package com.budgetfit.controllers;

import com.budgetfit.dao.InvestmentDAO;
import com.budgetfit.dao.TransactionDAO;
import com.budgetfit.model.Investment;
import com.budgetfit.session.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Controller managing the investment simulation engine.
 * Connects directly with the ATM cash balance for buying/selling assets and supports live market price simulation.
 */
public class InvestmentController {

    @FXML private Label portfolioValueLabel;
    @FXML private Label portfolioPnlLabel;
    @FXML private LineChart<String, Number> portfolioSparkline;
    @FXML private HBox topAssetsContainer;

    @FXML private TableView<Investment> investmentTable;
    @FXML private TableColumn<Investment, String> symCol;
    @FXML private TableColumn<Investment, Double> sharesCol;
    @FXML private TableColumn<Investment, Double> avgCol;
    @FXML private TableColumn<Investment, Double> curCol;
    @FXML private TableColumn<Investment, Double> valCol;
    @FXML private TableColumn<Investment, Double> pnlCol;

    @FXML private TextField buySymbolField;
    @FXML private TextField buySharesField;
    @FXML private TextField buyPriceField;

    private ObservableList<Investment> investmentList;

    @FXML
    public void initialize() {
        setupTable();
        loadInvestments();
    }

    private void setupTable() {
        symCol.setCellValueFactory(d -> d.getValue().symbolProperty());
        sharesCol.setCellValueFactory(d -> d.getValue().sharesProperty().asObject());
        avgCol.setCellValueFactory(d -> d.getValue().avgPriceProperty().asObject());
        curCol.setCellValueFactory(d -> d.getValue().currentPriceProperty().asObject());

        valCol.setCellValueFactory(d -> new javafx.beans.property.SimpleDoubleProperty(d.getValue().getTotalValue()).asObject());
        pnlCol.setCellValueFactory(d -> new javafx.beans.property.SimpleDoubleProperty(d.getValue().getProfitLoss()).asObject());

        // Format cells
        sharesCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("%,.4f", item));
            }
        });
        avgCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("$%,.2f", item));
            }
        });
        curCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("$%,.2f", item));
            }
        });
        valCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("$%,.2f", item));
            }
        });
        pnlCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("$%,.2f", item));
                    if (item < 0) setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    else if (item > 0) setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: #7f8c8d;");
                }
            }
        });
    }

    public void loadInvestments() {
        if (investmentTable == null) return;
        List<Investment> list = InvestmentDAO.getInvestments(UserSession.getUserId());
        investmentList = FXCollections.observableArrayList(list);
        investmentTable.setItems(investmentList);

        double totalVal = 0.0;
        double totalPnl = 0.0;
        for (Investment inv : list) {
            totalVal += inv.getTotalValue();
            totalPnl += inv.getProfitLoss();
        }

        portfolioValueLabel.setText(String.format("$%,.2f", totalVal));
        portfolioPnlLabel.setText(String.format("$%,.2f", totalPnl));

        if (totalPnl < 0) portfolioPnlLabel.setStyle("-fx-text-fill: #e74c3c;");
        else if (totalPnl > 0) portfolioPnlLabel.setStyle("-fx-text-fill: #2ecc71;");
        else portfolioPnlLabel.setStyle("-fx-text-fill: -bf-text-secondary;");

        if (portfolioSparkline != null) {
            portfolioSparkline.getData().clear();
            javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>(); series.setName("Value");
            series.getData().add(new javafx.scene.chart.XYChart.Data<>("Mon", totalVal * 0.95));
            series.getData().add(new javafx.scene.chart.XYChart.Data<>("Tue", totalVal * 0.98));
            series.getData().add(new javafx.scene.chart.XYChart.Data<>("Wed", totalVal * 1.02));
            series.getData().add(new javafx.scene.chart.XYChart.Data<>("Thu", totalVal * 0.99));
            series.getData().add(new javafx.scene.chart.XYChart.Data<>("Fri", totalVal));
            portfolioSparkline.getData().add(series);
        }

        if (topAssetsContainer != null) {
            topAssetsContainer.getChildren().clear();
            String[] styles = {"asset-card-1", "asset-card-2", "asset-card-3"};
            String[] pills = {"asset-pill-1", "asset-pill-2", "asset-pill-3"};
            String[] symbols = {"₿", "Ł", "Ξ"};
            String[] defNames = {"BTC", "LTC", "ETH"};
            double[] defShares = {1.25, 0.32, 1.25};
            double[] defPrices = {2948.04, 2948.04, 2948.04};
            String[] defChanges = {"+ 0.14%", "+ 0.31%", "+ 0.27%"};

            for (int i = 0; i < 3; i++) {
                String sym = i < list.size() ? list.get(i).getSymbol().toUpperCase() : defNames[i];
                double shares = i < list.size() ? list.get(i).getShares() : defShares[i];
                double val = i < list.size() ? list.get(i).getTotalValue() : (defShares[i] * defPrices[i]);
                String changeStr = i < list.size() ? String.format("%+.2f%%", (list.get(i).getCurrentPrice() - list.get(i).getAvgPrice())/list.get(i).getAvgPrice() * 100) : defChanges[i];

                VBox card = new VBox(12);
                card.getStyleClass().add(styles[i]);

                HBox top = new HBox(8); top.setAlignment(Pos.CENTER_LEFT);
                Label title = new Label(String.format("%.2f %s", shares, sym)); title.getStyleClass().add("asset-title");
                Region spc = new Region(); HBox.setHgrow(spc, Priority.ALWAYS);
                Label opt = new Label("⋮"); opt.setStyle("-fx-text-fill: #a0aec0; -fx-font-weight: bold;");
                top.getChildren().addAll(title, spc, opt);

                Label sub = new Label(String.format("$%,.2f", val)); sub.getStyleClass().add("asset-sub");

                HBox bot = new HBox(8); bot.setAlignment(Pos.CENTER_LEFT);
                Label iconLbl = new Label(symbols[i % symbols.length]); iconLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -bf-text-primary;");
                Region bSpc = new Region(); HBox.setHgrow(bSpc, Priority.ALWAYS);
                Label pill = new Label(changeStr); pill.getStyleClass().add(pills[i]);
                bot.getChildren().addAll(iconLbl, bSpc, pill);

                card.getChildren().addAll(top, sub, bot);
                topAssetsContainer.getChildren().add(card);
            }
        }
    }

    @FXML
    private void handleBuy(ActionEvent event) {
        try {
            String symbol = buySymbolField.getText().trim();
            if (symbol.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Please enter an asset symbol.");
                return;
            }
            double shares = Double.parseDouble(buySharesField.getText().trim());
            double price = Double.parseDouble(buyPriceField.getText().trim());

            if (shares <= 0 || price <= 0) {
                showAlert(Alert.AlertType.WARNING, "Shares and price must be greater than zero.");
                return;
            }

            double totalCost = shares * price;
            double currentAtmBal = AtmController.calculateAtmBalance(UserSession.getUserId());

            if (totalCost > currentAtmBal) {
                showAlert(Alert.AlertType.ERROR, "Insufficient ATM cash! Buying this asset requires $" + String.format("%,.2f", totalCost) + " but your ATM balance is $" + String.format("%,.2f", currentAtmBal));
                return;
            }

            // Deduct from ATM
            if (TransactionDAO.insertBankingTransaction(UserSession.getUserId(), "Expense", "Investment Buy: " + symbol.toUpperCase(), totalCost, "Investment Simulator", "WITHDRAWAL")) {
                if (InvestmentDAO.buyInvestment(UserSession.getUserId(), symbol, shares, price)) {
                    buySymbolField.clear();
                    buySharesField.clear();
                    buyPriceField.clear();
                    showAlert(Alert.AlertType.INFORMATION, "Successfully purchased " + shares + " shares of " + symbol.toUpperCase() + ".");
                    loadInvestments();
                    if (DashboardController.getInstance() != null) {
                        DashboardController.getInstance().refreshAll();
                    }
                }
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid numeric values entered for shares or price.");
        }
    }

    @FXML
    private void handleSell(ActionEvent event) {
        Investment selected = investmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Please select an investment holding from the table to sell.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getShares()));
        dialog.setTitle("Sell Investment");
        dialog.setHeaderText("Sell shares of " + selected.getSymbol());
        dialog.setContentText("Enter number of shares to sell (Max: " + selected.getShares() + "):");

        dialog.showAndWait().ifPresent(input -> {
            try {
                double sharesToSell = Double.parseDouble(input.trim());
                if (sharesToSell <= 0 || sharesToSell > selected.getShares()) {
                    showAlert(Alert.AlertType.ERROR, "Invalid number of shares entered.");
                    return;
                }

                double proceeds = sharesToSell * selected.getCurrentPrice();
                if (InvestmentDAO.sellInvestment(UserSession.getUserId(), selected.getSymbol(), sharesToSell)) {
                    // Add proceeds to ATM
                    TransactionDAO.insertBankingTransaction(UserSession.getUserId(), "Income", "Investment Sell: " + selected.getSymbol(), proceeds, "Investment Simulator", "DEPOSIT");
                    showAlert(Alert.AlertType.INFORMATION, "Successfully sold " + sharesToSell + " shares of " + selected.getSymbol() + " for $" + String.format("%,.2f", proceeds) + ".");
                    loadInvestments();
                    if (DashboardController.getInstance() != null) {
                        DashboardController.getInstance().refreshAll();
                    }
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid numeric value entered.");
            }
        });
    }

    @FXML
    private void handleSimulateMarket(ActionEvent event) {
        InvestmentDAO.simulateMarketFluctuations(UserSession.getUserId());
        loadInvestments();
        showAlert(Alert.AlertType.INFORMATION, "Market fluctuations simulated successfully! Prices have shifted +/- 5%.");
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.show();
    }
}
