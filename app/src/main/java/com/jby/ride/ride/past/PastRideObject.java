package com.jby.ride.ride.past;

public class PastRideObject {
    private String pickUpPoint;
    private String dropOffPoint;
    private String date;
    private String time;
    private String fare;
    private String id;

    public PastRideObject(String pickUpPoint, String dropOffPoint, String date, String time, String fare, String id) {
        this.pickUpPoint = pickUpPoint;
        this.dropOffPoint = dropOffPoint;
        this.date = date;
        this.time = time;
        this.fare = fare;
        this.id = id;
    }

    public String getPickUpPoint() {
        return pickUpPoint;
    }

    public String getDropOffPoint() {
        return dropOffPoint;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getFare() {
        return fare;
    }

    public String getId() {
        return id;
    }
}
