package org.example.eliteevents.models;

public class Client {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String company;

    public Client() {}

    public Client(int id, String name, String email, String phone, String company) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.company = company;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    @Override
    public String toString() {
        return name;
    }

}