package org.example.eliteevents;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Load main layout
        FXMLLoader mainLoader = new FXMLLoader(HelloApplication.class.getResource("main-layout.fxml"));
        Scene scene = new Scene(mainLoader.load());

        // Get the main controller
        MainController mainController = mainLoader.getController();

        // Debug: Print all namespace entries to see what's available
        System.out.println("=== Namespace Contents ===");
        Map<String, Object> namespace = mainLoader.getNamespace();
        for (String key : namespace.keySet()) {
            Object value = namespace.get(key);
            if (value != null) {
                System.out.println("Key: " + key + " -> " + value.getClass().getSimpleName());
            } else {
                System.out.println("Key: " + key + " -> NULL");
            }
        }
        System.out.println("==========================");

        // Get the sidebar controller directly (we can see it's available as "sidebarController")
        SidebarController sidebarController = (SidebarController) namespace.get("sidebarController");

        // Link the controllers
        if (sidebarController != null) {
            mainController.linkSidebarController(sidebarController);
            System.out.println("✅ Sidebar controller linked successfully!");
        } else {
            System.err.println("❌ Sidebar controller is null in namespace");
        }

        stage.setTitle("Elite Events!");
        stage.setScene(scene);
        stage.show();

        System.out.println("Application started successfully");
    }

    public static void main(String[] args) {
        launch();
    }
}