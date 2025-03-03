package com.jugos_jaco_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jugos_jaco_app.ui.utilities.Utilities;
import com.google.android.material.button.MaterialButton;
import android.view.View;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;
import com.android.volley.DefaultRetryPolicy;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    // Declarar los EditText y el botón
    private EditText etIdentity, etPassword;
    private MaterialButton loginButton;

    // SharedPreferences para guardar el token y el estado de inicio de sesión
    private SharedPreferences sharedPreferences;
    public static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    public static final String KEY_TOKEN = "token";
    public static final String TOKEN_TYPE = "token_type";

    private static final String ID_EMPLEADO = "id_empleado";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Configurar el padding para la barra de sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Verificar si el usuario ya está logueado
        if (sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            // Si ya está logueado, redirigir a la actividad principal
            redirectToMainActivity();
            return; // Salir del método onCreate para evitar cargar la pantalla de login
        }

        // Inicializar los EditText y el botón
        etIdentity = findViewById(R.id.etIdentity);
        etPassword = findViewById(R.id.etPassword);
        loginButton = findViewById(R.id.loginButton);

        // Mantener solo el nuevo listener
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    loginUser();
                }
            }
        });
    }

    // Método para validar los campos
    private boolean validateFields() {
        String identity = etIdentity.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validar que el campo de identidad no esté vacío
        if (TextUtils.isEmpty(identity)) {
            etIdentity.setError("El campo de identidad es obligatorio");
            etIdentity.requestFocus(); // Mover el foco al campo de identidad
            return false;
        }

        // Validar que la identidad sea numérica y tenga al menos 13 dígitos
        if (!TextUtils.isDigitsOnly(identity)) {
            etIdentity.setError("La identidad debe ser numérica");
            etIdentity.requestFocus();
            return false;
        }

        if (identity.length() < 13) {
            etIdentity.setError("La identidad debe tener al menos 13 dígitos");
            etIdentity.requestFocus();
            return false;
        }

        // Validar que el campo de contraseña no esté vacío
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("El campo de contraseña es obligatorio");
            etPassword.requestFocus(); // Mover el foco al campo de contraseña
            return false;
        }

        // Si todo está bien, retornar true
        return true;
    }

    // Método para hacer la solicitud de inicio de sesión
    @SuppressLint("HardwareIds")
    private void loginUser() {
        setLoading(true);
        // URL del endpoint de inicio de sesión
        String url = Utilities.URL + "login";

        // Obtener los valores de los campos
        final String identity = etIdentity.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        // Crear un objeto JSON con los parámetros
        Map<String, String> params = new HashMap<>();
        params.put("identity", identity);
        params.put("password", password);
        params.put("device_name", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));

        JSONObject jsonParams = new JSONObject(params);

        // Crear una solicitud POST con Volley usando JsonObjectRequest
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonParams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Verificar si la respuesta contiene la clave "data"
                            if (response.has("data")) {
                                // Obtener el objeto "data"
                                JSONObject data = response.getJSONObject("data");

                                // Obtener los datos del objeto "data"
                                String token = data.getString("token");
                                String id_empleado = data.getString("employee_id");
                                String token_type = data.getString("token_type");

                                // Obtener el mensaje que está fuera del objeto "data"
                                String message = response.getString("message");

                                Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();

                                // Guardar el token y el estado de inicio de sesión en SharedPreferences
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean(KEY_IS_LOGGED_IN, true);
                                editor.putString(KEY_TOKEN, token);
                                editor.putString(ID_EMPLEADO, id_empleado);
                                editor.putString(TOKEN_TYPE, token_type);

                                editor.apply();

                                // Redirigir a la actividad principal
                                redirectToMainActivity();
                            } else {
                                // Manejar el caso en que no exista la clave "data"
                                showError("Respuesta del servidor incompleta");
                            }
                        } catch (JSONException e) {
                            showError("Error al procesar la respuesta: " + e.getMessage());
                            e.printStackTrace(); // Imprime el error en el log para depuración
                        } finally {
                            setLoading(false);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage = "Error de conexión";
                        if (error.networkResponse != null) {
                            try {
                                String errorBody = new String(error.networkResponse.data);
                                JSONObject errorJson = new JSONObject(errorBody);
                                if(errorJson.has("message")){
                                    errorMessage = errorJson.getString("message");
                                }else{
                                    errorMessage = "Error inesperado";
                                }
                            } catch (JSONException e) {
                                if (error.networkResponse.statusCode == 401) {
                                    errorMessage = "Usuario o contraseña incorrectos";
                                }
                            }
                        }
                        showError(errorMessage);
                        setLoading(false);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Agregar la solicitud a la cola de Volley
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }    // Método para redirigir a la actividad principal
    private void redirectToMainActivity() {
        Intent intent = new Intent(Login.this, MainActivity.class);
        startActivity(intent);
        finish(); // Cerrar la actividad de login para evitar que el usuario regrese con el botón "Atrás"
    }

    // Método para obtener el token guardado en SharedPreferences
    public static String getToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    // Método para agregar el token a las cabeceras de una solicitud
    public static void addTokenToHeaders(Map<String, String> headers, Context context) {
        String token = getToken(context);
        if (token != null) {
            headers.put("Authorization", "Bearer " + token);
        }
    }

    private boolean validateInputs() {
        String usuario = etIdentity.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (usuario.isEmpty()) {
            etIdentity.setError("Ingrese su usuario");
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Ingrese su contraseña");
            return false;
        }

        return true;
    }


    private void setLoading(boolean isLoading) {
        if (isLoading) {
            loginButton.setEnabled(false);
            loginButton.setText("Iniciando sesión...");
            // Opcional: Cambiar el ícono del botón por un progress indicator
            loginButton.setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_popup_sync));
            
            // Deshabilitar campos de entrada
            etIdentity.setEnabled(false);
            etPassword.setEnabled(false);
        } else {
            loginButton.setEnabled(true);
            loginButton.setText("Iniciar Sesión");
            loginButton.setIcon(null);
            
            // Habilitar campos de entrada
            etIdentity.setEnabled(true);
            etPassword.setEnabled(true);
        }
    }

    private void showError(String message) {
        // Mostrar error usando Snackbar (más moderno que Toast)
        Snackbar.make(loginButton, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(this, R.color.error_color))
                .setTextColor(ContextCompat.getColor(this, R.color.white))
                .show();
    }
    // Añadir este método helper

    public static String getAuthorizationHeader(Context context) {


        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String token = sharedPreferences.getString(KEY_TOKEN, null);
        String tokenType = sharedPreferences.getString(TOKEN_TYPE, "Bearer");

        if (token != null) {
            return tokenType + " " + token;
        }
        return null;
    }

}