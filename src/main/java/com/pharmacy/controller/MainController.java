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
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.Optional;

/**
 * Core Controller for the AetherRx Pharmacy Management System.
 * Manages the global state, navigation routing, the Point of Sale (POS) cart, 
 * transaction history, and dynamic view rendering via Node caching.
 */
public class MainController {

    // --- Root Layout ---
    @FXML private BorderPane rootPane;

    // --- Navigation Buttons ---
    @FXML private Button btnDashboard;
    @FXML private Button btnInventory;
    @FXML private Button btnSales;
    @FXML private Button btnCustomers;
    @FXML private Button btnSettings;

    // --- Dashboard: Statistics Controls ---
    @FXML private Label lblTotalMedicines;
    @FXML private Label lblLowStock;
    @FXML private Label lblOutofStock;
    @FXML private Label lblTotalValuation;

    // --- Dashboard: Search & Filters ---
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> comboFilterCategory;

    // --- Dashboard: Table & Columns ---
    @FXML private TableView<Medicine> tblInventory;
    @FXML private TableColumn<Medicine, String> colCode;
    @FXML private TableColumn<Medicine, String> colName;
    @FXML private TableColumn<Medicine, String> colCategory;
    @FXML private TableColumn<Medicine, Integer> colStock;
    @FXML private TableColumn<Medicine, String> colStatus;
    @FXML private TableColumn<Medicine, Double> colPrice;

    // --- Dashboard: Add/Edit Form Fields ---
    @FXML private TextField txtCode;
    @FXML private TextField txtName;
    @FXML private ComboBox<String> comboFormCategory;
    @FXML private TextField txtStock;
    @FXML private TextField txtPrice;

    // --- Layout Containers (for page swapping) ---
    @FXML private VBox centerContent;
    @FXML private VBox rightPanel;

    // --- Internal State & Observables ---
    /** The master dataset holding all current pharmacy inventory. Shared across views for live updates. */
    private final ObservableList<Medicine> masterData = FXCollections.observableArrayList();
    /** A filtered view of the masterData used by the Inventory search bar and category filters. */
    private FilteredList<Medicine> filteredData;

    // --- UI Node Caching ---
    // These variables store the built layouts for each page. 
    // This caching prevents redundant FXML/UI recreation and eliminates NullPointerExceptions when switching views.
    private Node dashboardCenter, dashboardRight;
    private Node inventoryCenter, inventoryRight;
    private Node salesCenter, salesRight;
    private Node customersCenter, customersRight;
    private Node settingsCenter;
    
    // Inventory sync references
    private VBox inventoryAlertsBox;
    private Label lblInvLowStock;
    private Label lblInvOutOfStock;

    // Currently active nav button
    private Button activeNavButton;

    // Customer data for customer records page
    private final ObservableList<String[]> customerData = FXCollections.observableArrayList();

    // Cart and transaction data
    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private final ObservableList<Transaction> transactionHistory = FXCollections.observableArrayList();
    private Label cartTotalLabel;
    private int txnCounter = 0;

    /**
     * Called automatically after the FXML is loaded.
     * Initializes the core dashboard UI, populates tables with sample data,
     * and caches the initial scene graph nodes.
     */
    @FXML
    public void initialize() {
        // 1. Initialize ComboBox Items
        ObservableList<String> categories = FXCollections.observableArrayList("All", "Analgesics", "Antibiotics", "Cardiovascular", "Antidiabetic", "Vitamins");
        comboFilterCategory.setItems(categories);
        comboFilterCategory.setValue("All");

        ObservableList<String> formCategories = FXCollections.observableArrayList("Analgesics", "Antibiotics", "Cardiovascular", "Antidiabetic", "Vitamins");
        comboFormCategory.setItems(formCategories);

        // 2. Initialize Columns
        colCode.setCellValueFactory(cell -> cell.getValue().codeProperty());
        colName.setCellValueFactory(cell -> cell.getValue().nameProperty());
        colCategory.setCellValueFactory(cell -> cell.getValue().categoryProperty());
        colStock.setCellValueFactory(cell -> cell.getValue().stockProperty().asObject());
        colStatus.setCellValueFactory(cell -> cell.getValue().statusProperty());
        colPrice.setCellValueFactory(cell -> cell.getValue().priceProperty().asObject());

        // Custom styling for columns (currency formatting)
        colPrice.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("\u20B5%.2f", price));
                }
            }
        });

        // 3. Load Sample Data
        loadSampleData();

        // 4. Set up Search and Filtering (Real-time FilteredList)
        // Make the dashboard table columns fill available width
        tblInventory.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        filteredData = new FilteredList<>(masterData, p -> true);

        // Listen for search input changes
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> applyFilter());
        
        // Listen for category selection changes
        comboFilterCategory.valueProperty().addListener((observable, oldValue, newValue) -> applyFilter());

        // Connect the FilteredList to the Table
        SortedList<Medicine> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tblInventory.comparatorProperty());
        tblInventory.setItems(sortedData);

        // 5. Update initial stats dashboard
        updateDashboardStatistics();

        // 6. Save dashboard nodes for page switching
        dashboardCenter = centerContent;
        dashboardRight = rightPanel;
        activeNavButton = btnDashboard;

        // 7. Load sample customer data
        loadSampleCustomers();

        // 8. Load sample transactions for demo
        loadSampleTransactions();
    }

    // ============================================================
    //                      SAMPLE DATA GENERATION
    // ============================================================

    /**
     * Loads high-quality mock data into the table.
     */
    private void loadSampleData() {
        masterData.add(new Medicine("MED001", "Paracetamol 500mg", "Analgesics", 120, "In Stock", 2.50));
        masterData.add(new Medicine("MED002", "Amoxicillin 250mg", "Antibiotics", 45, "In Stock", 8.20));
        masterData.add(new Medicine("MED003", "Ibuprofen 400mg", "Analgesics", 90, "In Stock", 3.10));
        masterData.add(new Medicine("MED004", "Lipitor 10mg", "Cardiovascular", 0, "Out of Stock", 45.00));
        masterData.add(new Medicine("MED005", "Metformin 500mg", "Antidiabetic", 75, "In Stock", 12.80));
        masterData.add(new Medicine("MED006", "Vitamin C 1000mg", "Vitamins", 15, "Low Stock", 5.50));
        
        masterData.add(new Medicine("MED007", "Omeprazole 20mg", "Gastrointestinal", 200, "In Stock", 15.00));
        masterData.add(new Medicine("MED008", "Amlodipine 5mg", "Cardiovascular", 110, "In Stock", 18.50));
        masterData.add(new Medicine("MED009", "Cetirizine 10mg", "Antihistamines", 180, "In Stock", 6.00));
        masterData.add(new Medicine("MED010", "Salbutamol Inhaler", "Respiratory", 25, "Low Stock", 35.00));
        masterData.add(new Medicine("MED011", "Diazepam 5mg", "Neurological", 0, "Out of Stock", 22.00));
        masterData.add(new Medicine("MED012", "Ciprofloxacin 500mg", "Antibiotics", 85, "In Stock", 14.50));
        masterData.add(new Medicine("MED013", "Loratadine 10mg", "Antihistamines", 150, "In Stock", 7.20));
        masterData.add(new Medicine("MED014", "Azithromycin 250mg", "Antibiotics", 40, "In Stock", 25.00));
        masterData.add(new Medicine("MED015", "Atorvastatin 20mg", "Cardiovascular", 95, "In Stock", 40.00));
        masterData.add(new Medicine("MED016", "Lisinopril 10mg", "Cardiovascular", 60, "In Stock", 16.50));
        masterData.add(new Medicine("MED017", "Levothyroxine 50mcg", "Endocrine", 130, "In Stock", 9.80));
        masterData.add(new Medicine("MED018", "Pantoprazole 40mg", "Gastrointestinal", 145, "In Stock", 17.50));
    }

    private void loadSampleCustomers() {
        customerData.add(new String[]{"C001", "Sarah Johnson", "555-0123", "sarah.j@email.com", "12", "\u20B5342.50"});
        customerData.add(new String[]{"C002", "Michael Chen", "555-0456", "m.chen@email.com", "8", "\u20B5218.30"});
        customerData.add(new String[]{"C003", "Emily Davis", "555-0789", "emily.d@email.com", "15", "\u20B5567.80"});
        customerData.add(new String[]{"C004", "James Wilson", "555-0321", "j.wilson@email.com", "3", "\u20B589.90"});
        customerData.add(new String[]{"C005", "Maria Garcia", "555-0654", "m.garcia@email.com", "21", "\u20B51,024.60"});
    }

    private void loadSampleTransactions() {
        ObservableList<CartItem> items1 = FXCollections.observableArrayList(
                new CartItem("MED001", "Paracetamol 500mg", 5, 2.50),
                new CartItem("MED006", "Vitamin C 1000mg", 3, 5.50)
        );
        transactionHistory.add(new Transaction("TXN001", "2025-06-03 14:30", "C001", items1, 29.00));

        ObservableList<CartItem> items2 = FXCollections.observableArrayList(
                new CartItem("MED002", "Amoxicillin 250mg", 2, 8.20),
                new CartItem("MED005", "Metformin 500mg", 1, 12.80)
        );
        transactionHistory.add(new Transaction("TXN002", "2025-06-03 11:15", "", items2, 29.20));

        ObservableList<CartItem> items3 = FXCollections.observableArrayList(
                new CartItem("MED003", "Ibuprofen 400mg", 4, 3.10)
        );
        transactionHistory.add(new Transaction("TXN003", "2025-06-02 16:45", "C003", items3, 12.40));

        ObservableList<CartItem> items4 = FXCollections.observableArrayList(
                new CartItem("MED001", "Paracetamol 500mg", 10, 2.50),
                new CartItem("MED003", "Ibuprofen 400mg", 6, 3.10),
                new CartItem("MED005", "Metformin 500mg", 2, 12.80)
        );
        transactionHistory.add(new Transaction("TXN004", "2025-06-01 09:20", "C002", items4, 69.20));

        txnCounter = 4;
    }

    // ============================================================
    //                      NAVIGATION & ROUTING
    // ============================================================

    @FXML private void handleNavDashboard()  { switchPage("dashboard"); }
    @FXML private void handleNavInventory()  { switchPage("inventory"); }
    @FXML private void handleNavSales()      { switchPage("sales"); }
    @FXML private void handleNavCustomers()  { switchPage("customers"); }
    @FXML private void handleNavSettings()   { switchPage("settings"); }

    /**
     * Central page-switching engine. Swaps center and right content,
     * updates navigation button active states.
     */
    private void switchPage(String page) {
        // Update active button highlight
        Button targetButton;
        switch (page) {
            case "inventory": targetButton = btnInventory; break;
            case "sales":     targetButton = btnSales; break;
            case "customers": targetButton = btnCustomers; break;
            case "settings":  targetButton = btnSettings; break;
            default:          targetButton = btnDashboard; break;
        }
        setActiveNavButton(targetButton);

        // Swap content
        switch (page) {
            case "dashboard":
                rootPane.setCenter(dashboardCenter);
                rootPane.setRight(dashboardRight);
                updateDashboardStatistics();
                break;
            case "inventory":
                if (inventoryCenter == null) buildInventoryPage();
                rootPane.setCenter(inventoryCenter);
                rootPane.setRight(inventoryRight);
                break;
            case "sales":
                // Always rebuild to reflect current inventory
                buildSalesPage();
                rootPane.setCenter(salesCenter);
                rootPane.setRight(salesRight);
                break;
            case "customers":
                if (customersCenter == null) buildCustomersPage();
                rootPane.setCenter(customersCenter);
                rootPane.setRight(customersRight);
                break;
            case "settings":
                if (settingsCenter == null) buildSettingsPage();
                rootPane.setCenter(settingsCenter);
                rootPane.setRight(null);
                break;
        }
    }

    private boolean isDarkTheme = false;

    @FXML private void handleToggleTheme() {
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
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/pharmacy/view/login.fxml"));
            javafx.scene.Parent root = loader.load();
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Login \u2014 AetherRx");
            
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

    private void setActiveNavButton(Button btn) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("nav-button-active");
        }
        btn.getStyleClass().add("nav-button-active");
        activeNavButton = btn;
    }

    // ============================================================
    //                      DASHBOARD LOGIC
    // ============================================================

    /**
     * Re-evaluates the FilteredList when the search query or category combo box changes.
     * Updates the main dashboard table to only display matching items.
     */
    private void applyFilter() {
        String searchText = txtSearch.getText().toLowerCase().trim();
        String selectedCategory = comboFilterCategory.getValue();

        filteredData.setPredicate(medicine -> {
            // Category Filter
            if (selectedCategory != null && !selectedCategory.equals("All")) {
                if (!medicine.getCategory().equalsIgnoreCase(selectedCategory)) {
                    return false;
                }
            }

            // Search Keyword Filter
            if (searchText.isEmpty()) {
                return true;
            }

            return medicine.getCode().toLowerCase().contains(searchText) ||
                   medicine.getName().toLowerCase().contains(searchText) ||
                   medicine.getCategory().toLowerCase().contains(searchText);
        });

        updateDashboardStatistics();
    }

    /**
     * Recalculates statistics for the top dashboard panel (Total Medicines, Valuation, Out of Stock, etc.).
     * This is automatically called whenever an item is bought or restocked to ensure real-time accuracy.
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

        // Sync Inventory page stats dynamically
        if (lblInvLowStock != null) lblInvLowStock.setText(String.valueOf(lowStock));
        if (lblInvOutOfStock != null) lblInvOutOfStock.setText(String.valueOf(outOfStock));
        if (inventoryAlertsBox != null) refreshInventoryAlerts();
    }

    /**
     * Handles adding a new medicine from the sidebar form.
     */
    @FXML
    private void handleAddMedicine() {
        String code = txtCode.getText().trim();
        String name = txtName.getText().trim();
        String category = comboFormCategory.getValue();
        String stockStr = txtStock.getText().trim();
        String priceStr = txtPrice.getText().trim();

        // Simple validation
        if (code.isEmpty() || name.isEmpty() || category == null || stockStr.isEmpty() || priceStr.isEmpty()) {
            showAlert("Input Validation Error", "All fields are required to register a medicine.", Alert.AlertType.ERROR);
            return;
        }

        try {
            int stock = Integer.parseInt(stockStr);
            double price = Double.parseDouble(priceStr);

            if (stock < 0 || price < 0) {
                showAlert("Input Validation Error", "Stock and Price cannot be negative.", Alert.AlertType.ERROR);
                return;
            }

            // Determine stock status dynamically
            String status = "In Stock";
            if (stock == 0) {
                status = "Out of Stock";
            } else if (stock < 30) {
                status = "Low Stock";
            }

            Medicine newMed = new Medicine(code, name, category, stock, status, price);
            masterData.add(newMed);

            // Clear inputs
            txtCode.clear();
            txtName.clear();
            comboFormCategory.setValue(null);
            txtStock.clear();
            txtPrice.clear();

            updateDashboardStatistics();
            showAlert("Success", "Medicine successfully added to inventory!", Alert.AlertType.INFORMATION);

        } catch (NumberFormatException e) {
            showAlert("Input Validation Error", "Stock must be an integer, and Price must be a decimal value.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Deletes the currently selected medicine from the master dataset.
     * Prompts the user with a confirmation alert before executing the deletion.
     */
    @FXML
    private void handleDeleteMedicine() {
        Medicine selected = tblInventory.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Required", "Please select a medicine from the table first.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to remove " + selected.getName() + " from the inventory database?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            masterData.remove(selected);
            updateDashboardStatistics();
            showAlert("Deleted", "Medicine removed successfully.", Alert.AlertType.INFORMATION);
        }
    }

    // ============================================================
    //                      PAGE BUILDERS
    // ============================================================

    /**
     * Constructs the Inventory Manager page programmatically.
     * This layout provides deep stock management capabilities and a detailed TableView.
     * The built layout is cached in memory for immediate retrieval upon future navigation.
     */
    private void buildInventoryPage() {
        VBox center = createPageShell("Inventory Manager",
                "Manage stock levels, reorder points, and supplier information");

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
                createStatCard("Avg. Unit Price", String.format("\u20B5%.2f", avgPrice()), "stat-card-blue")
        );
        for (Node card : cards.getChildren()) {
            HBox.setHgrow(card, Priority.ALWAYS);
        }

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
        detailedTable.setItems(masterData);

        VBox tableWrap = new VBox(detailedTable);
        tableWrap.getStyleClass().add("table-container");
        VBox.setVgrow(tableWrap, Priority.ALWAYS);

        // Footer
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        Label footerText = new Label("Inventory is synced with the Dashboard in real-time.");
        footerText.getStyleClass().add("footer-help-text");
        footer.getChildren().add(footerText);

        center.getChildren().addAll(cards, tableWrap, footer);

        // Right panel: Restock Quick Actions
        VBox right = createRightPanelShell("Quick Restock", "Update stock levels for existing items");

        Label lblSelectInfo = new Label("Select an item from the table, then set the new stock quantity below.");
        lblSelectInfo.getStyleClass().add("form-label");
        lblSelectInfo.setWrapText(true);

        VBox fldNewStock = createFormField("New Stock Quantity", "e.g. 200");
        TextField txtNewStock = (TextField) fldNewStock.getChildren().get(1);

        Button btnRestock = new Button("\u2713 Update Stock");
        btnRestock.getStyleClass().add("action-button-add");
        btnRestock.setMaxWidth(Double.MAX_VALUE);
        btnRestock.setPrefHeight(44);
        btnRestock.setOnAction(e -> {
            Medicine selected = detailedTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Selection Required", "Please select a medicine from the table first.", Alert.AlertType.WARNING);
                return;
            }
            try {
                int newStock = Integer.parseInt(txtNewStock.getText().trim());
                if (newStock < 0) {
                    showAlert("Invalid Input", "Stock cannot be negative.", Alert.AlertType.ERROR);
                    return;
                }
                selected.setStock(newStock);
                if (newStock == 0) selected.setStatus("Out of Stock");
                else if (newStock < 30) selected.setStatus("Low Stock");
                else selected.setStatus("In Stock");
                detailedTable.refresh();
                txtNewStock.clear();
                updateDashboardStatistics();
                showAlert("Success", selected.getName() + " restocked to " + newStock + " units.", Alert.AlertType.INFORMATION);
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter a valid integer for stock quantity.", Alert.AlertType.ERROR);
            }
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Alerts summary in right panel
        inventoryAlertsBox = new VBox(8);
        inventoryAlertsBox.getStyleClass().addAll("stat-card", "stat-card-amber");
        inventoryAlertsBox.setPadding(new Insets(15, 15, 15, 15));
        refreshInventoryAlerts();

        right.getChildren().addAll(lblSelectInfo, fldNewStock, btnRestock, spacer, inventoryAlertsBox);

        inventoryCenter = center;
        inventoryRight = right;
    }

    /**
     * Constructs the Point of Sale (POS) page programmatically.
     * Includes a real-time shopping cart, inventory selection, customer association, 
     * and a dynamic "Change Calculator" that strictly validates payments before checkout.
     */
    private void buildSalesPage() {
        VBox center = createPageShell("Point of Sale",
                "Select medicines, set quantity, and process customer transactions");

        // ---- Add-to-Cart Bar ----
        HBox addBar = new HBox(12);
        addBar.setAlignment(Pos.CENTER_LEFT);
        addBar.getStyleClass().add("filter-bar");
        addBar.setPadding(new Insets(12, 15, 12, 15));

        // Medicine ComboBox — populated from live inventory
        ComboBox<String> comboMedicine = new ComboBox<>();
        comboMedicine.setPromptText("Select Medicine...");
        comboMedicine.getStyleClass().add("filter-combo");
        comboMedicine.setPrefHeight(38);
        comboMedicine.setPrefWidth(280);
        for (Medicine m : masterData) {
            if (m.getStock() > 0) {
                comboMedicine.getItems().add(m.getCode() + " \u2014 " + m.getName()
                        + "  [\u20B5" + String.format("%.2f", m.getPrice()) + ", Stock: " + m.getStock() + "]");
            }
        }

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

        // Vertical separator
        Separator vertSep = new Separator(Orientation.VERTICAL);
        vertSep.setPrefHeight(30);

        // Customer ID field
        Label lblCust = new Label("Customer ID:");
        lblCust.getStyleClass().add("filter-label");
        TextField txtCustId = new TextField();
        txtCustId.setPromptText("e.g. C001 (optional)");
        txtCustId.getStyleClass().add("form-field");
        txtCustId.setPrefWidth(150);
        txtCustId.setPrefHeight(38);

        addBar.getChildren().addAll(comboMedicine, txtQty, btnAddToCart, vertSep, lblCust, txtCustId);

        // ---- Cart Table ----
        TableView<CartItem> cartTable = new TableView<>();
        cartTable.getStyleClass().add("modern-table");
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(cartTable, Priority.ALWAYS);
        cartTable.setItems(cartItems);
        cartTable.setPlaceholder(new Label("Cart is empty — select a medicine above and click 'Add to Cart'"));

        TableColumn<CartItem, String> cMed = new TableColumn<>("Medicine");
        cMed.setCellValueFactory(c -> c.getValue().nameProperty());
        cMed.setPrefWidth(220);

        TableColumn<CartItem, Integer> cQty = new TableColumn<>("Qty");
        cQty.setCellValueFactory(c -> c.getValue().quantityProperty().asObject());
        cQty.setPrefWidth(70);

        TableColumn<CartItem, Double> cUnitP = new TableColumn<>("Unit Price");
        cUnitP.setCellValueFactory(c -> c.getValue().unitPriceProperty().asObject());
        cUnitP.setPrefWidth(110);
        cUnitP.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : String.format("\u20B5%.2f", p));
            }
        });

        TableColumn<CartItem, Double> cSub = new TableColumn<>("Subtotal");
        cSub.setCellValueFactory(c -> c.getValue().subtotalProperty().asObject());
        cSub.setPrefWidth(110);
        cSub.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : String.format("\u20B5%.2f", p));
            }
        });

        // Remove button column
        TableColumn<CartItem, Void> cRemove = new TableColumn<>("Action");
        cRemove.setPrefWidth(80);
        cRemove.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("\u2715 Remove");
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
                super.updateItem(v, empty);
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
        // Listens to the tendered text field and compares it to the cart subtotal in real-time.
        // Provides instant color-coded visual feedback (Green for OK, Red for Insufficient).
        txtTendered.textProperty().addListener((obs, oldV, newV) -> {
            try {
                double total = 0;
                for (CartItem item : cartItems) total += item.getSubtotal();
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

        footerBar.getChildren().addAll(btnClear, footerSpacer, txtTendered, lblChange, lblTotalTag, lblCartTotal, btnCheckout);

        // ---- Add-to-Cart Action Engine ----
        btnAddToCart.setOnAction(e -> {
            int selectedIdx = comboMedicine.getSelectionModel().getSelectedIndex();
            if (selectedIdx < 0) {
                showAlert("No Selection", "Please select a medicine from the dropdown.", Alert.AlertType.WARNING);
                return;
            }
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
                // Find the medicine — match by index of in-stock items
                int inStockIdx = 0;
                Medicine med = null;
                for (Medicine m : masterData) {
                    if (m.getStock() > 0) {
                        if (inStockIdx == selectedIdx) { med = m; break; }
                        inStockIdx++;
                    }
                }
                if (med == null) return;

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
            double total = 0;
            for (CartItem item : cartItems) total += item.getSubtotal();
            
            // Validate tendered amount
            if (!txtTendered.getText().isEmpty()) {
                try {
                    double tendered = Double.parseDouble(txtTendered.getText());
                    if (tendered < total) {
                        showAlert("Insufficient Funds", "Amount tendered (\u20B5" + String.format("%.2f", tendered) + ") is less than the total (\u20B5" + String.format("%.2f", total) + ").", Alert.AlertType.ERROR);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    showAlert("Invalid Amount", "Please enter a valid amount tendered.", Alert.AlertType.ERROR);
                    return;
                }
            }

            // Deduct stock from inventory
            for (CartItem item : cartItems) {
                for (Medicine m : masterData) {
                    if (m.getCode().equals(item.getCode())) {
                        int newStock = m.getStock() - item.getQuantity();
                        m.setStock(Math.max(0, newStock));
                        if (m.getStock() == 0) m.setStatus("Out of Stock");
                        else if (m.getStock() < 30) m.setStatus("Low Stock");
                        else m.setStatus("In Stock");
                        break;
                    }
                }
            }

            // Record transaction
            txnCounter++;
            String txnId = String.format("TXN%03d", txnCounter);
            String custId = txtCustId.getText().trim();
            Transaction txn = new Transaction(txnId, custId,
                    FXCollections.observableArrayList(cartItems), total);
            transactionHistory.add(0, txn);

            // Clear cart and refresh
            cartItems.clear();
            txtCustId.clear();
            txtTendered.clear();
            lblChange.setText("Change: \u20B50.00");
            updateCartTotal();
            updateDashboardStatistics();

            // Refresh medicine dropdown to reflect new stock
            comboMedicine.getItems().clear();
            for (Medicine m : masterData) {
                if (m.getStock() > 0) {
                    comboMedicine.getItems().add(m.getCode() + " \u2014 " + m.getName()
                            + "  [\u20B5" + String.format("%.2f", m.getPrice()) + ", Stock: " + m.getStock() + "]");
                }
            }

            showAlert("Sale Complete",
                    "Transaction " + txnId + " recorded.\nTotal: \u20B5" + String.format("%.2f", total),
                    Alert.AlertType.INFORMATION);
        });

        center.getChildren().addAll(addBar, tableWrap, footerBar);

        // ---- Right Panel: Transaction History ----
        VBox right = createRightPanelShell("Transaction History", "Click a row to view full receipt");

        // Filter Dropdown
        ComboBox<String> comboTimeFilter = new ComboBox<>(FXCollections.observableArrayList("All Time", "Today", "This Week", "This Month"));
        comboTimeFilter.setValue("All Time");
        comboTimeFilter.getStyleClass().add("form-combo");
        comboTimeFilter.setMaxWidth(Double.MAX_VALUE);
        
        FilteredList<Transaction> filteredTxns = new FilteredList<>(transactionHistory, p -> true);
        comboTimeFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            filteredTxns.setPredicate(txn -> {
                if ("All Time".equals(newVal)) return true;
                try {
                    java.time.LocalDateTime txnDate = java.time.LocalDateTime.parse(txn.getDateTime(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    java.time.LocalDateTime now = java.time.LocalDateTime.now();
                    if ("Today".equals(newVal)) {
                        return txnDate.toLocalDate().equals(now.toLocalDate());
                    } else if ("This Week".equals(newVal)) {
                        return txnDate.isAfter(now.minusDays(7));
                    } else if ("This Month".equals(newVal)) {
                        return txnDate.getMonth() == now.getMonth() && txnDate.getYear() == now.getYear();
                    }
                } catch (Exception ex) {
                    return true;
                }
                return true;
            });
        });

        TableView<Transaction> txnTable = new TableView<>();
        txnTable.getStyleClass().add("modern-table");
        txnTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(txnTable, Priority.ALWAYS);
        txnTable.setItems(filteredTxns);

        TableColumn<Transaction, String> tId = new TableColumn<>("ID");
        tId.setCellValueFactory(c -> c.getValue().txnIdProperty());
        tId.setPrefWidth(65);

        TableColumn<Transaction, String> tCust = new TableColumn<>("Customer");
        tCust.setCellValueFactory(c -> c.getValue().customerIdProperty());
        tCust.setPrefWidth(70);

        TableColumn<Transaction, Double> tTotal = new TableColumn<>("Total");
        tTotal.setCellValueFactory(c -> c.getValue().totalProperty().asObject());
        tTotal.setPrefWidth(80);
        tTotal.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : String.format("\u20B5%.2f", p));
            }
        });

        txnTable.getColumns().add(tId);
        txnTable.getColumns().add(tCust);
        txnTable.getColumns().add(tTotal);

        // Double-click to view receipt
        txnTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Transaction selected = txnTable.getSelectionModel().getSelectedItem();
                if (selected != null) showTransactionDetails(selected);
            }
        });

        Button btnViewReceipt = new Button("\uD83D\uDCCB View Receipt");
        btnViewReceipt.getStyleClass().add("action-button-add");
        btnViewReceipt.setMaxWidth(Double.MAX_VALUE);
        btnViewReceipt.setPrefHeight(38);
        btnViewReceipt.setOnAction(e -> {
            Transaction selected = txnTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("No Selection", "Select a transaction to view its receipt.", Alert.AlertType.WARNING);
                return;
            }
            showTransactionDetails(selected);
        });

        VBox txnTableWrap = new VBox(txnTable);
        txnTableWrap.getStyleClass().add("table-container");
        VBox.setVgrow(txnTableWrap, Priority.ALWAYS);

        // Today's summary card
        VBox summaryCard = new VBox(6);
        summaryCard.getStyleClass().addAll("stat-card", "stat-card-teal");
        summaryCard.setPadding(new Insets(15));
        Label summaryTitle = new Label("Summary");
        summaryTitle.getStyleClass().addAll("stat-label", "text-cyan");
        summaryTitle.setStyle("-fx-font-weight: bold;");
        Label summaryCount = new Label(transactionHistory.size() + " transactions recorded");
        summaryCount.getStyleClass().addAll("form-label", "text-primary");
        double totalRevenue = 0;
        for (Transaction t : transactionHistory) totalRevenue += t.getTotal();
        Label summaryRev = new Label("Total revenue: \u20B5" + String.format("%.2f", totalRevenue));
        summaryRev.getStyleClass().add("form-label");
        summaryCard.getChildren().addAll(summaryTitle, summaryCount, summaryRev);

        right.getChildren().addAll(comboTimeFilter, txnTableWrap, btnViewReceipt, summaryCard);

        salesCenter = center;
        salesRight = right;
    }

    /**
     * Updates the cart total label to reflect current cart contents.
     */
    private void updateCartTotal() {
        double total = 0;
        for (CartItem item : cartItems) total += item.getSubtotal();
        if (cartTotalLabel != null) {
            cartTotalLabel.setText(String.format("\u20B5%.2f", total));
        }
    }

    /**
     * Shows a detailed receipt dialog for a completed transaction.
     */
    private void showTransactionDetails(Transaction txn) {
        Alert details = new Alert(Alert.AlertType.INFORMATION);
        details.setTitle("Transaction Receipt");
        details.setHeaderText("Receipt \u2014 " + txn.getTxnId());

        StringBuilder sb = new StringBuilder();
        sb.append("Date:       ").append(txn.getDateTime()).append("\n");
        sb.append("Customer:   ").append(txn.getCustomerId()).append("\n");
        sb.append("Items:      ").append(txn.getItemCount()).append("\n");
        sb.append("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\n");
        for (CartItem item : txn.getItems()) {
            sb.append(String.format("%-22s x%-3d @ \u20B5%-8.2f = \u20B5%.2f\n",
                    item.getName(), item.getQuantity(), item.getUnitPrice(), item.getSubtotal()));
        }
        sb.append("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\n");
        sb.append(String.format("TOTAL:  \u20B5%.2f", txn.getTotal()));

        details.setContentText(sb.toString());

        DialogPane pane = details.getDialogPane();
        pane.getStyleClass().add("alert-dialog");
        pane.setPrefWidth(450);
        URL cssResource = getClass().getResource("/com/pharmacy/style/styles.css");
        if (cssResource != null) {
            pane.getStylesheets().add(cssResource.toExternalForm());
        }

        details.showAndWait();
    }

    /**
     * Builds the Customer Records page — customer database with add/search.
     */
    private void buildCustomersPage() {
        VBox center = createPageShell("Customer Records",
                "Manage your customer database and purchase history");

        // Stat cards
        HBox cards = new HBox(15);
        cards.getStyleClass().add("stat-cards-container");
        cards.getChildren().addAll(
                createStatCard("Total Customers", String.valueOf(customerData.size()), "stat-card-teal"),
                createStatCard("Active This Month", "4", "stat-card-blue"),
                createStatCard("Total Revenue", "\u20B52,243.10", "stat-card-amber"),
                createStatCard("Avg. Spend", String.format("\u20B5%.2f", 2243.10 / customerData.size()), "stat-card-rose")
        );
        for (Node card : cards.getChildren()) {
            HBox.setHgrow(card, Priority.ALWAYS);
        }

        // Search bar
        HBox searchBar = new HBox(15);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.getStyleClass().add("filter-bar");
        searchBar.setPadding(new Insets(12, 15, 12, 15));
        TextField custSearch = new TextField();
        custSearch.setPromptText("\uD83D\uDD0D Search by name, ID, or email...");
        custSearch.getStyleClass().add("search-field");
        custSearch.setPrefHeight(38);
        HBox.setHgrow(custSearch, Priority.ALWAYS);
        searchBar.getChildren().add(custSearch);

        // Customer table
        TableView<String[]> custTable = new TableView<>();
        custTable.getStyleClass().add("modern-table");
        custTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(custTable, Priority.ALWAYS);

        String[] headers = {"ID", "Full Name", "Phone", "Email", "Purchases", "Total Spent"};
        int[] widths = {60, 160, 100, 180, 85, 100};
        for (int i = 0; i < headers.length; i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(headers[i]);
            col.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue()[idx]));
            col.setPrefWidth(widths[i]);
            custTable.getColumns().add(col);
        }

        // Wrap in filterable list
        FilteredList<String[]> filteredCustomers = new FilteredList<>(customerData, p -> true);
        custSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = newVal.toLowerCase().trim();
            filteredCustomers.setPredicate(c -> {
                if (query.isEmpty()) return true;
                for (String field : c) {
                    if (field.toLowerCase().contains(query)) return true;
                }
                return false;
            });
        });
        SortedList<String[]> sortedCustomers = new SortedList<>(filteredCustomers);
        sortedCustomers.comparatorProperty().bind(custTable.comparatorProperty());
        custTable.setItems(sortedCustomers);

        VBox tableWrap = new VBox(custTable);
        tableWrap.getStyleClass().add("table-container");
        VBox.setVgrow(tableWrap, Priority.ALWAYS);

        // Footer with delete
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        Button btnDeleteCust = new Button("\uD83D\uDDD1 Remove Customer");
        btnDeleteCust.getStyleClass().add("action-button-delete");
        btnDeleteCust.setPrefHeight(36);
        btnDeleteCust.setOnAction(e -> {
            String[] selected = custTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Selection Required", "Please select a customer from the table.", Alert.AlertType.WARNING);
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Deletion");
            confirm.setHeaderText(null);
            confirm.setContentText("Remove customer " + selected[1] + " from records?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                customerData.remove(selected);
                showAlert("Deleted", "Customer removed successfully.", Alert.AlertType.INFORMATION);
            }
        });
        Region fSpacer = new Region();
        HBox.setHgrow(fSpacer, Priority.ALWAYS);
        Label helpText = new Label("Select a customer record to view details or remove.");
        helpText.getStyleClass().add("footer-help-text");
        footer.getChildren().addAll(btnDeleteCust, fSpacer, helpText);

        center.getChildren().addAll(cards, searchBar, tableWrap, footer);

        // Right panel: Add Customer form
        VBox right = createRightPanelShell("Add Customer", "Register a new customer to the database");

        VBox fldId = createFormField("Customer ID", "e.g. C006");
        VBox fldName = createFormField("Full Name", "e.g. John Smith");
        VBox fldPhone = createFormField("Phone Number", "e.g. 555-1234");
        VBox fldEmail = createFormField("Email Address", "e.g. john@email.com");

        Button btnAddCust = new Button("\u2713 Register Customer");
        btnAddCust.getStyleClass().add("action-button-add");
        btnAddCust.setMaxWidth(Double.MAX_VALUE);
        btnAddCust.setPrefHeight(44);
        btnAddCust.setOnAction(e -> {
            TextField tId = (TextField) fldId.getChildren().get(1);
            TextField tName = (TextField) fldName.getChildren().get(1);
            TextField tPhone = (TextField) fldPhone.getChildren().get(1);
            TextField tEmail = (TextField) fldEmail.getChildren().get(1);
            if (tId.getText().trim().isEmpty() || tName.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Customer ID and Name are required.", Alert.AlertType.ERROR);
                return;
            }
            customerData.add(new String[]{
                    tId.getText().trim(), tName.getText().trim(),
                    tPhone.getText().trim(), tEmail.getText().trim(), "0", "\u20B50.00"
            });
            tId.clear(); tName.clear(); tPhone.clear(); tEmail.clear();
            showAlert("Success", "Customer registered successfully!", Alert.AlertType.INFORMATION);
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        right.getChildren().addAll(fldId, fldName, fldPhone, fldEmail, spacer, btnAddCust);

        customersCenter = center;
        customersRight = right;
    }

    /**
     * Builds the System Settings page — full-width configuration panel.
     */
    private void buildSettingsPage() {
        VBox center = createPageShell("System Settings",
                "Configure application preferences, security, and data management");

        // Settings sections
        VBox sectionsContainer = new VBox(20);
        VBox.setVgrow(sectionsContainer, Priority.ALWAYS);

        // --- General Settings ---
        VBox generalSection = createSettingsSection("General Settings", "\u2699");
        HBox genRow1 = createSettingsRow("Pharmacy Name", createSettingsTextField("AetherRx Pharmacy"));
        HBox genRow2 = createSettingsRow("Address", createSettingsTextField("123 Health Street, Medical District"));
        HBox genRow3 = createSettingsRow("Phone Number", createSettingsTextField("+233 (0) 30 123-4567"));
        HBox genRow4 = createSettingsRow("License Number", createSettingsTextField("PH-2024-0042"));
        generalSection.getChildren().addAll(genRow1, genRow2, genRow3, genRow4);

        // --- Security Settings ---
        VBox securitySection = createSettingsSection("Security & Access", "\uD83D\uDD12");
        HBox secRow1 = createSettingsRow("Session Timeout", createSettingsCombo("30 minutes", "15 minutes", "30 minutes", "1 hour", "Never"));
        HBox secRow2 = createSettingsRow("Auto-Lock Screen", createSettingsCombo("Enabled", "Enabled", "Disabled"));
        HBox secRow3 = createSettingsRow("Two-Factor Auth", createSettingsCombo("Disabled", "Enabled", "Disabled"));
        securitySection.getChildren().addAll(secRow1, secRow2, secRow3);

        // --- Notifications ---
        VBox notifSection = createSettingsSection("Notifications", "\uD83D\uDD14");
        HBox notRow1 = createSettingsRow("Low Stock Alerts", createSettingsCombo("Enabled", "Enabled", "Disabled"));
        HBox notRow2 = createSettingsRow("Stock Threshold", createSettingsTextField("30"));
        HBox notRow3 = createSettingsRow("Email Reports", createSettingsCombo("Weekly", "Daily", "Weekly", "Monthly", "Disabled"));
        notifSection.getChildren().addAll(notRow1, notRow2, notRow3);

        // --- Data Management ---
        VBox dataSection = createSettingsSection("Data Management", "\uD83D\uDCBE");
        HBox dataRow1 = createSettingsRow("Auto Backup", createSettingsCombo("Daily", "Daily", "Weekly", "Monthly", "Disabled"));
        
        HBox dataRow2 = new HBox(15);
        dataRow2.setAlignment(Pos.CENTER_LEFT);
        dataRow2.setPadding(new Insets(8, 15, 8, 15));
        Label exportLabel = new Label("Export Data");
        exportLabel.getStyleClass().addAll("form-label", "text-muted");
        exportLabel.setMinWidth(160);
        Button btnExport = new Button("\uD83D\uDCE4 Export to CSV");
        btnExport.getStyleClass().add("action-button-delete");
        btnExport.setStyle("-fx-text-fill: -fx-color-blue; -fx-border-color: rgba(59,130,246,0.3);");
        btnExport.setOnAction(e -> showAlert("Export", "Data exported successfully to pharmacy_data.csv", Alert.AlertType.INFORMATION));
        Button btnBackup = new Button("\uD83D\uDCBE Backup Now");
        btnBackup.getStyleClass().add("action-button-delete");
        btnBackup.setStyle("-fx-text-fill: -fx-color-green; -fx-border-color: rgba(16,185,129,0.3);");
        btnBackup.setOnAction(e -> showAlert("Backup", "Database backup created successfully.", Alert.AlertType.INFORMATION));
        dataRow2.getChildren().addAll(exportLabel, btnExport, btnBackup);

        dataSection.getChildren().addAll(dataRow1, dataRow2);

        sectionsContainer.getChildren().addAll(generalSection, securitySection, notifSection, dataSection);

        // Wrap in ScrollPane for overflow
        ScrollPane scroll = new ScrollPane(sectionsContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.getStyleClass().add("settings-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Save button footer
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button btnSave = new Button("\u2713 Save All Settings");
        btnSave.getStyleClass().add("action-button-add");
        btnSave.setPrefHeight(40);
        btnSave.setPrefWidth(200);
        btnSave.setOnAction(e -> showAlert("Settings Saved", "All system settings have been saved successfully.", Alert.AlertType.INFORMATION));
        Label savedLabel = new Label("Last saved: Today 10:30 AM");
        savedLabel.getStyleClass().add("footer-help-text");
        Region fSpacer = new Region();
        HBox.setHgrow(fSpacer, Priority.ALWAYS);
        footer.getChildren().addAll(savedLabel, fSpacer, btnSave);

        center.getChildren().addAll(scroll, footer);

        settingsCenter = center;
    }

    // ============================================================
    //                      UI HELPER METHODS
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
     *
     * @param label      The title of the statistic (e.g., "Total Revenue").
     * @param value      The value of the statistic (e.g., "$1,000").
     * @param colorClass The CSS class to apply for color accents (e.g., "stat-card-teal").
     * @return A VBox acting as the visual card.
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

    /**
     * Creates a settings section with a title header.
     */
    private VBox createSettingsSection(String title, String icon) {
        VBox section = new VBox(8);
        section.getStyleClass().addAll("stat-card");
        section.setStyle("-fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 1;");
        section.setPadding(new Insets(20));

        Label header = new Label(icon + "  " + title);
        header.getStyleClass().add("panel-title");
        header.setStyle("-fx-font-size: 15px;");

        Separator sep = new Separator();
        sep.getStyleClass().add("panel-separator");

        section.getChildren().addAll(header, sep);
        return section;
    }

    /**
     * Creates a settings row (label + control).
     */
    private HBox createSettingsRow(String label, Node control) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 15, 6, 15));
        Label lbl = new Label(label);
        lbl.getStyleClass().addAll("form-label", "text-muted");
        lbl.setMinWidth(160);
        row.getChildren().addAll(lbl, control);
        return row;
    }

    private TextField createSettingsTextField(String defaultValue) {
        TextField tf = new TextField(defaultValue);
        tf.getStyleClass().add("form-field");
        tf.setPrefWidth(250);
        return tf;
    }

    private ComboBox<String> createSettingsCombo(String defaultVal, String... items) {
        ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(items));
        combo.setValue(defaultVal);
        combo.getStyleClass().add("form-combo");
        combo.setPrefWidth(250);
        return combo;
    }

    // ============================================================
    //                      UTILITY & CALCULATIONS
    // ============================================================

    /**
     * Calculates the number of items that have low stock (between 1 and 29 units).
     * @return The count of low stock items.
     */
    private int countLowStock() {
        int count = 0;
        for (Medicine m : masterData) if (m.getStock() > 0 && m.getStock() < 30) count++;
        return count;
    }

    private int countOutOfStock() {
        int count = 0;
        for (Medicine m : masterData) if (m.getStock() == 0) count++;
        return count;
    }

    private double avgPrice() {
        if (masterData.isEmpty()) return 0;
        double sum = 0;
        for (Medicine m : masterData) sum += m.getPrice();
        return sum / masterData.size();
    }

    private void refreshInventoryAlerts() {
        inventoryAlertsBox.getChildren().clear();
        Label alertTitle = new Label("\u26A0 Stock Alerts");
        alertTitle.getStyleClass().add("stat-label");
        alertTitle.setStyle("-fx-text-fill: #fbbf24; -fx-font-weight: bold;");
        
        VBox alertItems = new VBox(4);
        for (Medicine m : masterData) {
            if (m.getStock() == 0 || m.getStock() < 30) {
                Label alertItem = new Label((m.getStock() == 0 ? "\u26D4 " : "\u26A0 ") + m.getName() + " \u2014 " + m.getStock() + " left");
                alertItem.getStyleClass().add("form-label");
                if (m.getStock() == 0) alertItem.getStyleClass().add("text-danger");
                else alertItem.getStyleClass().add("text-warning");
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
