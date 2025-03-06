package com.jugos_jaco_app.ui.models;

public class Employee {
    private int id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String identity;
    private int branchId;
    private Branch branch;
    private String createdAt;
    private String updatedAt;

    // Clase interna para Branch
    public static class Branch {
        private int id;
        private String name;
        private String address;
        private String phoneNumber;

        // Constructor
        public Branch(int id, String name, String address, String phoneNumber) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.phoneNumber = phoneNumber;
        }

        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
        public String getAddress() { return address; }
        public String getPhoneNumber() { return phoneNumber; }
    }

    // Constructor
    public Employee(int id, String firstName, String lastName, String phoneNumber,
                   String address, String identity, int branchId, Branch branch,
                   String createdAt, String updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.identity = identity;
        this.branchId = branchId;
        this.branch = branch;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public String getIdentity() { return identity; }
    public int getBranchId() { return branchId; }
    public Branch getBranch() { return branch; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Método helper para obtener nombre completo
    public String getFullName() {
        return firstName + " " + lastName;
    }
} 