package org.example.checkout;

import org.example.cart.ShoppingCart;
import org.example.common.StoreException;

public final class CheckoutFacade {
    private final ShoppingCart cart;
    private final PaymentService payments;

    public CheckoutFacade(ShoppingCart cart, PaymentService payments) {
        this.cart = cart;
        this.payments = payments;
    }

    public String checkout() {
        if (cart.isEmpty()) {
            throw new StoreException("Client Error: Add a product before checkout.");
        }
        String paymentReference = payments.pay(cart.totals().total());
        cart.clear();
        return paymentReference;
    }
}
