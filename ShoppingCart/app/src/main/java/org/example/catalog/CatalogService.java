package org.example.catalog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.example.common.StoreException;
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
        CatalogProduct product = products.get(id);
        
        if (product == null) {
            throw new StoreException("Server Error: Unknown product: " + id);
        }
        return product;
    }


    public void addObserver(ProductObserver observer) {
        if (!observers.contains(observer)) { 
            observers.add(observer); 
        }
    }


    public void removeObserver(ProductObserver observer) { 
        observers.remove(observer); 
    }

    public void refresh() {
        List<CatalogProduct> updated = source.loadProducts();
        Map<String, CatalogProduct> batch = new LinkedHashMap<>();
        
        for (CatalogProduct product : updated) {
            if (batch.putIfAbsent(product.id(), product) != null) {
                throw new StoreException("Server Error: Duplicate supplier product: " + product.id());
            }
        }
        
        Map<String, CatalogProduct> previous = new LinkedHashMap<>(products);
        products.putAll(batch);
        
        for (CatalogProduct product : updated) {
            CatalogProduct old = previous.get(product.id());
            if (old != null && old.basePrice().compareTo(product.basePrice()) != 0) {
                for (ProductObserver observer : List.copyOf(observers)) {
                    observer.onPriceChanged(product.id(), product.basePrice());
                }
            }
        }
    }
}
