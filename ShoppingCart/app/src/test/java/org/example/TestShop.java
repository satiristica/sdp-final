package org.example;

import org.example.product.ConfiguredProduct;

final class TestShop {
    private TestShop() {}
    static StoreApplication create() { return new StoreApplication(); }
    static ConfiguredProduct coat(StoreApplication store) {
        return new ConfiguredProduct.Builder(store.catalog().find("COAT")).build();
    }
}
