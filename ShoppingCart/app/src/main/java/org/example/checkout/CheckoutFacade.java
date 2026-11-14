package org.example.checkout;

import java.math.BigDecimal;
import org.example.cart.ShoppingCart;
import org.example.common.StoreException;

public final class CheckoutFacade {
    private final ShoppingCart cart;
    private final PaymentService payments;
    private final OrderRepository orders;

    public CheckoutFacade(ShoppingCart cart, PaymentService payments, OrderRepository orders) {
        this.cart = cart;
        this.payments = payments;
        this.orders = orders;
    }

    public void requireItems() {
        if (cart.isEmpty()) {
            throw new StoreException("Client Error: Add a product before checkout.");
        }
    }

    public String checkout() {
        requireItems();
        BigDecimal total = cart.totals().total();
        String paymentReference = payments.pay(total);
        orders.save(paymentReference, total);
        cart.clear();
        return paymentReference;
    }
}
