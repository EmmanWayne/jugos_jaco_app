package com.jugos_jaco_app;

import android.app.ActivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;

import com.google.android.material.navigation.NavigationView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.jugos_jaco_app.databinding.ActivityMainBinding;
import com.jugos_jaco_app.ui.fragments_client.ClientsFragment;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.SharedPreferences;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONObject;
import com.android.volley.AuthFailureError;
import com.jugos_jaco_app.ui.utilities.Utilities;
import com.google.android.material.snackbar.Snackbar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onResume() {
        super.onResume();
        verificarPermisosUbicacion();
    }


    private AlertDialog gpsAlertDialog;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int REQUEST_ENABLE_GPS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_clientes, R.id.nav_ventas, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        verificarPermisosUbicacion();

    }

    private void verificarPermisosUbicacion() {
        // Verificar si los permisos están otorgados
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permisos
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Verificar si el GPS está activado
            verificarGPSActivado();
        }
    }

    private void verificarGPSActivado() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGPSOn) {
            if (gpsAlertDialog != null && gpsAlertDialog.isShowing()) {
                gpsAlertDialog.cancel();
            }
            gpsAlertDialog = new AlertDialog.Builder(this)
                    .setTitle("Activar GPS")
                    .setMessage("Debe activar la ubicación para continuar")
                    .setPositiveButton("Activar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Toast.makeText(MainActivity.this, "El GPS es necesario para esta función", Toast.LENGTH_SHORT).show();
                            finish();

                        }
                    })
                    .setCancelable(false)
                    .show();
        } else {
            startLocationService();
        }
    }    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LocationService.class.getName().equals(service.service.getClassName())) {
                    return true; // El servicio ya está en ejecución
                }
            }
        }
        return false;
    }
    private void startLocationService() {
        if (!isLocationServiceRunning()) { // Verifica si el servicio ya está activo
            Intent serviceIntent = new Intent(this, LocationService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        } else {
            Log.i("ServiceLocation", "El servicio de ubicación ya está en ejecución.");
        }
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        assert searchView != null;
        searchView.setQueryHint("Buscar clientes...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Manejar la búsqueda al presionar "Enter"
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filtrar la lista de clientes según el texto de búsqueda
                filterClients(newText);
                return true;
            }
        });
        return true;
    }

    private void filterClients(String query) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);

        if (currentFragment != null && currentFragment.getChildFragmentManager().getFragments().size() > 0) {
            Fragment activeFragment = currentFragment.getChildFragmentManager().getFragments().get(0);
            if (activeFragment instanceof ClientsFragment) {
                ((ClientsFragment) activeFragment).filterClients(query);
            }
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_GPS) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // El GPS fue activado, intentar abrir el mapa nuevamente
                // Puedes usar un callback o EventBus para notificar al adaptador
            } else {
                Toast.makeText(this, "Se requiere GPS para ver la ubicación", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            showLogoutConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Está seguro que desea cerrar sesión?")
            .setPositiveButton("Sí", (dialog, which) -> {
                performLogout();
            })
            .setNegativeButton("No", null)
            .setIcon(R.drawable.ic_logout)
            .show();
    }

    private void performLogout() {
        String url = Utilities.URL + "logout";

        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.POST,
            url,
            null,
            response -> {
                // Éxito en el logout
                clearSessionAndRedirectToLogin();
            },
            error -> {
                // Incluso si hay error, cerramos sesión localmente
                if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                    clearSessionAndRedirectToLogin();
                } else {
                    // Mostrar error pero aún así cerrar sesión
                    showError("Error al cerrar sesión en el servidor");
                    clearSessionAndRedirectToLogin();
                }
            }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String authHeader = Login.getAuthorizationHeader(MainActivity.this);
                if (authHeader != null) {
                    headers.put("Authorization", authHeader);
                }
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        // Agregar la solicitud a la cola
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void clearSessionAndRedirectToLogin() {
        // Limpiar SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(Login.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Detener el servicio de ubicación si está corriendo
        stopService(new Intent(this, LocationService.class));

        // Redirigir al login
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        ).setBackgroundTint(ContextCompat.getColor(this, R.color.error_color))
         .setTextColor(ContextCompat.getColor(this, R.color.white))
         .show();
    }
}