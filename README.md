# Pharmacy Stock Management Application - Complete Documentation

## Overview

The Pharmacy Stock Management Application is a comprehensive Java-based solution for managing pharmaceutical inventory, point-of-sale operations, and business analytics. Built with modern technologies and clean architecture principles.

**Version:** 1.0.0  
**Technology Stack:**
- **Frontend:** JavaFX 21.0.2 (FXML + CSS)
- **Backend:** Java 17+
- **Database:** Microsoft SQL Server (with MySQL/PostgreSQL support)
- **Build Tool:** Maven 3.6+
- **Logging:** SLF4J + Logback

---

## Project Structure

```
PharmacyManagement/
├── pom.xml                          # Maven dependencies and build configuration
├── src/
│   ├── main/java/com/pharmacy/
│   │   ├── Main.java               # Application entry point
│   │   ├── model/
│   │   │   ├── Medicine.java       # Medicine entity model
│   │   │   ├── Sale.java           # Sales transaction model
│   │   │   ├── User.java           # User authentication model
│   │   │   └── CartItem.java       # Shopping cart item model
│   │   ├── dao/
│   │   │   ├── DatabaseConnection.java   # JDBC connection manager
│   │   │   ├── MedicineDAO.java         # Medicine data operations
│   │   │   └── SaleDAO.java             # Sales data operations
│   │   ├── controller/
│   │   │   ├── DashboardController.java # Dashboard view logic
│   │   │   ├── InventoryController.java # Inventory management logic
│   │   │   ├── POSController.java       # Point of sale logic
│   │   │   └── AlertService.java        # Alert management
│   │   └── service/
│   │       ├── InventoryService.java    # Business logic - Inventory
│   │       ├── SalesService.java        # Business logic - Sales
│   │       └── StockAlertService.java   # Stock monitoring logic
│   └── main/resources/
│       ├── fxml/
│       │   ├── Main.fxml           # Main application layout
│       │   ├── Dashboard.fxml      # Dashboard view
│       │   ├── Inventory.fxml      # Inventory management view
│       │   └── POS.fxml            # Point of sale view
│       └── css/
│           └── styles.css          # Application styling
├── database/
│   ├── schema.sql                  # Database schema creation script
│   └── initial_data.sql            # Sample data insertion
└── README.md                        # This file
```

---

## Database Schema

### Tables

#### 1. **users**
Stores user credentials and role-based access control.

| Column | Type | Constraints |
|--------|------|-------------|
| user_id | INT | PRIMARY KEY IDENTITY |
| username | VARCHAR(50) | UNIQUE, NOT NULL |
| password_hash | VARCHAR(255) | NOT NULL |
| full_name | VARCHAR(100) | NOT NULL |
| email | VARCHAR(100) | |
| role | VARCHAR(20) | CHECK (ADMIN, PHARMACIST, CASHIER) |
| is_active | BIT | DEFAULT 1 |
| created_at | DATETIME | DEFAULT GETDATE() |
| updated_at | DATETIME | DEFAULT GETDATE() |

#### 2. **medicines**
Core inventory table for pharmaceutical products.

| Column | Type | Constraints |
|--------|------|-------------|
| medicine_id | INT | PRIMARY KEY IDENTITY |
| medicine_name | VARCHAR(255) | NOT NULL |
| category | VARCHAR(50) | NOT NULL |
| batch_number | VARCHAR(100) | NOT NULL |
| expiry_date | DATE | NOT NULL |
| supplier | VARCHAR(100) | NOT NULL |
| quantity_in_stock | INT | NOT NULL, >= 0 |
| unit_price | DECIMAL(10,2) | NOT NULL, > 0 |
| reorder_level | INT | DEFAULT 10, > 0 |
| reorder_quantity | INT | DEFAULT 50, > 0 |
| created_at | DATETIME | DEFAULT GETDATE() |
| updated_at | DATETIME | DEFAULT GETDATE() |
| created_by | INT | FOREIGN KEY(users) |

**Indexes:**
- `idx_medicine_name`: For fast name searches
- `idx_expiry_date`: For expiry tracking
- `idx_quantity`: For stock level monitoring

#### 3. **sales**
Records all sales transactions.

| Column | Type | Constraints |
|--------|------|-------------|
| sale_id | INT | PRIMARY KEY IDENTITY |
| sale_date | DATETIME | DEFAULT GETDATE() |
| total_amount | DECIMAL(12,2) | NOT NULL, >= 0 |
| payment_method | VARCHAR(20) | CHECK (CASH, CARD, CHEQUE) |
| cashier_id | INT | FOREIGN KEY(users) NOT NULL |
| notes | VARCHAR(500) | |
| created_at | DATETIME | DEFAULT GETDATE() |

#### 4. **sale_items**
Itemized details for each sale.

| Column | Type | Constraints |
|--------|------|-------------|
| sale_item_id | INT | PRIMARY KEY IDENTITY |
| sale_id | INT | FOREIGN KEY(sales), CASCADE DELETE |
| medicine_id | INT | FOREIGN KEY(medicines) |
| quantity_sold | INT | NOT NULL, > 0 |
| unit_price | DECIMAL(10,2) | NOT NULL, > 0 |
| total_price | DECIMAL(12,2) | NOT NULL, > 0 |
| created_at | DATETIME | DEFAULT GETDATE() |

#### 5. **stock_adjustments**
Tracks manual inventory adjustments.

| Column | Type | Constraints |
|--------|------|-------------|
| adjustment_id | INT | PRIMARY KEY IDENTITY |
| medicine_id | INT | FOREIGN KEY(medicines) |
| adjustment_quantity | INT | NOT NULL |
| reason | VARCHAR(100) | CHECK (DAMAGED, EXPIRED, RETURNED, CORRECTION) |
| adjusted_by | INT | FOREIGN KEY(users) |
| notes | VARCHAR(500) | |
| created_at | DATETIME | DEFAULT GETDATE() |

#### 6. **audit_log**
Compliance and audit trail for all system activities.

| Column | Type | Constraints |
|--------|------|-------------|
| log_id | INT | PRIMARY KEY IDENTITY |
| user_id | INT | FOREIGN KEY(users) |
| action | VARCHAR(50) | NOT NULL |
| table_name | VARCHAR(50) | |
| record_id | INT | |
| old_value | VARCHAR(500) | |
| new_value | VARCHAR(500) | |
| created_at | DATETIME | DEFAULT GETDATE() |

---

## Views

### vw_low_stock_medicines
Displays medicines with quantities at or below reorder level.

### vw_expiring_medicines
Shows medicines expiring within 30 days.

### vw_daily_sales_summary
Provides daily sales statistics and revenue tracking.

### vw_inventory_value
Calculates total inventory value and statistics.

---

## Stored Procedures

### sp_add_stock
Adds stock to medicines with automatic audit logging.

```sql
EXEC sp_add_stock @medicine_id=1, @quantity=50, @notes='Reorder received'
```

### sp_process_sale
Processes sales with automatic stock deduction and validation.

```sql
EXEC sp_process_sale @sale_id=1, @medicine_id=1, @quantity_sold=5, @unit_price=12.00
```

---

## Java Architecture

### Model Layer (`com.pharmacy.model`)

**Medicine.java**
- Represents a medicine in the inventory
- Key methods:
  - `isExpired()`: Check if medicine is expired
  - `isExpiringSoon()`: Check if expiring within 30 days
  - `isStockLow()`: Check if below reorder level
  - `getDaysUntilExpiry()`: Get days until expiration
  - `getStockValue()`: Calculate total stock value

**Sale.java**
- Represents a sales transaction
- Payment methods: CASH, CARD, CHEQUE

**User.java**
- User authentication and authorization
- Roles: ADMIN, PHARMACIST, CASHIER

**CartItem.java**
- Represents items in POS shopping cart
- Calculates total price per item

### DAO Layer (`com.pharmacy.dao`)

**DatabaseConnection.java**
- Singleton pattern for connection management
- Supports SQL Server, MySQL, PostgreSQL
- Features:
  - Connection pooling
  - Prepared statement support
  - Resource cleanup

**MedicineDAO.java**
- CRUD operations for medicines
- Key methods:
  - `createMedicine(Medicine)`: Add new medicine
  - `getAllMedicines()`: Retrieve all medicines
  - `getMedicineById(int)`: Get specific medicine
  - `getLowStockMedicines()`: Get low stock items
  - `getExpiringMedicines()`: Get expiring items
  - `searchMedicines(String)`: Full-text search
  - `updateStock(int, int)`: Deduct from inventory

**MedicineDAO.java** (Example Query)
```java
// Prepared statement for safety
String sql = "SELECT * FROM medicines WHERE medicine_id = ?";
PreparedStatement ps = connection.prepareStatement(sql);
ps.setInt(1, medicineId);
ResultSet rs = ps.executeQuery();
```

### Service Layer (`com.pharmacy.service`)

**InventoryService.java**
- Business logic for inventory management
- Validations and calculations
- Key methods:
  - `addMedicine(Medicine)`
  - `updateMedicine(Medicine)`
  - `getTotalInventoryValue()`
  - `getAverageMedicinePrice()`
  - `getMedicinesToReorder()`

**AlertService.java**
- Centralized alert management
- Stock and expiry monitoring
- Visual indicators and notifications

**SalesService.java**
- Business logic for sales transactions
- Cart management
- Payment processing

### Controller Layer (`com.pharmacy.controller`)

**InventoryController.java**
- Manages inventory view interactions
- Handles CRUD operations
- Filters and searches

**DashboardController.java**
- Dashboard statistics and summaries
- Real-time alerts

**POSController.java**
- Point of sale operations
- Cart management
- Transaction processing

---

## Dependencies (pom.xml)

### JavaFX
```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21.0.2</version>
</dependency>
```

### Database Drivers
- **MS SQL Server:** `mssql-jdbc:12.4.1.jre17`
- **MySQL:** `mysql-connector-java:8.0.33`
- **PostgreSQL:** `postgresql:42.6.0`

### Logging
- **SLF4J API:** `slf4j-api:2.0.7`
- **Logback:** `logback-classic:1.4.11`

### Build Plugins
- Maven Compiler: Java 17 compatibility
- JavaFX Maven Plugin: FXML support
- Shade Plugin: Executable JAR creation

---

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Microsoft SQL Server (or MySQL/PostgreSQL)

### 1. Database Setup

**SQL Server:**
```sql
-- Execute database_schema.sql to create tables, views, and procedures
-- Then execute initial_data.sql to populate sample data
```

**Connection String for SQL Server:**
```
jdbc:sqlserver://localhost:1433;databaseName=PharmacyManagement
```

### 2. Update Database Configuration

Edit `DatabaseConnection.java`:
```java
private static final String DB_URL = "jdbc:sqlserver://your_host:1433;databaseName=PharmacyManagement";
private static final String DB_USER = "your_username";
private static final String DB_PASSWORD = "your_password";
```

### 3. Build and Run

```bash
# Clone or download the project
cd PharmacyManagement

# Build with Maven
mvn clean package

# Run the application
mvn javafx:run

# Or run the JAR directly (after building)
java -jar target/PharmacyManagement-1.0.0.jar
```

---

## Features Overview

### 1. Dashboard
- **Real-time Master Data Sync:** Overview statistics pull directly from the active inventory list.
- **Dynamic Search & Filtering:** Filter medicines by category or search term instantly.
- **Low Stock & Out of Stock Alerts:** Visual status indicators automatically calculate based on stock quantity.
- **Total Valuation Calculation:** Instantly calculates the total monetary value of all inventory.

### 2. Inventory Management
- **Add/Remove Operations:** Full control over the medicine database.
- **Quick Restock Action:** Dedicated form for rapidly updating stock quantities with automatic status reassignment.
- **Smart Formatting:** Table columns utilize `CellFactory` to automatically format prices in Ghana Cedis (₵).
- **Responsive Layout:** The `TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS` ensures tables scale perfectly to any window size.

### 3. Point of Sale (POS)
- **Live Cart System:** Select medicines from an auto-filtered dropdown (only in-stock items) and specify quantities.
- **Automated Calculations:** `CartItem` models automatically calculate line subtotals, while the UI instantly tallies the grand total.
- **Real-Time Change Calculator:** Input the amount tendered and instantly see the change due. Sales are automatically blocked if the amount is insufficient.
- **Stock Validation:** Prevents adding more items to the cart than exist in physical inventory.
- **Transaction Processing:** Completing a sale automatically deducts stock, updates the dashboard statistics, and generates a new Transaction record.
- **Transaction History Filtering:** Filter past receipts by "Today", "This Week", "This Month", or "All Time" to streamline end-of-day accounting.
- **Digital Receipts:** View detailed, styled popup receipts for any past transaction.

### 4. Customer Records
- **Customer Database:** Add and manage customer profiles (Name, Phone, Email).
- **Purchase Tracking:** Track the number of purchases and total revenue generated per customer.
- **Universal Search:** Quickly find customers by name, ID, or contact details.

### 5. System Settings
- **Configuration Panel:** Manage General Settings, Security (Timeouts, 2FA), Notifications, and Data Backups.
- **Data Export:** UI flows for database backup and CSV export operations.

### 6. UI/UX Design (Glassmorphism)
- **Dark Theme Aesthetics:** Built with a rich `#0c0d10` dark background and translucent pane backgrounds (`rgba(255, 255, 255, 0.03)`).
- **Vibrant Accents:** Uses striking cyan (`#00f2fe`) and teal (`#4facfe`) gradients for active elements.
- **Micro-Animations:** Hover effects on buttons, dropdowns, and table rows to provide a responsive feel.
- **Custom Scrollbars and Popups:** Every element, including ComboBox dropdowns and Alert dialogs, is deeply styled to match the dark theme.

---

## Error Handling

### Database Errors
```java
try {
    medicine = medicineDAO.getMedicineById(medicineId);
} catch (SQLException e) {
    logger.error("Database error occurred", e);
    AlertService.showErrorAlert("Database Error", 
        "Failed to retrieve medicine. Please try again.");
}
```

### Validation Errors
```java
try {
    inventoryService.addMedicine(medicine);
} catch (IllegalArgumentException e) {
    AlertService.showWarningAlert("Validation Error", e.getMessage());
}
```

### Connection Errors
```java
if (!DatabaseConnection.getInstance().testConnection()) {
    AlertService.showErrorAlert("Connection Error", 
        "Cannot connect to database. Check configuration.");
}
```

---

## Best Practices Implemented

### 1. Object-Oriented Design
- Clear separation of concerns
- Single Responsibility Principle
- Encapsulation of data

### 2. Clean Code
- Meaningful variable and method names
- Proper comments for complex logic
- DRY (Don't Repeat Yourself) principle

### 3. Security
- Prepared statements prevent SQL injection
- Password hashing with SHA-256
- Role-based access control (RBAC)
- Audit logging for compliance

### 4. Performance
- Database indexing on frequently queried columns
- Connection pooling
- Lazy loading of data
- Optimized queries

### 5. Maintainability
- Centralized configuration
- Logging at appropriate levels
- Clear error messages
- Comprehensive documentation

---

## Sample Usage

### Adding a Medicine

```java
Medicine medicine = new Medicine(
    "Paracetamol 500mg",
    "Analgesics",
    "BATCH001",
    LocalDate.of(2025, 12, 31),
    "PharmaCorp",
    150,
    5.50
);

int medicineId = inventoryService.addMedicine(medicine);
if (medicineId > 0) {
    AlertService.showInfoAlert("Success", "Medicine added successfully");
}
```

### Processing a Sale

```java
Sale sale = new Sale();
sale.setSaleDate(LocalDateTime.now());
sale.setPaymentMethod(Sale.PaymentMethod.CASH);
sale.setCashierId(currentUserId);

// Add items to cart
CartItem item = new CartItem(medicine, 5, 12.00);

// Process sale
int saleId = salesService.processSale(sale, cartItems);
```

### Getting Alerts

```java
List<Medicine> medicines = inventoryService.getAllMedicines();
List<Medicine> alerts = AlertService.getAlertMedicines(medicines);

for (Medicine med : alerts) {
    String alertType = AlertService.getAlertType(med);
    String message = AlertService.getAlertMessage(med);
    System.out.println(message);
}
```

---

## Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| Database connection refused | Check host/port, SQL Server running, credentials correct |
| FXML not found | Ensure resources folder structure is correct, rebuild project |
| TableView not populating | Check DAO queries, verify data in database |
| OutOfMemoryError | Increase JVM heap size with `-Xmx1024m` |
| CSS not applying | Verify CSS file path, check syntax, hard refresh UI |

### Debugging

Enable debug logging in `logback.xml`:
```xml
<root level="DEBUG">
    <appender-ref ref="CONSOLE"/>
</root>
```

---

## Future Enhancements

- [ ] Barcode scanning integration
- [ ] Email notifications for alerts
- [ ] Receipt printing
- [ ] Multi-location support
- [ ] Advanced reporting (PDF exports)
- [ ] Mobile app companion
- [ ] User authentication UI
- [ ] Backup and restore functionality
- [ ] Supplier management module
- [ ] Purchase order automation
- [ ] Customer loyalty program
- [ ] Integration with accounting software

---

## Contributing

1. Follow the coding standards
2. Write meaningful commit messages
3. Test all changes before submitting
4. Document new features
5. Update this README for major changes

---

## License

This project is proprietary software. All rights reserved.

---

## Support

For issues or questions:
- Check the troubleshooting section
- Review the code comments
- Consult the database schema documentation
- Check application logs in `logs/` directory

---

## Contact

**Development Team:** Pharmacy Management Project  
**Version:** 1.1.0  
**Last Updated:** 2026-06-17

---

## Changelog

### Version 1.1.0 (Current Updates)
- ✅ Added real-time Change Calculator in the POS interface
- ✅ Implemented time-based filters (Today, This Week, This Month) for Transaction History
- ✅ Added a modern cyan medical cross Application Icon
- ✅ Added secure Login Screen with "Enter" key quick-submit
- ✅ Real-time low-stock alert syncing across the dashboard
- ✅ Expanded sample medicine database

### Version 1.0.0 (Initial Release)
- ✅ Core inventory management
- ✅ Database schema and setup
- ✅ JavaFX UI framework
- ✅ POS functionality
- ✅ Alert system
- ✅ Audit logging
- ✅ User authentication structure
- ✅ Comprehensive documentation
