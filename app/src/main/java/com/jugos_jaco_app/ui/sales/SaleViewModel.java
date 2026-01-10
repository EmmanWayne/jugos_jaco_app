package com.jugos_jaco_app.ui.sales;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jugos_jaco_app.ui.models.Sale;
import com.jugos_jaco_app.ui.utilities.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SaleViewModel extends ViewModel {

    private final MutableLiveData<List<Sale>> salesList;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;
    private final MutableLiveData<Double> cashSalesTotal;
    private final MutableLiveData<Double> creditSalesTotal;
    private final MutableLiveData<Double> totalSales;

    public SaleViewModel() {
        salesList = new MutableLiveData<>(new ArrayList<>());
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
        cashSalesTotal = new MutableLiveData<>(0.0);
        creditSalesTotal = new MutableLiveData<>(0.0);
        totalSales = new MutableLiveData<>(0.0);
    }

    public LiveData<List<Sale>> getSalesList() {
        return salesList;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Double> getCashSalesTotal() {
        return cashSalesTotal;
    }

    public LiveData<Double> getCreditSalesTotal() {
        return creditSalesTotal;
    }

    public LiveData<Double> getTotalSales() {
        return totalSales;
    }

    /**
     * Carga la lista de ventas desde el servidor.
     */
    public void loadSales(Context context) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        String url = Utilities.URL + "sales";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        processSalesResponse(response);
                        isLoading.setValue(false);
                    } catch (JSONException e) {
                        errorMessage.setValue("Error al procesar los datos: " + e.getMessage());
                        isLoading.setValue(false);
                    }
                },
                error -> {
                    errorMessage.setValue("Error de conexión: " + error.getMessage());
                    isLoading.setValue(false);
                }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                return Utilities.getAuthHeaders(context);
            }
        };

        // Agregar a la cola de peticiones
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    /**
     * Procesa la respuesta JSON del servidor y actualiza los datos.
     */
    private void processSalesResponse(JSONObject response) throws JSONException {
        if (response.has("data")) {
            JSONArray salesArray = response.getJSONArray("data");
            List<Sale> sales = new ArrayList<>();
            double cashTotal = 0;
            double creditTotal = 0;
            double allTotal = 0;

            for (int i = 0; i < salesArray.length(); i++) {
                JSONObject saleJson = salesArray.getJSONObject(i);
                
                int id = saleJson.getInt("id");
                String clientName = saleJson.getString("client_name");
                String businessName = saleJson.optString("business_name", "");
                String employeeName = saleJson.optString("employee_name", "");
                String saleDate = saleJson.getString("sale_date");
                double cashAmount = saleJson.getDouble("cash_amount");
                String paymentReference = saleJson.isNull("payment_reference") ? null : saleJson.getString("payment_reference");
                String notes = saleJson.isNull("notes") ? null : saleJson.getString("notes");
                String paymentMethod = saleJson.getString("payment_method");
                String paymentTerm = saleJson.getString("payment_term");
                double subtotal = saleJson.getDouble("subtotal");
                double totalAmount = saleJson.getDouble("total_amount");

                Sale sale = new Sale(id, clientName, businessName, employeeName, saleDate, cashAmount, 
                        paymentReference, notes, paymentMethod, paymentTerm, subtotal, totalAmount);
                
                // Formatear la fecha en letras
                sale.setFormattedSaleDate(formatDate(saleDate));
                
                sales.add(sale);

                // Actualizar totales según el término de pago
                if ("Contado".equals(paymentTerm)) {
                    cashTotal += totalAmount;
                } else {
                    creditTotal += totalAmount;
                }
                
                allTotal += totalAmount;
            }

            // Actualizar LiveData
            salesList.setValue(sales);
            cashSalesTotal.setValue(cashTotal);
            creditSalesTotal.setValue(creditTotal);
            totalSales.setValue(allTotal);
        } else {
            errorMessage.setValue("Formato de respuesta inválido");
        }
    }

    /**
     * Formatea una fecha en formato ISO (YYYY-MM-DD) a un formato legible en español.
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