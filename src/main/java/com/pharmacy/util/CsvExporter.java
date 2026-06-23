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
 * Uses a FileChooser dialog so the user can pick the save location.
 */
public class CsvExporter {

    /**
     * Exports the inventory table to a CSV file.
     *
     * @param medicines The list of medicines to export.
     * @param ownerStage The parent stage for the FileChooser dialog.
     */
    public static void exportInventory(ObservableList<Medicine> medicines, Stage ownerStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Inventory to CSV");
        fileChooser.setInitialFileName("Inventory_Export_" + java.time.LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(ownerStage);
        if (file == null) return; // User cancelled

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // Header row
            pw.println("Code,Medicine Name,Category,Stock,Status,Unit Price");

            // Data rows
            for (Medicine m : medicines) {
                pw.printf("%s,\"%s\",%s,%d,%s,%.2f%n",
                        m.getCode(),
                        m.getName().replace("\"", "\"\""),
                        m.getCategory(),
                        m.getStock(),
                        m.getStatus(),
                        m.getPrice()
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Exports the transaction history to a CSV file.
     *
     * @param transactions The list of transactions to export.
     * @param ownerStage   The parent stage for the FileChooser dialog.
     */
    public static void exportTransactions(ObservableList<Transaction> transactions, Stage ownerStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Transactions to CSV");
        fileChooser.setInitialFileName("Sales_Export_" + java.time.LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(ownerStage);
        if (file == null) return; // User cancelled

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // Header row
            pw.println("Transaction ID,Date/Time,Items,Total");

            // Data rows
            for (Transaction t : transactions) {
                pw.printf("%s,%s,%d,%.2f%n",
                        t.getTxnId(),
                        t.getDateTime(),
                        t.getItemCount(),
                        t.getTotal()
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
