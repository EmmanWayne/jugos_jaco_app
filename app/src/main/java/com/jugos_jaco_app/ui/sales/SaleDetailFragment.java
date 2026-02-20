package com.jugos_jaco_app.ui.sales;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.adapters.SaleDetailAdapter;
import com.jugos_jaco_app.ui.models.Sale;
import com.jugos_jaco_app.ui.models.SaleDetail;
import com.jugos_jaco_app.ui.utilities.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SaleDetailFragment extends Fragment {

    private Sale sale;
    private int salesId;
    private TextView tvSaleId, tvClientName, tvEmployeeName, tvSaleDate;
    private TextView tvPaymentMethod, tvPaymentTerm, tvSubtotal, tvTotal;
    private RecyclerView rvSaleDetails;
    private ProgressBar progressBar;
    private TextView tvNoDetails;
    private SaleDetailAdapter adapter;
    private List<SaleDetail> saleDetailList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey("sale")) {
                sale = (Sale) getArguments().getSerializable("sale");
                if (sale != null) {
                    salesId = sale.getId();
                }
            } else if (getArguments().containsKey("sales_id")) {
                salesId = getArguments().getInt("sales_id");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sale_detail, container, false);

        try {
            // Configurar el fondo de la vista principal
            root.setBackgroundColor(getResources().getColor(R.color.white));
            
            initializeViews(root);
            setupRecyclerView();
            loadSaleData();
            loadSaleDetails();
         }catch (Exception e){
            Log.e("DETALLEVENTA",e.toString());
        }
        return root;

    }

    private void initializeViews(View view) {
        tvSaleId = view.findViewById(R.id.tvSaleId);
        tvClientName = view.findViewById(R.id.tvClientName);
        tvEmployeeName = view.findViewById(R.id.tvEmployeeName);
        tvSaleDate = view.findViewById(R.id.tvSaleDate);
        tvPaymentMethod = view.findViewById(R.id.tvPaymentMethod);
        tvPaymentTerm = view.findViewById(R.id.tvPaymentTerm);
        tvSubtotal = view.findViewById(R.id.tvSubtotal);
        tvTotal = view.findViewById(R.id.tvTotal);
        rvSaleDetails = view.findViewById(R.id.rvSaleDetails);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoDetails = view.findViewById(R.id.tvNoDetails);
    }

    private void setupRecyclerView() {
        saleDetailList = new ArrayList<>();
        adapter = new SaleDetailAdapter(saleDetailList);
        rvSaleDetails.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSaleDetails.setAdapter(adapter);
    }

    private void loadSaleData() {
        if (sale != null) {
            tvSaleId.setText(String.format(Locale.getDefault(), "Venta #%d", sale.getId()));
            
            String clientInfo = sale.getClientName();
            if (sale.getBusinessName() != null && !sale.getBusinessName().isEmpty() && !sale.getBusinessName().equals("null")) {
                clientInfo += " (" + sale.getBusinessName() + ")";
            }
            tvClientName.setText(clientInfo);
            
            tvEmployeeName.setText(sale.getEmployeeName());
            tvSaleDate.setText(formatDate(sale.getSaleDate()));
            tvPaymentMethod.setText(sale.getPaymentMethod());
            tvPaymentTerm.setText(sale.getPaymentTerm());
            tvSubtotal.setText(String.format(Locale.getDefault(), "L. %.2f", sale.getSubtotal()));
            tvTotal.setText(String.format(Locale.getDefault(), "L. %.2f", sale.getTotalAmount()));
        }
    }

    private void loadSaleDetails() {
        if (salesId == 0 || getContext() == null) return;

        progressBar.setVisibility(View.VISIBLE);
        tvNoDetails.setVisibility(View.GONE);

        String url = Utilities.URL + "sales/" + salesId;
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        processSaleDetailsResponse(response);
                    } catch (JSONException e) {
                        Log.e("DETALLEVENTA", "Error parsing response: " + e.getMessage());
                        showError("Error al procesar los detalles de la venta");
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("DETALLEVENTA", "Error fetching details: " + error.toString());
                    showError("Error al cargar los detalles de la venta");
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                return Utilities.getAuthHeaders(getContext());
            }
        };

        queue.add(request);
    }

    private void processSaleDetailsResponse(JSONObject response) throws JSONException {
        JSONObject dataObj = null;
        
        // Verificar si la respuesta tiene la estructura { data: { header: ..., details: ... } }
        if (response.has("data")) {
            Object dataContent = response.get("data");
            if (dataContent instanceof JSONObject) {
                dataObj = (JSONObject) dataContent;
            } else if (dataContent instanceof JSONArray) {
                // Estructura antigua donde data era un array de detalles
                // En este caso no hay header, solo detalles
                processLegacyResponse((JSONArray) dataContent);
                return;
            }
        } else {
            // Si no hay 'data', intentamos buscar header/details en la raíz (por si acaso)
            dataObj = response;
        }
        
        if (dataObj == null) return;

        // 1. Procesar Header (Información de la Venta)
        if (dataObj.has("header")) {
            JSONObject header = dataObj.getJSONObject("header");
            
            int id = header.getInt("id");
            String clientName = header.getString("client_name");
            String businessName = header.optString("business_name", "");
            String employeeName = header.optString("employee_name", "");
            String saleDate = header.getString("sale_date");
            double cashAmount = header.getDouble("cash_amount");
            String paymentReference = header.isNull("payment_reference") ? null : header.getString("payment_reference");
            String notes = header.isNull("notes") ? null : header.getString("notes");
            String paymentMethod = header.getString("payment_method");
            String paymentTerm = header.getString("payment_term");
            double subtotal = header.getDouble("subtotal");
            double totalAmount = header.getDouble("total_amount");

            this.sale = new Sale(id, clientName, businessName, employeeName, saleDate, cashAmount, 
                    paymentReference, notes, paymentMethod, paymentTerm, subtotal, totalAmount);
            
            // Formatear la fecha
            this.sale.setFormattedSaleDate(formatDate(saleDate));
            
            // Actualizar UI con la información de la venta
            loadSaleData();
        }

        // 2. Procesar Details (Lista de productos)
        saleDetailList.clear();
        JSONArray dataArray = null;

        if (dataObj.has("details")) {
            Object detailsObj = dataObj.get("details");
            if (detailsObj instanceof JSONObject) {
                // Si details es un objeto, buscamos "data" dentro
                JSONObject detailsJson = (JSONObject) detailsObj;
                if (detailsJson.has("data")) {
                    dataArray = detailsJson.getJSONArray("data");
                }
            } else if (detailsObj instanceof JSONArray) {
                // Si details es directamente el array
                dataArray = (JSONArray) detailsObj;
            }
        }
        
        if (dataArray != null) {
            processDetailsArray(dataArray);
        } else {
            // Si no hay detalles, mostrar mensaje
            if (saleDetailList.isEmpty()) {
                tvNoDetails.setVisibility(View.VISIBLE);
            }
        }
    }

    private void processLegacyResponse(JSONArray dataArray) throws JSONException {
        saleDetailList.clear();
        processDetailsArray(dataArray);
    }

    private void processDetailsArray(JSONArray dataArray) throws JSONException {
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject detailObject = dataArray.getJSONObject(i);

            SaleDetail detail = new SaleDetail();
            detail.setId(detailObject.getInt("id"));
            detail.setProductId(detailObject.getInt("product_id"));
            detail.setProductName(detailObject.getString("product_name"));
            detail.setProductCode(detailObject.getString("product_code"));
            detail.setUnitName(detailObject.getString("unit_name"));
            detail.setUnitAbbreviation(detailObject.getString("unit_abbreviation"));
            detail.setQuantity(detailObject.getInt("quantity"));
            detail.setTaxCategoryName(detailObject.getString("tax_category_name"));
            detail.setTaxRate(detailObject.getDouble("tax_rate"));
            detail.setLineSubtotal(detailObject.getDouble("line_subtotal"));
            detail.setLineTaxAmount(detailObject.getDouble("line_tax_amount"));
            detail.setLineTotal(detailObject.getDouble("line_total"));
            detail.setPriceIncludeTax(detailObject.getBoolean("price_include_tax"));
            detail.setDiscountPercentage(detailObject.getDouble("discount_percentage"));
            detail.setDiscountAmount(detailObject.getDouble("discount_amount"));

            saleDetailList.add(detail);
        }

        adapter.notifyDataSetChanged();

        if (saleDetailList.isEmpty()) {
            tvNoDetails.setVisibility(View.VISIBLE);
        } else {
            tvNoDetails.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        tvNoDetails.setText(message);
        tvNoDetails.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private String formatDate(String date) {
        if (date == null || date.isEmpty()) return "";
        try {
            // Asumiendo que la fecha viene en formato ISO 8601 (yyyy-MM-dd)
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(date));
        } catch (Exception e) {
            Log.e("DETALLEVENTA", "Error al formatear fecha: " + e.getMessage());
            return date; // Devolver la fecha original si hay error
        }
    }
}