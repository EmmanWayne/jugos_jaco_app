package com.jugos_jaco_app.ui.api;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {
    @Multipart
    @POST("clients/{clientId}/image/business")
    Call<PhotoResponse> uploadBusinessImage(
        @Path("clientId") String clientId,
        @Part MultipartBody.Part image,
        @Header("Authorization") String token
    );
} 