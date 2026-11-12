package org.example;

import org.example.adapter.SupplierCatalogAdapter;
import org.example.cart.ShoppingCart;
import org.example.catalog.CatalogService;
import org.example.checkout.PaymentService;
import org.example.discount.PercentageDiscount;
import org.example.product.ConfiguredProduct;
import org.example.product.GiftWrap;
import org.example.supplier.ExternalSupplier;
import org.example.common.Money;

public final class Demos {
    private Demos() {}

    public static void main(String[] args) {
        CatalogService catalog = new CatalogService(new SupplierCatalogAdapter(new ExternalSupplier()));
        ShoppingCart cart = new ShoppingCart(catalog);
        catalog.addObserver(cart);
        PaymentService payments = new PaymentService();

        ConfiguredProduct coat = new ConfiguredProduct.Builder(catalog.find("COAT"))
                .brand("Gucci")
                .material("Wool")
                .build();
        cart.add(new GiftWrap(coat), 1);
        cart.applyDiscount(new PercentageDiscount(10));

        System.out.println("Checkout without the facade");
        ShoppingCart.Totals totals = cart.totals();
        String paymentReference = payments.pay(totals.total());
        cart.clear();

        System.out.println("TOTAL: " + Money.format(totals.total()));
        System.out.println("Simulated payment approved: " + paymentReference);
        System.out.println("Cart is empty: " + cart.isEmpty());
    }
}
