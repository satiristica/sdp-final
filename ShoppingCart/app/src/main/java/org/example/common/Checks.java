package org.example.common;

public final class Checks {
    private Checks() {}

    public static String text(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new StoreException("Server Error: " + field + " must not be blank.");
        }
        return value.trim();
    }

    public static int positive(int value, String field) {
        if (value <= 0) {
            throw new StoreException("Client Error: " + field + " must be greater than zero.");
        }
        return value;
    }
}
