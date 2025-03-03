package com.jugos_jaco_app;




import static com.jugos_jaco_app.Login.KEY_TOKEN;
import static com.jugos_jaco_app.Login.PREFS_NAME;
import static com.jugos_jaco_app.Login.TOKEN_TYPE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import com.android.volley.AuthFailureError;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import org.json.JSONObject;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jugos_jaco_app.ui.fragments_client.ClientsFragment;
import com.jugos_jaco_app.ui.utilities.Utilities;
import java.util.HashMap;
import java.util.Map;

public class LocationService extends Service {

     private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Llamar para iniciar el servicio en primer plano
        startForegroundService();

        // Inicializar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Crear la solicitud de ubicación
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                150000 // Intervalo de actualización (10 segundos)
        )
                .setMinUpdateIntervalMillis(7500) // Intervalo de actualización mínimo (5 segundos)
                .build();

        // Crear el callback para obtener la ubicación
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                // Get coordinates and send to server
                for (android.location.Location location : locationResult.getLocations()) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    Log.i("ServiceLocation", "Lat: " + latitude + ", Long: " + longitude);

                    // Send coordinates to server
                    sendLocationToServer(latitude, longitude);
                }
            }
        };

        // Verificar permisos y solicitar actualizaciones de ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }

        return START_STICKY; // Para que el servicio se reinicie si el sistema lo mata
    }


    private void startForegroundService() {
        String channelId = "location_channel";
        String channelName = "Location Service";

        // Crear un canal de notificación para dispositivos con Android 8.0 y superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, channelName, NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Crear la notificación que se mostrará mientras el servicio esté en primer plano
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Ubicación en segundo plano")
                .setContentText("El servicio está obteniendo la ubicación.")
                .setSmallIcon(R.drawable.logo_jugos_jaco) // Puedes cambiar este ícono
                .setPriority(NotificationCompat.PRIORITY_LOW);

        Notification notification = notificationBuilder.build();

        // Iniciar el servicio en primer plano
        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Detener las actualizaciones de ubicación cuando el servicio se destruye
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    public static String getIdEmpleado(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("id_empleado", null);
    }

    private void sendLocationToServer(double latitude, double longitude) {
        String url = Utilities.URL + "employees/"+getIdEmpleado(this)+"/location"; // Adjust the endpoint as needed

        // Create JSON object with coordinates
        Map<String, Object> params = new HashMap<>();
        params.put("latitude", latitude);
        params.put("longitude", longitude);


        JSONObject jsonParams = new JSONObject(params);

        // Create Volley request
        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonParams,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i("LocationService", "Coordinates sent successfully");
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("LocationService", "Error sending coordinates: " + 
                        (error.getMessage() != null ? error.getMessage() : "Unknown error"));
                }
            }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", getAuthorizationHeader(getApplicationContext()));
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };;

        // Add request to queue using VolleySingleton
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    public  String getAuthorizationHeader(Context context) {


        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String token = sharedPreferences.getString(KEY_TOKEN, null);
        String tokenType = sharedPreferences.getString(TOKEN_TYPE, "Bearer");

        if (token != null) {
            return tokenType + " " + token;
        }
        return null;
    }
}
