package com.jugos_jaco_app.ui.sales;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.GridLayoutManager;
import com.google.android.material.button.MaterialButton;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.models.CartItem;
import com.jugos_jaco_app.models.Product;
import java.util.ArrayList;
import java.util.List;

public class NewSaleFragment extends Fragment {
    
    private String clientId;
    private String clientName;
    private RecyclerView rvProducts;
    private RecyclerView rvCart;
    private ProductsAdapter productsAdapter;
    private CartAdapter cartAdapter;
    private TextView tvTotal;
    private MaterialButton btnFinishSale;
    private ArrayList<CartItem> cartItems = new ArrayList<>();
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_sale, container, false);
        
        // Obtener datos del cliente
        if (getArguments() != null) {
            clientId = getArguments().getString("clientId");
            clientName = getArguments().getString("clientName");
        }
        
        initializeViews(view);
        setupRecyclerViews();
        loadProducts();
        
        return view;
    }
    
    private void initializeViews(View view) {
        TextView tvClientName = view.findViewById(R.id.tvClientName);
        tvClientName.setText("Cliente: " + clientName);
        
        rvProducts = view.findViewById(R.id.rvProducts);
        rvCart = view.findViewById(R.id.rvCart);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnFinishSale = view.findViewById(R.id.btnFinishSale);
        
        btnFinishSale.setOnClickListener(v -> finishSale());
    }
    
    private void setupRecyclerViews() {
        // Configurar RecyclerView de productos
        productsAdapter = new ProductsAdapter(new ArrayList<>(), this::addToCart);
        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProducts.setAdapter(productsAdapter);
        
        // Configurar RecyclerView del carrito
        cartAdapter = new CartAdapter(cartItems, this::updateTotal);
        rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCart.setAdapter(cartAdapter);
    }
    
    private void loadProducts() {
        // Crear lista de productos de prueba
        List<Product> products = new ArrayList<>();
        
        products.add(new Product(
            "1",
            "Esencia de Vainilla",
            "VAN-001",
            "15ml",
            "1",
            "Esencias Dulces",
            25.00,
            "https://example.com/vanilla.jpg"  // URL de imagen de prueba
        ));
        
        products.add(new Product(
            "2",
            "Esencia de Chocolate",
            "CHO-001",
            "20ml",
            "1",
            "Esencias Dulces",
            30.00,
            "https://example.com/chocolate.jpg"
        ));
        
        products.add(new Product(
            "3",
            "Esencia de Fresa",
            "FRE-001",
            "15ml",
            "2",
            "Esencias Frutales",
            28.00,
            "https://example.com/strawberry.jpg"
        ));
        
        products.add(new Product(
            "4",
            "Esencia de Menta",
            "MEN-001",
            "20ml",
            "3",
            "Esencias Frescas",
            32.00,
            "https://example.com/mint.jpg"
        ));

        // Actualizar el adaptador con los productos
        productsAdapter.updateProducts(products);
    }
    
    private void addToCart(Product product) {
        // Buscar si el producto ya está en el carrito
        CartItem existingItem = null;
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(product.getId())) {
                existingItem = item;
                break;
            }
        }
        
        if (existingItem != null) {
            existingItem.incrementQuantity();
            cartAdapter.notifyDataSetChanged();
        } else {
            CartItem newItem = new CartItem(product, 1);
            cartItems.add(newItem);
            cartAdapter.notifyItemInserted(cartItems.size() - 1);
        }
        
        updateTotal();
    }
    
    private void updateTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getQuantity() * item.getProduct().getPrice();
        }
        tvTotal.setText(String.format("Total: L. %.2f", total));
    }
    
    private void finishSale() {
        // TODO: Implementar guardado de la venta
    }
} 