package com.jugos_jaco_app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.models.Payment;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para mostrar la lista de pagos en el RecyclerView.
 */
public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {

    private List<Payment> payments;
    private DecimalFormat decimalFormat;
    private SimpleDateFormat inputDateFormat;
    private SimpleDateFormat outputDateFormat;

    public PaymentAdapter(List<Payment> payments) {
        this.payments = new ArrayList<>(payments);
        this.decimalFormat = new DecimalFormat("#,##0.00");
        this.inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.outputDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = payments.get(position);
        holder.bind(payment);
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    /**
     * Actualiza la lista de pagos.
     */
    public void updatePayments(List<Payment> newPayments) {
        this.payments.clear();
        this.payments.addAll(newPayments);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder para cada item de pago.
     */
    public class PaymentViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPaymentAmount;
        private TextView tvBalanceAfterPayment;
        private TextView tvPaymentDate;
        private TextView tvPaymentMethod;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPaymentAmount = itemView.findViewById(R.id.tvPaymentAmount);
            tvBalanceAfterPayment = itemView.findViewById(R.id.tvBalanceAfterPayment);
            tvPaymentDate = itemView.findViewById(R.id.tvPaymentDate);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
        }

        public void bind(Payment payment) {
            // Formatear monto del pago
            tvPaymentAmount.setText("L. " + decimalFormat.format(payment.getAmount()));
            
            // Formatear saldo después del pago
            tvBalanceAfterPayment.setText("L. " + decimalFormat.format(payment.getBalanceAfterPayment()));
            
            // Formatear fecha de pago
            String formattedDate = formatDate(payment.getPaymentDate());
            tvPaymentDate.setText(formattedDate);
            
            // Método de pago
            tvPaymentMethod.setText(payment.getPaymentMethod());
        }

        /**
         * Convierte la fecha del formato yyyy-MM-dd a dd/MM/yyyy.
         */
        private String formatDate(String dateString) {
            try {
                Date date = inputDateFormat.parse(dateString);
                return outputDateFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return dateString; // Retorna la fecha original si hay error
            }
        }
    }
}