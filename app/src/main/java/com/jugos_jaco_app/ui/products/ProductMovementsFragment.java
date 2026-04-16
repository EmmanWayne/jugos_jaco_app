package com.jugos_jaco_app.ui.products;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.adapters.ProductMovementsAdapter;
import com.jugos_jaco_app.ui.models.ProductMovement;
import com.jugos_jaco_app.ui.utilities.Utilities;
import com.jugos_jaco_app.ui.utilities.VolleySingleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductMovementsFragment extends Fragment {

    private RecyclerView rvProductMovements;
    private ProductMovementsAdapter adapter;
    private TextView tvEmptyState;
    private TextView tvTotalRoyalties;
    private TextView tvTotalChanges;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddMovement;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_movements, container, false);

        rvProductMovements = view.findViewById(R.id.rvProductMovements);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvTotalRoyalties = view.findViewById(R.id.tvTotalRoyalties);
        tvTotalChanges = view.findViewById(R.id.tvTotalChanges);
        progressBar = view.findViewById(R.id.progressBar);
        fabAddMovement = view.findViewById(R.id.fabAddMovement);

        setupRecyclerView();
        setupFab();
        loadMovements();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new ProductMovementsAdapter();
        rvProductMovements.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProductMovements.setAdapter(adapter);
    }

    private void setupFab() {
        fabAddMovement.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_productMovementsFragment_to_createProductMovementFragment);
        });
    }

    private void loadMovements() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
        rvProductMovements.setVisibility(View.GONE);

        String url = Utilities.URL + "product-movements/";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        List<ProductMovement> movements = parseMovements(response);
                        if (movements.isEmpty()) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            rvProductMovements.setVisibility(View.GONE);
                            updateTotals(new ArrayList<>());
                        } else {
                            tvEmptyState.setVisibility(View.GONE);
                            rvProductMovements.setVisibility(View.VISIBLE);
                            adapter.setMovements(movements);
                            updateTotals(movements);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error al procesar datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    error.printStackTrace();
                    Toast.makeText(getContext(), "Error al cargar movimientos", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return Utilities.getAuthHeaders(requireContext());
            }
        };

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    private List<ProductMovement> parseMovements(JSONObject response) throws JSONException {
        List<ProductMovement> list = new ArrayList<>();
        JSONArray data = response.getJSONArray("data");

        for (int i = 0; i < data.length(); i++) {
            JSONObject obj = data.getJSONObject(i);
            int id = obj.optInt("id", -1);
            int productId = obj.optInt("product_id", -1);
            String productName = obj.optString("product_name", "Desconocido");
            String type = obj.optString("type", "");
            if (type.equals("change")) {
                type = "Cambio";
            } else if (type.equals("royalty")) {
                type = "Regalía";
            }
            int quantity = obj.optInt("quantity", 0);
            String note = obj.optString("note", "");

            list.add(new ProductMovement(id, productId, productName, type, quantity, note));
        }
        return list;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMovements(); // Recargar al volver de la pantalla de creación
    }

    private void updateTotals(List<ProductMovement> movements) {
        int totalChanges = 0;
        int totalRoyalties = 0;

        for (ProductMovement movement : movements) {
            if ("Cambio".equals(movement.getType())) {
                totalChanges += movement.getQuantity();
            } else if ("Regalía".equals(movement.getType())) {
                totalRoyalties += movement.getQuantity();
            }
        }

        if (tvTotalChanges != null) {
            tvTotalChanges.setText(String.valueOf(totalChanges));
        }
        if (tvTotalRoyalties != null) {
            tvTotalRoyalties.setText(String.valueOf(totalRoyalties));
        }
    }
}
