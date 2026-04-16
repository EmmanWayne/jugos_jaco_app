package com.jugos_jaco_app.ui.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class Utilities {
    public static String URL = "https://eaa9-45-170-32-59.ngrok-free.app/api/";

    public static int id_cliente = 0;

    public static String URL_FOTOS="https://eaa9-45-170-32-59.ngrok-free.app/" +
            "/";
    
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String TOKEN_TYPE = "token_type";
    
    public static Map<String, String> getAuthHeaders(Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", getAuthorizationHeader(context));
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");
        return headers;
    }
    
    private static String getAuthorizationHeader(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String token = sharedPreferences.getString(KEY_TOKEN, null);
        String tokenType = sharedPreferences.getString(TOKEN_TYPE, "Bearer");
        
        if (token != null) {
            return tokenType + " " + token;
        }
        return null;
    }
  }
