package org.example.eliteevents;

import org.example.eliteevents.models.*;
import org.example.eliteevents.services.DatabaseService;
import java.time.LocalDateTime;

public class TestEnhancedService {
    public static void main(String[] args) {
        try {
            DatabaseService db = DatabaseService.getInstance();

            System.out.println("=== Testing Enhanced Database Service ===");

            // Test getting clients
            var clients = db.getAllClients();
            System.out.println("✅ Clients loaded: " + clients.size());
            clients.forEach(c -> System.out.println("   - " + c.getName()));

            // Test getting venues
            var venues = db.getAllVenues();
            System.out.println("✅ Venues loaded: " + venues.size());
            venues.forEach(v -> System.out.println("   - " + v.getName() + " (Capacity: " + v.getCapacity() + ")"));

            // Test getting vendors
            var vendors = db.getAllVendors();
            System.out.println("✅ Vendors loaded: " + vendors.size());
            vendors.forEach(v -> System.out.println("   - " + v.getName() + " (" + v.getCategory() + ")"));

            // Test getting bookings (should be empty initially)
            var bookings = db.getAllBookings();
            System.out.println("✅ Current bookings: " + bookings.size());

            System.out.println("🎉 Enhanced service test completed!");

            db.close();

        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}