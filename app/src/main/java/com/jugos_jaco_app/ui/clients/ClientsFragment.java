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
import android.util.Log;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.lifecycle.ViewModelProvider;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.adapters.ClientAdapter;
import com.jugos_jaco_app.ui.models.Client;
import com.jugos_jaco_app.ui.utilities.Utilities;
import com.jugos_jaco_app.ui.utilities.VolleySingleton;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientsFragment extends Fragment implements ChipGroup.OnCheckedChangeListener {

    private ClientsViewModel viewModel;
    private RecyclerView recyclerView;
    private ClientAdapter clientAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final String URL_CLIENTS = Utilities.URL +"clients/"; // Reemplaza con tu URL real
    private boolean hasShownDaySelectionMessage = false; // Nueva variable para controlar el mensaje

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

    private boolean isUserInteraction = false;
    private boolean isRestoringState = false;

    private String lastSelectedDay = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = new ViewModelProvider(requireActivity()).get(ClientsViewModel.class);
        Log.d("ClientsFragment", "onCreate - Fragment creado");
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
        Log.d("ClientsFragment", "onCreateView - Creando vista del fragmento");
        View root = inflater.inflate(R.layout.fragment_clients, container, false);

        // Inicializar ViewModel a nivel de actividad
        viewModel = new ViewModelProvider(requireActivity()).get(ClientsViewModel.class);

        // Configurar vistas
        setupViews(root);

        // Observar cambios en la lista de clientes
        viewModel.getClients().observe(getViewLifecycleOwner(), clients -> {
            if (clients != null) {
                Log.d("ClientsFragment", "Observador de clientes - Actualizando lista con " + clients.size() + " clientes");
                clientAdapter.updateList(clients);
                swipeRefreshLayout.setRefreshing(false);
                hideLoading();
            }
        });

        // Observar cambios en el día seleccionado
        viewModel.getSelectedDay().observe(getViewLifecycleOwner(), day -> {
            Log.d("ClientsFragment", "Observador de selectedDay - Día recibido: " + day + 
                                    ", lastSelectedDay: " + lastSelectedDay + 
                                    ", isUserInteraction: " + isUserInteraction);
            
            // Solo actualizar si el día ha cambiado realmente y no estamos restaurando el estado
            if (day != null && !day.equals(lastSelectedDay) && !isRestoringState) {
                Log.d("ClientsFragment", "Actualizando selección de día - Nuevo día: " + day);
                lastSelectedDay = day;
                updateDaySelection(day);
                if (!viewModel.hasLoadedData()) {
                    Log.d("ClientsFragment", "Cargando clientes para el día: " + day);
                    viewModel.loadClients(requireContext());
                }
            } else if (day == null && lastSelectedDay != null && !isRestoringState) {
                Log.d("ClientsFragment", "Limpiando selección de día");
                lastSelectedDay = null;
                if (isUserInteraction) {
                    chipGroupDays.clearCheck();
                }
            }
        });

        // Solo establecer el día actual si no hay un día seleccionado previamente
        if (viewModel.getSelectedDay().getValue() == null && !viewModel.hasLoadedData()) {
            Log.d("ClientsFragment", "Estableciendo día actual");
            setCurrentDay();
        }

        return root;
    }

    private void setupViews(View root) {
        recyclerView = root.findViewById(R.id.recyclerView);
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        loadingOverlay = root.findViewById(R.id.loadingView);

        // Configurar RecyclerView
        clientAdapter = new ClientAdapter(new ArrayList<>(), requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(clientAdapter);

        // Configurar ItemTouchHelper para el arrastre
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                0) {

            @Override
            public boolean isLongPressDragEnabled() {
                if (viewModel.getSelectedDay().getValue() == null && !hasShownDaySelectionMessage) {
                    Toast.makeText(requireContext(), 
                        "Seleccione un día de la semana para poder reordenar los clientes",
                        Toast.LENGTH_LONG).show();
                    hasShownDaySelectionMessage = true;
                }
                return viewModel.getSelectedDay().getValue() != null;
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder,
                                @NonNull RecyclerView.ViewHolder target) {
                if (viewModel.getSelectedDay().getValue() == null) {
                    return false;
                }

                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                
                // Primero movemos el item en el adaptador
                clientAdapter.moveItem(fromPosition, toPosition);
                
                // Luego calculamos y actualizamos las posiciones
                updatePositionsAfterMove(fromPosition, toPosition);
                
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // No implementamos el deslizamiento
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                // Cuando se suelta el item, actualizamos en el servidor solo si hay un día seleccionado
                if (viewModel.getSelectedDay().getValue() != null) {
                    List<Client> clients = clientAdapter.getClients();
                    if (viewHolder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                        Client movedClient = clients.get(viewHolder.getAdapterPosition());
                        updateClientPosition(movedClient);
                    }
                }
            }
        };

        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
        clientAdapter.attachTouchHelper(touchHelper);

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

        // Configurar listeners para los chips
        chipGroupDays.setOnCheckedChangeListener(this);
    }

    private void updateDaySelection(String day) {
        Log.d("ClientsFragment", "updateDaySelection - Día: " + day + 
                                ", isRestoringState: " + isRestoringState + 
                                ", isUserInteraction: " + isUserInteraction);
        
        isRestoringState = true; // Indicar que estamos restaurando el estado
        isUserInteraction = false; // Desactivar la interacción del usuario
        
        switch (day) {
            case "lunes":
                chipMonday.setChecked(true);
                break;
            case "martes":
                chipTuesday.setChecked(true);
                break;
            case "miercoles":
                chipWednesday.setChecked(true);
                break;
            case "jueves":
                chipThursday.setChecked(true);
                break;
            case "viernes":
                chipFriday.setChecked(true);
                break;
            case "sabado":
                chipSaturday.setChecked(true);
                break;
        }
        
        isUserInteraction = true; // Reactivar la interacción del usuario
        isRestoringState = false; // Indicar que hemos terminado de restaurar el estado
        
        Log.d("ClientsFragment", "updateDaySelection completado - isRestoringState: " + isRestoringState + 
                                ", isUserInteraction: " + isUserInteraction);
    }

    private void setCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String currentDay = getDayName(dayOfWeek);
        if (currentDay != null && !currentDay.isEmpty()) {
            viewModel.setSelectedDay(currentDay);
        }
    }

    private String getDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return "lunes";
            case Calendar.TUESDAY:
                return "martes";
            case Calendar.WEDNESDAY:
                return "miercoles";
            case Calendar.THURSDAY:
                return "jueves";
            case Calendar.FRIDAY:
                return "viernes";
            case Calendar.SATURDAY:
                return "sabado";
            default:
                return ""; // Retornar string vacío en lugar de "lunes" por defecto
        }
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

    @Override
    public void onCheckedChanged(@NonNull ChipGroup group, int checkedId) {
        Log.d("ClientsFragment", "onCheckedChanged - checkedId: " + checkedId + 
                                ", isRestoringState: " + isRestoringState + 
                                ", isUserInteraction: " + isUserInteraction);
        
        // No procesar si estamos restaurando el estado o no es una interacción del usuario
        if (isRestoringState || !isUserInteraction) {
            Log.d("ClientsFragment", "Ignorando cambio de selección - isRestoringState o !isUserInteraction");
            return;
        }

        if (checkedId == View.NO_ID) {
            Log.d("ClientsFragment", "Día deseleccionado");
            viewModel.setSelectedDay(null);
            viewModel.forceLoadClients(requireContext());
            hasShownDaySelectionMessage = false;
        } else {
            Chip selectedChip = group.findViewById(checkedId);
            if (selectedChip != null) {
                String selectedDay = formatDayForServer(selectedChip.getText().toString());
                // Solo actualizar si el día ha cambiado realmente
                if (!selectedDay.equals(lastSelectedDay)) {
                    Log.d("ClientsFragment", "Nuevo día seleccionado: " + selectedDay);
                    viewModel.setSelectedDay(selectedDay);
                    viewModel.forceLoadClients(requireContext());
                    hasShownDaySelectionMessage = false;
                } else {
                    Log.d("ClientsFragment", "Ignorando selección del mismo día: " + selectedDay);
                }
            }
        }
    }

    private void updatePositionsAfterMove(int fromPosition, int toPosition) {
        List<Client> clients = clientAdapter.getClients();
        
        // Obtener el cliente que se movió
        Client movedClient = clients.get(toPosition);
        String oldPosition = movedClient.getPosition();

        // Determinar la nueva posición
        if (fromPosition > toPosition) {
            // Si se mueve hacia arriba
            if (toPosition == 0) {
                // Si se mueve a la primera posición, siempre será 1
                movedClient.setPosition("1");
                // Los demás clientes incrementan su posición
                for (int i = 1; i < clients.size(); i++) {
                    Client client = clients.get(i);
                    client.setPosition(String.valueOf(i + 1));
                    clientAdapter.notifyItemChanged(i);
                }
            } else {
                // Si se mueve a una posición intermedia hacia arriba
                movedClient.setPosition(String.valueOf(toPosition + 1));
                // Actualizar posiciones de los clientes que quedaron abajo
                for (int i = toPosition + 1; i < clients.size(); i++) {
                    Client client = clients.get(i);
                    client.setPosition(String.valueOf(i + 1));
                    clientAdapter.notifyItemChanged(i);
                }
            }
        } else if (fromPosition < toPosition) {
            // Si se mueve hacia abajo
            // El cliente movido toma la posición de destino + 1
            movedClient.setPosition(String.valueOf(toPosition + 1));
            
            // Los clientes entre la posición original y la destino decrementan su posición en 1
            for (int i = fromPosition; i < toPosition; i++) {
                Client client = clients.get(i);
                client.setPosition(String.valueOf(i + 1));
                clientAdapter.notifyItemChanged(i);
            }
        }

        // Mostrar información de las posiciones antes del cambio
        Log.d("REORDER_DEBUG", String.format(
            "Movimiento iniciado:\n" +
            "Cliente: %s %s\n" +
            "De posición lista: %d (position=%s)\n" +
            "A posición lista: %d (nueva position será=%s)",
            movedClient.getFirstName(),
            movedClient.getLastName(),
            fromPosition,
            oldPosition,
            toPosition,
            movedClient.getPosition()
        ));

        // Mostrar información final del movimiento
        Log.d("REORDER_DEBUG", String.format(
            "Movimiento completado:\n" +
            "Cliente %s %s\n" +
            "Position anterior: %s\n" +
            "Position nueva: %s\n" +
            "Position del cliente anterior: %s\n" +
            "Position del cliente siguiente: %s",
            movedClient.getFirstName(),
            movedClient.getLastName(),
            oldPosition,
            movedClient.getPosition(),
            toPosition > 0 ? clients.get(toPosition - 1).getPosition() : "ninguna",
            toPosition < clients.size() - 1 ? clients.get(toPosition + 1).getPosition() : "ninguna"
        ));

        // Notificar el cambio del item movido
        clientAdapter.notifyItemChanged(toPosition);
    }

    private void updateClientPosition(Client client) {
        String url = Utilities.URL + "clients/"+client.getId()+"/visit/position";
        
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("position", client.getPosition());
            jsonBody.put("day", client.getVisitDay());
            
            // Mostrar información antes de enviar al servidor
            Log.d("REORDER_DEBUG", String.format(
                "Enviando al servidor:\n" +
                "Cliente ID: %s\n" +
                "Nueva posición: %s\n" +
                "Día: %s",
                client.getId(),
                client.getPosition(),
                client.getVisitDay()
            ));
            
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                jsonBody,
                response -> {
                    // Éxito al actualizar la posición
                    Toast.makeText(requireContext(), 
                        String.format("Posición actualizada para %s %s", 
                            client.getFirstName(), 
                            client.getLastName()),
                        Toast.LENGTH_SHORT).show();
                    
                    // Recargar la lista para asegurar que todas las posiciones estén actualizadas
                    viewModel.forceLoadClients(requireContext());
                },
                error -> {
                    // Intentar obtener el mensaje de error del servidor
                    String errorMessage = "Error al actualizar posición";
                    try {
                        String responseBody = new String(error.networkResponse.data);
                        JSONObject jsonError = new JSONObject(responseBody);
                        if (jsonError.has("message")) {
                            errorMessage = jsonError.getString("message");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Mostrar el mensaje de error
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                    
                    // Log del error para debugging
                    Log.e("UPDATE_POSITION_ERROR", "Error: " + errorMessage);
                    
                    viewModel.forceLoadClients(requireContext());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", getAuthorizationHeader(requireContext()));
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    private String formatDayForServer(String day) {
        // Convertir el día al formato que espera el servidor
        switch (day.toLowerCase()) {
            case "lunes":
                return "Lunes";
            case "martes":
                return "Martes";
            case "miércoles":
            case "miercoles":
                return "Miércoles";
            case "jueves":
                return "Jueves";
            case "viernes":
                return "Viernes";
            case "sábado":
            case "sabado":
                return "Sábado";
            default:
                return day;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("ClientsFragment", "onResume - Fragment resumido");
        Log.d("ClientsFragment", "Estado actual - isUserInteraction: " + isUserInteraction + 
                                ", isRestoringState: " + isRestoringState + 
                                ", lastSelectedDay: " + lastSelectedDay);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("ClientsFragment", "onPause - Fragment pausado");
    }
}

