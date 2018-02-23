package com.example.key.my_carpathians.interfaces;

import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;

/**
 * Created by key on 28.08.17.
 */

public interface CommunicatorActionActivity {
	void saveChanges(Rout rout, Place place);
	void addToMap(Rout rout, Place place);
	void autoOrientationOff(boolean checker);

}
