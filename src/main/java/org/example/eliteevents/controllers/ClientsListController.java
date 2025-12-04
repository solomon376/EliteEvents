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
import org.example.eliteevents.models.Client;
import org.example.eliteevents.services.DatabaseService;
import java.util.List;
import java.util.Optional;

public class ClientsListController {
    @FXML private TableView<Client> tblClients;
    @FXML private TableColumn<Client, String> colClientName;
    @FXML private TableColumn<Client, String> colClientEmail;
    @FXML private TableColumn<Client, String> colClientPhone;
    @FXML private TableColumn<Client, String> colClientCompany;
    @FXML private TableColumn<Client, String> colClientActions;
    @FXML private TextField clientSearch;
    @FXML private Button btnAddClient;

    private DatabaseService dbService = DatabaseService.getInstance();

    @FXML
    private void initialize() {
        System.out.println("ClientsListController initialized");
        setupTableColumns();
        loadClients();
    }

    private void setupTableColumns() {
        colClientName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colClientEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colClientPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colClientCompany.setCellValueFactory(new PropertyValueFactory<>("company"));

        // Setup actions column with buttons
        colClientActions.setCellFactory(new Callback<TableColumn<Client, String>, TableCell<Client, String>>() {
            @Override
            public TableCell<Client, String> call(TableColumn<Client, String> param) {
                return new TableCell<Client, String>() {
                    private final Button editButton = new Button("Edit");
                    private final Button deleteButton = new Button("Delete");
                    private final HBox buttons = new HBox(5, editButton, deleteButton);

                    {
                        editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 10px;");
                        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 10px;");

                        editButton.setOnAction(event -> {
                            Client client = getTableView().getItems().get(getIndex());
                            editClient(client);
                        });

                        deleteButton.setOnAction(event -> {
                            Client client = getTableView().getItems().get(getIndex());
                            deleteClient(client);
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

    private void loadClients() {
        try {
            List<Client> clients = dbService.getAllClients();
            System.out.println("Loading " + clients.size() + " clients into table");
            tblClients.getItems().setAll(clients);
        } catch (Exception e) {
            System.err.println("Error loading clients: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void editClient(Client client) {
        System.out.println("Edit client: " + client.getId());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/eliteevents/client-form.fxml"));
            Parent editForm = loader.load();

            ClientFormController controller = loader.getController();
            controller.setClientToEdit(client);

            Stage editStage = new Stage();
            editStage.setTitle("Edit Client: " + client.getName());
            editStage.setScene(new Scene(editForm, 500, 400));
            editStage.initModality(Modality.APPLICATION_MODAL);
            editStage.showAndWait();

            loadClients(); // Refresh after editing

        } catch (Exception e) {
            System.err.println("Error loading client edit form: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not open edit form: " + e.getMessage());
        }
    }

    private void deleteClient(Client client) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Client");
        alert.setContentText("Are you sure you want to delete client: " + client.getName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = dbService.deleteClient(client.getId());
                if (deleted) {
                    showAlert("Success", "Client deleted successfully!");
                    loadClients();
                } else {
                    showAlert("Error", "Failed to delete client.");
                }
            } catch (Exception e) {
                showAlert("Error", "Error deleting client: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onAddClient() {
        // Navigation handled by sidebar
        System.out.println("Add Client clicked");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}