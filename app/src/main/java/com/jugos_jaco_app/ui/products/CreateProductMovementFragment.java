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
import java.util.UUID;

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
    private String pendingMovementUuid = null; // UUID de idempotencia; se conserva entre reintentos del mismo formulario

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
        setupUuidResetOnFormChange();

        return view;
    }

    private void setupTypeSelector() {
        String[] types = new String[]{"Cambio", "Regalía"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, types);
        actvType.setAdapter(adapter);

        actvType.setOnItemClickListener((parent, view, position, id) -> pendingMovementUuid = null);
    }

    /**
     * Si el usuario cambia algún dato del formulario, el UUID pendiente ya no
     * corresponde a esta operación: reutilizarlo haría que el servidor
     * descarte un movimiento distinto por creerlo un reintento del anterior.
     */
    private void setupUuidResetOnFormChange() {
        TextWatcher resetWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pendingMovementUuid = null;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etQuantity.addTextChangedListener(resetWatcher);
        etNote.addTextChangedListener(resetWatcher);
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
            pendingMovementUuid = null;
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

        // UUID de idempotencia: se genera una sola vez por formulario y se
        // reutiliza en los reintentos, para que el servidor descarte
        // movimientos duplicados (ver client_request_uuid en el backend).
        if (pendingMovementUuid == null) {
            pendingMovementUuid = UUID.randomUUID().toString();
        }

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
            jsonBody.put("client_request_uuid", pendingMovementUuid);
        } catch (JSONException e) {
            e.printStackTrace();
            // Liberar el guard: de lo contrario el botón queda bloqueado permanentemente
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
                    pendingMovementUuid = null; // Movimiento registrado: el UUID ya no debe reutilizarse
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Movimiento registrado exitosamente", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(getView()).popBackStack();
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    if (!isAdded()) return;
                    error.printStackTrace();
                    String errorMsg;
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        errorMsg = "Error al registrar movimiento";
                        try {
                            String errorData = new String(error.networkResponse.data);
                            JSONObject errorJson = new JSONObject(errorData);
                            errorMsg = errorJson.optString("message", errorMsg);
                        } catch (Exception e) {}
                    } else {
                        // Sin respuesta del servidor (timeout / sin conexión): el
                        // movimiento pudo haberse registrado; al reintentar se
                        // reutiliza el mismo UUID y el servidor lo descarta si ya existe.
                        errorMsg = "No se pudo confirmar el movimiento por problemas de conexión. " +
                                "Verifique su señal y presione Guardar de nuevo: " +
                                "el sistema evitará que se duplique.";
                    }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return Utilities.getAuthHeaders(requireContext());
            }
        };

        // Sin reintentos automáticos: un POST reintentado por Volley duplica el
        // movimiento. Timeout amplio (30s) para conexiones lentas; los reintentos
        // son manuales y quedan protegidos por el client_request_uuid.
        request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                30000, // 30 segundos de timeout
                0,     // 0 reintentos automáticos
                1.0f
        ));

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
