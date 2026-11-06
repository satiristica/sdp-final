package org.example;

import static org.junit.jupiter.api.Assertions.*;
import org.example.common.Money;
import org.example.common.StoreException;
import org.example.discount.PercentageDiscount;
import org.example.product.GiftWrap;
import org.junit.jupiter.api.Test;

class CheckoutFacadeTest {
    @Test
    void checkoutReturnsPaymentReferenceAndClearsCartAndDiscount() {
        var store = TestShop.create();
        store.cart().add(new GiftWrap(TestShop.coat(store)), 1);
        store.cart().applyDiscount(new PercentageDiscount(10));
        assertEquals("PAY-1", store.checkout().checkout());
        assertTrue(store.cart().isEmpty());
        assertEquals(Money.ZERO, store.cart().totals().total());
        assertTrue(store.cart().discount().description().contains("no discount"));
    }

    @Test
    void consecutivePurchasesReturnDifferentPaymentReferences() {
        var store = TestShop.create();
        store.cart().add(TestShop.coat(store), 1);
        assertEquals("PAY-1", store.checkout().checkout());
        store.cart().add(TestShop.coat(store), 1);
        assertEquals("PAY-2", store.checkout().checkout());
    }

    @Test
    void anEmptyCartCannotBePurchasedOrPurchasedTwice() {
        var store = TestShop.create();
        assertThrows(StoreException.class, () -> store.checkout().checkout());
        store.cart().add(TestShop.coat(store), 1);
        store.checkout().checkout();
        assertThrows(StoreException.class, () -> store.checkout().checkout());
    }
}
