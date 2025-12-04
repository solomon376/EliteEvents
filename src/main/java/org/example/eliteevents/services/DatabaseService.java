package org.example.eliteevents.services;

import org.example.eliteevents.models.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

public class DatabaseService {
    private static DatabaseService instance;
    private Connection connection;
    private static final Logger logger = Logger.getLogger(DatabaseService.class.getName());

    private DatabaseService() {
        connect();
    }

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    private void connect() {
        try {
            Properties props = new Properties();
            props.load(getClass().getResourceAsStream("/config/database.properties"));

            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            connection = DriverManager.getConnection(url, username, password);
            logger.info("✅ Connected to MySQL database");

        } catch (Exception e) {
            logger.severe("❌ Database connection failed: " + e.getMessage());
            throw new RuntimeException("Cannot connect to database", e);
        }
    }

    // Client operations
    public List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM clients ORDER BY name";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Client client = new Client();
                client.setId(rs.getInt("id"));
                client.setName(rs.getString("name"));
                client.setEmail(rs.getString("email"));
                client.setPhone(rs.getString("phone"));
                client.setCompany(rs.getString("company"));
                clients.add(client);
            }

        } catch (SQLException e) {
            logger.severe("Error fetching clients: " + e.getMessage());
            throw new RuntimeException("Failed to fetch clients", e);
        }

        return clients;
    }

    // Venue operations
    public List<Venue> getAllVenues() {
        List<Venue> venues = new ArrayList<>();
        String sql = "SELECT * FROM venues ORDER BY name";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Venue venue = new Venue();
                venue.setId(rs.getInt("id"));
                venue.setName(rs.getString("name"));
                venue.setAddress(rs.getString("address"));
                venue.setCapacity(rs.getInt("capacity"));
                venue.setPricePerHour(rs.getDouble("price_per_hour"));

                // Parse amenities
                String amenitiesStr = rs.getString("amenities");
                if (amenitiesStr != null && !amenitiesStr.isEmpty()) {
                    venue.setAmenities(Arrays.asList(amenitiesStr.split(",")));
                }

                venues.add(venue);
            }

        } catch (SQLException e) {
            logger.severe("Error fetching venues: " + e.getMessage());
            throw new RuntimeException("Failed to fetch venues", e);
        }

        return venues;
    }

    // Vendor operations
    public List<Vendor> getAllVendors() {
        List<Vendor> vendors = new ArrayList<>();
        String sql = "SELECT * FROM vendors ORDER BY name";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Vendor vendor = new Vendor();
                vendor.setId(rs.getInt("id"));
                vendor.setName(rs.getString("name"));
                vendor.setCategory(rs.getString("category"));
                vendor.setEmail(rs.getString("email"));
                vendor.setPhone(rs.getString("phone"));
                vendors.add(vendor);
            }

        } catch (SQLException e) {
            logger.severe("Error fetching vendors: " + e.getMessage());
            throw new RuntimeException("Failed to fetch vendors", e);
        }

        return vendors;
    }

    // Booking operations
    public void addBooking(Booking booking) {
        String sql = "INSERT INTO bookings (client_id, venue_id, event_type, start_datetime, end_datetime, " +
                "guest_count, catering_required, vendor_id, budget, notes, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, booking.getClient().getId());
            stmt.setInt(2, booking.getVenue().getId());
            stmt.setString(3, booking.getEventType());
            stmt.setTimestamp(4, Timestamp.valueOf(booking.getStartDateTime()));
            stmt.setTimestamp(5, Timestamp.valueOf(booking.getEndDateTime()));
            stmt.setInt(6, booking.getGuestCount());
            stmt.setBoolean(7, booking.isCateringRequired());
            stmt.setObject(8, booking.getVendor() != null ? booking.getVendor().getId() : null);
            stmt.setDouble(9, booking.getBudget());
            stmt.setString(10, booking.getNotes());
            stmt.setString(11, booking.getStatus());

            stmt.executeUpdate();

            // Get generated ID
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    booking.setId(generatedKeys.getInt(1));
                }
            }

            logger.info("✅ Booking added: " + booking.getId());

        } catch (SQLException e) {
            logger.severe("Error adding booking: " + e.getMessage());
            throw new RuntimeException("Failed to add booking", e);
        }
    }

    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, c.name as client_name, c.email as client_email, " +
                "c.phone as client_phone, c.company as client_company, " +
                "v.name as venue_name, v.address as venue_address, v.capacity as venue_capacity, " +
                "vd.name as vendor_name, vd.category as vendor_category " +
                "FROM bookings b " +
                "JOIN clients c ON b.client_id = c.id " +
                "JOIN venues v ON b.venue_id = v.id " +
                "LEFT JOIN vendors vd ON b.vendor_id = vd.id " +
                "ORDER BY b.start_datetime DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Booking booking = new Booking();
                booking.setId(rs.getInt("id"));

                // Client
                Client client = new Client();
                client.setId(rs.getInt("client_id"));
                client.setName(rs.getString("client_name"));
                client.setEmail(rs.getString("client_email"));
                client.setPhone(rs.getString("client_phone"));
                client.setCompany(rs.getString("client_company"));
                booking.setClient(client);

                // Venue
                Venue venue = new Venue();
                venue.setId(rs.getInt("venue_id"));
                venue.setName(rs.getString("venue_name"));
                venue.setAddress(rs.getString("venue_address"));
                venue.setCapacity(rs.getInt("venue_capacity"));
                booking.setVenue(venue);

                // Vendor (optional)
                if (rs.getObject("vendor_id") != null) {
                    Vendor vendor = new Vendor();
                    vendor.setId(rs.getInt("vendor_id"));
                    vendor.setName(rs.getString("vendor_name"));
                    vendor.setCategory(rs.getString("vendor_category"));
                    booking.setVendor(vendor);
                }

                booking.setEventType(rs.getString("event_type"));
                booking.setStartDateTime(rs.getTimestamp("start_datetime").toLocalDateTime());
                booking.setEndDateTime(rs.getTimestamp("end_datetime").toLocalDateTime());
                booking.setGuestCount(rs.getInt("guest_count"));
                booking.setCateringRequired(rs.getBoolean("catering_required"));
                booking.setBudget(rs.getDouble("budget"));
                booking.setNotes(rs.getString("notes"));
                booking.setStatus(rs.getString("status"));

                bookings.add(booking);
            }

        } catch (SQLException e) {
            logger.severe("Error fetching bookings: " + e.getMessage());
            throw new RuntimeException("Failed to fetch bookings", e);
        }

        return bookings;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("✅ Database connection closed");
            }
        } catch (SQLException e) {
            logger.severe("❌ Error closing connection: " + e.getMessage());
        }
    }

    // Add these methods to your existing DatabaseService class

    // Client CRUD
    public void updateClient(Client client) {
        String sql = "UPDATE clients SET name = ?, email = ?, phone = ?, company = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, client.getName());
            stmt.setString(2, client.getEmail());
            stmt.setString(3, client.getPhone());
            stmt.setString(4, client.getCompany());
            stmt.setInt(5, client.getId());

            stmt.executeUpdate();
            logger.info("✅ Client updated: " + client.getName());

        } catch (SQLException e) {
            logger.severe("Error updating client: " + e.getMessage());
            throw new RuntimeException("Failed to update client", e);
        }
    }

    public boolean deleteClient(int clientId) {
        String sql = "DELETE FROM clients WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("✅ Client deleted: " + clientId);
                return true;
            }
            return false;

        } catch (SQLException e) {
            logger.severe("Error deleting client: " + e.getMessage());
            throw new RuntimeException("Failed to delete client", e);
        }
    }

    // Venue CRUD
    public void updateVenue(Venue venue) {
        String sql = "UPDATE venues SET name = ?, address = ?, capacity = ?, price_per_hour = ?, amenities = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, venue.getName());
            stmt.setString(2, venue.getAddress());
            stmt.setInt(3, venue.getCapacity());
            stmt.setDouble(4, venue.getPricePerHour());
            stmt.setString(5, String.join(",", venue.getAmenities()));
            stmt.setInt(6, venue.getId());

            stmt.executeUpdate();
            logger.info("✅ Venue updated: " + venue.getName());

        } catch (SQLException e) {
            logger.severe("Error updating venue: " + e.getMessage());
            throw new RuntimeException("Failed to update venue", e);
        }
    }

    public boolean deleteVenue(int venueId) {
        String sql = "DELETE FROM venues WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, venueId);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("✅ Venue deleted: " + venueId);
                return true;
            }
            return false;

        } catch (SQLException e) {
            logger.severe("Error deleting venue: " + e.getMessage());
            throw new RuntimeException("Failed to delete venue", e);
        }
    }

    // Vendor CRUD
    public void updateVendor(Vendor vendor) {
        String sql = "UPDATE vendors SET name = ?, category = ?, email = ?, phone = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, vendor.getName());
            stmt.setString(2, vendor.getCategory());
            stmt.setString(3, vendor.getEmail());
            stmt.setString(4, vendor.getPhone());
            stmt.setInt(5, vendor.getId());

            stmt.executeUpdate();
            logger.info("✅ Vendor updated: " + vendor.getName());

        } catch (SQLException e) {
            logger.severe("Error updating vendor: " + e.getMessage());
            throw new RuntimeException("Failed to update vendor", e);
        }
    }

    public boolean deleteVendor(int vendorId) {
        String sql = "DELETE FROM vendors WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, vendorId);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("✅ Vendor deleted: " + vendorId);
                return true;
            }
            return false;

        } catch (SQLException e) {
            logger.severe("Error deleting vendor: " + e.getMessage());
            throw new RuntimeException("Failed to delete vendor", e);
        }
    }

    // Booking CRUD
    public void updateBooking(Booking booking) {
        String sql = "UPDATE bookings SET client_id = ?, venue_id = ?, event_type = ?, " +
                "start_datetime = ?, end_datetime = ?, guest_count = ?, catering_required = ?, " +
                "vendor_id = ?, budget = ?, notes = ?, status = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, booking.getClient().getId());
            stmt.setInt(2, booking.getVenue().getId());
            stmt.setString(3, booking.getEventType());
            stmt.setTimestamp(4, Timestamp.valueOf(booking.getStartDateTime()));
            stmt.setTimestamp(5, Timestamp.valueOf(booking.getEndDateTime()));
            stmt.setInt(6, booking.getGuestCount());
            stmt.setBoolean(7, booking.isCateringRequired());
            stmt.setObject(8, booking.getVendor() != null ? booking.getVendor().getId() : null);
            stmt.setDouble(9, booking.getBudget());
            stmt.setString(10, booking.getNotes());
            stmt.setString(11, booking.getStatus());
            stmt.setInt(12, booking.getId());

            stmt.executeUpdate();
            logger.info("✅ Booking updated: " + booking.getId());

        } catch (SQLException e) {
            logger.severe("Error updating booking: " + e.getMessage());
            throw new RuntimeException("Failed to update booking", e);
        }
    }

    public boolean deleteBooking(int bookingId) {
        String sql = "DELETE FROM bookings WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("✅ Booking deleted: " + bookingId);
                return true;
            }
            return false;

        } catch (SQLException e) {
            logger.severe("Error deleting booking: " + e.getMessage());
            throw new RuntimeException("Failed to delete booking", e);
        }
    }

    // Add new client, venue, vendor
    public void addClient(Client client) {
        String sql = "INSERT INTO clients (name, email, phone, company) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, client.getName());
            stmt.setString(2, client.getEmail());
            stmt.setString(3, client.getPhone());
            stmt.setString(4, client.getCompany());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    client.setId(generatedKeys.getInt(1));
                }
            }

            logger.info("✅ Client added: " + client.getName());

        } catch (SQLException e) {
            logger.severe("Error adding client: " + e.getMessage());
            throw new RuntimeException("Failed to add client", e);
        }
    }

    public void addVenue(Venue venue) {
        String sql = "INSERT INTO venues (name, address, capacity, price_per_hour, amenities) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, venue.getName());
            stmt.setString(2, venue.getAddress());
            stmt.setInt(3, venue.getCapacity());
            stmt.setDouble(4, venue.getPricePerHour());
            stmt.setString(5, String.join(",", venue.getAmenities()));

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    venue.setId(generatedKeys.getInt(1));
                }
            }

            logger.info("✅ Venue added: " + venue.getName());

        } catch (SQLException e) {
            logger.severe("Error adding venue: " + e.getMessage());
            throw new RuntimeException("Failed to add venue", e);
        }
    }

    public void addVendor(Vendor vendor) {
        String sql = "INSERT INTO vendors (name, category, email, phone) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, vendor.getName());
            stmt.setString(2, vendor.getCategory());
            stmt.setString(3, vendor.getEmail());
            stmt.setString(4, vendor.getPhone());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    vendor.setId(generatedKeys.getInt(1));
                }
            }

            logger.info("✅ Vendor added: " + vendor.getName());

        } catch (SQLException e) {
            logger.severe("Error adding vendor: " + e.getMessage());
            throw new RuntimeException("Failed to add vendor", e);
        }
    }
}