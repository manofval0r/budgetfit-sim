package com.budgetfit.service;

import com.budgetfit.model.TransactionEntry;

import java.util.List;

/**
 * Service class encapsulating core business logic and mathematical calculations
 * for budgeting, outflow aggregation, remaining income determination, and variance analysis.
 * Entirely decoupled from UI rendering threads to guarantee deterministic unit testability.
 */
public class BudgetService {

    /**
     * Calculates the aggregate budgeted (planned) amount for a collection of transactions.
     * Excludes uncommitted placeholder rows.
     *
     * @param entries the list of transaction entries to sum
     * @return the total planned amount
     */
    public double calculateTotalBudget(List<TransactionEntry> entries) {
        if (entries == null) return 0.0;
        return entries.stream()
                .filter(entry -> !entry.isPlaceholder())
                .mapToDouble(TransactionEntry::getBudgetedAmount)
                .sum();
    }

    /**
     * Calculates the aggregate actual spent/received amount for a collection of transactions.
     * Excludes uncommitted placeholder rows.
     *
     * @param entries the list of transaction entries to sum
     * @return the total actual amount
     */
    public double calculateTotalActual(List<TransactionEntry> entries) {
        if (entries == null) return 0.0;
        return entries.stream()
                .filter(entry -> !entry.isPlaceholder())
                .mapToDouble(TransactionEntry::getActualAmount)
                .sum();
    }

    /**
     * Aggregates standard monthly outflows comprising actual spends across Expenses, Bills, and Debt categories.
     * Income and pure Savings transfers do not subtract from standard leftover spendables.
     *
     * @param expenses actual expense transactions
     * @param bills actual bill transactions
     * @param debts actual debt repayments
     * @return the sum of actual standard outflows
     */
    public double calculateTotalOutflow(List<TransactionEntry> expenses,
                                        List<TransactionEntry> bills,
                                        List<TransactionEntry> debts) {
        return calculateTotalActual(expenses) +
               calculateTotalActual(bills) +
               calculateTotalActual(debts);
    }

    /**
     * Determines the remaining discretionary income after accounting for all actual standard outflows.
     * Clamps the value to zero to prevent invalid negative slices in allocation visualizations.
     *
     * @param monthlyIncome the baseline user income
     * @param totalOutflow aggregate actual spend
     * @return the non-negative amount left to spend
     */
    public double calculateLeftToSpend(double monthlyIncome, double totalOutflow) {
        return Math.max(monthlyIncome - totalOutflow, 0.0);
    }

    /**
     * Calculates the financial variance (Planned - Actual) for a single transaction item.
     * A positive variance indicates favorable underspending; a negative variance indicates unfavorable overspending.
     *
     * @param entry the target transaction entry
     * @return the calculated variance
     */
    public double calculateItemVariance(TransactionEntry entry) {
        if (entry == null || entry.isPlaceholder()) return 0.0;
        return entry.getBudgetedAmount() - entry.getActualAmount();
    }

    /**
     * Calculates the aggregate financial variance across an entire category list.
     *
     * @param entries the list of transaction entries
     * @return the overall category variance
     */
    public double calculateCategoryVariance(List<TransactionEntry> entries) {
        if (entries == null) return 0.0;
        return calculateTotalBudget(entries) - calculateTotalActual(entries);
    }
}
