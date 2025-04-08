package com.jugos_jaco_app.ui.clients;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.models.Client;
import com.jugos_jaco_app.ui.utilities.VolleySingleton;
import com.jugos_jaco_app.ui.utilities.LocationData;
import com.jugos_jaco_app.ui.utilities.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
  * create an instance of this fragment.
 */
public class EditClientFragment extends Fragment {
    private Spinner spinnerDepartament, spinnerTownship, spinnerTypePrice;
    private TextInputEditText etFirstName, etLastName, etPhoneNumber, etAddress, etLatitude, etLongitude, etBusinessName, etPosition, etVisitDay;
    private TextInputLayout tilFirstName, tilLastName, tilPhoneNumber, tilAddress, tilBusinessName, tilPosition, tilVisitDay;
    private MaterialButton btnSubmit, btnCaptureCoordinates;
    private String clientId;
    private List<String> departamentos = new ArrayList<>();
    private List<String> tiposPrecio = new ArrayList<>();
    private Map<String, List<String>> municipiosPorDepartamento = new HashMap<>();
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_ENABLE_GPS = 123;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 124;
    private LinearLayout linearLayoutMunicipio;
    private String[] diasSemana = {"lunes", "martes", "miercoles", "jueves", "viernes", "sabado"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_client, container, false);
        
        initializeViews(view);
        loadDepartamentos();
        loadDataFromArguments();
        setupVisitDaySpinner();
        
        btnSubmit.setOnClickListener(v -> {
            if (validateFields()) {
                updateClient();
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        btnCaptureCoordinates.setOnClickListener(v -> checkLocationAndGetCoordinates());

        return view;
    }

    private void initializeViews(View view) {
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etBusinessName = view.findViewById(R.id.etBusinessName);
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber);
        etAddress = view.findViewById(R.id.etAddress);
        etLatitude = view.findViewById(R.id.etLatitude);
        etLongitude = view.findViewById(R.id.etLongitude);
        spinnerDepartament = view.findViewById(R.id.spinnerDepartament);
        spinnerTownship = view.findViewById(R.id.spinnerTownship);
         btnSubmit = view.findViewById(R.id.btnSubmit);
        btnCaptureCoordinates = view.findViewById(R.id.btnCaptureCoordinates);
        linearLayoutMunicipio = view.findViewById(R.id.linearLayoutMunicipio);
        etPosition = view.findViewById(R.id.etPosition);
        etVisitDay = view.findViewById(R.id.etVisitDay);
        tilPosition = view.findViewById(R.id.tilPosition);
        tilVisitDay = view.findViewById(R.id.tilVisitDay);

        tilFirstName = view.findViewById(R.id.tilFirstName);
        tilLastName = view.findViewById(R.id.tilLastName);
        tilBusinessName = view.findViewById(R.id.tilBusinessName);
        tilPhoneNumber = view.findViewById(R.id.tilPhoneNumber);
        tilAddress = view.findViewById(R.id.tilAddress);

        btnSubmit.setText("Actualizar Cliente");
    }

    private void setupVisitDaySpinner() {
        etVisitDay.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Seleccionar día de visita");
            builder.setItems(diasSemana, (dialog, which) -> {
                String selectedDay = diasSemana[which];
                etVisitDay.setText(selectedDay);
                tilVisitDay.setError(null); // Limpiar el error si existe
            });
            builder.show();
        });
    }

    private void loadDataFromArguments() {
        if (getArguments() != null) {
            etFirstName.setText(getArguments().getString("first_name"));
            etLastName.setText(getArguments().getString("last_name"));
            etBusinessName.setText(getArguments().getString("business_name"));
            etPhoneNumber.setText(getArguments().getString("phone_number"));
            etAddress.setText(getArguments().getString("address"));
            etLatitude.setText(getArguments().getString("latitude"));
            etLongitude.setText(getArguments().getString("longitude"));
            etPosition.setText(getArguments().getString("position", ""));
            
            // Cargar el día de visita
            String visitDay = getArguments().getString("visit_day", "");
            if (!visitDay.isEmpty()) {
                // Convertir el día a minúsculas y normalizar
                visitDay = visitDay.toLowerCase().trim();
                // Buscar el día en el array de días de la semana
                for (String dia : diasSemana) {
                    if (dia.equals(visitDay)) {
                        etVisitDay.setText(dia);
                        break;
                    }
                }
            }
            
            clientId = getArguments().getString("client_id");

            String department = getArguments().getString("department");
            String township = getArguments().getString("township");

            setupSpinnersWithValues(department, township);
        }
    }

    private void setupSpinnersWithValues(String department, String township) {
        // Configurar adapter del departamento
        ArrayAdapter<String> departamentosAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                departamentos
        );
        departamentosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartament.setAdapter(departamentosAdapter);

        // Configurar listener del departamento
        spinnerDepartament.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean isInitialSelection = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDepartment = parent.getItemAtPosition(position).toString();
                if (!selectedDepartment.equals("Seleccione")) {
                    loadMunicipios(selectedDepartment);
                    linearLayoutMunicipio.setVisibility(View.VISIBLE);

                    // Solo seleccionar el municipio en la carga inicial
                    if (isInitialSelection && township != null && !township.isEmpty()) {
                        List<String> municipios = municipiosPorDepartamento.get(selectedDepartment);
                        if (municipios != null) {
                            int townshipPosition = municipios.indexOf(township);
                            if (townshipPosition != -1) {
                                spinnerTownship.setSelection(townshipPosition);
                            }
                        }
                        isInitialSelection = false;
                    }
                } else {
                    linearLayoutMunicipio.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Seleccionar el departamento
        if (department != null && !department.isEmpty()) {
            int departmentPosition = departamentos.indexOf(department);
            if (departmentPosition != -1) {
                spinnerDepartament.setSelection(departmentPosition);
            }
        }
    }

    private void loadMunicipios(String departament) {
        List<String> municipios = municipiosPorDepartamento.get(departament);
        if (municipios != null) {
            ArrayAdapter<String> municipiosAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    municipios
            );
            municipiosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTownship.setAdapter(municipiosAdapter);
        }
    }

    private void checkLocationAndGetCoordinates() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("GPS Desactivado")
                    .setMessage("Para obtener las coordenadas necesita activar el GPS. ¿Desea activarlo?")
                    .setPositiveButton("Activar GPS", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, REQUEST_ENABLE_GPS);
                    })
                    .setNegativeButton("Cancelar", null)
                    .create()
                    .show();
        } else {
            checkLocationPermission();
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        } else {
            getLocation();
        }
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            btnCaptureCoordinates.setEnabled(false);
            btnCaptureCoordinates.setText("Obteniendo ubicación...");

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            String latitude = String.format("%.6f", location.getLatitude());
                            String longitude = String.format("%.6f", location.getLongitude());

                            etLatitude.setText(latitude);
                            etLongitude.setText(longitude);

                            // Abrir el mapa directamente
                            openLocationInMap(latitude, longitude);

                            Toast.makeText(requireContext(),
                                    "Coordenadas actualizadas con éxito\nPrecisión: " +
                                            String.format("%.2f", location.getAccuracy()) + " metros",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(),
                                    "No se pudo obtener la ubicación. Intente de nuevo",
                                    Toast.LENGTH_SHORT).show();
                        }
                        btnCaptureCoordinates.setEnabled(true);
                        btnCaptureCoordinates.setText("Capturar Coordenadas");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(),
                                "Error al obtener ubicación: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        btnCaptureCoordinates.setEnabled(true);
                        btnCaptureCoordinates.setText("Capturar Coordenadas");
                    });
        }
    }

    private void openLocationInMap(String latitude, String longitude) {
        try {
            // Crear URI para Google Maps con las coordenadas
            Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" +
                    latitude + "," + longitude + "(Ubicación del Cliente)");

            // Crear intent para abrir Google Maps
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Verificar si Google Maps está instalado
            if (mapIntent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Si Google Maps no está instalado, abrir en el navegador
                Uri browserUri = Uri.parse("https://www.google.com/maps?q=" +
                        latitude + "," + longitude);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                startActivity(browserIntent);
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(),
                    "Error al abrir el mapa",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(requireContext(),
                        "Se necesita permiso de ubicación para obtener coordenadas",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_GPS) {
            LocationManager locationManager =
                    (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                checkLocationPermission();
            } else {
                Toast.makeText(requireContext(),
                        "Se requiere GPS para obtener coordenadas",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateClient() {
        if (!validateFields()) {
            return;
        }

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String businessName = etBusinessName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String department = spinnerDepartament.getSelectedItem().toString();
        String township = spinnerTownship.getSelectedItem().toString();
        String latitude = etLatitude.getText().toString().trim();
        String longitude = etLongitude.getText().toString().trim();
        String position = etPosition.getText().toString().trim();
        String visitDay = etVisitDay.getText().toString().trim();

        String url = Utilities.URL + "clients/" + clientId;
        Map<String, String> params = new HashMap<>();
        params.put("first_name", firstName);
        params.put("last_name", lastName);
        params.put("business_name", businessName);
        params.put("phone_number", phoneNumber);
        params.put("address", address);
        params.put("department", department);
        params.put("township", township);
        params.put("latitude", latitude);
        params.put("longitude", longitude);
        params.put("position", position);
        params.put("visit_day", visitDay);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                new JSONObject(params),
                response -> {
                    try {
                        // Obtener el mensaje
                        String message = response.getString("message");
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        // Verificar si la respuesta contiene la clave "data"
                        if (response.has("data")) {
                            // Obtener el objeto "data"
                            JSONObject dataJson = response.getJSONObject("data");
                            // Obtener el objeto location
                            JSONObject locationJson = dataJson.getJSONObject("location");
                            // Obtener type_price
                            String typePrice = "";
                            String plus_code = "";
                            if (dataJson.has("type_price") && !dataJson.isNull("type_price")) {
                                typePrice = dataJson.getString("type_price");
                            }

                            // Crear un nuevo objeto Client con los datos
                            Client updatedClient = new Client(
                                    String.valueOf(dataJson.getInt("id")),
                                    dataJson.getString("first_name"),
                                    dataJson.getString("last_name"),
                                    dataJson.getString("phone_number"),
                                    dataJson.getString("address"),
                                    dataJson.getString("department"),
                                    dataJson.getString("township"),
                                    locationJson.getString("latitude"),
                                    locationJson.getString("longitude"),
                                    locationJson.getString("plus_code"),
                                    typePrice,
                                    dataJson.getString("business_name"),
                                    dataJson.getString("position"),
                                    dataJson.getString("visit_day")
                            );
                            // Actualizar el ViewModel
                            ClientsViewModel viewModel = new ViewModelProvider(requireActivity())
                                    .get(ClientsViewModel.class);
                            viewModel.updateClient(updatedClient);
                            
                            // Navegar hacia atrás hasta ClientsFragment
                            Navigation.findNavController(requireView())
                                .popBackStack(R.id.nav_clientes, false);
                        } else {
                            Toast.makeText(requireContext(), "Respuesta del servidor incompleta", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    try {
                        String errorMessage = new String(error.networkResponse.data);
                        JSONObject errorResponse = new JSONObject(errorMessage);
                        if(errorResponse.has("message")){
                            String message = errorResponse.getString("message");
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(requireContext(), "Error inesperado", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", ClientsFragment.getAuthorizationHeader(requireContext()));
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    private boolean validateFields() {
        boolean isValid = true;

        // Validar nombre
        String firstName = etFirstName.getText().toString().trim();
        if (firstName.isEmpty()) {
            tilFirstName.setError("El nombre es requerido");
            isValid = false;
        }

        // Validar apellido
        String lastName = etLastName.getText().toString().trim();
        if (lastName.isEmpty()) {
            tilLastName.setError("El apellido es requerido");
            isValid = false;
        }

        // Validar teléfono
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        if (phoneNumber.isEmpty()) {
            tilPhoneNumber.setError("El teléfono es requerido");
            isValid = false;
        } else if (phoneNumber.length() < 8) {
            tilPhoneNumber.setError("El teléfono debe tener al menos 8 dígitos");
            isValid = false;
        }

        // Validar dirección
        String address = etAddress.getText().toString().trim();
        if (address.isEmpty()) {
            tilAddress.setError("La dirección es requerida");
            isValid = false;
        }

        // Validar negocio
        String businessName = etBusinessName.getText().toString().trim();
        if (businessName.isEmpty()) {
            tilBusinessName.setError("El nombre del negocio es requerido");
            isValid = false;
        }

        // Validar departamento
        if (spinnerDepartament.getSelectedItemPosition() == 0) {
            TextView errorText = (TextView) spinnerDepartament.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);
            errorText.setText("Seleccione un departamento");
            isValid = false;
        }

        // Validar municipio
        if (spinnerTownship.getSelectedItemPosition() < 0 || 
            spinnerDepartament.getSelectedItemPosition() == 0) {
            TextView errorText = (TextView) spinnerTownship.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);
            errorText.setText("Seleccione un municipio");
            isValid = false;
        }

        // Validar coordenadas (opcional pero con advertencia)
        String latitude = etLatitude.getText().toString().trim();
        String longitude = etLongitude.getText().toString().trim();
        if (latitude.equals("0.0") || longitude.equals("0.0")) {
            Toast.makeText(requireContext(),
                "Se recomienda capturar las coordenadas del cliente",
                Toast.LENGTH_SHORT).show();
        } else {
            try {
                double lat = Double.parseDouble(latitude);
                double lon = Double.parseDouble(longitude);
                
                // Validar rango de coordenadas para Honduras
                if (lat < 12.98 || lat > 16.02 || lon < -89.35 || lon > -83.15) {
                    etLatitude.setError("Coordenadas fuera de Honduras");
                    etLongitude.setError("Coordenadas fuera de Honduras");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etLatitude.setError("Coordenada inválida");
                etLongitude.setError("Coordenada inválida");
                isValid = false;
            }
        }

        if (etPosition.getText().toString().trim().isEmpty()) {
            tilPosition.setError("La posición es requerida");
            isValid = false;
        } else {
            tilPosition.setError(null);
        }

        if (etVisitDay.getText().toString().trim().isEmpty()) {
            tilVisitDay.setError("El día de visita es requerido");
            isValid = false;
        } else {
            tilVisitDay.setError(null);
        }

        if (!isValid) {
            Toast.makeText(requireContext(), 
                "Por favor, complete todos los campos requeridos correctamente", 
                Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }

    private void loadDepartamentos() {
        departamentos = LocationData.getDepartamentos();
        municipiosPorDepartamento = LocationData.getMunicipiosPorDepartamento();
    }
}