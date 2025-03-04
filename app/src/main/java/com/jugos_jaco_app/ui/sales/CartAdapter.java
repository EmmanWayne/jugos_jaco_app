package com.jugos_jaco_app.ui.sales;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.EditText;
import androidx.recyclerview.widget.RecyclerView;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.models.CartItem;
import java.util.List;
import android.view.inputmethod.EditorInfo;
import com.google.android.material.textfield.TextInputEditText;

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
        TextView tvName, tvPrice, tvSubtotal;
        ImageButton btnRemove;
        com.google.android.material.button.MaterialButton btnIncrease, btnDecrease;
        TextInputEditText etQuantity;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            etQuantity = itemView.findViewById(R.id.etQuantity);
        }
        
        void bind(CartItem item) {
            tvName.setText(item.getProduct().getName());
            etQuantity.setText(String.valueOf(item.getQuantity()));
            tvPrice.setText(String.format("L. %.2f", item.getProduct().getPrice()));
            updateSubtotal(item);
            
            // Manejar cambios en el texto
            etQuantity.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    try {
                        String text = s.toString();
                        if (!text.isEmpty()) {
                            int newQuantity = Integer.parseInt(text);
                            if (newQuantity > 0) {
                                item.setQuantity(newQuantity);
                                updateSubtotal(item);
                                if (listener != null) listener.onCartUpdated();
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Si no es un número válido, restaurar el valor anterior
                        etQuantity.setText(String.valueOf(item.getQuantity()));
                        etQuantity.setSelection(etQuantity.length());
                    }
                }
            });
            
            // Botones de incremento/decremento
            btnIncrease.setOnClickListener(v -> {
                item.incrementQuantity();
                etQuantity.setText(String.valueOf(item.getQuantity()));
                updateSubtotal(item);
                if (listener != null) listener.onCartUpdated();
            });
            
            btnDecrease.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    item.decrementQuantity();
                    etQuantity.setText(String.valueOf(item.getQuantity()));
                    updateSubtotal(item);
                    if (listener != null) listener.onCartUpdated();
                }
            });
            
            btnRemove.setOnClickListener(v -> {
                cartItems.remove(getAdapterPosition());
                notifyItemRemoved(getAdapterPosition());
                if (listener != null) listener.onCartUpdated();
            });
            
            // Mantener el resto del código existente...
            etQuantity.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    try {
                        int newQuantity = Integer.parseInt(etQuantity.getText().toString());
                        if (newQuantity > 0) {
                            item.setQuantity(newQuantity);
                            updateSubtotal(item);
                            if (listener != null) listener.onCartUpdated();
                        } else {
                            etQuantity.setText(String.valueOf(item.getQuantity()));
                        }
                    } catch (NumberFormatException e) {
                        etQuantity.setText(String.valueOf(item.getQuantity()));
                    }
                    return true;
                }
                return false;
            });
        }
        
        private void updateSubtotal(CartItem item) {
            double subtotal = item.getQuantity() * item.getProduct().getPrice();
            tvSubtotal.setText(String.format("L. %.2f", subtotal));
        }
    }
} 