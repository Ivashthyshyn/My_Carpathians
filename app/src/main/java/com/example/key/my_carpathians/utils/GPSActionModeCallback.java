package com.example.key.my_carpathians.utils;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.interfaces.CommunicatorMapActivity;

import static com.example.key.my_carpathians.activities.StartActivity.PLACE;
import static com.example.key.my_carpathians.activities.StartActivity.ROUT;

/**
 * Created by key on 26.10.17.
 */

public class GPSActionModeCallback implements ActionMode.Callback {
	private Context context;
	private int mType;


	public GPSActionModeCallback(Context context, int type) {
		this.context = context;
		this.mType = type;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mode.getMenuInflater().inflate(R.menu.action_mode_gps, menu);//Inflate the menu over action mode
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

		//So here show action menu according to SDK Levels
		if (Build.VERSION.SDK_INT < 11) {
			MenuItemCompat.setShowAsAction(menu.findItem(R.id.actionSaveRecord), MenuItemCompat.SHOW_AS_ACTION_NEVER);
			MenuItemCompat.setShowAsAction(menu.findItem(R.id.actionStop), MenuItemCompat.SHOW_AS_ACTION_NEVER);
			MenuItemCompat.setShowAsAction(menu.findItem(R.id.actionStartRec), MenuItemCompat.SHOW_AS_ACTION_NEVER);

		} else {
			menu.findItem(R.id.actionSaveRecord).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.findItem(R.id.actionStop).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.findItem(R.id.actionStartRec).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.findItem(R.id.recIndicator).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		}
		if (mType == PLACE){
			MenuItem saveAction = menu.findItem(R.id.actionSaveRecord);
			saveAction.setVisible(false);
			saveAction.setEnabled(false);
			MenuItem actionPause = menu.findItem(R.id.actionStop);
			actionPause.setIcon(R.drawable.ic_refresh_48px);
			actionPause.setVisible(false);
			actionPause.setEnabled(false);
			MenuItem actionStartRec =menu.findItem(R.id.actionStartRec);
			actionStartRec.setVisible(false);
			actionStartRec.setEnabled(false);
			CommunicatorMapActivity communicatorMapActivity = (CommunicatorMapActivity) context;
			communicatorMapActivity.enabledProgressGPS(true, mType);
		}else if(mType == ROUT){
			MenuItem saveAction = menu.findItem(R.id.actionSaveRecord);
			saveAction.setVisible(false);
			saveAction.setEnabled(false);
			MenuItem actionPause = menu.findItem(R.id.actionStop);
			actionPause.setIcon(R.drawable.ic_media_stop_dark);
			actionPause.setVisible(false);
			actionPause.setEnabled(false);
			MenuItem actionStartRec =menu.findItem(R.id.actionStartRec);
			actionStartRec.setVisible(false);
			actionStartRec.setEnabled(false);
			MenuItem recIndicator =menu.findItem(R.id.recIndicator);
			recIndicator.setVisible(false);
			recIndicator.setEnabled(false);
			CommunicatorMapActivity communicatorMapActivity = (CommunicatorMapActivity) context;
			communicatorMapActivity.enabledProgressGPS(true, mType);
		}


		return true;
	}


	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		CommunicatorMapActivity communicatorMapActivity = (CommunicatorMapActivity) context;
		if (mType == PLACE) {
			switch (item.getItemId()) {
				case R.id.actionSaveRecord:
					communicatorMapActivity.actionSaveLocation();
					break;
				case R.id.actionStop:
					communicatorMapActivity.actionRefreshLocation();
					break;
			}
		}else if(mType == ROUT){
			switch (item.getItemId()) {
				case R.id.actionStartRec:
					communicatorMapActivity.actionStartRecTrack();
					break;
				case R.id.actionStop:
					communicatorMapActivity.actionStopRecTrack();
					break;
			}
		}
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		CommunicatorMapActivity communicatorMapActivity = (CommunicatorMapActivity) context;
		if(mType == PLACE){
			communicatorMapActivity.enabledProgressGPS(false, mType);
			communicatorMapActivity.deleteActionForGPS();

		}else if(mType == ROUT){
			communicatorMapActivity.enabledProgressGPS(false, mType);
			communicatorMapActivity.deleteActionForGPS();
		}
		communicatorMapActivity.autoOrientationOff(false);
	}
}
