package org.example.discount;

import java.math.BigDecimal;

public interface DiscountStrategy {
    BigDecimal discountOn(BigDecimal productSubtotal);
    String description();
}
