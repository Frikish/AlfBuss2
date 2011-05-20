package com.alfsimen.bybuss;

public class Holdeplass {
    private int id;
    private String name;
    private double latitude;
    private double longitude;

    public Holdeplass(int id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Holdeplass() {

    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLatitude(double lat) {
        this.latitude = lat;
    }

    public void setLongtitude(double lon) {
        this.longitude = lon;
    }

    public String getName() {
        return this.name;
    }

    public double getLat() {
        return this.latitude;
    }

    public double getLon() {
        return this.longitude;
    }

    @Override
    public String toString() {
        return "Name: " + name + "\nLatitude: " + latitude + "\nLongitude: " + longitude;
    }
}