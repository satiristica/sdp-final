package org.example;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.example.common.Money;
import org.example.common.StoreException;
import org.example.discount.PercentageDiscount;
import org.junit.jupiter.api.Test;

class StoreApplicationTest {
    @Test
    void refreshCatalogReturnsPriceUpdatesForCartItems() {
        var store = TestShop.create();
        store.addToCart(TestShop.coat(store), 1);

        List<String> updates = store.refreshCatalog();

        assertEquals(List.of("Coat - 45000.00 KZT"), updates);
        assertEquals(Money.amount("50000"), store.cartTotals().total());
    }

    @Test
    void checkoutCannotStartWithAnEmptyCart() {
        var store = TestShop.create();

        var error = assertThrows(StoreException.class, store::startCheckout);

        assertEquals("Client Error: Add a product before checkout.", error.getMessage());
    }

    @Test
    void discountDescriptionFollowsTheSelectedStrategy() {
        var store = TestShop.create();

        store.applyDiscount(new PercentageDiscount(10));

        assertEquals("10% off clothing", store.discountDescription());
    }
}
