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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jugos_jaco_app.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewClientFragment extends Fragment {

    private Spinner spinnerDepartament, spinnerTownship, spinnerTypePrice;
    private TextInputEditText etPhoneNumber, etLatitude, etLongitude;
    private List<String> departamentos = new ArrayList<>();
    private LinearLayout linearLayoutMunicipio; // Referencia al LinearLayout de municipios

    private Map<String, List<String>> municipiosPorDepartamento = new HashMap<>();
    private List<String> tiposPrecio = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_client, container, false);

        // Inicializar vistas
        spinnerDepartament = view.findViewById(R.id.spinnerDepartament);
        spinnerTownship = view.findViewById(R.id.spinnerTownship);
        spinnerTypePrice = view.findViewById(R.id.spinnerTypePrice);
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber);
        etLatitude = view.findViewById(R.id.etLatitude);
        etLongitude = view.findViewById(R.id.etLongitude);
        linearLayoutMunicipio = view.findViewById(R.id.linearLayoutMunicipio); // Inicializar LinearLayout

        MaterialButton btnSubmit = view.findViewById(R.id.btnSubmit);

        // Deshabilitar interacción en latitud y longitud
        etLatitude.setFocusable(false);
        etLatitude.setClickable(false);
        etLongitude.setFocusable(false);
        etLongitude.setClickable(false);

        // Cargar departamentos, municipios y tipos de precio
        loadDepartamentos();
        loadTiposPrecio();
        setupDepartamentosSpinner();
        setupTiposPrecioSpinner();

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
            String departament = "";
            if (spinnerDepartament.getSelectedItem() != null) {
                departament = spinnerDepartament.getSelectedItem().toString();
            }

            String township = "";
            if (spinnerTownship.getSelectedItem() != null) {
                township = spinnerTownship.getSelectedItem().toString();
            }

            String typePrice = "";
            if (spinnerTypePrice.getSelectedItem() != null) {
                typePrice = spinnerTypePrice.getSelectedItem().toString();
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

            if (departament.equals("Seleccione") || departament.isEmpty()) {
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

            if (typePrice.equals("Seleccione") || typePrice.isEmpty()) {
                // Mostrar error en el Spinner de tipo de precio
                TextView errorText = (TextView) spinnerTypePrice.getSelectedView();
                if (errorText != null) {
                    errorText.setError("Seleccione un tipo de precio");
                    errorText.setTextColor(Color.RED); // Cambiar el color del texto a rojo
                }
                isValid = false;
            }

            // Si todos los campos son válidos, enviar datos al servidor
            if (isValid) {
                sendDataToServer(firstName, lastName, address, phoneNumber, departament, township, typePrice, latitude, longitude);
            }
        });        return view;
    }

    private void loadDepartamentos() {
        // Lista de los 18 departamentos de Honduras
        departamentos = Arrays.asList("Seleccione",
                "Atlántida", "Choluteca", "Colón", "Comayagua", "Copán", "Cortés",
                "El Paraíso", "Francisco Morazán", "Gracias a Dios", "Intibucá",
                "Islas de la Bahía", "La Paz", "Lempira", "Ocotepeque", "Olancho",
                "Santa Bárbara", "Valle", "Yoro"
        );


        // Municipios por departamento
        municipiosPorDepartamento.put("Atlántida", Arrays.asList(
                "La Ceiba", "Tela", "Jutiapa", "El Porvenir", "Esparta", "Arizona", "San Francisco"
        ));
        municipiosPorDepartamento.put("Choluteca", Arrays.asList(
                "Choluteca", "Pespire", "Nacaome", "San Marcos de Colón", "Duyure", "El Triunfo", "Concepción de María"
        ));
        municipiosPorDepartamento.put("Colón", Arrays.asList(
                "Trujillo", "Balfate", "Sonaguera", "Tocoa", "Bonito Oriental", "Santa Fe", "Iriona"
        ));
        municipiosPorDepartamento.put("Comayagua", Arrays.asList(
                "Comayagua", "Siguatepeque", "La Libertad", "San Jerónimo", "Esquías", "Humuya", "Ojos de Agua"
        ));
        municipiosPorDepartamento.put("Copán", Arrays.asList(
                "Santa Rosa de Copán", "Copán Ruinas", "Dulce Nombre", "San Agustín", "Concepción", "San Antonio", "Trinidad"
        ));
        municipiosPorDepartamento.put("Cortés", Arrays.asList(
                "San Pedro Sula", "Puerto Cortés", "Villanueva", "Choloma", "La Lima", "Omoa", "Pimienta"
        ));
        municipiosPorDepartamento.put("El Paraíso", Arrays.asList(
                "Yuscarán", "Danlí", "El Paraíso", "Texiguat", "Villa de San Francisco", "Morocelí", "Trojes"
        ));
        municipiosPorDepartamento.put("Francisco Morazán", Arrays.asList(
                "Tegucigalpa", "Comayagüela", "Valle de Ángeles", "Santa Lucía", "San Juancito", "Talanga", "Orica"
        ));
        municipiosPorDepartamento.put("Gracias a Dios", Arrays.asList(
                "Puerto Lempira", "Brus Laguna", "Ahuas", "Juan Francisco Bulnes", "Villeda Morales", "Wampusirpi", "Palacios"
        ));
        municipiosPorDepartamento.put("Intibucá", Arrays.asList(
                "La Esperanza", "Intibucá", "Yamaranguila", "San Juan", "San Marcos de la Sierra", "Magdalena", "Camasca"
        ));
        municipiosPorDepartamento.put("Islas de la Bahía", Arrays.asList(
                "Roatán", "Guanaja", "Utila", "José Santos Guardiola", "Santa Elena", "Santa Fe", "Juan Francisco"
        ));
        municipiosPorDepartamento.put("La Paz", Arrays.asList(
                "La Paz", "Marcala", "Cabañas", "San Pedro de Tutule", "Santa María", "San José", "Opatoro"
        ));
        municipiosPorDepartamento.put("Lempira", Arrays.asList(
                "Gracias", "Lepaera", "Erandique", "San Manuel Colohete", "San Rafael", "La Campa", "Talgua"
        ));
        municipiosPorDepartamento.put("Ocotepeque", Arrays.asList(
                "Ocotepeque", "Sensenti", "San Marcos", "La Encarnación", "San Francisco del Valle", "Concepción", "Dolores Merendón"
        ));
        municipiosPorDepartamento.put("Olancho", Arrays.asList(
                "Juticalpa", "Catacamas", "Campamento", "San Esteban", "Gualaco", "Guata", "Dulce Nombre de Culmí"
        ));
        municipiosPorDepartamento.put("Santa Bárbara", Arrays.asList(
                "Santa Bárbara", "Quimistán", "Ilama", "San Luis", "San José de Colinas", "Naranjito", "Gualala"
        ));
        municipiosPorDepartamento.put("Valle", Arrays.asList(
                "Nacaome", "San Lorenzo", "Langue", "Amapala", "Goascorán", "Alianza", "Aramecina"
        ));
        municipiosPorDepartamento.put("Yoro", Arrays.asList(
                "Yoro", "El Progreso", "Olanchito", "Morazán", "Victoria", "Jocón", "Santa Rita"
        ));
    }

    private void loadTiposPrecio() {
        // Lista de tipos de precio
        tiposPrecio = Arrays.asList(
                "Seleccione", // Placeholder
                "Precio A", "Precio B", "Precio C"
        );
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

    private void sendDataToServer(String firstName, String lastName, String address, String phoneNumber, String departament, String township, String typePrice, String latitude, String longitude) {
        // Aquí implementas la lógica para enviar los datos al servidor
        // Ejemplo con Retrofit:
        /*
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.createClient(firstName, lastName, address, phoneNumber, departament, township, typePrice, latitude, longitude);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Cliente creado exitosamente", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Error al crear el cliente", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
        */
    }
}