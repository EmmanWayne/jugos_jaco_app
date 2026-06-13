package com.jugos_jaco_app.ui.adapters;

import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
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
    private OnMovementTypeChangeListener movementListener; // Listener for movement type changes

    public interface OnMovementTypeChangeListener {
        void onMovementTypeChanged(CartItem item);
    }

    public void setOnMovementTypeChangeListener(OnMovementTypeChangeListener listener) {
        this.movementListener = listener;
    }
    
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
    
    /**
     * Attaches swipe-to-reveal gesture handling to the cart RecyclerView.
     * The foreground card slides left to reveal two tappable action buttons.
     */
    public void attachSwipe(RecyclerView recyclerView) {
        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            private float startX;
            private int swipedPosition = RecyclerView.NO_POSITION;
            private boolean isSwiping = false;
            private boolean isSwipeLeft = false;
            private static final float SWIPE_INTERCEPT_DP = 20;
            private static final float MAX_SLIDE_DP = 220;
            private static final float REVEAL_THRESHOLD_DP = 130;
            private static final float CLEAR_THRESHOLD_DP = 80;
            private float density;

            private float dpToPx(float dp, RecyclerView rv) {
                return dp * rv.getResources().getDisplayMetrics().density;
            }

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                density = rv.getResources().getDisplayMetrics().density;
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = e.getX();
                        View child = rv.findChildViewUnder(e.getX(), e.getY());
                        swipedPosition = child != null ? rv.getChildAdapterPosition(child) : RecyclerView.NO_POSITION;
                        isSwiping = false;
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        if (!isSwiping && swipedPosition != RecyclerView.NO_POSITION) {
                            float deltaX = startX - e.getX();
                            if (Math.abs(deltaX) > dpToPx(SWIPE_INTERCEPT_DP, rv)) {
                                isSwiping = true;
                                isSwipeLeft = deltaX > 0;
                                ViewHolder holder = (ViewHolder) rv.findViewHolderForAdapterPosition(swipedPosition);
                                if (holder != null) {
                                    holder.onSwipeStarted();
                                }
                                return true;
                            }
                        }
                        return false;
                    default:
                        return false;
                }
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                ViewHolder holder = (ViewHolder) rv.findViewHolderForAdapterPosition(swipedPosition);
                if (holder == null) return;

                switch (e.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = startX - e.getX();
                        if (isSwipeLeft) {
                            if (deltaX > 0) {
                                holder.cardForeground.setTranslationX(-Math.min(deltaX, dpToPx(MAX_SLIDE_DP, rv)));
                            }
                        } 
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        float totalSwipe = startX - e.getX();
                        if (isSwipeLeft) {
                            if (totalSwipe > dpToPx(REVEAL_THRESHOLD_DP, rv)) {
                                holder.revealButtons(density);
                            } else {
                                holder.snapBack();
                            }
                        } else {
                            if (totalSwipe < -dpToPx(CLEAR_THRESHOLD_DP, rv) && holder.itemView.findViewById(R.id.btnSwipeClear) != null) {
                                holder.clearMovementType();
                            } else {
                                holder.snapBack();
                            }
                        }
                        isSwiping = false;
                        swipedPosition = RecyclerView.NO_POSITION;
                        break;
                }
            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View cardForeground;
        TextView tvName, tvPrice, tvSubtotal, tvMovementBadge;
        ImageButton btnRemove;
        com.google.android.material.button.MaterialButton btnIncrease, btnDecrease;
        TextInputEditText etQuantity;
        com.google.android.material.button.MaterialButton btnSwipeRegalia;
        com.google.android.material.button.MaterialButton btnSwipeCambio;
        com.google.android.material.button.MaterialButton btnSwipeClear;
        android.text.TextWatcher currentTextWatcher;
        private boolean isRevealed = false;
        
        ViewHolder(View itemView) {
            super(itemView);
            cardForeground = itemView.findViewById(R.id.cardForeground);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
            tvMovementBadge = itemView.findViewById(R.id.tvMovementBadge);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            etQuantity = itemView.findViewById(R.id.etQuantity);
            
            btnSwipeRegalia = itemView.findViewById(R.id.btnSwipeRegalia);
            btnSwipeCambio  = itemView.findViewById(R.id.btnSwipeCambio);
            btnSwipeClear   = itemView.findViewById(R.id.btnSwipeClear);
            
            btnSwipeRegalia.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                CartItem item = cartItems.get(pos);
                item.setMovementType("royalty");
                snapBack();
                notifyItemChanged(pos);
                notifyTotalUpdate();
                if (movementListener != null) movementListener.onMovementTypeChanged(item);
            });
            
            btnSwipeCambio.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                CartItem item = cartItems.get(pos);
                item.setMovementType("change");
                snapBack();
                notifyItemChanged(pos);
                notifyTotalUpdate();
                if (movementListener != null) movementListener.onMovementTypeChanged(item);
            });

            btnSwipeClear.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                CartItem item = cartItems.get(pos);
                item.setMovementType(null);
                snapBack();
                notifyItemChanged(pos);
                notifyTotalUpdate();
                if (movementListener != null) movementListener.onMovementTypeChanged(item);
            });
        }
        
        void bind(CartItem item) {
            // Remover el watcher anterior si existe para evitar updates fantasma
            if (currentTextWatcher != null) {
                etQuantity.removeTextChangedListener(currentTextWatcher);
            }

            cardForeground.setTranslationX(0);
            isRevealed = false;
            setSwipeButtonsEnabled(false);

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
            currentTextWatcher = new android.text.TextWatcher() {
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
                        // Verificar que la cantidad sea mayor que 0
                        if (newQuantity > 0) {
                            // Limitar la cantidad al máximo disponible
                            if (newQuantity > item.getProduct().getQuantity()) {
                                newQuantity = item.getProduct().getQuantity();
                                // IMPORTANTE: Remover listener temporalmente para evitar loop infinito
                                etQuantity.removeTextChangedListener(this);
                                etQuantity.setText(String.valueOf(newQuantity));
                                etQuantity.setSelection(etQuantity.length());
                                etQuantity.addTextChangedListener(this);
                            }
                            
                            // Solo actualizar si el valor cambió realmente
                            if (item.getQuantity() != newQuantity) {
                                item.setQuantity(newQuantity);
                                updateSubtotal(item);
                                notifyTotalUpdate();
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Remover listener temporalmente
                        etQuantity.removeTextChangedListener(this);
                        etQuantity.setText(String.valueOf(item.getQuantity()));
                        etQuantity.setSelection(etQuantity.length());
                        etQuantity.addTextChangedListener(this);
                    }
                }
            };
            etQuantity.addTextChangedListener(currentTextWatcher);
            
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
            
            // Handle movement type badge
            String movementType = item.getMovementType();
            if (movementType != null) {
                tvMovementBadge.setVisibility(View.VISIBLE);
                if (movementType.equals("royalty")) {
                    tvMovementBadge.setText("Regal\u00eda");
                    tvMovementBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                } else if (movementType.equals("change")) {
                    tvMovementBadge.setText("Cambio");
                    tvMovementBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
                }
            } else {
                tvMovementBadge.setVisibility(View.GONE);
            }

            // Override price display for movement type items
            if (item.hasMovementType()) {
                tvPrice.setText("L. 0.00");
            }
        }
        
        private void updateSubtotal(CartItem item) {
            double subtotal;
            if (item.hasMovementType()) {
                subtotal = 0;
            } else {
                subtotal = item.getQuantity() * item.getProduct().getPrice();
            }
            tvSubtotal.setText(String.format("L. %.2f", subtotal));
        }

        void onSwipeStarted() {
            isRevealed = false;
        }

        void revealButtons(float density) {
            isRevealed = true;
            setSwipeButtonsEnabled(true);
            animateTranslationX((int)(-175 * density));
        }

        void clearMovementType() {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            CartItem item = cartItems.get(pos);
            item.setMovementType(null);
            snapBack();
            notifyItemChanged(pos);
            notifyTotalUpdate();
            if (movementListener != null) movementListener.onMovementTypeChanged(item);
        }

        void snapBack() {
            isRevealed = false;
            setSwipeButtonsEnabled(false);
            animateTranslationX(0);
        }

        private void setSwipeButtonsEnabled(boolean enabled) {
            btnSwipeRegalia.setEnabled(enabled);
            btnSwipeCambio.setEnabled(enabled);
            btnSwipeClear.setEnabled(enabled);
        }

        private void animateTranslationX(int targetX) {
            float currentX = cardForeground.getTranslationX();
            ValueAnimator animator = ValueAnimator.ofFloat(currentX, targetX);
            animator.setDuration(200);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                cardForeground.setTranslationX((Float) animation.getAnimatedValue());
            });
            animator.start();
        }
    }
}
