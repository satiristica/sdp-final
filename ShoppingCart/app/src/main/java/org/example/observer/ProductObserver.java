package org.example.observer;

import java.math.BigDecimal;

public interface ProductObserver {
    void onPriceChanged(String id, BigDecimal newPrice);
}
