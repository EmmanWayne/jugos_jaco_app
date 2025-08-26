package com.jugos_jaco_app.ui.sales;

import android.content.Context;
import android.os.Bundle;
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
import com.jugos_jaco_app.utils.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SaleDetailFragment extends Fragment {

    private Sale sale;
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
            sale = (Sale) getArguments().getSerializable("sale");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sale_detail, container, false);
        initializeViews(root);
        setupRecyclerView();
        loadSaleData();
        loadSaleDetails();
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
            tvClientName.setText(sale.getClientName());
            tvEmployeeName.setText(sale.getEmployeeName());
            tvSaleDate.setText(formatDate(sale.getSaleDate()));
            tvPaymentMethod.setText(sale.getPaymentMethod());
            tvPaymentTerm.setText(sale.getPaymentTerm());
            tvSubtotal.setText(String.format(Locale.getDefault(), "L. %.2f", sale.getSubtotal()));
            tvTotal.setText(String.format(Locale.getDefault(), "L. %.2f", sale.getTotalAmount()));
        }
    }

    private void loadSaleDetails() {
        if (sale == null || getContext() == null) return;

        progressBar.setVisibility(View.VISIBLE);
        tvNoDetails.setVisibility(View.GONE);

        String url = Utilities.URL + "sales/" + sale.getId();
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
                        showError("Error al procesar los detalles de la venta");
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
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
        saleDetailList.clear();

        if (response.has("data")) {
            JSONArray dataArray = response.getJSONArray("data");

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
        } else {
            showError("No se encontraron detalles para esta venta");
        }
    }

    private void showError(String message) {
        tvNoDetails.setText(message);
        tvNoDetails.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }
}