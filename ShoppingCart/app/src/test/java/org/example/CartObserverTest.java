package org.example;

import static org.junit.jupiter.api.Assertions.*;
import org.example.cart.ShoppingCart;
import org.example.common.Money;
import org.example.product.GiftWrap;
import org.junit.jupiter.api.Test;

class CartObserverTest {
    @Test
    void refreshUpdatesAllMatchingLinesAndPublishesOneNoticePerProduct() {
        var store = TestShop.create();
        store.cart().add(TestShop.coat(store), 1);
        store.cart().add(new GiftWrap(TestShop.coat(store)), 2);
        store.catalog().refresh();
        assertEquals(Money.amount("153000"), store.cart().totals().total());
        assertEquals(Money.amount("45000"), store.cart().lines().getFirst().basePrice());
        assertEquals(1, store.cart().notifications().size());
        store.cart().clearNotifications();
        store.catalog().refresh();
        assertTrue(store.cart().notifications().isEmpty());
    }

    @Test
    void multipleCartsReceiveUpdatesAndAnEmptyCartUnsubscribes() {
        var store = TestShop.create();
        var otherCart = new ShoppingCart(store.catalog());
        store.cart().add(TestShop.coat(store), 1);
        otherCart.add(TestShop.coat(store), 1);
        store.catalog().refresh();
        assertEquals(1, store.cart().notifications().size());
        assertEquals(1, otherCart.notifications().size());
        otherCart.clear();
        assertTrue(otherCart.isEmpty());
        assertTrue(otherCart.notifications().isEmpty());
    }

    @Test
    void removingTheLastLineUnsubscribesAndAddingAgainResubscribes() {
        var store = TestShop.create();
        store.cart().add(TestShop.coat(store), 1);
        store.cart().remove(1);
        store.catalog().refresh();
        assertTrue(store.cart().notifications().isEmpty());
        store.cart().add(TestShop.coat(store), 1);
        assertEquals(Money.amount("50000"), store.cart().totals().total());
    }
}
