package com.jugos_jaco_app.ui.products;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.textfield.TextInputEditText;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.utilities.Utilities;
import com.jugos_jaco_app.ui.utilities.VolleySingleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateProductMovementFragment extends Fragment {

    private AutoCompleteTextView actvProduct;
    private AutoCompleteTextView actvType;
    private TextInputEditText etQuantity;
    private TextInputEditText etNote;
    private Button btnSave;
    private ProgressBar progressBar;

    private List<ProductSelectionItem> productList = new ArrayList<>();
    private ArrayAdapter<ProductSelectionItem> productAdapter;
    private ProductSelectionItem selectedProduct;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_product_movement, container, false);

        actvProduct = view.findViewById(R.id.actvProduct);
        actvType = view.findViewById(R.id.actvType);
        etQuantity = view.findViewById(R.id.etQuantity);
        etNote = view.findViewById(R.id.etNote);
        btnSave = view.findViewById(R.id.btnSave);
        progressBar = view.findViewById(R.id.progressBar);

        setupTypeSelector();
        loadProducts();
        setupSaveButton();

        return view;
    }

    private void setupTypeSelector() {
        String[] types = new String[]{"Cambio", "Regalía"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, types);
        actvType.setAdapter(adapter);
    }

    private void loadProducts() {
        String url = Utilities.URL + "products/assigned";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        productList.clear();
                        JSONArray data = response.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            // Usamos 'id' como detail_assigned_product_id segun requerimiento
                            int id = obj.optInt("id", -1); 
                            String name = obj.optString("productName", "Sin Nombre");
                            // Si productName viene vacio, intentar concatenar
                            if (name.isEmpty() || name.equals("Sin Nombre")) {
                                 name = obj.optString("name", "Sin Nombre");
                            }
                            
                            if (id != -1) {
                                productList.add(new ProductSelectionItem(id, name));
                            }
                        }
                        setupProductAdapter();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error al cargar productos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(getContext(), "Error de red al cargar productos", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return Utilities.getAuthHeaders(requireContext());
            }
        };

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    private void setupProductAdapter() {
        productAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, productList);
        actvProduct.setAdapter(productAdapter);
        
        actvProduct.setOnItemClickListener((parent, view, position, id) -> {
            selectedProduct = productAdapter.getItem(position);
        });
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                createMovement();
            }
        });
    }

    private boolean validateForm() {
        if (selectedProduct == null) {
            actvProduct.setError("Seleccione un producto");
            return false;
        }
        
        // Verificar que el texto coincida con el seleccionado (para evitar que escriban cualquier cosa)
        String currentText = actvProduct.getText().toString();
        if (!currentText.equals(selectedProduct.toString())) {
             // Intentar buscar de nuevo si el usuario escribio el nombre exacto
             boolean found = false;
             for(ProductSelectionItem item : productList) {
                 if(item.toString().equals(currentText)) {
                     selectedProduct = item;
                     found = true;
                     break;
                 }
             }
             if(!found) {
                 actvProduct.setError("Seleccione un producto válido de la lista");
                 return false;
             }
        }
        actvProduct.setError(null);

        String type = actvType.getText().toString();
        if (type.isEmpty() || (!type.equals("Cambio") && !type.equals("Regalía"))) {
            actvType.setError("Seleccione un tipo válido");
            return false;
        }
        actvType.setError(null);

        String quantityStr = etQuantity.getText().toString();
        if (quantityStr.isEmpty()) {
            etQuantity.setError("Ingrese cantidad");
            return false;
        }
        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                etQuantity.setError("La cantidad debe ser mayor a 0");
                return false;
            }
        } catch (NumberFormatException e) {
            etQuantity.setError("Cantidad inválida");
            return false;
        }
        etQuantity.setError(null);

        return true;
    }

    private void createMovement() {
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        String url = Utilities.URL + "product-movements";
        
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("detail_assigned_product_id", selectedProduct.id);
            
            String selectedType = actvType.getText().toString();
            String typeValue = "";
            if (selectedType.equals("Cambio")) {
                typeValue = "change";
            } else if (selectedType.equals("Regalía")) {
                typeValue = "royalty";
            }
            jsonBody.put("type", typeValue);
            
            jsonBody.put("note", etNote.getText().toString());
            jsonBody.put("quantity", Integer.parseInt(etQuantity.getText().toString()));
        } catch (JSONException e) {
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(getContext(), "Movimiento registrado exitosamente", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(getView()).popBackStack();
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    error.printStackTrace();
                    String errorMsg = "Error al registrar movimiento";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String errorData = new String(error.networkResponse.data);
                            JSONObject errorJson = new JSONObject(errorData);
                            errorMsg = errorJson.optString("message", errorMsg);
                        } catch (Exception e) {}
                    }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return Utilities.getAuthHeaders(requireContext());
            }
        };

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    // Clase interna para el spinner
    private static class ProductSelectionItem {
        int id;
        String name;

        ProductSelectionItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }
}
