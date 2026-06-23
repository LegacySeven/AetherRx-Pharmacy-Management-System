package com.pharmacy.util;

import com.pharmacy.model.Medicine;
import com.pharmacy.model.Transaction;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Utility class for exporting inventory and transaction data to CSV files.
 * Uses a native JavaFX FileChooser dialog so the user can pick the save location visually.
 */
public class CsvExporter {

    /**
     * Exports the current inventory table data to a CSV file.
     *
     * @param medicines The observable list of medicines currently displayed in the inventory.
     * @param ownerStage The parent window stage, used to block interaction while the dialog is open.
     */
    public static void exportInventory(ObservableList<Medicine> medicines, Stage ownerStage) {
        // 1. Instantiate the native file chooser dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Inventory to CSV");
        
        // 2. Pre-fill the filename with the current date for convenience
        fileChooser.setInitialFileName("Inventory_Export_" + java.time.LocalDate.now() + ".csv");
        
        // 3. Restrict the file types the user can select to strictly .csv files
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        // 4. Present the Save Dialog to the user and wait for their input
        File file = fileChooser.showSaveDialog(ownerStage);
        
        // 5. If the user clicks "Cancel" or closes the dialog, gracefully abort
        if (file == null) return; 

        // 6. Attempt to write to the selected file using a PrintWriter wrapped around a FileWriter
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // 7. Write the standard CSV Header row
            pw.println("Code,Medicine Name,Category,Stock,Status,Unit Price");

            // 8. Iterate through the inventory list and write each row
            for (Medicine m : medicines) {
                pw.printf("%s,\"%s\",%s,%d,%s,%.2f%n",
                        m.getCode(),
                        // Replace internal quotes with double quotes to prevent CSV parsing errors
                        m.getName().replace("\"", "\"\""), 
                        m.getCategory(),
                        m.getStock(),
                        m.getStatus(),
                        m.getPrice()
                );
            }
        } catch (IOException e) {
            // 9. Catch and log any file permission or IO errors that happen during writing
            e.printStackTrace();
        }
    }

    /**
     * Exports the entire transaction history to a CSV file.
     *
     * @param transactions The observable list of transactions representing the sales history.
     * @param ownerStage   The parent window stage for the dialog.
     */
    public static void exportTransactions(ObservableList<Transaction> transactions, Stage ownerStage) {
        // 1. Instantiate the native file chooser dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Transactions to CSV");
        
        // 2. Pre-fill the filename with the current date
        fileChooser.setInitialFileName("Sales_Export_" + java.time.LocalDate.now() + ".csv");
        
        // 3. Restrict the extension filter
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        // 4. Show the dialog and capture the selected file target
        File file = fileChooser.showSaveDialog(ownerStage);
        
        // 5. Abort if the user canceled the action
        if (file == null) return; 

        // 6. Open the file output stream
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // 7. Write the column headers for the sales data
            pw.println("Transaction ID,Date/Time,Items,Total");

            // 8. Iterate through the sales history and write the details
            for (Transaction t : transactions) {
                pw.printf("%s,%s,%d,%.2f%n",
                        t.getTxnId(),
                        t.getDateTime(),
                        t.getItemCount(),
                        t.getTotal()
                );
            }
        } catch (IOException e) {
            // 9. Handle potential IO errors during file creation
            e.printStackTrace();
        }
    }
}
