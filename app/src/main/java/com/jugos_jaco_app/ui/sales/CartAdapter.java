package com.jugos_jaco_app.ui.sales;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.models.CartItem;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    
    private List<CartItem> cartItems;
    private OnCartUpdateListener listener;
    
    public interface OnCartUpdateListener {
        void onCartUpdated();
    }
    
    public CartAdapter(List<CartItem> cartItems, OnCartUpdateListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return cartItems.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvPrice, tvSubtotal;
        ImageButton btnIncrease, btnDecrease, btnRemove;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
        
        void bind(CartItem item) {
            tvName.setText(item.getProduct().getName());
            tvQuantity.setText(String.valueOf(item.getQuantity()));
            tvPrice.setText(String.format("L. %.2f", item.getProduct().getPrice()));
            tvSubtotal.setText(String.format("L. %.2f", 
                item.getQuantity() * item.getProduct().getPrice()));
            
            btnIncrease.setOnClickListener(v -> {
                item.incrementQuantity();
                notifyItemChanged(getAdapterPosition());
                if (listener != null) listener.onCartUpdated();
            });
            
            btnDecrease.setOnClickListener(v -> {
                item.decrementQuantity();
                notifyItemChanged(getAdapterPosition());
                if (listener != null) listener.onCartUpdated();
            });
            
            btnRemove.setOnClickListener(v -> {
                cartItems.remove(getAdapterPosition());
                notifyItemRemoved(getAdapterPosition());
                if (listener != null) listener.onCartUpdated();
            });
        }
    }
} 