package com.jugos_jaco_app.ui.models;

import java.io.Serializable;

/**
 * Modelo para representar una venta en la aplicación.
 */
public class Sale implements Serializable {
    private int id;
    private String clientName;
    private String businessName;
    private String employeeName;
    private String saleDate;
    private String formattedSaleDate; // Fecha formateada en letras
    private double cashAmount;
    private String paymentReference;
    private String notes;
    private String paymentMethod;
    private String paymentTerm;
    private double subtotal;
    private double totalAmount;

    public Sale(int id, String clientName, String businessName, String employeeName, String saleDate,
               double cashAmount, String paymentReference, String notes, 
               String paymentMethod, String paymentTerm, double subtotal, double totalAmount) {
        this.id = id;
        this.clientName = clientName;
        this.businessName = businessName;
        this.employeeName = employeeName;
        this.saleDate = saleDate;
        this.cashAmount = cashAmount;
        this.paymentReference = paymentReference;
        this.notes = notes;
        this.paymentMethod = paymentMethod;
        this.paymentTerm = paymentTerm;
        this.subtotal = subtotal;
        this.totalAmount = totalAmount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }
    
    public String getFormattedSaleDate() {
        return formattedSaleDate;
    }

    public void setFormattedSaleDate(String formattedSaleDate) {
        this.formattedSaleDate = formattedSaleDate;
    }

    public double getCashAmount() {
        return cashAmount;
    }

    public void setCashAmount(double cashAmount) {
        this.cashAmount = cashAmount;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentTerm() {
        return paymentTerm;
    }

    public void setPaymentTerm(String paymentTerm) {
        this.paymentTerm = paymentTerm;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}