package org.example.eliteevents.controllers;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.eliteevents.services.ConflictDetectionService;

import java.util.List;

public class ConflictAlertDialog {

    public static boolean showConflictAlert(ConflictDetectionService.ConflictCheckResult conflictResult) {
        if (!conflictResult.hasConflicts()) {
            return true; // No conflicts, proceed
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Booking Conflict Detected");
        alert.setHeaderText("The selected venue is not available for the chosen time");

        // Create detailed content
        VBox content = new VBox(10);
        content.getChildren().add(new Label("Conflicting bookings found:"));

        for (String reason : conflictResult.getConflictReasons()) {
            Label conflictLabel = new Label("• " + reason);
            conflictLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px;");
            content.getChildren().add(conflictLabel);
        }

        content.getChildren().add(new Label("\nDo you want to proceed anyway?"));

        alert.getDialogPane().setContent(content);

        // Add custom buttons
        alert.getButtonTypes().setAll(
                ButtonType.YES,
                ButtonType.NO
        );

        var result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    // Fixed method - now takes only one parameter
    public static void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}