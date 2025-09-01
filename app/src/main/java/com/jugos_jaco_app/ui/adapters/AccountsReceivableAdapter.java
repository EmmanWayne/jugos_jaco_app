package com.jugos_jaco_app.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.models.AccountReceivable;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AccountsReceivableAdapter extends RecyclerView.Adapter<AccountsReceivableAdapter.ViewHolder> {

    private List<AccountReceivable> accountsList;
    private List<AccountReceivable> filteredList;
    private final OnAccountClickListener listener;
    private final NumberFormat currencyFormat;
    private final SimpleDateFormat inputDateFormat;
    private final SimpleDateFormat outputDateFormat;

    public interface OnAccountClickListener {
        void onAccountClick(AccountReceivable account);
    }

    public AccountsReceivableAdapter(List<AccountReceivable> accountsList, OnAccountClickListener listener) {
        this.accountsList = new ArrayList<>(accountsList);
        this.filteredList = new ArrayList<>(accountsList);
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "US"));
        this.inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.outputDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account_receivable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AccountReceivable account = filteredList.get(position);
        holder.bind(account);
    }

    @Override
    public int getItemCount() {
        int count = filteredList.size();
        android.util.Log.i("CUENTAS", "getItemCount devuelve: " + count);
        return count;
    }

    public void filterByClient(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(accountsList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (AccountReceivable account : accountsList) {
                if (account.getClientName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(account);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filterByStatus(String status) {
        filteredList.clear();
        if (status.equals("Todos")) {
            filteredList.addAll(accountsList);
        } else {
            for (AccountReceivable account : accountsList) {
                if (account.getStatus().equals(status)) {
                    filteredList.add(account);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateAccounts(List<AccountReceivable> newAccounts) {
        android.util.Log.i("CUENTAS", "updateAccounts llamado con " + newAccounts.size() + " elementos");
        this.accountsList.clear();
        this.accountsList.addAll(newAccounts);
        this.filteredList.clear();
        this.filteredList.addAll(newAccounts);
        android.util.Log.i("CUENTAS", "Después de actualizar - accountsList: " + this.accountsList.size() + ", filteredList: " + this.filteredList.size());
        notifyDataSetChanged();
    }

    private String formatDate(String dateString) {
        try {
            Date date = inputDateFormat.parse(dateString);
            return outputDateFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvClientName;
        private final TextView tvStatus;
        private final TextView tvTotalAmount;
        private final TextView tvRemainingBalance;
        private final TextView tvDueDate;
        private final ProgressBar progressPayment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvRemainingBalance = itemView.findViewById(R.id.tvRemainingBalance);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            progressPayment = itemView.findViewById(R.id.progressPayment);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAccountClick(filteredList.get(position));
                }
            });
        }

        public void bind(AccountReceivable account) {
            tvClientName.setText(account.getClientName());
            tvTotalAmount.setText(currencyFormat.format(account.getTotalAmount()));
            tvRemainingBalance.setText(currencyFormat.format(account.getRemainingBalance()));
            tvDueDate.setText(formatDate(account.getDueDate()));
            
            // Set status with appropriate color
            tvStatus.setText(account.getStatus());
            if (account.isPaid()) {
                tvStatus.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
            } else {
                tvStatus.setBackgroundColor(Color.parseColor("#F44336")); // Red
            }
            
            // Calculate and set payment progress
            if (account.getTotalAmount() > 0) {
                double paidAmount = account.getPaidAmount();
                int progress = (int) ((paidAmount / account.getTotalAmount()) * 100);
                progressPayment.setProgress(progress);
            } else {
                progressPayment.setProgress(0);
            }
            
            // Set progress bar color based on status
            if (account.isPaid()) {
                progressPayment.getProgressDrawable().setColorFilter(
                    Color.parseColor("#4CAF50"), android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                progressPayment.getProgressDrawable().setColorFilter(
                    Color.parseColor("#F44336"), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }
    }
}