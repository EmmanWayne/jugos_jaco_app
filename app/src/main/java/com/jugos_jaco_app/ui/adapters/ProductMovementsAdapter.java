package com.jugos_jaco_app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.models.ProductMovement;
import java.util.ArrayList;
import java.util.List;

public class ProductMovementsAdapter extends RecyclerView.Adapter<ProductMovementsAdapter.ViewHolder> {

    private List<ProductMovement> movements;

    public ProductMovementsAdapter() {
        this.movements = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_movement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductMovement movement = movements.get(position);
        holder.bind(movement);
    }

    @Override
    public int getItemCount() {
        return movements.size();
    }

    public void setMovements(List<ProductMovement> movements) {
        this.movements = movements;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName;
        TextView tvMovementType;
        TextView tvQuantity;
        TextView tvNote;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvMovementType = itemView.findViewById(R.id.tvMovementType);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvNote = itemView.findViewById(R.id.tvNote);
        }

        public void bind(ProductMovement movement) {
            tvProductName.setText(movement.getProductName());
            tvMovementType.setText(movement.getType());
            tvQuantity.setText(String.valueOf(movement.getQuantity()));
            tvNote.setText(movement.getNote() != null && !movement.getNote().isEmpty() ? movement.getNote() : "Sin nota");
        }
    }
}
