package org.example.eliteevents.models;

public class Vendor {
    private int id;
    private String name;
    private String category;
    private String email;
    private String phone;

    public Vendor() {}

    public Vendor(int id, String name, String category, String email, String phone) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.email = email;
        this.phone = phone;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String toString() {
        return name + " (" + category + ")";
    }
}