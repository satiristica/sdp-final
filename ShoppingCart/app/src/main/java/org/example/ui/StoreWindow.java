package org.example.ui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.example.StoreApplication;
import org.example.common.Money;

public final class StoreWindow extends Application {
    private final StoreApplication store = new StoreApplication();

    @Override
    public void start(Stage stage) {
        Label title = new Label("E-Commerce Shopping Cart");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        ListView<String> catalog = new ListView<>(FXCollections.observableArrayList(
                store.catalog().products().stream()
                        .map(product -> product.name() + " - " + Money.format(product.basePrice()))
                        .toList()));

        BorderPane root = new BorderPane(catalog);
        root.setTop(title);
        root.setPadding(new Insets(16));
        BorderPane.setMargin(title, new Insets(0, 0, 12, 0));

        stage.setTitle("Shopping Cart");
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }
}