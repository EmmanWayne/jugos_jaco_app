package com.jugos_jaco_app.api;

import com.jugos_jaco_app.ui.utilities.Utilities;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Crear interceptor para logging
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Crear cliente OkHttp con el interceptor
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

            // Crear instancia de Retrofit
            retrofit = new Retrofit.Builder()
                .baseUrl(Utilities.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
} 