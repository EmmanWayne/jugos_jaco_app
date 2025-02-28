package com.jugos_jaco_app.ui.fragments_client;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.utilities.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ClientsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ClientAdapter clientAdapter;
    private List<Client> clients;
    private List<Client> clientsFull;
    private RequestQueue requestQueue;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final String URL_CLIENTS = Utilities.URL +"clientes"; // Reemplaza con tu URL real

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

        // Inicializar Volley
        requestQueue = Volley.newRequestQueue(requireContext());

        // Inicializar SwipeRefreshLayout
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::loadClientsFromServer);

        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        clients = new ArrayList<>();
        clientsFull = new ArrayList<>();
        clientAdapter = new ClientAdapter(clients, requireContext());
        recyclerView.setAdapter(clientAdapter);

        FloatingActionButton fabCrearCliente = root.findViewById(R.id.fab_crear_cliente);
        fabCrearCliente.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.nuevoClienteFragment);
        });

        // Cargar clientes del servidor
        loadClientsFromServer();

        return root;
    }

    private void loadClientsFromServer() {
        swipeRefreshLayout.setRefreshing(true);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                URL_CLIENTS,
                null,
                response -> {
                    clients.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject clientJson = response.getJSONObject(i);
                            Client client = new Client(
                                    clientJson.getString("firstName"),
                                     clientJson.getString("lastName"),
                                    clientJson.getString("phone_number"),
                                    clientJson.getString("address"),
                                    clientJson.getString("departament"),
                                    clientJson.getString("township"),
                                     clientJson.optString("latitude", null),
                                    clientJson.optString("longitude", null)
                            );
                            clients.add(client);
                        }
                        clientsFull = new ArrayList<>(clients);
                        clientAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error al procesar los datos del servidor", Toast.LENGTH_SHORT).show();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                },
                error -> {
                    Toast.makeText(getContext(), "Error al cargar los clientes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
        );

        request.setTag(this);
        requestQueue.add(request);
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
        List<Client> filteredList = new ArrayList<>();

        for (Client client : clientsFull) {
            if (client.getFirstName().toLowerCase().contains(query.toLowerCase()) ||
                    client.getLastName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(client);
            }
        }

        clientAdapter.updateList(filteredList);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }
}

