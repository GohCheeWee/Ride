package com.jby.ride.ride.comfirm;

public class ConfirmRideObject {
    private String pickUpPoint;
    private String dropOffPoint;
    private String paymentMethod;
    private String date;
    private String time;
    private String note;
    private String fare;
    private String id;
    private String status;
    private String driverName;
    private String driverImage;
    private String driverGender;
    private String driver_plate;
    private String driver_brand;
    private String driver_model;


    public ConfirmRideObject(String pickUpPoint, String dropOffPoint, String paymentMethod, String date, String time, String note, String fare, String id, String status, String driverName, String driverImage, String driverGender, String driver_plate, String driver_brand, String driver_model) {
        this.pickUpPoint = pickUpPoint;
        this.dropOffPoint = dropOffPoint;
        this.paymentMethod = paymentMethod;
        this.date = date;
        this.time = time;
        this.note = note;
        this.fare = fare;
        this.id = id;
        this.status = status;
        this.driverName = driverName;
        this.driverImage = driverImage;
        this.driverGender = driverGender;
        this.driver_plate = driver_plate;
        this.driver_brand = driver_brand;
        this.driver_model = driver_model;
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

    public String getStatus() {
        return status;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDriverImage() {
        return driverImage;
    }

    public String getDriverGender() {
        return driverGender;
    }

    public String getDriver_plate() {
        return driver_plate;
    }

    public String getDriver_brand() {
        return driver_brand;
    }

    public String getDriver_model() {
        return driver_model;
    }
}
