package org.example.product;

import java.math.BigDecimal;
import java.util.Objects;

public abstract class ItemDecorator implements CartItem {
    private final CartItem wrapped;
    
    protected ItemDecorator(CartItem wrapped) { 
        this.wrapped = Objects.requireNonNull(wrapped); 
    }


    @Override
    public String id() { 
        return wrapped.id(); 
    }


    @Override
    public String description() { 
        return wrapped.description(); 
    }


    @Override
    public BigDecimal productPrice(BigDecimal currentBasePrice) { 
        return wrapped.productPrice(currentBasePrice); 
    }


    @Override
    public BigDecimal extrasPrice() { 
        return wrapped.extrasPrice(); 
    }
}
