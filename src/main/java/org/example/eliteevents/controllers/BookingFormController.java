package org.example.eliteevents.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.eliteevents.models.*;
import org.example.eliteevents.services.DatabaseService;
import org.example.eliteevents.services.ConflictDetectionService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class BookingFormController {
    // Form controls
    @FXML private ComboBox<Client> clientCombo;
    @FXML private ComboBox<Venue> venueCombo;
    @FXML private TextField eventType;
    @FXML private DatePicker startDate;
    @FXML private ComboBox<LocalTime> startTime;
    @FXML private DatePicker endDate;
    @FXML private ComboBox<LocalTime> endTime;
    @FXML private Spinner<Integer> guestCount;
    @FXML private CheckBox catering;
    @FXML private ComboBox<Vendor> vendorCombo;
    @FXML private TextField budget;
    @FXML private TextArea notes;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Label formTitle;

    // Business logic components
    private Booking bookingToEdit;
    private boolean isEditMode = false;
    private DatabaseService dbService = DatabaseService.getInstance();
    private ConflictDetectionService conflictService = new ConflictDetectionService();

    @FXML
    private void initialize() {
        System.out.println("BookingFormController initialized - Loading data from MySQL");
        initializeTimeComboBoxes();
        loadComboBoxData();
        setupSpinner();

        if (formTitle != null) {
            formTitle.setText("Create New Booking");
        }
    }

    /**
     * Sets the booking to edit and populates the form with existing data
     */
    public void setBookingToEdit(Booking booking) {
        this.bookingToEdit = booking;
        this.isEditMode = true;
        populateFormWithBookingData();

        if (formTitle != null) {
            formTitle.setText("Edit Booking #" + booking.getId());
        }
        if (btnSave != null) {
            btnSave.setText("Update Booking");
        }
    }

    /**
     * Populates form fields with data from the booking being edited
     */
    private void populateFormWithBookingData() {
        if (bookingToEdit == null) return;

        try {
            // Client
            for (Client client : clientCombo.getItems()) {
                if (client.getId() == bookingToEdit.getClient().getId()) {
                    clientCombo.setValue(client);
                    break;
                }
            }

            // Venue
            for (Venue venue : venueCombo.getItems()) {
                if (venue.getId() == bookingToEdit.getVenue().getId()) {
                    venueCombo.setValue(venue);
                    break;
                }
            }

            // Vendor
            if (bookingToEdit.getVendor() != null) {
                for (Vendor vendor : vendorCombo.getItems()) {
                    if (vendor.getId() == bookingToEdit.getVendor().getId()) {
                        vendorCombo.setValue(vendor);
                        break;
                    }
                }
            }

            // Other fields
            eventType.setText(bookingToEdit.getEventType());
            startDate.setValue(bookingToEdit.getStartDateTime().toLocalDate());
            startTime.setValue(bookingToEdit.getStartDateTime().toLocalTime());
            endDate.setValue(bookingToEdit.getEndDateTime().toLocalDate());
            endTime.setValue(bookingToEdit.getEndDateTime().toLocalTime());
            guestCount.getValueFactory().setValue(bookingToEdit.getGuestCount());
            catering.setSelected(bookingToEdit.isCateringRequired());
            budget.setText(String.valueOf(bookingToEdit.getBudget()));
            notes.setText(bookingToEdit.getNotes());

        } catch (Exception e) {
            System.err.println("Error populating form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initializes time combo boxes with 30-minute intervals
     */
    private void initializeTimeComboBoxes() {
        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute < 60; minute += 30) {
                LocalTime time = LocalTime.of(hour, minute);
                startTime.getItems().add(time);
                endTime.getItems().add(time);
            }
        }
        startTime.setValue(LocalTime.of(9, 0));
        endTime.setValue(LocalTime.of(17, 0));
    }

    /**
     * Loads data for combo boxes from database
     */
    private void loadComboBoxData() {
        try {
            clientCombo.getItems().setAll(dbService.getAllClients());
            venueCombo.getItems().setAll(dbService.getAllVenues());
            vendorCombo.getItems().setAll(dbService.getAllVendors());

            // Custom cell factories for better display
            clientCombo.setCellFactory(param -> new ListCell<Client>() {
                @Override
                protected void updateItem(Client client, boolean empty) {
                    super.updateItem(client, empty);
                    setText(empty || client == null ? "" : client.getName() + " - " + client.getCompany());
                }
            });

            venueCombo.setCellFactory(param -> new ListCell<Venue>() {
                @Override
                protected void updateItem(Venue venue, boolean empty) {
                    super.updateItem(venue, empty);
                    if (empty || venue == null) {
                        setText("");
                    } else {
                        setText(venue.getName() + " (Capacity: " + venue.getCapacity() + ", $" + venue.getPricePerHour() + "/hr)");
                    }
                }
            });

            vendorCombo.setCellFactory(param -> new ListCell<Vendor>() {
                @Override
                protected void updateItem(Vendor vendor, boolean empty) {
                    super.updateItem(vendor, empty);
                    setText(empty || vendor == null ? "" : vendor.getName() + " (" + vendor.getCategory() + ")");
                }
            });

            // Set default dates
            startDate.setValue(LocalDate.now().plusDays(7));
            endDate.setValue(LocalDate.now().plusDays(7));

            System.out.println("✅ Form data loaded successfully from MySQL");

        } catch (Exception e) {
            System.err.println("❌ Error loading form data: " + e.getMessage());
            showAlert("Database Error", "Could not load data from database: " + e.getMessage());
        }
    }

    /**
     * Sets up the guest count spinner
     */
    private void setupSpinner() {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 50);
        guestCount.setValueFactory(valueFactory);
    }

    /**
     * Handles save button click - includes conflict detection
     */
    @FXML
    private void onSave() {
        try {
            if (validateForm()) {
                // Create booking object first to check for conflicts
                Booking booking = createBookingFromForm();

                if (isEditMode && bookingToEdit != null) {
                    // For editing, pass the existing booking ID to exclude it from conflict check
                    if (!validateBookingTime(booking, bookingToEdit.getId())) {
                        return; // User chose not to proceed with conflicts
                    }
                    updateBooking();
                } else {
                    // For new booking, no ID to exclude
                    if (!validateBookingTime(booking, null)) {
                        return; // User chose not to proceed with conflicts
                    }
                    createNewBooking();
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error saving booking: " + e.getMessage());
            showAlert("Save Error", "Could not save booking: " + e.getMessage());
        }
    }

    /**
     * Checks for booking conflicts before saving
     * @param booking The booking to validate
     * @param existingBookingId The ID of the booking being edited (null for new bookings)
     * @return true if no conflicts or user chooses to proceed anyway, false if conflicts and user cancels
     */
    private boolean validateBookingTime(Booking booking, Integer existingBookingId) {
        ConflictDetectionService.ConflictCheckResult conflictResult =
                conflictService.checkBookingConflict(
                        booking.getVenue().getId(),
                        booking.getStartDateTime(),
                        booking.getEndDateTime(),
                        existingBookingId
                );

        if (conflictResult.hasConflicts()) {
            // Show conflict alert and let user decide whether to proceed
            return ConflictAlertDialog.showConflictAlert(conflictResult);
        }

        return true; // No conflicts, proceed with save
    }

    /**
     * Updates an existing booking in the database
     */
    private void updateBooking() {
        Booking updatedBooking = createBookingFromForm();
        updatedBooking.setId(bookingToEdit.getId());
        updatedBooking.setStatus(bookingToEdit.getStatus());

        dbService.updateBooking(updatedBooking);
        // Fixed: Only pass one parameter
        ConflictAlertDialog.showSuccessAlert("Booking #" + updatedBooking.getId() + " has been successfully updated!");
        closeFormIfModal();
    }

    /**
     * Creates a new booking in the database
     */
    private void createNewBooking() {
        Booking booking = createBookingFromForm();
        dbService.addBooking(booking);
        // Fixed: Only pass one parameter
        ConflictAlertDialog.showSuccessAlert("Booking has been successfully created in the database!");
        clearForm();
    }

    /**
     * Creates a Booking object from form data
     */
    private Booking createBookingFromForm() {
        LocalDateTime startDateTime = LocalDateTime.of(startDate.getValue(), startTime.getValue());
        LocalDateTime endDateTime = LocalDateTime.of(endDate.getValue(), endTime.getValue());

        double budgetAmount;
        try {
            String budgetText = budget.getText().replace(",", "").trim();
            budgetAmount = Double.parseDouble(budgetText);
        } catch (NumberFormatException e) {
            budgetAmount = 0.0;
        }

        Booking booking = new Booking();
        booking.setClient(clientCombo.getValue());
        booking.setVenue(venueCombo.getValue());
        booking.setEventType(eventType.getText());
        booking.setStartDateTime(startDateTime);
        booking.setEndDateTime(endDateTime);
        booking.setGuestCount(guestCount.getValue());
        booking.setCateringRequired(catering.isSelected());
        booking.setVendor(vendorCombo.getValue());
        booking.setBudget(budgetAmount);
        booking.setNotes(notes.getText());
        booking.setStatus("CONFIRMED");

        return booking;
    }

    /**
     * Closes the form if it's opened in a modal window
     */
    private void closeFormIfModal() {
        if (btnSave != null) {
            javafx.stage.Window window = btnSave.getScene().getWindow();
            if (window instanceof Stage) {
                ((Stage) window).close();
            }
        }
    }

    /**
     * Handles cancel button click
     */
    @FXML
    private void onCancel() {
        closeFormIfModal();
    }

    /**
     * Validates all form fields
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateForm() {
        if (clientCombo.getValue() == null) {
            showAlert("Validation Error", "Please select a client");
            return false;
        }
        if (venueCombo.getValue() == null) {
            showAlert("Validation Error", "Please select a venue");
            return false;
        }
        if (eventType.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter event type");
            return false;
        }
        if (startDate.getValue() == null || startTime.getValue() == null) {
            showAlert("Validation Error", "Please select start date and time");
            return false;
        }
        if (endDate.getValue() == null || endTime.getValue() == null) {
            showAlert("Validation Error", "Please select end date and time");
            return false;
        }
        if (budget.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter budget");
            return false;
        }
        try {
            String budgetText = budget.getText().replace(",", "");
            Double.parseDouble(budgetText);
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid budget amount (numbers only)");
            return false;
        }
        return true;
    }

    /**
     * Clears all form fields
     */
    private void clearForm() {
        clientCombo.setValue(null);
        venueCombo.setValue(null);
        eventType.clear();
        startDate.setValue(null);
        startTime.setValue(null);
        endDate.setValue(null);
        endTime.setValue(null);
        guestCount.getValueFactory().setValue(50);
        catering.setSelected(false);
        vendorCombo.setValue(null);
        budget.clear();
        notes.clear();
    }

    /**
     * Shows a success alert
     */
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an error alert
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}