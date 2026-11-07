package org.example.checkout;

import java.math.BigDecimal;
import org.example.common.Money;

public final class PaymentService {
    private int nextPayment = 1;

    public String pay(BigDecimal amount) {
        System.out.println("Paid: " + Money.format(amount));
        return "PAY-" + nextPayment++;
    }
}
