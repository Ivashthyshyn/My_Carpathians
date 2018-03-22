package com.keyVas.key.my_carpathians.interfaces;

/**
 * Created by key on 26.10.17.
 */

public interface CommunicatorMapActivity {
	void undoAction();
	void saveAction();
	void deleteActionForHand();
	void autoOrientationOff(boolean on);
	void actionDelRegion();
	void actionStartRecTrack();
	void actionStopRecTrack();
	void actionDownloadRegion();
	void actionSaveLocation();
	void enabledProgressGPS(boolean b, int type);
	void deleteActionForGPS(int type);
	void actionRefreshLocation();
	void deleteActionOfflineRegion();
	void setNullActionMode();
}
