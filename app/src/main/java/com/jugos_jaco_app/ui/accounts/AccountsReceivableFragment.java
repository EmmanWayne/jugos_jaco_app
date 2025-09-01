package com.jugos_jaco_app.ui.accounts;

import static com.jugos_jaco_app.Login.KEY_TOKEN;
import static com.jugos_jaco_app.Login.PREFS_NAME;
import static com.jugos_jaco_app.Login.TOKEN_TYPE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.adapters.AccountsReceivableAdapter;
import com.jugos_jaco_app.ui.models.AccountReceivable;
import com.jugos_jaco_app.ui.utilities.Utilities;
import com.jugos_jaco_app.ui.utilities.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AccountsReceivableFragment extends Fragment implements AccountsReceivableAdapter.OnAccountClickListener {

    private RecyclerView rvAccountsReceivable;
    private AccountsReceivableAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState;
    private EditText etSearchClient;
    private ChipGroup chipGroupStatus;
    private Chip chipAll, chipPending, chipPaid;
    private TextView tvTotalPending, tvTotalAccounts;

    private List<AccountReceivable> accountsList;
    private NumberFormat currencyFormat;
    private static final String URL_ACCOUNTS_RECEIVABLE = Utilities.URL + "account-receivable/";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountsList = new ArrayList<>();
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "US"));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_accounts_receivable, container, false);

        initializeViews(root);
        setupRecyclerView();
        setupSearchAndFilters();
        setupSwipeRefresh();

        loadAccountsReceivable();

        return root;
    }

    private void initializeViews(View root) {
        rvAccountsReceivable = root.findViewById(R.id.rvAccountsReceivable);
        progressBar = root.findViewById(R.id.progressBar);
        layoutEmptyState = root.findViewById(R.id.layoutEmptyState);
        etSearchClient = root.findViewById(R.id.etSearchClient);
        chipGroupStatus = root.findViewById(R.id.chipGroupStatus);
        chipAll = root.findViewById(R.id.chipAll);
        chipPending = root.findViewById(R.id.chipPending);
        chipPaid = root.findViewById(R.id.chipPaid);
        tvTotalPending = root.findViewById(R.id.tvTotalPending);
        tvTotalAccounts = root.findViewById(R.id.tvTotalAccounts);

        // Setup SwipeRefreshLayout
        swipeRefreshLayout = new SwipeRefreshLayout(requireContext());
        ViewGroup parent = (ViewGroup) rvAccountsReceivable.getParent();
        int index = parent.indexOfChild(rvAccountsReceivable);
        parent.removeView(rvAccountsReceivable);
        swipeRefreshLayout.addView(rvAccountsReceivable);
        parent.addView(swipeRefreshLayout, index);
    }

    private void setupRecyclerView() {
        adapter = new AccountsReceivableAdapter(accountsList, this);
        rvAccountsReceivable.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAccountsReceivable.setAdapter(adapter);
    }

    private void setupSearchAndFilters() {
        // Search functionality
        etSearchClient.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filterByClient(s.toString());
                updateEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Status filter chips
        chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
            String status = "Todos";
            if (checkedId == R.id.chipPending) {
                status = "Pendiente";
            } else if (checkedId == R.id.chipPaid) {
                status = "Pagado";
            }
            adapter.filterByStatus(status);
            updateEmptyState();
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadAccountsReceivable);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.lighter_red,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
        );
    }

    private void loadAccountsReceivable() {
        showLoading();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                URL_ACCOUNTS_RECEIVABLE,
                null,
                response -> {
                    hideLoading();
                    try {
                        JSONArray dataArray = response.getJSONArray("data");
                         accountsList.clear();

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject accountJson = dataArray.getJSONObject(i);
                            Log.i("CUENTAS", dataArray.toString());

                            AccountReceivable account = new AccountReceivable(
                                    accountJson.getInt("id"),
                                    accountJson.getString("client_name"),
                                    accountJson.getDouble("total_amount"),
                                    accountJson.getDouble("remaining_balance"),
                                    accountJson.getString("due_date"),
                                    accountJson.getString("status")
                            );
                            
                            accountsList.add(account);
                        }
                        
                        Log.i("CUENTAS", "Lista cargada con " + accountsList.size() + " elementos");
                        adapter.updateAccounts(accountsList);
                        Log.i("CUENTAS", "Adaptador actualizado. ItemCount: " + adapter.getItemCount());
                        updateSummary();
                        updateEmptyState();
                        
                    } catch (JSONException e) {
                        Toast.makeText(requireContext(), "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    hideLoading();
                    Toast.makeText(requireContext(), "Error al cargar las cuentas por cobrar", Toast.LENGTH_SHORT).show();
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

    private void updateSummary() {
        double totalPending = 0;
        int totalAccounts = accountsList.size();
        
        for (AccountReceivable account : accountsList) {
            if (account.isPending()) {
                totalPending += account.getRemainingBalance();
            }
        }
        
        tvTotalPending.setText(currencyFormat.format(totalPending));
        tvTotalAccounts.setText(String.valueOf(totalAccounts));
    }

    private void updateEmptyState() {
        int itemCount = adapter.getItemCount();
        Log.i("CUENTAS", "updateEmptyState - ItemCount: " + itemCount);
        if (itemCount == 0) {
            Log.i("CUENTAS", "Mostrando estado vacío");
            rvAccountsReceivable.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            Log.i("CUENTAS", "Mostrando RecyclerView con datos");
            rvAccountsReceivable.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void showLoading() {
        if (swipeRefreshLayout.isRefreshing()) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        rvAccountsReceivable.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        rvAccountsReceivable.setVisibility(View.VISIBLE);
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
    public void onAccountClick(AccountReceivable account) {
        // Navegar al detalle de la cuenta pasando el ID como argumento
        Bundle args = new Bundle();
        args.putInt("account_id", account.getId());
        
        try {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_accountsReceivableFragment_to_accountReceivableDetailFragment, args);
        } catch (Exception e) {
            Log.e("Navigation", "Error al navegar al detalle: " + e.getMessage());
            Toast.makeText(requireContext(), "Error al abrir el detalle", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        if (adapter != null && accountsList.isEmpty()) {
            loadAccountsReceivable();
        }
    }
}