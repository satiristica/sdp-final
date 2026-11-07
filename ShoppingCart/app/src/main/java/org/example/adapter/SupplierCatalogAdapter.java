package org.example.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.example.catalog.CatalogProduct;
import org.example.catalog.CatalogSource;
import org.example.common.Money;
import org.example.common.StoreException;
import org.example.supplier.ExternalSupplier;

public final class SupplierCatalogAdapter implements CatalogSource {
    private final ExternalSupplier supplier;

    public SupplierCatalogAdapter(ExternalSupplier supplier) {
        this.supplier = supplier;
    }

    @Override
    public List<CatalogProduct> loadProducts() {
        List<CatalogProduct> products = new ArrayList<>();
        for (String row : supplier.fetchItems()) {
            String[] parts = row.split(":", 2);
            if (parts.length != 2 || !parts[1].trim().matches("\\d+(\\.\\d{1,2})?")) {
                throw new StoreException("Server Error: Invalid supplier row: " + row);
            }
            String name = parts[0].trim();
            String id = name.replace("-", "").toUpperCase(Locale.ROOT);
            products.add(new CatalogProduct(id, name, Money.amount(parts[1])));
        }
        return products;
    }
}
