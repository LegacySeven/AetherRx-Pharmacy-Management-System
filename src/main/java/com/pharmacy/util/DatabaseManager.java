package com.pharmacy.util;

import com.pharmacy.model.CartItem;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;


/**
 * Manages the SQLite embedded database connection and provides methods
 * for querying, inserting, and seeding the data.
 */
public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:aether_rx.db";

    /**
     * Initializes the database tables. If the database is empty, it seeds it
     * with default mock data for demonstration.
     */
    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(URL)) {
            if (conn != null) {
                // Create tables
                try (Statement stmt = conn.createStatement()) {
                    // Medicines Table
                    stmt.execute("CREATE TABLE IF NOT EXISTS medicines (" +
                            "code TEXT PRIMARY KEY, " +
                            "name TEXT NOT NULL, " +
                            "category TEXT NOT NULL, " +
                            "stock INTEGER NOT NULL, " +
                            "status TEXT NOT NULL, " +
                            "price REAL NOT NULL" +
                            ");");

                    // Customers Table
                    stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                            "id TEXT PRIMARY KEY, " +
                            "name TEXT NOT NULL, " +
                            "phone TEXT NOT NULL, " +
                            "email TEXT NOT NULL, " +
                            "purchases TEXT NOT NULL, " +
                            "spent TEXT NOT NULL" +
                            ");");

                    // Transactions Table
                    stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                            "txn_id TEXT PRIMARY KEY, " +
                            "customer_id TEXT, " +
                            "date_time TEXT NOT NULL, " +
                            "total REAL NOT NULL" +
                            ");");

                    // Transaction Items Table (to store receipt items)
                    stmt.execute("CREATE TABLE IF NOT EXISTS transaction_items (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "txn_id TEXT NOT NULL, " +
                            "code TEXT NOT NULL, " +
                            "name TEXT NOT NULL, " +
                            "quantity INTEGER NOT NULL, " +
                            "unit_price REAL NOT NULL, " +
                            "FOREIGN KEY (txn_id) REFERENCES transactions (txn_id)" +
                            ");");

                    // Users Table (for Role-Based Access Control)
                    stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                            "username TEXT PRIMARY KEY, " +
                            "password TEXT NOT NULL, " +
                            "role TEXT NOT NULL" +
                            ");");
                }

                // Seed data if the database is brand new
                if (isTableEmpty(conn, "medicines")) {
                    seedMedicines(conn);
                }
                if (isTableEmpty(conn, "customers")) {
                    seedCustomers(conn);
                }
                if (isTableEmpty(conn, "transactions")) {
                    seedTransactions(conn);
                }
                if (isTableEmpty(conn, "users")) {
                    seedUsers(conn);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Initialization Error: " + e.getMessage());
        }
    }

    private static boolean isTableEmpty(Connection conn, String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + tableName)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        }
        return true;
    }

    private static void seedMedicines(Connection conn) throws SQLException {
        String sql = "INSERT INTO medicines (code, name, category, stock, status, price) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Object[][] initialData = {
                {"MED001", "Paracetamol 500mg", "Analgesics", 120, "In Stock", 2.50},
                {"MED002", "Amoxicillin 250mg", "Antibiotics", 45, "In Stock", 8.20},
                {"MED003", "Ibuprofen 400mg", "Analgesics", 90, "In Stock", 3.10},
                {"MED004", "Lipitor 10mg", "Cardiovascular", 0, "Out of Stock", 45.00},
                {"MED005", "Metformin 500mg", "Antidiabetic", 75, "In Stock", 12.80},
                {"MED006", "Vitamin C 1000mg", "Vitamins", 15, "Low Stock", 5.50},
                {"MED007", "Omeprazole 20mg", "Gastrointestinal", 200, "In Stock", 15.00},
                {"MED008", "Amlodipine 5mg", "Cardiovascular", 110, "In Stock", 18.50},
                {"MED009", "Cetirizine 10mg", "Antihistamines", 180, "In Stock", 6.00},
                {"MED010", "Salbutamol Inhaler", "Respiratory", 25, "Low Stock", 35.00},
                {"MED011", "Diazepam 5mg", "Neurological", 0, "Out of Stock", 22.00},
                {"MED012", "Ciprofloxacin 500mg", "Antibiotics", 85, "In Stock", 14.50},
                {"MED013", "Loratadine 10mg", "Antihistamines", 150, "In Stock", 7.20},
                {"MED014", "Azithromycin 250mg", "Antibiotics", 40, "In Stock", 25.00},
                {"MED015", "Atorvastatin 20mg", "Cardiovascular", 95, "In Stock", 40.00},
                {"MED016", "Lisinopril 10mg", "Cardiovascular", 60, "In Stock", 16.50},
                {"MED017", "Levothyroxine 50mcg", "Endocrine", 130, "In Stock", 9.80},
                {"MED018", "Pantoprazole 40mg", "Gastrointestinal", 145, "In Stock", 17.50}
            };

            for (Object[] row : initialData) {
                pstmt.setString(1, (String) row[0]);
                pstmt.setString(2, (String) row[1]);
                pstmt.setString(3, (String) row[2]);
                pstmt.setInt(4, (Integer) row[3]);
                pstmt.setString(5, (String) row[4]);
                pstmt.setDouble(6, (Double) row[5]);
                pstmt.executeUpdate();
            }
        }
    }

    private static void seedCustomers(Connection conn) throws SQLException {
        String sql = "INSERT INTO customers (id, name, phone, email, purchases, spent) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Object[][] initialData = {
                {"C001", "Emma Watson", "555-0101", "emma@email.com", "12", "\u20B5450.20"},
                {"C002", "Michael B. Jordan", "555-0202", "michael@email.com", "4", "\u20B5120.00"},
                {"C003", "Sarah Connor", "555-0303", "sarah@email.com", "8", "\u20B5340.50"},
                {"C004", "Tom Hanks", "555-0404", "tom@email.com", "2", "\u20B545.00"},
                {"C005", "Zendaya Coleman", "555-0505", "zendaya@email.com", "15", "\u20B5890.75"}
            };
            for (Object[] row : initialData) {
                pstmt.setString(1, (String) row[0]);
                pstmt.setString(2, (String) row[1]);
                pstmt.setString(3, (String) row[2]);
                pstmt.setString(4, (String) row[3]);
                pstmt.setString(5, (String) row[4]);
                pstmt.setString(6, (String) row[5]);
                pstmt.executeUpdate();
            }
        }
    }

    private static void seedTransactions(Connection conn) throws SQLException {
        String sql = "INSERT INTO transactions (txn_id, customer_id, date_time, total) VALUES (?, ?, ?, ?)";
        String itemSql = "INSERT INTO transaction_items (txn_id, code, name, quantity, unit_price) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             PreparedStatement pItem = conn.prepareStatement(itemSql)) {
            
            // Txn 1
            pstmt.setString(1, "TXN001");
            pstmt.setString(2, "C001");
            pstmt.setString(3, "2026-06-15 14:30");
            pstmt.setDouble(4, 25.00);
            pstmt.executeUpdate();

            pItem.setString(1, "TXN001");
            pItem.setString(2, "MED001");
            pItem.setString(3, "Paracetamol 500mg");
            pItem.setInt(4, 10);
            pItem.setDouble(5, 2.50);
            pItem.executeUpdate();

            // Txn 2
            pstmt.setString(1, "TXN002");
            pstmt.setString(2, "Walk-in");
            pstmt.setString(3, "2026-06-16 09:15");
            pstmt.setDouble(4, 35.00);
            pstmt.executeUpdate();

            pItem.setString(1, "TXN002");
            pItem.setString(2, "MED010");
            pItem.setString(3, "Salbutamol Inhaler");
            pItem.setInt(4, 1);
            pItem.setDouble(5, 35.00);
            pItem.executeUpdate();

            // Txn 3
            pstmt.setString(1, "TXN003");
            pstmt.setString(2, "C005");
            pstmt.setString(3, "2026-06-17 11:45");
            pstmt.setDouble(4, 45.00);
            pstmt.executeUpdate();

            pItem.setString(1, "TXN003");
            pItem.setString(2, "MED004");
            pItem.setString(3, "Lipitor 10mg");
            pItem.setInt(4, 1);
            pItem.setDouble(5, 45.00);
            pItem.executeUpdate();
        }
    }

    // ==========================================================
    //                        DATA FETCHING
    // ==========================================================

    public static ObservableList<Medicine> loadMedicines() {
        ObservableList<Medicine> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM medicines";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Medicine(
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getInt("stock"),
                        rs.getString("status"),
                        rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static ObservableList<String[]> loadCustomers() {
        ObservableList<String[]> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM customers";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("purchases"),
                        rs.getString("spent")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static ObservableList<Transaction> loadTransactions() {
        ObservableList<Transaction> list = FXCollections.observableArrayList();
        String sqlTxn = "SELECT * FROM transactions ORDER BY txn_id DESC";
        String sqlItem = "SELECT * FROM transaction_items WHERE txn_id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmtTxn = conn.createStatement();
             ResultSet rsTxn = stmtTxn.executeQuery(sqlTxn);
             PreparedStatement pItem = conn.prepareStatement(sqlItem)) {

            while (rsTxn.next()) {
                String txnId = rsTxn.getString("txn_id");
                
                // Fetch items for this transaction
                pItem.setString(1, txnId);
                ResultSet rsItem = pItem.executeQuery();
                ObservableList<CartItem> items = FXCollections.observableArrayList();
                while (rsItem.next()) {
                    items.add(new CartItem(
                            rsItem.getString("code"),
                            rsItem.getString("name"),
                            rsItem.getInt("quantity"),
                            rsItem.getDouble("unit_price")
                    ));
                }

                Transaction t = new Transaction(
                        txnId,
                        rsTxn.getString("date_time"),
                        rsTxn.getString("customer_id"),
                        items,
                        rsTxn.getDouble("total")
                );
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ==========================================================
    //                        DATA MODIFICATION
    // ==========================================================

    public static void addMedicine(Medicine m) throws SQLException {
        String sql = "INSERT INTO medicines (code, name, category, stock, status, price) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, m.getCode());
            pstmt.setString(2, m.getName());
            pstmt.setString(3, m.getCategory());
            pstmt.setInt(4, m.getStock());
            pstmt.setString(5, m.getStatus());
            pstmt.setDouble(6, m.getPrice());
            pstmt.executeUpdate();
        }
    }

    public static void updateMedicineStock(String code, int newStock, String newStatus) throws SQLException {
        String sql = "UPDATE medicines SET stock = ?, status = ? WHERE code = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newStock);
            pstmt.setString(2, newStatus);
            pstmt.setString(3, code);
            pstmt.executeUpdate();
        }
    }

    public static void deleteMedicine(String code) throws SQLException {
        String sql = "DELETE FROM medicines WHERE code = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            pstmt.executeUpdate();
        }
    }

    public static void addCustomer(String[] c) throws SQLException {
        String sql = "INSERT INTO customers (id, name, phone, email, purchases, spent) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c[0]);
            pstmt.setString(2, c[1]);
            pstmt.setString(3, c[2]);
            pstmt.setString(4, c[3]);
            pstmt.setString(5, c[4]);
            pstmt.setString(6, c[5]);
            pstmt.executeUpdate();
        }
    }

    public static void deleteCustomer(String id) throws SQLException {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    public static void saveTransaction(Transaction txn) throws SQLException {
        String sql = "INSERT INTO transactions (txn_id, customer_id, date_time, total) VALUES (?, ?, ?, ?)";
        String itemSql = "INSERT INTO transaction_items (txn_id, code, name, quantity, unit_price) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             PreparedStatement pItem = conn.prepareStatement(itemSql)) {
            
            // Save transaction
            pstmt.setString(1, txn.getTxnId());
            pstmt.setString(2, txn.getCustomerId());
            pstmt.setString(3, txn.getDateTime());
            pstmt.setDouble(4, txn.getTotal());
            pstmt.executeUpdate();

            // Save items
            for (CartItem item : txn.getItems()) {
                pItem.setString(1, txn.getTxnId());
                pItem.setString(2, item.getCode());
                pItem.setString(3, item.getName());
                pItem.setInt(4, item.getQuantity());
                pItem.setDouble(5, item.getUnitPrice());
                pItem.executeUpdate();
            }
        }
    }

    // ==========================================================
    //                        USER AUTHENTICATION
    // ==========================================================

    /**
     * Seeds default user accounts for Role-Based Access Control.
     * Creates one Admin and one Cashier account for demonstration.
     */
    private static void seedUsers(Connection conn) throws SQLException {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Admin account
            pstmt.setString(1, "admin");
            pstmt.setString(2, "admin123");
            pstmt.setString(3, "Admin");
            pstmt.executeUpdate();

            // Cashier account
            pstmt.setString(1, "cashier");
            pstmt.setString(2, "cashier123");
            pstmt.setString(3, "Cashier");
            pstmt.executeUpdate();
        }
    }

    /**
     * Authenticates a user against the database.
     *
     * @param username The username entered at the login screen.
     * @param password The password entered at the login screen.
     * @return The user's role ("Admin" or "Cashier") if credentials are valid, or null if invalid.
     */
    public static String authenticateUser(String username, String password) {
        String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
