package com.jby.ride.Object;

import android.os.Parcel;
import android.os.Parcelable;

public class AddressObject implements Parcelable {
    private String fullAddress;
    private String streetName;

    public AddressObject(String fullAddress, String streetName) {
        this.fullAddress = fullAddress;
        this.streetName = streetName;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public String getStreetName() {
        return streetName;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int arg1) {
        // TODO Auto-generated method stub
        dest.writeString(fullAddress);
        dest.writeString(streetName);
    }

    private AddressObject(Parcel in) {
        fullAddress = in.readString();
        streetName = in.readString();
    }

    public static final Parcelable.Creator<AddressObject> CREATOR = new Parcelable.Creator<AddressObject>() {
        public AddressObject createFromParcel(Parcel in) {
            return new AddressObject(in);
        }

        public AddressObject[] newArray(int size) {
            return new AddressObject[size];
        }
    };
}
