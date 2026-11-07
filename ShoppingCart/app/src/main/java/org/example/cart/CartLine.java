package org.example.cart;

import java.math.BigDecimal;
import org.example.common.StoreException;
import org.example.product.CartItem;

public record CartLine(CartItem item, int quantity, BigDecimal basePrice) {
    public CartLine {
        if (quantity <= 0) {
            throw new StoreException("Client Error: Quantity must be greater than zero.");
        }
    }

    public BigDecimal productTotal() {
        return item.productPrice(basePrice).multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal extrasTotal() {
        return item.extrasPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public CartLine withPrice(BigDecimal price) {
        return new CartLine(item, quantity, price);
    }
}
