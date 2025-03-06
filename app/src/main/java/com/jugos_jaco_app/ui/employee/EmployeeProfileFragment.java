package com.jugos_jaco_app.ui.employee;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jugos_jaco_app.Login;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.models.Employee;
import com.jugos_jaco_app.ui.utilities.Utilities;
import com.jugos_jaco_app.ui.utilities.VolleySingleton;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EmployeeProfileFragment extends Fragment {

    private TextView tvEmployeeName;
    private TextView tvEmployeeId;
    private TextView tvIdentity;
    private TextView tvPhoneNumber;
    private TextView tvAddress;
    private TextView tvBranchName;
    private TextView tvBranchPhone;
    private TextView tvBranchAddress;
    private TextView tvStartDate;
    private TextView tvLastUpdate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employee_profile, container, false);

        initializeViews(view);
        loadEmployeeData();

        return view;
    }

    private void initializeViews(View view) {
        tvEmployeeName = view.findViewById(R.id.tvEmployeeName);
        tvEmployeeId = view.findViewById(R.id.tvEmployeeId);
        tvIdentity = view.findViewById(R.id.tvIdentity);
        tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvBranchName = view.findViewById(R.id.tvBranchName);
        tvBranchPhone = view.findViewById(R.id.tvBranchPhone);
        tvBranchAddress = view.findViewById(R.id.tvBranchAddress);
        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvLastUpdate = view.findViewById(R.id.tvLastUpdate);
    }

    private void loadEmployeeData() {

 
         String url = Utilities.URL + "employees/"+getIdEmpleado(requireContext());
        Toast.makeText(getContext(), ""+url, Toast.LENGTH_SHORT).show();

        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            response -> {
                try {
                    JSONObject data = response.getJSONObject("data");
                    JSONObject branchData = data.getJSONObject("branch");

                    // Crear objeto Branch
                    Employee.Branch branch = new Employee.Branch(
                        branchData.getInt("id"),
                        branchData.getString("name"),
                        branchData.getString("address"),
                        branchData.getString("phone_number")
                    );

                    // Crear objeto Employee
                    Employee employee = new Employee(
                        data.getInt("id"),
                        data.getString("first_name"),
                        data.getString("last_name"),
                        data.getString("phone_number"),
                        data.getString("address"),
                        data.getString("identity"),
                        data.getInt("branch_id"),
                        branch,
                        data.getString("created_at"),
                        data.getString("updated_at")
                    );

                    updateUI(employee);

                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error al procesar los datos del empleado");
                }
            },
            error -> {
                String errorMessage = "Error desconocido";
                if (error.networkResponse != null) {
                    errorMessage = "Error " + error.networkResponse.statusCode;
                    try {
                        String responseBody = new String(error.networkResponse.data, "utf-8");
                        JSONObject errorJson = new JSONObject(responseBody);
                        if (errorJson.has("message")) {
                            errorMessage = errorJson.getString("message");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                showError("Error al cargar los datos: " + errorMessage);
            }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Login.getAuthorizationHeader(requireContext()));
                headers.put("Accept", "application/json");

                return headers;
            }
        };

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    private void updateUI(Employee employee) {
        // Información del empleado
        tvEmployeeName.setText(employee.getFullName());
        tvEmployeeId.setText("ID: " + employee.getId());
        tvIdentity.setText(employee.getIdentity());
        tvPhoneNumber.setText(employee.getPhoneNumber());
        tvAddress.setText(employee.getAddress());

        // Información de la sucursal
        Employee.Branch branch = employee.getBranch();
        tvBranchName.setText(branch.getName());
        tvBranchPhone.setText(branch.getPhoneNumber());
        tvBranchAddress.setText(branch.getAddress());

        // Formatear y mostrar las fechas
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE dd 'de' MMMM 'de' yyyy 'a las' HH:mm", new Locale("es", "ES"));

        try {
            // Fecha de inicio
            String startDate = outputFormat.format(inputFormat.parse(employee.getCreatedAt()))
                    .replace("á", "a")
                    .replace("é", "e")
                    .replace("í", "i")
                    .replace("ó", "o")
                    .replace("ú", "u");
            tvStartDate.setText(startDate);

            // Última actualización
            String lastUpdate = outputFormat.format(inputFormat.parse(employee.getUpdatedAt()))
                    .replace("á", "a")
                    .replace("é", "e")
                    .replace("í", "i")
                    .replace("ó", "o")
                    .replace("ú", "u");
            tvLastUpdate.setText(lastUpdate);
        } catch (Exception e) {
            e.printStackTrace();
            tvStartDate.setText(employee.getCreatedAt());
            tvLastUpdate.setText(employee.getUpdatedAt());
        }
    }



    private void showError(String message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public static String getIdEmpleado(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("id_empleado", null);
    }
} 