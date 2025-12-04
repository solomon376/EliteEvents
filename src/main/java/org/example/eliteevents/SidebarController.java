package org.example.eliteevents;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SidebarController {

    public Button btnNewBooking;
    private MainController main;

    public void setMainController(MainController controller) {
        this.main = controller;
    }

    @FXML
    private void onDashboard() {
        if (main != null) {
            main.loadPage("dashboard.fxml");
        } else {
            System.err.println("Main controller is null!");
        }
    }

    @FXML
    private void onNewBooking() {
        if (main != null) main.loadPage("booking-form.fxml");
    }

    @FXML
    private void onAllBookings() {
        if (main != null) main.loadPage("bookings-list.fxml");
    }

    @FXML
    private void onVenues() {
        if (main != null) main.loadPage("venues.fxml");
    }

    @FXML
    private void onVendors() {
        if (main != null) main.loadPage("vendors.fxml");
    }

    @FXML
    private void onClients() {
        if (main != null) main.loadPage("clients.fxml");
    }

    @FXML
    private void onPayments() {
        if (main != null) main.loadPage("payments.fxml");
    }

    @FXML
    private void onSettings() {
        if (main != null) main.showPlaceholder("Settings Page - Coming Soon");
    }

    //
    @FXML
    private void onAddClient() {
        if (main != null) main.loadPage("client-form.fxml");
    }

    @FXML
    private void onAddVenue() {
        if (main != null) main.loadPage("venue-form.fxml");
    }

    @FXML
    private void onAddVendor() {
        if (main != null) main.loadPage("vendor-form.fxml");
    }
    @FXML
    private void onCalendar() {
        if (main != null) main.loadPage("calendar.fxml");
    }
}