package org.example;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import org.example.ui.ConsoleStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

@ResourceLock(Resources.SYSTEM_OUT)
class ConsoleStoreTest {
    @Test
    void customerChoosesClothingBrandMaterialAndCompletesPurchase() {
        var store = TestShop.create();
        String output = run(store, "2", "1", "1", "2", "y", "1", "4", "2", "6", "y", "7");
        assertTrue(output.contains("Mr Robot Clothing Shop"));
        assertFalse(output.contains("Administrator"));
        assertTrue(output.contains("Coat (Gucci, Wool) + gift wrapping"));
        assertTrue(output.contains("TOTAL: 44700.00 KZT"));
        assertTrue(output.contains("Simulated payment approved: PAY-1"));
        assertTrue(store.cart().isEmpty());
    }

    @Test
    void refreshNotifiesCustomerAndRepricesCartBeforeCheckout() {
        var store = TestShop.create();
        String output = run(store, "2", "1", "1", "1", "n", "1", "5", "6", "y", "7");
        assertTrue(output.contains("Price update: Coat"));
        assertTrue(output.contains("TOTAL: 50000.00 KZT"));
        assertTrue(output.contains("Simulated payment approved: PAY-1"));
    }

    @Test
    void customerCanChooseQuantityAndRemoveAnItem() {
        var store = TestShop.create();
        String output = run(store, "2", "4", "3", "4", "n", "2", "3", "1", "1", "7");
        assertTrue(output.contains("T-shirt (Yves Saint Laurent, Leather) x2"));
        assertTrue(store.cart().isEmpty());
    }

    @Test
    void cancelledCheckoutKeepsTheCart() {
        var store = TestShop.create();
        String output = run(store, "2", "2", "2", "3", "n", "1", "6", "n", "7");
        assertTrue(output.contains("Purchase cancelled"));
        assertFalse(output.contains("Simulated payment approved"));
        assertFalse(store.cart().isEmpty());
    }

    @Test
    void invalidChoicesDoNotAddPartialProducts() {
        var store = TestShop.create();
        String output = run(store, "2", "9", "2", "1", "wrong", "7");
        assertTrue(output.contains("Client Error: Choose a number from 1 to 4"));
        assertTrue(output.contains("Client Error: Enter a whole number."));
        assertTrue(output.contains("Goodbye!"));
        assertTrue(store.cart().isEmpty());
    }

    @Test
    void aspectHandlesBadQuantityAndEmptyCheckoutWithoutStoppingTheMenu() {
        var store = TestShop.create();
        String output = run(store, "2", "1", "1", "1", "n", "0", "6", "7");
        assertTrue(output.contains("Quantity must be greater than zero"));
        assertTrue(output.contains("Add a product before checkout"));
        assertTrue(output.contains("Goodbye!"));
        assertTrue(store.cart().isEmpty());
    }

    @Test
    void cartNumbersFollowTheRemainingItemsAfterRemoval() {
        var store = TestShop.create();
        store.cart().add(TestShop.coat(store), 1);
        store.cart().add(TestShop.coat(store), 2);
        String output = run(store, "3", "1", "1", "3", "1", "1", "7");
        assertTrue(output.contains("2. Coat (Gucci, Cotton) x2"));
        assertTrue(output.contains("1. Coat (Gucci, Cotton) x2"));
        assertTrue(store.cart().isEmpty());
    }

    private String run(StoreApplication store, String... commands) {
        var captured = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (var console = new PrintStream(captured, true)) {
            System.setOut(console);
            new ConsoleStore(store, new StringReader(String.join("\n", commands) + "\n")).run();
        } finally {
            System.setOut(original);
        }
        return captured.toString();
    }
}
