package org.example;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.example.adapter.SupplierCatalogAdapter;
import org.example.catalog.CatalogService;
import org.example.common.Money;
import org.example.common.StoreException;
import org.example.supplier.ExternalSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SupplierAdapterTest {
    @Test
    void adapterConvertsFourClothingRecordsIntoObjects() {
        var products = new SupplierCatalogAdapter(new ExternalSupplier()).loadProducts();
        assertEquals(List.of("Coat", "Turtleneck", "Sneakers", "T-shirt"),
                products.stream().map(product -> product.name()).toList());
        assertEquals(List.of("COAT", "TURTLENECK", "SNEAKERS", "TSHIRT"),
                products.stream().map(product -> product.id()).toList());
        assertEquals(Money.amount("40000"), products.getFirst().basePrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {"wrong", "-10", "1.001"})
    void badSupplierPricesAreRejected(String price) {
        var error = assertThrows(StoreException.class, () -> adapter(new String[] {"Coat: " + price}).loadProducts());
        assertTrue(error.getMessage().startsWith("Server Error: "));
    }

    @Test
    void badRowShapeAndNullDataAreRejected() {
        assertThrows(StoreException.class, () -> adapter(new String[] {"Coat"}).loadProducts());
        assertThrows(StoreException.class, () -> adapter(null).loadProducts());
    }

    @Test
    void aMalformedRefreshDoesNotPartiallyUpdateTheCatalog() {
        ExternalSupplier supplier = new ExternalSupplier() {
            private int calls;
            @Override public String[] fetchItems() {
                if (calls++ == 0) { return new String[] {"Coat: 100"}; }
                return new String[] {"Coat: 200", "Shirt: bad"};
            }
        };
        var catalog = new CatalogService(new SupplierCatalogAdapter(supplier));
        assertThrows(StoreException.class, catalog::refresh);
        assertEquals(Money.amount("100"), catalog.find("COAT").basePrice());
        assertEquals(1, catalog.products().size());
    }

    @Test
    void duplicateProductIdsAreRejected() {
        assertThrows(StoreException.class, () -> new CatalogService(adapter(new String[] {
            "T-shirt: 100", "Tshirt: 200"
        })));
    }

    private SupplierCatalogAdapter adapter(String[] rows) {
        return new SupplierCatalogAdapter(new ExternalSupplier() {
            @Override public String[] fetchItems() { return rows; }
        });
    }
}
