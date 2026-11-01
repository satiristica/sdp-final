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
        String[] rows = supplier.fetchItems();
        
        if (rows == null) {
            throw new StoreException("Server Error: Supplier returned no data.");
        }
        
        List<CatalogProduct> products = new ArrayList<>();
        for (String row : rows) {
            if (row == null) {
                throw new StoreException("Server Error: Supplier item is missing.");
            }
            
            String[] parts = row.split(":", 2);
            if (parts.length != 2) {
                throw new StoreException("Server Error: Expected supplier item in the format 'Name: price': " + row);
            }

            String name = parts[0].trim();
            String id = name.replace("-", "").toUpperCase(Locale.ROOT);
            products.add(
                new CatalogProduct(id, name, Money.amount(parts[1]))
            );
        }
        return List.copyOf(products);
    }
}
