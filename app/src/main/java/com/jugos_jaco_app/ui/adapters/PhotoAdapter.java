package com.jugos_jaco_app.ui.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.clients.FullscreenImageDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    public static class PhotoItem {
        private String path;
        private boolean isUploaded;
        private boolean isUploading;

        public PhotoItem(String path, boolean isUploaded) {
            this.path = path;
            this.isUploaded = isUploaded;
            this.isUploading = false;
        }

        // Getters y setters
        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isUploaded() {
            return isUploaded;
        }

        public void setUploaded(boolean uploaded) {
            isUploaded = uploaded;
        }

        public boolean isUploading() {
            return isUploading;
        }

        public void setUploading(boolean uploading) {
            isUploading = uploading;
        }
    }

    private List<PhotoItem> photos;
    private Context context;
    private OnPhotoListener onPhotoListener;

    public PhotoAdapter(List<PhotoItem> photos, Context context, OnPhotoListener onPhotoListener) {
        this.photos = new ArrayList<>(photos);
        this.context = context;
        this.onPhotoListener = onPhotoListener;
    }

    // Método para agregar una foto local
    public void addLocalPhoto(String path) {
        photos.add(new PhotoItem(path, false));
        notifyItemInserted(photos.size() - 1);
    }

    // Método para actualizar el estado de subida
    public void setPhotoUploading(int position, boolean uploading) {
        if (position >= 0 && position < photos.size()) {
            photos.get(position).isUploading = uploading;
            notifyItemChanged(position);
        }
    }

    // Método para marcar una foto como subida
    public void setPhotoUploaded(int position, String serverUrl) {
        if (position >= 0 && position < photos.size()) {
            PhotoItem photo = photos.get(position);
            photo.path = serverUrl;
            photo.isUploaded = true;
            photo.isUploading = false;
            notifyItemChanged(position);
        }
    }

    // Método para actualizar la lista de fotos de forma segura
    public void updatePhotos(List<PhotoItem> newPhotos) {
        this.photos = new ArrayList<>(newPhotos);
        notifyDataSetChanged();
    }

    // Método para eliminar una foto de forma segura
    public void removePhoto(int position) {
        if (position >= 0 && position < photos.size()) {
            photos.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, photos.size());
        }
    }

    // Agregar este método
    public List<PhotoItem> getPendingPhotos() {
        List<PhotoItem> pendingPhotos = new ArrayList<>();
        for (PhotoItem photo : photos) {
            if (!photo.isUploaded()) {
                pendingPhotos.add(photo);
            }
        }
        return pendingPhotos;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        PhotoItem photoItem = photos.get(position);
        String photoPath = photoItem.getPath();

        // Cargar la imagen usando Glide
        if (photoPath.startsWith("http")) {
            // Es una URL
            Glide.with(context)
                .load(photoPath)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .centerCrop()
                .into(holder.imageView);
        } else {
            // Es un archivo local
            File imageFile = new File(photoPath);
            if (imageFile.exists()) {
                Glide.with(context)
                    .load(imageFile)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error)
                    .centerCrop()
                    .into(holder.imageView);
            } else {
                // Si el archivo no existe, intentar cargar directamente el URI
                Uri imageUri = Uri.parse(photoPath);
                Glide.with(context)
                    .load(imageUri)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error)
                    .centerCrop()
                    .into(holder.imageView);
            }
        }

        // Mostrar estado de la foto
        if (photoItem.isUploading()) {
            holder.progressUpload.setVisibility(View.VISIBLE);
            holder.statusIcon.setVisibility(View.GONE);
        } else {
            holder.progressUpload.setVisibility(View.GONE);
            holder.statusIcon.setVisibility(View.VISIBLE);
            
            if (photoItem.isUploaded()) {
                // Foto subida exitosamente
                holder.statusIcon.setImageResource(R.drawable.ic_uploaded);
                holder.statusIcon.setContentDescription("Foto subida");
            } else {
                // Foto pendiente de subir
                holder.statusIcon.setImageResource(R.drawable.ic_pending);
                holder.statusIcon.setContentDescription("Pendiente de subir");
            }
        }

        // Configurar el clic para mostrar la imagen en pantalla completa
        holder.imageView.setOnClickListener(v -> {
            Uri imageUri;
            
            if (photoPath.startsWith("http")) {
                // Es una URL
                imageUri = Uri.parse(photoPath);
            } else {
                // Es un archivo local
                File imageFile = new File(photoPath);
                if (imageFile.exists()) {
                    imageUri = Uri.fromFile(imageFile);
                } else {
                    // Intentar usar el path como URI directamente
                    imageUri = Uri.parse(photoPath);
                }
            }
            
            FullscreenImageDialog dialog = new FullscreenImageDialog(context, imageUri);
            dialog.show();
        });

        // Configurar el clic largo para eliminar la imagen
        holder.imageView.setOnLongClickListener(v -> {
            if (position < photos.size()) {
                removePhoto(position);
                if (onPhotoListener != null) {
                    onPhotoListener.onPhotoClick(position);
                }
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ProgressBar progressUpload;
        ImageView statusIcon;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivPhoto);
            progressUpload = itemView.findViewById(R.id.progressUpload);
            statusIcon = itemView.findViewById(R.id.ivStatus);
        }
    }

    public interface OnPhotoListener {
        void onPhotoClick(int position);
    }
}