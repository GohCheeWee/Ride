package com.jby.ride.ride.comfirm.object;

import android.os.Parcel;
import android.os.Parcelable;

public class OtherRiderInCarObject implements Parcelable {
    private String username, gender, user_id, profile_picture;

    public OtherRiderInCarObject(String username, String gender, String user_id, String profile_picture) {
        this.username = username;
        this.gender = gender;
        this.user_id = user_id;
        this.profile_picture = profile_picture;
    }
    public String getUsername() {
        return username;
    }

    public String getGender() {
        return gender;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getProfile_picture() {
        return profile_picture;
    }

    protected OtherRiderInCarObject(Parcel in) {
        username = in.readString();
        gender = in.readString();
        user_id = in.readString();
        profile_picture = in.readString();
    }

    public static final Creator<OtherRiderInCarObject> CREATOR = new Creator<OtherRiderInCarObject>() {
        @Override
        public OtherRiderInCarObject createFromParcel(Parcel in) {
            return new OtherRiderInCarObject(in);
        }

        @Override
        public OtherRiderInCarObject[] newArray(int size) {
            return new OtherRiderInCarObject[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(username);
        parcel.writeString(gender);
        parcel.writeString(user_id);
        parcel.writeString(profile_picture);
    }
}
