package org.example.product;

import java.math.BigDecimal;

public interface CartItem {
    String id();
    String description();
    BigDecimal productPrice(BigDecimal currentBasePrice);
    BigDecimal extrasPrice();
}
