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

    private Spinner spinnerDepartament, spinnerTownship, spinnerTypePrice;
    private TextInputEditText etPhoneNumber, etLatitude, etLongitude;
    private List<String> departamentos = new ArrayList<>();
    private LinearLayout linearLayoutMunicipio; // Referencia al LinearLayout de municipios

    private Map<String, List<String>> municipiosPorDepartamento = new HashMap<>();
    private List<String> tiposPrecio = new ArrayList<>();

    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_ENABLE_GPS = 123;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 124;
    private MaterialButton btnCaptureCoordinates;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_client, container, false);

        // Inicializar vistas
        spinnerDepartament = view.findViewById(R.id.spinnerDepartament);
        spinnerTownship = view.findViewById(R.id.spinnerTownship);
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber);
        etLatitude = view.findViewById(R.id.etLatitude);
        etLongitude = view.findViewById(R.id.etLongitude);
        linearLayoutMunicipio = view.findViewById(R.id.linearLayoutMunicipio); // Inicializar LinearLayout

        MaterialButton btnSubmit = view.findViewById(R.id.btnSubmit);
        btnCaptureCoordinates = view.findViewById(R.id.btnCaptureCoordinates);

        // Deshabilitar interacción en latitud y longitud
        etLatitude.setFocusable(false);
        etLatitude.setClickable(false);
        etLongitude.setFocusable(false);
        etLongitude.setClickable(false);

        // Cargar departamentos, municipios y tipos de precio
        loadDepartamentos();
         setupDepartamentosSpinner();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        btnCaptureCoordinates.setOnClickListener(v -> checkLocationAndGetCoordinates());

        // Configurar el botón de enviar
        btnSubmit.setOnClickListener(v -> {
            // Obtener referencias a los TextInputLayout
            TextInputLayout tilFirstName = view.findViewById(R.id.tilFirstName);
            TextInputLayout tilLastName = view.findViewById(R.id.tilLastName);
            TextInputLayout tilPhoneNumber = view.findViewById(R.id.tilPhoneNumber);
            TextInputLayout tilAddress = view.findViewById(R.id.tilAddress); // Referencia para el campo de dirección

            // Obtener datos del formulario
            String firstName = ((TextInputEditText) view.findViewById(R.id.etFirstName)).getText().toString();
            String lastName = ((TextInputEditText) view.findViewById(R.id.etLastName)).getText().toString();
            String address = ((TextInputEditText) view.findViewById(R.id.etAddress)).getText().toString();
            String phoneNumber = etPhoneNumber.getText().toString();

            // Verificar si los Spinner tienen un valor seleccionado
            String department = "";
            if (spinnerDepartament.getSelectedItem() != null) {
                department = spinnerDepartament.getSelectedItem().toString();
            }

            String township = "";
            if (spinnerTownship.getSelectedItem() != null) {
                township = spinnerTownship.getSelectedItem().toString();
            }



            String latitude = etLatitude.getText().toString();
            String longitude = etLongitude.getText().toString();

            // Limpiar errores previos
            tilFirstName.setError(null);
            tilLastName.setError(null);
            tilPhoneNumber.setError(null);
            tilAddress.setError(null);

            // Validar datos
            boolean isValid = true;

            if (firstName.isEmpty()) {
                tilFirstName.setError("Este campo es obligatorio");
                isValid = false;
            }

            if (lastName.isEmpty()) {
                tilLastName.setError("Este campo es obligatorio");
                isValid = false;
            }

            if (phoneNumber.isEmpty()) {
                tilPhoneNumber.setError("Este campo es obligatorio");
                isValid = false;
            } else if (!phoneNumber.matches("\\d+")) {
                tilPhoneNumber.setError("El teléfono solo debe contener números");
                isValid = false;
            }

            if (address.isEmpty()) {
                tilAddress.setError("Este campo es obligatorio");
                isValid = false;
            }

            if (department.equals("Seleccione") || department.isEmpty()) {
                // Mostrar error en el Spinner de departamento
                TextView errorText = (TextView) spinnerDepartament.getSelectedView();
                if (errorText != null) {
                    errorText.setError("Seleccione un departamento");
                    errorText.setTextColor(Color.RED); // Cambiar el color del texto a rojo
                }
                isValid = false;
            }

            if (township.isEmpty()) {
                // Mostrar error en el Spinner de municipio
                TextView errorText = (TextView) spinnerTownship.getSelectedView();
                if (errorText != null) {
                    errorText.setError("Seleccione un municipio");
                    errorText.setTextColor(Color.RED); // Cambiar el color del texto a rojo
                }
                isValid = false;
            }



            // Si todos los campos son válidos, enviar datos al servidor
            if (isValid) {
                storeEmploye(firstName, lastName, address, phoneNumber, department, township, latitude, longitude);
                //sendDataToServer(firstName, lastName, address, phoneNumber, department, township, typePrice, latitude, longitude);
            }
        });

        return view;
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

    private void storeEmploye(String first_name, String last_name, String address, String phone_number,
                              String department, String township, String latitude, String longitude) {
        String url = Utilities.URL + "clients";
        Map<String, String> params = new HashMap<>();
        params.put("first_name", first_name);
        params.put("last_name", last_name);
        params.put("address", address);
        params.put("phone_number", phone_number);
        params.put("department", department);
        params.put("township", township);
        params.put("latitude", latitude);
        params.put("longitude", longitude);

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
                                    typePrice
                            );

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