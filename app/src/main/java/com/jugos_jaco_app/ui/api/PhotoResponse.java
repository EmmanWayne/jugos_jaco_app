package com.jugos_jaco_app.ui.api;

import com.google.gson.annotations.SerializedName;

public class PhotoResponse {
    @SerializedName("url")
    private String url;
    
    @SerializedName("message")
    private String message;

    public String getUrl() {
        return url;
    }

    public String getMessage() {
        return message;
    }
} 