package org.example.catalog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.example.observer.ProductObserver;

public final class CatalogService {
    private final CatalogSource source;
    private final Map<String, CatalogProduct> products = new LinkedHashMap<>();
    private final List<ProductObserver> observers = new ArrayList<>();

    public CatalogService(CatalogSource source) {
        this.source = source;
        refresh();
    }

    public List<CatalogProduct> products() {
        return List.copyOf(products.values());
    }

    public CatalogProduct find(String id) {
        return products.get(id);
    }

    public void addObserver(ProductObserver observer) {
        observers.add(observer);
    }

    public void refresh() {
        for (CatalogProduct product : source.loadProducts()) {
            CatalogProduct old = products.put(product.id(), product);
            if (old != null && old.basePrice().compareTo(product.basePrice()) != 0) {
                for (ProductObserver observer : observers) {
                    observer.onPriceChanged(product.id(), product.basePrice());
                }
            }
        }
    }
}
