package org.example.ui;

import java.io.Reader;
import java.util.NoSuchElementException;
import org.example.StoreApplication;
import org.example.catalog.CatalogProduct;
import org.example.common.StoreException;
import org.example.discount.NoDiscount;
import org.example.discount.PercentageDiscount;
import org.example.product.CartItem;
import org.example.product.ConfiguredProduct;
import org.example.product.GiftWrap;


public final class ConsoleStore {
    private final StoreApplication store;
    private final ConsoleInput input;
    private final StoreView view;

    public ConsoleStore(StoreApplication store, Reader reader) {
        this.store = store;
        input = new ConsoleInput(reader);
        view = new StoreView();
    }

    public void run() {
        try {
            while (true) {
                showNotifications();
                view.menu("Mr Robot Clothing Shop", "1. View catalog", "2. Configure and add product",
                        "3. View cart", "4. Apply discount", "5. Refresh catalog", "6. Checkout", "7. Exit");
                String choice = input.text("Select an option");
                if (choice.equals("7")) { break; }
                handle(choice);
            }
        } catch (NoSuchElementException ignored) {
        }
        System.out.println("Goodbye!");
    }

    private void handle(String choice) {
        switch (choice) {
            case "1" -> view.catalog(store.catalog().products());
            case "2" -> configureAndAdd();
            case "3" -> manageCart();
            case "4" -> selectDiscount();
            case "5" -> {
                store.catalog().refresh();
                System.out.println("Supplier catalog refreshed.");
                view.catalog(store.catalog().products());
            }
            case "6" -> checkout();
            default -> throw new StoreException("Client Error: Choose a number from 1 to 7.");
        }
    }

    private void configureAndAdd() {
        var products = store.catalog().products();
        view.catalog(products);
        CatalogProduct product = products.get(input.choice("Product number", products.size()));
        view.menu("Brand", "1. Gucci (+5000 KZT)", "2. Louis Vuitton (+10000 KZT)",
                "3. Yves Saint Laurent (+7500 KZT)");
        String brand = ConfiguredProduct.BRANDS.get(input.choice("Brand number", 3));
        view.menu("Material", "1. Cotton (+0 KZT)", "2. Wool (+3000 KZT)",
                "3. Polyester (+1000 KZT)", "4. Leather (+7000 KZT)");
        String material = ConfiguredProduct.MATERIALS.get(input.choice("Material number", 4));
        CartItem item = new ConfiguredProduct.Builder(product).brand(brand).material(material).build();
        if (input.confirm("Add gift wrapping for 1500 KZT per item?")) { item = new GiftWrap(item); }
        store.cart().add(item, input.integer("Quantity"));
        System.out.println("Product added.");
        view.cart(store.cart());
    }


    private void manageCart() {
        view.cart(store.cart());
        if (store.cart().isEmpty()) { return; }
        view.menu("Cart actions", "1. Remove item", "0. Back");
        switch (input.text("Select an option")) {
            case "1" -> store.cart().remove(input.integer("Cart item number"));
            case "0" -> { return; }
            default -> throw new StoreException("Client Error: Choose 1 or 0.");
        }
        view.cart(store.cart());
    }

    private void selectDiscount() {
        view.menu("Discount", "1. No discount", "2. 10% off clothing");
        switch (input.text("Select an option")) {
            case "1" -> store.cart().applyDiscount(new NoDiscount());
            case "2" -> store.cart().applyDiscount(new PercentageDiscount(10));
            default -> throw new StoreException("Client Error: Choose 1 or 2.");
        }
        view.cart(store.cart());
    }

    private void checkout() {
        if (store.cart().isEmpty()) { 
            throw new StoreException("Client Error: Add a product before checkout."); 
        }
        
        view.cart(store.cart());
        
        if (input.confirm("Confirm purchase?")) {
            view.paymentApproved(store.checkout().checkout());
        } else {
            System.out.println("Purchase cancelled. Your cart was kept.");
        }
    }

    private void showNotifications() {
        for (String message : store.cart().notifications()) {
            System.out.println("Price update: " + message);
        }
        store.cart().clearNotifications();
    }
}
