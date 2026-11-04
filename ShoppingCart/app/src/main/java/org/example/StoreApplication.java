package org.example;

import org.example.adapter.SupplierCatalogAdapter;
import org.example.cart.ShoppingCart;
import org.example.catalog.CatalogService;
import org.example.checkout.CheckoutFacade;
import org.example.checkout.PaymentService;
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

    public CatalogService catalog() { 
        return catalog; 
    }
    
    public ShoppingCart cart() { 
        return cart; 
    }
    
    public CheckoutFacade checkout() { 
        return checkout; 
    }
}
