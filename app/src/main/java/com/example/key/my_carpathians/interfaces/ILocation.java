package com.example.key.my_carpathians.interfaces;

import android.location.Location;

/**
 * Created by Key on 03.07.2017.
 */

public interface ILocation {
    void update(Location location);
    void connectionState(int state);

}
