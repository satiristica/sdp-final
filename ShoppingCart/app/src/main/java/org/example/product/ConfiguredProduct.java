package org.example.product;

import java.math.BigDecimal;
import java.util.List;
import org.example.catalog.CatalogProduct;
import org.example.common.Money;
import org.example.common.StoreException;

public final class ConfiguredProduct implements CartItem {
    public static final List<Option> BRANDS = List.of(
            new Option("Gucci", 5000),
            new Option("Louis Vuitton", 10000),
            new Option("Yves Saint Laurent", 7500));
    public static final List<Option> MATERIALS = List.of(
            new Option("Cotton", 0),
            new Option("Wool", 3000),
            new Option("Polyester", 1000),
            new Option("Leather", 7000));
    private final CatalogProduct product;
    private final Option brand;
    private final Option material;

    private ConfiguredProduct(CatalogProduct product, Option brand, Option material) {
        this.product = product;
        this.brand = brand;
        this.material = material;
    }

    @Override
    public String id() {
        return product.id();
    }

    public String brand() {
        return brand.name();
    }

    public String material() {
        return material.name();
    }

    @Override
    public String description() {
        return product.name() + " (" + brand.name() + ", " + material.name() + ")";
    }

    @Override
    public BigDecimal productPrice(BigDecimal basePrice) {
        return basePrice.add(BigDecimal.valueOf(brand.surcharge() + material.surcharge()));
    }

    @Override
    public BigDecimal extrasPrice() {
        return Money.ZERO;
    }

    public record Option(String name, int surcharge) {}

    public static final class Builder {
        private final CatalogProduct product;
        private String brand = "Gucci";
        private String material = "Cotton";

        public Builder(CatalogProduct product) {
            this.product = product;
        }

        public Builder brand(String value) {
            brand = value;
            return this;
        }

        public Builder material(String value) {
            material = value;
            return this;
        }

        public ConfiguredProduct build() {
            return new ConfiguredProduct(product, find(BRANDS, brand, "brand"), find(MATERIALS, material, "material"));
        }

        private static Option find(List<Option> options, String name, String kind) {
            return options.stream()
                    .filter(option -> option.name().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new StoreException("Client Error: Choose a supported " + kind + "."));
        }
    }
}
