package com.jugos_jaco_app.ui.models;

import java.io.Serializable;

/**
 * Modelo que representa un item en el carrito de compras.
 * Implementa Serializable para poder guardar el estado del carrito.
 */
public class CartItem implements Serializable {
    private Product product;    // Producto seleccionado
    private int quantity;       // Cantidad del producto
    private String movementType; // Tipo de movimiento: null=normal, "royalty"=Regalía, "change"=Cambio

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.movementType = null;
    }

    // Métodos para manejar la cantidad
    public void incrementQuantity() {
        this.quantity++;
    }

    public void decrementQuantity() {
        if (this.quantity > 1) {
            this.quantity--;
        }
    }

    // Getters y setters
    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public boolean hasMovementType() {
        return movementType != null;
    }
} 