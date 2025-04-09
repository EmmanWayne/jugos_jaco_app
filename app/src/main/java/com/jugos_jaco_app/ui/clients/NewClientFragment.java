package com.jugos_jaco_app.ui.clients;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
 import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;

import com.android.volley.toolbox.JsonObjectRequest;
 import com.google.android.gms.location.FusedLocationProviderClient;
 import com.google.android.gms.location.LocationServices;

import com.google.android.gms.location.Priority;
 import android.app.AlertDialog;
import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
 import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.models.Client;
import com.jugos_jaco_app.ui.utilities.VolleySingleton;
import com.jugos_jaco_app.ui.utilities.Utilities;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import com.jugos_jaco_app.ui.utilities.LocationData;

public class NewClientFragment extends Fragment {

    private Spinner spinnerDepartament, spinnerTownship, spinnerTypePrice, spinnerVisitDay;
    private TextInputEditText etFirstName, etLastName, etBusinessName, etPhoneNumber, etLatitude, etLongitude, etAddress, etPosition;
    private TextInputLayout tilFirstName, tilLastName, tilBusinessName, tilPhoneNumber, tilAddress, tilPosition;
    private List<String> departamentos = new ArrayList<>();
    private LinearLayout linearLayoutMunicipio; // Referencia al LinearLayout de municipios
    private MaterialButton btnSubmit, btnCaptureCoordinates;

    private Map<String, List<String>> municipiosPorDepartamento = new HashMap<>();
    private List<String> tiposPrecio = new ArrayList<>();

    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_ENABLE_GPS = 123;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 124;

    private String visitDay = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_client, container, false);

        // Inicializar vistas
        setupViews(view);

        // Cargar departamentos, municipios y tipos de precio
        loadDepartamentos();
         setupDepartamentosSpinner();
         setupVisitDaySpinner();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        btnCaptureCoordinates.setOnClickListener(v -> checkLocationAndGetCoordinates());

        // Configurar el botón de enviar
        btnSubmit.setOnClickListener(v -> {
            if (validateFields()) {
                createClient();
            }
        });

        return view;
    }

    private void setupViews(View root) {
        spinnerDepartament = root.findViewById(R.id.spinnerDepartament);
        spinnerTownship = root.findViewById(R.id.spinnerTownship);
        spinnerVisitDay = root.findViewById(R.id.spinnerVisitDay);
        etFirstName = root.findViewById(R.id.etFirstName);
        etLastName = root.findViewById(R.id.etLastName);
        etBusinessName = root.findViewById(R.id.etBusinessName);
        etPhoneNumber = root.findViewById(R.id.etPhoneNumber);
        etAddress = root.findViewById(R.id.etAddress);
        etLatitude = root.findViewById(R.id.etLatitude);
        etLongitude = root.findViewById(R.id.etLongitude);
        linearLayoutMunicipio = root.findViewById(R.id.linearLayoutMunicipio);
        etPosition = root.findViewById(R.id.etPosition);

        // Inicializar TextInputLayouts
        tilFirstName = root.findViewById(R.id.tilFirstName);
        tilLastName = root.findViewById(R.id.tilLastName);
        tilBusinessName = root.findViewById(R.id.tilBusinessName);
        tilPhoneNumber = root.findViewById(R.id.tilPhoneNumber);
        tilAddress = root.findViewById(R.id.tilAddress);
        tilPosition = root.findViewById(R.id.tilPosition);

        btnSubmit = root.findViewById(R.id.btnSubmit);
        btnCaptureCoordinates = root.findViewById(R.id.btnCaptureCoordinates);

        // Deshabilitar interacción en latitud y longitud
        etLatitude.setFocusable(false);
        etLatitude.setClickable(false);
        etLongitude.setFocusable(false);
        etLongitude.setClickable(false);
    }

    private void loadDepartamentos() {
        departamentos = LocationData.getDepartamentos();
        municipiosPorDepartamento = LocationData.getMunicipiosPorDepartamento();
    }

    private void setupDepartamentosSpinner() {
        ArrayAdapter<String> departamentosAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, departamentos);
        departamentosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartament.setAdapter(departamentosAdapter);

        spinnerDepartament.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Ignorar el placeholder
                    String selectedDepartament = departamentos.get(position);
                    loadMunicipios(selectedDepartament);
                    linearLayoutMunicipio.setVisibility(View.VISIBLE); // Mostrar el LinearLayout de municipios
                } else {
                    linearLayoutMunicipio.setVisibility(View.GONE); // Ocultar el LinearLayout de municipios
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });
    }

    private void setupTiposPrecioSpinner() {
        ArrayAdapter<String> tiposPrecioAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, tiposPrecio);
        tiposPrecioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTypePrice.setAdapter(tiposPrecioAdapter);
    }

    private void loadMunicipios(String departament) {
        List<String> municipios = municipiosPorDepartamento.get(departament);
        if (municipios == null) {
            municipios = new ArrayList<>();
        }
        ArrayAdapter<String> municipiosAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, municipios);
        municipiosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTownship.setAdapter(municipiosAdapter);
    }

    private void setupVisitDaySpinner() {
        String[] diasSemana = new String[]{
            "Seleccionar día",
            "Lunes",
            "Martes",
            "Miércoles",
            "Jueves",
            "Viernes",
            "Sábado"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            diasSemana
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVisitDay.setAdapter(adapter);

        spinnerVisitDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Ignorar "Seleccionar día"
                    visitDay = diasSemana[position]; // Ya está en el formato correcto
                } else {
                    visitDay = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                visitDay = "";
            }
        });
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

    private boolean validateFields() {
        boolean isValid = true;

        // Validar nombre
        String firstName = etFirstName.getText().toString().trim();
        if (firstName.isEmpty()) {
            tilFirstName.setError("El nombre es requerido");
            isValid = false;
        } else {
            tilFirstName.setError(null);
        }

        // Validar apellido
        String lastName = etLastName.getText().toString().trim();
        if (lastName.isEmpty()) {
            tilLastName.setError("El apellido es requerido");
            isValid = false;
        } else {
            tilLastName.setError(null);
        }

        // Validar teléfono
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        if (phoneNumber.isEmpty()) {
            tilPhoneNumber.setError("El teléfono es requerido");
            isValid = false;
        } else if (phoneNumber.length() < 8) {
            tilPhoneNumber.setError("El teléfono debe tener al menos 8 dígitos");
            isValid = false;
        } else {
            tilPhoneNumber.setError(null);
        }

        // Validar dirección
        String address = etAddress.getText().toString().trim();
        if (address.isEmpty()) {
            tilAddress.setError("La dirección es requerida");
            isValid = false;
        } else {
            tilAddress.setError(null);
        }

        // Validar negocio
        String businessName = etBusinessName.getText().toString().trim();
        if (businessName.isEmpty()) {
            tilBusinessName.setError("El nombre del negocio es requerido");
            isValid = false;
        } else {
            tilBusinessName.setError(null);
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
            TextView errorText = null;
            try {
                errorText = (TextView) spinnerTownship.getSelectedView();
            } catch (Exception e) {
                // Si no hay vista seleccionada, mostrar un Toast
                Toast.makeText(requireContext(), 
                    "Seleccione un municipio", 
                    Toast.LENGTH_SHORT).show();
            }
            if (errorText != null) {
                errorText.setError("");
                errorText.setTextColor(Color.RED);
                errorText.setText("Seleccione un municipio");
            }
            isValid = false;
        }

        // Validar posición
        if (etPosition.getText().toString().trim().isEmpty()) {
            tilPosition.setError("La posición es requerida");
            isValid = false;
        } else {
            tilPosition.setError(null);
        }

        // Validar día de visita
        if (spinnerVisitDay.getSelectedItemPosition() == 0) {
            ((TextView) spinnerVisitDay.getSelectedView()).setError("Seleccione un día de visita");
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

        if (!isValid) {
            Toast.makeText(requireContext(), 
                "Por favor, complete todos los campos requeridos correctamente", 
                Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }

    private void createClient() {
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

        String url = Utilities.URL + "clients";
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

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
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

                            // Obtener type_price, manejando el caso de null
                            String typePrice = "";
                            String plus_code = "";

                            if (dataJson.has("type_price") && !dataJson.isNull("type_price")) {
                                typePrice = dataJson.getString("type_price");
                            }

                            // Crear un nuevo objeto Client con los datos
                            Client newClient = new Client(
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
                                    dataJson.optString("position", ""),
                                    dataJson.optString("visit_day", ""),
                                    ""
                            );
                            Toast.makeText(requireContext(), locationJson.getString("plus_code")
                                    , Toast.LENGTH_SHORT).show();

                            // Actualizar el ViewModel con el nuevo cliente
                            ClientsViewModel viewModel = new ViewModelProvider(requireActivity())
                                    .get(ClientsViewModel.class);
                            viewModel.addNewClient(newClient);

                            // Navegar de vuelta a ClientsFragment
                            Navigation.findNavController(requireView()).navigateUp();
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

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(jsonObjectRequest);
    }

}