package com.budgetfit.model;

import javafx.beans.property.*;

/**
 * Domain model representing an investment asset holding (e.g. Stock, Crypto).
 * Fully backed by JavaFX properties for direct UI table binding.
 */
public class Investment {
    private final IntegerProperty id;
    private final IntegerProperty userId;
    private final StringProperty symbol;
    private final DoubleProperty shares;
    private final DoubleProperty avgPrice;
    private final DoubleProperty currentPrice;

    public Investment(int id, int userId, String symbol, double shares, double avgPrice, double currentPrice) {
        this.id = new SimpleIntegerProperty(id);
        this.userId = new SimpleIntegerProperty(userId);
        this.symbol = new SimpleStringProperty(symbol == null ? "" : symbol);
        this.shares = new SimpleDoubleProperty(shares);
        this.avgPrice = new SimpleDoubleProperty(avgPrice);
        this.currentPrice = new SimpleDoubleProperty(currentPrice);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public int getUserId() { return userId.get(); }
    public IntegerProperty userIdProperty() { return userId; }
    public void setUserId(int userId) { this.userId.set(userId); }

    public String getSymbol() { return symbol.get(); }
    public StringProperty symbolProperty() { return symbol; }
    public void setSymbol(String symbol) { this.symbol.set(symbol); }

    public double getShares() { return shares.get(); }
    public DoubleProperty sharesProperty() { return shares; }
    public void setShares(double shares) { this.shares.set(shares); }

    public double getAvgPrice() { return avgPrice.get(); }
    public DoubleProperty avgPriceProperty() { return avgPrice; }
    public void setAvgPrice(double avgPrice) { this.avgPrice.set(avgPrice); }

    public double getCurrentPrice() { return currentPrice.get(); }
    public DoubleProperty currentPriceProperty() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice.set(currentPrice); }

    public double getTotalValue() {
        return getShares() * getCurrentPrice();
    }

    public double getProfitLoss() {
        return (getCurrentPrice() - getAvgPrice()) * getShares();
    }
}
