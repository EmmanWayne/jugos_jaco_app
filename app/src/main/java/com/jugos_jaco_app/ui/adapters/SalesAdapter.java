package com.jugos_jaco_app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.models.Sale;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para mostrar la lista de ventas en un RecyclerView.
 * Implementa Filterable para permitir búsqueda por nombre de cliente.
 */
public class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.ViewHolder> implements Filterable {
    
    private List<Sale> sales;
    private List<Sale> salesFull; // Lista completa para filtrado
    private OnSaleClickListener listener;
    
    /**
     * Interface para manejar clics en las ventas.
     */
    public interface OnSaleClickListener {
        void onSaleClick(Sale sale);
    }
    
    public SalesAdapter(List<Sale> sales, OnSaleClickListener listener) {
        this.sales = sales;
        this.salesFull = new ArrayList<>(sales); // Copia para filtrado
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sale, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Sale sale = sales.get(position);
        holder.bind(sale, position);
    }
    
    @Override
    public int getItemCount() {
        return sales.size();
    }
    
    /**
     * Actualiza la lista de ventas y notifica cambios.
     */
    public void updateSales(List<Sale> newSales) {
        this.sales.clear();
        this.sales.addAll(newSales);
        this.salesFull.clear();
        this.salesFull.addAll(newSales); // Actualizar copia para filtrado
        notifyDataSetChanged();
    }
    
    /**
     * Implementación de Filterable para búsqueda por nombre de cliente.
     */
    @Override
    public Filter getFilter() {
        return salesFilter;
    }
    
    private Filter salesFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Sale> filteredList = new ArrayList<>();
            
            if (constraint == null || constraint.length() == 0) {
                // Si no hay texto de búsqueda, mostrar toda la lista
                filteredList.addAll(salesFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                
                // Filtrar por nombre de cliente o nombre del negocio
                for (Sale sale : salesFull) {
                    boolean matchesClient = sale.getClientName().toLowerCase().contains(filterPattern);
                    boolean matchesBusiness = sale.getBusinessName() != null && 
                                            sale.getBusinessName().toLowerCase().contains(filterPattern);
                    
                    if (matchesClient || matchesBusiness) {
                        filteredList.add(sale);
                    }
                }
            }
            
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }
        
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            sales.clear();
            sales.addAll((List<Sale>) results.values);
            notifyDataSetChanged();
        }
    };
    
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSaleId, tvSaleDate, tvClientName;
        TextView tvPaymentMethod, tvPaymentTerm;
        TextView tvSubtotal, tvCashAmount, tvTotalAmount;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSaleId = itemView.findViewById(R.id.tvSaleId);
            tvSaleDate = itemView.findViewById(R.id.tvSaleDate);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvPaymentTerm = itemView.findViewById(R.id.tvPaymentTerm);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
            tvCashAmount = itemView.findViewById(R.id.tvCashAmount);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSaleClick(sales.get(position));
                }
            });
        }
        
        void bind(Sale sale, int position) {
            // Configurar ID de venta (ahora muestra el número de índice en la lista)
            tvSaleId.setText(String.format("Venta #%d", position + 1));
            
            // Configurar fecha formateada
            if (sale.getFormattedSaleDate() != null) {
                tvSaleDate.setText(sale.getFormattedSaleDate());
            } else {
                // Formatear la fecha si no viene ya formateada
                tvSaleDate.setText(formatDate(sale.getSaleDate()));
            }
            
            // Configurar nombre del cliente
            String clientInfo = sale.getClientName();
            if (sale.getBusinessName() != null && !sale.getBusinessName().isEmpty() && !sale.getBusinessName().equals("null")) {
                clientInfo += " (" + sale.getBusinessName() + ")";
            }
            tvClientName.setText(clientInfo);
            
            // Configurar método y término de pago
            tvPaymentMethod.setText(sale.getPaymentMethod());
            tvPaymentTerm.setText(sale.getPaymentTerm());
            
            // Configurar montos
            tvSubtotal.setText(String.format(Locale.getDefault(), "L. %.2f", sale.getSubtotal()));
            tvCashAmount.setText(String.format(Locale.getDefault(), "L. %.2f", sale.getCashAmount()));
            tvTotalAmount.setText(String.format(Locale.getDefault(), "L. %.2f", sale.getTotalAmount()));
        }
        
        /**
         * Formatea una fecha en formato ISO (YYYY-MM-DD) a un formato legible.
         */
        private String formatDate(String isoDate) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd 'de' MMMM, yyyy", new Locale("es", "ES"));
                Date date = inputFormat.parse(isoDate);
                return outputFormat.format(date);
            } catch (ParseException e) {
                return isoDate; // Devolver la fecha original si hay error
            }
        }
    }
}