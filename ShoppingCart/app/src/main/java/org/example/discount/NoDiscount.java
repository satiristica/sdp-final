package org.example.discount;

import java.math.BigDecimal;
import org.example.common.Money;

public final class NoDiscount implements DiscountStrategy {
    @Override
    public BigDecimal discountOn(BigDecimal productSubtotal) { 
        return Money.ZERO; 
    }

    @Override
    public String description() { 
        return "NONE - no discount"; 
    }
}
