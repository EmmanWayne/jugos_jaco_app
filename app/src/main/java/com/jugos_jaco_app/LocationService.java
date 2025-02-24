package com.jugos_jaco_app;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

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

                // Obtener las coordenadas de la ubicación
                for (android.location.Location location : locationResult.getLocations()) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    Log.i("ServiceLocation", "Lat: " + latitude + ", Long: " + longitude);

                    // Aquí puedes manejar las coordenadas (enviarlas a un servidor, almacenarlas, etc.)
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
}
