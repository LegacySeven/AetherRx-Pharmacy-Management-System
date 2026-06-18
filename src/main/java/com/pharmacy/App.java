package com.pharmacy;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application launcher for the AetherRx Pharmacy Management System.
 */
public class App extends Application {

    /**
     * The main entry point for all JavaFX applications.
     * This method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     * @param stage The primary stage for this application, onto which the application scene can be set.
     */
    @Override
    public void start(Stage stage) {
        try {
            // Attempt to locate and load the login FXML view
            URL fxmlUrl = getClass().getResource("/com/pharmacy/view/login.fxml");
            if (fxmlUrl == null) {
                System.err.println("FATAL: Cannot find FXML at /com/pharmacy/view/login.fxml");
                return;
            }
            System.out.println("[OK] Found FXML: " + fxmlUrl);

            // Parse the FXML to create the JavaFX scene graph
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            System.out.println("[OK] FXML loaded successfully");

            // Initialize the scene with specific login window dimensions
            Scene scene = new Scene(root, 500, 450);

            // Load and apply the global CSS stylesheet for modern styling
            URL cssUrl = getClass().getResource("/com/pharmacy/style/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("[OK] CSS loaded: " + cssUrl);
            } else {
                System.err.println("WARNING: Cannot find CSS at /com/pharmacy/style/styles.css");
            }

            // Configure the primary stage properties
            stage.setTitle("Login \u2014 AetherRx");
            stage.setScene(scene);
            
            // Apply the custom application icon to the window and taskbar
            java.net.URL iconUrl = getClass().getResource("/com/pharmacy/icon.png");
            if (iconUrl != null) {
                stage.getIcons().add(new javafx.scene.image.Image(iconUrl.toExternalForm()));
            }
            
            // Prevent resizing to maintain login form design integrity
            stage.setResizable(false);
            stage.show();
            System.out.println("[OK] Application launched successfully!");

        } catch (Exception e) {
            System.err.println("====== APPLICATION STARTUP ERROR ======");
            e.printStackTrace();
            System.err.println("=======================================");
        }
    }

    /**
     * The standard main method for the Java application.
     * It delegates the launch process to the JavaFX application lifecycle.
     *
     * @param args Command line arguments passed to the application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
