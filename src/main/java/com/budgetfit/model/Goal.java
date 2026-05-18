package com.budgetfit.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Goal {
    private final IntegerProperty id;
    private final StringProperty name;
    private final DoubleProperty targetAmount;
    private final DoubleProperty currentAmount;
    private final StringProperty targetDate;

    public Goal(int id, String name, double targetAmount, double currentAmount, String targetDate) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.targetAmount = new SimpleDoubleProperty(targetAmount);
        this.currentAmount = new SimpleDoubleProperty(currentAmount);
        this.targetDate = new SimpleStringProperty(targetDate != null && !targetDate.isEmpty() ? targetDate : "2026-12");
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public void setName(String name) { this.name.set(name); }

    public double getTargetAmount() { return targetAmount.get(); }
    public DoubleProperty targetAmountProperty() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount.set(targetAmount); }

    public double getCurrentAmount() { return currentAmount.get(); }
    public DoubleProperty currentAmountProperty() { return currentAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount.set(currentAmount); }

    public String getTargetDate() { return targetDate.get(); }
    public StringProperty targetDateProperty() { return targetDate; }
    public void setTargetDate(String targetDate) { this.targetDate.set(targetDate); }

    public double getProgress() {
        if (getTargetAmount() <= 0) return 0;
        return getCurrentAmount() / getTargetAmount();
    }

    public boolean isAheadOfPace() {
        try {
            String[] parts = getTargetDate().split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int currentYear = LocalDate.now().getYear();
            int currentMonth = LocalDate.now().getMonthValue();

            int totalMonths = ((year - 2026) * 12) + month; // Assuming base start Jan 2026
            int elapsedMonths = ((currentYear - 2026) * 12) + currentMonth;
            if (totalMonths <= 0) totalMonths = 1;
            double expected = (double) elapsedMonths / totalMonths;
            if (expected > 1.0) expected = 1.0;
            return getProgress() >= expected;
        } catch (Exception e) {
            return getProgress() >= 0.5;
        }
    }

    public String getPaceStatus() {
        try {
            String[] parts = getTargetDate().split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int currentYear = LocalDate.now().getYear();
            int currentMonth = LocalDate.now().getMonthValue();

            int totalMonths = ((year - 2026) * 12) + month;
            int elapsedMonths = ((currentYear - 2026) * 12) + currentMonth;
            if (totalMonths <= 0) totalMonths = 1;
            double expected = (double) elapsedMonths / totalMonths;
            if (expected > 1.0) expected = 1.0;

            double diff = Math.abs(getProgress() - expected) * 100.0;
            boolean ahead = getProgress() >= expected;
            return String.format("You're %s and should reach your goal %.0f%% %s schedule (Target: %s)",
                    ahead ? "ahead of pace" : "behind pace", diff, ahead ? "ahead of" : "behind", getTargetDate());
        } catch (Exception e) {
            return "Pace tracking active (Target: " + getTargetDate() + ")";
        }
    }
}

