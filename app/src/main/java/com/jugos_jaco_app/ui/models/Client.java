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
    private String businessName;
    private String position;
    private String visitDay;
    private String profileImage;
    private int countAccountReceivable;
    private double totalAccountReceivable;

    public Client(String id, String firstName, String lastName, String phoneNumber, String adress, 
                 String departament, String township, String latitude, String longitude, 
                 String plus_code, String typePrice, String businessName, String position, String visitDay,
                 String profileImage, int countAccountReceivable, double totalAccountReceivable) {
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
        this.businessName = businessName;
        this.position = position;
        this.visitDay = visitDay;
        this.profileImage = profileImage;
        this.countAccountReceivable = countAccountReceivable;
        this.totalAccountReceivable = totalAccountReceivable;
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

    public String getBusinessName() {
        return businessName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getVisitDay() {
        return visitDay;
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

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public int getCountAccountReceivable() {
        return countAccountReceivable;
    }

    public void setCountAccountReceivable(int countAccountReceivable) {
        this.countAccountReceivable = countAccountReceivable;
    }

    public double getTotalAccountReceivable() {
        return totalAccountReceivable;
    }

    public void setTotalAccountReceivable(double totalAccountReceivable) {
        this.totalAccountReceivable = totalAccountReceivable;
    }

    public boolean hasActiveCredits() {
        return countAccountReceivable > 0 && totalAccountReceivable > 0;
    }
}