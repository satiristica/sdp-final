package org.example.ui;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.StoreApplication;
import org.example.cart.CartLine;
import org.example.catalog.CatalogProduct;
import org.example.common.Money;
import org.example.common.StoreException;
import org.example.discount.NoDiscount;
import org.example.discount.PercentageDiscount;
import org.example.product.CartItem;
import org.example.product.ConfiguredProduct;
import org.example.product.GiftWrap;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class StoreWindow extends Application {
    private final StoreApplication store = new StoreApplication();

    private final ObservableList<CatalogProduct> catalogItems = FXCollections.observableArrayList();
    private final ObservableList<CartLine> cartItems = FXCollections.observableArrayList();

    private TableView<CatalogProduct> catalogTable;
    private TableView<CartLine> cartTable;

    private ComboBox<String> brandBox;
    private ComboBox<String> materialBox;
    private Spinner<Integer> qtySpinner;
    private CheckBox giftWrapCheckBox;
    private TextField discountField;
    private Label clothingLabel;
    private Label wrappingLabel;
    private Label discountLabel;
    private Label totalLabel;
    private TextArea logArea;

    @Override
    public void start(Stage stage) {
        Label title = new Label("🛍️ Mr Robot Clothing Shop");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label subtitle = new Label("Design Patterns Storefront • Adapter, Observer, Builder, Decorator, Strategy & Facade");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        Button refreshBtn = new Button("🔄 Refresh Catalog");
        refreshBtn.setStyle(secondaryButtonStyle());
        refreshBtn.setOnAction(e -> run(this::handleRefresh));

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        VBox titleBox = new VBox(2, title, subtitle);
        HBox header = new HBox(15, titleBox, headerSpacer, refreshBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 15, 0));

        SplitPane splitPane = new SplitPane(createCatalogSection(), createCartSection());
        splitPane.setDividerPositions(0.48);
        splitPane.setStyle("-fx-background-color: transparent;");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(4);
        logArea.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px; -fx-control-inner-background: #0f172a; -fx-text-fill: #38bdf8;");
        log("System initialized. Welcome to Mr Robot Clothing Shop!");

        VBox bottomBox = new VBox(6, sectionHeader("📜 Store Activity & Checkout Log"), logArea);
        bottomBox.setPadding(new Insets(12, 0, 0, 0));

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(splitPane);
        root.setBottom(bottomBox);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f1f5f9;");

        showCatalog();
        showCart();

        Scene scene = new Scene(root, 1080, 740);
        stage.setTitle("Mr Robot Clothing Shop — GUI");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createCatalogSection() {
        catalogTable = new TableView<>(catalogItems);
        catalogTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        catalogTable.setStyle("-fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px;");

        TableColumn<CatalogProduct, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().name()));

        TableColumn<CatalogProduct, String> priceCol = new TableColumn<>("Base Price");
        priceCol.setCellValueFactory(p -> new SimpleStringProperty(Money.format(p.getValue().basePrice())));
        priceCol.setMaxWidth(140);

        catalogTable.getColumns().setAll(List.of(nameCol, priceCol));
        VBox.setVgrow(catalogTable, Priority.ALWAYS);

        brandBox = optionBox(ConfiguredProduct.BRANDS);
        materialBox = optionBox(ConfiguredProduct.MATERIALS);

        HBox optionRow = new HBox(12, boldLabel("Brand:"), brandBox, boldLabel("Material:"), materialBox);
        optionRow.setAlignment(Pos.CENTER_LEFT);
        optionRow.setPadding(new Insets(8));
        optionRow.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 6px;");

        qtySpinner = new Spinner<>(1, 50, 1);
        qtySpinner.setPrefWidth(75);

        giftWrapCheckBox = new CheckBox("🎁 Add Gift Wrap (+" + Money.format(GiftWrap.PRICE) + " per item)");
        giftWrapCheckBox.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");

        HBox configRow = new HBox(12, boldLabel("Qty:"), qtySpinner, giftWrapCheckBox);
        configRow.setAlignment(Pos.CENTER_LEFT);
        configRow.setPadding(new Insets(8));
        configRow.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 6px;");

        Button addBtn = new Button("➕ Configure & Add to Cart");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setStyle(primaryButtonStyle("#10b981"));
        addBtn.setOnAction(e -> run(this::handleAddToCart));

        VBox card = new VBox(12, sectionHeader("📦 Available Catalog"), catalogTable, optionRow, configRow, addBtn);
        card.setPadding(new Insets(15));
        card.setStyle(cardStyle());
        return card;
    }

    private VBox createCartSection() {
        cartTable = new TableView<>(cartItems);
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        cartTable.setPlaceholder(new Label("Your shopping cart is empty"));
        cartTable.setStyle("-fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px;");

        TableColumn<CartLine, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().item().description()));

        TableColumn<CartLine, String> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().quantity())));
        qtyCol.setMaxWidth(60);

        TableColumn<CartLine, String> totalCol = new TableColumn<>("Line Total");
        totalCol.setCellValueFactory(c -> new SimpleStringProperty(
                Money.format(c.getValue().productTotal().add(c.getValue().extrasTotal()))));
        totalCol.setMaxWidth(140);

        cartTable.getColumns().setAll(List.of(itemCol, qtyCol, totalCol));
        VBox.setVgrow(cartTable, Priority.ALWAYS);

        Button removeBtn = new Button("🗑️ Remove Selected");
        removeBtn.setStyle(secondaryButtonStyle());
        removeBtn.setOnAction(e -> run(this::handleRemoveSelected));

        HBox itemActions = new HBox(10, removeBtn);
        itemActions.setAlignment(Pos.CENTER_RIGHT);

        discountField = new TextField();
        discountField.setPromptText("Discount % (e.g. 10)");
        discountField.setPrefWidth(150);

        Button applyDiscountBtn = new Button("🏷️ Apply Discount");
        applyDiscountBtn.setStyle(secondaryButtonStyle());
        applyDiscountBtn.setOnAction(e -> run(this::handleApplyDiscount));

        HBox discountBox = new HBox(10, new Label("Promo Discount:"), discountField, applyDiscountBtn);
        discountBox.setAlignment(Pos.CENTER_LEFT);
        discountBox.setPadding(new Insets(8));
        discountBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 6px;");

        clothingLabel = new Label();
        clothingLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");

        wrappingLabel = new Label();
        wrappingLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");

        discountLabel = new Label();
        discountLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 13px;");

        totalLabel = new Label();
        totalLabel.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox totalsBox = new VBox(4, clothingLabel, wrappingLabel, discountLabel, totalLabel);
        totalsBox.setAlignment(Pos.CENTER_RIGHT);

        Button checkoutBtn = new Button("💳 Proceed to Checkout");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setStyle(primaryButtonStyle("#3b82f6"));
        checkoutBtn.setOnAction(e -> run(this::handleCheckout));

        VBox card = new VBox(12, sectionHeader("🛒 Shopping Cart & Checkout"), cartTable, itemActions, discountBox, totalsBox, checkoutBtn);
        card.setPadding(new Insets(15));
        card.setStyle(cardStyle());
        return card;
    }

    private void handleRefresh() {
        List<String> updates = store.refreshCatalog();
        updates.forEach(update -> log("Price update: " + update));
        log("Catalog refreshed (" + updates.size() + " price updates in your cart).");
        showCatalog();
        showCart();
    }

    private void handleAddToCart() {
        CatalogProduct selected = catalogTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new StoreException("Client Error: Select a product from the catalog first.");
        }
        CartItem item = new ConfiguredProduct.Builder(selected)
                .brand(selectedName(brandBox, ConfiguredProduct.BRANDS))
                .material(selectedName(materialBox, ConfiguredProduct.MATERIALS))
                .build();
        if (giftWrapCheckBox.isSelected()) {
            item = new GiftWrap(item);
        }
        int quantity = qtySpinner.getValue();
        store.addToCart(item, quantity);

        qtySpinner.getValueFactory().setValue(1);
        giftWrapCheckBox.setSelected(false);
        showCart();
        log("Added to cart: " + quantity + "x " + item.description());
    }

    private void handleRemoveSelected() {
        int index = cartTable.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            throw new StoreException("Client Error: Select an item in your cart to remove.");
        }
        String description = cartItems.get(index).item().description();
        store.removeFromCart(index + 1);
        showCart();
        log("Removed from cart: " + description);
    }

    private void handleApplyDiscount() {
        String percent = discountField.getText().trim();
        if (percent.isEmpty()) {
            store.applyDiscount(new NoDiscount());
        } else {
            store.applyDiscount(new PercentageDiscount(Integer.parseInt(percent)));
        }
        showCart();
        log("Discount: " + store.discountDescription());
    }

    private void handleCheckout() {
        store.startCheckout();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Pay " + Money.format(store.cartTotals().total()) + "?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Confirm Purchase");
        confirm.setHeaderText(null);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            log("Purchase cancelled. Your cart was kept.");
            return;
        }
        String reference = store.completeCheckout();
        discountField.clear();
        showCart();
        log("Simulated payment approved: " + reference);
        showAlert(Alert.AlertType.INFORMATION, "Order Confirmed", "Simulated payment approved: " + reference);
    }

    private void showCatalog() {
        catalogItems.setAll(store.products());
        catalogTable.getSelectionModel().selectFirst();
    }

    private void showCart() {
        cartItems.setAll(store.cartLines());
        var totals = store.cartTotals();
        clothingLabel.setText("Clothing: " + Money.format(totals.products()));
        wrappingLabel.setText("Wrapping: " + Money.format(totals.extras()));
        discountLabel.setText("Discount: " + store.discountDescription() + " (-" + Money.format(totals.discount()) + ")");
        totalLabel.setText("Total: " + Money.format(totals.total()));
    }

    private void run(Runnable action) {
        try {
            action.run();
        } catch (StoreException exception) {
            showAlert(Alert.AlertType.ERROR, "Error", exception.getMessage());
        } catch (NumberFormatException exception) {
            showAlert(Alert.AlertType.ERROR, "Error", "Client Error: Enter a whole number.");
        }
    }

    private ComboBox<String> optionBox(List<ConfiguredProduct.Option> options) {
        ComboBox<String> box = new ComboBox<>(FXCollections.observableArrayList(
                options.stream().map(option -> option.name() + " (+" + option.surcharge() + ")").toList()));
        box.getSelectionModel().selectFirst();
        return box;
    }

    private String selectedName(ComboBox<String> box, List<ConfiguredProduct.Option> options) {
        return options.get(box.getSelectionModel().getSelectedIndex()).name();
    }

    private void log(String message) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.appendText("[" + time + "] " + message + "\n");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Label boldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        return label;
    }

    private Label sectionHeader(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        return label;
    }

    private String cardStyle() {
        return "-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-background-radius: 8px;";
    }

    private String primaryButtonStyle(String hexColor) {
        return "-fx-background-color: " + hexColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 10px 16px; -fx-background-radius: 6px; -fx-cursor: hand;";
    }

    private String secondaryButtonStyle() {
        return "-fx-background-color: #e2e8f0; -fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 7px 12px; -fx-background-radius: 6px; -fx-cursor: hand;";
    }
}
