package com.jugos_jaco_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    // Declarar los EditText y el botón
    private EditText etIdentity, etPassword;
    private Button btnLogin;

    // SharedPreferences para guardar el token y el estado de inicio de sesión
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_TOKEN = "token";

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
        btnLogin = findViewById(R.id.btnLogin);

        // Configurar el clic del botón de inicio de sesión
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validar los campos antes de hacer la solicitud
                if (validateFields()) {
                    // Si todas las validaciones pasan, ejecutar el método para loguearse
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
    private void loginUser() {
        // URL del endpoint de inicio de sesión
        String url = Utilities.URL+"login";

        // Obtener los valores de los campos
        final String identity = etIdentity.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        // Crear un objeto JSON con los parámetros
        Map<String, String> params = new HashMap<>();
        params.put("id_modelo", identity);
        params.put("password", password);
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
                            // Obtener el token del servidor desde la respuesta JSON
                            String token = response.getString("token");
                            Toast.makeText(Login.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                            // Guardar el token y el estado de inicio de sesión en SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(KEY_IS_LOGGED_IN, true);
                            editor.putString(KEY_TOKEN, token);
                            editor.apply();

                            // Redirigir a la actividad principal
                            redirectToMainActivity();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(Login.this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Manejar el error de la solicitud
                        try {
                            // Obtener el mensaje de error del cuerpo de la respuesta
                            String errorMessage = new String(error.networkResponse.data);
                            JSONObject errorResponse = new JSONObject(errorMessage);
                            String message = errorResponse.getString("message");
                            Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(Login.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("TAGASIEMPRE",""+error.getMessage());
                        }
                    }
                }
        );

        // Agregar la solicitud a la cola de Volley
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    // Método para redirigir a la actividad principal
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
}