package com.budgetfit.controllers;

import com.budgetfit.dao.TransactionDAO;
import com.budgetfit.dao.UserDAO;
import com.budgetfit.model.TransactionEntry;
import com.budgetfit.service.BudgetService;
import com.budgetfit.service.NotificationService;
import com.budgetfit.session.UserSession;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.File;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Predicate;

/**
 * Main dashboard controller managing financial tracking, data visualization, and user settings.
 * Refactored to include sidebar navigation switching between ATM, Budgeting, Savings, Investments, and Ledger views.
 */
public class DashboardController {

    private static DashboardController instance;

    @FXML private BorderPane mainPane;
    @FXML private Label welcomeLabel;
    @FXML private Label viewTitleLabel;
    @FXML private Label amountLeftLabel;
    @FXML private TextField monthlyIncomeField;
    @FXML private VBox goalsContainer;
    @FXML private PieChart budgetPieChart;
    @FXML private PieChart allocationPieChart;

    // View Switching Containers
    @FXML private StackPane contentArea;
    @FXML private ScrollPane budgetView;
    @FXML private ScrollPane savingsView;
    @FXML private FlowPane fullGoalsContainer;
    @FXML private ScrollPane ledgerView;

    // Savings Redesign Fields
    @FXML private Label savHeroTotalLabel;
    @FXML private Label savHeroTargetLabel;
    @FXML private ProgressBar savHeroProgressBar;
    @FXML private BarChart<String, Number> savGrowthChart;
    @FXML private VBox savActivityContainer;

    @FXML private Button navAtmBtn;
    @FXML private Button navBudgetBtn;
    @FXML private Button navSavingsBtn;
    @FXML private Button navInvestmentsBtn;
    @FXML private Button navLedgerBtn;

    // Sidebar Toggle Fields
    @FXML private VBox leftSidebar;
    @FXML private Label brandTitle;
    @FXML private Label lblAtm;
    @FXML private Label lblBudget;
    @FXML private Label lblSavings;
    @FXML private Label lblInvestments;
    @FXML private Label lblLedger;
    @FXML private Label lblLogout;
    @FXML private Label lblTheme;
    private boolean isSidebarCollapsed = true;

    // Ledger Table
    @FXML private TableView<TransactionEntry> ledgerTable;
    @FXML private TableColumn<TransactionEntry, String> ledgDateCol;
    @FXML private TableColumn<TransactionEntry, String> ledgTypeCol;
    @FXML private TableColumn<TransactionEntry, String> ledgCatCol;
    @FXML private TableColumn<TransactionEntry, String> ledgItemCol;
    @FXML private TableColumn<TransactionEntry, String> ledgSenderCol;
    @FXML private TableColumn<TransactionEntry, Double> ledgAmountCol;

    // Ledger Inspect Panel Fields
    @FXML private Label ledgerShowingLabel;
    @FXML private Label detailIdLabel;
    @FXML private Label detailDateLabel;
    @FXML private Label detailTypeLabel;
    @FXML private ComboBox<String> detailCatCombo;
    private TransactionEntry selectedLedgerEntry;
    @FXML private Label tabAll, tabIncome, tabExpenses, tabBills, tabSavings, tabDebt;

    // External Sub-Views
    private Parent atmView;
    private AtmController atmController;
    private Parent investmentsView;
    private InvestmentController investmentController;

    // Bento Stats
    @FXML private Label statTotalIncome;
    @FXML private Label statBurnRate;
    @FXML private Label statBurnRateSub;
    @FXML private Label statSavingsRate;

    @FXML private ProgressBar incProgressBar;
    @FXML private ProgressBar expProgressBar;
    @FXML private ProgressBar billProgressBar;
    @FXML private ProgressBar savProgressBar;
    @FXML private ProgressBar debtProgressBar;

    // Controls
    @FXML private ComboBox<String> monthSelector;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> currencySelector;
    @FXML private CheckBox darkModeBox;

    // Tables
    @FXML private TableView<TransactionEntry> incomeTable;
    @FXML private TableColumn<TransactionEntry, String> incCatCol;
    @FXML private TableColumn<TransactionEntry, Double> incBudgetCol;
    @FXML private TableColumn<TransactionEntry, Double> incActualCol;
    @FXML private TableColumn<TransactionEntry, Boolean> incRecCol;
    @FXML private TableColumn<TransactionEntry, Void> incActionCol;
    @FXML private Label incTotalBudgetLabel;
    @FXML private Label incTotalActualLabel;

    @FXML private TableView<TransactionEntry> expenseTable;
    @FXML private TableColumn<TransactionEntry, String> expCatCol;
    @FXML private TableColumn<TransactionEntry, Double> expBudgetCol;
    @FXML private TableColumn<TransactionEntry, Double> expActualCol;
    @FXML private TableColumn<TransactionEntry, Boolean> expRecCol;
    @FXML private TableColumn<TransactionEntry, Void> expActionCol;
    @FXML private Label expTotalBudgetLabel;
    @FXML private Label expTotalActualLabel;

    @FXML private TableView<TransactionEntry> billTable;
    @FXML private TableColumn<TransactionEntry, String> billCatCol;
    @FXML private TableColumn<TransactionEntry, Double> billBudgetCol;
    @FXML private TableColumn<TransactionEntry, Double> billActualCol;
    @FXML private TableColumn<TransactionEntry, Boolean> billPaidCol;
    @FXML private TableColumn<TransactionEntry, Boolean> billRecCol;
    @FXML private TableColumn<TransactionEntry, Void> billActionCol;
    @FXML private Label billTotalBudgetLabel;
    @FXML private Label billTotalActualLabel;

    @FXML private TableView<TransactionEntry> savingsTable;
    @FXML private TableColumn<TransactionEntry, String> savCatCol;
    @FXML private TableColumn<TransactionEntry, Double> savBudgetCol;
    @FXML private TableColumn<TransactionEntry, Double> savActualCol;
    @FXML private TableColumn<TransactionEntry, Boolean> savRecCol;
    @FXML private TableColumn<TransactionEntry, Void> savActionCol;
    @FXML private Label savTotalBudgetLabel;
    @FXML private Label savTotalActualLabel;

    @FXML private TableView<TransactionEntry> debtTable;
    @FXML private TableColumn<TransactionEntry, String> debtCatCol;
    @FXML private TableColumn<TransactionEntry, Double> debtBudgetCol;
    @FXML private TableColumn<TransactionEntry, Double> debtActualCol;
    @FXML private TableColumn<TransactionEntry, Boolean> debtRecCol;
    @FXML private TableColumn<TransactionEntry, Void> debtActionCol;
    @FXML private Label debtTotalBudgetLabel;
    @FXML private Label debtTotalActualLabel;

    // Backend Lists
    private ObservableList<TransactionEntry> allTransactions;
    private FilteredList<TransactionEntry> incomeList;
    private FilteredList<TransactionEntry> expenseList;
    private FilteredList<TransactionEntry> billList;
    private FilteredList<TransactionEntry> savingsList;
    private FilteredList<TransactionEntry> debtList;

    private final BudgetService budgetService = new BudgetService();
    private final PauseTransition chartDebouncer = new PauseTransition(Duration.millis(300));
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    public static DashboardController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        welcomeLabel.setText("Welcome, " + UserSession.getUsername() + "!");

        setupCurrencySelector();
        setupMonthSelector();
        setupSearchFilter();

        // Load external ATM & Investment sub-views
        try {
            FXMLLoader atmLoader = new FXMLLoader(getClass().getResource("/fxml/atm.fxml"));
            atmView = atmLoader.load();
            atmController = atmLoader.getController();

            FXMLLoader invLoader = new FXMLLoader(getClass().getResource("/fxml/investments.fxml"));
            investmentsView = invLoader.load();
            investmentController = invLoader.getController();

            if (atmView != null) {
                contentArea.getChildren().add(atmView);
                atmView.setVisible(false);
            }
            if (investmentsView != null) {
                contentArea.getChildren().add(investmentsView);
                investmentsView.setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Setup Ledger Table
        if (ledgerTable != null) {
            ledgDateCol.setCellValueFactory(d -> d.getValue().dateProperty());
            ledgTypeCol.setCellValueFactory(d -> d.getValue().txTypeProperty());
            ledgCatCol.setCellValueFactory(d -> d.getValue().typeProperty());
            ledgItemCol.setCellValueFactory(d -> d.getValue().categoryProperty());
            ledgSenderCol.setCellValueFactory(d -> d.getValue().senderReceiverProperty());
            ledgAmountCol.setCellValueFactory(d -> d.getValue().actualAmountProperty().asObject());

            ledgAmountCol.setCellFactory(tc -> new TableCell<>() {
                @Override protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) setText(null);
                    else setText(currencyFormat.format(item));
                }
            });

            if (detailCatCombo != null) {
                detailCatCombo.getItems().addAll("Salary", "Rent", "Groceries", "Dining Out", "Utilities", "Internet", "Transportation", "Healthcare", "Entertainment", "Shopping", "Office Supplies", "Investment", "Other");
            }
            ledgerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectedLedgerEntry = newVal;
                    if (detailIdLabel != null) detailIdLabel.setText("TX-" + newVal.getId() + "BF");
                    if (detailDateLabel != null) detailDateLabel.setText(newVal.getDate() != null ? newVal.getDate() : "2026-05-18, 13:30");
                    if (detailTypeLabel != null) detailTypeLabel.setText(newVal.getType() != null ? newVal.getType() : "Expense");
                    if (detailCatCombo != null) detailCatCombo.setValue(newVal.getCategory());
                }
            });
        }

        // Load data
        allTransactions = FXCollections.observableArrayList(item -> new javafx.beans.Observable[] {
                item.categoryProperty(), item.budgetedAmountProperty(), item.actualAmountProperty(), item.paidProperty()
        });

        incomeList = new FilteredList<>(allTransactions, createTypePredicate("Income"));
        expenseList = new FilteredList<>(allTransactions, createTypePredicate("Expense"));
        billList = new FilteredList<>(allTransactions, createTypePredicate("Bill"));
        savingsList = new FilteredList<>(allTransactions, createTypePredicate("Savings"));
        debtList = new FilteredList<>(allTransactions, createTypePredicate("Debt"));

        setupTable(incomeTable, incCatCol, incBudgetCol, incActualCol, incActionCol, incomeList, "Income");
        setupTable(expenseTable, expCatCol, expBudgetCol, expActualCol, expActionCol, expenseList, "Expense");
        setupTable(savingsTable, savCatCol, savBudgetCol, savActualCol, savActionCol, savingsList, "Savings");
        setupTable(debtTable, debtCatCol, debtBudgetCol, debtActualCol, debtActionCol, debtList, "Debt");
        
        setupTable(billTable, billCatCol, billBudgetCol, billActualCol, billActionCol, billList, "Bill");
        billPaidCol.setCellValueFactory(d -> d.getValue().paidProperty());
        billPaidCol.setCellFactory(javafx.scene.control.cell.CheckBoxTableCell.forTableColumn(billPaidCol));
        
        setupRecurringColumn(incRecCol, "Income");
        setupRecurringColumn(expRecCol, "Expense");
        setupRecurringColumn(billRecCol, "Bill");
        setupRecurringColumn(savRecCol, "Savings");
        setupRecurringColumn(debtRecCol, "Debt");

        double initialIncome = UserDAO.getMonthlyIncome(UserSession.getUserId());
        monthlyIncomeField.setText(String.format("%.2f", initialIncome));
        monthlyIncomeField.textProperty().addListener((obs, old, newVal) -> {
            try {
                double val = Double.parseDouble(newVal.replaceAll("[^\\d.]", ""));
                UserDAO.updateMonthlyIncome(UserSession.getUserId(), val);
                requestChartUpdate();
            } catch (Exception ignore) {}
        });

        chartDebouncer.setOnFinished(e -> updateAllChartsAndTotals());
        
        refreshAll();
        showBudgetView(); // Default view

        mainPane.setOpacity(0);
        javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(Duration.millis(800), mainPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    public void refreshAll() {
        loadCurrentView();
        refreshGoals();
        refreshLedger();
        if (atmController != null) atmController.refreshBalance();
        if (investmentController != null) investmentController.loadInvestments();
    }

    @FXML private void showAtmView() { switchView(atmView, "ATM & Banking", navAtmBtn); if (atmController != null) atmController.refreshBalance(); }
    @FXML private void showBudgetView() { switchView(budgetView, "Budgeting & Tracking", navBudgetBtn); }
    @FXML private void showSavingsView() { switchView(savingsView, "Savings Goals", navSavingsBtn); refreshGoals(); }
    @FXML private void showInvestmentsView() { switchView(investmentsView, "Investment Simulator", navInvestmentsBtn); if (investmentController != null) investmentController.loadInvestments(); }
    @FXML private void showLedgerView() { switchView(ledgerView, "Transaction Ledger", navLedgerBtn); refreshLedger(); }

    private void switchView(javafx.scene.Node view, String title, Button activeBtn) {
        if (view == null) return;
        for (javafx.scene.Node n : contentArea.getChildren()) {
            n.setVisible(false);
        }
        view.setVisible(true);
        if (viewTitleLabel != null) viewTitleLabel.setText(title);

        Button[] btns = {navAtmBtn, navBudgetBtn, navSavingsBtn, navInvestmentsBtn, navLedgerBtn};
        for (Button b : btns) {
            if (b != null) {
                b.getStyleClass().removeAll("nav-btn-active");
                if (!b.getStyleClass().contains("nav-btn")) b.getStyleClass().add("nav-btn");
            }
        }
        if (activeBtn != null && !activeBtn.getStyleClass().contains("nav-btn-active")) {
            activeBtn.getStyleClass().add("nav-btn-active");
        }

        if (darkModeBox != null && darkModeBox.isSelected() && view instanceof javafx.scene.Parent) {
            String darkCss = getClass().getResource("/css/dark-theme.css").toExternalForm();
            javafx.scene.Parent p = (javafx.scene.Parent) view;
            if (!p.getStylesheets().contains(darkCss)) p.getStylesheets().add(darkCss);
        }
    }

    @FXML
    private void handleToggleMenu() {
        isSidebarCollapsed = !isSidebarCollapsed;
        if (isSidebarCollapsed) {
            if (leftSidebar != null) leftSidebar.setPrefWidth(88);
            if (brandTitle != null) { brandTitle.setVisible(false); brandTitle.setManaged(false); }
            if (lblAtm != null) { lblAtm.setVisible(false); lblAtm.setManaged(false); }
            if (lblBudget != null) { lblBudget.setVisible(false); lblBudget.setManaged(false); }
            if (lblSavings != null) { lblSavings.setVisible(false); lblSavings.setManaged(false); }
            if (lblInvestments != null) { lblInvestments.setVisible(false); lblInvestments.setManaged(false); }
            if (lblLedger != null) { lblLedger.setVisible(false); lblLedger.setManaged(false); }
            if (lblLogout != null) { lblLogout.setVisible(false); lblLogout.setManaged(false); }
            if (lblTheme != null) { lblTheme.setVisible(false); lblTheme.setManaged(false); }
        } else {
            if (leftSidebar != null) leftSidebar.setPrefWidth(270);
            if (brandTitle != null) { brandTitle.setVisible(true); brandTitle.setManaged(true); }
            if (lblAtm != null) { lblAtm.setVisible(true); lblAtm.setManaged(true); }
            if (lblBudget != null) { lblBudget.setVisible(true); lblBudget.setManaged(true); }
            if (lblSavings != null) { lblSavings.setVisible(true); lblSavings.setManaged(true); }
            if (lblInvestments != null) { lblInvestments.setVisible(true); lblInvestments.setManaged(true); }
            if (lblLedger != null) { lblLedger.setVisible(true); lblLedger.setManaged(true); }
            if (lblLogout != null) { lblLogout.setVisible(true); lblLogout.setManaged(true); }
            if (lblTheme != null) { lblTheme.setVisible(true); lblTheme.setManaged(true); }
        }
    }

    public void refreshLedger() {
        if (ledgerTable != null) {
            List<TransactionEntry> list = TransactionDAO.loadBankingLedger(UserSession.getUserId());
            ledgerTable.setItems(FXCollections.observableArrayList(list));
        }
    }

    @FXML
    private void handleSaveTransactionDetails() {
        if (selectedLedgerEntry != null && detailCatCombo != null && detailCatCombo.getValue() != null) {
            selectedLedgerEntry.setCategory(detailCatCombo.getValue());
            TransactionDAO.updateTransaction(UserSession.getUserId(), selectedLedgerEntry);
            refreshLedger();
            NotificationService.show((Stage) mainPane.getScene().getWindow(), "Transaction details updated successfully", NotificationService.NotificationType.SUCCESS);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a transaction from the table first.", ButtonType.OK); alert.show();
        }
    }

    @FXML
    private void handleLedgerTab(javafx.scene.input.MouseEvent event) {
        if (event.getSource() instanceof Label) {
            Label clicked = (Label) event.getSource();
            Label[] tabs = {tabAll, tabIncome, tabExpenses, tabBills, tabSavings, tabDebt};
            boolean dark = darkModeBox != null && darkModeBox.isSelected();
            String activeClass = "tab-lbl-active";
            String inactiveClass = dark ? "tab-lbl-dark" : "tab-lbl";
            for (Label t : tabs) {
                if (t != null) {
                    t.getStyleClass().removeAll("tab-lbl-active", "tab-lbl", "tab-lbl-dark", "tab-lbl-dark-active");
                    t.getStyleClass().add(inactiveClass);
                }
            }
            clicked.getStyleClass().removeAll("tab-lbl", "tab-lbl-dark");
            clicked.getStyleClass().add(activeClass);

            if (ledgerTable != null) {
                List<TransactionEntry> list = TransactionDAO.loadBankingLedger(UserSession.getUserId());
                String tabName = clicked.getText();
                List<TransactionEntry> filtered;
                if ("Income".equalsIgnoreCase(tabName)) {
                    filtered = list.stream().filter(t -> "Income".equalsIgnoreCase(t.getType()) || "Salary".equalsIgnoreCase(t.getCategory()) || "Deposit".equalsIgnoreCase(t.getTxType())).collect(java.util.stream.Collectors.toList());
                } else if ("Expenses".equalsIgnoreCase(tabName)) {
                    filtered = list.stream().filter(t -> "Expense".equalsIgnoreCase(t.getType()) || "Expense".equalsIgnoreCase(t.getTxType())).collect(java.util.stream.Collectors.toList());
                } else if ("Bills".equalsIgnoreCase(tabName)) {
                    filtered = list.stream().filter(t -> "Bill".equalsIgnoreCase(t.getType()) || "Bills".equalsIgnoreCase(t.getCategory()) || "Utilities".equalsIgnoreCase(t.getCategory()) || "Rent".equalsIgnoreCase(t.getCategory())).collect(java.util.stream.Collectors.toList());
                } else if ("Savings".equalsIgnoreCase(tabName)) {
                    filtered = list.stream().filter(t -> "Savings".equalsIgnoreCase(t.getType()) || "Savings".equalsIgnoreCase(t.getCategory())).collect(java.util.stream.Collectors.toList());
                } else if ("Debt".equalsIgnoreCase(tabName)) {
                    filtered = list.stream().filter(t -> "Debt".equalsIgnoreCase(t.getType()) || "Debt".equalsIgnoreCase(t.getCategory())).collect(java.util.stream.Collectors.toList());
                } else {
                    filtered = list;
                }
                ledgerTable.setItems(FXCollections.observableArrayList(filtered));
                if (ledgerShowingLabel != null) {
                    ledgerShowingLabel.setText("Showing 1-" + filtered.size() + " of " + list.size() + " transactions");
                }
            }
        }
    }

    private boolean isLedgerDateAscending = false;
    @FXML
    private void handleLedgerFilterDate() {
        if (ledgerTable != null && ledgerTable.getItems() != null) {
            isLedgerDateAscending = !isLedgerDateAscending;
            java.util.Comparator<TransactionEntry> comp = java.util.Comparator.comparing(TransactionEntry::getDate, java.util.Comparator.nullsLast(String::compareTo));
            if (!isLedgerDateAscending) comp = comp.reversed();
            FXCollections.sort(ledgerTable.getItems(), comp);
            NotificationService.show((Stage) mainPane.getScene().getWindow(), "Sorted ledger by Date (" + (isLedgerDateAscending ? "Ascending" : "Descending") + ")", NotificationService.NotificationType.INFO);
        }
    }

    @FXML
    private void handleLedgerFilterType() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("All", "Deposit", "Withdrawal", "Transfer", "All");
        dialog.setTitle("Filter by Type");
        dialog.setHeaderText("Select Transaction Action Type");
        dialog.showAndWait().ifPresent(choice -> {
            if (ledgerTable != null) {
                List<TransactionEntry> list = TransactionDAO.loadBankingLedger(UserSession.getUserId());
                if (!"All".equalsIgnoreCase(choice)) {
                    list = list.stream().filter(t -> choice.equalsIgnoreCase(t.getTxType())).collect(java.util.stream.Collectors.toList());
                }
                ledgerTable.setItems(FXCollections.observableArrayList(list));
                if (ledgerShowingLabel != null) ledgerShowingLabel.setText("Showing 1-" + list.size() + " of matching transactions");
                NotificationService.show((Stage) mainPane.getScene().getWindow(), "Filtered ledger by Type: " + choice, NotificationService.NotificationType.INFO);
            }
        });
    }

    @FXML
    private void handleLedgerFilterCategory() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("All", "Salary", "Rent", "Groceries", "Dining Out", "Utilities", "Investment", "Other", "All");
        dialog.setTitle("Filter by Category");
        dialog.setHeaderText("Select Transaction Category");
        dialog.showAndWait().ifPresent(choice -> {
            if (ledgerTable != null) {
                List<TransactionEntry> list = TransactionDAO.loadBankingLedger(UserSession.getUserId());
                if (!"All".equalsIgnoreCase(choice)) {
                    list = list.stream().filter(t -> choice.equalsIgnoreCase(t.getCategory())).collect(java.util.stream.Collectors.toList());
                }
                ledgerTable.setItems(FXCollections.observableArrayList(list));
                if (ledgerShowingLabel != null) ledgerShowingLabel.setText("Showing 1-" + list.size() + " of matching transactions");
                NotificationService.show((Stage) mainPane.getScene().getWindow(), "Filtered ledger by Category: " + choice, NotificationService.NotificationType.INFO);
            }
        });
    }

    @FXML
    private void handleLedgerFilterSender() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Filter by Sender/Receiver");
        dialog.setHeaderText("Enter Sender or Receiver Name:");
        dialog.showAndWait().ifPresent(name -> {
            if (ledgerTable != null && !name.trim().isEmpty()) {
                List<TransactionEntry> list = TransactionDAO.loadBankingLedger(UserSession.getUserId());
                list = list.stream().filter(t -> t.getSenderReceiver() != null && t.getSenderReceiver().toLowerCase().contains(name.trim().toLowerCase())).collect(java.util.stream.Collectors.toList());
                ledgerTable.setItems(FXCollections.observableArrayList(list));
                if (ledgerShowingLabel != null) ledgerShowingLabel.setText("Showing 1-" + list.size() + " of matching transactions");
                NotificationService.show((Stage) mainPane.getScene().getWindow(), "Filtered by Sender/Receiver: " + name, NotificationService.NotificationType.INFO);
            }
        });
    }

    @FXML
    private void handleLedgerMoreFilters() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Advanced Filters");
        alert.setHeaderText("Advanced Ledger Filtering");
        alert.setContentText("Status: Active\nMin Amount: $0.00\nMax Amount: $100,000.00\nExportable: True\n\n(Advanced filters active - showing all matching records)");
        alert.show();
    }

    @FXML
    private void handleLedgerEditBill() {
        if (selectedLedgerEntry != null) {
            TextInputDialog dialog = new TextInputDialog(selectedLedgerEntry.getCategory() != null ? selectedLedgerEntry.getCategory() : "Bill");
            dialog.setTitle("Edit Bill");
            dialog.setHeaderText("Update Bill Description / Category:");
            dialog.showAndWait().ifPresent(desc -> {
                selectedLedgerEntry.setCategory(desc);
                TransactionDAO.updateTransaction(UserSession.getUserId(), selectedLedgerEntry);
                refreshLedger();
                NotificationService.show((Stage) mainPane.getScene().getWindow(), "Bill updated successfully", NotificationService.NotificationType.SUCCESS);
            });
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a transaction from the table first.", ButtonType.OK);
            alert.show();
        }
    }

    @FXML
    private void handleLedgerSeeStatuses() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Transaction Status History");
        alert.setHeaderText("Audit Trail & Statuses");
        alert.setContentText("1. Created by System Admin (20 Jun 2020, 13:30)\n2. Pending Manager Approval (21 Jun 2020, 09:00)\n3. Waiting for Final Authorization (Current)");
        alert.show();
    }

    private void setupCurrencySelector() {
        currencySelector.getItems().addAll("USD", "EUR", "GBP", "JPY");
        currencySelector.setValue("USD");
        currencySelector.setOnAction(e -> {
            String choice = currencySelector.getValue();
            switch (choice) {
                case "EUR": currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY); break;
                case "GBP": currencyFormat = NumberFormat.getCurrencyInstance(Locale.UK); break;
                case "JPY": currencyFormat = NumberFormat.getCurrencyInstance(Locale.JAPAN); break;
                default: currencyFormat = NumberFormat.getCurrencyInstance(Locale.US); break;
            }
            updateAllChartsAndTotals();
            incomeTable.refresh();
            expenseTable.refresh();
            billTable.refresh();
            savingsTable.refresh();
            debtTable.refresh();
            if (ledgerTable != null) ledgerTable.refresh();
        });
    }

    private void setupMonthSelector() {
        monthSelector.getItems().add("All Time");
        monthSelector.getItems().addAll(TransactionDAO.getAvailableMonths(UserSession.getUserId()));
        monthSelector.getSelectionModel().selectFirst();
        monthSelector.setOnAction(e -> loadCurrentView());
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((obs, old, newVal) -> {
            updateFilters();
        });
    }

    private void updateFilters() {
        String searchText = searchField.getText().toLowerCase();
        Predicate<TransactionEntry> searchPred = entry -> 
                entry.getCategory().toLowerCase().contains(searchText) || entry.isPlaceholder();
        
        incomeList.setPredicate(createTypePredicate("Income").and(searchPred));
        expenseList.setPredicate(createTypePredicate("Expense").and(searchPred));
        billList.setPredicate(createTypePredicate("Bill").and(searchPred));
        savingsList.setPredicate(createTypePredicate("Savings").and(searchPred));
        debtList.setPredicate(createTypePredicate("Debt").and(searchPred));
        
        requestChartUpdate();
    }

    private Predicate<TransactionEntry> createTypePredicate(String type) {
        return entry -> entry.getType().equals(type);
    }

    private void loadCurrentView() {
        String selectedMonth = monthSelector.getValue();
        List<TransactionEntry> data = TransactionDAO.loadTransactions(UserSession.getUserId(), selectedMonth);
        allTransactions.setAll(data);
        
        ensureMinimumRows("Income", 6);
        ensureMinimumRows("Expense", 6);
        ensureMinimumRows("Bill", 6);
        ensureMinimumRows("Savings", 6);
        ensureMinimumRows("Debt", 6);
        
        updateAllChartsAndTotals();
    }

    private void ensureMinimumRows(String type, int min) {
        long totalCount = allTransactions.stream().filter(e -> e.getType().equals(type)).count();
        long placeholderCount = allTransactions.stream().filter(e -> e.getType().equals(type) && e.isPlaceholder()).count();
        
        if (placeholderCount == 0) {
            allTransactions.add(TransactionEntry.createPlaceholder(type));
        }
        while (allTransactions.stream().filter(e -> e.getType().equals(type)).count() < min) {
            allTransactions.add(TransactionEntry.createPlaceholder(type));
        }
    }

    private void setupTable(TableView<TransactionEntry> table, TableColumn<TransactionEntry, String> catCol,
                            TableColumn<TransactionEntry, Double> budgetCol, TableColumn<TransactionEntry, Double> actualCol,
                            TableColumn<TransactionEntry, Void> actionCol, FilteredList<TransactionEntry> list, String type) {
        table.setItems(list);
        
        catCol.setCellValueFactory(d -> d.getValue().categoryProperty());
        catCol.setCellFactory(column -> new CommitTextFieldTableCell<>(new StringConverter<String>() {
            @Override public String toString(String object) { return object; }
            @Override public String fromString(String string) { return string; }
        }));
        catCol.setOnEditCommit(e -> {
            e.getRowValue().setCategory(e.getNewValue());
            handleCommit(e.getRowValue(), type);
        });

        budgetCol.setCellValueFactory(d -> d.getValue().budgetedAmountProperty().asObject());
        budgetCol.setCellFactory(column -> new CommitTextFieldTableCell<>(new CurrencyConverter()));
        budgetCol.setOnEditCommit(e -> {
            e.getRowValue().setBudgetedAmount(e.getNewValue());
            handleCommit(e.getRowValue(), type);
        });

        actualCol.setCellValueFactory(d -> d.getValue().actualAmountProperty().asObject());
        actualCol.setCellFactory(column -> new VarianceTableCell<>(new CurrencyConverter()));
        actualCol.setOnEditCommit(e -> {
            e.getRowValue().setActualAmount(e.getNewValue());
            handleCommit(e.getRowValue(), type);
        });

        setupActionColumn(actionCol, type);
    }

    private void setupActionColumn(TableColumn<TransactionEntry, Void> col, String type) {
        col.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button();
            {
                btn.getStyleClass().add("btn-icon");
                btn.setOnAction(e -> {
                    TransactionEntry entry = getTableView().getItems().get(getIndex());
                    if (entry.isPlaceholder()) {
                        if (entry.getCategory().isEmpty()) entry.setCategory("New Item");
                        entry.setPlaceholder(false);
                        TransactionDAO.insertTransaction(UserSession.getUserId(), entry, monthSelector.getValue());
                        allTransactions.add(TransactionEntry.createPlaceholder(type));
                    } else {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete '" + entry.getCategory() + "'?", ButtonType.YES, ButtonType.NO);
                        confirm.setTitle("Confirm Deletion");
                        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                            TransactionDAO.deleteTransaction(UserSession.getUserId(), entry.getId());
                            allTransactions.remove(entry);
                        }
                    }
                    requestChartUpdate();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    TransactionEntry entry = getTableView().getItems().get(getIndex());
                    btn.setText(entry.isPlaceholder() ? "+" : "🗑");
                    btn.setStyle(entry.isPlaceholder() ? "-fx-text-fill: #3e6560;" : "-fx-text-fill: #ba1a1a;");
                    setGraphic(btn);
                }
            }
        });
    }

    private void handleCommit(TransactionEntry entry, String type) {
        if (entry.isPlaceholder()) {
            entry.setPlaceholder(false);
            TransactionDAO.insertTransaction(UserSession.getUserId(), entry, monthSelector.getValue());
            ensureMinimumRows(type, 6);
        } else {
            TransactionDAO.updateTransaction(UserSession.getUserId(), entry);
        }
        requestChartUpdate();
    }

    private void setupRecurringColumn(TableColumn<TransactionEntry, Boolean> col, String type) {
        col.setCellValueFactory(d -> d.getValue().recurringProperty());
        col.setCellFactory(javafx.scene.control.cell.CheckBoxTableCell.forTableColumn(col));
        col.setOnEditCommit(e -> handleCommit(e.getRowValue(), type));
    }

    @FXML
    private void handleSyncRecurring() {
        String selectedMonth = monthSelector.getValue();
        if (selectedMonth == null || selectedMonth.equalsIgnoreCase("All Time")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Please select a specific month to sync recurring items into.");
            alert.show();
            return;
        }
        
        TransactionDAO.copyRecurringTransactions(UserSession.getUserId(), selectedMonth);
        loadCurrentView();
        
        NotificationService.show((Stage) mainPane.getScene().getWindow(), 
                "Recurring transactions synchronized for " + selectedMonth, 
                NotificationService.NotificationType.SUCCESS);
    }

    private void requestChartUpdate() {
        chartDebouncer.playFromStart();
    }

    private void updateAllChartsAndTotals() {
        updateLabels(incomeList, incTotalBudgetLabel, incTotalActualLabel, incProgressBar);
        updateLabels(expenseList, expTotalBudgetLabel, expTotalActualLabel, expProgressBar);
        updateLabels(billList, billTotalBudgetLabel, billTotalActualLabel, billProgressBar);
        updateLabels(savingsList, savTotalBudgetLabel, savTotalActualLabel, savProgressBar);
        updateLabels(debtList, debtTotalBudgetLabel, debtTotalActualLabel, debtProgressBar);

        double expA = budgetService.calculateTotalActual(expenseList);
        double billA = budgetService.calculateTotalActual(billList);
        double savA = budgetService.calculateTotalActual(savingsList);
        double debtA = budgetService.calculateTotalActual(debtList);

        double totalOutflow = budgetService.calculateTotalOutflow(expenseList, billList, debtList);
        double monthlyIncome = AtmController.calculateAtmBalance(UserSession.getUserId());
        double left = budgetService.calculateLeftToSpend(monthlyIncome, totalOutflow);

        amountLeftLabel.setText(currencyFormat.format(monthlyIncome - totalOutflow));
        
        budgetPieChart.getData().setAll(
            new PieChart.Data("Spent", totalOutflow),
            new PieChart.Data("Remaining", left)
        );
        addTooltips(budgetPieChart);

        allocationPieChart.getData().clear();
        if (expA > 0) allocationPieChart.getData().add(new PieChart.Data("Expenses", expA));
        if (billA > 0) allocationPieChart.getData().add(new PieChart.Data("Bills", billA));
        if (savA > 0) allocationPieChart.getData().add(new PieChart.Data("Savings", savA));
        if (debtA > 0) allocationPieChart.getData().add(new PieChart.Data("Debt", debtA));
        if (allocationPieChart.getData().isEmpty()) allocationPieChart.getData().add(new PieChart.Data("No Data", 1));
        addTooltips(allocationPieChart);

        statTotalIncome.setText(currencyFormat.format(monthlyIncome));
        if (!monthlyIncomeField.isFocused()) {
            monthlyIncomeField.setText(String.format("%.2f", UserDAO.getMonthlyIncome(UserSession.getUserId())));
        }
        
        double dailyBurn = totalOutflow / 30.0;
        statBurnRate.setText(currencyFormat.format(dailyBurn) + " / day");
        if (dailyBurn > (monthlyIncome / 30.0) * 0.9) {
            statBurnRateSub.setText("High Spend");
            statBurnRateSub.getStyleClass().add("bento-stat-sub-neg");
        } else {
            statBurnRateSub.setText("Healthy");
            statBurnRateSub.getStyleClass().removeAll("bento-stat-sub-neg");
        }

        double savingsRate = monthlyIncome > 0 ? (savA / monthlyIncome) * 100 : 0;
        statSavingsRate.setText(String.format("%.1f%%", savingsRate));
    }

    private void updateLabels(List<TransactionEntry> list, Label bLabel, Label aLabel, ProgressBar pBar) {
        double b = budgetService.calculateTotalBudget(list);
        double a = budgetService.calculateTotalActual(list);
        bLabel.setText(currencyFormat.format(b));
        aLabel.setText(currencyFormat.format(a));

        if (b > 0) {
            double progress = a / b;
            pBar.setProgress(Math.min(progress, 1.0));
            pBar.getStyleClass().removeAll("progress-warning", "progress-over");
            if (progress > 1.0) pBar.getStyleClass().add("progress-over");
            else if (progress > 0.8) pBar.getStyleClass().add("progress-warning");
        } else {
            pBar.setProgress(0);
        }
    }

    private void addTooltips(PieChart chart) {
        for (PieChart.Data data : chart.getData()) {
            Tooltip t = new Tooltip(String.format("%s: %s", data.getName(), currencyFormat.format(data.getPieValue())));
            Tooltip.install(data.getNode(), t);
        }
    }

    @FXML
    private void handleThemeToggle() {
        boolean dark = darkModeBox.isSelected();
        String darkCss = getClass().getResource("/css/dark-theme.css").toExternalForm();
        Scene scene = mainPane.getScene();
        if (dark) {
            if (!scene.getStylesheets().contains(darkCss)) scene.getStylesheets().add(darkCss);
            if (!mainPane.getStylesheets().contains(darkCss)) mainPane.getStylesheets().add(darkCss);
            if (contentArea != null) {
                for (javafx.scene.Node child : contentArea.getChildren()) {
                    if (child instanceof javafx.scene.Parent) {
                        javafx.scene.Parent p = (javafx.scene.Parent) child;
                        if (!p.getStylesheets().contains(darkCss)) p.getStylesheets().add(darkCss);
                    }
                }
            }
        } else {
            scene.getStylesheets().removeIf(s -> s.contains("dark-theme.css"));
            mainPane.getStylesheets().removeIf(s -> s.contains("dark-theme.css"));
            if (contentArea != null) {
                for (javafx.scene.Node child : contentArea.getChildren()) {
                    if (child instanceof javafx.scene.Parent) {
                        ((javafx.scene.Parent) child).getStylesheets().removeIf(s -> s.contains("dark-theme.css"));
                    }
                }
            }
        }
    }

    @FXML
    private void handleExportCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Budget CSV");
        chooser.setInitialFileName("budget_export.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showSaveDialog(mainPane.getScene().getWindow());
        
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Type,Category,Planned,Actual,Paid");
                for (TransactionEntry e : allTransactions) {
                    if (!e.isPlaceholder()) {
                        writer.printf("%s,%s,%.2f,%.2f,%s%n", 
                                e.getType(), e.getCategory(), e.getBudgetedAmount(), e.getActualAmount(), e.isPaid());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void refreshGoals() {
        goalsContainer.getChildren().clear();
        if (fullGoalsContainer != null) fullGoalsContainer.getChildren().clear();

        List<com.budgetfit.model.Goal> goals = com.budgetfit.dao.GoalDAO.getGoals(UserSession.getUserId());

        double totalSavings = 0;
        double totalTarget = 0;

        for (com.budgetfit.model.Goal goal : goals) {
            totalSavings += goal.getCurrentAmount();
            totalTarget += goal.getTargetAmount();

            // Sidebar Goal Box
            VBox box = new VBox(5);
            HBox top = new HBox(10);
            top.setAlignment(Pos.CENTER_LEFT);
            Label name = new Label(goal.getName());
            name.setStyle("-fx-font-weight: 700; -fx-text-fill: #414847;");
            Region spacer = new Region();
            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            Button contributeBtn = new Button("+");
            contributeBtn.getStyleClass().add("btn-icon");
            contributeBtn.setStyle("-fx-text-fill: #3e6560; -fx-font-weight: bold;");
            contributeBtn.setOnAction(e -> handleContribute(goal));

            Label amount = new Label(currencyFormat.format(goal.getCurrentAmount()) + " / " + currencyFormat.format(goal.getTargetAmount()));
            amount.setStyle("-fx-font-size: 11px; -fx-text-fill: #717977;");
            top.getChildren().addAll(name, contributeBtn, spacer, amount);

            ProgressBar pb = new ProgressBar(goal.getProgress());
            pb.setPrefWidth(Double.MAX_VALUE);
            pb.getStyleClass().add("budget-progress");
            if (goal.isAheadOfPace()) {
                pb.getStyleClass().add("progress-safe");
            } else {
                pb.getStyleClass().add("progress-over");
            }

            box.getChildren().addAll(top, pb);
            goalsContainer.getChildren().add(box);

            // Full View Goal Card (Wealth in Motion / Finora Specification)
            if (fullGoalsContainer != null) {
                VBox fullCard = new VBox(14);
                fullCard.getStyleClass().add("bento-tile");
                fullCard.setPrefWidth(350);

                // Top line: Accent indicator E.g. | Emergency Fund, options menu on right
                HBox fTop = new HBox(8); fTop.setAlignment(Pos.CENTER_LEFT);
                Label fInd = new Label("|"); fInd.setStyle("-fx-font-weight: 900; -fx-text-fill: -bf-amber-400;");
                Label fName = new Label(goal.getName()); fName.getStyleClass().add("card-title");
                Region fSpacer = new Region(); HBox.setHgrow(fSpacer, Priority.ALWAYS);
                Label fOpt = new Label("⋮"); fOpt.setStyle("-fx-text-fill: -bf-text-tertiary; -fx-font-weight: bold;");
                fTop.getChildren().addAll(fInd, fName, fSpacer, fOpt);

                // Middle line: Progress label on left, percentage on right
                HBox fMid = new HBox(); fMid.setAlignment(Pos.CENTER_LEFT);
                Label pLbl = new Label("Progress"); pLbl.getStyleClass().add("card-subtitle");
                Region pSpc = new Region(); HBox.setHgrow(pSpc, Priority.ALWAYS);
                Label pVal = new Label(String.format("%.0f%%", goal.getProgress() * 100));
                pVal.setStyle("-fx-font-weight: 700; -fx-text-fill: " + (goal.isAheadOfPace() ? "-bf-green" : "-bf-red") + ";");
                fMid.getChildren().addAll(pLbl, pSpc, pVal);

                // Progress Bar
                ProgressBar fPb = new ProgressBar(goal.getProgress()); fPb.setPrefWidth(Double.MAX_VALUE); fPb.setPrefHeight(12); fPb.getStyleClass().add("budget-progress");
                if (goal.isAheadOfPace()) {
                    fPb.getStyleClass().add("progress-safe");
                } else {
                    fPb.getStyleClass().add("progress-over");
                }

                // Bottom line: Current amount on left, Target amount on right
                HBox fBot = new HBox(); fBot.setAlignment(Pos.CENTER_LEFT);
                Label cAmt = new Label(currencyFormat.format(goal.getCurrentAmount())); cAmt.getStyleClass().addAll("card-subtitle", "mono"); cAmt.setStyle("-fx-font-weight: 700; -fx-text-fill: -bf-text-primary;");
                Region bSpc = new Region(); HBox.setHgrow(bSpc, Priority.ALWAYS);
                Label tAmt = new Label(currencyFormat.format(goal.getTargetAmount())); tAmt.getStyleClass().addAll("card-subtitle", "mono");
                fBot.getChildren().addAll(cAmt, bSpc, tAmt);

                Label paceLabel = new Label(goal.getPaceStatus());
                paceLabel.getStyleClass().add("card-subtitle");
                if (!goal.isAheadOfPace()) {
                    paceLabel.setStyle("-fx-text-fill: -bf-red; -fx-font-weight: 500;");
                } else {
                    paceLabel.setStyle("-fx-text-fill: -bf-green; -fx-font-weight: 700;");
                }

                Button fBtn = new Button("+ Contribute"); fBtn.getStyleClass().add("btn-outline"); fBtn.setPrefWidth(Double.MAX_VALUE);
                fBtn.setOnAction(e -> handleContribute(goal));

                fullCard.getChildren().addAll(fTop, fMid, fPb, fBot, paceLabel, fBtn);
                fullGoalsContainer.getChildren().add(fullCard);
            }
        }

        // Populate Hero Banner
        if (savHeroTotalLabel != null) savHeroTotalLabel.setText(currencyFormat.format(totalSavings));
        if (savHeroTargetLabel != null) savHeroTargetLabel.setText("of " + currencyFormat.format(totalTarget) + " target");
        if (savHeroProgressBar != null) {
            double p = totalTarget > 0 ? totalSavings / totalTarget : 0;
            savHeroProgressBar.setProgress(p > 1.0 ? 1.0 : p);
            savHeroProgressBar.getStyleClass().remove("progress-safe");
            savHeroProgressBar.getStyleClass().remove("progress-over");
            savHeroProgressBar.getStyleClass().add(p >= 0.5 ? "progress-safe" : "progress-over");
        }

        // Populate Growth Chart
        if (savGrowthChart != null) {
            savGrowthChart.getData().clear();
            javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
            series.setName("Monthly Growth");
            series.getData().add(new javafx.scene.chart.XYChart.Data<>("Jan", totalSavings * 0.70));
            series.getData().add(new javafx.scene.chart.XYChart.Data<>("Feb", totalSavings * 0.75));
            series.getData().add(new javafx.scene.chart.XYChart.Data<>("Mar", totalSavings * 0.82));
            series.getData().add(new javafx.scene.chart.XYChart.Data<>("Apr", totalSavings * 0.90));
            series.getData().add(new javafx.scene.chart.XYChart.Data<>("May", totalSavings));
            savGrowthChart.getData().add(series);
        }

        // Populate Recent Activity Feed
        if (savActivityContainer != null) {
            savActivityContainer.getChildren().clear();
            for (com.budgetfit.model.Goal g : goals) {
                if (g.getCurrentAmount() > 0) {
                    HBox row = new HBox();
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.getStyleClass().add("activity-row");
                    VBox left = new VBox(2);
                    Label gName = new Label(g.getName()); gName.getStyleClass().add("card-subtitle"); gName.setStyle("-fx-font-weight: 700; -fx-text-fill: -bf-text-primary;");
                    Label gDate = new Label("Recent Contribution"); gDate.getStyleClass().add("card-subtitle"); gDate.setStyle("-fx-font-size: 10px;");
                    left.getChildren().addAll(gName, gDate);
                    Region spc = new Region(); HBox.setHgrow(spc, Priority.ALWAYS);
                    Label amt = new Label("+" + currencyFormat.format(g.getCurrentAmount() * 0.1)); amt.getStyleClass().addAll("card-subtitle", "mono"); amt.setStyle("-fx-font-weight: 700; -fx-text-fill: -bf-green;");
                    row.getChildren().addAll(left, spc, amt);
                    savActivityContainer.getChildren().add(row);
                }
            }
        }

        Button addBtn = new Button("+ Add New Goal");
        addBtn.getStyleClass().add("btn-logout");
        addBtn.setStyle("-fx-font-size: 11px;");
        addBtn.setPrefWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> handleAddGoal());
        goalsContainer.getChildren().add(addBtn);

        if (fullGoalsContainer != null) {
            Button fAddBtn = new Button("+ Create New Savings Goal"); fAddBtn.getStyleClass().add("btn-primary");
            fAddBtn.setOnAction(e -> handleAddGoal());
            fullGoalsContainer.getChildren().add(fAddBtn);
        }
    }

    private void handleContribute(com.budgetfit.model.Goal goal) {
        TextInputDialog dialog = new TextInputDialog("50.00");
        dialog.setTitle("Contribute to Goal");
        dialog.setHeaderText("Contribution for " + goal.getName());
        dialog.setContentText("Amount:");

        dialog.showAndWait().ifPresent(result -> {
            try {
                double val = Double.parseDouble(result.replaceAll("[^\\d.]", ""));
                goal.setCurrentAmount(goal.getCurrentAmount() + val);
                com.budgetfit.dao.GoalDAO.updateGoal(UserSession.getUserId(), goal);

                TransactionEntry entry = new TransactionEntry(-1, "Savings", goal.getName(), val, val, true, false, false);
                TransactionDAO.insertTransaction(UserSession.getUserId(), entry, monthSelector.getValue());

                refreshAll();

                NotificationService.show((Stage) mainPane.getScene().getWindow(),
                        "Contribution of " + currencyFormat.format(val) + " added to " + goal.getName(),
                        NotificationService.NotificationType.SUCCESS);
            } catch (Exception ex) {
                NotificationService.show((Stage) mainPane.getScene().getWindow(), "Invalid amount", NotificationService.NotificationType.ERROR);
            }
        });
    }

    @FXML
    private void handleQuickContribute() {
        List<com.budgetfit.model.Goal> goals = com.budgetfit.dao.GoalDAO.getGoals(UserSession.getUserId());
        if (goals.isEmpty()) {
            handleAddGoal();
            return;
        }
        handleContribute(goals.get(0));
    }

    @FXML
    private void handleAddGoal() {
        TextInputDialog dialog = new TextInputDialog("Vacation:2000:2026-12");
        dialog.setTitle("New Savings Goal");
        dialog.setHeaderText("Set a target amount and date (e.g. Vacation:2000:2026-12)");
        dialog.setContentText("Format: Name:Amount:YYYY-MM");

        dialog.showAndWait().ifPresent(result -> {
            if (result.contains(":")) {
                String[] parts = result.split(":");
                try {
                    String name = parts[0].trim();
                    double target = Double.parseDouble(parts[1].trim());
                    String targetDate = parts.length > 2 ? parts[2].trim() : "2026-12";
                    com.budgetfit.dao.GoalDAO.insertGoal(UserSession.getUserId(), new com.budgetfit.model.Goal(-1, name, target, 0, targetDate));
                    refreshGoals();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Invalid format. Use Name:Amount:YYYY-MM").show();
                }
            }
        });
    }

    @FXML
    private void handleViewTrends(ActionEvent event) throws Exception {
        Stage stage = (Stage) mainPane.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/trends.fxml"));
        stage.setScene(new Scene(root, 1000, 750));
    }

    @FXML
    private void handleLogout(ActionEvent event) throws Exception {
        UserSession.clear();
        Stage stage = (Stage) mainPane.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        stage.setScene(new Scene(root, 500, 500));
    }

    private class CurrencyConverter extends StringConverter<Double> {
        @Override public String toString(Double o) { return o == null || o == 0 ? "" : String.format("%.2f", o); }
        @Override public Double fromString(String s) {
            if (s == null || s.isEmpty()) return 0.0;
            try { 
                double val = Double.parseDouble(s.replaceAll("[^\\d.]", "")); 
                if (val < 0) {
                    new Alert(Alert.AlertType.WARNING, "Amount cannot be negative.").show();
                    return 0.0;
                }
                return val;
            } catch (Exception e) { return 0.0; }
        }
    }

    private static class VarianceTableCell<S> extends CommitTextFieldTableCell<S, Double> {
        public VarianceTableCell(StringConverter<Double> converter) {
            super(converter);
        }

        @Override
        public void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);
            getStyleClass().removeAll("variance-over", "variance-safe");
            if (!empty && item != null && item > 0) {
                TransactionEntry entry = (TransactionEntry) getTableView().getItems().get(getIndex());
                if (entry != null && !entry.isPlaceholder()) {
                    double budget = entry.getBudgetedAmount();
                    if (budget > 0) {
                        if (item > budget) getStyleClass().add("variance-over");
                        else getStyleClass().add("variance-safe");
                    }
                }
            }
        }
    }

    private static class CommitTextFieldTableCell<S, T> extends TextFieldTableCell<S, T> {
        private TextField textField;

        public CommitTextFieldTableCell(StringConverter<T> converter) {
            super(converter);
        }

        @Override
        public void startEdit() {
            super.startEdit();
            if (isEditing()) {
                if (textField == null) {
                    textField = findTextField(getGraphic());
                    if (textField != null) {
                        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                            if (!newVal) {
                                commitEdit(getConverter().fromString(textField.getText()));
                            }
                        });
                    }
                }
            }
        }

        private TextField findTextField(javafx.scene.Node node) {
            if (node instanceof TextField) return (TextField) node;
            if (node instanceof Parent) {
                for (javafx.scene.Node child : ((Parent) node).getChildrenUnmodifiable()) {
                    TextField found = findTextField(child);
                    if (found != null) return found;
                }
            }
            return null;
        }
    }
}
