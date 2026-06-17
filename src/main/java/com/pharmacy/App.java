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

    @Override
    public void start(Stage stage) {
        try {
            URL fxmlUrl = getClass().getResource("/com/pharmacy/view/login.fxml");
            if (fxmlUrl == null) {
                System.err.println("FATAL: Cannot find FXML at /com/pharmacy/view/login.fxml");
                return;
            }
            System.out.println("[OK] Found FXML: " + fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            System.out.println("[OK] FXML loaded successfully");

            Scene scene = new Scene(root, 500, 450);

            URL cssUrl = getClass().getResource("/com/pharmacy/style/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("[OK] CSS loaded: " + cssUrl);
            } else {
                System.err.println("WARNING: Cannot find CSS at /com/pharmacy/style/styles.css");
            }

            stage.setTitle("Login \u2014 AetherRx");
            stage.setScene(scene);
            
            java.net.URL iconUrl = getClass().getResource("/com/pharmacy/icon.png");
            if (iconUrl != null) {
                stage.getIcons().add(new javafx.scene.image.Image(iconUrl.toExternalForm()));
            }
            
            stage.setResizable(false);
            stage.show();
            System.out.println("[OK] Application launched successfully!");

        } catch (Exception e) {
            System.err.println("====== APPLICATION STARTUP ERROR ======");
            e.printStackTrace();
            System.err.println("=======================================");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
