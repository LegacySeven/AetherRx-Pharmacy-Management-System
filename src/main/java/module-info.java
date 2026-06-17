module com.pharmacy {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive javafx.base;
    
    // Allow JavaFX runtime to access main App class
    exports com.pharmacy;
    opens com.pharmacy to javafx.graphics, javafx.fxml;
    
    // Allow FXML loader to reflectively inject @FXML fields into controllers
    exports com.pharmacy.controller;
    opens com.pharmacy.controller to javafx.fxml;
    
    // Allow TableView to reflectively read property fields from models
    exports com.pharmacy.model;
    opens com.pharmacy.model to javafx.base;
}
