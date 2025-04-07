package com.jugos_jaco_app.ui.clients;

import static com.jugos_jaco_app.Login.KEY_TOKEN;
import static com.jugos_jaco_app.Login.PREFS_NAME;
import static com.jugos_jaco_app.Login.TOKEN_TYPE;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.adapters.ClientAdapter;
import com.jugos_jaco_app.ui.utilities.Utilities;

import java.util.ArrayList;
import java.util.Calendar;

public class ClientsFragment extends Fragment {

    private ClientsViewModel viewModel;
    private RecyclerView recyclerView;
    private ClientAdapter clientAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final String URL_CLIENTS = Utilities.URL +"clients/"; // Reemplaza con tu URL real

    private ActivityResultLauncher<Intent> locationSettingsLauncher;
    private final ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        showBackgroundLocationDialog();
                    }
                } else {
                    Toast.makeText(requireContext(),
                            "Se necesita el permiso de ubicación para esta función",
                            Toast.LENGTH_LONG).show();
                }
            }
    );

    private View loadingOverlay;
    private ChipGroup chipGroupDays;
    private Chip chipMonday, chipTuesday, chipWednesday, chipThursday, chipFriday, chipSaturday;
    private MaterialCardView cardDaySelector;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = new ViewModelProvider(requireActivity()).get(ClientsViewModel.class);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_select_day) {
            if (cardDaySelector.getVisibility() == View.VISIBLE) {
                cardDaySelector.setVisibility(View.GONE);
            } else {
                cardDaySelector.setVisibility(View.VISIBLE);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_clients, container, false);

        // Inicializar el launcher para la configuración de ubicación
        locationSettingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> checkAndRequestLocationPermissions()
        );

        // Verificar GPS y permisos
        checkAndRequestLocationPermissions();

        // Inicializar vistas
        setupViews(root);

        // Inicializar overlay de carga
        loadingOverlay = root.findViewById(R.id.loadingView);

        // Observar cambios en la lista de clientes
        viewModel.getClients().observe(getViewLifecycleOwner(), clients -> {
            clientAdapter.updateList(clients);
            swipeRefreshLayout.setRefreshing(false);
            hideLoading(); // Ocultar loading cuando se cargan los datos
        });

        // Observar mensajes de error
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
                hideLoading(); // Ocultar loading en caso de error
            }
        });

        // Mostrar loading al cargar clientes
        if (viewModel.getClients().getValue() == null || viewModel.getClients().getValue().isEmpty()) {
            showLoading();
        }

        // Cargar clientes solo si es necesario
        viewModel.loadClientsIfNeeded(requireContext());

        // Configurar listeners
        setupListeners();

        // Establecer día actual por defecto
        setCurrentDay();

        return root;
    }

    private void setupViews(View root) {
        recyclerView = root.findViewById(R.id.recyclerView);
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);

        // Configurar RecyclerView
        clientAdapter = new ClientAdapter(new ArrayList<>(), requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(clientAdapter);

        // Configurar SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.forceLoadClients(requireContext());
        });

        // Configurar FAB
        FloatingActionButton fabCrearCliente = root.findViewById(R.id.fab_crear_cliente);
        fabCrearCliente.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.newClientFragment);
        });

        // Inicializar chips
        cardDaySelector = root.findViewById(R.id.cardDaySelector);
        chipGroupDays = root.findViewById(R.id.chipGroupDays);
        chipMonday = root.findViewById(R.id.chipMonday);
        chipTuesday = root.findViewById(R.id.chipTuesday);
        chipWednesday = root.findViewById(R.id.chipWednesday);
        chipThursday = root.findViewById(R.id.chipThursday);
        chipFriday = root.findViewById(R.id.chipFriday);
        chipSaturday = root.findViewById(R.id.chipSaturday);
    }

    private void setupListeners() {
        // Configurar listeners para los chips
        chipGroupDays.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedDay = "";
            if (checkedId == R.id.chipMonday) {
                selectedDay = "lunes";
            } else if (checkedId == R.id.chipTuesday) {
                selectedDay = "martes";
            } else if (checkedId == R.id.chipWednesday) {
                selectedDay = "miercoles";
            } else if (checkedId == R.id.chipThursday) {
                selectedDay = "jueves";
            } else if (checkedId == R.id.chipFriday) {
                selectedDay = "viernes";
            } else if (checkedId == R.id.chipSaturday) {
                selectedDay = "sabado";
            }
            viewModel.setSelectedDay(selectedDay);
            viewModel.loadClients(requireContext());
            cardDaySelector.setVisibility(View.GONE);
        });
    }

    private void setCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String currentDay = "";

        switch (dayOfWeek) {
            case Calendar.MONDAY:
                currentDay = "lunes";
                chipMonday.setChecked(true);
                break;
            case Calendar.TUESDAY:
                currentDay = "martes";
                chipTuesday.setChecked(true);
                break;
            case Calendar.WEDNESDAY:
                currentDay = "miercoles";
                chipWednesday.setChecked(true);
                break;
            case Calendar.THURSDAY:
                currentDay = "jueves";
                chipThursday.setChecked(true);
                break;
            case Calendar.FRIDAY:
                currentDay = "viernes";
                chipFriday.setChecked(true);
                break;
            case Calendar.SATURDAY:
                currentDay = "sabado";
                chipSaturday.setChecked(true);
                break;
            default:
                // Si es domingo, mostrar lunes
                currentDay = "lunes";
                chipMonday.setChecked(true);
                break;
        }

        viewModel.setSelectedDay(currentDay);
        viewModel.loadClients(requireContext());
    }

    private void checkGPSEnabled() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("GPS Desactivado")
                    .setMessage("Esta aplicación requiere el GPS para funcionar correctamente. ¿Desea activarlo?")
                    .setCancelable(false)
                    .setPositiveButton("Activar GPS", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        locationSettingsLauncher.launch(intent);
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        Toast.makeText(requireContext(),
                                "El GPS es necesario para usar la aplicación",
                                Toast.LENGTH_LONG).show();
                        requireActivity().finish();
                    })
                    .create()
                    .show();
        } else {
            checkAndRequestLocationPermissions();
        }
    }

    private void checkAndRequestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solo mostrar el diálogo si no tiene el permiso de ubicación en segundo plano
            showBackgroundLocationDialog();
        }
        // Si ya tiene todos los permisos, no hacer nada
    }

    private void showBackgroundLocationDialog() {
        // Verificar primero si ya tiene el permiso
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return; // Si ya tiene el permiso, no mostrar el diálogo
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Permiso de ubicación")
                .setMessage("Para un mejor seguimiento de rutas, necesitamos acceder a tu ubicación todo el tiempo. ¿Deseas permitirlo?")
                .setPositiveButton("Configurar", (dialog, which) -> {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);

                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }

    public void filterClients(String query) {
        viewModel.filterClients(query);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Ya no necesitamos cancelar las solicitudes aquí porque 
        // VolleySingleton maneja el ciclo de vida de las solicitudes
    }

    public static String getAuthorizationHeader(Context context) {


        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String token = sharedPreferences.getString(KEY_TOKEN, null);
        String tokenType = sharedPreferences.getString(TOKEN_TYPE, "Bearer");

        if (token != null) {
            return tokenType + " " + token;
        }
        return null;
    }

    private void showLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
            // Animación de fade in
            loadingOverlay.setAlpha(0f);
            loadingOverlay.animate()
                .alpha(1f)
                .setDuration(200)
                .start();
        }
    }

    private void hideLoading() {
        if (loadingOverlay != null && loadingOverlay.getVisibility() == View.VISIBLE) {
            // Animación de fade out
            loadingOverlay.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> loadingOverlay.setVisibility(View.GONE))
                .start();
        }
    }
}

