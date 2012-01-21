package com.alfsimen.bybuss;

public class busStop {
    private int busStopId;
    private String name;
    private String nameWithAbbreviations;
    private String busStopMaintainer;
    private String locationId;
    private double longitude;
    private double latitude;

    public busStop() {
    }

    public int getBusStopId() {
        return busStopId;
    }

    public void setBusStopId(int busStopId) {
        this.busStopId = busStopId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameWithAbbreviations() {
        return nameWithAbbreviations;
    }

    public void setNameWithAbbreviations(String nameWithAbbreviations) {
        this.nameWithAbbreviations = nameWithAbbreviations;
    }

    public String getBusStopMaintainer() {
        return busStopMaintainer;
    }

    public void setBusStopMaintainer(String busStopMaintainer) {
        this.busStopMaintainer = busStopMaintainer;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(long longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }
}
