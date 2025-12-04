package org.example.eliteevents.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.eliteevents.models.Venue;
import org.example.eliteevents.services.DatabaseService;
import java.util.Arrays;

public class VenueFormController {
    @FXML private TextField txtName;
    @FXML private TextArea txtAddress;
    @FXML private Spinner<Integer> spnCapacity;
    @FXML private TextField txtPrice;
    @FXML private TextField txtAmenities;
    @FXML private Button btnSave;

    private DatabaseService dbService = DatabaseService.getInstance();
    private Venue venueToEdit;
    private boolean isEditMode = false;

    @FXML
    private void initialize() {
        System.out.println("VenueFormController initialized");
        // Setup capacity spinner (10-5000 people)
        spnCapacity.setValueFactory(new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(10, 5000, 100));
    }

    public void setVenueToEdit(Venue venue) {
        this.venueToEdit = venue;
        this.isEditMode = true;
        populateFormWithVenueData();

        if (btnSave != null) {
            btnSave.setText("Update Venue");
        }
    }

    private void populateFormWithVenueData() {
        if (venueToEdit == null) return;

        txtName.setText(venueToEdit.getName());
        txtAddress.setText(venueToEdit.getAddress());
        spnCapacity.getValueFactory().setValue(venueToEdit.getCapacity());
        txtPrice.setText(String.valueOf(venueToEdit.getPricePerHour()));
        txtAmenities.setText(String.join(", ", venueToEdit.getAmenities()));
    }

    @FXML
    private void onSave() {
        if (validateForm()) {
            Venue venue = new Venue();
            venue.setName(txtName.getText().trim());
            venue.setAddress(txtAddress.getText().trim());
            venue.setCapacity(spnCapacity.getValue());
            venue.setPricePerHour(Double.parseDouble(txtPrice.getText().trim()));

            String amenities = txtAmenities.getText().trim();
            if (!amenities.isEmpty()) {
                venue.setAmenities(Arrays.asList(amenities.split(",")));
            }

            try {
                if (isEditMode && venueToEdit != null) {
                    // UPDATE existing venue
                    venue.setId(venueToEdit.getId());
                    dbService.updateVenue(venue);
                    showSuccessAlert("Venue Updated", "Venue '" + venue.getName() + "' has been updated successfully!");
                    closeFormIfModal();
                } else {
                    // CREATE new venue
                    dbService.addVenue(venue);
                    showSuccessAlert("Venue Created", "Venue '" + venue.getName() + "' has been added successfully!");
                    clearForm();
                }
            } catch (Exception e) {
                showAlert("Error", "Failed to save venue: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onCancel() {
        closeFormIfModal();
    }

    private boolean validateForm() {
        if (txtName.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Venue name is required");
            return false;
        }
        if (txtPrice.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Price per hour is required");
            return false;
        }
        try {
            Double.parseDouble(txtPrice.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid price");
            return false;
        }
        return true;
    }

    private void clearForm() {
        txtName.clear();
        txtAddress.clear();
        spnCapacity.getValueFactory().setValue(100);
        txtPrice.clear();
        txtAmenities.clear();
    }

    private void closeFormIfModal() {
        if (btnSave != null) {
            javafx.stage.Window window = btnSave.getScene().getWindow();
            if (window instanceof Stage) {
                ((Stage) window).close();
            }
        }
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}