package org.example.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.example.common.StoreException;

public record PercentageDiscount(int percent) implements DiscountStrategy {
    public PercentageDiscount {
        if (percent < 1 || percent > 100) {
            throw new StoreException("Client Error: Discount must be between 1 and 100 percent.");
        }
    }

    @Override
    public BigDecimal discountOn(BigDecimal productSubtotal) {
        return productSubtotal.multiply(BigDecimal.valueOf(percent)).movePointLeft(2)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String description() { return percent + "% off clothing"; }
}
