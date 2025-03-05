package com.jugos_jaco_app.ui.sales;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import androidx.appcompat.widget.SearchView;

public class NewSaleFragment extends Fragment implements CartAdapter.OnCartUpdateListener {
    
    private String clientId;
    private String clientName;
    private RecyclerView rvProducts;
    private RecyclerView rvCart;
    private ProductsAdapter productsAdapter;
    private CartAdapter cartAdapter;
    private TextView tvTotal;
    private MaterialButton btnFinishSale;
    private ArrayList<CartItem> cartItems;
    private View layoutProducts;
    private View layoutCart;
    private int currentView = 0; // 0: ambos, 1: solo productos, 2: solo carrito
    private static final String KEY_CART_ITEMS = "cart_items";
    private static final String KEY_CURRENT_VIEW = "current_view";
    private List<Product> allProducts = new ArrayList<>(); // Lista completa de productos
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Habilitar menú de opciones
        
        // Inicializar cartItems
        if (savedInstanceState != null) {
            cartItems = (ArrayList<CartItem>) savedInstanceState.getSerializable(KEY_CART_ITEMS);
        }
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_sale, container, false);
        
        // Obtener datos del cliente
        if (getArguments() != null) {
            clientId = getArguments().getString("clientId");
            clientName = getArguments().getString("clientName");
        }
        
        // Inicializar vistas
        initializeViews(view);
        
        // Configurar RecyclerViews
        setupRecyclerViews();
        
        // Cargar productos
        loadProducts();
        
        layoutProducts = view.findViewById(R.id.layoutProducts);
        layoutCart = view.findViewById(R.id.layoutCart);
        
        // Restaurar el estado de la vista
        if (savedInstanceState != null) {
            currentView = savedInstanceState.getInt(KEY_CURRENT_VIEW, 0);
        }
        
        // Actualizar estado de productos en carrito
        if (!cartItems.isEmpty()) {
            List<String> productIds = new ArrayList<>();
            for (CartItem item : cartItems) {
                productIds.add(item.getProduct().getId());
            }
            productsAdapter.updateCartState(productIds);
            updateTotal();
        }
        
        // Restaurar la vista actual
        updateViewVisibility();
        
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_CART_ITEMS, cartItems);
        outState.putInt(KEY_CURRENT_VIEW, currentView);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        
        // Obtener el SearchView que ya existe
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        
        // Configurar el SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText);
                return true;
            }
        });

        // Agregar el botón de vista que se eliminó accidentalmente
        menu.add(Menu.NONE, 1, Menu.NONE, "Cambiar Vista")
            .setIcon(R.drawable.ic_view_list)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            toggleView();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        cartAdapter.setOnCartUpdateListener(new CartAdapter.OnCartUpdateListener() {
            @Override
            public void onCartItemRemoved(CartItem item) {
                productsAdapter.setProductInCart(item.getProduct().getId(), false);
                updateTotal();
            }

            @Override
            public void onCartUpdated() {
                updateTotal();
            }
        });
        rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCart.setAdapter(cartAdapter);
    }
    
    private void loadProducts() {
        // Crear lista de productos de prueba
        allProducts = new ArrayList<>(); // Guardar la lista completa
        
        allProducts.add(new Product(
            "1",
            "Esencia de Vainilla",
            "VAN-001",
            "15ml",
            "1",
            "Esencias Dulces",
            25.00,
            "https://example.com/vanilla.jpg"
        ));
        
        allProducts.add(new Product(
            "2",
            "Esencia de Chocolate",
            "CHO-001",
            "20ml",
            "1",
            "Esencias Dulces",
            30.00,
            "https://example.com/chocolate.jpg"
        ));
        
        allProducts.add(new Product(
            "3",
            "Esencia de Fresa",
            "FRE-001",
            "15ml",
            "2",
            "Esencias Frutales",
            28.00,
            "https://example.com/strawberry.jpg"
        ));
        
        allProducts.add(new Product(
            "4",
            "Esencia de Menta",
            "MEN-001",
            "20ml",
            "3",
            "Esencias Frescas",
            32.00,
            "https://example.com/mint.jpg"
        ));

        // Actualizar el adaptador con todos los productos
        productsAdapter.updateProducts(allProducts);
    }
    
    private void addToCart(Product product) {
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
            productsAdapter.setProductInCart(product.getId(), true);
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

    private void toggleView() {
        currentView = (currentView + 1) % 3;
        updateViewVisibility();
    }

    private void updateViewVisibility() {
        switch (currentView) {
            case 0: // Mostrar ambos
                layoutProducts.setVisibility(View.VISIBLE);
                layoutCart.setVisibility(View.VISIBLE);
                break;
            case 1: // Solo productos
                layoutProducts.setVisibility(View.VISIBLE);
                layoutCart.setVisibility(View.GONE);
                break;
            case 2: // Solo carrito
                layoutProducts.setVisibility(View.GONE);
                layoutCart.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void filterProducts(String query) {
        if (query == null || query.isEmpty()) {
            productsAdapter.updateProducts(allProducts);
            return;
        }

        List<Product> filteredList = new ArrayList<>();
        String searchQuery = query.toLowerCase().trim();

        for (Product product : allProducts) {
            if (product.getName().toLowerCase().contains(searchQuery) ||
                product.getCode().toLowerCase().contains(searchQuery)) {
                filteredList.add(product);
            }
        }

        productsAdapter.updateProducts(filteredList);
    }

    @Override
    public void onCartItemRemoved(CartItem item) {
        productsAdapter.setProductInCart(item.getProduct().getId(), false);
    }

    @Override
    public void onCartUpdated() {
        updateTotal(); // Actualizar el total cuando cambia la cantidad
    }
} 