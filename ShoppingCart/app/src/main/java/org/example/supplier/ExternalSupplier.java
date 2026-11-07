package org.example.supplier;

public class ExternalSupplier {
    private boolean firstRequest = true;

    public String[] fetchItems() {
        if (firstRequest) {
            firstRequest = false;
            return new String[] {
                "Coat: 40000", "Turtleneck: 15000", "Sneakers: 30000", "T-shirt: 10000"
            };
        }
        return new String[] {
            "Coat: 45000", "Turtleneck: 17000", "Sneakers: 32000", "T-shirt: 12000"
        };
    }
}
