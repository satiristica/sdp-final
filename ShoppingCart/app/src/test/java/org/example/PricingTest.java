package org.example;

import static org.junit.jupiter.api.Assertions.*;
import org.example.common.Money;
import org.example.common.StoreException;
import org.example.discount.NoDiscount;
import org.example.discount.PercentageDiscount;
import org.example.product.GiftWrap;
import org.junit.jupiter.api.Test;

class PricingTest {
    @Test
    void discountCoversClothingAndConfigurationButNotWrapping() {
        var store = TestShop.create();
        store.cart().add(new GiftWrap(TestShop.coat(store)), 2);
        store.cart().applyDiscount(new PercentageDiscount(10));
        var totals = store.cart().totals();
        assertEquals(Money.amount("90000"), totals.products());
        assertEquals(Money.amount("3000"), totals.extras());
        assertEquals(Money.amount("9000"), totals.discount());
        assertEquals(Money.amount("84000"), totals.total());
    }

    @Test
    void selectingANewStrategyReplacesThePreviousOne() {
        var store = TestShop.create();
        store.cart().add(TestShop.coat(store), 1);
        store.cart().applyDiscount(new PercentageDiscount(10));
        assertEquals(Money.amount("40500"), store.cart().totals().total());
        store.cart().applyDiscount(new NoDiscount());
        assertEquals(Money.amount("45000"), store.cart().totals().total());
    }

    @Test
    void discountsAreRoundedToTwoDecimalPlaces() {
        assertEquals(Money.amount("0.01"), new PercentageDiscount(10).discountOn(Money.amount("0.05")));
        assertThrows(StoreException.class, () -> new PercentageDiscount(101));
        assertEquals("1.01 KZT", Money.format(Money.amount("1.005")));
    }

    @Test
    void badQuantitiesLeaveCartUnchanged() {
        var store = TestShop.create();
        assertThrows(StoreException.class, () -> store.cart().add(TestShop.coat(store), 0));
        assertTrue(store.cart().isEmpty());
        store.cart().add(TestShop.coat(store), 1);
        assertThrows(StoreException.class, () -> store.cart().add(TestShop.coat(store), -1));
        assertEquals(1, store.cart().lines().size());
        assertEquals(1, store.cart().lines().getFirst().quantity());
        assertThrows(StoreException.class, () -> store.cart().remove(99));
    }

    @Test
    void removingAnItemUsesItsCurrentPositionInTheList() {
        var store = TestShop.create();
        store.cart().add(TestShop.coat(store), 1);
        store.cart().add(new GiftWrap(TestShop.coat(store)), 2);
        store.cart().remove(1);
        assertEquals(2, store.cart().lines().getFirst().quantity());
        assertEquals(Money.amount("93000"), store.cart().totals().total());
        assertThrows(StoreException.class, () -> store.cart().remove(0));
        store.cart().remove(1);
        assertTrue(store.cart().isEmpty());
    }
}
