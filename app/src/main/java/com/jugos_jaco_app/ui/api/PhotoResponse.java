package com.jugos_jaco_app.ui.api;

import com.google.gson.annotations.SerializedName;

public class PhotoResponse {
    @SerializedName("data")
    private PhotoData data;
    
    @SerializedName("message")
    private String message;

    public static class PhotoData {
        @SerializedName("id")
        private int id;
        
        @SerializedName("type")
        private String type;
        
        @SerializedName("path")
        private String path;

        public int getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getPath() {
            return path;
        }
    }

    public PhotoData getData() {
        return data;
    }

    public String getUrl() {
        return data != null ? data.getPath() : null;
    }

    public String getMessage() {
        return message;
    }
} 