package org.example.product;

import java.math.BigDecimal;
import java.util.List;
import org.example.catalog.CatalogProduct;
import org.example.common.Money;
import org.example.common.StoreException;

public final class ConfiguredProduct implements CartItem {
    public static final List<String> BRANDS = List.of("Gucci", "Louis Vuitton", "Yves Saint Laurent");
    public static final List<String> MATERIALS = List.of("Cotton", "Wool", "Polyester", "Leather");
    private final CatalogProduct product;
    private final String brand;
    private final String material;

    private ConfiguredProduct(Builder builder) {
        product = builder.product;
        brand = builder.brand;
        material = builder.material;
    }

    @Override
    public String id() {
        return product.id();
    }

    public String brand() {
        return brand;
    }

    public String material() {
        return material;
    }

    @Override
    public String description() {
        return product.name() + " (" + brand + ", " + material + ")";
    }

    @Override
    public BigDecimal productPrice(BigDecimal basePrice) {
        int brandExtra = switch (brand) {
            case "Gucci" -> 5000;
            case "Louis Vuitton" -> 10000;
            default -> 7500;
        };
        int materialExtra = switch (material) {
            case "Wool" -> 3000;
            case "Polyester" -> 1000;
            case "Leather" -> 7000;
            default -> 0;
        };
        return basePrice.add(BigDecimal.valueOf(brandExtra + materialExtra));
    }

    @Override
    public BigDecimal extrasPrice() {
        return Money.ZERO;
    }

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
            if (!BRANDS.contains(brand)) {
                throw new StoreException("Client Error: Choose a supported brand.");
            }

            if (!MATERIALS.contains(material)) {
                throw new StoreException("Client Error: Choose a supported material.");
            }
            return new ConfiguredProduct(this);

        }

    }
}
