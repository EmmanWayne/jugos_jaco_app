package com.jugos_jaco_app.ui.api;

import com.google.gson.annotations.SerializedName;

public class MessageResponse {
    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }
} 