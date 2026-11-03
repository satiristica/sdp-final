package org.example.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final List<String> notifications = new ArrayList<>();
    private DiscountStrategy discount = new NoDiscount();
    private int nextNumber = 1;

    public ShoppingCart(CatalogService catalog) { 
        this.catalog = catalog; 
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
    
    public List<String> notifications() { 
        return List.copyOf(notifications); 
    }

    public void clearNotifications() { 
        notifications.clear(); 
    }

    public void add(CartItem item, int quantity) {
        BigDecimal price = catalog.find(item.id()).basePrice();
        CartLine line = new CartLine(nextNumber, item, quantity, price);
        
        if (isEmpty()) { 
            catalog.addObserver(this); 
        }
        
        lines.add(line);
        nextNumber++;
    }

    public void remove(int number) {
        lines.remove(indexOf(number));
        
        if (isEmpty()) { 
            catalog.removeObserver(this); 
        }
    }

    public void applyDiscount(DiscountStrategy strategy) { 
        discount = Objects.requireNonNull(strategy); 
    }

    public Totals totals() {
        BigDecimal products = Money.ZERO;
        BigDecimal extras = Money.ZERO;
        
        for (CartLine line : lines) {
            products = products.add(line.productTotal());
            extras = extras.add(line.extrasTotal());
        }

        BigDecimal saved = Money.amount(discount.discountOn(products));
        if (saved.compareTo(products) > 0) { 
            throw new StoreException("Server Error: Discount exceeds product prices."); 
        }
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
            notifications.add(catalog.find(id).name() + ": new base price " + Money.format(newPrice));
        }
    }

    public void clear() {
        catalog.removeObserver(this);
        lines.clear();
        notifications.clear();
        discount = new NoDiscount();
    }

    private int indexOf(int number) {
        for (int index = 0; index < lines.size(); index++) {
            if (lines.get(index).number() == number) { 
                return index; 
            }
        }
        throw new StoreException("Client Error: Cart item not found: " + number);
    }

    public record Totals
    (
        BigDecimal products, BigDecimal extras, BigDecimal discount, BigDecimal total
    ) {}
}
