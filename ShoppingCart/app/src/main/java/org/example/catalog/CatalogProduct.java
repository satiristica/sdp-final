package org.example.catalog;

import java.math.BigDecimal;
import org.example.common.Checks;
import org.example.common.Money;

public record CatalogProduct(String id, String name, BigDecimal basePrice) {
    public CatalogProduct {
        id = Checks.text(id, "Product ID");
        name = Checks.text(name, "Product name");
        basePrice = Money.amount(basePrice);
    }
}
