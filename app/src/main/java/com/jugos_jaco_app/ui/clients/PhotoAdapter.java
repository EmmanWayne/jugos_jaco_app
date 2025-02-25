package com.jugos_jaco_app.ui.clients;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.jugos_jaco_app.R;
import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private List<String> photos;
    private Context context;
    private OnPhotoListener onPhotoListener;

    public PhotoAdapter(List<String> photos, Context context, OnPhotoListener onPhotoListener) {
        this.photos = new ArrayList<>(photos); // Crear una copia de la lista
        this.context = context;
        this.onPhotoListener = onPhotoListener;
    }

    // Método para actualizar la lista de fotos de forma segura
    public void updatePhotos(List<String> newPhotos) {
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

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        String photoPath = photos.get(position);
        Uri imageUri = Uri.parse(photoPath);

        // Cargar la imagen usando Glide
        Glide.with(context)
                .load(imageUri)
                .centerCrop()
                .into(holder.imageView);

        // Configurar el clic para mostrar la imagen en pantalla completa
        holder.imageView.setOnClickListener(v -> {
            FullscreenImageDialog dialog = new FullscreenImageDialog(context, imageUri);
            dialog.show();
        });

        // Configurar el clic largo para eliminar la imagen
        holder.imageView.setOnLongClickListener(v -> {
            if (position < photos.size()) { // Verificar que la posición sea válida
                onPhotoListener.onPhotoClick(position);
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

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivPhoto);
        }
    }

    public interface OnPhotoListener {
        void onPhotoClick(int position);
    }
}