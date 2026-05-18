package com.budgetfit.model;

import javafx.beans.property.*;

/**
 * Domain model representing a financial transaction entry (Income, Expense, Bill, Savings, or Debt).
 * Fully backed by JavaFX properties to enable bidirectional UI data binding and live updates.
 * Expanded to support ATM banking ledger metadata (sender/receiver and txType).
 */
public class TransactionEntry {
    private final IntegerProperty id;
    private final StringProperty type; // E.g. "Income", "Expense", "Bill"
    private final SimpleStringProperty category; // Used as the specific item name / label in the UI
    private final SimpleDoubleProperty budgetedAmount;
    private final SimpleDoubleProperty actualAmount;
    private final SimpleBooleanProperty paid;
    private final SimpleBooleanProperty recurring;
    private final SimpleStringProperty senderReceiver;
    private final SimpleStringProperty txType; // E.g. "DEPOSIT", "WITHDRAWAL", "TRANSFER", "SALARY", "BUDGET"
    private final SimpleStringProperty date;
    private boolean placeholder;

    /**
     * Constructs a comprehensive transaction entry with banking metadata.
     */
    public TransactionEntry(int id, String type, String category, double budgetedAmount, double actualAmount, boolean paid, boolean recurring, String senderReceiver, String txType, String date, boolean placeholder) {
        this.id = new SimpleIntegerProperty(id);
        this.type = new SimpleStringProperty(type == null ? "Expense" : type);
        this.category = new SimpleStringProperty(category == null ? "" : category);
        this.budgetedAmount = new SimpleDoubleProperty(budgetedAmount);
        this.actualAmount = new SimpleDoubleProperty(actualAmount);
        this.paid = new SimpleBooleanProperty(paid);
        this.recurring = new SimpleBooleanProperty(recurring);
        this.senderReceiver = new SimpleStringProperty(senderReceiver == null ? "" : senderReceiver);
        this.txType = new SimpleStringProperty(txType == null ? "BUDGET" : txType);
        this.date = new SimpleStringProperty(date == null ? "" : date);
        this.placeholder = placeholder;
    }

    /**
     * Legacy constructor for budgeting compatibility.
     */
    public TransactionEntry(int id, String type, String category, double budgetedAmount, double actualAmount, boolean paid, boolean recurring, boolean placeholder) {
        this(id, type, category, budgetedAmount, actualAmount, paid, recurring, "", "BUDGET", "", placeholder);
    }

    /**
     * Convenience constructor for basic expense entries.
     */
    public TransactionEntry(String category, double budgetedAmount, double actualAmount) {
        this(-1, "Expense", category, budgetedAmount, actualAmount, false, false, false);
    }

    /**
     * Factory method creating an unpersisted placeholder entry for table insertion rows.
     */
    public static TransactionEntry createPlaceholder(String type) {
        return new TransactionEntry(-1, type, "", 0.0, 0.0, false, false, true);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public String getType() { return type.get(); }
    public StringProperty typeProperty() { return type; }
    public void setType(String type) { this.type.set(type); }

    public StringProperty categoryProperty() { return category; }
    public String getCategory() { return category.get(); }
    public void setCategory(String category) { this.category.set(category); }

    public DoubleProperty budgetedAmountProperty() { return budgetedAmount; }
    public double getBudgetedAmount() { return budgetedAmount.get(); }
    public void setBudgetedAmount(double budgetedAmount) { this.budgetedAmount.set(budgetedAmount); }

    public DoubleProperty actualAmountProperty() { return actualAmount; }
    public double getActualAmount() { return actualAmount.get(); }
    public void setActualAmount(double actualAmount) { this.actualAmount.set(actualAmount); }

    public BooleanProperty paidProperty() { return paid; }
    public boolean isPaid() { return paid.get(); }
    public void setPaid(boolean paid) { this.paid.set(paid); }

    public BooleanProperty recurringProperty() { return recurring; }
    public boolean isRecurring() { return recurring.get(); }
    public void setRecurring(boolean recurring) { this.recurring.set(recurring); }

    public StringProperty senderReceiverProperty() { return senderReceiver; }
    public String getSenderReceiver() { return senderReceiver.get(); }
    public void setSenderReceiver(String senderReceiver) { this.senderReceiver.set(senderReceiver); }

    public StringProperty txTypeProperty() { return txType; }
    public String getTxType() { return txType.get(); }
    public void setTxType(String txType) { this.txType.set(txType); }

    public StringProperty dateProperty() { return date; }
    public String getDate() { return date.get(); }
    public void setDate(String date) { this.date.set(date); }

    public boolean isPlaceholder() { return placeholder; }
    public void setPlaceholder(boolean placeholder) { this.placeholder = placeholder; }
}
