package com.jugos_jaco_app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.models.Product;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Adaptador para mostrar la lista de productos en un RecyclerView.
 * Maneja la visualización de productos y su estado (en carrito/no en carrito).
 */
public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {
    
    private List<Product> products;              // Lista de productos a mostrar
    private OnProductClickListener listener;     // Listener para clicks en productos
    private Set<String> productsInCart;         // Set de IDs de productos en carrito
    private boolean isAssignedMode;             // Indica si estamos en modo productos asignados

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }
    
    public ProductsAdapter(List<Product> products, OnProductClickListener listener, boolean isAssignedMode) {
        this.products = products;
        this.listener = listener;
        this.productsInCart = new HashSet<>();
        this.isAssignedMode = isAssignedMode;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (isAssignedMode) {
            // Usar layout de lista para modo asignado
            view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_list, parent, false);
        } else {
            // Usar layout de grid para modo normal
            view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        }
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }
    
    @Override
    public int getItemCount() {
        return products.size();
    }

    /**
     * Actualiza la lista de productos y notifica cambios en la UI.
     * Usado para búsqueda y filtrado.
     */
    public void updateProducts(List<Product> newProducts) {
        products.clear();
        products.addAll(newProducts);
        notifyDataSetChanged();
    }

    /**
     * Marca o desmarca un producto como "en carrito".
     * Actualiza la UI correspondiente.
     */
    public void setProductInCart(String productId, boolean inCart) {
        if (inCart) {
            productsInCart.add(productId);
        } else {
            productsInCart.remove(productId);
        }
        notifyDataSetChanged();
    }

    /**
     * Actualiza el estado de múltiples productos en el carrito.
     * Usado al restaurar el estado.
     */
    public void updateCartState(List<String> productIds) {
        productsInCart.clear();
        productsInCart.addAll(productIds);
        notifyDataSetChanged();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCode, tvContent, tvCategory, tvInCart, tvQuantity;
        MaterialButton btnAdd;
        ImageView ivProduct;
        
        ViewHolder(View itemView) {
            super(itemView);
            // Inicializar vistas
            tvName = itemView.findViewById(R.id.tvProductName);
            // tvCode ya no se usa individualmente
            // tvCode = itemView.findViewById(R.id.tvProductCode);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnAdd = itemView.findViewById(R.id.btnAddToCart);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvInCart = itemView.findViewById(R.id.tvInCart);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }
        
        void bind(Product product) {
            // Configurar datos del producto
            if (tvName != null) {
                // Formato: Codigo - Product Name
                String formattedName = product.getCode() + " - " + product.getName();
                tvName.setText(formattedName);
            }
            // if (tvCode != null) tvCode.setText("Código: " + product.getCode());
            if (tvContent != null) tvContent.setText(product.getContent());
            if (tvCategory != null) tvCategory.setText(product.getCategoryName());
            
            // Mostrar cantidad con badges visuales usando Spannable
            if (tvQuantity != null) {
                android.text.SpannableStringBuilder spannable = new android.text.SpannableStringBuilder();
                int grayColor = android.graphics.Color.GRAY;
                int blueColor = 0xFF1976D2; // Azul para vendidos
                int greenColor = 0xFF388E3C; // Verde para regalías
                int orangeColor = 0xFFF57C00; // Naranja para cambios
                
                // Cantidad
                spannable.append("Cant: ");
                spannable.append(String.valueOf(product.getQuantity()));
                spannable.setSpan(new android.text.style.ForegroundColorSpan(grayColor), 0, spannable.length(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                
                // Stock
                spannable.append(" | Stock: ");
                int stockStart = spannable.length();
                spannable.append(String.valueOf(product.getStock()));
                spannable.setSpan(new android.text.style.ForegroundColorSpan(grayColor), stockStart, spannable.length(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                
                // Vendidos
                if (product.getSold() > 0) {
                    spannable.append(" | Vend: ");
                    int soldStart = spannable.length();
                    spannable.append(String.valueOf(product.getSold()));
                    spannable.setSpan(new android.text.style.ForegroundColorSpan(blueColor), soldStart, spannable.length(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                
                // Regalías
                if (product.getRoyaltiesQuantity() > 0) {
                    spannable.append(" | Reg: ");
                    int regStart = spannable.length();
                    spannable.append(String.valueOf(product.getRoyaltiesQuantity()));
                    spannable.setSpan(new android.text.style.ForegroundColorSpan(greenColor), regStart, spannable.length(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                
                // Cambios
                if (product.getChangesQuantity() > 0) {
                    spannable.append(" | Cam: ");
                    int camStart = spannable.length();
                    spannable.append(String.valueOf(product.getChangesQuantity()));
                    spannable.setSpan(new android.text.style.ForegroundColorSpan(orangeColor), camStart, spannable.length(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                
                tvQuantity.setText(spannable);
            }
            
            // Si el listener es null, ocultar el botón de agregar (modo solo visualización)
            if (listener == null) {
                btnAdd.setVisibility(View.GONE);
                tvInCart.setVisibility(View.GONE);
            }
            // Si no, manejar estado visual normal (en carrito/no en carrito)
            else if (productsInCart.contains(product.getId())) {
                btnAdd.setVisibility(View.GONE);
                tvInCart.setVisibility(View.VISIBLE);
            } else {
                btnAdd.setVisibility(View.VISIBLE);
                tvInCart.setVisibility(View.GONE);
            }
            
            // Configurar click listener
            btnAdd.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
            
            // Ocultar la imagen del producto como solicitó el usuario
            if (ivProduct != null) {
                ivProduct.setVisibility(View.GONE);
            }
        }
    }
}