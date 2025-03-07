package com.jugos_jaco_app.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
        private String serverUrl;
        private boolean isUploaded;
        private boolean isUploading;

        public PhotoItem(String path) {
            this.path = path;
            this.isUploaded = false;
            this.isUploading = false;
            this.serverUrl = null;
        }

        public PhotoItem(String path, boolean isUploaded) {
            this.path = path;
            this.isUploaded = isUploaded;
            this.isUploading = false;
            this.serverUrl = null;
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

        public String getServerUrl() {
            return serverUrl;
        }

        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
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
            PhotoItem photo = photos.get(position);
            photo.setUploading(uploading);
            notifyItemChanged(position);
        }
    }

    // Método para marcar una foto como subida
    public void setPhotoUploaded(int position, String serverUrl) {
        if (position >= 0 && position < photos.size()) {
            PhotoItem photo = photos.get(position);
            photo.setUploaded(true);
            photo.setUploading(false);
            photo.setServerUrl(serverUrl);
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
        
        // Cargar imagen...
        loadImage(holder, photoItem);
        
        // Actualizar estados de los indicadores
        updateIndicators(holder, photoItem);

        // Configurar el clic para mostrar la imagen en pantalla completa
        holder.imageView.setOnClickListener(v -> {
            Uri imageUri;
            
            String photoPath = photoItem.getPath();
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

    private void loadImage(PhotoViewHolder holder, PhotoItem photoItem) {
        String photoPath = photoItem.getPath();
        if (photoPath == null) {
            holder.imageView.setImageResource(R.drawable.product_placeholder);
            return;
        }

        if (photoPath.startsWith("http")) {
            Glide.with(holder.imageView.getContext())
                .load(photoPath)
                .placeholder(R.drawable.product_placeholder)
                .error(R.drawable.product_placeholder)
                .into(holder.imageView);
        } else {
            File imgFile = new File(photoPath);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                holder.imageView.setImageBitmap(myBitmap);
            } else {
                holder.imageView.setImageResource(R.drawable.product_placeholder);
            }
        }
    }

    private void updateIndicators(PhotoViewHolder holder, PhotoItem photoItem) {
        // Ocultar todos los indicadores primero
        holder.progressUpload.setVisibility(View.GONE);
        holder.uploadIndicator.setVisibility(View.GONE);
        holder.statusIcon.setVisibility(View.GONE);

        // Mostrar el indicador correspondiente
        if (photoItem.isUploading()) {
            holder.progressUpload.setVisibility(View.VISIBLE);
        } else if (photoItem.isUploaded()) {
            holder.uploadIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.statusIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ProgressBar progressUpload;
        ImageView statusIcon;
        ImageView uploadIndicator;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivPhoto);
            progressUpload = itemView.findViewById(R.id.progressUpload);
            statusIcon = itemView.findViewById(R.id.ivStatus);
            uploadIndicator = itemView.findViewById(R.id.ivUploadIndicator);
        }
    }

    public interface OnPhotoListener {
        void onPhotoClick(int position);
    }
}