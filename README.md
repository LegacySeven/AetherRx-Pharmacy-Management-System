# Pharmacy Management System

A modern, robust, and visually stunning desktop-based **Pharmacy Management System** built with **Java 17+** and **JavaFX 21**. It features a premium glassmorphism dark-mode UI and uses an embedded **SQLite** database for a zero-configuration setup.

## Features

- **Authentication & RBAC**: Secure login system with Role-Based Access Control (Admin vs Cashier) and a real-time password visibility toggle.
- **Dashboard Analytics**: Real-time visualization of total revenue, transactions, and low-stock alerts, including an interactive 7-day revenue trend line chart.
- **Inventory Management**: Full CRUD operations for medicines with auto-generated IDs, duplicate prevention, and real-time stock tracking.
- **Point of Sale (POS)**: A streamlined POS module with live cart updates, automatic stock deduction, a real-time Change Calculator that prevents insufficient funds, and digital receipt generation.
- **Transaction History**: Time-based filtering (Today, This Week, This Month, All Time) with dynamic stat cards that instantly recalculate based on the filter.
- **Export to CSV**: Easily export both the inventory catalog and transaction history directly to CSV files for use in Microsoft Excel.

## Technology Stack

- **Frontend**: JavaFX 21.0.2 (FXML + CSS)
- **Backend**: Java 17+
- **Database**: SQLite (Embedded via `sqlite-jdbc` - no external server required!)
- **Build Tool**: Apache Maven 3.6+

## Project Structure

```text
PharmacyManagementSystem/
├── pom.xml                                   # Maven dependencies and build config
├── run.bat                                   # Zero-config auto-launcher script
├── pharmacy_management.db                    # Embedded SQLite Database (Auto-created)
├── src/
│   ├── main/java/com/pharmacy/
│   │   ├── App.java                          # Application Entry Point
│   │   ├── controller/
│   │   │   ├── LoginController.java          # Authentication Logic
│   │   │   └── MainController.java           # Dashboard, POS, and Inventory Logic
│   │   ├── model/
│   │   │   ├── Medicine.java                 # Inventory entity model
│   │   │   ├── Transaction.java              # Sales entity model
│   │   │   └── CartItem.java                 # POS cart item model
│   │   └── util/
│   │       ├── DatabaseManager.java          # SQLite schema, queries, and seeding
│   │       ├── CsvExporter.java              # CSV export utilities
│   │       └── UserSession.java              # Global role and session state
│   └── main/resources/com/pharmacy/
│       ├── icon.png                          # Application icon
│       ├── view/
│       │   ├── login.fxml                    # Login layout
│       │   └── main.fxml                     # Main application layout
│       └── style/
│           └── styles.css                    # CSS Styling (Glassmorphism & Dark Mode)
```

## Getting Started

### Prerequisites
- **Java 17** or higher installed and added to your system PATH.
- **Maven** (Optional, the provided `run.bat` can handle compilation via bundled wrappers or your local Maven).

### Running the Application (Windows)
The easiest way to run the project on Windows is using the provided batch script. It automatically compiles the JavaFX application and launches it:

1. Double-click `run.bat` in the root folder.
2. The system will compile the application and launch the GUI.
3. On the first launch, `pharmacy_management.db` will be auto-generated and seeded with initial data.

**Default Login Credentials:**
- **Admin:** `admin` / `admin123` (Full Access)
- **Cashier:** `cashier` / `cashier123` (Restricted Access - Cannot modify inventory)

### Running Manually via Maven
If you prefer to run it manually via a terminal:
```bash
mvn clean javafx:run
```

## Database Information
This project is designed for maximum portability. It does **NOT** require XAMPP, MySQL, or SQL Server. The entire database is contained within the `pharmacy_management.db` file, which is created automatically using SQLite. All queries are handled in the `DatabaseManager.java` utility class.

## Recent Updates
- **Dead Code Eradication:** Cleaned up unused files and legacy global variables.
- **Comprehensive Documentation:** Every Java class is now heavily documented with Javadocs and granular inline comments to explain the JavaFX bindings and SQL logic.
- **Dynamic Filtering:** The transaction history stat cards now dynamically update when searching for specific timeframes.
- **Password Visibility:** Added a UI toggle on the login screen for password visibility.
