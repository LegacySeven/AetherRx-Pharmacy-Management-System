package com.pharmacy.controller;

import com.pharmacy.model.CartItem;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.Optional;

/**
 * Core Controller for the Pharmacy Management System.
 * Manages the global state, navigation routing, the Point of Sale (POS) cart,
 * transaction history, and dynamic view rendering via Node caching.
 */
public class MainController {

    // --- Root Layout ---
    @FXML
    private BorderPane rootPane;

    // --- Navigation Buttons ---
    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnInventory;
    @FXML
    private Button btnSales;
    @FXML
    private Button btnTransactions;

    // --- Dashboard: Statistics Labels ---
    @FXML
    private Label lblTotalMedicines;
    @FXML
    private Label lblLowStock;
    @FXML
    private Label lblOutofStock;
    @FXML
    private Label lblTotalValuation;

    // --- Dashboard: Revenue Chart ---
    @FXML
    private javafx.scene.chart.LineChart<String, Number> revenueChart;

    // --- Dashboard: Alerts Box ---
    @FXML
    private VBox dashboardAlertsBox;

    // --- Layout Containers (for page swapping) ---
    @FXML
    private VBox centerContent;

    // --- Internal State & Observables ---
    /**
     * The master dataset holding all current pharmacy inventory. Shared across
     * views for live updates.
     */
    private final ObservableList<Medicine> masterData = FXCollections.observableArrayList();

    // --- UI Node Caching ---
    // These variables store the built layouts for each page.
    // This caching prevents redundant UI recreation and eliminates
    // NullPointerExceptions when switching views.
    private Node dashboardCenter;
    private Node inventoryCenter, inventoryRight;
    private Node salesCenter, salesRight;
    private Node transactionsCenter, transactionsRight;

    // Inventory sync references
    private VBox inventoryAlertsBox;
    private Label lblInvLowStock;
    private Label lblInvOutOfStock;

    // Currently active nav button
    private Button activeNavButton;

    // Cart and transaction data
    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private final ObservableList<Transaction> transactionHistory = FXCollections.observableArrayList();
    private Label cartTotalLabel;
    private int txnCounter = 0;

    // Transaction History stat labels (removed from class level, demoted to local)

    /**
     * Called automatically after the FXML is loaded.
     * Initializes the analytics dashboard, populates statistics,
     * and caches the initial scene graph nodes.
     */
    @FXML
    public void initialize() {
        // 1. Load data from database
        loadSampleData();

        // 2. Update dashboard statistics, chart, and alerts
        updateDashboardStatistics();

        // 3. Save dashboard node for page switching
        dashboardCenter = centerContent;
        activeNavButton = btnDashboard;

        // 4. Load transaction data
        loadSampleTransactions();

        // 5. Enforce Role-Based Access Control (RBAC)
        if (com.pharmacy.util.UserSession.isCashier()) {
            // Visually indicate restricted pages with lock icons
            btnInventory.setText("\uD83D\uDD12 Inventory");
            btnInventory.setOpacity(0.5);
        }
    }

    // ============================================================
    // SAMPLE DATA GENERATION & INITIALIZATION
    // ============================================================

    /**
     * Loads the master inventory data from the SQLite database.
     * This populates the `masterData` observable list which is shared across the UI.
     */
    private void loadSampleData() {
        // 1. Fetch medicine records from the database and populate the in-memory observable list
        masterData.setAll(com.pharmacy.util.DatabaseManager.loadMedicines());
    }

    /**
     * Loads historical transaction data from the SQLite database.
     * Also calculates the highest transaction ID to properly initialize the txnCounter.
     */
    private void loadSampleTransactions() {
        // 1. Fetch all previous transactions and populate the history list
        transactionHistory.setAll(com.pharmacy.util.DatabaseManager.loadTransactions());
        
        // 2. If transactions exist, find the highest numeric ID to prevent ID collisions
        if (!transactionHistory.isEmpty()) {
            int maxId = 0;
            for (Transaction t : transactionHistory) {
                try {
                    // Extract the numeric portion of "TXN001" -> 1
                    int id = Integer.parseInt(t.getTxnId().replace("TXN", ""));
                    if (id > maxId)
                        maxId = id;
                } catch (Exception ignored) {
                    // Ignore parsing errors for malformed IDs
                }
            }
            // 3. Set the global counter to the highest found ID
            txnCounter = maxId;
        } else {
            // 3. Default to 0 if no transactions exist
            txnCounter = 0;
        }
    }

    // ============================================================
    // NAVIGATION & ROUTING
    // ============================================================

    @FXML
    private void handleNavDashboard() {
        switchPage("dashboard");
    }

    @FXML
    private void handleNavInventory() {
        switchPage("inventory");
    }

    @FXML
    private void handleNavSales() {
        switchPage("sales");
    }

    @FXML
    private void handleNavTransactions() {
        switchPage("transactions");
    }

    /**
     * Central page-switching engine. Swaps center and right content,
     * updates navigation button active states.
     */
    private void switchPage(String page) {
        // RBAC: Block Cashier from restricted pages
        if (com.pharmacy.util.UserSession.isCashier() && "inventory".equals(page)) {
            showAlert("Access Denied",
                    "\uD83D\uDD12 This section requires Administrator privileges.\n\nPlease contact your Admin to access this feature.",
                    Alert.AlertType.WARNING);
            return;
        }

        // Update active button highlight
        Button targetButton;
        switch (page) {
            case "inventory":
                targetButton = btnInventory;
                break;
            case "sales":
                targetButton = btnSales;
                break;
            case "transactions":
                targetButton = btnTransactions;
                break;
            default:
                targetButton = btnDashboard;
                break;
        }
        setActiveNavButton(targetButton);

        // Swap content
        switch (page) {
            case "dashboard":
                rootPane.setCenter(dashboardCenter);
                rootPane.setRight(null);
                updateDashboardStatistics();
                break;
            case "inventory":
                if (inventoryCenter == null)
                    buildInventoryPage();
                rootPane.setCenter(inventoryCenter);
                rootPane.setRight(inventoryRight);
                break;
            case "sales":
                // Always rebuild to reflect current inventory
                buildSalesPage();
                rootPane.setCenter(salesCenter);
                rootPane.setRight(salesRight);
                break;
            case "transactions":
                if (transactionsCenter == null)
                    buildTransactionHistoryPage();
                rootPane.setCenter(transactionsCenter);
                rootPane.setRight(transactionsRight);
                break;
        }
    }

    private boolean isDarkTheme = false;

    @FXML
    private void handleToggleTheme() {
        isDarkTheme = !isDarkTheme;
        if (isDarkTheme) {
            rootPane.getStyleClass().add("dark-theme");
            ((Button) rootPane.lookup("#btnToggleTheme")).setText("☀️ Toggle Light Mode");
        } else {
            rootPane.getStyleClass().remove("dark-theme");
            ((Button) rootPane.lookup("#btnToggleTheme")).setText("🌗 Toggle Dark Mode");
        }
    }

    @FXML
    private void handleLogout() {
        // Clear the active user session
        com.pharmacy.util.UserSession.logout();

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/pharmacy/view/login.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Login \u2014 Pharmacy Management System");

            java.net.URL iconUrl = getClass().getResource("/com/pharmacy/icon.png");
            if (iconUrl != null) {
                stage.getIcons().add(new javafx.scene.image.Image(iconUrl.toExternalForm()));
            }

            javafx.scene.Scene scene = new javafx.scene.Scene(root, 500, 450);

            java.net.URL cssUrl = getClass().getResource("/com/pharmacy/style/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

            ((javafx.stage.Stage) rootPane.getScene().getWindow()).close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the visual styling of the navigation menu to reflect the active page.
     * Removes the 'nav-button-active' CSS class from the previously selected button 
     * and applies it to the newly selected button.
     *
     * @param btn The navigation button that was clicked.
     */
    private void setActiveNavButton(Button btn) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("nav-button-active");
        }
        btn.getStyleClass().add("nav-button-active");
        activeNavButton = btn;
    }

    // ============================================================
    // DASHBOARD LOGIC
    // ============================================================

    /**
     * Recalculates statistics for the dashboard panel (Total Medicines, Valuation,
     * Out of Stock, etc.).
     * This is automatically called whenever an item is bought or restocked to
     * ensure real-time accuracy.
     */
    private void updateDashboardStatistics() {
        int total = masterData.size();
        int lowStock = 0;
        int outOfStock = 0;
        double totalValuation = 0.0;

        for (Medicine m : masterData) {
            totalValuation += (m.getStock() * m.getPrice());
            if (m.getStock() == 0) {
                outOfStock++;
            } else if (m.getStock() < 30) {
                lowStock++;
            }
        }

        lblTotalMedicines.setText(String.valueOf(total));
        lblLowStock.setText(String.valueOf(lowStock));
        lblOutofStock.setText(String.valueOf(outOfStock));
        lblTotalValuation.setText(String.format("\u20B5%.2f", totalValuation));

        // Update revenue chart
        populateRevenueChart();

        // Refresh dashboard alerts
        refreshDashboardAlerts();

        // Sync Inventory page stats dynamically
        if (lblInvLowStock != null)
            lblInvLowStock.setText(String.valueOf(lowStock));
        if (lblInvOutOfStock != null)
            lblInvOutOfStock.setText(String.valueOf(outOfStock));
        if (inventoryAlertsBox != null)
            refreshInventoryAlerts();
    }

    /**
     * Populates the dashboard revenue line chart with data from the last 7 days.
     */
    private void populateRevenueChart() {
        if (revenueChart == null)
            return;
        revenueChart.getData().clear();

        java.util.Map<String, Double> revenue = com.pharmacy.util.DatabaseManager.getRevenueByDay(7);

        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Daily Revenue");

        for (java.util.Map.Entry<String, Double> entry : revenue.entrySet()) {
            // Show only the month-day portion for cleaner labels
            String label = entry.getKey().substring(5); // "MM-dd"
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(label, entry.getValue()));
        }

        revenueChart.getData().add(series);
    }

    /**
     * Refreshes the low stock and out of stock alerts on the Dashboard.
     */
    private void refreshDashboardAlerts() {
        if (dashboardAlertsBox == null)
            return;
        dashboardAlertsBox.getChildren().clear();

        Label alertTitle = new Label("\u26A0 Low Stock & Out of Stock Alerts");
        alertTitle.getStyleClass().addAll("stat-label", "text-warning");
        alertTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        dashboardAlertsBox.getChildren().add(alertTitle);

        boolean hasAlerts = false;
        for (Medicine m : masterData) {
            if (m.getStock() == 0 || m.getStock() < 30) {
                String icon = m.getStock() == 0 ? "\u26D4 " : "\u26A0 ";
                Label alertItem = new Label(icon + m.getName() + " \u2014 " + m.getStock() + " units remaining");
                alertItem.getStyleClass().add("form-label");
                if (m.getStock() == 0)
                    alertItem.getStyleClass().add("text-danger");
                else
                    alertItem.getStyleClass().add("text-warning");
                dashboardAlertsBox.getChildren().add(alertItem);
                hasAlerts = true;
            }
        }

        if (!hasAlerts) {
            Label noAlerts = new Label("✅ All stock levels are healthy!");
            noAlerts.getStyleClass().add("form-label");
            noAlerts.setStyle("-fx-text-fill: -fx-color-green;");
            dashboardAlertsBox.getChildren().add(noAlerts);
        }
    }

    // ============================================================
    // PAGE BUILDERS
    // ============================================================

    /**
     * Constructs the Inventory Manager page programmatically.
     * This layout provides deep stock management capabilities, a detailed
     * TableView,
     * and the "Register Medicine" form on the right panel.
     * The built layout is cached in memory for immediate retrieval upon future
     * navigation.
     */
    private void buildInventoryPage() {
        VBox center = createPageShell("Inventory Manager",
                "Manage stock levels, search medicines, and register new formulations");

        // Stat cards row
        HBox cards = new HBox(15);
        cards.getStyleClass().add("stat-cards-container");

        VBox lowStockCard = createStatCard("Need Reorder", String.valueOf(countLowStock()), "stat-card-amber");
        lblInvLowStock = (Label) lowStockCard.getChildren().get(1);

        VBox outOfStockCard = createStatCard("Out of Stock", String.valueOf(countOutOfStock()), "stat-card-rose");
        lblInvOutOfStock = (Label) outOfStockCard.getChildren().get(1);

        cards.getChildren().addAll(
                createStatCard("Total SKUs", String.valueOf(masterData.size()), "stat-card-teal"),
                lowStockCard,
                outOfStockCard,
                createStatCard("Avg. Unit Price", String.format("\u20B5%.2f", avgPrice()), "stat-card-blue"));
        for (Node card : cards.getChildren()) {
            HBox.setHgrow(card, Priority.ALWAYS);
        }

        // Search and Filter Bar
        HBox searchBar = new HBox(15);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.getStyleClass().add("filter-bar");
        searchBar.setPadding(new Insets(12, 15, 12, 15));

        TextField txtInvSearch = new TextField();
        txtInvSearch.setPromptText("\uD83D\uDD0D Search code, name, category...");
        txtInvSearch.getStyleClass().add("search-field");
        txtInvSearch.setPrefHeight(38);
        HBox.setHgrow(txtInvSearch, Priority.ALWAYS);

        Label filterLabel = new Label("Filter:");
        filterLabel.getStyleClass().add("filter-label");

        ComboBox<String> comboInvFilter = new ComboBox<>();
        comboInvFilter.getStyleClass().add("filter-combo");
        comboInvFilter.setPrefHeight(38);
        comboInvFilter.setPrefWidth(150);
        comboInvFilter.setItems(
                FXCollections.observableArrayList("All", "Analgesics", "Antibiotics", "Cardiovascular", "Antidiabetic",
                        "Vitamins", "Gastrointestinal", "Antihistamines", "Respiratory", "Neurological", "Endocrine"));
        comboInvFilter.setValue("All");

        searchBar.getChildren().addAll(txtInvSearch, filterLabel, comboInvFilter);

        // Detailed inventory table
        TableView<Medicine> detailedTable = new TableView<>();
        detailedTable.getStyleClass().add("modern-table");
        detailedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(detailedTable, Priority.ALWAYS);

        TableColumn<Medicine, String> cCode = new TableColumn<>("Code");
        cCode.setCellValueFactory(c -> c.getValue().codeProperty());
        cCode.setPrefWidth(80);

        TableColumn<Medicine, String> cName = new TableColumn<>("Medicine");
        cName.setCellValueFactory(c -> c.getValue().nameProperty());
        cName.setPrefWidth(180);

        TableColumn<Medicine, String> cCat = new TableColumn<>("Category");
        cCat.setCellValueFactory(c -> c.getValue().categoryProperty());
        cCat.setPrefWidth(120);

        TableColumn<Medicine, Integer> cStock = new TableColumn<>("Current Stock");
        cStock.setCellValueFactory(c -> c.getValue().stockProperty().asObject());
        cStock.setPrefWidth(100);

        TableColumn<Medicine, String> cStatus = new TableColumn<>("Status");
        cStatus.setCellValueFactory(c -> c.getValue().statusProperty());
        cStatus.setPrefWidth(100);

        TableColumn<Medicine, Double> cPrice = new TableColumn<>("Unit Price");
        cPrice.setCellValueFactory(c -> c.getValue().priceProperty().asObject());
        cPrice.setPrefWidth(90);
        cPrice.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("\u20B5%.2f", price));
            }
        });

        TableColumn<Medicine, Double> cValue = new TableColumn<>("Total Value");
        cValue.setCellValueFactory(c -> c.getValue().priceProperty().asObject());
        cValue.setPrefWidth(110);
        cValue.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    Medicine m = getTableView().getItems().get(getIndex());
                    setText(String.format("\u20B5%.2f", m.getStock() * m.getPrice()));
                }
            }
        });

        detailedTable.getColumns().add(cCode);
        detailedTable.getColumns().add(cName);
        detailedTable.getColumns().add(cCat);
        detailedTable.getColumns().add(cStock);
        detailedTable.getColumns().add(cStatus);
        detailedTable.getColumns().add(cPrice);
        detailedTable.getColumns().add(cValue);

        // Filtered list for search/filter
        FilteredList<Medicine> invFiltered = new FilteredList<>(masterData, p -> true);

        txtInvSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            invFiltered.setPredicate(med -> {
                String search = newVal == null ? "" : newVal.toLowerCase().trim();
                String cat = comboInvFilter.getValue();
                if (cat != null && !"All".equals(cat) && !med.getCategory().equalsIgnoreCase(cat))
                    return false;
                if (search.isEmpty())
                    return true;
                return med.getCode().toLowerCase().contains(search) ||
                        med.getName().toLowerCase().contains(search) ||
                        med.getCategory().toLowerCase().contains(search);
            });
        });

        comboInvFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            invFiltered.setPredicate(med -> {
                String search = txtInvSearch.getText() == null ? "" : txtInvSearch.getText().toLowerCase().trim();
                if (newVal != null && !"All".equals(newVal) && !med.getCategory().equalsIgnoreCase(newVal))
                    return false;
                if (search.isEmpty())
                    return true;
                return med.getCode().toLowerCase().contains(search) ||
                        med.getName().toLowerCase().contains(search) ||
                        med.getCategory().toLowerCase().contains(search);
            });
        });

        SortedList<Medicine> invSorted = new SortedList<>(invFiltered);
        invSorted.comparatorProperty().bind(detailedTable.comparatorProperty());
        detailedTable.setItems(invSorted);

        VBox tableWrap = new VBox(detailedTable);
        tableWrap.getStyleClass().add("table-container");
        VBox.setVgrow(tableWrap, Priority.ALWAYS);

        // Footer with Delete + Export CSV buttons
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        Button btnDeleteMed = new Button("\uD83D\uDDD1 Delete Selected");
        btnDeleteMed.getStyleClass().add("action-button-delete");
        btnDeleteMed.setPrefHeight(36);
        btnDeleteMed.setOnAction(e -> {
            Medicine selected = detailedTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Selection Required", "Please select a medicine from the table first.",
                        Alert.AlertType.WARNING);
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Deletion");
            confirm.setHeaderText(null);
            confirm.setContentText(
                    "Are you sure you want to remove " + selected.getName() + " from the inventory database?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    com.pharmacy.util.DatabaseManager.deleteMedicine(selected.getCode());
                    masterData.remove(selected);
                    updateDashboardStatistics();
                    showAlert("Deleted", "Medicine removed successfully.", Alert.AlertType.INFORMATION);
                } catch (java.sql.SQLException ex) {
                    showAlert("Database Error", "Failed to delete from database: " + ex.getMessage(),
                            Alert.AlertType.ERROR);
                }
            }
        });

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        Label footerText = new Label("Inventory is synced with the Dashboard in real-time.");
        footerText.getStyleClass().add("footer-help-text");

        Button btnExportInv = new Button("\uD83D\uDCE5 Export CSV");
        btnExportInv.getStyleClass().add("action-button-add");
        btnExportInv.setPrefHeight(36);
        btnExportInv.setOnAction(e -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) rootPane.getScene().getWindow();
            com.pharmacy.util.CsvExporter.exportInventory(masterData, stage);
            showAlert("Export Complete", "Inventory data exported successfully!", Alert.AlertType.INFORMATION);
        });

        footer.getChildren().addAll(btnDeleteMed, footerSpacer, footerText, btnExportInv);

        center.getChildren().addAll(cards, searchBar, tableWrap, footer);

        // Right panel: Register Medicine + Quick Restock
        VBox right = createRightPanelShell("Register Medicine", "Add a new formulation to inventory");

        // --- Add Medicine Form ---
        VBox fldName = createFormField("Medicine Name", "e.g. Advil 200mg");

        VBox fldCategory = new VBox(5);
        Label lblCat = new Label("Category");
        lblCat.getStyleClass().add("form-label");
        ComboBox<String> comboAddCat = new ComboBox<>();
        comboAddCat.setPromptText("Select Category");
        comboAddCat.getStyleClass().add("form-combo");
        comboAddCat.setMaxWidth(Double.MAX_VALUE);
        comboAddCat.setItems(
                FXCollections.observableArrayList("Analgesics", "Antibiotics", "Cardiovascular", "Antidiabetic",
                        "Vitamins", "Gastrointestinal", "Antihistamines", "Respiratory", "Neurological", "Endocrine"));
        fldCategory.getChildren().addAll(lblCat, comboAddCat);

        VBox fldStock = createFormField("Opening Stock", "e.g. 100");
        VBox fldPrice = createFormField("Unit Price (GHS)", "e.g. 12.99");

        Button btnAddMed = new Button("\u2713 Register Medicine");
        btnAddMed.getStyleClass().add("action-button-add");
        btnAddMed.setMaxWidth(Double.MAX_VALUE);
        btnAddMed.setPrefHeight(44);
        btnAddMed.setOnAction(e -> {
            TextField tName = (TextField) fldName.getChildren().get(1);
            String category = comboAddCat.getValue();
            TextField tStock = (TextField) fldStock.getChildren().get(1);
            TextField tPrice = (TextField) fldPrice.getChildren().get(1);

            String name = tName.getText().trim();
            String stockStr = tStock.getText().trim();
            String priceStr = tPrice.getText().trim();

            if (name.isEmpty() || category == null || stockStr.isEmpty() || priceStr.isEmpty()) {
                showAlert("Input Validation Error", "All fields are required to register a medicine.",
                        Alert.AlertType.ERROR);
                return;
            }

            // Check for duplicate name
            for (Medicine m : masterData) {
                if (m.getName().equalsIgnoreCase(name)) {
                    showAlert("Duplicate Medicine", "A medicine with the name '" + name + "' already exists in the inventory.", Alert.AlertType.ERROR);
                    return;
                }
            }

            // Auto-generate code
            int maxId = 0;
            for (Medicine m : masterData) {
                String c = m.getCode();
                if (c != null && c.startsWith("MED")) {
                    try {
                        int num = Integer.parseInt(c.substring(3));
                        if (num > maxId) maxId = num;
                    } catch (NumberFormatException ignored) {}
                }
            }
            String code = String.format("MED%03d", maxId + 1);

            try {
                int stock = Integer.parseInt(stockStr);
                double price = Double.parseDouble(priceStr);

                if (stock < 0 || price < 0) {
                    showAlert("Input Validation Error", "Stock and Price cannot be negative.", Alert.AlertType.ERROR);
                    return;
                }

                String status = "In Stock";
                if (stock == 0)
                    status = "Out of Stock";
                else if (stock < 30)
                    status = "Low Stock";

                Medicine newMed = new Medicine(code, name, category, stock, status, price);
                try {
                    com.pharmacy.util.DatabaseManager.addMedicine(newMed);
                    masterData.add(newMed);
                } catch (java.sql.SQLException ex) {
                    showAlert("Database Error", "Failed to save medicine to database: " + ex.getMessage(),
                            Alert.AlertType.ERROR);
                    return;
                }

                tName.clear();
                comboAddCat.setValue(null);
                tStock.clear();
                tPrice.clear();
                updateDashboardStatistics();
                showAlert("Success", "Medicine successfully added to inventory!", Alert.AlertType.INFORMATION);

            } catch (NumberFormatException ex) {
                showAlert("Input Validation Error", "Stock must be an integer, and Price must be a decimal value.",
                        Alert.AlertType.ERROR);
            }
        });

        Separator midSep = new Separator();
        midSep.getStyleClass().add("panel-separator");

        // --- Quick Update Section ---
        Label restockTitle = new Label("Quick Update");
        restockTitle.getStyleClass().add("panel-title");
        restockTitle.setStyle("-fx-font-size: 14px;");

        Label lblSelectInfo = new Label("Select an item from the table, then update stock and/or price.");
        lblSelectInfo.getStyleClass().add("form-label");
        lblSelectInfo.setWrapText(true);

        VBox fldNewStock = createFormField("New Stock Quantity", "e.g. 200 (Leave empty to skip)");
        TextField txtNewStock = (TextField) fldNewStock.getChildren().get(1);
        
        VBox fldNewPrice = createFormField("New Unit Price", "e.g. 15.00 (Leave empty to skip)");
        TextField txtNewPrice = (TextField) fldNewPrice.getChildren().get(1);

        Button btnRestock = new Button("\u2713 Update Medicine");
        btnRestock.getStyleClass().add("action-button-add");
        btnRestock.setMaxWidth(Double.MAX_VALUE);
        btnRestock.setPrefHeight(44);
        btnRestock.setOnAction(e -> {
            Medicine selected = detailedTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Selection Required", "Please select a medicine from the table first.",
                        Alert.AlertType.WARNING);
                return;
            }
            
            String stockStr = txtNewStock.getText().trim();
            String priceStr = txtNewPrice.getText().trim();
            
            if (stockStr.isEmpty() && priceStr.isEmpty()) {
                showAlert("No Input", "Please provide a new stock or price to update.", Alert.AlertType.WARNING);
                return;
            }

            try {
                int newStock = selected.getStock();
                double newPrice = selected.getPrice();

                if (!stockStr.isEmpty()) {
                    newStock = Integer.parseInt(stockStr);
                    if (newStock < 0) {
                        showAlert("Invalid Input", "Stock cannot be negative.", Alert.AlertType.ERROR);
                        return;
                    }
                }
                
                if (!priceStr.isEmpty()) {
                    newPrice = Double.parseDouble(priceStr);
                    if (newPrice < 0) {
                        showAlert("Invalid Input", "Price cannot be negative.", Alert.AlertType.ERROR);
                        return;
                    }
                }

                String status = "In Stock";
                if (newStock == 0)
                    status = "Out of Stock";
                else if (newStock < 30)
                    status = "Low Stock";

                try {
                    com.pharmacy.util.DatabaseManager.updateMedicineStockAndPrice(selected.getCode(), newStock, newPrice, status);
                    selected.setStock(newStock);
                    selected.setPrice(newPrice);
                    selected.setStatus(status);
                    detailedTable.refresh();
                } catch (java.sql.SQLException ex) {
                    showAlert("Database Error", "Failed to update database: " + ex.getMessage(),
                            Alert.AlertType.ERROR);
                    return;
                }

                txtNewStock.clear();
                txtNewPrice.clear();
                updateDashboardStatistics();
                showAlert("Success", selected.getName() + " updated successfully.",
                        Alert.AlertType.INFORMATION);
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Ensure stock is a whole number and price is a decimal number.", Alert.AlertType.ERROR);
            }
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Alerts summary in right panel
        inventoryAlertsBox = new VBox(8);
        inventoryAlertsBox.getStyleClass().addAll("stat-card", "stat-card-amber");
        inventoryAlertsBox.setPadding(new Insets(15, 15, 15, 15));
        refreshInventoryAlerts();

        right.getChildren().addAll(
                fldName, fldCategory, fldStock, fldPrice, btnAddMed,
                midSep,
                restockTitle, lblSelectInfo, fldNewStock, fldNewPrice, btnRestock,
                spacer, inventoryAlertsBox);

        inventoryCenter = center;
        inventoryRight = right;
    }

    private void buildSalesPage() {
        VBox center = createPageShell("Point of Sale",
                "Select medicines, set quantity, and process walk-in transactions");

        // ---- Add-to-Cart Bar ----
        HBox addBar = new HBox(12);
        addBar.setAlignment(Pos.CENTER_LEFT);
        addBar.getStyleClass().add("filter-bar");
        addBar.setPadding(new Insets(12, 15, 12, 15));

        // Medicine Search Field
        TextField txtMedSearch = new TextField();
        txtMedSearch.setPromptText("\uD83D\uDD0D Search Med...");
        txtMedSearch.getStyleClass().add("search-field");
        txtMedSearch.setPrefHeight(38);
        txtMedSearch.setPrefWidth(140);

        // Medicine ComboBox — populated from live inventory
        ComboBox<String> comboMedicine = new ComboBox<>();
        comboMedicine.setPromptText("Select Medicine...");
        comboMedicine.getStyleClass().add("filter-combo");
        comboMedicine.setPrefHeight(38);
        comboMedicine.setPrefWidth(260);

        Runnable updateCombo = () -> {
            comboMedicine.getItems().clear();
            String search = txtMedSearch.getText().toLowerCase();
            for (Medicine m : masterData) {
                if (m.getStock() > 0) {
                    String display = m.getCode() + " \u2014 " + m.getName() + "  [\u20B5"
                            + String.format("%.2f", m.getPrice()) + ", Stock: " + m.getStock() + "]";
                    if (search.isEmpty() || display.toLowerCase().contains(search)) {
                        comboMedicine.getItems().add(display);
                    }
                }
            }
            if (!comboMedicine.getItems().isEmpty()) {
                comboMedicine.getSelectionModel().selectFirst();
            }
        };

        txtMedSearch.textProperty().addListener((obs, oldV, newV) -> updateCombo.run());
        updateCombo.run();

        // Quantity field
        TextField txtQty = new TextField();
        txtQty.setPromptText("Qty");
        txtQty.getStyleClass().add("form-field");
        txtQty.setPrefWidth(70);
        txtQty.setPrefHeight(38);

        // Add to Cart button
        Button btnAddToCart = new Button("+ Add to Cart");
        btnAddToCart.getStyleClass().add("action-button-add");
        btnAddToCart.setPrefHeight(38);

        addBar.getChildren().addAll(txtMedSearch, comboMedicine, txtQty, btnAddToCart);

        // ---- Cart Table ----
        TableView<CartItem> cartTable = new TableView<>();
        cartTable.getStyleClass().add("modern-table");
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(cartTable, Priority.ALWAYS);
        cartTable.setItems(cartItems);

        TableColumn<CartItem, String> cMed = new TableColumn<>("Medicine");
        cMed.setCellValueFactory(c -> c.getValue().nameProperty());
        cMed.setPrefWidth(180);

        TableColumn<CartItem, Integer> cQty = new TableColumn<>("Qty");
        cQty.setCellValueFactory(c -> c.getValue().quantityProperty().asObject());
        cQty.setPrefWidth(60);

        TableColumn<CartItem, Double> cUnitP = new TableColumn<>("Unit Price");
        cUnitP.setCellValueFactory(c -> c.getValue().unitPriceProperty().asObject());
        cUnitP.setPrefWidth(90);
        cUnitP.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : String.format("\u20B5%.2f", p));
            }
        });

        TableColumn<CartItem, Double> cSub = new TableColumn<>("Subtotal");
        cSub.setCellValueFactory(c -> c.getValue().subtotalProperty().asObject());
        cSub.setPrefWidth(100);
        cSub.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : String.format("\u20B5%.2f", p));
            }
        });

        TableColumn<CartItem, Void> cRemove = new TableColumn<>("");
        cRemove.setPrefWidth(60);
        cRemove.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("\u2715");
            {
                btn.getStyleClass().add("action-button-delete");
                btn.setStyle("-fx-padding: 3 10; -fx-font-size: 11;");
                btn.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    cartItems.remove(item);
                    updateCartTotal();
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(empty ? null : v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        cartTable.getColumns().add(cMed);
        cartTable.getColumns().add(cQty);
        cartTable.getColumns().add(cUnitP);
        cartTable.getColumns().add(cSub);
        cartTable.getColumns().add(cRemove);

        VBox tableWrap = new VBox(cartTable);
        tableWrap.getStyleClass().add("table-container");
        VBox.setVgrow(tableWrap, Priority.ALWAYS);

        // ---- Footer: Total + Checkout ----
        HBox footerBar = new HBox(15);
        footerBar.setAlignment(Pos.CENTER_LEFT);

        Button btnClear = new Button("\uD83D\uDDD1 Clear Cart");
        btnClear.getStyleClass().add("action-button-delete");
        btnClear.setPrefHeight(40);
        btnClear.setOnAction(e -> {
            cartItems.clear();
            updateCartTotal();
        });

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        // Calculator
        TextField txtTendered = new TextField();
        txtTendered.setPromptText("Tendered (\u20B5)");
        txtTendered.getStyleClass().add("form-field");
        txtTendered.setPrefWidth(120);
        txtTendered.setPrefHeight(40);

        Label lblChange = new Label("Change: \u20B50.00");
        lblChange.getStyleClass().add("form-label");
        lblChange.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        // --- Change Calculator Logic ---
        txtTendered.textProperty().addListener((obs, oldV, newV) -> {
            try {
                double total = getCartTotal();
                if (newV.isEmpty()) {
                    lblChange.setText("Change: \u20B50.00");
                    lblChange.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -fx-text-primary;");
                    return;
                }
                double tendered = Double.parseDouble(newV);
                double change = tendered - total;
                if (change >= 0) {
                    lblChange.setText(String.format("Change: \u20B5%.2f", change));
                    lblChange.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #10b981;"); // green
                } else {
                    lblChange.setText("Insufficient");
                    lblChange.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #ef4444;"); // red
                }
            } catch (NumberFormatException ex) {
                lblChange.setText("Invalid Amt");
                lblChange.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
            }
        });

        Label lblTotalTag = new Label("Total:");
        lblTotalTag.getStyleClass().add("page-title");
        lblTotalTag.setStyle("-fx-font-size: 18px;");

        Label lblCartTotal = new Label("\u20B50.00");
        lblCartTotal.getStyleClass().addAll("stat-value", "text-cyan");
        lblCartTotal.setStyle("-fx-font-size: 28px;");
        cartTotalLabel = lblCartTotal;
        updateCartTotal(); // set initial value

        Button btnCheckout = new Button("\u2713 Complete Sale");
        btnCheckout.getStyleClass().add("action-button-add");
        btnCheckout.setPrefHeight(44);
        btnCheckout.setPrefWidth(170);

        footerBar.getChildren().addAll(btnClear, footerSpacer, txtTendered, lblChange, lblTotalTag, lblCartTotal,
                btnCheckout);

        // ---- Add-to-Cart Action Engine ----
        btnAddToCart.setOnAction(e -> {
            String sel = comboMedicine.getValue();
            if (sel == null) {
                showAlert("No Selection", "Please select a medicine from the dropdown.", Alert.AlertType.WARNING);
                return;
            }
            String code = sel.split(" \u2014 ")[0];

            String qtyStr = txtQty.getText().trim();
            if (qtyStr.isEmpty()) {
                showAlert("No Quantity", "Please enter a quantity.", Alert.AlertType.WARNING);
                return;
            }
            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty <= 0) {
                    showAlert("Invalid Quantity", "Quantity must be greater than zero.", Alert.AlertType.ERROR);
                    return;
                }

                Medicine med = null;
                for (Medicine m : masterData) {
                    if (m.getCode().equals(code)) {
                        med = m;
                        break;
                    }
                }
                if (med == null)
                    return;

                // Check stock availability (account for items already in cart)
                int alreadyInCart = 0;
                for (CartItem ci : cartItems) {
                    if (ci.getCode().equals(med.getCode())) {
                        alreadyInCart = ci.getQuantity();
                    }
                }
                if (qty + alreadyInCart > med.getStock()) {
                    showAlert("Insufficient Stock",
                            med.getName() + " has " + med.getStock() + " in stock"
                                    + (alreadyInCart > 0 ? " (" + alreadyInCart + " already in cart)" : "") + ".",
                            Alert.AlertType.ERROR);
                    return;
                }

                // Add to cart or increase quantity if already present
                boolean found = false;
                for (CartItem ci : cartItems) {
                    if (ci.getCode().equals(med.getCode())) {
                        ci.setQuantity(ci.getQuantity() + qty);
                        cartTable.refresh();
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    cartItems.add(new CartItem(med.getCode(), med.getName(), qty, med.getPrice()));
                }

                txtQty.clear();
                comboMedicine.getSelectionModel().clearSelection();
                updateCartTotal();
            } catch (NumberFormatException ex) {
                showAlert("Invalid Quantity", "Please enter a valid whole number.", Alert.AlertType.ERROR);
            }
        });

        // ---- Checkout Action ----
        btnCheckout.setOnAction(e -> {
            if (cartItems.isEmpty()) {
                showAlert("Empty Cart", "Add items to the cart before completing a sale.", Alert.AlertType.WARNING);
                return;
            }

            // Calculate total
            double total = getCartTotal();

            // Validate tendered amount
            if (!txtTendered.getText().isEmpty()) {
                try {
                    double tendered = Double.parseDouble(txtTendered.getText());
                    if (tendered < total) {
                        showAlert("Insufficient Funds",
                                "Amount tendered (\u20B5" + String.format("%.2f", tendered)
                                        + ") is less than the total (\u20B5" + String.format("%.2f", total) + ").",
                                Alert.AlertType.ERROR);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    showAlert("Invalid Amount", "Please enter a valid amount tendered.", Alert.AlertType.ERROR);
                    return;
                }
            }

            // Record transaction
            txnCounter++;
            String txnId = String.format("TXN%03d", txnCounter);
            Transaction txn = new Transaction(txnId,
                    FXCollections.observableArrayList(cartItems), total);

            try {
                com.pharmacy.util.DatabaseManager.saveTransaction(txn);

                // Deduct stock from inventory
                for (CartItem item : cartItems) {
                    for (Medicine m : masterData) {
                        if (m.getCode().equals(item.getCode())) {
                            int newStock = m.getStock() - item.getQuantity();
                            newStock = Math.max(0, newStock);
                            String status = "In Stock";
                            if (newStock == 0)
                                status = "Out of Stock";
                            else if (newStock < 30)
                                status = "Low Stock";
                            com.pharmacy.util.DatabaseManager.updateMedicineStockAndPrice(m.getCode(), newStock, m.getPrice(), status);

                            m.setStock(newStock);
                            m.setStatus(status);
                            break;
                        }
                    }
                }
                transactionHistory.add(0, txn);
            } catch (java.sql.SQLException ex) {
                showAlert("Database Error", "Failed to save sale to database: " + ex.getMessage(),
                        Alert.AlertType.ERROR);
                return;
            }

            // Clear cart and refresh
            cartItems.clear();
            txtTendered.clear();
            lblChange.setText("Change: \u20B50.00");
            updateCartTotal();
            updateDashboardStatistics();

            // Refresh medicine dropdown to reflect new stock
            txtMedSearch.clear();
            updateCombo.run();

            showAlert("Sale Complete",
                    "Transaction " + txnId + " recorded.\nTotal: \u20B5" + String.format("%.2f", total),
                    Alert.AlertType.INFORMATION);
        });

        center.getChildren().addAll(addBar, tableWrap, footerBar);

        salesCenter = center;
        salesRight = null;
    }

    private void updateCartTotal() {
        double total = getCartTotal();
        if (cartTotalLabel != null) {
            cartTotalLabel.setText("Cart Total: \u20B5" + String.format("%.2f", total));
        }
    }

    private double getCartTotal() {
        double total = 0;
        for (CartItem ci : cartItems)
            total += ci.getSubtotal();
        return total;
    }

    /**
     * Constructs the Transaction History page.
     */
    private void buildTransactionHistoryPage() {
        VBox center = createPageShell("Transaction History",
                "View and manage past sales transactions");

        // 1. Stat Cards Container
        HBox cards = new HBox(15);
        cards.getStyleClass().add("stat-cards-container");

        // 2. Initialize labels for total count and revenue (now strictly local variables)
        Label localTxnTotalCountLabel = new Label("0");
        localTxnTotalCountLabel.getStyleClass().add("stat-value");
        VBox card1 = new VBox(5, new Label("Total Transactions"), localTxnTotalCountLabel);
        card1.getStyleClass().addAll("stat-card", "stat-card-teal");
        card1.setPadding(new Insets(15, 20, 15, 20));
        ((Label) card1.getChildren().get(0)).getStyleClass().add("stat-label");

        Label localTxnTotalRevenueLabel = new Label("\u20B50.00");
        localTxnTotalRevenueLabel.getStyleClass().add("stat-value");
        VBox card2 = new VBox(5, new Label("Total Revenue"), localTxnTotalRevenueLabel);
        card2.getStyleClass().addAll("stat-card", "stat-card-blue");
        card2.setPadding(new Insets(15, 20, 15, 20));
        ((Label) card2.getChildren().get(0)).getStyleClass().add("stat-label");

        // 3. Add cards to the horizontal box layout
        cards.getChildren().addAll(card1, card2);
        for (Node card : cards.getChildren()) {
            HBox.setHgrow(card, Priority.ALWAYS);
        }

        // Search bar
        HBox searchBar = new HBox(15);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.getStyleClass().add("filter-bar");
        searchBar.setPadding(new Insets(12, 15, 12, 15));

        TextField txtTxnSearch = new TextField();
        txtTxnSearch.setPromptText("\uD83D\uDD0D Search transactions by ID...");
        txtTxnSearch.getStyleClass().add("search-field");
        txtTxnSearch.setPrefHeight(38);
        HBox.setHgrow(txtTxnSearch, Priority.ALWAYS);

        ComboBox<String> comboTimeFilter = new ComboBox<>(
                FXCollections.observableArrayList("All Time", "Today", "This Week", "This Month"));
        comboTimeFilter.setValue("All Time");
        comboTimeFilter.getStyleClass().add("form-combo");
        comboTimeFilter.setPrefHeight(38);

        searchBar.getChildren().addAll(txtTxnSearch, comboTimeFilter);

        // Transaction Table
        TableView<Transaction> txnTable = new TableView<>();
        txnTable.getStyleClass().add("modern-table");
        txnTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(txnTable, Priority.ALWAYS);

        TableColumn<Transaction, String> cId = new TableColumn<>("Transaction ID");
        cId.setCellValueFactory(c -> c.getValue().txnIdProperty());

        TableColumn<Transaction, String> cDate = new TableColumn<>("Date & Time");
        cDate.setCellValueFactory(c -> c.getValue().dateTimeProperty());

        TableColumn<Transaction, Integer> cItems = new TableColumn<>("Items");
        cItems.setCellValueFactory(c -> c.getValue().itemCountProperty().asObject());

        TableColumn<Transaction, Double> cTotal = new TableColumn<>("Total Amount");
        cTotal.setCellValueFactory(c -> c.getValue().totalProperty().asObject());
        cTotal.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : String.format("\u20B5%.2f", p));
            }
        });

        txnTable.getColumns().add(cId);
        txnTable.getColumns().add(cDate);
        txnTable.getColumns().add(cItems);
        txnTable.getColumns().add(cTotal);

        FilteredList<Transaction> filteredTxns = new FilteredList<>(transactionHistory, p -> true);

        java.util.function.Predicate<Transaction> filterLogic = t -> {
            String search = txtTxnSearch.getText().toLowerCase();
            if (!t.getTxnId().toLowerCase().contains(search))
                return false;

            String timeFilter = comboTimeFilter.getValue();
            if ("All Time".equals(timeFilter))
                return true;

            try {
                java.time.LocalDateTime txnDate = java.time.LocalDateTime.parse(t.getDateTime(),
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                if ("Today".equals(timeFilter)) {
                    return txnDate.toLocalDate().equals(now.toLocalDate());
                } else if ("This Week".equals(timeFilter)) {
                    return txnDate.isAfter(now.minusDays(7));
                } else if ("This Month".equals(timeFilter)) {
                    return txnDate.getMonth() == now.getMonth() && txnDate.getYear() == now.getYear();
                }
            } catch (Exception ex) {
                return true;
            }
            return true;
        };

        // 7. Add listeners to the search inputs to re-evaluate the filter logic
        txtTxnSearch.textProperty().addListener((obs, oldVal, newVal) -> filteredTxns.setPredicate(filterLogic));
        comboTimeFilter.valueProperty().addListener((obs, oldVal, newVal) -> filteredTxns.setPredicate(filterLogic));

        // 8. Bind the filtered list to the TableView
        txnTable.setItems(filteredTxns);
        
        // 9. Add a listener to dynamically update the stat cards whenever the filtered list changes
        Runnable updateTxnStats = () -> {
            int count = filteredTxns.size();
            double sum = 0.0;
            for (Transaction t : filteredTxns) {
                sum += t.getTotal();
            }
            localTxnTotalCountLabel.setText(String.valueOf(count));
            localTxnTotalRevenueLabel.setText("\u20B5" + String.format("%.2f", sum));
        };
        
        // Call once to initialize
        updateTxnStats.run();
        
        // Attach listener to update stats dynamically when filters are applied
        filteredTxns.addListener((javafx.collections.ListChangeListener.Change<? extends Transaction> c) -> {
            updateTxnStats.run();
        });

        VBox tableWrap = new VBox(txnTable);
        tableWrap.getStyleClass().add("table-container");
        VBox.setVgrow(tableWrap, Priority.ALWAYS);

        // Footer
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        Region fSpacer = new Region();
        HBox.setHgrow(fSpacer, Priority.ALWAYS);

        Button btnExportSales = new Button("\uD83D\uDCE5 Export CSV");
        btnExportSales.getStyleClass().add("action-button-add");
        btnExportSales.setPrefHeight(36);
        btnExportSales.setOnAction(e -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) rootPane.getScene().getWindow();
            com.pharmacy.util.CsvExporter.exportTransactions(transactionHistory, stage);
            showAlert("Export Complete", "Transaction data exported successfully!", Alert.AlertType.INFORMATION);
        });

        footer.getChildren().addAll(fSpacer, btnExportSales);

        center.getChildren().addAll(cards, searchBar, tableWrap, footer);

        // Right panel: Transaction Details (Receipt)
        VBox right = createRightPanelShell("Receipt Details", "Select a transaction to view receipt");
        right.setPrefWidth(480);

        TextArea receiptArea = new TextArea();
        receiptArea.setEditable(false);
        receiptArea.setWrapText(false);
        receiptArea.setStyle("-fx-font-family: monospace; -fx-font-size: 14px;");
        VBox.setVgrow(receiptArea, Priority.ALWAYS);

        txnTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("            PHARMACY MANAGEMENT SYSTEM\n");
                sb.append("=================================================\n");
                sb.append(String.format("Receipt ID : %s\n", newVal.getTxnId()));
                sb.append(String.format("Date       : %s\n", newVal.getDateTime()));
                sb.append("-------------------------------------------------\n");
                sb.append(String.format("%-26s %5s %16s\n", "Item", "Qty", "Subtotal"));
                sb.append("-------------------------------------------------\n");
                for (CartItem ci : newVal.getItems()) {
                    String name = ci.getName();
                    if (name.length() > 24) name = name.substring(0, 21) + "...";
                    String subText = String.format("\u20B5%.2f", ci.getSubtotal());
                    sb.append(String.format("%-26s %5d %16s\n", name, ci.getQuantity(), subText));
                }
                sb.append("-------------------------------------------------\n");
                String totalText = String.format("\u20B5%.2f", newVal.getTotal());
                sb.append(String.format("%-32s %16s\n", "TOTAL DUE:", totalText));
                sb.append("=================================================\n");
                sb.append("          Thank you for your business!");
                receiptArea.setText(sb.toString());
            } else {
                receiptArea.setText("");
            }
        });

        right.getChildren().addAll(receiptArea);

        transactionsCenter = center;
        transactionsRight = right;
    }

    // ============================================================
    // UI HELPER METHODS
    // ============================================================

    /**
     * Creates a standardized structural shell for right-side main content pages.
     * Generates the title, subtitle, and layout padding.
     *
     * @param title    The main title of the page.
     * @param subtitle A short description of the page's purpose.
     * @return A constructed VBox containing the page header.
     */
    private VBox createPageShell(String title, String subtitle) {
        VBox page = new VBox(20);
        page.getStyleClass().add("main-content");
        page.setPadding(new Insets(30, 25, 25, 25));

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox();
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("page-title");
        Label lblSub = new Label(subtitle);
        lblSub.getStyleClass().add("page-subtitle");
        titleBox.getChildren().addAll(lblTitle, lblSub);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label badge = new Label("System Online");
        badge.getStyleClass().add("status-badge-online");
        header.getChildren().addAll(titleBox, spacer, badge);

        page.getChildren().add(header);
        return page;
    }

    /**
     * Creates a stylized metric/statistic card for dashboards.
     */
    private VBox createStatCard(String label, String value, String colorClass) {
        VBox card = new VBox(5);
        card.getStyleClass().addAll("stat-card", colorClass);
        card.setPadding(new Insets(15, 20, 15, 20));
        Label lblLabel = new Label(label);
        lblLabel.getStyleClass().add("stat-label");
        Label lblValue = new Label(value);
        lblValue.getStyleClass().add("stat-value");
        card.getChildren().addAll(lblLabel, lblValue);
        return card;
    }

    /**
     * Creates a right panel shell with title and separator.
     */
    private VBox createRightPanelShell(String title, String subtitle) {
        VBox panel = new VBox(20);
        panel.getStyleClass().add("right-panel");
        panel.setPrefWidth(280);
        panel.setPadding(new Insets(30, 20, 20, 20));

        VBox titleBox = new VBox(5);
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("panel-title");
        Label lblSub = new Label(subtitle);
        lblSub.getStyleClass().add("panel-subtitle");
        titleBox.getChildren().addAll(lblTitle, lblSub);

        Separator sep = new Separator();
        sep.getStyleClass().add("panel-separator");

        panel.getChildren().addAll(titleBox, sep);
        return panel;
    }

    /**
     * Creates a labeled text field for forms.
     */
    private VBox createFormField(String label, String prompt) {
        VBox field = new VBox(5);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("form-label");
        TextField txt = new TextField();
        txt.setPromptText(prompt);
        txt.getStyleClass().add("form-field");
        field.getChildren().addAll(lbl, txt);
        return field;
    }

    // ============================================================
    // UTILITY & CALCULATIONS
    // ============================================================

    /**
     * Calculates the number of items that have low stock (between 1 and 29 units).
     * 
     * @return The count of low stock items.
     */
    private int countLowStock() {
        int count = 0;
        for (Medicine m : masterData)
            if (m.getStock() > 0 && m.getStock() < 30)
                count++;
        return count;
    }

    private int countOutOfStock() {
        int count = 0;
        for (Medicine m : masterData)
            if (m.getStock() == 0)
                count++;
        return count;
    }

    private double avgPrice() {
        if (masterData.isEmpty())
            return 0;
        double sum = 0;
        for (Medicine m : masterData)
            sum += m.getPrice();
        return sum / masterData.size();
    }

    private void refreshInventoryAlerts() {
        inventoryAlertsBox.getChildren().clear();
        Label alertTitle = new Label("\u26A0 Stock Alerts");
        alertTitle.getStyleClass().addAll("stat-label", "text-warning");
        alertTitle.setStyle("-fx-font-weight: bold;");

        VBox alertItems = new VBox(4);
        for (Medicine m : masterData) {
            if (m.getStock() == 0 || m.getStock() < 30) {
                Label alertItem = new Label((m.getStock() == 0 ? "\u26D4 " : "\u26A0 ") + m.getName() + " \u2014 "
                        + m.getStock() + " left");
                alertItem.getStyleClass().add("form-label");
                if (m.getStock() == 0)
                    alertItem.getStyleClass().add("text-danger");
                else
                    alertItem.getStyleClass().add("text-warning");
                alertItems.getChildren().add(alertItem);
            }
        }
        inventoryAlertsBox.getChildren().addAll(alertTitle, alertItems);
    }

    /**
     * Displays a customized native alert dialog.
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        DialogPane pane = alert.getDialogPane();
        pane.getStyleClass().add("alert-dialog");
        URL cssResource = getClass().getResource("/com/pharmacy/style/styles.css");
        if (cssResource != null) {
            pane.getStylesheets().add(cssResource.toExternalForm());
        }

        alert.showAndWait();
    }
}
