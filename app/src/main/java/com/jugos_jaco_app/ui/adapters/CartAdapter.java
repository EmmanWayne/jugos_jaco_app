package com.jugos_jaco_app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.models.CartItem;
import java.util.List;
import android.view.inputmethod.EditorInfo;

/**
 * Adaptador para mostrar los items en el carrito de compras.
 * Maneja la interacción con cantidades y cálculo de subtotales.
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    
    private List<CartItem> cartItems;           // Lista de items en el carrito
    private OnCartUpdateListener listener;      // Listener para eventos del carrito
    private OnTotalUpdateListener totalListener; // Listener para actualizar total
    
    /**
     * Interface para manejar eventos del carrito
     */
    public interface OnCartUpdateListener {
        void onCartItemRemoved(CartItem item);  // Cuando se elimina un item
        void onCartUpdated();                   // Cuando cambia una cantidad
        void onKeyboardShowing();               // Cuando aparece el teclado
        void onKeyboardHiding();                // Cuando se oculta el teclado
    }

    /**
     * Interface para actualizar el total de la venta
     */
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

    public void setOnCartUpdateListener(OnCartUpdateListener listener) {
        this.listener = listener;
    }

    /**
     * Notifica cambios en el total a los listeners correspondientes
     */
    private void notifyTotalUpdate() {
        if (listener != null) listener.onCartUpdated();
        if (totalListener != null) totalListener.onTotalUpdate();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvSubtotal;
        ImageButton btnRemove;
        com.google.android.material.button.MaterialButton btnIncrease, btnDecrease;
        TextInputEditText etQuantity;
        
        ViewHolder(View itemView) {
            super(itemView);
            // Inicializar vistas
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            etQuantity = itemView.findViewById(R.id.etQuantity);
        }
        
        void bind(CartItem item) {
            // Configurar datos básicos
            tvName.setText(item.getProduct().getName());
            etQuantity.setText(String.valueOf(item.getQuantity()));
            tvPrice.setText(String.format("L. %.2f", item.getProduct().getPrice()));
            updateSubtotal(item);
            
            // Agregar listener para detectar cuando se oculta el teclado con el botón atrás
            etQuantity.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                    etQuantity.clearFocus();
                    if (listener != null) {
                        listener.onKeyboardHiding();
                    }
                    return true;
                }
                return false;
            });

            // Configurar el EditText
            etQuantity.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    if (listener != null) {
                        listener.onKeyboardShowing();
                    }
                } else {
                    // Validar al perder el foco
                    String text = etQuantity.getText().toString();
                    if (text.isEmpty() || text.equals("0")) {
                        etQuantity.setText("1");
                        item.setQuantity(1);
                        updateSubtotal(item);
                        notifyTotalUpdate();
                    }
                    // Notificar que el teclado se ocultará
                    if (listener != null) {
                        listener.onKeyboardHiding();
                    }
                }
            });

            // Configurar para que el teclado se cierre al presionar Done
            etQuantity.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    etQuantity.clearFocus();
                    return true;
                }
                return false;
            });

            // Manejar click en el EditText
            etQuantity.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onKeyboardShowing();
                    etQuantity.requestFocus();
                    etQuantity.setSelection(etQuantity.length());
                }
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
                        return; // Permitir que esté vacío temporalmente
                    }
                    try {
                        int newQuantity = Integer.parseInt(text);
                        // Verificar que la cantidad sea mayor que 0 y no exceda la cantidad disponible
                        if (newQuantity > 0) {
                            // Limitar la cantidad al máximo disponible
                            if (newQuantity > item.getProduct().getQuantity()) {
                                newQuantity = item.getProduct().getQuantity();
                                etQuantity.setText(String.valueOf(newQuantity));
                                etQuantity.setSelection(etQuantity.length());
                            }
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
            
            // Configurar botones de incremento/decremento
            btnIncrease.setOnClickListener(v -> {
                // Verificar si la cantidad actual es menor que la cantidad asignada disponible
                if (item.getQuantity() < item.getProduct().getQuantity()) {
                    item.incrementQuantity();
                    etQuantity.setText(String.valueOf(item.getQuantity()));
                    updateSubtotal(item);
                    notifyTotalUpdate();
                }
            });
            
            btnDecrease.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    item.decrementQuantity();
                    etQuantity.setText(String.valueOf(item.getQuantity()));
                    updateSubtotal(item);
                    notifyTotalUpdate();
                }
            });
            
            // Configurar botón de eliminar
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
}
