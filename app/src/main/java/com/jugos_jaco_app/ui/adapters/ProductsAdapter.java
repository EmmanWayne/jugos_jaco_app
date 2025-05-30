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
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_product, parent, false);
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
            tvCode = itemView.findViewById(R.id.tvProductCode);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnAdd = itemView.findViewById(R.id.btnAddToCart);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvInCart = itemView.findViewById(R.id.tvInCart);
            tvQuantity = itemView.findViewById(R.id.tvQuantity); // Asegúrate que exista en el layout
        }
        
        void bind(Product product) {
            // Configurar datos del producto
            tvName.setText(product.getName());
            tvCode.setText(product.getCode());
            tvContent.setText(product.getContent());
            tvCategory.setText(product.getCategoryName());
            
            // Mostrar cantidad
            if (tvQuantity != null) {
                tvQuantity.setText("Cantidad: " + product.getQuantity());
            }
            // Si está en modo productos asignados, ocultar el botón de agregar
            if (isAssignedMode) {
                btnAdd.setVisibility(View.GONE);
                tvInCart.setVisibility(View.GONE);
            } else {
                // Manejar estado visual (en carrito/no en carrito)
                if (productsInCart.contains(product.getId())) {
                    btnAdd.setVisibility(View.GONE);
                    tvInCart.setVisibility(View.VISIBLE);
                } else {
                    btnAdd.setVisibility(View.VISIBLE);
                    tvInCart.setVisibility(View.GONE);
                }
            }
            
            // Configurar click listener
            btnAdd.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
            
            // Cargar imagen con Glide
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.producto_jaco)
                    .centerCrop()
                    .into(ivProduct);
            }
        }
    }
} 