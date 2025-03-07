package com.jugos_jaco_app.ui.adapters;

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
import com.jugos_jaco_app.ui.api.ServerPhotosResponse.ServerPhoto;
import com.jugos_jaco_app.ui.clients.FullscreenImageDialog;
import com.jugos_jaco_app.ui.utilities.Utilities;
import java.util.List;

public class ServerPhotoAdapter extends RecyclerView.Adapter<ServerPhotoAdapter.ViewHolder> {
    private List<ServerPhoto> photos;
    private Context context;

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
        String fullUrl = Utilities.URL + photo.getPath();
        
        Glide.with(context)
            .load(fullUrl)
            .placeholder(R.drawable.product_placeholder)
            .error(R.drawable.product_placeholder)
            .into(holder.imageView);

        // Configurar clic para ver imagen en pantalla completa
        holder.imageView.setOnClickListener(v -> {
            Uri imageUri = Uri.parse(fullUrl);
            FullscreenImageDialog dialog = new FullscreenImageDialog(context, imageUri);
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivServerPhoto);
        }
    }
} 