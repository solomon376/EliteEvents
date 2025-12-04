package org.example.eliteevents.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Booking {
    private int id;
    private Client client;
    private Venue venue;
    private String eventType;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private int guestCount;
    private boolean cateringRequired;
    private Vendor vendor;
    private double budget;
    private String notes;
    private String status;

    public Booking() {
        this.status = "PENDING";
    }

    public Booking(int id, Client client, Venue venue, String eventType,
                   LocalDateTime startDateTime, LocalDateTime endDateTime,
                   int guestCount, boolean cateringRequired, Vendor vendor,
                   double budget, String notes, String status) {
        this.id = id;
        this.client = client;
        this.venue = venue;
        this.eventType = eventType;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.guestCount = guestCount;
        this.cateringRequired = cateringRequired;
        this.vendor = vendor;
        this.budget = budget;
        this.notes = notes;
        this.status = status;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }

    public LocalDateTime getEndDateTime() { return endDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }

    public int getGuestCount() { return guestCount; }
    public void setGuestCount(int guestCount) { this.guestCount = guestCount; }

    public boolean isCateringRequired() { return cateringRequired; }
    public void setCateringRequired(boolean cateringRequired) { this.cateringRequired = cateringRequired; }

    public Vendor getVendor() { return vendor; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Booking #" + id + " - " + eventType + " at " + (venue != null ? venue.getName() : "Unknown Venue");
    }
    // Add to your existing Booking class
    public boolean overlapsWith(Booking other) {
        return !(this.endDateTime.isBefore(other.startDateTime) ||
                other.endDateTime.isBefore(this.startDateTime));
    }

    public String getConflictDescription(Booking other) {
        return String.format(
                "Conflict with %s booking: %s to %s",
                other.getEventType(),
                other.getStartDateTime().format(DateTimeFormatter.ofPattern("MMM d, h:mm a")),
                other.getEndDateTime().format(DateTimeFormatter.ofPattern("h:mm a"))
        );
    }
}