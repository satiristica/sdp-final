package org.example.product;

import java.math.BigDecimal;
import org.example.common.Money;

public final class GiftWrap extends ItemDecorator {
    public static final BigDecimal PRICE = Money.amount("1500");
    
    public GiftWrap(CartItem item) { 
        super(item); 
    }

    
    @Override
    public String description() { 
        return super.description() + " + gift wrapping"; 
    }


    @Override
    public BigDecimal extrasPrice() { 
        return super.extrasPrice().add(PRICE); 
    }
}
