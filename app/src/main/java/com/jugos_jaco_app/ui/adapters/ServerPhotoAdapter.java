package com.jugos_jaco_app.ui.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.api.ServerPhotosResponse.ServerPhoto;
import com.jugos_jaco_app.ui.clients.FullscreenImageDialog;
import com.jugos_jaco_app.ui.utilities.Utilities;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerPhotoAdapter extends RecyclerView.Adapter<ServerPhotoAdapter.ViewHolder> {
    private List<ServerPhoto> photos;
    private Context context;
    private Set<Integer> selectedPhotos = new HashSet<>();
    private boolean isSelectionMode = false;
    private Set<Integer> deletingPhotos = new HashSet<>();

    public interface SelectionModeListener {
        void onSelectionModeChanged(boolean isSelectionMode);
    }

    private SelectionModeListener selectionModeListener;

    public ServerPhotoAdapter(List<ServerPhoto> photos, Context context) {
        this.photos = photos;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_server_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServerPhoto photo = photos.get(position);
        String path = photo.getPath();
        
        // Determinar si es una ruta local o una URL del servidor
        Object imageSource;
        String fullImageUrl;
        
        if (path.startsWith("file://") || path.startsWith("/")) {
            // Es una ruta local
            imageSource = new File(path.startsWith("file://") ? path.substring(7) : path);
            fullImageUrl = path;
        } else {
            // Es una URL del servidor
            imageSource = Utilities.URL_FOTOS + "storage/" + path;
            fullImageUrl = Utilities.URL_FOTOS + "storage/" + path;
        }
        
        // Mostrar/ocultar progress según estado de eliminación
        holder.progressBar.setVisibility(
            deletingPhotos.contains(photo.getId()) ? View.VISIBLE : View.GONE
        );

        // Cargar la imagen usando Glide
        Glide.with(context)
            .load(imageSource)
            .placeholder(R.drawable.product_placeholder)
            .error(R.drawable.product_placeholder)
            .into(holder.imageView);

        // Configurar el click en la imagen
        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                togglePhotoSelection(position);
            } else {
                // Usar la URL o ruta completa para mostrar la imagen en grande
                Uri imageUri = Uri.parse(fullImageUrl);
                FullscreenImageDialog dialog = new FullscreenImageDialog(context, imageUri);
                dialog.show();
            }
        });

        // Configurar la selección
        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                isSelectionMode = true;
                if (selectionModeListener != null) {
                    selectionModeListener.onSelectionModeChanged(true);
                }
                togglePhotoSelection(position);
            }
            return true;
        });

        // Actualizar el estado visual de selección
        holder.checkBox.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
        holder.checkBox.setChecked(selectedPhotos.contains(photo.getId()));
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public void setSelectionModeListener(SelectionModeListener listener) {
        this.selectionModeListener = listener;
    }

    public void toggleSelectionMode() {
        isSelectionMode = !isSelectionMode;
        if (!isSelectionMode) {
            selectedPhotos.clear();
        }
        notifyDataSetChanged();
        if (selectionModeListener != null) {
            selectionModeListener.onSelectionModeChanged(isSelectionMode);
        }
    }

    public void togglePhotoSelection(int position) {
        ServerPhoto photo = photos.get(position);
        if (selectedPhotos.contains(photo.getId())) {
            selectedPhotos.remove(photo.getId());
        } else {
            selectedPhotos.add(photo.getId());
        }
        notifyItemChanged(position);
        
        // Notificar cambio en el estado de selección
        if (selectionModeListener != null) {
            selectionModeListener.onSelectionModeChanged(isSelectionMode);
        }
    }

    public Set<Integer> getSelectedPhotos() {
        return new HashSet<>(selectedPhotos);
    }

    public void setPhotoDeleting(int photoId, boolean isDeleting) {
        if (isDeleting) {
            deletingPhotos.add(photoId);
        } else {
            deletingPhotos.remove(photoId);
        }
        notifyDataSetChanged();
    }

    public void removePhoto(int photoId) {
        for (int i = 0; i < photos.size(); i++) {
            if (photos.get(i).getId() == photoId) {
                photos.remove(i);
                selectedPhotos.remove(photoId);
                deletingPhotos.remove(photoId);
                notifyItemRemoved(i);
                break;
            }
        }
        // Si no quedan fotos seleccionadas, salir del modo selección
        if (selectedPhotos.isEmpty()) {
            toggleSelectionMode();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        View selectionOverlay;
        View selectionCheck;
        ProgressBar progressBar;
        CheckBox checkBox;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivServerPhoto);
            selectionOverlay = itemView.findViewById(R.id.selectionOverlay);
            selectionCheck = itemView.findViewById(R.id.selectionCheck);
            progressBar = itemView.findViewById(R.id.progressBar);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
} 