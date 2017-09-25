package com.example.key.my_carpathians.database;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Key on 29.06.2017.
 */
@IgnoreExtraProperties
public class Position {
    public double longitude;
    public double latitude;

    public Position() {
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
