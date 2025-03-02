package com.jugos_jaco_app.ui.fragments_client;

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




    public Client(String id,String firstName,  String lastName,String phoneNumber, String adress, String departament, String township, String latitude, String longitude,String typePrice) {
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
        return latitude != null && longitude != null;
    }
}