package com.keyVas.key.my_carpathians.interfaces;

import android.location.Location;

/**
 * Created by Key on 03.07.2017.
 */

public interface ILocation {
    void update(Location location, int type);
    void connectionState(int state);
}
