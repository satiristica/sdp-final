package org.example.ui;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

public final class StoreWindow extends Application {
    private final StoreApplication store = new StoreApplication();
    private final String stylesheet = StoreWindow.class.getResource("store.css").toExternalForm();

    private final ObservableList<CatalogProduct> catalogItems = FXCollections.observableArrayList();
    private final ObservableList<CartLine> cartItems = FXCollections.observableArrayList();

    private TableView<CatalogProduct> catalogTable;
    private TableView<CartLine> cartTable;

    private ComboBox<String> brandBox;
    private ComboBox<String> materialBox;
    private Spinner<Integer> qtySpinner;
    private CheckBox giftWrapCheckBox;
    private Label previewLabel;
    private Label cartCountLabel;
    private TextField discountField;
    private Label clothingValue;
    private Label wrappingValue;
    private Label discountName;
    private Label discountValue;
    private Label totalValue;
    private TextArea logArea;

    @Override
    public void start(Stage stage) {
        Label title = styled(new Label("🛍️ Mr Robot Clothing Shop"), "title");
        Label subtitle = styled(new Label("Design Patterns Storefront • Adapter, Observer, Builder, Decorator, Strategy & Facade"), "subtitle");

        Button refreshBtn = button("🔄 Refresh Catalog", "secondary", this::handleRefresh);
        refreshBtn.setTooltip(new Tooltip("Load the latest supplier prices"));

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        HBox header = new HBox(15, new VBox(2, title, subtitle), headerSpacer, refreshBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 16, 0));

        SplitPane splitPane = new SplitPane(createCatalogSection(), createCartSection());
        splitPane.setDividerPositions(0.48);

        logArea = styled(new TextArea(), "log");
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(4);
        log("Welcome to Mr Robot Clothing Shop!");

        VBox bottomBox = new VBox(6, styled(new Label("📜 Store Activity"), "section-header"), logArea);
        bottomBox.setPadding(new Insets(14, 0, 0, 0));

        BorderPane root = new BorderPane(splitPane, header, null, bottomBox, null);
        root.setPadding(new Insets(20));

        showCatalog();
        showCart();

        Scene scene = new Scene(root, 1100, 800);
        scene.getStylesheets().add(stylesheet);
        stage.setTitle("Mr Robot Clothing Shop");
        stage.setMinWidth(920);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    private VBox createCatalogSection() {
        catalogTable = new TableView<>(catalogItems);
        catalogTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        catalogTable.getColumns().setAll(List.of(
                column("Product", CatalogProduct::name),
                fixedWidth(column("Base Price", product -> Money.format(product.basePrice())), 140)));
        catalogTable.setRowFactory(table -> {
            TableRow<CatalogProduct> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    run(this::handleAddToCart);
                }
            });
            return row;
        });
        VBox.setVgrow(catalogTable, Priority.ALWAYS);

        brandBox = optionBox(ConfiguredProduct.BRANDS);
        materialBox = optionBox(ConfiguredProduct.MATERIALS);

        qtySpinner = new Spinner<>(1, 50, 1);
        qtySpinner.setPrefWidth(80);

        giftWrapCheckBox = new CheckBox("🎁 Gift wrap (+" + Money.format(GiftWrap.PRICE) + " each)");

        HBox quantityRow = new HBox(16, qtySpinner, giftWrapCheckBox);
        quantityRow.setAlignment(Pos.CENTER_LEFT);

        GridPane options = styled(new GridPane(), "panel");
        options.setHgap(12);
        options.setVgap(8);
        options.addRow(0, fieldLabel("Brand"), brandBox);
        options.addRow(1, fieldLabel("Material"), materialBox);
        options.addRow(2, fieldLabel("Quantity"), quantityRow);
        options.getColumnConstraints().addAll(new ColumnConstraints(), growingColumn());

        previewLabel = styled(new Label(), "price-preview");
        previewLabel.setWrapText(true);
        previewLabel.setMinHeight(Region.USE_PREF_SIZE);
        List.<ObservableValue<?>>of(
                catalogTable.getSelectionModel().selectedItemProperty(),
                brandBox.valueProperty(),
                materialBox.valueProperty(),
                qtySpinner.valueProperty(),
                giftWrapCheckBox.selectedProperty())
                .forEach(property -> property.addListener((observable, before, after) -> showPreview()));

        Button addBtn = button("➕ Add to Cart", "primary", this::handleAddToCart);
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.disableProperty().bind(catalogTable.getSelectionModel().selectedItemProperty().isNull());

        return styled(new VBox(12, sectionHeader("📦 Catalog"), catalogTable, options, previewLabel, addBtn), "card");
    }

    private VBox createCartSection() {
        cartTable = new TableView<>(cartItems);
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        cartTable.setPlaceholder(new Label("Your cart is empty — pick something from the catalog."));

        cartTable.getColumns().setAll(List.of(
                column("Item", line -> line.item().description()),
                fixedWidth(column("Qty", line -> String.valueOf(line.quantity())), 60),
                fixedWidth(column("Line Total", line -> Money.format(line.productTotal().add(line.extrasTotal()))), 130)));
        VBox.setVgrow(cartTable, Priority.ALWAYS);

        cartCountLabel = styled(new Label(), "badge");
        HBox cartHeader = new HBox(10, sectionHeader("🛒 Cart"), cartCountLabel);
        cartHeader.setAlignment(Pos.CENTER_LEFT);

        discountField = new TextField();
        discountField.setPromptText("Discount %");
        discountField.setPrefColumnCount(7);
        discountField.setOnAction(e -> run(this::handleApplyDiscount));

        Button applyDiscountBtn = button("🏷️ Apply", "secondary", this::handleApplyDiscount);
        applyDiscountBtn.setTooltip(new Tooltip("Leave empty to remove the discount"));

        Button removeBtn = button("🗑️ Remove", "secondary", this::handleRemoveSelected);
        removeBtn.disableProperty().bind(cartTable.getSelectionModel().selectedItemProperty().isNull());

        Region actionSpacer = new Region();
        HBox.setHgrow(actionSpacer, Priority.ALWAYS);

        HBox actions = styled(new HBox(10, fieldLabel("Discount"), discountField, applyDiscountBtn, actionSpacer, removeBtn), "panel");
        actions.setAlignment(Pos.CENTER_LEFT);

        clothingValue = styled(new Label(), "totals-value");
        wrappingValue = styled(new Label(), "totals-value");
        discountName = styled(new Label(), "discount-line");
        discountValue = styled(new Label(), "discount-line");
        totalValue = styled(new Label(), "total-value");

        GridPane totals = styled(new GridPane(), "totals");
        totals.setVgap(5);
        totals.addRow(0, styled(new Label("Clothing"), "totals-label"), clothingValue);
        totals.addRow(1, styled(new Label("Gift wrapping"), "totals-label"), wrappingValue);
        totals.addRow(2, discountName, discountValue);
        totals.add(new Separator(), 0, 3, 2, 1);
        totals.addRow(4, styled(new Label("Total"), "total-value"), totalValue);
        totals.getColumnConstraints().addAll(growingColumn(), rightColumn());

        Button checkoutBtn = button("💳 Checkout", "dark", this::handleCheckout);
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.disableProperty().bind(Bindings.isEmpty(cartItems));

        return styled(new VBox(12, cartHeader, cartTable, actions, totals, checkoutBtn), "card");
    }

    private void handleRefresh() {
        List<String> updates = store.refreshCatalog();
        updates.forEach(update -> log("Price update: " + update));
        log("Catalog refreshed (" + updates.size() + " price updates in your cart).");
        showCatalog();
        showCart();
    }

    private void handleAddToCart() {
        CartItem item = configuredItem();
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
        Alert confirm = dialog(Alert.AlertType.CONFIRMATION, "Confirm Purchase",
                "Pay " + Money.format(store.cartTotals().total()) + " for " + cartCountLabel.getText() + "?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            log("Purchase cancelled. Your cart was kept.");
            return;
        }
        String reference = store.completeCheckout();
        discountField.clear();
        showCart();
        log("Simulated payment approved: " + reference);
        dialog(Alert.AlertType.INFORMATION, "Order Confirmed", "Simulated payment approved: " + reference).showAndWait();
    }

    private CartItem configuredItem() {
        CatalogProduct product = catalogTable.getSelectionModel().getSelectedItem();
        if (product == null) {
            throw new StoreException("Client Error: Select a product from the catalog first.");
        }
        CartItem item = new ConfiguredProduct.Builder(product)
                .brand(selectedName(brandBox, ConfiguredProduct.BRANDS))
                .material(selectedName(materialBox, ConfiguredProduct.MATERIALS))
                .build();
        return giftWrapCheckBox.isSelected() ? new GiftWrap(item) : item;
    }

    private void showPreview() {
        CatalogProduct product = catalogTable.getSelectionModel().getSelectedItem();
        if (product == null) {
            previewLabel.setText("Select a product to see its price.");
            return;
        }
        CartItem item = configuredItem();
        BigDecimal each = item.productPrice(product.basePrice()).add(item.extrasPrice());
        int quantity = qtySpinner.getValue();
        String forAll = quantity > 1 ? " · " + Money.format(each.multiply(BigDecimal.valueOf(quantity))) + " for " + quantity : "";
        previewLabel.setText(item.description() + "\n" + Money.format(each) + " each" + forAll);
    }

    private void showCatalog() {
        catalogItems.setAll(store.products());
        catalogTable.getSelectionModel().selectFirst();
    }

    private void showCart() {
        cartItems.setAll(store.cartLines());
        int count = cartItems.stream().mapToInt(CartLine::quantity).sum();
        cartCountLabel.setText(count + (count == 1 ? " item" : " items"));

        var totals = store.cartTotals();
        clothingValue.setText(Money.format(totals.products()));
        wrappingValue.setText(Money.format(totals.extras()));
        discountName.setText("Discount: " + store.discountDescription());
        discountValue.setText("-" + Money.format(totals.discount()));
        totalValue.setText(Money.format(totals.total()));
    }

    private void run(Runnable action) {
        try {
            action.run();
        } catch (StoreException exception) {
            dialog(Alert.AlertType.ERROR, "Error", exception.getMessage()).showAndWait();
        } catch (NumberFormatException exception) {
            dialog(Alert.AlertType.ERROR, "Error", "Client Error: Enter a whole number.").showAndWait();
        }
    }

    private Alert dialog(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type, content);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.getDialogPane().getStylesheets().add(stylesheet);
        return alert;
    }

    private void log(String message) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.appendText("[" + time + "] " + message + "\n");
    }

    private <T> TableColumn<T, String> column(String title, Function<T, String> value) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new SimpleStringProperty(value.apply(cell.getValue())));
        return column;
    }

    private <T> TableColumn<T, String> fixedWidth(TableColumn<T, String> column, double width) {
        column.setMinWidth(width);
        column.setPrefWidth(width);
        column.setMaxWidth(width);
        return column;
    }

    private ComboBox<String> optionBox(List<ConfiguredProduct.Option> options) {
        ComboBox<String> box = new ComboBox<>(FXCollections.observableArrayList(
                options.stream().map(option -> option.name() + " (+" + option.surcharge() + ")").toList()));
        box.getSelectionModel().selectFirst();
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private String selectedName(ComboBox<String> box, List<ConfiguredProduct.Option> options) {
        return options.get(box.getSelectionModel().getSelectedIndex()).name();
    }

    private Button button(String text, String style, Runnable action) {
        Button button = styled(new Button(text), style);
        button.setOnAction(e -> run(action));
        return button;
    }

    private Label sectionHeader(String text) {
        return styled(new Label(text), "section-header");
    }

    private Label fieldLabel(String text) {
        Label label = styled(new Label(text), "field-label");
        label.setMinWidth(Region.USE_PREF_SIZE);
        return label;
    }

    private <T extends Node> T styled(T node, String style) {
        node.getStyleClass().add(style);
        return node;
    }

    private ColumnConstraints growingColumn() {
        ColumnConstraints column = new ColumnConstraints();
        column.setHgrow(Priority.ALWAYS);
        return column;
    }

    private ColumnConstraints rightColumn() {
        ColumnConstraints column = new ColumnConstraints();
        column.setHalignment(HPos.RIGHT);
        return column;
    }
}
