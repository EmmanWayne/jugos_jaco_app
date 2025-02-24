package com.jugos_jaco_app.ui.gallery;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.jugos_jaco_app.R;
import java.util.List;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder> {

    private List<Client> clients;
    private Context context;

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
                Toast.makeText(context, "Mostrando ubicación de " + client.getFirstName(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "No hay coordenadas disponibles", Toast.LENGTH_SHORT).show();
            }
        });

        holder.ivPhoneIcon.setOnClickListener(v ->
                Toast.makeText(context, "Llamando a " + client.getPhoneNumber(), Toast.LENGTH_SHORT).show()
        );

        holder.ivVentaIcon.setOnClickListener(v ->
                Toast.makeText(context, "Iniciando venta para " + client.getFirstName(), Toast.LENGTH_SHORT).show()
        );

        // Evento de clic en el elemento completo para navegar a ClientDetailsFragment
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("firstName", client.getFirstName());
            bundle.putString("lastName", client.getLastName());
            bundle.putString("adress", client.getAdress());

            bundle.putString("phoneNumber", client.getPhoneNumber());
                 if (client.hasCoordinates()) {
                     bundle.putString("latitude", client.getLatitude() != null ? client.getLatitude() : null);
                     bundle.putString("longitude", client.getLongitude() != null ? client.getLongitude() : null);
                }

            NavController navController = Navigation.findNavController(v);

                 try{
                     navController.navigate(R.id.clientDetailsFragment, bundle);

                 }catch (Exception e){
                     Log.i("TAGASIEMPRE",e.toString());

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
}
