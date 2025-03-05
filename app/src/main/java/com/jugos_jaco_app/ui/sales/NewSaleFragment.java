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
import androidx.appcompat.widget.SearchView;
import androidx.activity.OnBackPressedCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.models.CartItem;
import com.jugos_jaco_app.models.Product;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment principal para la creación de una nueva venta.
 * Maneja la visualización de productos, carrito y proceso de venta.
 */
public class NewSaleFragment extends Fragment implements CartAdapter.OnCartUpdateListener {
    
    // Datos del cliente
    private String clientId;
    private String clientName;
    
    // Vistas principales
    private RecyclerView rvProducts;
    private RecyclerView rvCart;
    private TextView tvTotal;
    private MaterialButton btnFinishSale;
    private View layoutProducts;
    private View layoutCart;
    
    // Adaptadores
    private ProductsAdapter productsAdapter;
    private CartAdapter cartAdapter;
    
    // Datos
    private ArrayList<CartItem> cartItems;
    private List<Product> allProducts = new ArrayList<>(); // Lista completa de productos
    
    // Control de estado
    private int currentView = 0;  // 0: ambos, 1: solo productos, 2: solo carrito
    private int previousView = 0; // Para recordar la vista anterior al mostrar teclado
    
    // Constantes para guardar estado
    private static final String KEY_CART_ITEMS = "cart_items";
    private static final String KEY_CURRENT_VIEW = "current_view";

    /**
     * Inicialización del Fragment.
     * Configura el manejo del botón atrás y restaura el estado del carrito.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Habilitar menú de opciones
        
        // Configurar interceptor del botón atrás para confirmar salida con carrito lleno
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!cartItems.isEmpty()) {
                    showExitConfirmationDialog();
                } else {
                    this.remove();
                    requireActivity().onBackPressed();
                }
            }
        });
        
        // Restaurar estado del carrito si existe
        if (savedInstanceState != null) {
            cartItems = (ArrayList<CartItem>) savedInstanceState.getSerializable(KEY_CART_ITEMS);
        }
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
    }

    /**
     * Creación de la vista del Fragment.
     * Inicializa las vistas y configura los componentes principales.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_sale, container, false);
        
        // Obtener datos del cliente de los argumentos
        if (getArguments() != null) {
            clientId = getArguments().getString("clientId");
            clientName = getArguments().getString("clientName");
        }
        
        // Inicializar y configurar componentes
        initializeViews(view);
        setupRecyclerViews();
        loadProducts();
        
        layoutProducts = view.findViewById(R.id.layoutProducts);
        layoutCart = view.findViewById(R.id.layoutCart);
        
        // Restaurar estado de la vista
        if (savedInstanceState != null) {
            currentView = savedInstanceState.getInt(KEY_CURRENT_VIEW, 0);
        }
        
        // Restaurar estado de productos en carrito
        if (!cartItems.isEmpty()) {
            List<String> productIds = new ArrayList<>();
            for (CartItem item : cartItems) {
                productIds.add(item.getProduct().getId());
            }
            productsAdapter.updateCartState(productIds);
            updateTotal();
        }
        
        updateViewVisibility();
        return view;
    }

    /**
     * Guarda el estado del Fragment para restauración.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_CART_ITEMS, cartItems);
        outState.putInt(KEY_CURRENT_VIEW, currentView);
    }

    /**
     * Configura el menú de opciones con búsqueda y cambio de vista.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        
        // Configurar SearchView
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
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

        // Agregar botón de cambio de vista
        menu.add(Menu.NONE, 1, Menu.NONE, "Cambiar Vista")
            .setIcon(R.drawable.ic_view_list)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    /**
     * Maneja las selecciones del menú de opciones.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            toggleView();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Inicializa todas las vistas del Fragment.
     * Configura el nombre del cliente y los listeners básicos.
     */
    private void initializeViews(View view) {
        TextView tvClientName = view.findViewById(R.id.tvClientName);
        tvClientName.setText("Cliente: " + clientName);
        
        rvProducts = view.findViewById(R.id.rvProducts);
        rvCart = view.findViewById(R.id.rvCart);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnFinishSale = view.findViewById(R.id.btnFinishSale);
        
        btnFinishSale.setOnClickListener(v -> finishSale());
    }
    
    /**
     * Configura los RecyclerViews de productos y carrito.
     * Establece los adaptadores y sus listeners.
     */
    private void setupRecyclerViews() {
        // Configurar RecyclerView de productos con 3 columnas
        productsAdapter = new ProductsAdapter(new ArrayList<>(), this::addToCart);
        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 3));
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

            @Override
            public void onKeyboardShowing() {
                if (currentView == 0) { // Si se están mostrando ambos
                    previousView = currentView;
                    currentView = 2; // Cambiar a solo carrito
                    updateViewVisibility();
                }
            }

            @Override
            public void onKeyboardHiding() {
                if (previousView == 0) { // Si antes se mostraban ambos
                    currentView = previousView;
                    previousView = -1; // Resetear el previousView
                    updateViewVisibility();
                }
            }
        });
        rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCart.setAdapter(cartAdapter);
    }
    
    /**
     * Carga la lista inicial de productos.
     * TODO: Reemplazar con carga desde base de datos.
     */
    private void loadProducts() {
        allProducts = new ArrayList<>();
        
        // Datos de prueba
        allProducts.add(new Product(
            "1", "Esencia de Vainilla", "VAN-001", "15ml",
            "1", "Esencias Dulces", 25.00,
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

        productsAdapter.updateProducts(allProducts);
    }
    
    /**
     * Agrega un producto al carrito o incrementa su cantidad si ya existe.
     * Actualiza la UI y el total.
     */
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
    
    /**
     * Calcula y actualiza el total de la venta.
     */
    private void updateTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getQuantity() * item.getProduct().getPrice();
        }
        tvTotal.setText(String.format("Total: L. %.2f", total));
    }
    
    /**
     * Método temporal para finalizar la venta.
     * TODO: Implementar la lógica completa de guardado
     */
    private void finishSale() {
        // TODO: Implementar guardado de la venta
    }

    /**
     * Alterna entre las diferentes vistas disponibles:
     * 0: Productos y carrito
     * 1: Solo productos
     * 2: Solo carrito
     */
    private void toggleView() {
        currentView = (currentView + 1) % 3;
        updateViewVisibility();
    }

    /**
     * Actualiza la visibilidad de las vistas según el modo actual.
     * Controla la visualización de productos y carrito.
     */
    private void updateViewVisibility() {
        switch (currentView) {
            case 0: // Mostrar ambos
                layoutProducts.setVisibility(View.VISIBLE);
                layoutCart.setVisibility(View.VISIBLE);
                break;
            case 1: // Solo carrito
                layoutProducts.setVisibility(View.GONE);
                layoutCart.setVisibility(View.VISIBLE);

                break;
            case 2: // Solo productos
                layoutProducts.setVisibility(View.VISIBLE);
                layoutCart.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * Filtra la lista de productos según el texto de búsqueda.
     * Busca coincidencias en nombre y código del producto.
     */
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

    // Implementación de CartAdapter.OnCartUpdateListener

    /**
     * Llamado cuando se elimina un item del carrito.
     * Actualiza el estado del producto en la lista de productos.
     */
    @Override
    public void onCartItemRemoved(CartItem item) {
        productsAdapter.setProductInCart(item.getProduct().getId(), false);
    }

    /**
     * Llamado cuando se actualiza un item del carrito.
     * Recalcula el total de la venta.
     */
    @Override
    public void onCartUpdated() {
        updateTotal();
    }

    /**
     * Llamado cuando se muestra el teclado.
     * Cambia a vista de solo carrito si se estaban mostrando ambas vistas.
     */
    @Override
    public void onKeyboardShowing() {
        if (currentView == 0) { // Si se están mostrando ambos
            previousView = currentView;
            currentView = 2; // Cambiar a solo carrito
            updateViewVisibility();
        }
    }

    /**
     * Llamado cuando se oculta el teclado.
     * Restaura la vista anterior si se estaban mostrando ambas vistas.
     */
    @Override
    public void onKeyboardHiding() {
        if (previousView == 0) { // Si antes se mostraban ambos
            currentView = previousView;
            previousView = -1; // Resetear el previousView
            updateViewVisibility();
        }
    }

    /**
     * Muestra un diálogo de confirmación al intentar salir con productos en el carrito.
     * Permite al usuario cancelar la acción o confirmar y perder los productos.
     */
    private void showExitConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("¿Desea salir?")
            .setMessage("Si sale ahora, perderá todos los productos en el carrito.")
            .setPositiveButton("Salir", (dialog, which) -> {
                // Limpiar el carrito y salir
                cartItems.clear();
                requireActivity().onBackPressed();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
} 