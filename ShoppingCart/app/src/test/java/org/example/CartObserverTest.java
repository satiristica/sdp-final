package org.example;

import static org.junit.jupiter.api.Assertions.*;
import org.example.cart.ShoppingCart;
import org.example.common.Money;
import org.example.product.GiftWrap;
import org.junit.jupiter.api.Test;

class CartObserverTest {
    @Test
    void refreshUpdatesAllMatchingLinesAndKeepsUnchangedPrices() {
        var store = TestShop.create();
        store.cart().add(TestShop.coat(store), 1);
        store.cart().add(new GiftWrap(TestShop.coat(store)), 2);
        store.catalog().refresh();
        assertEquals(Money.amount("153000"), store.cart().totals().total());
        assertEquals(Money.amount("45000"), store.cart().lines().getFirst().basePrice());
        assertEquals(Money.amount("45000"), store.cart().lines().get(1).basePrice());
        assertEquals(1, store.cart().takePriceUpdates().size());
        assertTrue(store.cart().takePriceUpdates().isEmpty());
        store.catalog().refresh();
        assertEquals(Money.amount("153000"), store.cart().totals().total());
    }

    @Test
    void cartsSharingACatalogReceiveNewPrices() {
        var store = TestShop.create();
        var otherCart = new ShoppingCart(store.catalog());
        store.cart().add(TestShop.coat(store), 1);
        otherCart.add(TestShop.coat(store), 1);
        store.catalog().refresh();
        assertEquals(Money.amount("50000"), store.cart().totals().total());
        assertEquals(Money.amount("50000"), otherCart.totals().total());
        otherCart.clear();
        assertTrue(otherCart.isEmpty());
        assertEquals(Money.ZERO, otherCart.totals().total());
    }

    @Test
    void addingAfterAnEmptyCartRefreshUsesTheCurrentPrice() {
        var store = TestShop.create();
        store.cart().add(TestShop.coat(store), 1);
        store.cart().remove(1);
        store.catalog().refresh();
        assertTrue(store.cart().isEmpty());
        store.cart().add(TestShop.coat(store), 1);
        assertEquals(Money.amount("50000"), store.cart().totals().total());
    }
}
