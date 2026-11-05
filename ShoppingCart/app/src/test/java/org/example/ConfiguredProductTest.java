package org.example;

import static org.junit.jupiter.api.Assertions.*;
import org.example.common.Money;
import org.example.common.StoreException;
import org.example.product.ConfiguredProduct;
import org.example.product.GiftWrap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ConfiguredProductTest {
    @Test
    void builderCreatesTheChosenBrandAndMaterial() {
        var product = new ConfiguredProduct.Builder(TestShop.create().catalog().find("COAT"))
                .brand("Louis Vuitton").material("Leather").build();
        assertEquals("Louis Vuitton", product.brand());
        assertEquals("Leather", product.material());
        assertEquals(Money.amount("57000"), product.productPrice(Money.amount("40000")));
        assertEquals("Coat (Louis Vuitton, Leather)", product.description());
    }

    @ParameterizedTest
    @CsvSource({"Gucci,Cotton,45000", "Louis Vuitton,Wool,53000",
                "Yves Saint Laurent,Polyester,48500", "Gucci,Leather,52000"})
    void supportedOptionsAddTheCorrectSurcharges(String brand, String material, String price) {
        var product = new ConfiguredProduct.Builder(TestShop.create().catalog().find("COAT"))
                .brand(brand).material(material).build();
        assertEquals(Money.amount(price), product.productPrice(Money.amount("40000")));
    }

    @Test
    void reusingABuilderDoesNotChangeAnEarlierProduct() {
        var builder = new ConfiguredProduct.Builder(TestShop.create().catalog().find("COAT"));
        var first = builder.build();
        builder.brand("Louis Vuitton").material("Leather").build();
        assertEquals("Gucci", first.brand());
        assertEquals("Cotton", first.material());
    }

    @Test
    void unknownOptionsAreRejected() {
        var catalogProduct = TestShop.create().catalog().find("COAT");
        assertThrows(StoreException.class, () -> new ConfiguredProduct.Builder(catalogProduct).brand("Unknown").build());
        assertThrows(StoreException.class, () -> new ConfiguredProduct.Builder(catalogProduct).material("Unknown").build());
    }

    @Test
    void giftWrappingPreservesTheProductAndAddsOnlyTheServicePrice() {
        var item = new GiftWrap(TestShop.coat(TestShop.create()));
        assertEquals("COAT", item.id());
        assertEquals(Money.amount("45000"), item.productPrice(Money.amount("40000")));
        assertEquals(Money.amount("1500"), item.extrasPrice());
        assertTrue(item.description().contains("gift wrapping"));
    }
}
