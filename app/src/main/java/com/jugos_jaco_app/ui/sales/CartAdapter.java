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
    private OnTotalUpdateListener totalListener;
    
    public interface OnCartUpdateListener {
        void onCartItemRemoved(CartItem item);
        void onCartUpdated();
        void onKeyboardShowing();
        void onKeyboardHiding();
    }

    public interface OnTotalUpdateListener {
        void onTotalUpdate();
    }
    
    public CartAdapter(List<CartItem> cartItems, OnTotalUpdateListener totalListener) {
        this.cartItems = cartItems;
        this.totalListener = totalListener;
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
            
            // Manejar la tecla Done
            etQuantity.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String text = etQuantity.getText().toString();
                    if (text.isEmpty() || text.equals("0")) {
                        etQuantity.setText("1");
                        item.setQuantity(1);
                        updateSubtotal(item);
                        notifyTotalUpdate();
                    }
                    etQuantity.clearFocus();
                    // Notificar que el teclado se ocultará
                    if (listener != null) listener.onKeyboardHiding();
                    return true;
                }
                return false;
            });
            
            // Configurar el EditText
            etQuantity.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    // Notificar al fragment que el teclado se mostrará
                    if (listener != null) listener.onKeyboardShowing();
                } else {
                    // Validar al perder el foco
                    String text = etQuantity.getText().toString();
                    if (text.isEmpty() || text.equals("0")) {
                        etQuantity.setText("1");
                        item.setQuantity(1);
                        updateSubtotal(item);
                        notifyTotalUpdate();
                    }
                    // Notificar al fragment que el teclado se ocultará
                    if (listener != null) listener.onKeyboardHiding();
                }
            });
            
            // Agregar listener para detectar cuando se oculta el teclado con el botón atrás
            etQuantity.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                    etQuantity.clearFocus();
                    if (listener != null) listener.onKeyboardHiding();
                    return true;
                }
                return false;
            });
            
            // Manejar cambios en el texto
            etQuantity.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    String text = s.toString();
                    if (text.isEmpty()) {
                        return; // Permitir que esté vacío temporalmente mientras se edita
                    }
                    try {
                        int newQuantity = Integer.parseInt(text);
                        if (newQuantity > 0) {
                            item.setQuantity(newQuantity);
                            updateSubtotal(item);
                            notifyTotalUpdate();
                        }
                    } catch (NumberFormatException e) {
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
                notifyTotalUpdate();
            });
            
            btnDecrease.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    item.decrementQuantity();
                    etQuantity.setText(String.valueOf(item.getQuantity()));
                    updateSubtotal(item);
                    notifyTotalUpdate();
                }
            });
            
            btnRemove.setOnClickListener(v -> {
                cartItems.remove(getAdapterPosition());
                notifyItemRemoved(getAdapterPosition());
                if (listener != null) listener.onCartItemRemoved(item);
            });
        }
        
        private void updateSubtotal(CartItem item) {
            double subtotal = item.getQuantity() * item.getProduct().getPrice();
            tvSubtotal.setText(String.format("L. %.2f", subtotal));
        }
    }

    public void setOnCartUpdateListener(OnCartUpdateListener listener) {
        this.listener = listener;
    }

    private void notifyTotalUpdate() {
        if (listener != null) listener.onCartUpdated();
        if (totalListener != null) totalListener.onTotalUpdate();
    }
} 