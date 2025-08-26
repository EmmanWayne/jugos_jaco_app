package com.jugos_jaco_app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.models.SaleDetail;

import java.util.List;
import java.util.Locale;

public class SaleDetailAdapter extends RecyclerView.Adapter<SaleDetailAdapter.ViewHolder> {

    private final List<SaleDetail> saleDetailList;

    public SaleDetailAdapter(List<SaleDetail> saleDetailList) {
        this.saleDetailList = saleDetailList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sale_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SaleDetail detail = saleDetailList.get(position);
        holder.bind(detail);
    }

    @Override
    public int getItemCount() {
        return saleDetailList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProductName;
        private final TextView tvProductCode;
        private final TextView tvQuantity;
        private final TextView tvUnitPrice;
        private final TextView tvSubtotal;
        private final TextView tvTax;
        private final TextView tvTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductCode = itemView.findViewById(R.id.tvProductCode);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvUnitPrice = itemView.findViewById(R.id.tvUnitPrice);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
            tvTax = itemView.findViewById(R.id.tvTax);
            tvTotal = itemView.findViewById(R.id.tvTotal);
        }

        public void bind(SaleDetail detail) {
            tvProductName.setText(detail.getProductName());
            tvProductCode.setText(detail.getProductCode());
            
            // Mostrar cantidad con unidad
            String quantityText = String.format(Locale.getDefault(), "%d %s", 
                    detail.getQuantity(), detail.getUnitAbbreviation());
            tvQuantity.setText(quantityText);
            
            // Calcular precio unitario (subtotal / cantidad)
            double unitPrice = detail.getLineSubtotal() / detail.getQuantity();
            tvUnitPrice.setText(String.format(Locale.getDefault(), "L. %.2f", unitPrice));
            
            // Mostrar subtotal, impuesto y total
            tvSubtotal.setText(String.format(Locale.getDefault(), "L. %.2f", detail.getLineSubtotal()));
            tvTax.setText(String.format(Locale.getDefault(), "L. %.2f", detail.getLineTaxAmount()));
            tvTotal.setText(String.format(Locale.getDefault(), "L. %.2f", detail.getLineTotal()));
        }
    }
}