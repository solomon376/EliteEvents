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
import org.example.eliteevents.models.Venue;
import org.example.eliteevents.services.DatabaseService;
import java.util.List;
import java.util.Optional;

public class VenuesListController {
    @FXML private TableView<Venue> tblVenues;
    @FXML private TableColumn<Venue, String> colVenueName;
    @FXML private TableColumn<Venue, String> colVenueAddress;
    @FXML private TableColumn<Venue, Integer> colVenueCapacity;
    @FXML private TableColumn<Venue, Double> colVenuePrice;
    @FXML private TableColumn<Venue, String> colVenueActions;

    private DatabaseService dbService = DatabaseService.getInstance();

    @FXML
    private void initialize() {
        System.out.println("VenuesListController initialized");
        setupTableColumns();
        loadVenues();
    }

    private void setupTableColumns() {
        colVenueName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colVenueAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colVenueCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        colVenuePrice.setCellValueFactory(new PropertyValueFactory<>("pricePerHour"));

        colVenueActions.setCellFactory(new Callback<TableColumn<Venue, String>, TableCell<Venue, String>>() {
            @Override
            public TableCell<Venue, String> call(TableColumn<Venue, String> param) {
                return new TableCell<Venue, String>() {
                    private final Button editButton = new Button("Edit");
                    private final Button deleteButton = new Button("Delete");
                    private final HBox buttons = new HBox(5, editButton, deleteButton);

                    {
                        editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 10px;");
                        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 10px;");

                        editButton.setOnAction(event -> {
                            Venue venue = getTableView().getItems().get(getIndex());
                            editVenue(venue);
                        });

                        deleteButton.setOnAction(event -> {
                            Venue venue = getTableView().getItems().get(getIndex());
                            deleteVenue(venue);
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

    private void loadVenues() {
        try {
            List<Venue> venues = dbService.getAllVenues();
            System.out.println("Loading " + venues.size() + " venues into table");
            tblVenues.getItems().setAll(venues);
        } catch (Exception e) {
            System.err.println("Error loading venues: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void editVenue(Venue venue) {
        System.out.println("Edit venue: " + venue.getId());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/eliteevents/venue-form.fxml"));
            Parent editForm = loader.load();

            VenueFormController controller = loader.getController();
            controller.setVenueToEdit(venue);

            Stage editStage = new Stage();
            editStage.setTitle("Edit Venue: " + venue.getName());
            editStage.setScene(new Scene(editForm, 500, 500));
            editStage.initModality(Modality.APPLICATION_MODAL);
            editStage.showAndWait();

            loadVenues();

        } catch (Exception e) {
            System.err.println("Error loading venue edit form: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not open edit form: " + e.getMessage());
        }
    }

    private void deleteVenue(Venue venue) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Venue");
        alert.setContentText("Are you sure you want to delete venue: " + venue.getName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = dbService.deleteVenue(venue.getId());
                if (deleted) {
                    showAlert("Success", "Venue deleted successfully!");
                    loadVenues();
                } else {
                    showAlert("Error", "Failed to delete venue.");
                }
            } catch (Exception e) {
                showAlert("Error", "Error deleting venue: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}