package org.example.eliteevents;

import org.example.eliteevents.models.*;
import org.example.eliteevents.services.DatabaseService;
import java.time.LocalDateTime;

public class TestBookingCreation {
    public static void main(String[] args) {
        try {
            DatabaseService db = DatabaseService.getInstance();

            // Get first client, venue, and vendor for testing
            Client client = db.getAllClients().get(0);
            Venue venue = db.getAllVenues().get(0);
            Vendor vendor = db.getAllVendors().get(0);

            // Create a test booking
            Booking testBooking = new Booking();
            testBooking.setClient(client);
            testBooking.setVenue(venue);
            testBooking.setEventType("Test Conference");
            testBooking.setStartDateTime(LocalDateTime.now().plusDays(1));
            testBooking.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(4));
            testBooking.setGuestCount(100);
            testBooking.setCateringRequired(true);
            testBooking.setVendor(vendor);
            testBooking.setBudget(5000.0);
            testBooking.setNotes("Test booking from Java code");
            testBooking.setStatus("CONFIRMED");

            // Save to database
            db.addBooking(testBooking);

            // Verify it was saved
            var bookings = db.getAllBookings();
            System.out.println("✅ Booking created successfully!");
            System.out.println("Total bookings in database: " + bookings.size());
            bookings.forEach(b -> System.out.println(" - " + b));

            db.close();

        } catch (Exception e) {
            System.err.println("❌ Booking test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}