module org.example.eliteevents {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.eliteevents to javafx.fxml;
    exports org.example.eliteevents;
}