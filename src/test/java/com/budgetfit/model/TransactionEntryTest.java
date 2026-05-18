package com.budgetfit.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TransactionEntryTest {

    @Test
    void testPropertyBinding() {
        TransactionEntry entry = new TransactionEntry(1, "Expense", "Initial", 100.0, 50.0, false, false, false);
        
        // Test initial values
        assertEquals("Initial", entry.getCategory());
        assertEquals(100.0, entry.getBudgetedAmount());
        
        // Test property updates
        entry.setCategory("Updated");
        assertEquals("Updated", entry.categoryProperty().get());
        
        entry.setBudgetedAmount(200.0);
        assertEquals(200.0, entry.budgetedAmountProperty().get());
    }

    @Test
    void testPlaceholderFactory() {
        TransactionEntry p = TransactionEntry.createPlaceholder("Income");
        assertTrue(p.isPlaceholder());
        assertEquals("Income", p.getType());
        assertEquals(0.0, p.getBudgetedAmount());
        assertEquals(-1, p.getId());
    }
}
