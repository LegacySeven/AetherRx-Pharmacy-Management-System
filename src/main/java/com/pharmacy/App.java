package com.pharmacy;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application launcher for the Pharmacy Management System.
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

        // 1. Initialize local SQLite database before the UI loads to ensure data readiness
        com.pharmacy.util.DatabaseManager.initializeDatabase();
        
        try {
            // 2. Attempt to locate and load the login FXML view from resources
            URL fxmlUrl = getClass().getResource("/com/pharmacy/view/login.fxml");
            
            // 2a. Fail fast if the FXML resource cannot be found to prevent cryptic UI errors
            if (fxmlUrl == null) {
                System.err.println("FATAL: Cannot find FXML at /com/pharmacy/view/login.fxml");
                return;
            }
            // 2b. Log successful discovery of the FXML resource
            System.out.println("[OK] Found FXML: " + fxmlUrl);

            // 3. Parse the FXML file to instantiate the JavaFX scene graph nodes
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            // 3a. Log successful parsing of the scene graph
            System.out.println("[OK] FXML loaded successfully");

            // 4. Wrap the root node in a Scene, strictly enforcing the 500x450 dimensions for the login window
            Scene scene = new Scene(root, 500, 450);

            // 5. Load and apply the global CSS stylesheet for modern glassmorphism styling
            URL cssUrl = getClass().getResource("/com/pharmacy/style/styles.css");
            if (cssUrl != null) {
                // 5a. Attach the stylesheet to the scene so all nodes inherit the custom properties
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("[OK] CSS loaded: " + cssUrl);
            } else {
                // 5b. Provide a warning if CSS is missing, though the app will still function without it
                System.err.println("WARNING: Cannot find CSS at /com/pharmacy/style/styles.css");
            }

            // 6. Configure the primary stage (window) title properties
            stage.setTitle("Login \u2014 Pharmacy Management System");
            
            // 7. Attach the constructed Scene to the primary Stage
            stage.setScene(scene);
            
            // 8. Load the custom application icon from resources
            java.net.URL iconUrl = getClass().getResource("/com/pharmacy/icon.png");
            if (iconUrl != null) {
                // 8a. Apply the loaded image as the window and taskbar icon
                stage.getIcons().add(new javafx.scene.image.Image(iconUrl.toExternalForm()));
            }
            
            // 9. Prevent the user from resizing the window to maintain the login form's design integrity
            stage.setResizable(false);
            
            // 10. Finally, display the fully constructed window to the user
            stage.show();
            
            // 11. Log successful completion of the startup sequence
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
