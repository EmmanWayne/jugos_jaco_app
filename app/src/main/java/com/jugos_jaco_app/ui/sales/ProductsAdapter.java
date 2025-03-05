package com.jugos_jaco_app.ui.sales;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.models.Product;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {
    
    private List<Product> products;
    private OnProductClickListener listener;
    private Set<String> productsInCart;
    
    public interface OnProductClickListener {
        void onProductClick(Product product);
    }
    
    public ProductsAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
        this.productsInCart = new HashSet<>();
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
    
    public void updateProducts(List<Product> newProducts) {
        products.clear();
        products.addAll(newProducts);
        notifyDataSetChanged();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCode, tvContent, tvCategory, tvInCart;
        MaterialButton btnAdd;
        ImageView ivProduct;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvCode = itemView.findViewById(R.id.tvProductCode);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnAdd = itemView.findViewById(R.id.btnAddToCart);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvInCart = itemView.findViewById(R.id.tvInCart);
        }
        
        void bind(Product product) {
            tvName.setText(product.getName());
            tvCode.setText(product.getCode());
            tvContent.setText(product.getContent());
            tvCategory.setText(product.getCategoryName());
            
            if (productsInCart.contains(product.getId())) {
                btnAdd.setVisibility(View.GONE);
                tvInCart.setVisibility(View.VISIBLE);
            } else {
                btnAdd.setVisibility(View.VISIBLE);
                tvInCart.setVisibility(View.GONE);
            }
            
            btnAdd.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                    setProductInCart(product.getId(), true);
                }
            });
            
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.producto_jaco)
                    .centerCrop()
                    .into(ivProduct);
            }
        }
    }

    public void setProductInCart(String productId, boolean inCart) {
        if (inCart) {
            productsInCart.add(productId);
        } else {
            productsInCart.remove(productId);
        }
        notifyDataSetChanged();
    }

    public void updateCartState(List<String> productIds) {
        productsInCart.clear();
        productsInCart.addAll(productIds);
        notifyDataSetChanged();
    }
} 