package com.jby.ride.Object;

import com.google.android.gms.maps.model.LatLng;

public class MarkerObject {
    private LatLng location;
    private String locationName;
    private String locationAddress;

    public MarkerObject(LatLng location, String locationName, String locationAddress) {
        this.location = location;
        this.locationName = locationName;
        this.locationAddress = locationAddress;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationAddress() {
        return locationAddress;
    }
}
