package org.example.ui;

import java.util.List;
import org.example.cart.CartLine;
import org.example.cart.ShoppingCart;
import org.example.catalog.CatalogProduct;
import org.example.common.Money;

public final class StoreView {

    public void menu(String title, String... options) {
        System.out.println("\n" + title + "\n");

        for (String option : options) { 
            System.out.println(option); 
        }

        System.out.println();
    }


    public void catalog(List<CatalogProduct> products) {
        System.out.println("\nCatalog\n");
        for (int index = 0; index < products.size(); index++) {
            CatalogProduct product = products.get(index);
            System.out.println((index + 1) + ". " + product.name() + " - " + Money.format(product.basePrice()));
        }
        System.out.println("Brand and material surcharges are added to the base price.");
    }


    public void cart(ShoppingCart cart) {
        
        if (cart.isEmpty()) { 
            System.out.println("Your cart is empty."); 
            return; 
        }

        System.out.println("\nYour cart\n");
        for (CartLine line : cart.lines()) {
            System.out.println(line.number() + ". " + line.item().description() + " x" + line.quantity());
            System.out.println("   Clothing: " + Money.format(line.productTotal())
                    + "; gift wrapping: " + Money.format(line.extrasTotal()));
        }
        System.out.println("Discount: " + cart.discount().description());
        totals(cart.totals());
    }


    public void totals(ShoppingCart.Totals totals) {
        System.out.println("Clothing: " + Money.format(totals.products()));
        System.out.println("Wrapping: " + Money.format(totals.extras()));
        System.out.println("Discount: -" + Money.format(totals.discount()));
        System.out.println("TOTAL: " + Money.format(totals.total()));
    }


    public void paymentApproved(String paymentReference) {
        System.out.println("Simulated payment approved: " + paymentReference);
    }
}
