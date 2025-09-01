package com.jugos_jaco_app.ui.accounts_receivable;

import static com.jugos_jaco_app.Login.KEY_TOKEN;
import static com.jugos_jaco_app.Login.PREFS_NAME;
import static com.jugos_jaco_app.Login.TOKEN_TYPE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.adapters.PaymentAdapter;
import com.jugos_jaco_app.ui.models.Payment;
import com.jugos_jaco_app.ui.utilities.Utilities;
import com.jugos_jaco_app.ui.utilities.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment para mostrar el detalle de una cuenta por cobrar.
 */
public class AccountReceivableDetailFragment extends Fragment {

    private static final String TAG = "AccountReceivableDetail";
    private static final String ARG_ACCOUNT_ID = "account_id";

    // UI Components
    private TextView tvClientName;
    private TextView tvTotalAmount;
    private TextView tvRemainingBalance;
    private TextView tvDueDate;
    private TextView tvStatus;
    private RecyclerView rvPayments;
    private TextView tvNoPayments;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddPayment;

    // Data
    private int accountId;
    private PaymentAdapter paymentAdapter;
    private List<Payment> paymentsList;

    // Formatters
    private DecimalFormat decimalFormat;
    private SimpleDateFormat inputDateFormat;
    private SimpleDateFormat outputDateFormat;

    public static AccountReceivableDetailFragment newInstance(int accountId) {
        AccountReceivableDetailFragment fragment = new AccountReceivableDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ACCOUNT_ID, accountId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            accountId = getArguments().getInt("account_id", -1);
        }
        
        // Initialize formatters
        decimalFormat = new DecimalFormat("#,##0.00");
        inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        outputDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_receivable_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupRecyclerView();
        setupFloatingActionButton();
        loadAccountDetail();
    }

    private void initializeViews(View view) {
        tvClientName = view.findViewById(R.id.tvClientName);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        tvRemainingBalance = view.findViewById(R.id.tvRemainingBalance);
        tvDueDate = view.findViewById(R.id.tvDueDate);
        tvStatus = view.findViewById(R.id.tvStatus);
        rvPayments = view.findViewById(R.id.rvPayments);
        tvNoPayments = view.findViewById(R.id.tvNoPayments);
        progressBar = view.findViewById(R.id.progressBar);
        fabAddPayment = view.findViewById(R.id.fabAddPayment);
    }

    private void setupRecyclerView() {
        paymentsList = new ArrayList<>();
        paymentAdapter = new PaymentAdapter(paymentsList);
        rvPayments.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPayments.setAdapter(paymentAdapter);
    }

    private void setupFloatingActionButton() {
        fabAddPayment.setOnClickListener(v -> showAddPaymentDialog());
    }

    private void showAddPaymentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_payment, null);
        
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        Spinner spinnerPaymentMethod = dialogView.findViewById(R.id.spinnerPaymentMethod);
        EditText etNotes = dialogView.findViewById(R.id.etNotes);
        
        // Configurar spinner de métodos de pago
        String[] paymentMethods = {"Efectivo", "Depósito"};
        String[] paymentMethodValues = {"cash", "deposit"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, paymentMethods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentMethod.setAdapter(adapter);
        
        builder.setView(dialogView)
                .setTitle("Agregar Abono")
                .setIcon(R.drawable.ic_add)
                .setPositiveButton("Agregar", (dialog, which) -> {
                    String amountStr = etAmount.getText().toString().trim();
                    String notes = etNotes.getText().toString().trim();
                    int selectedPosition = spinnerPaymentMethod.getSelectedItemPosition();
                    
                    if (TextUtils.isEmpty(amountStr)) {
                        Toast.makeText(requireContext(), "Por favor ingrese el monto", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    try {
                        double amount = Double.parseDouble(amountStr);
                        if (amount <= 0) {
                            Toast.makeText(requireContext(), "El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        String paymentMethod = paymentMethodValues[selectedPosition];
                        createPayment(amount, paymentMethod, notes);
                        
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Monto inválido", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void createPayment(double amount, String paymentMethod, String notes) {
        showLoading(true);
        
        String url = Utilities.URL + "account-receivable/" + accountId + "/payments";
        Log.d(TAG, "Creando abono: " + url);
        
        try {
            JSONObject paymentData = new JSONObject();
            paymentData.put("amount", amount);
            paymentData.put("payment_method", paymentMethod);
            paymentData.put("notes", notes);
            
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    paymentData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "Abono creado exitosamente: " + response.toString());
                            Toast.makeText(requireContext(), "Abono agregado exitosamente", Toast.LENGTH_SHORT).show();
                            // Recargar los datos del detalle para mostrar el nuevo abono
                            loadAccountDetail();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error al crear abono: " + error.getMessage());
                            String errorMessage = "Error al agregar el abono";
                            if (error.networkResponse != null && error.networkResponse.data != null) {
                                try {
                                    String errorBody = new String(error.networkResponse.data);
                                    JSONObject errorJson = new JSONObject(errorBody);
                                    if (errorJson.has("message")) {
                                        errorMessage = errorJson.getString("message");
                                    }
                                } catch (JSONException e) {
                                    // Usar mensaje por defecto
                                }
                            }
                            showError(errorMessage);
                            showLoading(false);
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", getAuthorizationHeader(requireContext()));
                    headers.put("Accept", "application/json");
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
            
            VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
            
        } catch (JSONException e) {
            Log.e(TAG, "Error al crear JSON: " + e.getMessage());
            showError("Error al procesar los datos");
            showLoading(false);
        }
    }

    private void loadAccountDetail() {
        showLoading(true);
        
        String url = Utilities.URL + "account-receivable/" + accountId;
        Log.d(TAG, "Cargando detalle de cuenta: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Respuesta recibida: " + response.toString());
                        parseAccountDetail(response);
                        showLoading(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error al cargar detalle: " + error.getMessage());
                        showError("Error al cargar el detalle de la cuenta");
                        showLoading(false);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", getAuthorizationHeader(requireContext()));
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    private void parseAccountDetail(JSONObject response) {
        try {
            JSONObject data = response.getJSONObject("data");
            
            // Información básica de la cuenta
            String clientName = data.getString("client_name");
            double totalAmount = data.getDouble("total_amount");
            double remainingBalance = data.getDouble("remaining_balance");
            String dueDate = data.getString("due_date");
            String status = data.getString("status");
            
            // Actualizar UI con información de la cuenta
            updateAccountInfo(clientName, totalAmount, remainingBalance, dueDate, status);
            
            // Parsear pagos
            JSONArray paymentsArray = data.getJSONArray("payments");
            List<Payment> payments = new ArrayList<>();
            
            for (int i = 0; i < paymentsArray.length(); i++) {
                JSONObject paymentObj = paymentsArray.getJSONObject(i);
                
                Payment payment = new Payment();
                payment.setId(paymentObj.getInt("id"));
                payment.setAmount(paymentObj.getDouble("amount"));
                payment.setBalanceAfterPayment(paymentObj.getDouble("balance_after_payment"));
                payment.setPaymentDate(paymentObj.getString("payment_date"));
                payment.setPaymentMethod(paymentObj.getString("payment_method"));
                
                payments.add(payment);
            }
            
            // Actualizar lista de pagos
            updatePaymentsList(payments);
            
            Log.d(TAG, "Detalle cargado exitosamente. Pagos: " + payments.size());
            
        } catch (JSONException e) {
            Log.e(TAG, "Error al parsear respuesta: " + e.getMessage());
            showError("Error al procesar los datos");
        }
    }

    private void updateAccountInfo(String clientName, double totalAmount, double remainingBalance, String dueDate, String status) {
        tvClientName.setText(clientName);
        tvTotalAmount.setText("L. " + decimalFormat.format(totalAmount));
        tvRemainingBalance.setText("L. " + decimalFormat.format(remainingBalance));
        tvDueDate.setText(formatDate(dueDate));
        tvStatus.setText(status);
        
        // Cambiar color del estado según el valor
        updateStatusColor(status);
    }

    private void updateStatusColor(String status) {
        int colorRes;
        switch (status.toLowerCase()) {
            case "pagado":
                colorRes = R.color.success_color;
                break;
            case "pendiente":
                colorRes = R.color.warning_color;
                break;
            case "vencido":
                colorRes = R.color.error_color;
                break;
            default:
                colorRes = R.color.text_secondary;
                break;
        }
        tvStatus.setBackgroundTintList(getResources().getColorStateList(colorRes, null));
    }

    private void updatePaymentsList(List<Payment> payments) {
        paymentAdapter.updatePayments(payments);
        
        if (payments.isEmpty()) {
            rvPayments.setVisibility(View.GONE);
            tvNoPayments.setVisibility(View.VISIBLE);
        } else {
            rvPayments.setVisibility(View.VISIBLE);
            tvNoPayments.setVisibility(View.GONE);
        }
    }

    private String formatDate(String dateString) {
        try {
            Date date = inputDateFormat.parse(dateString);
            return outputDateFormat.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "Error al formatear fecha: " + e.getMessage());
            return dateString;
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static String getAuthorizationHeader(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String token = sharedPreferences.getString(KEY_TOKEN, "");
        String tokenType = sharedPreferences.getString(TOKEN_TYPE, "Bearer");
        
        if (!token.isEmpty()) {
            return tokenType + " " + token;
        }
        return "";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cancel any pending requests
        VolleySingleton.getInstance(requireContext()).getRequestQueue().cancelAll(TAG);
    }
}