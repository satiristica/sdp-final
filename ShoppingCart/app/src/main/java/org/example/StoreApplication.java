package org.example;

import java.util.List;
import org.example.adapter.SupplierCatalogAdapter;
import org.example.cart.CartLine;
import org.example.cart.ShoppingCart;
import org.example.catalog.CatalogProduct;
import org.example.catalog.CatalogService;
import org.example.checkout.CheckoutFacade;
import org.example.checkout.PaymentService;
import org.example.discount.DiscountStrategy;
import org.example.product.CartItem;
import org.example.supplier.ExternalSupplier;

public final class StoreApplication {
    private final CatalogService catalog;
    private final ShoppingCart cart;
    private final CheckoutFacade checkout;

    public StoreApplication() {
        catalog = new CatalogService(new SupplierCatalogAdapter(new ExternalSupplier()));
        cart = new ShoppingCart(catalog);
        checkout = new CheckoutFacade(cart, new PaymentService());
    }

    CatalogService catalog() {
        return catalog;
    }

    ShoppingCart cart() {
        return cart;
    }

    CheckoutFacade checkout() {
        return checkout;
    }

    public List<CatalogProduct> products() {
        return catalog.products();
    }

    public List<String> refreshCatalog() {
        catalog.refresh();
        return cart.takePriceUpdates();
    }

    public void addToCart(CartItem item, int quantity) {
        cart.add(item, quantity);
    }

    public void removeFromCart(int number) {
        cart.remove(number);
    }

    public void applyDiscount(DiscountStrategy strategy) {
        cart.applyDiscount(strategy);
    }

    public boolean cartIsEmpty() {
        return cart.isEmpty();
    }

    public List<CartLine> cartLines() {
        return cart.lines();
    }

    public ShoppingCart.Totals cartTotals() {
        return cart.totals();
    }

    public String discountDescription() {
        return cart.discount().description();
    }

    public void startCheckout() {
        checkout.requireItems();
    }

    public String completeCheckout() {
        return checkout.checkout();
    }
}
