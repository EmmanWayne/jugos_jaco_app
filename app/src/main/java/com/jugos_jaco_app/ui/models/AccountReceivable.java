package com.jugos_jaco_app.ui.models;

public class AccountReceivable {
    private int id;
    private int salesId;
    private String clientName;
    private double totalAmount;
    private double remainingBalance;
    private String dueDate;
    private String status;

    public AccountReceivable() {
    }

    public AccountReceivable(int id, int salesId, String clientName, double totalAmount, double remainingBalance, String dueDate, String status) {
        this.id = id;
        this.salesId = salesId;
        this.clientName = clientName;
        this.totalAmount = totalAmount;
        this.remainingBalance = remainingBalance;
        this.dueDate = dueDate;
        this.status = status;
    }

    public AccountReceivable(int id, String clientName, double totalAmount, double remainingBalance, String dueDate, String status) {
        this(id, 0, clientName, totalAmount, remainingBalance, dueDate, status);
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getSalesId() {
        return salesId;
    }

    public String getClientName() {
        return clientName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getRemainingBalance() {
        return remainingBalance;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setSalesId(int salesId) {
        this.salesId = salesId;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setRemainingBalance(double remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Helper methods
    public boolean isPaid() {
        return "Pagado".equals(status);
    }

    public boolean isPending() {
        return "Pendiente".equals(status);
    }

    public double getPaidAmount() {
        return totalAmount - remainingBalance;
    }

}