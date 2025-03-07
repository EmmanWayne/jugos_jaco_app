package com.jugos_jaco_app.ui.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ServerPhotosResponse {
    @SerializedName("data")
    private List<ServerPhoto> photos;

    public List<ServerPhoto> getPhotos() {
        return photos;
    }

    public static class ServerPhoto {
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
} 