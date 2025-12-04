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
import org.example.eliteevents.models.Vendor;
import org.example.eliteevents.services.DatabaseService;
import java.util.List;
import java.util.Optional;

public class VendorsListController {
    @FXML private TableView<Vendor> tblVendors;
    @FXML private TableColumn<Vendor, String> colVendorName;
    @FXML private TableColumn<Vendor, String> colVendorCategory;
    @FXML private TableColumn<Vendor, String> colVendorContact;
    @FXML private TableColumn<Vendor, String> colVendorActions;
    @FXML private TextField vendorSearch;
    @FXML private Button btnAddVendor;

    private DatabaseService dbService = DatabaseService.getInstance();

    @FXML
    private void initialize() {
        System.out.println("VendorsListController initialized");
        setupTableColumns();
        setupEventHandlers();
        loadVendors();
    }

    private void setupTableColumns() {
        colVendorName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colVendorCategory.setCellValueFactory(new PropertyValueFactory<>("category"));

        // Custom cell value factory for contact info (email + phone)
        colVendorContact.setCellValueFactory(new PropertyValueFactory<>("email"));
        colVendorContact.setCellFactory(new Callback<TableColumn<Vendor, String>, TableCell<Vendor, String>>() {
            @Override
            public TableCell<Vendor, String> call(TableColumn<Vendor, String> param) {
                return new TableCell<Vendor, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                        } else {
                            Vendor vendor = getTableRow().getItem();
                            String contactInfo = vendor.getEmail() + "\n" + vendor.getPhone();
                            setText(contactInfo);
                        }
                    }
                };
            }
        });

        colVendorActions.setCellFactory(new Callback<TableColumn<Vendor, String>, TableCell<Vendor, String>>() {
            @Override
            public TableCell<Vendor, String> call(TableColumn<Vendor, String> param) {
                return new TableCell<Vendor, String>() {
                    private final Button editButton = new Button("Edit");
                    private final Button deleteButton = new Button("Delete");
                    private final HBox buttons = new HBox(5, editButton, deleteButton);

                    {
                        editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 10px;");
                        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 10px;");

                        editButton.setOnAction(event -> {
                            Vendor vendor = getTableView().getItems().get(getIndex());
                            editVendor(vendor);
                        });

                        deleteButton.setOnAction(event -> {
                            Vendor vendor = getTableView().getItems().get(getIndex());
                            deleteVendor(vendor);
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

    private void setupEventHandlers() {
        btnAddVendor.setOnAction(event -> addNewVendor());

        // Search functionality
        vendorSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterVendors(newValue);
        });
    }

    private void loadVendors() {
        try {
            List<Vendor> vendors = dbService.getAllVendors();
            System.out.println("Loading " + vendors.size() + " vendors into table");
            tblVendors.getItems().setAll(vendors);
        } catch (Exception e) {
            System.err.println("Error loading vendors: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load vendors: " + e.getMessage());
        }
    }

    private void filterVendors(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            loadVendors();
            return;
        }

        try {
            List<Vendor> allVendors = dbService.getAllVendors();
            List<Vendor> filteredVendors = allVendors.stream()
                    .filter(vendor -> vendor.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                            vendor.getCategory().toLowerCase().contains(searchText.toLowerCase()) ||
                            vendor.getEmail().toLowerCase().contains(searchText.toLowerCase()) ||
                            vendor.getPhone().contains(searchText))
                    .toList();

            tblVendors.getItems().setAll(filteredVendors);
        } catch (Exception e) {
            System.err.println("Error filtering vendors: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addNewVendor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/eliteevents/vendor-form.fxml"));
            Parent vendorForm = loader.load();

            Stage vendorStage = new Stage();
            vendorStage.setTitle("Add New Vendor");
            vendorStage.setScene(new Scene(vendorForm, 400, 400));
            vendorStage.initModality(Modality.APPLICATION_MODAL);
            vendorStage.showAndWait();

            // Refresh the table after the form is closed
            loadVendors();

        } catch (Exception e) {
            System.err.println("Error loading vendor form: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not open vendor form: " + e.getMessage());
        }
    }

    private void editVendor(Vendor vendor) {
        System.out.println("Edit vendor: " + vendor.getId());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/eliteevents/vendor-form.fxml"));
            Parent editForm = loader.load();

            VendorFormController controller = loader.getController();
            controller.setVendorToEdit(vendor);

            Stage editStage = new Stage();
            editStage.setTitle("Edit Vendor: " + vendor.getName());
            editStage.setScene(new Scene(editForm, 400, 400));
            editStage.initModality(Modality.APPLICATION_MODAL);
            editStage.showAndWait();

            loadVendors();

        } catch (Exception e) {
            System.err.println("Error loading vendor edit form: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not open edit form: " + e.getMessage());
        }
    }

    private void deleteVendor(Vendor vendor) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Vendor");
        alert.setContentText("Are you sure you want to delete vendor: " + vendor.getName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = dbService.deleteVendor(vendor.getId());
                if (deleted) {
                    showAlert("Success", "Vendor deleted successfully!");
                    loadVendors();
                } else {
                    showAlert("Error", "Failed to delete vendor.");
                }
            } catch (Exception e) {
                showAlert("Error", "Error deleting vendor: " + e.getMessage());
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