package org.example.eliteevents;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.eliteevents.controllers.BookingFormController;

public class TestBookingForm extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("booking-form.fxml"));
        Scene scene = new Scene(loader.load());

        // Get the controller to verify it's created
        BookingFormController controller = loader.getController();
        System.out.println("Controller loaded: " + (controller != null));

        stage.setTitle("Test Booking Form");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}