package com.jugos_jaco_app.ui.gallery;

public class Client {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String latitude;
    private String longitude;
    private String adress;


    public Client(String firstName,String adress, String lastName, String phoneNumber, String latitude, String longitude) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.adress = adress;

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

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }
}