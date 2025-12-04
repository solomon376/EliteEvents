package org.example.eliteevents.models;

import java.util.ArrayList;
import java.util.List;

public class Venue {
    private int id;
    private String name;
    private String address;
    private int capacity;
    private double pricePerHour;
    private List<String> amenities;

    public Venue() {
        this.amenities = new ArrayList<>();
    }

    public Venue(int id, String name, String address, int capacity, double pricePerHour, List<String> amenities) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.capacity = capacity;
        this.pricePerHour = pricePerHour;
        this.amenities = amenities != null ? amenities : new ArrayList<>();
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public double getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(double pricePerHour) { this.pricePerHour = pricePerHour; }

    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }

    @Override
    public String toString() {
        return name + " (Capacity: " + capacity + ")";
    }
}