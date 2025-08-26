package com.jugos_jaco_app.ui.sales;

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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.databinding.FragmentSalesBinding;
import com.jugos_jaco_app.ui.adapters.SalesAdapter;
import com.jugos_jaco_app.ui.models.Sale;

import java.util.ArrayList;
import java.util.Locale;

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
            // Navegar a la pantalla de nueva venta
            Navigation.findNavController(v).navigate(R.id.action_nav_ventas_to_newSaleFragment);
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
                salesAdapter.getFilter().filter(s);
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
}