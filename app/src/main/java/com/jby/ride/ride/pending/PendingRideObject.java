package com.jby.ride.ride.pending;

public class PendingRideObject {
    private String pickUpPoint;
    private String dropOffPoint;
    private String paymentMethod;
    private String date;
    private String time;
    private String note;
    private String fare;
    private String id;

    public PendingRideObject(String pickUpPoint, String dropOffPoint, String paymentMethod, String date, String time, String note, String fare, String id) {
        this.pickUpPoint = pickUpPoint;
        this.dropOffPoint = dropOffPoint;
        this.paymentMethod = paymentMethod;
        this.date = date;
        this.time = time;
        this.note = note;
        this.fare = fare;
        this.id = id;
    }

    public String getPickUpPoint() {
        return pickUpPoint;
    }

    public String getDropOffPoint() {
        return dropOffPoint;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getNote() {
        return note;
    }

    public String getFare() {
        return fare;
    }

    public String getId() {
        return id;
    }
}
