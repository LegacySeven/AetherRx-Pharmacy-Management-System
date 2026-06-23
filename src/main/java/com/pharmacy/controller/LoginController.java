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

    // 1. Root container of the login UI, used to acquire the current Window reference
    @FXML private AnchorPane rootPane;
    
    // 2. Input fields for capturing user credentials
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    
    // 3. Elements related to the password visibility toggle feature
    @FXML private TextField txtPasswordVisible;
    @FXML private javafx.scene.control.Button btnTogglePassword;

    @FXML
    public void initialize() {
        // Keep the hidden text field and the password field perfectly synced
        txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());
    }

    /**
     * Handles the login button click or Enter key submission.
     * Validates the provided credentials against the SQLite user database.
     */
    @FXML
    private void handleLogin() {
        // 1. Retrieve and trim whitespace from the username input
        String username = txtUsername.getText().trim();
        // 2. Retrieve the password input (without trimming, as spaces may be valid in passwords)
        String password = txtPassword.getText();

        // 3. Basic client-side validation to prevent empty submissions
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Login Failed", "Please enter both username and password.");
            return;
        }

        // 4. Authenticate the credentials against the SQLite database using DatabaseManager
        // This returns the user's role ("Admin" or "Cashier") if successful, or null if it fails
        String role = com.pharmacy.util.DatabaseManager.authenticateUser(username, password);

        // 5. Check if authentication was successful
        if (role != null) {
            // 5a. Store the authenticated username and role globally in the UserSession
            // This is used across the app to enforce Role-Based Access Control (RBAC)
            com.pharmacy.util.UserSession.login(username, role);
            
            // 5b. Transition from the login window to the main dashboard application
            loadDashboard();
        } else {
            showAlert("Login Failed", "Invalid username or password.\n\nHint: admin/admin123 or cashier/cashier123");
        }
    }

    /**
     * Toggles the visibility of the password field to plain text.
     */
    @FXML
    private void handleTogglePassword() {
        if (txtPassword.isVisible()) {
            // Show plain text
            txtPassword.setVisible(false);
            txtPasswordVisible.setVisible(true);
            btnTogglePassword.setText("🙈");
        } else {
            // Hide plain text (show stars)
            txtPassword.setVisible(true);
            txtPasswordVisible.setVisible(false);
            btnTogglePassword.setText("👁");
        }
    }

    /**
     * Loads the primary dashboard view (main.fxml) and closes the login window.
     * Injects necessary CSS and application icons into the new Stage.
     */
    private void loadDashboard() {
        try {
            // 1. Locate and parse the main dashboard FXML hierarchy
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pharmacy/view/main.fxml"));
            Parent root = loader.load();

            // 2. Instantiate a new primary Stage (window) for the main application
            Stage stage = new Stage();
            stage.setTitle("Pharmacy Management System");
            
            // 3. Apply the custom application icon to the new window
            java.net.URL iconUrl = getClass().getResource("/com/pharmacy/icon.png");
            if (iconUrl != null) {
                stage.getIcons().add(new javafx.scene.image.Image(iconUrl.toExternalForm()));
            }
            
            // 4. Wrap the root node in a Scene, explicitly defining the initial resolution (1150x720)
            Scene scene = new Scene(root, 1150, 720);
            
            // 5. Inject the global CSS stylesheet into the newly created Scene
            // This is crucial to ensure the UI design system stays intact across views
            java.net.URL cssUrl = getClass().getResource("/com/pharmacy/style/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            // 6. Configure the stage constraints and assign the Scene
            stage.setScene(scene);
            stage.setMinWidth(1000); // Prevent horizontal crushing
            stage.setMinHeight(650); // Prevent vertical crushing
            stage.setMaximized(true); // Automatically maximize the window on modern screens
            
            // 7. Reveal the new dashboard window to the user
            stage.show();

            // 8. Gracefully close the old login window by acquiring its stage reference from rootPane
            ((Stage) rootPane.getScene().getWindow()).close();

        } catch (IOException e) {
            // 9. Catch and log any I/O exceptions that occur during FXML parsing
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
