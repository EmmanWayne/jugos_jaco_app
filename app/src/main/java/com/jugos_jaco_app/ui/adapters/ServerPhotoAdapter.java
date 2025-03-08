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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerPhotoAdapter extends RecyclerView.Adapter<ServerPhotoAdapter.ViewHolder> {
    private List<ServerPhoto> photos;
    private Context context;
    private Set<Integer> selectedPhotos = new HashSet<>();
    private boolean isSelectionMode = false;

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
        String fullUrl = Utilities.URL_FOTOS +"storage/" +photo.getPath();
        
        Glide.with(context)
            .load(fullUrl)
            .placeholder(R.drawable.product_placeholder)
            .error(R.drawable.product_placeholder)
            .into(holder.imageView);

        // Mostrar indicador de selección si está en modo selección
        holder.selectionOverlay.setVisibility(
            isSelectionMode ? View.VISIBLE : View.GONE
        );
        
        holder.selectionCheck.setVisibility(
            selectedPhotos.contains(photo.getId()) ? View.VISIBLE : View.GONE
        );

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                toggleSelectionMode();
                togglePhotoSelection(position);
                return true;
            }
            return false;
        });

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                togglePhotoSelection(position);
            } else {
                Uri imageUri = Uri.parse(fullUrl);
                FullscreenImageDialog dialog = new FullscreenImageDialog(context, imageUri);
                dialog.show();
            }
        });
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
    }

    public Set<Integer> getSelectedPhotos() {
        return new HashSet<>(selectedPhotos);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        View selectionOverlay;
        View selectionCheck;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivServerPhoto);
            selectionOverlay = itemView.findViewById(R.id.selectionOverlay);
            selectionCheck = itemView.findViewById(R.id.selectionCheck);
        }
    }
} 