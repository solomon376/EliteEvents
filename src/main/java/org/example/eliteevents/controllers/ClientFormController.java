package org.example.eliteevents.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.eliteevents.models.Client;
import org.example.eliteevents.services.DatabaseService;

public class ClientFormController {
    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextField txtCompany;
    @FXML private Button btnSave;

    private DatabaseService dbService = DatabaseService.getInstance();
    private Client clientToEdit;
    private boolean isEditMode = false;

    @FXML
    private void initialize() {
        System.out.println("ClientFormController initialized");
    }

    public void setClientToEdit(Client client) {
        this.clientToEdit = client;
        this.isEditMode = true;
        populateFormWithClientData();

        if (btnSave != null) {
            btnSave.setText("Update Client");
        }
    }

    private void populateFormWithClientData() {
        if (clientToEdit == null) return;

        txtName.setText(clientToEdit.getName());
        txtEmail.setText(clientToEdit.getEmail());
        txtPhone.setText(clientToEdit.getPhone());
        txtCompany.setText(clientToEdit.getCompany());
    }

    @FXML
    private void onSave() {
        if (validateForm()) {
            Client client = new Client();
            client.setName(txtName.getText().trim());
            client.setEmail(txtEmail.getText().trim());
            client.setPhone(txtPhone.getText().trim());
            client.setCompany(txtCompany.getText().trim());

            try {
                if (isEditMode && clientToEdit != null) {
                    // UPDATE existing client
                    client.setId(clientToEdit.getId());
                    dbService.updateClient(client);
                    showSuccessAlert("Client Updated", "Client '" + client.getName() + "' has been updated successfully!");
                    closeFormIfModal();
                } else {
                    // CREATE new client
                    dbService.addClient(client);
                    showSuccessAlert("Client Created", "Client '" + client.getName() + "' has been added successfully!");
                    clearForm();
                }
            } catch (Exception e) {
                showAlert("Error", "Failed to save client: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onCancel() {
        closeFormIfModal();
    }

    private boolean validateForm() {
        if (txtName.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Client name is required");
            return false;
        }
        return true;
    }

    private void clearForm() {
        txtName.clear();
        txtEmail.clear();
        txtPhone.clear();
        txtCompany.clear();
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