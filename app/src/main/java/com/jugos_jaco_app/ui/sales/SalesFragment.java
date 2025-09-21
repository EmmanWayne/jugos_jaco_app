package com.jugos_jaco_app.ui.sales;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;

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
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    
    // Variables para almacenar los totales
    private double currentCashSales = 0.0;
    private double currentCreditSales = 0.0;
    private double currentTotalPayments = 0.0;
    
    // Vistas
    private RecyclerView rvSales;
    private TextView tvCashSales, tvCreditSales, tvTotalSales, tvNoSales, tvTotalPayments, tvTotalExpected;
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
        tvTotalPayments = view.findViewById(R.id.tvTotalPayments);
        tvTotalExpected = view.findViewById(R.id.tvTotalExpected);
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
            currentCashSales = total;
            tvCashSales.setText(String.format(Locale.getDefault(), "L. %.2f", total));
            updateCalculatedTotals();
        });

        saleViewModel.getCreditSalesTotal().observe(getViewLifecycleOwner(), total -> {
            currentCreditSales = total;
            tvCreditSales.setText(String.format(Locale.getDefault(), "L. %.2f", total));
            updateCalculatedTotals();
        });

        saleViewModel.getTotalSales().observe(getViewLifecycleOwner(), total -> {
            // El total acumulado ahora incluye abonos, se calcula en updateCalculatedTotals()
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
            loadTodayPayments(); // Cargar también los pagos del día
        }
    }

    /**
     * Carga los pagos del día actual desde el servidor.
     */
    private void loadTodayPayments() {
        // Obtener la fecha actual en formato yyyy-MM-dd
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = dateFormat.format(new Date());
        
        String url = Utilities.URL + "account-receivable/payments?date=" + todayDate;
        
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONArray dataArray = response.getJSONArray("data");
                        double totalPayments = 0.0;
                        
                        // Sumar todos los pagos del día
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject payment = dataArray.getJSONObject(i);
                            double amount = payment.getDouble("amount");
                            totalPayments += amount;
                        }
                        
                        // Actualizar la vista con el total
                        currentTotalPayments = totalPayments;
                        tvTotalPayments.setText(String.format(Locale.getDefault(), "L. %.2f", totalPayments));
                        updateCalculatedTotals();
                        
                    } catch (JSONException e) {
                        e.printStackTrace();
                        tvTotalPayments.setText("L. 0.00");
                    }
                },
                error -> {
                    // En caso de error, mostrar 0.00
                    tvTotalPayments.setText("L. 0.00");
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
        
        // Agregar la solicitud a la cola
        Volley.newRequestQueue(requireContext()).add(request);
    }

    /**
     * Actualiza los totales calculados (Total Esperado y Total Acumulado).
     */
    private void updateCalculatedTotals() {
        // Total Esperado = Ventas al Contado + Abonos del Día
        double totalExpected = currentCashSales + currentTotalPayments;
        tvTotalExpected.setText(String.format(Locale.getDefault(), "L. %.2f", totalExpected));
        
        // Total Acumulado = Ventas al Contado + Ventas al Crédito + Abonos del Día
        double totalAccumulated = currentCashSales + currentCreditSales + currentTotalPayments;
        tvTotalSales.setText(String.format(Locale.getDefault(), "L. %.2f", totalAccumulated));
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
     * Actualiza el adaptador con la lista filtrada de clientes
     * @param adapter El adaptador a actualizar
     * @param clientList La lista de clientes filtrada
     */
    private void updateClientAdapter(ArrayAdapter<String> adapter, List<Client> clientList) {
        adapter.clear();
        for (Client client : clientList) {
            String displayName = client.getFirstName() + " " + client.getLastName();
            if (client.getBusinessName() != null && !client.getBusinessName().isEmpty()) {
                displayName += " - " + client.getBusinessName();
            }
            adapter.add(displayName);
        }
        adapter.notifyDataSetChanged();
    }
    
    /**
     * Muestra un diálogo para seleccionar un cliente antes de crear una nueva venta
     * con un campo de búsqueda para filtrar la lista de clientes
     */
    private void showClientSelectionDialog() {
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
                        List<Client> filteredClientList = new ArrayList<>();
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
                                    businessName, "", "", "", 0, 0.0
                            );
                            clientList.add(client);
                            filteredClientList.add(client);
                        }
                        
                        // Crear un layout personalizado para el diálogo con un campo de búsqueda
                        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_client_search, null);
                        EditText searchEditText = dialogView.findViewById(R.id.etSearchClient);
                        ListView clientListView = dialogView.findViewById(R.id.lvClients);
                        
                        // Crear un adaptador para la lista de clientes
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), 
                                android.R.layout.simple_list_item_1);
                        clientListView.setAdapter(adapter);
                        
                        // Actualizar el adaptador con la lista de clientes
                        updateClientAdapter(adapter, filteredClientList);
                        
                        // Configurar el listener para el campo de búsqueda
                        searchEditText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                // No se necesita implementación
                            }
                            
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                // Filtrar la lista de clientes según el texto de búsqueda
                                String searchText = s.toString().toLowerCase();
                                filteredClientList.clear();
                                
                                for (Client client : clientList) {
                                    String fullName = client.getFirstName().toLowerCase() + " " + client.getLastName().toLowerCase();
                                    String business = client.getBusinessName() != null ? client.getBusinessName().toLowerCase() : "";
                                    
                                    if (fullName.contains(searchText) || business.contains(searchText)) {
                                        filteredClientList.add(client);
                                    }
                                }
                                
                                // Actualizar el adaptador con la lista filtrada
                                updateClientAdapter(adapter, filteredClientList);
                            }
                            
                            @Override
                            public void afterTextChanged(Editable s) {
                                // No se necesita implementación
                            }
                        });
                        
                        // Crear el diálogo con el layout personalizado
                        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                                .setTitle("Seleccionar Cliente")
                                .setView(dialogView)
                                .setNegativeButton("Cancelar", (dialogInterface, which) -> dialogInterface.dismiss())
                                .create();
                        
                        // Configurar el listener para la selección de un cliente
                        clientListView.setOnItemClickListener((parent, view, position, id) -> {
                            Client selectedClient = filteredClientList.get(position);
                            // Navegar a NewSaleFragment con el cliente seleccionado
                            Bundle bundle = new Bundle();
                            bundle.putString("clientId", selectedClient.getId());
                            bundle.putString("clientName", selectedClient.getFirstName() + " " + selectedClient.getLastName());
                            // Cerrar el diálogo antes de navegar
                            dialog.dismiss();
                            Navigation.findNavController(getView()).navigate(R.id.action_nav_ventas_to_newSaleFragment, bundle);
                        });
                        
                        // Mostrar el diálogo
                        dialog.show();
                        
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