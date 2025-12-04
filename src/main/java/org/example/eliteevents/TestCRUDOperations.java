package org.example.eliteevents;

import org.example.eliteevents.models.*;
import org.example.eliteevents.services.DatabaseService;
import java.util.List;

public class TestCRUDOperations {
    public static void main(String[] args) {
        try {
            DatabaseService db = DatabaseService.getInstance();

            System.out.println("=== Testing CRUD Operations ===");

            // Test adding a new client
            Client newClient = new Client(0, "New Test Client", "new@test.com", "555-9999", "Test Corp");
            db.addClient(newClient);
            System.out.println("✅ New client added: " + newClient.getName());

            // Test updating a client
            newClient.setEmail("updated@test.com");
            db.updateClient(newClient);
            System.out.println("✅ Client updated with new email");

            // Test adding a new venue
            Venue newVenue = new Venue(0, "Test Venue", "123 Test St", 150, 1000.0,
                    List.of("WiFi", "Projector"));
            db.addVenue(newVenue);
            System.out.println("✅ New venue added: " + newVenue.getName());

            // Test deleting operations (commented out for safety)
            // db.deleteClient(newClient.getId());
            // System.out.println("✅ Client deleted");

            System.out.println("🎉 CRUD operations test completed!");

            db.close();

        } catch (Exception e) {
            System.err.println("❌ CRUD test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}