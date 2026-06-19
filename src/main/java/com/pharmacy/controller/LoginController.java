package com.pharmacy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller responsible for handling the authentication flow.
 * Validates user credentials and transitions to the main dashboard application upon success.
 */
public class LoginController {

    @FXML private AnchorPane rootPane;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;

    /**
     * Handles the login button click or Enter key submission.
     * Validates the provided credentials against the SQLite user database.
     */
    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Login Failed", "Please enter both username and password.");
            return;
        }

        // Authenticate against the database
        String role = com.pharmacy.util.DatabaseManager.authenticateUser(username, password);

        if (role != null) {
            // Store the session globally
            com.pharmacy.util.UserSession.login(username, role);
            loadDashboard();
        } else {
            showAlert("Login Failed", "Invalid username or password.\n\nHint: admin/admin123 or cashier/cashier123");
        }
    }

    /**
     * Loads the primary dashboard view (main.fxml) and closes the login window.
     * Injects necessary CSS and application icons into the new Stage.
     */
    private void loadDashboard() {
        try {
            // Load the main dashboard UI hierarchy
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pharmacy/view/main.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("AetherRx \u2014 Pharmacy Management System");
            
            // Apply the custom application icon to the window
            java.net.URL iconUrl = getClass().getResource("/com/pharmacy/icon.png");
            if (iconUrl != null) {
                stage.getIcons().add(new javafx.scene.image.Image(iconUrl.toExternalForm()));
            }
            
            // Revert back to the exact window size requested by main UI
            Scene scene = new Scene(root, 1150, 720);
            
            // INJECT STYLES.CSS HERE SO THE UI STAYS INTACT
            java.net.URL cssUrl = getClass().getResource("/com/pharmacy/style/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.setMinWidth(1000);
            stage.setMinHeight(650);
            stage.setMaximized(true);
            stage.show();

            // Close the login window
            ((Stage) rootPane.getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load the main dashboard.");
        }
    }

    /**
     * Displays an error alert dialog to the user.
     *
     * @param title   The title of the alert window.
     * @param content The descriptive error message to display.
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
