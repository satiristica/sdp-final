package org.example.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {
    public static final BigDecimal ZERO = new BigDecimal("0.00");
    private Money() {}

    public static BigDecimal amount(String value) {
        return new BigDecimal(value.trim()).setScale(2, RoundingMode.HALF_UP);
    }

    public static String format(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString() + " KZT";
    }
}
