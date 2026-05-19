package com.budgetfit.controllers;

import com.budgetfit.dao.TransactionDAO;
import com.budgetfit.dao.UserDAO;
import com.budgetfit.model.TransactionEntry;
import com.budgetfit.model.User;
import com.budgetfit.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

/**
 * Controller managing ATM banking operations (Deposit, Withdrawal, Peer-to-Peer Transfer).
 * Enforces 4-digit PIN Step-Up authentication before executing any financial transaction.
 */
public class AtmController {

    @FXML private Label atmBalanceLabel;
    @FXML private TextField depAmountField;
    @FXML private TextField depSourceField;
    @FXML private TextField withAmountField;
    @FXML private TextField withDestField;
    @FXML private TextField transRecipientField;
    @FXML private TextField transAmountField;

    @FXML private Label availableCashLabel;
    @FXML private Label totalIncomingLabel;
    @FXML private Label totalOutgoingLabel;
    @FXML private Label activityIncomingTxLabel;
    @FXML private Label activityIncomingAmountLabel;
    @FXML private Label activityOutgoingTxLabel;
    @FXML private Label activityOutgoingAmountLabel;
    @FXML private Label activityWithdrawnTxLabel;
    @FXML private Label activityWithdrawnAmountLabel;
    @FXML private Label checkingBalanceLabel;
    @FXML private Label checkingTxLabel;
    @FXML private Label checkingAvgLabel;
    @FXML private Label savingsBalanceLabel;
    @FXML private Label savingsTxLabel;
    @FXML private Label savingsAvgLabel;
    @FXML private Label investmentBalanceLabel;
    @FXML private Label investmentTxLabel;
    @FXML private Label investmentAvgLabel;
    @FXML private javafx.scene.layout.VBox recentTransactionsContainer;

    @FXML
    public void initialize() {
        refreshBalance();
    }

    private static java.time.LocalDate parseTxDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return java.time.LocalDate.now();
        }
        try {
            String d = dateStr.trim();
            if (d.contains(" ")) {
                d = d.substring(0, d.indexOf(" "));
            }
            return java.time.LocalDate.parse(d);
        } catch (Exception e) {
            return java.time.LocalDate.now();
        }
    }

    private void populateRecentTransactions(List<TransactionEntry> ledger) {
        recentTransactionsContainer.getChildren().clear();
        if (ledger.isEmpty()) {
            Label noTxLabel = new Label("No recent transactions. Add a deposit to start banking.");
            noTxLabel.getStyleClass().add("card-subtitle");
            noTxLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: -bf-text-secondary; -fx-padding: 20 0;");
            recentTransactionsContainer.getChildren().add(noTxLabel);
            return;
        }

        int limit = Math.min(5, ledger.size());
        for (int i = 0; i < limit; i++) {
            TransactionEntry t = ledger.get(i);
            
            HBox row = new HBox(16);
            row.getStyleClass().add("sub-card");
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            javafx.scene.layout.StackPane badgeBox = new javafx.scene.layout.StackPane();
            badgeBox.getStyleClass().add("circle-badge-box");
            
            javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(16);
            circle.setFill(javafx.scene.paint.Color.web("#e2e8f0"));
            circle.setStrokeWidth(2.5);
            
            Label badgeText = new Label();
            badgeText.getStyleClass().add("circle-badge-text");
            
            String txType = t.getTxType();
            String category = t.getCategory();
            boolean isDeposit = "DEPOSIT".equalsIgnoreCase(txType) || "SALARY".equalsIgnoreCase(txType) || "Income".equalsIgnoreCase(category);
            boolean isSavings = "Savings".equalsIgnoreCase(category);
            
            if (isDeposit) {
                circle.setStroke(javafx.scene.paint.Color.web("#10b981"));
                badgeText.setText("✔");
                badgeText.setStyle("-fx-text-fill: #10b981; -fx-font-size: 14px; -fx-font-weight: bold;");
            } else if (isSavings) {
                circle.setStroke(javafx.scene.paint.Color.web("#8b5cf6"));
                badgeText.setText("💼");
                badgeText.setStyle("-fx-text-fill: #8b5cf6; -fx-font-size: 12px; -fx-font-weight: bold;");
            } else {
                circle.setStroke(javafx.scene.paint.Color.web("#ef4444"));
                badgeText.setText("✖");
                badgeText.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-font-weight: bold;");
            }
            badgeBox.getChildren().addAll(circle, badgeText);
            
            VBox detailsBox = new VBox(2);
            HBox.setHgrow(detailsBox, javafx.scene.layout.Priority.ALWAYS);
            
            Label titleLabel = new Label(t.getCategory());
            titleLabel.getStyleClass().add("card-title");
            
            Label subtitleLabel = new Label(t.getSenderReceiver() != null && !t.getSenderReceiver().isEmpty() ? t.getSenderReceiver() : t.getType());
            subtitleLabel.getStyleClass().add("card-subtitle");
            
            detailsBox.getChildren().addAll(titleLabel, subtitleLabel);
            
            VBox amountBox = new VBox(2);
            amountBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            
            Label amountLabel = new Label();
            amountLabel.getStyleClass().addAll("bento-stat-sub", "mono");
            amountLabel.setStyle("-fx-font-size: 15px;");
            
            if (isDeposit) {
                amountLabel.setText("+ " + String.format("$%,.2f", t.getActualAmount()));
                amountLabel.setStyle(amountLabel.getStyle() + " -fx-text-fill: #10b981;");
            } else {
                amountLabel.setText("- " + String.format("$%,.2f", t.getActualAmount()));
                amountLabel.setStyle(amountLabel.getStyle() + " -fx-text-fill: #ef4444;");
            }
            
            Label statusLabel = new Label(t.isPaid() ? "Settled" : "Pending");
            statusLabel.getStyleClass().add("card-subtitle");
            statusLabel.setStyle("-fx-font-size: 11px;");
            
            amountBox.getChildren().addAll(amountLabel, statusLabel);
            
            row.getChildren().addAll(badgeBox, detailsBox, amountBox);
            recentTransactionsContainer.getChildren().add(row);
        }
    }

    public void refreshBalance() {
        int userId = UserSession.getUserId();
        List<TransactionEntry> ledger = TransactionDAO.loadBankingLedger(userId);
        
        double checkingBal = calculateAtmBalance(userId);
        if (atmBalanceLabel != null) {
            atmBalanceLabel.setText(String.format("$%,.2f", checkingBal));
        }
        if (availableCashLabel != null) {
            availableCashLabel.setText(String.format("$%,.2f", checkingBal));
        }

        double totalIncoming = 0.0;
        double totalOutgoing = 0.0;
        
        int checkingTxCount = 0;
        int savingsTxCount = 0;
        int investmentTxCount = 0;
        
        int incomingTxWeek = 0;
        double incomingSumWeek = 0.0;
        int outgoingTxWeek = 0;
        double outgoingSumWeek = 0.0;
        int withdrawnTxWeek = 0;
        double withdrawnSumWeek = 0.0;
        
        java.time.LocalDate now = java.time.LocalDate.now();
        
        for (TransactionEntry t : ledger) {
            String txType = t.getTxType();
            String type = t.getType();
            String category = t.getCategory();
            double amt = t.getActualAmount();
            
            boolean isDeposit = "DEPOSIT".equalsIgnoreCase(txType) || "SALARY".equalsIgnoreCase(txType) || "Income".equalsIgnoreCase(type);
            boolean isWithdrawal = "WITHDRAWAL".equalsIgnoreCase(txType);
            boolean isTransfer = "TRANSFER".equalsIgnoreCase(txType);
            boolean isSavings = "Savings".equalsIgnoreCase(type);
            boolean isInvestment = category != null && category.toLowerCase().contains("investment");
            
            if (isDeposit) {
                totalIncoming += amt;
                checkingTxCount++;
            } else if (isWithdrawal || isTransfer || isSavings) {
                totalOutgoing += amt;
                if (isSavings) {
                    savingsTxCount++;
                } else if (isInvestment) {
                    investmentTxCount++;
                } else {
                    checkingTxCount++;
                }
            }
            
            java.time.LocalDate txDate = parseTxDate(t.getDate());
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(txDate, now);
            if (daysBetween >= 0 && daysBetween <= 7) {
                if (isDeposit) {
                    incomingTxWeek++;
                    incomingSumWeek += amt;
                } else if (isWithdrawal) {
                    withdrawnTxWeek++;
                    withdrawnSumWeek += amt;
                } else if (isTransfer || isSavings) {
                    outgoingTxWeek++;
                    outgoingSumWeek += amt;
                }
            }
        }
        
        if (totalIncomingLabel != null) {
            totalIncomingLabel.setText(String.format("$%,.2f", totalIncoming));
        }
        if (totalOutgoingLabel != null) {
            totalOutgoingLabel.setText(String.format("$%,.2f", totalOutgoing));
        }
        
        if (activityIncomingTxLabel != null) activityIncomingTxLabel.setText(String.format("%d TX", incomingTxWeek));
        if (activityIncomingAmountLabel != null) activityIncomingAmountLabel.setText(String.format("$%,.2f", incomingSumWeek));
        if (activityOutgoingTxLabel != null) activityOutgoingTxLabel.setText(String.format("%d TX", outgoingTxWeek));
        if (activityOutgoingAmountLabel != null) activityOutgoingAmountLabel.setText(String.format("$%,.2f", outgoingSumWeek));
        if (activityWithdrawnTxLabel != null) activityWithdrawnTxLabel.setText(String.format("%d TX", withdrawnTxWeek));
        if (activityWithdrawnAmountLabel != null) activityWithdrawnAmountLabel.setText(String.format("$%,.2f", withdrawnSumWeek));
        
        if (checkingBalanceLabel != null) checkingBalanceLabel.setText(String.format("$%,.2f", checkingBal));
        if (checkingTxLabel != null) checkingTxLabel.setText(String.format("%d TX", checkingTxCount));
        if (checkingAvgLabel != null) {
            double avg = checkingTxCount > 0 ? (checkingBal / checkingTxCount) : 0.0;
            checkingAvgLabel.setText(String.format("$%,.2f", avg));
        }
        
        double savingsBal = 0.0;
        try {
            List<com.budgetfit.model.Goal> goals = com.budgetfit.dao.GoalDAO.getGoals(userId);
            for (com.budgetfit.model.Goal g : goals) {
                savingsBal += g.getCurrentAmount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (savingsBalanceLabel != null) savingsBalanceLabel.setText(String.format("$%,.2f", savingsBal));
        if (savingsTxLabel != null) savingsTxLabel.setText(String.format("%d TX", savingsTxCount));
        if (savingsAvgLabel != null) {
            double avg = savingsTxCount > 0 ? (savingsBal / savingsTxCount) : 0.0;
            savingsAvgLabel.setText(String.format("$%,.2f", avg));
        }
        
        double investmentBal = 0.0;
        try {
            List<com.budgetfit.model.Investment> investments = com.budgetfit.dao.InvestmentDAO.getInvestments(userId);
            for (com.budgetfit.model.Investment inv : investments) {
                investmentBal += inv.getTotalValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (investmentBalanceLabel != null) investmentBalanceLabel.setText(String.format("$%,.2f", investmentBal));
        if (investmentTxLabel != null) investmentTxLabel.setText(String.format("%d TX", investmentTxCount));
        if (investmentAvgLabel != null) {
            double avg = investmentTxCount > 0 ? (investmentBal / investmentTxCount) : 0.0;
            investmentAvgLabel.setText(String.format("$%,.2f", avg));
        }
        
        if (recentTransactionsContainer != null) {
            populateRecentTransactions(ledger);
        }
    }

    public static double calculateAtmBalance(int userId) {
        double balance = 0.0;
        List<TransactionEntry> ledger = TransactionDAO.loadBankingLedger(userId);
        for (TransactionEntry t : ledger) {
            String txType = t.getTxType();
            if ("DEPOSIT".equalsIgnoreCase(txType) || "SALARY".equalsIgnoreCase(txType)) {
                balance += t.getActualAmount();
            } else if ("WITHDRAWAL".equalsIgnoreCase(txType) || "TRANSFER".equalsIgnoreCase(txType)) {
                balance -= t.getActualAmount();
            }
        }
        return balance;
    }

    private boolean promptForPin(String actionName) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("PIN Verification Required");
        dialog.setHeaderText("Authorize " + actionName);
        dialog.setContentText("Please enter your 4-digit ATM PIN:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String pin = result.get().trim();
            if (UserDAO.verifyPin(UserSession.getUserId(), pin)) {
                return true;
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid PIN entered. Transaction aborted.");
                alert.show();
                return false;
            }
        }
        return false;
    }

    @FXML
    private void handleDeposit(ActionEvent event) {
        try {
            double amount = Double.parseDouble(depAmountField.getText().trim());
            if (amount <= 0) {
                showAlert(Alert.AlertType.WARNING, "Please enter a valid deposit amount greater than zero.");
                return;
            }
            String source = depSourceField.getText().trim();
            if (source.isEmpty()) {
                source = "BudgetFit Bank (Cash Deposit)";
            }

            if (promptForPin("Cash Deposit of $" + amount)) {
                if (TransactionDAO.insertBankingTransaction(UserSession.getUserId(), "Income", "Cash Deposit", amount, source, "DEPOSIT")) {
                    depAmountField.clear();
                    depSourceField.clear();
                    showAlert(Alert.AlertType.INFORMATION, "Successfully deposited $" + amount + " into your ATM account.");
                    refreshBalance();
                    if (DashboardController.getInstance() != null) {
                        DashboardController.getInstance().refreshAll();
                    }
                }
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid numeric amount entered.");
        }
    }

    @FXML
    private void handleWithdrawal(ActionEvent event) {
        try {
            double amount = Double.parseDouble(withAmountField.getText().trim());
            if (amount <= 0) {
                showAlert(Alert.AlertType.WARNING, "Please enter a valid withdrawal amount greater than zero.");
                return;
            }
            double currentBal = calculateAtmBalance(UserSession.getUserId());
            if (amount > currentBal) {
                showAlert(Alert.AlertType.ERROR, "Insufficient ATM funds! Your available balance is $" + String.format("%,.2f", currentBal));
                return;
            }

            String dest = withDestField.getText().trim();
            if (dest.isEmpty()) {
                dest = "ATM Cash Withdrawal";
            }

            if (promptForPin("Cash Withdrawal of $" + amount)) {
                if (TransactionDAO.insertBankingTransaction(UserSession.getUserId(), "Expense", dest, amount, "ATM Withdrawal", "WITHDRAWAL")) {
                    withAmountField.clear();
                    withDestField.clear();
                    showAlert(Alert.AlertType.INFORMATION, "Successfully withdrew $" + amount + " from your ATM account.");
                    refreshBalance();
                    if (DashboardController.getInstance() != null) {
                        DashboardController.getInstance().refreshAll();
                    }
                }
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid numeric amount entered.");
        }
    }

    @FXML
    private void handleTransfer(ActionEvent event) {
        try {
            String recipient = transRecipientField.getText().trim();
            if (recipient.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Please enter a recipient username.");
                return;
            }
            if (recipient.equalsIgnoreCase(UserSession.getUsername())) {
                showAlert(Alert.AlertType.WARNING, "You cannot transfer funds to yourself.");
                return;
            }
            if (!UserDAO.usernameExists(recipient)) {
                showAlert(Alert.AlertType.ERROR, "Recipient username '" + recipient + "' does not exist in the system.");
                return;
            }

            double amount = Double.parseDouble(transAmountField.getText().trim());
            if (amount <= 0) {
                showAlert(Alert.AlertType.WARNING, "Please enter a valid transfer amount greater than zero.");
                return;
            }
            double currentBal = calculateAtmBalance(UserSession.getUserId());
            if (amount > currentBal) {
                showAlert(Alert.AlertType.ERROR, "Insufficient ATM funds! Your available balance is $" + String.format("%,.2f", currentBal));
                return;
            }

            if (promptForPin("P2P Transfer of $" + amount + " to " + recipient)) {
                boolean s1 = TransactionDAO.insertBankingTransaction(UserSession.getUserId(), "Expense", "Transfer to " + recipient, amount, "P2P Transfer", "TRANSFER");
                
                int recUserId = -1;
                for (User u : UserDAO.getAllUsers()) {
                    if (u.getUsername().equalsIgnoreCase(recipient)) {
                        recUserId = u.getId();
                        break;
                    }
                }

                if (s1 && recUserId != -1) {
                    TransactionDAO.insertBankingTransaction(recUserId, "Income", "Transfer from " + UserSession.getUsername(), amount, "P2P Transfer", "DEPOSIT");
                    transRecipientField.clear();
                    transAmountField.clear();
                    showAlert(Alert.AlertType.INFORMATION, "Successfully transferred $" + amount + " to " + recipient + ".");
                    refreshBalance();
                    if (DashboardController.getInstance() != null) {
                        DashboardController.getInstance().refreshAll();
                    }
                }
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid numeric amount entered.");
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.show();
    }
}
