package com.budgetfit.model;

import javafx.beans.property.*;

/**
 * Domain model representing a user account in the BudgetFit system.
 * Utilizes JavaFX properties to allow direct binding in UI tables.
 * Expanded to include secure PIN hash for ATM step-up authentication.
 */
public class User {
    private final IntegerProperty id;
    private final StringProperty username;
    private final StringProperty role;
    private final StringProperty lastActive;
    private final DoubleProperty monthlyIncome;
    private final StringProperty pinHash;

    public User(int id, String username, String role, String lastActive, double monthlyIncome, String pinHash) {
        this.id = new SimpleIntegerProperty(id);
        this.username = new SimpleStringProperty(username == null ? "" : username);
        this.role = new SimpleStringProperty(role == null ? "user" : role);
        this.lastActive = new SimpleStringProperty(lastActive == null ? "" : lastActive);
        this.monthlyIncome = new SimpleDoubleProperty(monthlyIncome);
        this.pinHash = new SimpleStringProperty(pinHash == null ? "" : pinHash);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getUsername() {
        return username.get();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getRole() {
        return role.get();
    }

    public StringProperty roleProperty() {
        return role;
    }

    public void setRole(String role) {
        this.role.set(role);
    }

    public String getLastActive() {
        return lastActive.get();
    }

    public StringProperty lastActiveProperty() {
        return lastActive;
    }

    public void setLastActive(String lastActive) {
        this.lastActive.set(lastActive);
    }

    public double getMonthlyIncome() {
        return monthlyIncome.get();
    }

    public DoubleProperty monthlyIncomeProperty() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(double monthlyIncome) {
        this.monthlyIncome.set(monthlyIncome);
    }

    public String getPinHash() {
        return pinHash.get();
    }

    public StringProperty pinHashProperty() {
        return pinHash;
    }

    public void setPinHash(String pinHash) {
        this.pinHash.set(pinHash);
    }
}
