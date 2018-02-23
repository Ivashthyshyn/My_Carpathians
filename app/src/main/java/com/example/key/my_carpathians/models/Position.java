package com.example.key.my_carpathians.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

/**
 * Created by Key on 29.06.2017.
 */
@IgnoreExtraProperties
public class Position implements Serializable {
    public double latitude;
    public double longitude;

    public Position() {
    }
    public Position(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
