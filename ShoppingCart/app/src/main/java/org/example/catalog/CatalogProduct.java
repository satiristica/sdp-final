package org.example.catalog;

import java.math.BigDecimal;

public record CatalogProduct(String id, String name, BigDecimal basePrice) {}
