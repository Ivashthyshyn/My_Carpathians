package com.example.key.my_carpathians.interfaces;

/**
 * Created by key on 26.10.17.
 */

public interface CommunicatorMapActivity {
	void undoAction();
	void saveAction();
	void deleteActionForHand();
	void autoOrientationOff(boolean on);
	void getLocationPosition();
	void actionStartRecTrack();
	void actionStopRecTrack();
	void actionPauseRecTrack();
	void actionSaveRecTrack();
	void actionSaveLocation();

	void enabledProgressGPS(boolean b, int type);

	void deleteActionForGPS();

	void actionRefreshLocation();
}
