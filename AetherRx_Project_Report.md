# AetherRx Pharmacy Management System - Project Report

## 1. Main Aim and Specific Objectives

### Main Aim
To design, develop, and deploy **AetherRx**, a robust, secure, and visually stunning desktop-based Pharmacy Management System. Utilizing Java 17+, JavaFX 21, and modern software architectures, AetherRx streamlines inventory management, point-of-sale (POS) operations, and financial record-keeping while providing a premium "glassmorphism" dark-mode user experience.

### Specific Objectives
- Implement a secure user authentication system with rapid "Enter-key" submission workflows.
- Create an interactive Dashboard that visualizes real-time metrics (available medicines, low-stock alerts, total revenue).
- Build an Inventory Management module allowing users to execute CRUD operations on medicine records with dynamic stock tracking.
- Design a sophisticated Point of Sale (POS) module with a live cart, strict inventory validation, a real-time Change Calculator, and detailed digital receipts.
- Implement time-based transaction filtering (Today, This Week, This Month, All Time) for seamless end-of-day accounting.
- Deliver a premium, responsive UI utilizing advanced CSS styling and JavaFX custom cell factories.

## 2. Introduction

### Problem Statement
Pharmacies relying on manual record-keeping face critical operational challenges. Hand-written stock records lead to inventory errors, missing items, and an inability to track low stock efficiently. During busy periods, manually calculating prices and customer change causes slow transactions and billing mistakes. Without automated reporting and transaction filtering, pharmacy owners have no quick way to audit daily or weekly revenue, and paper records are highly susceptible to data loss.

### Purpose
The purpose of AetherRx is to digitize and automate core pharmacy operations with an emphasis on speed and aesthetics. It replaces manual counting and calculation with an automated system that manages stock levels, instantly calculates customer change, synchronizes low-stock alerts in real-time, and generates filtered financial analytics to aid in business decision-making.

### Intended Users
- **Pharmacy Owners / Administrators:** For tracking overall income, analyzing filtered transaction histories, and securely managing inventory data.
- **Pharmacists / Cashiers:** For rapidly searching the database, processing customer transactions via the streamlined POS system, and generating accurate checkout totals and change.

## 3. Requirements

### Software Requirements:
- **Language:** Java (JDK 17 or higher)
- **GUI Framework:** JavaFX SDK 21.0.2
- **Build Tool:** Apache Maven 3.6+
- **Database Engine:** Microsoft SQL Server / MySQL / PostgreSQL (via JDBC)
- **IDE/Editor:** Visual Studio Code / IntelliJ IDEA / Eclipse
- **Logging:** SLF4J + Logback
- **Version Control:** Git and GitHub

### Hardware Requirements:
- Processor: Intel Core i3 or equivalent minimum
- RAM: 4 GB minimum (8 GB recommended for database hosting)
- Storage: 500 MB free space minimum

### Setup & Configuration Tools:
- Maven `pom.xml` configuration for dependency management, JavaFX compilation, and executing the application via `mvn clean javafx:run` or the integrated `run.bat` script.

## 4. Design

### Use Case Flow:
1. Administrator launches the application and logs in using secure credentials (with optimized Enter-key binding).
2. Users navigate to the **Dashboard** to view live statistics and globally synchronized low-stock alerts.
3. Users navigate to the **Inventory** screen to input new medicines or utilize the dynamic search bar to filter existing stock and perform quick restocks.
4. The user navigates to the **Point of Sale (POS)** screen to process a sale. The system allows selecting medicines, calculates the cart total, requires the cashier to input the amount tendered, instantly calculates the exact change due in green (or blocks the sale in red if insufficient), processes the transaction, deducts stock, and auto-clears the cart.
5. Users navigate to the **Transaction History** panel to filter past receipts by time periods (Today, Week, Month) to audit sales.

### Data Structure (Core Models):
- **Medicine:** Represents the inventory catalog (`medicineId`, `medicineName`, `category`, `quantityInStock`, `unitPrice`).
- **Sale / Transaction:** Acts as the ledger for completed purchases (`txnId`, `dateTime`, `totalAmount`, `paymentMethod`).
- **CartItem:** Represents transient items currently in the POS cart before checkout (`medicine`, `quantity`, `unitPrice`, `subtotal`).

### Class Architecture (MVC Pattern):
- **View (Presentation):** `login.fxml`, `main.fxml`, `styles.css`.
- **Controller (Logic):** `App.java` (Entry Point), `LoginController.java` (Authentication), `MainController.java` (Core Dashboard, POS, and Inventory logic).
- **Model (Data):** `Medicine.java`, `Transaction.java`, `CartItem.java`.

## 5. Implementation

### Major Modules & Features:

1. **Authentication Module:**
   - Validates admin credentials before granting access to the main UI.
   - Implements a seamless `defaultButton` property in FXML to allow rapid Enter-key submission.
   - Handles the injection of global CSS styles and the application icon (`icon.png`) into the application windows.

2. **Dashboard Analytics & Alert Syncing:**
   - Dynamically calculates total revenue, total transactions, and low-stock items.
   - **Live Sync:** When an item is restocked in the POS or Inventory module, the low-stock alert on the Dashboard vanishes instantly without requiring an application restart.

3. **Inventory Management Module:**
   - Features a real-time `FilteredList` Search Bar to instantly filter table rows by name or ID.
   - Includes quick-restock actions and formats currency columns safely using JavaFX `CellFactory`.

4. **Point of Sale (POS) Module:**
   - **Real-Time Change Calculator:** A dedicated `TextField` with a text property listener that mathematically compares the tendered amount against the cart total in real-time. It dynamically changes text color (Green for sufficient change, Red for insufficient) and prevents the "Complete Sale" button from firing if funds are short.
   - **Time-Based Transaction Filtering:** Utilizes a `ComboBox` linked to a `FilteredList` predicate and `java.time.LocalDateTime` parsing to instantly sort thousands of transaction records by "Today", "This Week", or "This Month".
   - **Auto-Clear Workflow:** Automatically resets all inputs, zeroes the register, and refreshes the cart table to a blank slate after a successful purchase.

## 6. Testing

| Test Case | Feature Tested | Expected Result | Actual Result |
| :--- | :--- | :--- | :--- |
| **TC-01** | Real-Time Change Calculator | Entering an amount less than the total displays red "Insufficient". Entering a higher amount displays green "Change: ₵X". | Handled gracefully via `textProperty().addListener` and Double parsing. |
| **TC-02** | Anti-Theft Payment Block | Clicking "Complete Sale" with an insufficient tendered amount should trigger an Error Alert and abort the transaction. | Alert pops up: "Insufficient Funds...". Transaction is aborted. |
| **TC-03** | Time-Based Filtering | Selecting "Today" in the Transaction History dropdown should hide all receipts from yesterday. | UI filters smoothly using `FilteredList.setPredicate()` and `LocalDate.equals()`. |
| **TC-04** | Live Alert Synchronization | Restocking a "Low Stock" medicine from 5 to 50 should immediately remove it from the Dashboard Alert list. | The alert list updates instantly via shared `ObservableList` and UI refresh methods. |
| **TC-05** | UI Node Caching Stability | Navigating rapidly between POS, Inventory, and Dashboard should not cause Memory Leaks or `NullPointerExceptions`. | Stable. Page roots (`dashboardCenter`, `salesCenter`) are cached in variables preventing redundant FXML loading. |

## 7. Challenges & Conclusion

### Key Issues Faced & Solutions:

1. **UI Event Re-initialization & Null Pointers:** 
   - *Issue:* When reloading views, variables referencing UI elements (like `salesCenter` or `dashboardCenter`) would sometimes be overwritten or nullified, causing the application to crash or throw compilation errors.
   - *Solution:* Implemented a strict node-caching mechanism within `MainController.java`. Instead of rebuilding the UI every time a user clicks a sidebar button, the application stores the built `VBox` layouts in memory and simply swaps them into the main `BorderPane`, preserving state and drastically improving performance.

2. **Real-Time Global State Synchronization:**
   - *Issue:* When a cashier processed a sale that caused an item to go out of stock, the Dashboard's "Low Stock" alert panel did not update until the application was restarted.
   - *Solution:* Refactored the core methods (`updateDashboardStatistics` and `loadSampleData`) to share references to the same underlying `ObservableList`. Calling `updateDashboardStatistics()` immediately after a sale or restock action forces the UI to re-evaluate the inventory and instantly add or remove alerts.

### Summary:
The development of the AetherRx Pharmacy Management System successfully yielded a modern, responsive, and highly secure desktop application. By utilizing JavaFX with a premium dark-mode aesthetic and powerful background logic, the software solves the critical issues of manual inventory tracking, vulnerable point-of-sale processing, and lack of analytical oversight. The inclusion of the real-time change calculator and time-based transaction filters elevates it to an enterprise-ready solution.

### Future Improvements:
- **Receipt Generation:** Implement a PDF library (like iText or Apache PDFBox) to generate and automatically print physical receipts for customers.
- **Database Backend Hookup:** Transition the current sample data loading to a live MySQL/SQL Server JDBC connection.
- **Role-Based Access Control (RBAC):** Add multi-level user roles (e.g., Admin vs. Cashier) with restricted permissions to prevent standard cashiers from deleting inventory.
