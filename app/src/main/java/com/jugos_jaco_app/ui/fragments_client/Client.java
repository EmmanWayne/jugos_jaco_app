package com.jugos_jaco_app.ui.fragments_client;

public class Client {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String latitude;
    private String longitude;
    private String adress;
    private String departament;
    private String township;



    public Client(String firstName,  String lastName,String phoneNumber, String adress, String departament, String township, String latitude, String longitude) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.adress = adress;
        this.departament = departament;
        this.township = township;

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
    public String getDepartament() {
        return departament;
    }
    public String getTownship() {
        return township;
    }

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }
}