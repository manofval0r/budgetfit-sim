package com.budgetfit.service;

import com.budgetfit.model.TransactionEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BudgetServiceTest {

    private BudgetService budgetService;

    @BeforeEach
    void setUp() {
        budgetService = new BudgetService();
    }

    @Test
    void testCalculateTotalBudget() {
        List<TransactionEntry> entries = Arrays.asList(
                new TransactionEntry(1, "Expense", "Food", 100.0, 50.0, false, false, false),
                new TransactionEntry(2, "Expense", "Rent", 1000.0, 1000.0, true, false, false),
                TransactionEntry.createPlaceholder("Expense") // Should be ignored
        );
        assertEquals(1100.0, budgetService.calculateTotalBudget(entries));
    }

    @Test
    void testCalculateTotalActual() {
        List<TransactionEntry> entries = Arrays.asList(
                new TransactionEntry(1, "Expense", "Food", 100.0, 50.0, false, false, false),
                new TransactionEntry(2, "Expense", "Rent", 1000.0, 1200.0, true, false, false),
                TransactionEntry.createPlaceholder("Expense") // Should be ignored
        );
        assertEquals(1250.0, budgetService.calculateTotalActual(entries));
    }

    @Test
    void testCalculateLeftToSpend() {
        assertEquals(2000.0, budgetService.calculateLeftToSpend(5000.0, 3000.0));
        assertEquals(0.0, budgetService.calculateLeftToSpend(5000.0, 6000.0)); // Clamped to zero
    }

    @Test
    void testCalculateItemVariance() {
        TransactionEntry entry = new TransactionEntry(1, "Expense", "Food", 100.0, 80.0, false, false, false);
        assertEquals(20.0, budgetService.calculateItemVariance(entry));
        
        TransactionEntry overspent = new TransactionEntry(2, "Expense", "Food", 100.0, 120.0, false, false, false);
        assertEquals(-20.0, budgetService.calculateItemVariance(overspent));
    }
}
