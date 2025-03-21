package com.jugos_jaco_app.ui.api;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.GET;
import retrofit2.http.DELETE;

public interface ApiService {
    @Multipart
    @POST("clients/{clientId}/image/business")
    Call<PhotoResponse> uploadBusinessImage(
        @Path("clientId") String clientId,
        @Part MultipartBody.Part image,
        @Header("Authorization") String token
    );

    @GET("clients/{id}/images/business")
    Call<ServerPhotosResponse> getClientImages(
        @Path("id") String clientId,
        @Header("Authorization") String token
    );

    @DELETE("media/{id}")
    Call<MessageResponse> deleteMedia(
        @Path("id") int mediaId,
        @Header("Authorization") String token
    );

    @Multipart
    @POST("{id}/image/profile")
    Call<PhotoResponse> uploadProfileImage(
        @Path("id") String clientId,
        @Part MultipartBody.Part image,
        @Header("Authorization") String token
    );
} 