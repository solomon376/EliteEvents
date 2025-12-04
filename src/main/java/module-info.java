module org.example.eliteevents {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;

    // Export all packages that need to be accessible
    exports org.example.eliteevents;
    exports org.example.eliteevents.controllers;
    exports org.example.eliteevents.models;
    exports org.example.eliteevents.services;

    // Open all packages to javafx.fxml for reflection
    opens org.example.eliteevents to javafx.fxml;
    opens org.example.eliteevents.controllers to javafx.fxml;
    opens org.example.eliteevents.models to javafx.fxml;
    opens org.example.eliteevents.services to javafx.fxml;
}