package org.example.eliteevents.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.eliteevents.models.Vendor;
import org.example.eliteevents.services.DatabaseService;

public class VendorFormController {
    @FXML private TextField txtName;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private Button btnSave;

    private DatabaseService dbService = DatabaseService.getInstance();
    private Vendor vendorToEdit;
    private boolean isEditMode = false;

    @FXML
    private void initialize() {
        System.out.println("VendorFormController initialized");
        // Populate categories
        cmbCategory.getItems().addAll(
                "Catering",
                "Photography",
                "Decor",
                "Entertainment",
                "Florist",
                "Audio/Visual",
                "Transportation",
                "Other"
        );
        cmbCategory.setValue("Catering"); // Default value
    }

    public void setVendorToEdit(Vendor vendor) {
        this.vendorToEdit = vendor;
        this.isEditMode = true;
        populateFormWithVendorData();

        if (btnSave != null) {
            btnSave.setText("Update Vendor");
        }
    }

    private void populateFormWithVendorData() {
        if (vendorToEdit == null) return;

        txtName.setText(vendorToEdit.getName());
        cmbCategory.setValue(vendorToEdit.getCategory());
        txtEmail.setText(vendorToEdit.getEmail());
        txtPhone.setText(vendorToEdit.getPhone());
    }

    @FXML
    private void onSave() {
        if (validateForm()) {
            Vendor vendor = new Vendor();
            vendor.setName(txtName.getText().trim());
            vendor.setCategory(cmbCategory.getValue());
            vendor.setEmail(txtEmail.getText().trim());
            vendor.setPhone(txtPhone.getText().trim());

            try {
                if (isEditMode && vendorToEdit != null) {
                    // UPDATE existing vendor
                    vendor.setId(vendorToEdit.getId());
                    dbService.updateVendor(vendor);
                    showSuccessAlert("Vendor Updated", "Vendor '" + vendor.getName() + "' has been updated successfully!");
                    closeFormIfModal();
                } else {
                    // CREATE new vendor
                    dbService.addVendor(vendor);
                    showSuccessAlert("Vendor Created", "Vendor '" + vendor.getName() + "' has been added successfully!");
                    clearForm();
                }
            } catch (Exception e) {
                showAlert("Error", "Failed to save vendor: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onCancel() {
        closeFormIfModal();
    }

    private boolean validateForm() {
        if (txtName.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Vendor name is required");
            return false;
        }
        if (cmbCategory.getValue() == null || cmbCategory.getValue().isEmpty()) {
            showAlert("Validation Error", "Please select a category");
            return false;
        }
        return true;
    }

    private void clearForm() {
        txtName.clear();
        cmbCategory.setValue("Catering");
        txtEmail.clear();
        txtPhone.clear();
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