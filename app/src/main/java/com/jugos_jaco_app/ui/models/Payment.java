package com.jugos_jaco_app.ui.models;

/**
 * Modelo que representa un pago de una cuenta por cobrar.
 */
public class Payment {
    private int id;
    private double amount;
    private double balanceAfterPayment;
    private String paymentDate;
    private String paymentMethod;

    public Payment() {
    }

    public Payment(int id, double amount, double balanceAfterPayment, String paymentDate, String paymentMethod) {
        this.id = id;
        this.amount = amount;
        this.balanceAfterPayment = balanceAfterPayment;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
    }

    // Getters
    public int getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalanceAfterPayment() {
        return balanceAfterPayment;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setBalanceAfterPayment(double balanceAfterPayment) {
        this.balanceAfterPayment = balanceAfterPayment;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}