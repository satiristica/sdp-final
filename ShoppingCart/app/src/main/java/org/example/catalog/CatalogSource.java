package org.example.catalog;

import java.util.List;

public interface CatalogSource {
    List<CatalogProduct> loadProducts();
}
