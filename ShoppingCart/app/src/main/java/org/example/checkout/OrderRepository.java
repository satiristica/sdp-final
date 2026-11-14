package org.example.checkout;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.example.common.StoreException;

public final class OrderRepository {
    private final Connection connection;

    public OrderRepository(String url) {
        try {
            connection = DriverManager.getConnection(url);
            try (var statement = connection.createStatement()) {
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS orders (
                            id INTEGER PRIMARY KEY,
                            payment_reference TEXT NOT NULL,
                            total TEXT NOT NULL,
                            created_at TEXT NOT NULL)""");
            }
        } catch (SQLException exception) {
            throw new StoreException("Server Error: Could not open the order database.");
        }
    }

    public void save(String paymentReference, BigDecimal total) {
        try (var statement = connection.prepareStatement(
                "INSERT INTO orders (payment_reference, total, created_at) VALUES (?, ?, ?)")) {
            statement.setString(1, paymentReference);
            statement.setString(2, total.toPlainString());
            statement.setString(3, LocalDateTime.now().withNano(0).toString());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new StoreException("Server Error: Could not save the order.");
        }
    }
}
