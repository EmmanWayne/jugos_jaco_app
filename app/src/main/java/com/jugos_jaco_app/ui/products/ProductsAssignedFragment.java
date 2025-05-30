package com.jugos_jaco_app.ui.products;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.adapters.ProductsAdapter;
import com.jugos_jaco_app.ui.models.Product;
import com.jugos_jaco_app.ui.utilities.Utilities;
import com.jugos_jaco_app.ui.utilities.VolleySingleton;
import java.util.ArrayList;
import java.util.List;

public class ProductsAssignedFragment extends Fragment {
    private RecyclerView rvProducts;
    private ProductsAdapter productsAdapter;
    private List<Product> allProducts = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products_assigned, container, false);
        rvProducts = view.findViewById(R.id.rvProductsAssigned);
        productsAdapter = new ProductsAdapter(new ArrayList<>(), product -> {}, true);
        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProducts.setAdapter(productsAdapter);
        loadProducts();
        return view;
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
                            allProducts.add(new Product(
                                    productId,
                                    productName,
                                    productCode,
                                    content + " " + contentType,
                                    "",
                                    "",
                                    0.0,
                                    "",
                                    quantity
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
}
