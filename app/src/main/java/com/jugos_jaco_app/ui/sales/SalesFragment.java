package com.jugos_jaco_app.ui.sales;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.databinding.FragmentSalesBinding;
import com.jugos_jaco_app.ui.adapters.SalesAdapter;
import com.jugos_jaco_app.ui.clients.ClientsFragment;
import com.jugos_jaco_app.ui.models.Client;
import com.jugos_jaco_app.ui.models.Sale;
import com.jugos_jaco_app.ui.utilities.Utilities;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment para mostrar la lista de ventas.
 */
public class SalesFragment extends Fragment implements SalesAdapter.OnSaleClickListener {

    private FragmentSalesBinding binding;
    private SaleViewModel saleViewModel;
    private SalesAdapter salesAdapter;
    
    // Vistas
    private RecyclerView rvSales;
    private TextView tvCashSales, tvCreditSales, tvTotalSales;
    private TextView tvNoSales;
    private ProgressBar progressBar;
    private ExtendedFloatingActionButton fabNewSale;
    private EditText etSearchClient;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inicializar ViewModel
        saleViewModel = new ViewModelProvider(this).get(SaleViewModel.class);

        // Inflar layout
        binding = FragmentSalesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar vistas
        initializeViews(root);
        setupRecyclerView();
        setupObservers();
        setupListeners();
        
        // Cargar datos
        loadSales();

        return root;
    }

    /**
     * Inicializa las vistas del fragment.
     */
    private void initializeViews(View view) {
        rvSales = view.findViewById(R.id.rvSales);
        tvCashSales = view.findViewById(R.id.tvCashSales);
        tvCreditSales = view.findViewById(R.id.tvCreditSales);
        tvTotalSales = view.findViewById(R.id.tvTotalSales);
        tvNoSales = view.findViewById(R.id.tvNoSales);
        progressBar = view.findViewById(R.id.progressBar);
        fabNewSale = view.findViewById(R.id.fabNewSale);
        etSearchClient = view.findViewById(R.id.etSearchClient);
    }

    /**
     * Configura el RecyclerView para mostrar las ventas.
     */
    private void setupRecyclerView() {
        salesAdapter = new SalesAdapter(new ArrayList<>(), this);
        rvSales.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSales.setAdapter(salesAdapter);
    }

    /**
     * Configura los observadores para los LiveData del ViewModel.
     */
    private void setupObservers() {
        // Observar lista de ventas
        saleViewModel.getSalesList().observe(getViewLifecycleOwner(), sales -> {
            salesAdapter.updateSales(sales);
            
            // Mostrar mensaje si no hay ventas
            if (sales.isEmpty()) {
                tvNoSales.setVisibility(View.VISIBLE);
                rvSales.setVisibility(View.GONE);
            } else {
                tvNoSales.setVisibility(View.GONE);
                rvSales.setVisibility(View.VISIBLE);
            }
        });

        // Observar estado de carga
        saleViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observar mensajes de error
        saleViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
        });

        // Observar totales
        saleViewModel.getCashSalesTotal().observe(getViewLifecycleOwner(), total -> {
            tvCashSales.setText(String.format(Locale.getDefault(), "L. %.2f", total));
        });

        saleViewModel.getCreditSalesTotal().observe(getViewLifecycleOwner(), total -> {
            tvCreditSales.setText(String.format(Locale.getDefault(), "L. %.2f", total));
        });

        saleViewModel.getTotalSales().observe(getViewLifecycleOwner(), total -> {
            tvTotalSales.setText(String.format(Locale.getDefault(), "L. %.2f", total));
        });
    }

    /**
     * Configura los listeners para los componentes interactivos.
     */
    private void setupListeners() {
        // Listener para el botón de nueva venta
        fabNewSale.setOnClickListener(v -> {
            showClientSelectionDialog();
        });
        
        // Listener para el campo de búsqueda
        etSearchClient.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se necesita implementación
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filtrar la lista de ventas por nombre de cliente
                if (salesAdapter != null) {
                    salesAdapter.getFilter().filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No se necesita implementación
            }
        });
    }

    /**
     * Carga las ventas desde el servidor.
     */
    private void loadSales() {
        if (getContext() != null) {
            saleViewModel.loadSales(getContext());
        }
    }

    /**
     * Maneja el clic en una venta.
     */
    @Override
    public void onSaleClick(Sale sale) {
        // Navegar al fragmento de detalles de venta
        Bundle bundle = new Bundle();
        bundle.putSerializable("sale", sale);
        Navigation.findNavController(getView()).navigate(R.id.action_nav_ventas_to_saleDetailFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    
    /**
     * Muestra un diálogo para seleccionar un cliente antes de crear una nueva venta
     */
    private void showClientSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Seleccionar Cliente");
        
        // Mostrar un diálogo de progreso mientras se cargan los clientes
        AlertDialog progressDialog = new AlertDialog.Builder(requireContext())
                .setMessage("Cargando clientes...")
                .setCancelable(false)
                .create();
        progressDialog.show();
        
        // Cargar la lista de clientes desde el servidor
        String url = Utilities.URL + "clients";
        
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressDialog.dismiss();
                    try {
                        List<Client> clientList = new ArrayList<>();
                        JSONArray clientsJson = response.getJSONArray("data");
                        
                        for (int i = 0; i < clientsJson.length(); i++) {
                            org.json.JSONObject clientJson = clientsJson.getJSONObject(i);
                            String id = String.valueOf(clientJson.getInt("id"));
                            String firstName = clientJson.getString("first_name");
                            String lastName = clientJson.getString("last_name");
                            String businessName = clientJson.getString("business_name");
                            
                            // Crear un objeto Client con los datos mínimos necesarios
                            Client client = new Client(
                                    id, firstName, lastName, "", "", "", "", "", "", "", "", 
                                    businessName, "", "", ""
                            );
                            clientList.add(client);
                        }
                        
                        // Crear array de nombres para mostrar en el diálogo
                        String[] clientNames = new String[clientList.size()];
                        for (int i = 0; i < clientList.size(); i++) {
                            Client client = clientList.get(i);
                            String displayName = client.getFirstName() + " " + client.getLastName();
                            if (client.getBusinessName() != null && !client.getBusinessName().isEmpty()) {
                                displayName += " - " + client.getBusinessName();
                            }
                            clientNames[i] = displayName;
                        }
                        
                        // Mostrar el diálogo con la lista de clientes
                        builder.setItems(clientNames, (dialog, which) -> {
                            Client selectedClient = clientList.get(which);
                            // Navegar a NewSaleFragment con el cliente seleccionado
                            Bundle bundle = new Bundle();
                            bundle.putString("clientId", selectedClient.getId());
                            bundle.putString("clientName", selectedClient.getFirstName() + " " + selectedClient.getLastName());
                            Navigation.findNavController(getView()).navigate(R.id.action_nav_ventas_to_newSaleFragment, bundle);
                        });
                        
                        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
                        builder.show();
                        
                    } catch (JSONException e) {
                        Toast.makeText(requireContext(), "Error al procesar los datos de clientes", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Error al cargar los clientes", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", ClientsFragment.getAuthorizationHeader(requireContext()));
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        
        Volley.newRequestQueue(requireContext()).add(request);
    }
}