package org.example;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.example.adapter.SupplierCatalogAdapter;
import org.example.catalog.CatalogService;
import org.example.common.Money;
import org.example.supplier.ExternalSupplier;
import org.junit.jupiter.api.Test;

class SupplierAdapterTest {
    @Test
    void adapterConvertsFourClothingRecordsIntoObjects() {
        var products = new SupplierCatalogAdapter(new ExternalSupplier()).loadProducts();
        assertEquals(List.of("Coat", "Turtleneck", "Sneakers", "T-shirt"),
                products.stream().map(product -> product.name()).toList());
        assertEquals(List.of("COAT", "TURTLENECK", "SNEAKERS", "TSHIRT"),
                products.stream().map(product -> product.id()).toList());
        assertEquals(List.of("40000.00", "15000.00", "30000.00", "10000.00"),
                products.stream().map(product -> product.basePrice().toPlainString()).toList());
    }

    @Test
    void refreshReplacesSupplierPricesWithoutDuplicatingProducts() {
        var catalog = new CatalogService(new SupplierCatalogAdapter(new ExternalSupplier()));
        catalog.refresh();
        assertEquals(List.of("45000.00", "17000.00", "32000.00", "12000.00"),
                catalog.products().stream().map(product -> product.basePrice().toPlainString()).toList());
        catalog.refresh();
        assertEquals(4, catalog.products().size());
        assertEquals(Money.amount("45000"), catalog.find("COAT").basePrice());
    }
}
