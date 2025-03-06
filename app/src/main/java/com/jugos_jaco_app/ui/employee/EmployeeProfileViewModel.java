package com.jugos_jaco_app.ui.employee;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jugos_jaco_app.Login;
import com.jugos_jaco_app.ui.models.Employee;
import com.jugos_jaco_app.ui.utilities.Utilities;
import com.jugos_jaco_app.ui.utilities.VolleySingleton;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class EmployeeProfileViewModel extends ViewModel {
    private MutableLiveData<Employee> employeeData = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private boolean isDataLoaded = false;
    private Employee cachedEmployee = null;

    public LiveData<Employee> getEmployee() {
        return employeeData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadEmployeeIfNeeded(Context context) {
        if (!isDataLoaded) {
            loadEmployeeData(context);
        } else if (cachedEmployee != null) {
            employeeData.setValue(cachedEmployee);
        }
    }

    public void forceLoadEmployee(Context context) {
        loadEmployeeData(context);
    }

    private void loadEmployeeData(Context context) {
        String url = Utilities.URL + "employees/" + EmployeeProfileFragment.getIdEmpleado(context);

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

                    cachedEmployee = employee;
                    employeeData.setValue(employee);
                    isDataLoaded = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    errorMessage.setValue("Error al procesar los datos del empleado");
                }
            },
            error -> {
                String errorMsg = "Error desconocido";
                if (error.networkResponse != null) {
                    errorMsg = "Error " + error.networkResponse.statusCode;
                    try {
                        String responseBody = new String(error.networkResponse.data, "utf-8");
                        JSONObject errorJson = new JSONObject(responseBody);
                        if (errorJson.has("message")) {
                            errorMsg = errorJson.getString("message");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                errorMessage.setValue("Error al cargar los datos: " + errorMsg);
            }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Login.getAuthorizationHeader(context));
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }
} 