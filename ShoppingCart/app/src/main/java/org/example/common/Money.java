package org.example.common;

import java.math.BigDecimal;
import java.math.RoundingMode;


public final class Money {
    public static final BigDecimal ZERO = new BigDecimal("0.00");
    private Money() {}

    public static BigDecimal amount(String value) {
        try {
            return amount(new BigDecimal(Checks.text(value, "Price")));
        } 
        catch (NumberFormatException exception) {
            throw new StoreException("Server Error: Enter a valid decimal price.", exception);
        }
    }

    public static BigDecimal amount(BigDecimal value) {
        if (value == null || value.signum() < 0) {
            throw new StoreException("Server Error: Money must be a non-negative amount.");
        }
        
        try {
            return value.setScale(2, RoundingMode.UNNECESSARY);
        } 
        catch (ArithmeticException exception) {
            throw new StoreException("Server Error: Money supports at most two decimal places.", exception);
        }
    }

    public static String format(BigDecimal value) {
        return amount(value).toPlainString() + " KZT";
    }
}
