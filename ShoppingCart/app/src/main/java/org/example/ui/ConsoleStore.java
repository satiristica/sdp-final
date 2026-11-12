package org.example.ui;

import java.io.Reader;
import java.util.Scanner;
import org.example.StoreApplication;
import org.example.common.Money;
import org.example.common.StoreException;
import org.example.discount.NoDiscount;
import org.example.discount.PercentageDiscount;
import org.example.product.CartItem;
import org.example.product.ConfiguredProduct;
import org.example.product.GiftWrap;

public final class ConsoleStore {
    private static final String MENU = """

            Mr Robot Clothing Shop
            1. View catalog
            2. Configure and add product
            3. View cart
            4. Apply discount
            5. Refresh catalog
            6. Checkout
            7. Exit""";

    private final StoreApplication store;
    private final Scanner scanner;

    public ConsoleStore(StoreApplication store, Reader reader) {
        this.store = store;
        scanner = new Scanner(reader);
    }

    public void run() {
        while (true) {
            System.out.println(MENU);
            String choice = text("Select an option");
            if (choice.equals("7")) { break; }
            handle(choice);
        }
        System.out.println("Goodbye!");
    }

    private void handle(String choice) {
        switch (choice) {
            case "1" -> showCatalog();
            case "2" -> configureAndAdd();
            case "3" -> manageCart();
            case "4" -> selectDiscount();
            case "5" -> {
                store.refreshCatalog().forEach(update -> System.out.println("Price update: " + update));
                showCatalog();
            }
            case "6" -> checkout();
            default -> throw new StoreException("Client Error: Choose a number from 1 to 7.");
        }
    }

    private void configureAndAdd() {
        showCatalog();
        var products = store.products();
        var product = products.get(choice("Product number", products.size()));
        System.out.println("Brand: 1. Gucci (+5000)  2. Louis Vuitton (+10000)  3. Yves Saint Laurent (+7500)");
        String brand = ConfiguredProduct.BRANDS.get(choice("Brand number", ConfiguredProduct.BRANDS.size())).name();
        System.out.println("Material: 1. Cotton (+0)  2. Wool (+3000)  3. Polyester (+1000)  4. Leather (+7000)");
        String material = ConfiguredProduct.MATERIALS.get(choice("Material number", ConfiguredProduct.MATERIALS.size())).name();
        CartItem item = new ConfiguredProduct.Builder(product).brand(brand).material(material).build();
        if (confirm("Add gift wrapping for " + Money.format(GiftWrap.PRICE) + " per item?")) {
            item = new GiftWrap(item);
        }
        store.addToCart(item, integer("Quantity"));
        showCart();
    }

    private void manageCart() {
        showCart();
        if (store.cartIsEmpty()) { return; }
        System.out.println("1. Remove item  0. Back");
        switch (text("Select an option")) {
            case "1" -> store.removeFromCart(integer("Cart item number"));
            case "0" -> { return; }
            default -> throw new StoreException("Client Error: Choose 1 or 0.");
        }
        showCart();
    }

    private void selectDiscount() {
        System.out.println("1. No discount  2. 10% off clothing");
        switch (text("Select an option")) {
            case "1" -> store.applyDiscount(new NoDiscount());
            case "2" -> store.applyDiscount(new PercentageDiscount(10));
            default -> throw new StoreException("Client Error: Choose 1 or 2.");
        }
        showCart();
    }

    private void checkout() {
        store.startCheckout();
        showCart();
        if (confirm("Confirm purchase?")) {
            System.out.println("Simulated payment approved: " + store.completeCheckout());
        } else {
            System.out.println("Purchase cancelled. Your cart was kept.");
        }
    }

    private void showCatalog() {
        var products = store.products();
        for (int index = 0; index < products.size(); index++) {
            var product = products.get(index);
            System.out.println((index + 1) + ". " + product.name() + " - " + Money.format(product.basePrice()));
        }
    }

    private void showCart() {
        if (store.cartIsEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        var lines = store.cartLines();
        for (int index = 0; index < lines.size(); index++) {
            var line = lines.get(index);
            System.out.println((index + 1) + ". " + line.item().description() + " x" + line.quantity());
        }
        var totals = store.cartTotals();
        System.out.println("Clothing: " + Money.format(totals.products()));
        System.out.println("Wrapping: " + Money.format(totals.extras()));
        System.out.println("Discount: " + store.discountDescription()
                + " (-" + Money.format(totals.discount()) + ")");
        System.out.println("TOTAL: " + Money.format(totals.total()));
    }

    private String text(String prompt) {
        System.out.println(prompt + ":");
        return scanner.nextLine().trim();
    }

    private int integer(String prompt) {
        return Integer.parseInt(text(prompt));
    }

    private int choice(String prompt, int count) {
        int number = integer(prompt);
        if (number < 1 || number > count) {
            throw new StoreException("Client Error: Choose a number from 1 to " + count + ".");
        }
        return number - 1;
    }

    private boolean confirm(String prompt) {
        String answer = text(prompt + " [y/n]");
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }
}