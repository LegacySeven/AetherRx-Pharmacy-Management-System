package com.pharmacy.util;

import com.pharmacy.model.CartItem;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages the SQLite embedded database connection and provides methods
 * for querying, inserting, and seeding the data.
 * All operations use JDBC (Java Database Connectivity) to interact with the local database file.
 */
public class DatabaseManager {
    // 1. Define the connection URL for the SQLite database. This creates the file if it doesn't exist.
    private static final String URL = "jdbc:sqlite:pharmacy_management.db";

    /**
     * Initializes the database schema on application startup.
     * Creates necessary tables (medicines, transactions, transaction_items, users)
     * and seeds them with initial data if they are empty.
     */
    public static void initializeDatabase() {
        // 2. Establish a connection to the SQLite database
        try (Connection conn = DriverManager.getConnection(URL)) {
            if (conn != null) {
                // 3. Create a statement object to execute DDL (Data Definition Language) queries
                try (Statement stmt = conn.createStatement()) {
                    // 4. Create the 'medicines' table to store inventory data
                    stmt.execute("CREATE TABLE IF NOT EXISTS medicines (" +
                            "code TEXT PRIMARY KEY, " +
                            "name TEXT NOT NULL, " +
                            "category TEXT NOT NULL, " +
                            "stock INTEGER NOT NULL, " +
                            "status TEXT NOT NULL, " +
                            "price REAL NOT NULL" +
                            ");");

                    // 5. Create the 'transactions' table to record sale events
                    stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                            "txn_id TEXT PRIMARY KEY, " +
                            "date_time TEXT NOT NULL, " +
                            "total REAL NOT NULL" +
                            ");");

                    // 6. Create the 'transaction_items' table to record individual items sold per transaction
                    // Uses a foreign key to link back to the parent transaction
                    stmt.execute("CREATE TABLE IF NOT EXISTS transaction_items (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "txn_id TEXT NOT NULL, " +
                            "code TEXT NOT NULL, " +
                            "name TEXT NOT NULL, " +
                            "quantity INTEGER NOT NULL, " +
                            "unit_price REAL NOT NULL, " +
                            "FOREIGN KEY (txn_id) REFERENCES transactions (txn_id)" +
                            ");");

                    // 7. Create the 'users' table to handle system authentication and roles
                    stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                            "username TEXT PRIMARY KEY, " +
                            "password TEXT NOT NULL, " +
                            "role TEXT NOT NULL" +
                            ");");
                }

                // 8. Seed the database with initial mock data if the tables are newly created
                if (isTableEmpty(conn, "medicines")) seedMedicines(conn);
                if (isTableEmpty(conn, "transactions")) seedTransactions(conn);
                if (isTableEmpty(conn, "users")) seedUsers(conn);
            }
        } catch (SQLException e) {
            // 9. Log any critical SQL errors that occur during initialization
            System.err.println("Database Initialization Error: " + e.getMessage());
        }
    }

    /**
     * Utility method to check if a specific table contains any rows.
     * @param conn The active database connection
     * @param tableName The name of the table to check
     * @return true if the table has 0 rows, false otherwise
     * @throws SQLException If a database access error occurs
     */
    private static boolean isTableEmpty(Connection conn, String tableName) throws SQLException {
        // 1. Execute a COUNT query to get the total number of rows in the table
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + tableName)) {
            // 2. If the result set has data, extract the integer count
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        }
        // 3. Default to true (empty) if the query fails to return a row
        return true;
    }

    /**
     * Seeds the 'medicines' table with an initial catalog of pharmaceutical products.
     * @param conn The active database connection
     * @throws SQLException If the insertion fails
     */
    private static void seedMedicines(Connection conn) throws SQLException {
        // 1. Prepare the parameterized SQL INSERT statement for security and performance
        String sql = "INSERT INTO medicines (code, name, category, stock, status, price) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 2. Define a multidimensional array containing the mock inventory data
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

            // 3. Iterate through each mock item and execute the prepared statement
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

    /**
     * Placeholder method for seeding transactions if necessary.
     * Currently left empty as transactions should naturally occur through system usage.
     * @param conn The active database connection
     * @throws SQLException If execution fails
     */
    private static void seedTransactions(Connection conn) throws SQLException {
        // No initial transactions needed
    }

    /**
     * Seeds the 'users' table with default administrator and cashier accounts.
     * @param conn The active database connection
     * @throws SQLException If the insertion fails
     */
    private static void seedUsers(Connection conn) throws SQLException {
        // 1. Prepare the SQL INSERT statement for users
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 2. Insert the root Administrator account
            pstmt.setString(1, "admin");
            pstmt.setString(2, "admin123");
            pstmt.setString(3, "Admin");
            pstmt.executeUpdate();
            
            // 3. Insert the standard Cashier account
            pstmt.setString(1, "cashier");
            pstmt.setString(2, "cashier123");
            pstmt.setString(3, "Cashier");
            pstmt.executeUpdate();
        }
    }

    // ==========================================================
    //                        DATA FETCHING
    // ==========================================================

    /**
     * Retrieves all inventory items from the database and maps them to model objects.
     * @return An ObservableList of Medicine objects for binding to UI components
     */
    public static ObservableList<Medicine> loadMedicines() {
        // 1. Initialize an empty observable list to hold the loaded records
        ObservableList<Medicine> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM medicines";
        
        // 2. Execute the SELECT query
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            // 3. Iterate through the result set and construct Medicine instances
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
            // 4. Log errors to standard error stream
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Retrieves all historical transactions and their associated items.
     * @return An ObservableList of Transaction objects, ordered by date descending
     */
    public static ObservableList<Transaction> loadTransactions() {
        // 1. Initialize an empty observable list for the transactions
        ObservableList<Transaction> list = FXCollections.observableArrayList();
        
        // 2. Define queries for both the parent transaction and its child items
        String sqlTxn = "SELECT * FROM transactions ORDER BY date_time DESC";
        String sqlItems = "SELECT * FROM transaction_items WHERE txn_id = ?";
        
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmtTxn = conn.createStatement();
             ResultSet rsTxn = stmtTxn.executeQuery(sqlTxn)) {
             
            // 3. Iterate through each parent transaction
            while (rsTxn.next()) {
                String txnId = rsTxn.getString("txn_id");
                
                // 4. Initialize a sub-list for the items within this specific transaction
                ObservableList<CartItem> items = FXCollections.observableArrayList();
                
                // 5. Query the child items using the transaction ID as the foreign key
                try (PreparedStatement pstmtItems = conn.prepareStatement(sqlItems)) {
                    pstmtItems.setString(1, txnId);
                    ResultSet rsItems = pstmtItems.executeQuery();
                    while (rsItems.next()) {
                        // 6. Construct CartItem instances and add them to the sub-list
                        items.add(new CartItem(
                                rsItems.getString("code"),
                                rsItems.getString("name"),
                                rsItems.getInt("quantity"),
                                rsItems.getDouble("unit_price")
                        ));
                    }
                }
                
                // 7. Construct the parent Transaction object and add it to the main list
                Transaction t = new Transaction(
                        txnId,
                        rsTxn.getString("date_time"),
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

    /**
     * Inserts a newly created medicine into the inventory database.
     * @param m The Medicine object containing the data to insert
     * @throws SQLException If a database constraint is violated (e.g., duplicate code)
     */
    public static void addMedicine(Medicine m) throws SQLException {
        // 1. Prepare the INSERT statement mapped to the Medicine fields
        String sql = "INSERT INTO medicines (code, name, category, stock, status, price) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 2. Bind the Java object properties to the SQL parameters
            pstmt.setString(1, m.getCode());
            pstmt.setString(2, m.getName());
            pstmt.setString(3, m.getCategory());
            pstmt.setInt(4, m.getStock());
            pstmt.setString(5, m.getStatus());
            pstmt.setDouble(6, m.getPrice());
            
            // 3. Execute the insertion
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates the mutable fields of an existing medicine record.
     * @param code The primary key of the medicine to update
     * @param newStock The updated stock quantity
     * @param newPrice The updated monetary price
     * @param newStatus The updated textual status (e.g., "In Stock")
     * @throws SQLException If the update fails
     */
    public static void updateMedicineStockAndPrice(String code, int newStock, double newPrice, String newStatus) throws SQLException {
        // 1. Prepare the UPDATE statement
        String sql = "UPDATE medicines SET stock = ?, price = ?, status = ? WHERE code = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 2. Bind the updated values and target key
            pstmt.setInt(1, newStock);
            pstmt.setDouble(2, newPrice);
            pstmt.setString(3, newStatus);
            pstmt.setString(4, code);
            
            // 3. Execute the modification
            pstmt.executeUpdate();
        }
    }

    /**
     * Removes a medicine record entirely from the inventory database.
     * @param code The primary key of the medicine to delete
     * @throws SQLException If the deletion fails
     */
    public static void deleteMedicine(String code) throws SQLException {
        // 1. Prepare the DELETE statement
        String sql = "DELETE FROM medicines WHERE code = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 2. Bind the target key
            pstmt.setString(1, code);
            // 3. Execute the deletion
            pstmt.executeUpdate();
        }
    }

    /**
     * Persists a completed sale transaction and its associated items to the database.
     * Uses SQL transactions to ensure atomicity (all-or-nothing insertion).
     * @param t The Transaction object representing the sale
     * @throws SQLException If the persistence fails or rolls back
     */
    public static void saveTransaction(Transaction t) throws SQLException {
        String sqlTxn = "INSERT INTO transactions (txn_id, date_time, total) VALUES (?, ?, ?)";
        String sqlItem = "INSERT INTO transaction_items (txn_id, code, name, quantity, unit_price) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(URL)) {
            // 1. Disable auto-commit to begin an atomic SQL transaction block
            conn.setAutoCommit(false);
            
            // 2. Insert the parent transaction record
            try (PreparedStatement pTxn = conn.prepareStatement(sqlTxn)) {
                pTxn.setString(1, t.getTxnId());
                pTxn.setString(2, t.getDateTime());
                pTxn.setDouble(3, t.getTotal());
                pTxn.executeUpdate();
            }
            
            // 3. Iterate and insert all child items associated with this transaction
            try (PreparedStatement pItem = conn.prepareStatement(sqlItem)) {
                for (CartItem item : t.getItems()) {
                    pItem.setString(1, t.getTxnId());
                    pItem.setString(2, item.getCode());
                    pItem.setString(3, item.getName());
                    pItem.setInt(4, item.getQuantity());
                    pItem.setDouble(5, item.getUnitPrice());
                    pItem.executeUpdate();
                }
            }
            // 4. If all insertions succeeded, commit the transaction to the database
            conn.commit();
        }
    }

    /**
     * Validates a user's login credentials against the users table.
     * @param username The inputted username
     * @param password The inputted password
     * @return The user's role (e.g., "Admin") if successful, or null if invalid
     */
    public static String authenticateUser(String username, String password) {
        // 1. Query for the role matching the exact username and password combination
        String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 2. Bind the credentials to the statement to prevent SQL injection
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            
            // 3. If a match is found, return the role string
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // 4. Return null if no matching user was found or an error occurred
        return null;
    }

    // ==========================================================
    //                        REVENUE ANALYTICS
    // ==========================================================

    /**
     * Calculates the daily revenue sums over a specified past duration.
     * Useful for plotting analytical charts on the dashboard.
     * @param days The number of days to look back (including today)
     * @return A map of String dates (YYYY-MM-DD) to Double daily total revenue
     */
    public static Map<String, Double> getRevenueByDay(int days) {
        // 1. Initialize a LinkedHashMap to preserve the chronological ordering of dates
        Map<String, Double> revenue = new LinkedHashMap<>();

        // 2. Pre-fill the map with 0.0 for every date in the range. 
        // This ensures the chart still draws correctly even on days with zero sales.
        java.time.LocalDate today = java.time.LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            revenue.put(today.minusDays(i).toString(), 0.0);
        }

        // 3. Execute a SQL aggregate query grouping the sum of 'total' by 'date_time'
        String sql = "SELECT date(date_time) as day, SUM(total) as daily_total " +
                     "FROM transactions " +
                     "WHERE date(date_time) >= date('now', '-' || ? || ' days') " +
                     "GROUP BY day ORDER BY day ASC";
                     
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 4. Bind the 'days' parameter to the WHERE clause modifier
            pstmt.setInt(1, days);
            ResultSet rs = pstmt.executeQuery();
            
            // 5. Update the pre-filled map with actual revenue data where it exists
            while (rs.next()) {
                String day = rs.getString("day");
                double total = rs.getDouble("daily_total");
                if (day != null) {
                    // Overwrite the default 0.0 value with the SQL sum
                    revenue.put(day, total);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // 6. Return the finalized chronological map to the caller
        return revenue;
    }
}
