package com.jugos_jaco_app.ui.api;

import com.google.gson.annotations.SerializedName;

public class PhotoResponse {
    @SerializedName("data")
    private PhotoData data;
    
    @SerializedName("message")
    private String message;

    public static class PhotoData {
        @SerializedName("path")
        private String path;

        public String getPath() {
            return path;
        }
    }

    public String getUrl() {
        return data != null ? data.getPath() : null;
    }

    public String getMessage() {
        return message;
    }
} 