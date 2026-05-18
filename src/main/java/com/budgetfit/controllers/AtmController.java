package com.budgetfit.controllers;

import com.budgetfit.dao.TransactionDAO;
import com.budgetfit.dao.UserDAO;
import com.budgetfit.model.TransactionEntry;
import com.budgetfit.model.User;
import com.budgetfit.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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

    @FXML
    public void initialize() {
        refreshBalance();
    }

    public void refreshBalance() {
        if (atmBalanceLabel != null) {
            double bal = calculateAtmBalance(UserSession.getUserId());
            atmBalanceLabel.setText(String.format("$%,.2f", bal));
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
