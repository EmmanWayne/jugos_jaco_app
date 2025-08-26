package com.jugos_jaco_app.ui.models;

import java.io.Serializable;

public class SaleDetail implements Serializable {
    private int id;
    private int productId;
    private String productName;
    private String productCode;
    private String unitName;
    private String unitAbbreviation;
    private int quantity;
    private String taxCategoryName;
    private double taxRate;
    private double lineSubtotal;
    private double lineTaxAmount;
    private double lineTotal;
    private boolean priceIncludeTax;
    private double discountPercentage;
    private double discountAmount;

    public SaleDetail() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getUnitAbbreviation() {
        return unitAbbreviation;
    }

    public void setUnitAbbreviation(String unitAbbreviation) {
        this.unitAbbreviation = unitAbbreviation;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getTaxCategoryName() {
        return taxCategoryName;
    }

    public void setTaxCategoryName(String taxCategoryName) {
        this.taxCategoryName = taxCategoryName;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
    }

    public double getLineSubtotal() {
        return lineSubtotal;
    }

    public void setLineSubtotal(double lineSubtotal) {
        this.lineSubtotal = lineSubtotal;
    }

    public double getLineTaxAmount() {
        return lineTaxAmount;
    }

    public void setLineTaxAmount(double lineTaxAmount) {
        this.lineTaxAmount = lineTaxAmount;
    }

    public double getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(double lineTotal) {
        this.lineTotal = lineTotal;
    }

    public boolean isPriceIncludeTax() {
        return priceIncludeTax;
    }

    public void setPriceIncludeTax(boolean priceIncludeTax) {
        this.priceIncludeTax = priceIncludeTax;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }
}