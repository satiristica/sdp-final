package org.example.ui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.StoreApplication;
import org.example.common.Money;
import org.example.catalog.CatalogProduct;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class StoreWindow extends Application {
    private final StoreApplication store = new StoreApplication();

    public static class UiCartItem {
        private final String name;
        private final int quantity;
        private final boolean giftWrapped;
        private final BigDecimal unitPrice;

        public UiCartItem(String name, int quantity, boolean giftWrapped, BigDecimal unitPrice) {
            this.name = name;
            this.quantity = quantity;
            this.giftWrapped = giftWrapped;
            this.unitPrice = unitPrice;
        }

        public String getDescription() {
            return name + (giftWrapped ? " [ Gift Wrap]" : "");
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getLineTotal() {
            BigDecimal wrapCost = giftWrapped ? new BigDecimal("1500.00") : BigDecimal.ZERO;
            return unitPrice.add(wrapCost).multiply(BigDecimal.valueOf(quantity));
        }
    }

    private final ObservableList<CatalogProduct> catalogItems = FXCollections.observableArrayList();
    private final ObservableList<UiCartItem> cartItems = FXCollections.observableArrayList();

    private TableView<CatalogProduct> catalogTable;
    private TableView<UiCartItem> cartTable;

    private Spinner<Integer> qtySpinner;
    private CheckBox giftWrapCheckBox;
    private TextField discountField;
    private Label subtotalLabel;
    private Label discountLabel;
    private Label totalLabel;
    private TextArea logArea;

    private BigDecimal discountPercent = BigDecimal.ZERO;


    @Override
    public void start(Stage stage) {
        Label title = new Label("🛍️ Mr Robot Clothing Shop");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label subtitle = new Label("Design Patterns Storefront • Catalog, Builder, Decorator, Strategy & Facade");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        Button refreshBtn = new Button("🔄 Refresh Catalog");
        refreshBtn.setStyle(secondaryButtonStyle());
        refreshBtn.setOnAction(e -> loadCatalog(true));

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

        loadCatalog(false);
        recalculateTotals();

        Scene scene = new Scene(root, 1080, 740);
        stage.setTitle("Mr Robot Clothing Shop — GUI");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createCatalogSection() {
        catalogTable = new TableView<>(catalogItems);
        catalogTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        catalogTable.setStyle("-fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px;");

        TableColumn<CatalogProduct, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().name()));

        TableColumn<CatalogProduct, String> priceCol = new TableColumn<>("Base Price");
        priceCol.setCellValueFactory(p -> new SimpleStringProperty(Money.format(p.getValue().basePrice())));
        priceCol.setMaxWidth(140);

        catalogTable.getColumns().addAll(nameCol, priceCol);
        VBox.setVgrow(catalogTable, Priority.ALWAYS);

        Label qtyLbl = new Label("Qty:");
        qtyLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

        qtySpinner = new Spinner<>(1, 50, 1);
        qtySpinner.setPrefWidth(75);

        giftWrapCheckBox = new CheckBox("🎁 Add Gift Wrap (+1,500.00 KZT)");
        giftWrapCheckBox.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");

        HBox configRow = new HBox(12, qtyLbl, qtySpinner, giftWrapCheckBox);
        configRow.setAlignment(Pos.CENTER_LEFT);
        configRow.setPadding(new Insets(8));
        configRow.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 6px;");

        Button addBtn = new Button("➕ Configure & Add to Cart");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setStyle(primaryButtonStyle("#10b981"));
        addBtn.setOnAction(e -> handleAddToCart());

        VBox card = new VBox(12, sectionHeader("📦 Available Catalog"), catalogTable, configRow, addBtn);
        card.setPadding(new Insets(15));
        card.setStyle(cardStyle());
        return card;
    }

    private VBox createCartSection() {
        cartTable = new TableView<>(cartItems);
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        cartTable.setPlaceholder(new Label("Your shopping cart is empty"));
        cartTable.setStyle("-fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px;");

        TableColumn<UiCartItem, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));

        TableColumn<UiCartItem, String> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));
        qtyCol.setMaxWidth(60);

        TableColumn<UiCartItem, String> totalCol = new TableColumn<>("Line Total");
        totalCol.setCellValueFactory(c -> new SimpleStringProperty(formatKzt(c.getValue().getLineTotal())));
        totalCol.setMaxWidth(140);

        cartTable.getColumns().addAll(itemCol, qtyCol, totalCol);
        VBox.setVgrow(cartTable, Priority.ALWAYS);

        Button removeBtn = new Button("🗑️ Remove Selected");
        removeBtn.setStyle(secondaryButtonStyle());
        removeBtn.setOnAction(e -> handleRemoveSelected());

        Button clearBtn = new Button("Clear Cart");
        clearBtn.setStyle(secondaryButtonStyle());
        clearBtn.setOnAction(e -> {
            cartItems.clear();
            discountPercent = BigDecimal.ZERO;
            discountField.clear();
            recalculateTotals();
            log("Cart cleared.");
        });

        HBox itemActions = new HBox(10, removeBtn, clearBtn);
        itemActions.setAlignment(Pos.CENTER_RIGHT);

        discountField = new TextField();
        discountField.setPromptText("Discount % (e.g. 10)");
        discountField.setPrefWidth(150);

        Button applyDiscountBtn = new Button("🏷️ Apply Discount");
        applyDiscountBtn.setStyle(secondaryButtonStyle());
        applyDiscountBtn.setOnAction(e -> handleApplyDiscount());

        HBox discountBox = new HBox(10, new Label("Promo Discount:"), discountField, applyDiscountBtn);
        discountBox.setAlignment(Pos.CENTER_LEFT);
        discountBox.setPadding(new Insets(8));
        discountBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 6px;");

        subtotalLabel = new Label("Subtotal: 0.00 KZT");
        subtotalLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");

        discountLabel = new Label("Discount (0%): -0.00 KZT");
        discountLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 13px;");

        totalLabel = new Label("Total: 0.00 KZT");
        totalLabel.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox totalsBox = new VBox(4, subtotalLabel, discountLabel, totalLabel);
        totalsBox.setAlignment(Pos.CENTER_RIGHT);

        Button checkoutBtn = new Button("💳 Proceed to Checkout");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setStyle(primaryButtonStyle("#3b82f6"));
        checkoutBtn.setOnAction(e -> handleCheckout());

        VBox card = new VBox(12, sectionHeader("🛒 Shopping Cart & Checkout"), cartTable, itemActions, discountBox, totalsBox, checkoutBtn);
        card.setPadding(new Insets(15));
        card.setStyle(cardStyle());
        return card;
    }

    private void loadCatalog(boolean notify) {
        catalogItems.setAll(store.catalog().products());
        if (!catalogItems.isEmpty()) {
            catalogTable.getSelectionModel().selectFirst();
        }
        if (notify) {
            log("Catalog refreshed from store sources (" + catalogItems.size() + " items loaded).");
        }
    }

    private void handleAddToCart() {
        CatalogProduct selected = catalogTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Product Selected", "Please select a product from the catalog first.");
            return;
        }

        int qty = qtySpinner.getValue();
        boolean wrap = giftWrapCheckBox.isSelected();
        BigDecimal price = extractNumericAmount(Money.format(selected.basePrice()));

        UiCartItem item = new UiCartItem(selected.name(), qty, wrap, price);
        cartItems.add(item);

        qtySpinner.getValueFactory().setValue(1);
        giftWrapCheckBox.setSelected(false);

        recalculateTotals();
        log("Added to cart: " + qty + "x " + item.getDescription() + " (" + formatKzt(item.getLineTotal()) + ")");
    }

    private void handleRemoveSelected() {
        UiCartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Item Selected", "Select an item in your cart to remove.");
            return;
        }
        cartItems.remove(selected);
        recalculateTotals();
        log("Removed from cart: " + selected.getDescription());
    }

    private void handleApplyDiscount() {
        String raw = discountField.getText().trim();
        if (raw.isEmpty()) {
            discountPercent = BigDecimal.ZERO;
            recalculateTotals();
            log("Discount cleared.");
            return;
        }

        try {
            BigDecimal val = new BigDecimal(raw);
            if (val.compareTo(BigDecimal.ZERO) < 0 || val.compareTo(new BigDecimal("100")) > 0) {
                showAlert(Alert.AlertType.ERROR, "Invalid Discount", "Please enter a percentage between 0 and 100.");
                return;
            }
            discountPercent = val;
            recalculateTotals();
            log("Applied discount strategy: " + discountPercent + "% off.");
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Enter a valid number (e.g., 15).");
        }
    }

    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Cart", "Your cart is empty. Add items before checking out.");
            return;
        }

        BigDecimal subtotal = getSubtotal();
        BigDecimal discountAmount = getDiscountAmount(subtotal);
        BigDecimal finalTotal = subtotal.subtract(discountAmount);

        StringBuilder receipt = new StringBuilder();
        receipt.append("=== ORDER RECEIPT ===\n");
        for (UiCartItem item : cartItems) {
            receipt.append(String.format(" • %dx %s — %s\n",
                    item.getQuantity(), item.getDescription(), formatKzt(item.getLineTotal())));
        }
        receipt.append("---------------------\n");
        receipt.append("Subtotal: ").append(formatKzt(subtotal)).append("\n");
        receipt.append("Discount (").append(discountPercent).append("%): -").append(formatKzt(discountAmount)).append("\n");
        receipt.append("TOTAL PAID: ").append(formatKzt(finalTotal));

        log("Checkout completed! Total charged: " + formatKzt(finalTotal));
        showAlert(Alert.AlertType.INFORMATION, "Order Confirmed", receipt.toString());

        cartItems.clear();
        discountPercent = BigDecimal.ZERO;
        discountField.clear();
        recalculateTotals();
    }

    private void recalculateTotals() {
        BigDecimal subtotal = getSubtotal();
        BigDecimal discountAmount = getDiscountAmount(subtotal);
        BigDecimal total = subtotal.subtract(discountAmount);

        subtotalLabel.setText("Subtotal: " + formatKzt(subtotal));
        discountLabel.setText("Discount (" + discountPercent + "%): -" + formatKzt(discountAmount));
        totalLabel.setText("Total: " + formatKzt(total));
    }

    private BigDecimal getSubtotal() {
        return cartItems.stream()
                .map(UiCartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getDiscountAmount(BigDecimal subtotal) {
        if (discountPercent.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return subtotal.multiply(discountPercent)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal extractNumericAmount(String formattedMoney) {
        String cleaned = formattedMoney.replaceAll("[^0-9.]", "");
        return cleaned.isEmpty() ? BigDecimal.ZERO : new BigDecimal(cleaned);
    }

    private String formatKzt(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP) + " KZT";
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