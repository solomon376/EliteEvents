package org.example.eliteevents;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainController {
    @FXML
    public TextField globalSearch;
    @FXML
    public Button btnNewBooking;
    @FXML
    public Button btnNotifications;
    @FXML
    public Button btnProfile;
    public VBox sidebar;
    @FXML
    private AnchorPane contentRoot;

    // Remove the direct sidebar controller injection
    // @FXML
    // private SidebarController sidebarController;

    @FXML
    private void initialize() {
        System.out.println("MainController initialized");

        // We'll use a different approach to get the sidebar controller
        // For now, just load the default page
        loadPage("dashboard.fxml");
    }

    // Add this method to manually find and link the sidebar controller
    public void linkSidebarController(SidebarController sidebarController) {
        if (sidebarController != null) {
            sidebarController.setMainController(this);
            System.out.println("Sidebar controller linked successfully");
        } else {
            System.err.println("Failed to link sidebar controller");
        }
    }

    public void loadPage(String fxmlName) {
        try {
            System.out.println("Loading: " + fxmlName);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlName));

            if (loader.getLocation() == null) {
                System.err.println("Resource not found: " + fxmlName);
                showPlaceholder("Page not found: " + fxmlName);
                return;
            }

            Node page = loader.load();
            contentRoot.getChildren().setAll(page);

            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);

            System.out.println("Successfully loaded: " + fxmlName);

        } catch (Exception e) {
            System.err.println("Error loading " + fxmlName + ": " + e.getMessage());
            e.printStackTrace();
            showPlaceholder("Error loading: " + fxmlName + "\n" + e.getClass().getSimpleName());
        }
    }

    public void showPlaceholder(String message) {
        Label placeholder = new Label(message);
        placeholder.setStyle("-fx-font-size: 16px; -fx-padding: 20; -fx-alignment: center;");

        contentRoot.getChildren().setAll(placeholder);
        AnchorPane.setTopAnchor(placeholder, 0.0);
        AnchorPane.setBottomAnchor(placeholder, 0.0);
        AnchorPane.setLeftAnchor(placeholder, 0.0);
        AnchorPane.setRightAnchor(placeholder, 0.0);
    }
}