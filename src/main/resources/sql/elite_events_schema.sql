-- Create database
CREATE DATABASE IF NOT EXISTS elite_events;
USE elite_events;

-- Clients table
CREATE TABLE IF NOT EXISTS clients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    company VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Venues table
CREATE TABLE IF NOT EXISTS venues (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    capacity INT,
    price_per_hour DECIMAL(10,2),
    amenities TEXT, -- JSON string of amenities
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Vendors table
CREATE TABLE IF NOT EXISTS vendors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bookings table
CREATE TABLE IF NOT EXISTS bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    client_id INT,
    venue_id INT,
    event_type VARCHAR(100),
    start_datetime DATETIME,
    end_datetime DATETIME,
    guest_count INT,
    catering_required BOOLEAN DEFAULT FALSE,
    vendor_id INT,
    budget DECIMAL(10,2),
    notes TEXT,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(id),
    FOREIGN KEY (venue_id) REFERENCES venues(id),
    FOREIGN KEY (vendor_id) REFERENCES vendors(id)
);

-- Insert sample data
INSERT INTO clients (name, email, phone, company) VALUES
('Tech Innovations Inc', 'events@techinnovations.com', '555-0101', 'Tech Corporation'),
('Sarah Johnson', 'sarah.johnson@email.com', '555-0102', 'Individual'),
('Global Marketing Ltd', 'bookings@globalmarketing.com', '555-0103', 'Marketing Agency');

INSERT INTO venues (name, address, capacity, price_per_hour, amenities) VALUES
('Grand Ballroom', '1 Luxury Ave', 500, 2000.00, '["Stage", "Sound System", "Lighting", "Projector"]'),
('Garden Pavilion', '2 Park Road', 200, 1200.00, '["Outdoor Space", "Garden", "Fountain", "Marquee"]'),
('Conference Hall A', '3 Business Park', 100, 800.00, '["Projector", "Whiteboard", "WiFi", "Catering Kitchen"]');

INSERT INTO vendors (name, category, email, phone) VALUES
('Elite Catering Co', 'Catering', 'catering@elite.com', '555-0201'),
('Perfect Photos Studio', 'Photography', 'photos@perfect.com', '555-0202'),
('Floral Designs', 'Decor', 'info@floraldesigns.com', '555-0203');