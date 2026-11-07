package org.example.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.example.catalog.CatalogService;
import org.example.common.Money;
import org.example.common.StoreException;
import org.example.discount.DiscountStrategy;
import org.example.discount.NoDiscount;
import org.example.observer.ProductObserver;
import org.example.product.CartItem;

public final class ShoppingCart implements ProductObserver {
    private final CatalogService catalog;
    private final List<CartLine> lines = new ArrayList<>();
    private DiscountStrategy discount = new NoDiscount();

    public ShoppingCart(CatalogService catalog) {
        this.catalog = catalog;
        catalog.addObserver(this);
    }

    public List<CartLine> lines() {
        return List.copyOf(lines);
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public DiscountStrategy discount() {
        return discount;
    }

    public void add(CartItem item, int quantity) {
        lines.add(new CartLine(item, quantity, catalog.find(item.id()).basePrice()));
    }

    public void remove(int number) {
        if (number < 1 || number > lines.size()) {
            throw new StoreException("Client Error: Cart item not found: " + number);
        }
        lines.remove(number - 1);
    }

    public void applyDiscount(DiscountStrategy strategy) {
        discount = strategy;
    }

    public Totals totals() {
        BigDecimal products = Money.ZERO;
        BigDecimal extras = Money.ZERO;
        for (CartLine line : lines) {
            products = products.add(line.productTotal());
            extras = extras.add(line.extrasTotal());
        }
        BigDecimal saved = discount.discountOn(products);
        return new Totals(products, extras, saved, products.subtract(saved).add(extras));
    }

    @Override
    public void onPriceChanged(String id, BigDecimal newPrice) {
        boolean changed = false;
        for (int index = 0; index < lines.size(); index++) {
            CartLine line = lines.get(index);
            if (line.item().id().equals(id)) {
                lines.set(index, line.withPrice(newPrice));
                changed = true;
            }
        }
        if (changed) {
            System.out.println("Price update: " + catalog.find(id).name() + " - " + Money.format(newPrice));
        }
    }

    public void clear() {
        lines.clear();
        discount = new NoDiscount();
    }

    public record Totals(BigDecimal products, BigDecimal extras, BigDecimal discount, BigDecimal total) {}
}
