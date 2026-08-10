package com.jugos_jaco_app.ui.sales;

 import static com.jugos_jaco_app.Login.PREFS_NAME;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.appcompat.widget.SearchView;
import androidx.activity.OnBackPressedCallback;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jugos_jaco_app.ui.clients.ClientsFragment;

import java.util.HashMap;
import java.util.Map;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.adapters.CartAdapter;
import com.jugos_jaco_app.ui.adapters.ProductsAdapter;
import com.jugos_jaco_app.ui.models.CartItem;
import com.jugos_jaco_app.ui.models.Product;
import com.jugos_jaco_app.ui.utilities.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        boolean fromProductsAssigned = false;
        if (getArguments() != null) {
            clientId = getArguments().getString("clientId");
            Toast.makeText(getContext(), ""+clientId, Toast.LENGTH_SHORT).show();
            clientName = getArguments().getString("clientName");
            fromProductsAssigned = getArguments().getBoolean("fromProductsAssigned", false);
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
        // Verificar si viene desde ProductsAssignedFragment
        boolean fromProductsAssigned = false;
        if (getArguments() != null) {
            fromProductsAssigned = getArguments().getBoolean("fromProductsAssigned", false);
        }
        
        // Configurar RecyclerView de productos en formato lista
        // Si viene desde ProductsAssignedFragment, pasar null como listener para ocultar el botón
        if (fromProductsAssigned) {
            productsAdapter = new ProductsAdapter(new ArrayList<>(), null, true);
        } else {
            productsAdapter = new ProductsAdapter(new ArrayList<>(), this::addToCart, true);
        }
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
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
                    currentView = 1; // Cambiar a solo carrito
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
        String url = Utilities.URL + "products?client_id=" + clientId;

        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                com.android.volley.Request.Method.GET,
                url,
                null,
                response -> {



                    try {
                        org.json.JSONArray dataArray = response.getJSONArray("data");
                        for (int i = 0; i < dataArray.length(); i++) {
                            org.json.JSONObject obj = dataArray.getJSONObject(i);
                            // Procesar los campos según el nuevo formato
                            String id = obj.optString("id", "");
                            String name = obj.optString("name", "");
                            String code = obj.optString("code", "");
                            String description = obj.optString("description", "Sin descripción");
                            
                            // Obtener la cantidad asignada del objeto quantity
                            org.json.JSONObject quantityObj = obj.optJSONObject("quantity");
                            int quantity = 0;
                            if (quantityObj != null) {
                                quantity = quantityObj.optInt("assigned", 0);
                            }
                            
                            // Obtener el stock disponible
                            int stock = quantityObj.optInt("available", 0);

                            String unit = obj.optString("unit", "Unidad");
                            String unitAbbreviation = obj.optString("unit_abbreviation", "u");
                            double price = obj.optDouble("price", 0.0);
                            
                            // Obtener el product_price_id
                            String productPriceId = obj.optString("product_price_id", "");
                            
                            // Crear el objeto Product con los campos del nuevo formato
                            allProducts.add(new Product(
                                    id,
                                    name,
                                    code,
                                    description + " (" + unit + ")",
                                    "", // categoryId (no viene en el nuevo formato)
                                    "", // categoryName (no viene en el nuevo formato)
                                    price,
                                    "", // imageUrl (no necesario según requerimiento)
                                    quantity,
                                    stock,
                                    0,
                                    productPriceId
                            ));
                        }
                        productsAdapter.updateProducts(allProducts);
                    } catch (org.json.JSONException e) {
                        Toast.makeText(getContext(), ""+e.toString() + " "+clientId, Toast.LENGTH_SHORT).show();

                        e.printStackTrace();
                    }
                },
                error -> {

                    Toast.makeText(getContext(), ""+error.toString() + " "+clientId, Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                android.content.Context context = requireContext();
                headers.put("Authorization", com.jugos_jaco_app.ui.clients.ClientsFragment.getAuthorizationHeader(context));
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        com.jugos_jaco_app.ui.utilities.VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
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
        double total = calculateTotalAmount();
        tvTotal.setText(String.format("Total: L. %.2f", total));
    }
    
    /**
     * Calcula el total de la venta.
     * @return El total calculado
     */
    private double calculateTotalAmount() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getQuantity() * item.getProduct().getPrice();
        }
        return total;
    }
    
    /**
     * Método para finalizar la venta.
     * Muestra un diálogo para confirmar el pago y envía los datos al servidor.
     */
    private void finishSale() {
        if (cartItems.isEmpty()) {
            Toast.makeText(requireContext(), "El carrito está vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calcular el total de la venta
        final double totalAmount = calculateTotalAmount();

        // Inflar el layout del diálogo
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_payment, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Finalizar Venta");
        builder.setView(dialogView);

        // Obtener referencias a las vistas del diálogo
        TextView tvTotalAmount = dialogView.findViewById(R.id.tvTotalAmount);
        RadioGroup rgPaymentMethod = dialogView.findViewById(R.id.rgPaymentMethod);
        RadioGroup rgPaymentTerm = dialogView.findViewById(R.id.rgPaymentTerm);
        TextInputLayout tilCashAmount = dialogView.findViewById(R.id.tilCashAmount);
        TextInputEditText etCashAmount = dialogView.findViewById(R.id.etCashAmount);
        TextInputLayout tilPaymentReference = dialogView.findViewById(R.id.tilPaymentReference);
        TextInputEditText etPaymentReference = dialogView.findViewById(R.id.etPaymentReference);
        TextView tvChange = dialogView.findViewById(R.id.tvChange);
        TextInputLayout tilNotes = dialogView.findViewById(R.id.tilNotes);
        TextInputEditText etNotes = dialogView.findViewById(R.id.etNotes);

        // Configurar el total
        tvTotalAmount.setText(String.format("Total: L. %.2f", totalAmount));

        // Configurar listeners para los radio buttons
        rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCash) {
                tilCashAmount.setVisibility(View.VISIBLE);
                tilPaymentReference.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbDeposit) {
                tilCashAmount.setVisibility(View.GONE);
                tilPaymentReference.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rbCredit) {
                tilCashAmount.setVisibility(View.GONE);
                tilPaymentReference.setVisibility(View.GONE);
            }
        });

        // Configurar listener para el campo de monto en efectivo
        etCashAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    try {
                        double cashAmount = Double.parseDouble(s.toString());
                        if (cashAmount >= totalAmount) {
                            double change = cashAmount - totalAmount;
                            tvChange.setText(String.format("Cambio: L. %.2f", change));
                            tvChange.setVisibility(View.VISIBLE);
                        } else {
                            tvChange.setVisibility(View.GONE);
                        }
                    } catch (NumberFormatException e) {
                        tvChange.setVisibility(View.GONE);
                    }
                } else {
                    tvChange.setVisibility(View.GONE);
                }
            }
        });

        // Configurar botones del diálogo
        builder.setPositiveButton("Confirmar", null); // Se sobrescribirá después
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Sobrescribir el botón positivo para evitar que se cierre automáticamente si hay errores
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // Validar el método de pago seleccionado
            int paymentMethodId = rgPaymentMethod.getCheckedRadioButtonId();
            int paymentTermId = rgPaymentTerm.getCheckedRadioButtonId();
            String paymentMethod = "";
            String paymentTerm = "";
            double cashAmount = 0;
            String paymentReference = "";
            String notes = etNotes.getText().toString().trim();

            // Determinar el método de pago
            if (paymentMethodId == R.id.rbCash) {
                paymentMethod = "cash";
                String cashAmountStr = etCashAmount.getText().toString().trim();
                if (cashAmountStr.isEmpty()) {
                    tilCashAmount.setError("Ingrese el monto recibido");
                    return;
                }
                try {
                    cashAmount = Double.parseDouble(cashAmountStr);
                    if (cashAmount < totalAmount) {
                        tilCashAmount.setError("El monto debe ser igual o mayor al total");
                        return;
                    }
                } catch (NumberFormatException e) {
                    tilCashAmount.setError("Monto inválido");
                    return;
                }
            } else if (paymentMethodId == R.id.rbDeposit) {
                paymentMethod = "deposit";
                paymentReference = etPaymentReference.getText().toString().trim();
                if (paymentReference.isEmpty()) {
                    tilPaymentReference.setError("Ingrese la referencia del depósito");
                    return;
                }
            } else if (paymentMethodId == R.id.rbCredit) {
                paymentMethod = "credit";
            }

            // Determinar el plazo de pago
            if (paymentTermId == R.id.rbCashTerm) {
                paymentTerm = "cash";
            } else if (paymentTermId == R.id.rbCreditTerm) {
                paymentTerm = "credit";
            }

            // Preparar los datos para enviar al servidor
            sendSaleData(paymentMethod, paymentTerm, cashAmount, paymentReference, notes,clientId);
            dialog.dismiss();
        });
    }

    /**
     * Muestra un AlertDialog con el error del servidor.
     */
    private void showErrorDialog(String errorMessage) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Error al crear la venta")
                .setMessage(errorMessage)
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Envía los datos de la venta al servidor.
     */
    private void sendSaleData(String paymentMethod, String paymentTerm, double cashAmount, String paymentReference, String notes, String clientId) {
        // Obtener el ID del cliente de los argumentos

        // Obtener el ID del empleado desde las preferencias compartidas
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String employeeId = sharedPreferences.getString("id_empleado","");

        // Crear la lista de productos para enviar
        JSONArray productsArray = new JSONArray();
        try {
            for (CartItem item : cartItems) {
                JSONObject productObject = new JSONObject();
                productObject.put("product_id", item.getProduct().getId());
                productObject.put("quantity", item.getQuantity());
                // Usar el product_price_id guardado en el objeto Product
                productObject.put("product_price_id", item.getProduct().getProductPriceId());
                productsArray.put(productObject);
            }

            // Crear el objeto JSON principal
            JSONObject saleObject = new JSONObject();
            saleObject.put("client_id", clientId);
            saleObject.put("employee_id", employeeId);
            saleObject.put("payment_method", paymentMethod);
            saleObject.put("payment_term", paymentTerm);
            saleObject.put("cash_amount", cashAmount);
            saleObject.put("payment_reference", paymentReference.isEmpty() ? JSONObject.NULL : paymentReference);
            saleObject.put("notes", notes.isEmpty() ? JSONObject.NULL : notes);
            saleObject.put("products", productsArray);


            // Enviar los datos al servidor
            String url = Utilities.URL + "sales";
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    saleObject,
                    response -> {
                        // Venta exitosa
                        Toast.makeText(requireContext(), "Venta realizada con éxito", Toast.LENGTH_SHORT).show();
                        // Limpiar el carrito
                        cartItems.clear();
                        cartAdapter.notifyDataSetChanged();
                        updateTotal();
                        // Actualizar el estado de los productos
                        for (Product product : allProducts) {
                            productsAdapter.setProductInCart(product.getId(), false);
                        }
                    },
                    error -> {
                        try {
                            String errorMessage = new String(error.networkResponse.data);
                            JSONObject errorResponse = new JSONObject(errorMessage);
                            
                            String displayMessage = "Error inesperado";
                            
                            // Verificar si hay un mensaje específico del servidor
                            if(errorResponse.has("error")){
                                displayMessage = errorResponse.getString("error");
                            } else if(errorResponse.has("error")) {
                                displayMessage = errorResponse.getString("error");
                            }
                            
                            Log.d("CREARVENTA", "sendSaleData: " + displayMessage);
                            showErrorDialog(displayMessage);
                            
                        } catch (Exception e) {
                            e.printStackTrace();
                            showErrorDialog("Error: " + error.getMessage());
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", ClientsFragment.getAuthorizationHeader(requireContext()));
                    headers.put("Accept", "application/json");
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
            
            // Agregar la solicitud a la cola
            Volley.newRequestQueue(requireContext()).add(request);

        } catch (JSONException e) {
            Toast.makeText(requireContext(), "Error al preparar los datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
            currentView = 1; // Cambiar a solo carrito
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