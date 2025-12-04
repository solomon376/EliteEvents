package org.example.eliteevents.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.eliteevents.models.Booking;
import org.example.eliteevents.services.DatabaseService;

import java.util.List;
import java.util.Optional;

public class BookingsListController {
    @FXML private TableView<Booking> tblBookings;
    @FXML private TableColumn<Booking, Integer> colId;
    @FXML private TableColumn<Booking, String> colClient;
    @FXML private TableColumn<Booking, String> colEvent;
    @FXML private TableColumn<Booking, String> colVenue;
    @FXML private TableColumn<Booking, String> colDateTime;
    @FXML private TableColumn<Booking, String> colStatus;
    @FXML private TableColumn<Booking, String> colActions;

    @FXML private DatePicker filterFrom;
    @FXML private DatePicker filterTo;
    @FXML private ComboBox<String> filterVenue;
    @FXML private ComboBox<String> filterEventType;
    @FXML private TextField filterClient;
    @FXML private Button btnSearch;
    @FXML private Button btnExport;

    private DatabaseService dbService = DatabaseService.getInstance();

    @FXML
    private void initialize() {
        System.out.println("BookingsListController initialized");
        setupTableColumns();
        loadBookings();
        setupFilters();
    }

    private void setupTableColumns() {
        // Configure table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        colClient.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getClient() != null ?
                                cellData.getValue().getClient().getName() : ""));

        colEvent.setCellValueFactory(new PropertyValueFactory<>("eventType"));

        colVenue.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getVenue() != null ?
                                cellData.getValue().getVenue().getName() : ""));

        colDateTime.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getStartDateTime() != null ?
                                cellData.getValue().getStartDateTime().toString() + " to " +
                                        cellData.getValue().getEndDateTime().toString() : ""));

        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Setup actions column with buttons
        colActions.setCellFactory(new Callback<TableColumn<Booking, String>, TableCell<Booking, String>>() {
            @Override
            public TableCell<Booking, String> call(TableColumn<Booking, String> param) {
                return new TableCell<Booking, String>() {
                    private final Button editButton = new Button("Edit");
                    private final Button deleteButton = new Button("Delete");
                    private final HBox buttons = new HBox(5, editButton, deleteButton);

                    {
                        editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 10px;");
                        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 10px;");

                        editButton.setOnAction(event -> {
                            Booking booking = getTableView().getItems().get(getIndex());
                            editBooking(booking);
                        });

                        deleteButton.setOnAction(event -> {
                            Booking booking = getTableView().getItems().get(getIndex());
                            deleteBooking(booking);
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(buttons);
                        }
                    }
                };
            }
        });
    }

    private void setupFilters() {
        // Populate venue filter
        filterVenue.getItems().add("All Venues");
        dbService.getAllVenues().forEach(venue ->
                filterVenue.getItems().add(venue.getName()));

        // Populate event type filter
        filterEventType.getItems().addAll("All Events", "Conference", "Wedding", "Meeting", "Party", "Corporate Event");

        // Set default values
        filterVenue.setValue("All Venues");
        filterEventType.setValue("All Events");
    }

    private void loadBookings() {
        try {
            List<Booking> bookings = dbService.getAllBookings();
            System.out.println("Loading " + bookings.size() + " bookings into table");
            tblBookings.getItems().setAll(bookings);

        } catch (Exception e) {
            System.err.println("Error loading bookings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void editBooking(Booking booking) {
        System.out.println("Edit booking: " + booking.getId());

        try {
            // Load the booking form in edit mode
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/eliteevents/booking-form.fxml"));
            Parent editForm = loader.load();

            // Get the controller and pass the booking to edit
            BookingFormController controller = loader.getController();
            controller.setBookingToEdit(booking);

            // Create a new stage for the edit form
            Stage editStage = new Stage();
            editStage.setTitle("Edit Booking #" + booking.getId());
            editStage.setScene(new Scene(editForm, 600, 700));
            editStage.initModality(Modality.APPLICATION_MODAL);
            editStage.showAndWait();

            // Refresh the bookings list after editing
            loadBookings();

        } catch (Exception e) {
            System.err.println("Error loading edit form: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not open edit form: " + e.getMessage());
        }
    }

    private void deleteBooking(Booking booking) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Booking");
        alert.setContentText("Are you sure you want to delete booking #" + booking.getId() + " for " +
                booking.getClient().getName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = dbService.deleteBooking(booking.getId());
                if (deleted) {
                    showAlert("Success", "Booking deleted successfully!");
                    loadBookings(); // Refresh the table
                } else {
                    showAlert("Error", "Failed to delete booking.");
                }
            } catch (Exception e) {
                showAlert("Error", "Error deleting booking: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onSearch() {
        System.out.println("Search button clicked - filtering bookings");
        // For now, just reload all bookings
        loadBookings();
    }

    @FXML
    private void onExport() {
        System.out.println("Export button clicked");
        showAlert("Export Feature", "Export functionality will be implemented soon!");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}