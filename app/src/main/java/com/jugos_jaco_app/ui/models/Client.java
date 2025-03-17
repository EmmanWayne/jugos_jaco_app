package com.jugos_jaco_app.ui.models;

import java.io.Serializable;

public class Client implements Serializable {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String latitude;
    private String longitude;
    private String adress;
    private String departament;
    private String township;
    private String id;
    private String typePrice;
    private String plus_code;




    public Client(String id,String firstName,  String lastName,String phoneNumber, String adress, String departament, String township, String latitude, String longitude,String plus_code ,String typePrice) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.adress = adress;
        this.departament = departament;
        this.township = township;
        this.typePrice = typePrice;
        this.plus_code = plus_code;

    }

    public String getId() {
        return id;
    }
    public String getTypePrice() {
        return typePrice;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public String getPlus_code() {
        return plus_code;
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLatitude() {
        return latitude;
    }
    public String getAdress() {
        return adress;
    }

    public String getLongitude() {
        return longitude;
    }
    public String getDepartment() {
        return departament;
    }
    public String getTownship() {
        return township;
    }

    public boolean hasCoordinates() {
        try {
            // Verificar que latitude y longitude no sean null y no estén vacíos
            if (latitude == null || longitude == null 
                || latitude.isEmpty() || longitude.isEmpty()) {
                return false;
            }
            
            // Intentar convertir a double para validar que sean números válidos
            double lat = Double.parseDouble(latitude);
            double lon = Double.parseDouble(longitude);
            
            // Verificar que no sean 0,0 (coordenadas inválidas)
            return !(lat == 0.0 && lon == 0.0);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}