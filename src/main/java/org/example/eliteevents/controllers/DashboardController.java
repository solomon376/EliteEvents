package org.example.eliteevents.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import org.example.eliteevents.services.DatabaseService;
import org.example.eliteevents.models.Booking;
import org.example.eliteevents.models.Venue;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label metricTotalBookings;
    @FXML private Label metricRevenue;
    @FXML private Label metricUpcoming;
    @FXML private Label metricLeads;
    @FXML private Label lblCurrentDate;
    @FXML private ListView<String> lvUpcomingEvents;
    @FXML private ListView<String> lvVenueAvailability;

    // Properties for data binding
    private IntegerProperty totalBookings = new SimpleIntegerProperty(0);
    private DoubleProperty totalRevenue = new SimpleDoubleProperty(0.0);
    private IntegerProperty upcomingEvents = new SimpleIntegerProperty(0);
    private IntegerProperty newLeads = new SimpleIntegerProperty(0);

    private ObservableList<String> upcomingEventsList = FXCollections.observableArrayList();
    private ObservableList<String> venueAvailabilityList = FXCollections.observableArrayList();

    private DatabaseService databaseService;
    private Timeline autoRefreshTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        databaseService = DatabaseService.getInstance();
        setupDataBindings();
        updateCurrentDate();
        refreshDashboard();

        // Auto-refresh every 30 seconds
        setupAutoRefresh();
    }

    private void setupDataBindings() {
        // Bind labels to properties
        metricTotalBookings.textProperty().bind(totalBookings.asString());
        metricRevenue.textProperty().bind(totalRevenue.asString("$%,.0f"));
        metricUpcoming.textProperty().bind(upcomingEvents.asString());
        metricLeads.textProperty().bind(newLeads.asString());

        // Bind list views
        lvUpcomingEvents.setItems(upcomingEventsList);
        lvVenueAvailability.setItems(venueAvailabilityList);
    }

    private void setupAutoRefresh() {
        autoRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> refreshDashboard())
        );
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private void updateCurrentDate() {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        lblCurrentDate.setText("Today: " + currentDate);
    }

    @FXML
    public void refresh() {
        refreshDashboard();
    }

    private void refreshDashboard() {
        updateMetrics();
        updateUpcomingEvents();
        updateVenueAvailability();
    }

    private void updateMetrics() {
        try {
            List<Booking> allBookings = databaseService.getAllBookings();
            LocalDate today = LocalDate.now();
            LocalDate nextWeek = today.plusDays(7);

            int total = allBookings.size();
            double revenue = 0;
            int upcoming = 0;
            int leads = 0;

            for (Booking booking : allBookings) {
                // Calculate revenue (using budget for confirmed bookings)
                if ("CONFIRMED".equals(booking.getStatus())) {
                    revenue += booking.getBudget();
                }

                // Count upcoming confirmed events (next 7 days)
                if ("CONFIRMED".equals(booking.getStatus()) &&
                        !booking.getStartDateTime().toLocalDate().isBefore(today) &&
                        booking.getStartDateTime().toLocalDate().isBefore(nextWeek)) {
                    upcoming++;
                }

                // Count pending bookings as leads
                if ("PENDING".equals(booking.getStatus())) {
                    leads++;
                }
            }

            totalBookings.set(total);
            totalRevenue.set(revenue);
            upcomingEvents.set(upcoming);
            newLeads.set(leads);

        } catch (Exception e) {
            System.err.println("Error updating dashboard metrics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateUpcomingEvents() {
        upcomingEventsList.clear();

        try {
            List<Booking> allBookings = databaseService.getAllBookings();
            LocalDateTime now = LocalDateTime.now();

            allBookings.stream()
                    .filter(booking -> "CONFIRMED".equals(booking.getStatus()))
                    .filter(booking -> booking.getStartDateTime().isAfter(now))
                    .sorted((b1, b2) -> b1.getStartDateTime().compareTo(b2.getStartDateTime()))
                    .limit(10) // Show only next 10 events
                    .forEach(booking -> {
                        String eventText = String.format("%s - %s - %s",
                                booking.getEventType(),
                                booking.getVenue().getName(),
                                booking.getStartDateTime().format(DateTimeFormatter.ofPattern("MMM d, h:mm a"))
                        );
                        upcomingEventsList.add(eventText);
                    });

            if (upcomingEventsList.isEmpty()) {
                upcomingEventsList.add("No upcoming events");
            }

        } catch (Exception e) {
            System.err.println("Error fetching upcoming events: " + e.getMessage());
            e.printStackTrace();
            upcomingEventsList.add("Error loading events");
        }
    }

    private void updateVenueAvailability() {
        venueAvailabilityList.clear();

        try {
            List<Venue> allVenues = databaseService.getAllVenues();
            List<Booking> allBookings = databaseService.getAllBookings();
            LocalDate today = LocalDate.now();

            for (Venue venue : allVenues) {
                boolean isBookedToday = allBookings.stream()
                        .filter(booking -> "CONFIRMED".equals(booking.getStatus()))
                        .filter(booking -> booking.getVenue().getId() == venue.getId())
                        .anyMatch(booking -> booking.getStartDateTime().toLocalDate().equals(today));

                String status = isBookedToday ? "Booked today" : "Available today";
                venueAvailabilityList.add(venue.getName() + ": " + status);
            }

            if (venueAvailabilityList.isEmpty()) {
                venueAvailabilityList.add("No venues configured");
            }

        } catch (Exception e) {
            System.err.println("Error fetching venue availability: " + e.getMessage());
            e.printStackTrace();
            venueAvailabilityList.add("Error loading availability");
        }
    }
}