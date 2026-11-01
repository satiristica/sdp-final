package org.example.supplier;

public class ExternalSupplier {
    private boolean firstRequest = true;

    public String[] fetchItems() {
        String coatPrice = "45000";
        String turtleneckPrice = "17000";
        String sneakersPrice = "32000";
        String tShirtPrice = "12000";

        if (firstRequest) {
            coatPrice = "40000";
            turtleneckPrice = "15000";
            sneakersPrice = "30000";
            tShirtPrice = "10000";
            firstRequest = false;
        }

        return new String[] {
            "Coat: " + coatPrice,
            "Turtleneck: " + turtleneckPrice,
            "Sneakers: " + sneakersPrice,
            "T-shirt: " + tShirtPrice
        };
    }
}
