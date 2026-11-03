package org.example.cart;

import java.math.BigDecimal;
import org.example.common.Checks;
import org.example.common.Money;
import org.example.product.CartItem;

public record CartLine(int number, CartItem item, int quantity, BigDecimal basePrice) {
    public CartLine {
        Checks.positive(quantity, "Quantity");
        basePrice = Money.amount(basePrice);
    }

    public BigDecimal productTotal() {
        return item.productPrice(basePrice).multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal extrasTotal() {
        return item.extrasPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public CartLine withPrice(BigDecimal price) { 
        return new CartLine(number, item, quantity, price); 
    }
}
