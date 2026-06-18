## **Pharmacy Management System Project Report** 

## **1. Main Aim and Specific Objectives** 

## **Main Aim** 

To design, develop, and deploy a robust, secure, and user-friendly desktop-based Pharmacy Management System using JavaFX and Microsoft SQL Server to streamline inventory management, sales processing, and financial record-keeping for modern pharmacies. **Specific Objectives** 

- Implement a secure user authentication system for administrators. 

- Create an interactive Dashboard that visualizes real-time metrics (available medicines, total income, total customers) using dynamic area charts. 

- Build an Inventory Management module allowing users to execute CRUD (Create, Read, Update, Delete) operations on medicine records, including image uploading. 

- Design a Point of Sale (POS) module with cascading dropdowns, cart management, and anti-theft payment validation. 

- Ensure strict data integrity by utilizing a robust relational database (Microsoft SQL Server) with precise data-type matching. 

## **2. Introduction** 

## **Problem Statement** 

Pharmacies that rely on manual or paper-based record-keeping face critical operational challenges. Hand-written stock records lead to inventory errors, missing items, and difficulty tracking available stock. Furthermore, manually calculating prices and customer change during busy periods causes slow transactions and billing mistakes. Without automated reporting, pharmacy owners have no quick way to view total income or sales trends, and paper records are highly susceptible to data loss and security breaches. 

## **Purpose** 

The purpose of this project is to digitize and automate core pharmacy operations. It replaces manual counting and calculation with an automated system that manages stock levels, calculates precise customer change, and generates visual financial analytics to aid in business decision-making. 

## **Intended Users** 

- **Pharmacy Owners / Administrators:** For tracking overall income, analyzing sales charts, and securely managing inventory data. 

- **Pharmacists / Cashiers:** For rapidly searching the database, processing customer transactions via the POS system, and generating accurate checkout totals. 

## **3. Requirements** 

## **Software Requirements:** 

- **Language:** Java (JDK 26.0.1)

---

- **GUI Framework:** JavaFX SDK 

- **Database Engine:** Microsoft SQL Server Express 

- **Database Management:** SQL Server Management Studio (SSMS) 

- **IDE/Editor:** Visual Studio Code (VS Code) 

- **Drivers & Libraries:** Microsoft JDBC Driver for SQL Server (mssql-jdbc-13.4.0.jre11.jar and mssql-jdbc_auth.dll), FontAwesomeFX (fontawesomefx-8.2.jar) 

- **Version Control:** Git and GitHub 

## **Hardware Requirements:** 

- Processor: Intel Core i3 or equivalent minimum 

- RAM: 4 GB minimum (8 GB recommended for database hosting) 

- Storage: 500 MB free space minimum 

## **Setup & Configuration Tools:** 

- Customized .vscode/launch.json utilizing advanced VM arguments (--module-path, --add-modules javafx.controls,javafx.fxml, and --enable-native-access=ALL-UNNAMED) to allow Java 26 strict security protocols to interface with Windows native database authentication. 

## **4. Design** 

## **Use Case Flow:** 

1. Administrator launches the application and logs in with secure credentials. 

2. Users navigate to the **Add Medicines** screen to input new inventory (defining Type, Brand, Product Name, Price, and Image) or use the dynamic search bar to filter existing stock. 

3. The user navigates to the **Purchase Medicines** screen to process a customer sale. The system cascades selections (Type → Med ID → Brand → Product), calculates the cart total, strictly validates the payment amount against the total, processes the receipt to the database, and auto-clears the cart for the next customer. 

## **Data Structure (Relational Schema):** 

- The pharmacy database utilizes three primary tables to manage the application: 

   1. medicine: Stores the main inventory catalogue (Columns: _id, medicine_id, brand, productName, type, status, price, image, date_ ). 

   2. customer: Acts as the active, itemized ledger for purchases in the cart (Columns: _id, customer_id, type, medicine_id, brand, productName, quantity, price, date_ ). 

   3. customer_info: Stores finalized transaction receipts and grand totals (Columns: _id, customer_id, total, date_ ). 

## **Class Architecture (MVC Pattern):** 

   - **View (Presentation):** FXMLDocument.fxml, dashboard.fxml, loginDesign.css, dashboardDesign.css. 

   - **Controller (Logic):** FXMLDocumentController.java, dashboardController.java. 

- **Model (Data):** medicineData.java, customerData.java, getData.java, database.java. 

- **Screenshots & UI Design:** 

## _- - LegacySeven/Pharmacy Management System_

---

- [Insert image_7a00e5.png here] - **Dashboard Screen:** Shows real-time aggregate data and the income chart. 

- [Insert image_7a00c2.png here] - **Add Medicines Screen:** Features the inventory form, image preview, and the real-time FilteredList table. 

- [Insert image_79fe15.png here] - **Purchase Medicines Screen:** Displays the cascading POS dropdowns, active cart table, and payment validation area. 

## **5. Implementation** 

## **Major Modules & Features:** 

1. **Authentication Module:** Validates admin credentials against the database before granting access to the main UI, supporting a secure logout and session-clearing workflow. 

2. **Dashboard Analytics Module:** Uses Microsoft SQL Server aggregate functions (SUM, COUNT, and TOP 9) mapped to Java variables to dynamically draw Area Charts and update total income/customer metric cards. 

3. **Inventory Management Module (Add Medicines):** Features a real-time FilteredList Search Bar that implements strict null-checking to instantly filter table rows by ID, brand, or type without crashing on empty values. Handles full CRUD database operations. 

4. **Point of Sale (POS) Module (Purchase Medicines):** * **Anti-Theft Logic:** Prevents the purchasePay() method from executing if the cashier enters a payment amount less than the cart total. 

   - **Cascading Dropdowns:** Dynamically populates dropdowns based on previous selections. 

   - **Auto-Clear Workflow:** Automatically resets all spinners, zeroes the register, and refreshes the cart table to a blank slate after a successful purchase. 

   - **Clear Cart Database Hook:** An integrated purchaseClearCart() function that safely wipes the UI and actively drops unpaid/pending items from the SQL database using a DELETE query to maintain inventory accuracy. 

## **6. Testin g** 

|**6. Testing**||||
|---|---|---|---|
|**Test Case**|**Feature Tested**|**Expected Result**|**Actual Result**|
|**TC-01**|Search Bar<br>Null-Handling|Filtering the table<br>when some<br>medicine properties<br>are null should not<br>crash the app.|Handled gracefully<br>due to explicit !=<br>null checks in the<br>Java Predicate<br>logic.|
|**TC-02**|Cascading<br>Dropdowns|Selecting a "Type"<br>should load<br>"Medicine IDs"<br>without causing a<br>StackOverflowError.|UI updates<br>smoothly. Infinite<br>event loops were<br>bypassed using<br>.setOnAction(null).|

---

|**TC-03**|Anti-Theft Payment|Clicking "Pay" with<br>a payment amount<br>lower than the cart<br>total should trigger<br>an error.|Alert pops up:<br>"Insufficient<br>Payment! The total<br>is $X". Transaction<br>is aborted.|
|---|---|---|---|
|**TC-04**|Auto-Clear Cart|After a successful<br>payment, the UI and<br>internal trackers<br>should reset for the<br>next customer.|Spinners reset to 0,<br>text fields clear, and<br>the TableView<br>blanks out securely.|
|**TC-05**|Database Syntax<br>Migration|Aggregate chart<br>queries (COUNT,<br>SUM, LIMIT) should<br>execute<br>successfully on MS<br>SQL Server.|Successfully<br>translated MySQL<br>syntax (LIMIT 9,<br>TIMESTAMP) to<br>SQL Server syntax<br>(TOP 9, indexed<br>fetching).|



## **7. Challenges & Conclusion** 

## **Key Issues Faced & Solutions:** 

1. **Strict Java 26 Native Security:** Modern JDK versions block unverified native libraries, which prevents the MS SQL JDBC driver from authenticating Windows credentials. 

   - _Solution:_ Modified the VS Code launch.json VM arguments to explicitly grant native access (--enable-native-access=ALL-UNNAMED) to the Microsoft JDBC Authentication DLL, successfully bridging Java and Windows Security. 

2. **Database Migration Syntax:** Porting the application's backend from MySQL to Microsoft SQL Server caused crashes due to differing SQL dialects (e.g., MS SQL rejecting LIMIT and strict naming conventions for alias columns). 

   - _Solution:_ Refactored the controller methods to use TOP instead of LIMIT, removed incompatible TIMESTAMP() wrappers, and fetched ResultSet data via array indices (e.g., getInt(1)) rather than literal string names. 

3. **UI Event Infinite Loops:** The FXML design initially caused the cascading purchase dropdowns to trigger each other infinitely in a loop, resulting in a StackOverflowError memory crash. 

   - _Solution:_ Surgically detached and re-attached the JavaFX event listeners (.setOnAction(null)) during the data-loading phases to safely break the loop and prevent duplicate triggers. 

## **Summary:**

---

The development of this Pharmacy Management System successfully yielded a modern, responsive, and highly secure desktop application. By utilizing JavaFX for a fluid front-end and Microsoft SQL Server for robust, relational data storage, the software solves the critical issues of manual inventory tracking, vulnerable point-of-sale processing, and lack of analytical oversight. 

## **Future Improvements:** 

- **Receipt Generation:** Implement a PDF library (like iText or Apache PDFBox) to generate and automatically print physical receipts for customers. 

- **Role-Based Access Control (RBAC):** Add multi-level user roles (e.g., Admin vs. Cashier) with restricted permissions to prevent standard cashiers from deleting inventory. 

- **Expiry Date Tracking:** Integrate an "Expiry Date" column to the medicine table to automatically trigger dashboard alerts when stock is nearing expiration.