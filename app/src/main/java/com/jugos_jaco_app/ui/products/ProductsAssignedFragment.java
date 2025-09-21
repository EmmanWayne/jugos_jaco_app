package com.jugos_jaco_app.ui.products;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.adapters.ProductsAdapter;
import com.jugos_jaco_app.ui.models.Product;
import com.jugos_jaco_app.ui.utilities.Utilities;
import com.jugos_jaco_app.ui.utilities.VolleySingleton;
import java.util.ArrayList;
import java.util.List;

public class ProductsAssignedFragment extends Fragment {
    private androidx.appcompat.widget.SearchView searchView;

    private RecyclerView rvProducts;
    private ProductsAdapter productsAdapter;
    private List<Product> allProducts = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products_assigned, container, false);
        rvProducts = view.findViewById(R.id.rvProductsAssigned);
        productsAdapter = new ProductsAdapter(new ArrayList<>(), null, true);
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(productsAdapter);
        loadProducts();
        // Configurar búsqueda
        setHasOptionsMenu(true);
        return view;
    }
    
    /**
     * Navega a NewSaleFragment cuando se hace clic en un producto
     * Pasa el parámetro fromProductsAssigned=true para indicar que viene desde este fragmento
     */
    private void navigateToNewSale(Product product) {
        Bundle args = new Bundle();
        args.putString("client_id", ""); // No hay cliente seleccionado aún
        args.putString("client_name", "Sin cliente");
        args.putBoolean("fromProductsAssigned", true); // Indicador de origen
        
        Navigation.findNavController(requireView())
                .navigate(R.id.action_productsAssignedFragment_to_newSaleFragment, args);
    }

    private void loadProducts() {
        allProducts = new ArrayList<>();
        String url = Utilities.URL + "products/assigned";
        com.android.volley.toolbox.JsonObjectRequest
        request = new com.android.volley.toolbox.JsonObjectRequest(
                com.android.volley.Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        org.json.JSONArray dataArray = response.getJSONArray("data");
                        for (int i = 0; i < dataArray.length(); i++) {
                            org.json.JSONObject obj = dataArray.getJSONObject(i);
                            String id = obj.optString("id", "");
                            String productId = obj.optString("productId", "");
                            String productName = obj.optString("productName", "");
                            String contentType = obj.optString("content_type", "");
                            String content = obj.optString("content", "");
                            String productCode = obj.optString("productCode", "");
                            int quantity = obj.optInt("quantity", 0);
                            int stock = obj.optInt("stock", 0);
                            allProducts.add(new Product(
                                    productId,
                                    productName,
                                    productCode,
                                    content + " " + contentType,
                                    "",
                                    "",
                                    0.0,
                                    "",
                                    quantity,
                                    stock,
                                    productId
                            ));
                        }
                        productsAdapter.updateProducts(allProducts);
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
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
        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull android.view.Menu menu, @NonNull android.view.MenuInflater inflater) {
        menu.clear(); // Eliminar cualquier menú anterior
        inflater.inflate(R.menu.menu_search, menu); // Usar menú de búsqueda
        android.view.MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setQueryHint("Buscar producto...");
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
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
    }

    // Método para filtrar productos por nombre
    private void filterProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            productsAdapter.updateProducts(allProducts);
            return;
        }
        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            if (p.getName() != null && p.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(p);
            }
        }
        productsAdapter.updateProducts(filtered);
    }
}

