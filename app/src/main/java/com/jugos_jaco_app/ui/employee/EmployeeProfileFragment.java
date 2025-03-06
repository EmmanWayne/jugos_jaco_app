package com.jugos_jaco_app.ui.employee;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jugos_jaco_app.Login;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.models.Employee;
import com.jugos_jaco_app.ui.utilities.Utilities;
import com.jugos_jaco_app.ui.utilities.VolleySingleton;
import com.jugos_jaco_app.ui.employee.EmployeeProfileViewModel;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EmployeeProfileFragment extends Fragment {

    private EmployeeProfileViewModel viewModel;
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
    private View loadingOverlay;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(EmployeeProfileViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employee_profile, container, false);

        initializeViews(view);
        loadingOverlay = view.findViewById(R.id.loadingView);
        
        // Observar cambios en los datos del empleado
        viewModel.getEmployee().observe(getViewLifecycleOwner(), employee -> {
            updateUI(employee);
            hideLoading(); // Ocultar loading cuando los datos se cargan
        });
        
        // Observar mensajes de error
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                showError(message);
                hideLoading();
            }
        });

        showLoading();
        viewModel.loadEmployeeIfNeeded(requireContext());

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

    private void showLoading() {
        if (loadingOverlay != null) {
            // Configurar el mensaje de carga
            TextView tvLoadingMessage = loadingOverlay.findViewById(R.id.tvLoadingMessage);
            tvLoadingMessage.setText("Cargando información del empleado...");
            
            loadingOverlay.setVisibility(View.VISIBLE);
            // Animación de fade in
            loadingOverlay.setAlpha(0f);
            loadingOverlay.animate()
                .alpha(1f)
                .setDuration(200)
                .start();
        }
    }

    private void hideLoading() {
        if (loadingOverlay != null && loadingOverlay.getVisibility() == View.VISIBLE) {
            // Animación de fade out
            loadingOverlay.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> loadingOverlay.setVisibility(View.GONE))
                .start();
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