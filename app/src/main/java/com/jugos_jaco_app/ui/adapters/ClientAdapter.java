package com.jugos_jaco_app.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
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
import androidx.recyclerview.widget.ItemTouchHelper;

import com.bumptech.glide.load.engine.GlideException;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.models.Client;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.load.DataSource;
import android.graphics.drawable.Drawable;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import android.graphics.Bitmap;

import com.jugos_jaco_app.ui.utilities.Utilities;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import android.Manifest;

import de.hdodenhof.circleimageview.CircleImageView;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder> {

    private List<Client> clients;
    private Context context;
    private static final int REQUEST_ENABLE_GPS = 123;
    private static final int REQUEST_CALL_PERMISSION = 124;
    private ItemTouchHelper touchHelper;
    private static final int IMAGE_SIZE = 100; // Tamaño en dp para las imágenes en miniatura

    public ClientAdapter(List<Client> clients, Context context) {
        this.clients = clients;
        this.context = context;
    }

    public void attachTouchHelper(ItemTouchHelper touchHelper) {
        this.touchHelper = touchHelper;
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
        holder.bind(client);

        holder.ivCoordinatesIcon.setOnClickListener(v -> {
            if (client.hasCoordinates()) {
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

        holder.ivVentaIcon.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("clientId", client.getId());
            bundle.putString("clientName", client.getFirstName() + " " + client.getLastName());
            
            Navigation.findNavController(v)
                .navigate(R.id.action_nav_clientes_to_newSaleFragment, bundle);
        });

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
            bundle.putString("businessName", client.getBusinessName() != null && !client.getBusinessName().isEmpty() ? 
                client.getBusinessName() : "Sin nombre de negocio");
            bundle.putString("phoneNumber", client.getPhoneNumber());
            bundle.putString("position", client.getPosition());
            bundle.putString("visit_day", client.getVisitDay());
            
            // Agregar información de créditos activos
            bundle.putInt("countAccountReceivable", client.getCountAccountReceivable());
            bundle.putDouble("totalAccountReceivable", client.getTotalAccountReceivable());

            if (client.hasCoordinates()) {
                bundle.putString("latitude", client.getLatitude() != null ? client.getLatitude() : null);
                bundle.putString("longitude", client.getLongitude() != null ? client.getLongitude() : null);
                bundle.putString("plus_code", client.getPlus_code() != null ? client.getPlus_code() : null);
            }

            NavController navController = Navigation.findNavController(v);

            try {
                navController.navigate(R.id.clientDetailsFragment, bundle);
            } catch (Exception e) {
                Log.e("Navigation", "Error al navegar: " + e.getMessage());
            }
        });

        // Configurar el evento de mantener presionado
        holder.itemView.setOnLongClickListener(v -> {
            touchHelper.startDrag(holder);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return clients.size();
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
            // Obtener el Plus Code del cliente
            String plusCode = client.getPlus_code();

            // Codificar el Plus Code para asegurarse de que el signo '+' se maneje correctamente
            String encodedPlusCode = URLEncoder.encode(plusCode, "UTF-8");
             // Crear URI para Google Maps con el Plus Code codificado
             Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + encodedPlusCode);;
            // Crear intent para abrir Google Maps
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

                 context.startActivity(mapIntent);

        } catch (UnsupportedEncodingException e) {
            // Si ocurre un error en la codificación, mostrar mensaje de error
            Toast.makeText(context, "Error al codificar el Plus Code", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Si ocurre cualquier otro error, mostrar mensaje genérico
            Toast.makeText(context, "Error al abrir el mapa", Toast.LENGTH_SHORT).show();
        }
    }    private void makePhoneCall(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            context.startActivity(intent);
        } catch (SecurityException e) {
            Toast.makeText(context, "Error al realizar la llamada", Toast.LENGTH_SHORT).show();
        }
    }

    public void moveItem(int fromPosition, int toPosition) {
        Client movedClient = clients.get(fromPosition);
        clients.remove(fromPosition);
        clients.add(toPosition, movedClient);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void updatePosition(int position, String newPosition) {
        if (position >= 0 && position < clients.size()) {
            Client client = clients.get(position);
            client.setPosition(newPosition);
            notifyItemChanged(position);
        }
    }

    public List<Client> getClients() {
        return clients;
    }

    class ClientViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvBusinessName;
        private final TextView tvFullName;
        private final TextView tvPhoneNumber;
        private final TextView tvVisitDay;
        private final TextView tvAccountReceivable;

        private final CircleImageView imageItem;
        private final ImageView ivCoordinatesIcon;
        private final ImageView ivPhoneIcon;
        private final ImageView ivVentaIcon;

        public ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBusinessName = itemView.findViewById(R.id.tvBusinessName);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
            imageItem = itemView.findViewById(R.id.imageitem);
            ivCoordinatesIcon = itemView.findViewById(R.id.ivCoordinatesIcon);
            ivPhoneIcon = itemView.findViewById(R.id.ivPhoneIcon);
            ivVentaIcon = itemView.findViewById(R.id.ivVentaIcon);
            tvVisitDay =  itemView.findViewById(R.id.tvVisitDay);
            tvAccountReceivable = itemView.findViewById(R.id.tvAccountReceivable);

            // Configurar listeners para los iconos
            setupIconListeners();
        }

        private void setupIconListeners() {
            ivCoordinatesIcon.setOnClickListener(v -> {
                // Manejar clic en icono de coordenadas
            });

            ivPhoneIcon.setOnClickListener(v -> {
                // Manejar clic en icono de teléfono
            });

            ivVentaIcon.setOnClickListener(v -> {
                // Manejar clic en icono de venta
            });
        }

        public void bind(Client client) {
            tvBusinessName.setText(client.getBusinessName());
            tvVisitDay.setText(client.getVisitDay());

            tvFullName.setText(String.format("%s %s", client.getFirstName(), client.getLastName()));
            tvPhoneNumber.setText(String.format("%s", client.getPhoneNumber()));

            // Mostrar información de cuentas por cobrar si existe
            if (client.hasActiveCredits()) {
                tvAccountReceivable.setVisibility(View.VISIBLE);
                tvAccountReceivable.setText(String.format("Deuda: %d facturas - $%.2f", 
                    client.getCountAccountReceivable(), client.getTotalAccountReceivable()));
            } else {
                tvAccountReceivable.setVisibility(View.GONE);
            }

            // Cargar imagen de perfil comprimida
            if (client.getProfileImage() != null && !client.getProfileImage().isEmpty()) {
                String imageUrl = Utilities.URL_FOTOS + "storage/" + client.getProfileImage();
                
                // Opciones para la imagen comprimida en la lista
                RequestOptions options = new RequestOptions()
                    .override(IMAGE_SIZE, IMAGE_SIZE)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.cliente_icon)
                    .error(R.drawable.cliente_icon);

                // Cargar la imagen comprimida con Glide
                Glide.with(context)
                    .load(imageUrl)
                    .apply(options)
                    .into(imageItem);

                // Agregar evento de clic para ver la imagen original
                imageItem.setOnClickListener(v -> {
                    Log.d("ImageDebug", "Iniciando carga de imagen en tamaño completo");
                    Log.d("ImageDebug", "URL de la imagen: " + imageUrl);

                    // Crear un diálogo para mostrar la imagen en tamaño completo
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_full_image, null);
                    ImageView fullImageView = dialogView.findViewById(R.id.fullImageView);
                    ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);

                    // Mostrar el ProgressBar mientras se carga la imagen
                    progressBar.setVisibility(View.VISIBLE);
                    Log.d("ImageDebug", "ProgressBar visible");

                    // Configurar opciones de Glide para la imagen en tamaño completo
                    RequestOptions fullImageOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.cliente_icon)
                        .dontAnimate()
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);

                    // Cargar la imagen original sin compresión
                    Glide.with(context)
                        .load(imageUrl)
                        .apply(fullImageOptions)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                Log.e("ImageDebug", "Error al cargar la imagen: " + (e != null ? e.getMessage() : "Error desconocido"));
                                if (e != null) {
                                    Log.e("ImageDebug", "Causa del error: " + e.getRootCauses());
                                    for (Throwable t : e.getRootCauses()) {
                                        Log.e("ImageDebug", "Causa raíz: " + t.getMessage());
                                    }
                                }
                                Toast.makeText(context, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                Log.d("ImageDebug", "Imagen cargada exitosamente");
                                Log.d("ImageDebug", "Tamaño de la imagen: " + resource.getIntrinsicWidth() + "x" + resource.getIntrinsicHeight());
                                Log.d("ImageDebug", "Fuente de datos: " + dataSource);
                                return false;
                            }
                        })
                        .into(fullImageView);

                    // Configurar el diálogo para que ocupe toda la pantalla
                    builder.setView(dialogView);
                    AlertDialog dialog = builder.create();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    dialog.show();
                    Log.d("ImageDebug", "Diálogo mostrado");
                });
            } else {
                imageItem.setImageResource(R.drawable.cliente_icon);
                imageItem.setOnClickListener(null);
            }
        }
    }
}
