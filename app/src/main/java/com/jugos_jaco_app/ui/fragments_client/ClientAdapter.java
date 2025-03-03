package com.jugos_jaco_app.ui.fragments_client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.jugos_jaco_app.R;
import java.util.List;
import android.Manifest;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder> {

    private List<Client> clients;
    private Context context;
    private static final int REQUEST_ENABLE_GPS = 123;
    private static final int REQUEST_CALL_PERMISSION = 124;

    public ClientAdapter(List<Client> clients, Context context) {
        this.clients = clients;
        this.context = context;
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client, parent, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        Client client = clients.get(position);

        holder.tvFullName.setText(client.getFirstName() + " " + client.getLastName());
        holder.tvPhoneNumber.setText(client.getPhoneNumber());

        if (client.hasCoordinates()) {
            holder.ivCoordinatesIcon.setImageResource(R.drawable.ic_has_coordinates);
        } else {
            holder.ivCoordinatesIcon.setImageResource(R.drawable.ic_no_coordinates);
        }

        holder.imageItem.setOnClickListener(v ->
                Toast.makeText(context, "Imagen de " + client.getFirstName(), Toast.LENGTH_SHORT).show()
        );

        holder.ivCoordinatesIcon.setOnClickListener(v -> {
            if (client.hasCoordinates()) {
                Toast.makeText(context, ""+client.getLatitude()+" "+client.getLongitude(), Toast.LENGTH_SHORT).show();

                checkLocationAndOpenMap(client);
            } else {
                Toast.makeText(context, "No hay coordenadas disponibles", Toast.LENGTH_SHORT).show();
            }
        });

        holder.ivPhoneIcon.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) 
                    != PackageManager.PERMISSION_GRANTED) {
                // No tenemos permiso, mostrar diálogo explicativo
                new AlertDialog.Builder(context)
                    .setTitle("Permiso necesario")
                    .setMessage("Para realizar llamadas necesitamos su permiso. ¿Desea otorgarlo?")
                    .setPositiveButton("Configurar", (dialog, which) -> {
                        // Solicitar el permiso
                        ActivityCompat.requestPermissions(
                            (Activity) context,
                            new String[]{Manifest.permission.CALL_PHONE},
                            REQUEST_CALL_PERMISSION
                        );
                    })
                    .setNegativeButton("No", (dialog, which) -> 
                        Toast.makeText(context, 
                            "No se pueden realizar llamadas sin el permiso", 
                            Toast.LENGTH_SHORT).show())
                    .create()
                    .show();
            } else {
                // Tenemos permiso, realizar la llamada
                makePhoneCall(client.getPhoneNumber());
            }
        });

        holder.ivVentaIcon.setOnClickListener(v ->
                Toast.makeText(context, "Iniciando venta para " + client.getFirstName(), Toast.LENGTH_SHORT).show()
        );

        // Evento de clic en el elemento completo para navegar a ClientDetailsFragment
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("firstName", client.getFirstName());
            bundle.putString("lastName", client.getLastName());
            bundle.putString("adress", client.getAdress());
            bundle.putString("department", client.getDepartment());
            bundle.putString("township", client.getTownship());
            bundle.putString("id", client.getId());
            bundle.putString("typePrice", client.getTypePrice());



            bundle.putString("phoneNumber", client.getPhoneNumber());
                 if (client.hasCoordinates()) {
                     bundle.putString("latitude", client.getLatitude() != null ? client.getLatitude() : null);
                     bundle.putString("longitude", client.getLongitude() != null ? client.getLongitude() : null);
                }

            NavController navController = Navigation.findNavController(v);

                 try{
                     navController.navigate(R.id.clientDetailsFragment, bundle);

                 }catch (Exception e){

                 }
         });
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    public static class ClientViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvPhoneNumber;
        ImageView ivCoordinatesIcon, ivPhoneIcon, ivVentaIcon, imageItem;

        public ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
            ivCoordinatesIcon = itemView.findViewById(R.id.ivCoordinatesIcon);
            ivPhoneIcon = itemView.findViewById(R.id.ivPhoneIcon);
            ivVentaIcon = itemView.findViewById(R.id.ivVentaIcon);
            imageItem = itemView.findViewById(R.id.imageitem);
        }
    }
    public void updateList(List<Client> newClients) {
        clients.clear();
        clients.addAll(newClients);
        notifyDataSetChanged();
    }

    private void checkLocationAndOpenMap(Client client) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new AlertDialog.Builder(context)
                .setTitle("GPS Desactivado")
                .setMessage("Para ver la ubicación del cliente necesita activar el GPS. ¿Desea activarlo?")
                .setPositiveButton("Activar GPS", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    ((Activity) context).startActivityForResult(intent, REQUEST_ENABLE_GPS);
                })
                .setNegativeButton("Cancelar", null)
                .create()
                .show();
        } else {
            openGoogleMaps(client);
        }
    }

    private void openGoogleMaps(Client client) {
        try {
            // Crear URI para Google Maps con las coordenadas del cliente
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + 
                client.getLatitude() + "," + client.getLongitude());
            
            // Crear intent para abrir Google Maps
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Verificar si Google Maps está instalado
            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
            } else {

                 // Si Google Maps no está instalado, abrir en el navegador
                Uri browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + 
                    client.getLatitude() + "," + client.getLongitude());
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                context.startActivity(browserIntent);
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error al abrir el mapa", Toast.LENGTH_SHORT).show();
        }
    }

    private void makePhoneCall(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            context.startActivity(intent);
        } catch (SecurityException e) {
            Toast.makeText(context, "Error al realizar la llamada", Toast.LENGTH_SHORT).show();
        }
    }
}
