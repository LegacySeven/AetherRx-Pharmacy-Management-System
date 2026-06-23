# Pharmacy Management System - Project Report

## 1. Main Aim and Specific Objectives

### Main Aim
To design, develop, and deploy a robust, secure, and visually stunning desktop-based **Pharmacy Management System**. Utilizing Java 17+, JavaFX 21, and modern software architectures, the system streamlines inventory management, point-of-sale (POS) operations, and financial record-keeping while providing a premium "glassmorphism" dark-mode user experience.

### Specific Objectives
- Implement a secure user authentication system with Role-Based Access Control (Admin vs. Cashier) and rapid "Enter-key" submission workflows.
- Create an interactive Dashboard that visualizes real-time metrics (available medicines, low-stock alerts, total revenue) and a revenue trend line chart.
- Build an Inventory Management module allowing users to execute CRUD operations on medicine records with dynamic stock tracking, automatic ID generation, duplicate name prevention, and CSV export.
- Design a sophisticated Point of Sale (POS) module with a live cart, strict inventory validation, a real-time Change Calculator, and detailed digital receipts.
- Implement time-based transaction filtering (Today, This Week, This Month, All Time) for seamless end-of-day accounting.
- Deliver a premium, responsive UI utilizing advanced CSS styling and JavaFX custom cell factories.

## 2. Introduction

### Problem Statement
Pharmacies relying on manual record-keeping face critical operational challenges. Hand-written stock records lead to inventory errors, missing items, and an inability to track low stock efficiently. During busy periods, manually calculating prices and customer change causes slow transactions and billing mistakes. Without automated reporting and transaction filtering, pharmacy owners have no quick way to audit daily or weekly revenue, and paper records are highly susceptible to data loss. Furthermore, without access control, any staff member can modify critical inventory or financial data without accountability.

### Purpose
The purpose of this system is to digitize and automate core pharmacy operations with an emphasis on speed, security, and aesthetics. It replaces manual counting and calculation with an automated system that manages stock levels, instantly calculates customer change, synchronizes low-stock alerts in real-time, generates filtered financial analytics, and enforces role-based permissions to aid in business decision-making and security.

### Intended Users
- **Pharmacy Owners / Administrators:** Full access to all modules — tracking overall income, analyzing filtered transaction histories, managing inventory data, and exporting reports.
- **Pharmacists / Cashiers:** Restricted access — rapidly searching the database, processing customer transactions via the streamlined POS system, and generating accurate checkout totals and change.

## 3. Requirements

### Software Requirements:
- **Language:** Java (JDK 17 or higher)
- **GUI Framework:** JavaFX SDK 21+
- **Build Tool:** Apache Maven 3.6+
- **Database Engine:** SQLite (embedded via `sqlite-jdbc` — zero external setup required)
- **IDE/Editor:** Visual Studio Code / IntelliJ IDEA / Eclipse
- **Version Control:** Git and GitHub

### Hardware Requirements:
- Processor: Intel Core i3 or equivalent minimum
- RAM: 4 GB minimum
- Storage: 500 MB free space minimum

### Setup & Configuration Tools:
- Maven `pom.xml` configuration for dependency management, JavaFX compilation, and executing the application via `mvn clean javafx:run` or the integrated `run.bat` zero-config launcher script.

## 4. Design

### Use Case Flow:
1. Administrator or Cashier launches the application and logs in using secure credentials (with optimized Enter-key binding). The system validates credentials against the database and assigns the appropriate role.
2. Users navigate to the **Dashboard** to view live statistics (total medicines, revenue, low-stock count, out-of-stock count, total valuation) and an interactive revenue trend line chart showing the last 7 days.
3. Users navigate to the **Inventory** screen to input new medicines (benefiting from auto-generated IDs and duplicate prevention) or utilize the dynamic search bar to filter existing stock, update stock/prices instantly, delete records, and export the full inventory to CSV.
4. The user navigates to the **Point of Sale (POS)** screen to process a sale. The system allows selecting medicines, calculates the cart total, requires the cashier to input the amount tendered, instantly calculates the exact change due in green (or blocks the sale in red if insufficient), processes the transaction, deducts stock, and auto-clears the cart.
5. Users navigate to the **Transaction History** panel to filter past receipts by time periods (Today, Week, Month) to audit sales and export transaction data to CSV.
6. **Cashiers** are restricted from accessing the Inventory Management module; only Admins have full access to all features.

### Data Structure (Core Models):
- **Medicine:** Represents the inventory catalog (`code`, `name`, `category`, `stock`, `status`, `price`).
- **Transaction:** Acts as the ledger for completed purchases (`txnId`, `dateTime`, `customerId`, `total`, `items`).
- **CartItem:** Represents transient items currently in the POS cart before checkout (`code`, `name`, `quantity`, `unitPrice`, `subtotal`).

### Class Architecture (MVC Pattern):
- **View (Presentation):** `login.fxml`, `main.fxml`, `styles.css`.
- **Controller (Logic):** `App.java` (Entry Point), `LoginController.java` (Authentication & RBAC), `MainController.java` (Core Dashboard, POS, Inventory, and Customers logic).
- **Model (Data):** `Medicine.java`, `Transaction.java`, `CartItem.java`.
- **Utility:** `DatabaseManager.java` (SQLite CRUD & Analytics), `UserSession.java` (Session & Role Management), `CsvExporter.java` (CSV Report Generation).

## 5. Implementation

### Major Modules & Features:

1. **Authentication & Role-Based Access Control (RBAC):**
   - Validates credentials against a `users` table in the embedded SQLite database.
   - Implements a seamless `defaultButton` property in FXML to allow rapid Enter-key submission.
   - **Password Visibility Toggle:** A modern "eye" icon allows users to toggle password visibility in real-time.
   - Assigns roles (`Admin` or `Cashier`) via the `UserSession` singleton, which persists for the session duration.
   - **Cashier Lockout:** When a Cashier is logged in, the Inventory Management sidebar button is disabled and greyed out, preventing unauthorized data modification.

2. **Dashboard Analytics & Revenue Chart:**
   - Dynamically calculates total revenue, total transactions, low-stock items, out-of-stock items, and total inventory valuation.
   - **Interactive Revenue Line Chart:** A JavaFX `LineChart` displays revenue data for the last 7 days, populated from a grouped SQL query (`GROUP BY day`).
   - **Live Sync:** When an item is restocked in the POS or Inventory module, the low-stock alert on the Dashboard vanishes instantly without requiring an application restart.

3. **Inventory Management Module:**
   - **Smart Registration:** Features automatic sequence-based Medicine ID generation and real-time duplicate name prevention when adding new stock.
   - **Quick Update Panel:** Allows users to independently update the stock quantity and/or unit price of a selected medicine instantly.
   - Features a real-time `FilteredList` Search Bar to instantly filter table rows by name or ID.
   - Includes deletion confirmation dialogs and formats currency columns safely using JavaFX `CellFactory`.
   - **CSV Export:** An "Export CSV" button opens a `FileChooser` save dialog, allowing the admin to export the full inventory table (Code, Name, Category, Stock, Status, Price) to a `.csv` file for analysis in Microsoft Excel.

4. **Point of Sale (POS) Module:**
   - **Real-Time Change Calculator:** A dedicated `TextField` with a text property listener that mathematically compares the tendered amount against the cart total in real-time. It dynamically changes text color (Green for sufficient change, Red for insufficient) and prevents the "Complete Sale" button from firing if funds are short.
   - **Time-Based Transaction Filtering:** Utilizes a `ComboBox` linked to a `FilteredList` predicate and `java.time.LocalDateTime` parsing to instantly sort thousands of transaction records by "Today", "This Week", or "This Month".
   - **Dynamic Revenue Stats:** The "Total Transactions" and "Total Revenue" stat cards dynamically recalculate based on the active time filter, allowing for accurate instant auditing.
   - **Auto-Clear Workflow:** Automatically resets all inputs, zeroes the register, and refreshes the cart table to a blank slate after a successful purchase.
   - **CSV Export:** Transactions can be exported to CSV from the Transaction History panel.

5. **Embedded SQLite Database:**
   - The application uses an embedded SQLite database (`pharmacy_management.db`) via the `sqlite-jdbc` driver, requiring **zero external database setup**.
   - On first launch, the database is auto-created and seeded with 18 sample medicines, 2 sample customers, and 2 user accounts (admin/cashier).
   - All data persists across application restarts.

## 6. Testing

| Test Case | Feature Tested | Expected Result | Actual Result |
| :--- | :--- | :--- | :--- |
| **TC-01** | Real-Time Change Calculator | Entering an amount less than the total displays red "Insufficient". Entering a higher amount displays green "Change: ₵X". | Handled gracefully via `textProperty().addListener` and Double parsing. |
| **TC-02** | Anti-Theft Payment Block | Clicking "Complete Sale" with an insufficient tendered amount should trigger an Error Alert and abort the transaction. | Alert pops up: "Insufficient Funds...". Transaction is aborted. |
| **TC-03** | Time-Based Filtering | Selecting "Today" in the Transaction History dropdown should hide all receipts from yesterday. | UI filters smoothly using `FilteredList.setPredicate()` and `LocalDate.equals()`. |
| **TC-04** | Live Alert Synchronization | Restocking a "Low Stock" medicine from 5 to 50 should immediately remove it from the Dashboard Alert list. | The alert list updates instantly via shared `ObservableList` and UI refresh methods. |
| **TC-05** | UI Node Caching Stability | Navigating rapidly between POS, Inventory, and Dashboard should not cause Memory Leaks or `NullPointerExceptions`. | Stable. Page roots (`dashboardCenter`, `salesCenter`) are cached in variables preventing redundant FXML loading. |
| **TC-06** | Role-Based Access Control | Logging in as "cashier" should disable the Inventory sidebar button. | Button is greyed out. Clicking it does nothing. Only Admin can access those modules. |
| **TC-07** | CSV Export | Clicking "Export CSV" on the Inventory page should open a save dialog and write a valid CSV file. | File is saved successfully with correct headers and data. Opens properly in Excel. |
| **TC-08** | Revenue Chart | Processing a sale should cause the Dashboard revenue chart to reflect the new daily total. | Chart updates after sale completion via `populateRevenueChart()`. |
| **TC-09** | Duplicate Prevention | Attempting to register a medicine with a name that already exists should block the action. | Error alert displays warning about duplicate. Record is not saved. |
| **TC-10** | Price & Stock Update | Entering a new price without changing the stock should update the price on the table successfully. | Table and Database update correctly without zeroing out stock. |
## 7. Challenges & Conclusion

### Key Issues Faced & Solutions:

1. **UI Event Re-initialization & Null Pointers:**
   - *Issue:* When reloading views, variables referencing UI elements (like `salesCenter` or `dashboardCenter`) would sometimes be overwritten or nullified, causing the application to crash or throw compilation errors.
   - *Solution:* Implemented a strict node-caching mechanism within `MainController.java`. Instead of rebuilding the UI every time a user clicks a sidebar button, the application stores the built `VBox` layouts in memory and simply swaps them into the main `BorderPane`, preserving state and drastically improving performance.

2. **Real-Time Global State Synchronization:**
   - *Issue:* When a cashier processed a sale that caused an item to go out of stock, the Dashboard's "Low Stock" alert panel did not update until the application was restarted.
   - *Solution:* Refactored the core methods (`updateDashboardStatistics` and `loadSampleData`) to share references to the same underlying `ObservableList`. Calling `updateDashboardStatistics()` immediately after a sale or restock action forces the UI to re-evaluate the inventory and instantly add or remove alerts.

3. **Portable Database Deployment:**
   - *Issue:* Using an external database server (SQL Server, MySQL) would require the lecturer or end-user to install and configure the database engine separately, making portability difficult.
   - *Solution:* Migrated to an embedded SQLite database (`pharmacy_management.db`) via the `sqlite-jdbc` Maven dependency. The database file is auto-created alongside the application on first run, requiring zero external setup.

4. **Codebase Maintainability:**
   - *Issue:* The project was large and contained unused code and redundant files.
   - *Solution:* Fully refactored the codebase, removed dead code and obsolete test files, and added hyper-granular Javadoc and inline comments to every class (`MainController`, `App`, Models, etc.) to ensure long-term maintainability.

### Summary:
The development of the Pharmacy Management System successfully yielded a modern, responsive, and highly secure desktop application. By utilizing JavaFX with a premium dark-mode aesthetic and powerful background logic, the software solves the critical issues of manual inventory tracking, vulnerable point-of-sale processing, and lack of analytical oversight. The inclusion of the real-time change calculator, time-based transaction filters, interactive revenue charts, CSV export functionality, and role-based access control elevates it to an enterprise-ready solution.

### Future Improvements:
- **Receipt Generation:** Implement a PDF library (like iText or Apache PDFBox) to generate and automatically print physical receipts for customers.
- **Barcode Scanning:** Integrate a barcode scanning library to allow rapid medicine lookup at the point of sale.
- **Multi-Branch Support:** Extend the database schema to support multiple pharmacy branches with centralized reporting.
