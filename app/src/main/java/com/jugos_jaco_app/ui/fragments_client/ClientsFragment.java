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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jugos_jaco_app.R;

import java.util.ArrayList;
import java.util.List;

public class ClientsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ClientAdapter clientAdapter;
    private List<Client> clients;
    private List<Client> clientsFull; // Copia de la lista completa de clientes

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
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        // Inicializar el launcher para la configuración de ubicación
        locationSettingsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> checkGPSEnabled()
        );

        // Verificar GPS y permisos
        checkGPSEnabled();

        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        clients = new ArrayList<>();
        clientAdapter = new ClientAdapter(clients, requireContext());
        recyclerView.setAdapter(clientAdapter);

        FloatingActionButton fabCrearCliente = root.findViewById(R.id.fab_crear_cliente);
        fabCrearCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nuevoClienteFragment);
            }
        });

        fillClientsData();
        return root;
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
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            })
            .setNegativeButton("No", null)
            .create()
            .show();
    }

    private void fillClientsData() {
        if (clients == null) {
            clients = new ArrayList<>();
        }

        clients.add(new Client("Juan", "Danli Colonia cofradia", "Pérez", "555-1234", "14.6349", "-90.5069"));
        clients.add(new Client("María", "Danli Colonia cofradia", "López", "555-5678", null, null));
        clients.add(new Client("Carlos", "Danli Colonia cofradia", "Gómez", "555-9876", "13.9670", "-89.2064"));
        clients.add(new Client("Ana", "Danli Colonia cofradia", "Ramírez", "555-6543", null, null));

        clientsFull = new ArrayList<>(clients); // Guardar copia completa de la lista

        if (clientAdapter != null) {
            clientAdapter.notifyDataSetChanged();
        }
    }

    public void filterClients(String query) {
        List<Client> filteredList = new ArrayList<>();

        for (Client client : clientsFull) {
            if (client.getFirstName().toLowerCase().contains(query.toLowerCase()) ||
                    client.getLastName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(client);
            }
        }

        clientAdapter.updateList(filteredList); // Llamamos al método en el adaptador
    }
}

