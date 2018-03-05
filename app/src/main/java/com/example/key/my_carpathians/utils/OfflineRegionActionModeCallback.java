package com.example.key.my_carpathians.utils;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.interfaces.CommunicatorMapActivity;

/**
 * Created by key on 31.10.17.
 */

public class OfflineRegionActionModeCallback implements ActionMode.Callback {
	private Context context;



	public OfflineRegionActionModeCallback(Context context) {
		this.context = context;

	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mode.getMenuInflater().inflate(R.menu.action_mode_offline_region, menu);//Inflate the menu over action mode
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

		//So here show action menu according to SDK Levels
		if (Build.VERSION.SDK_INT < 11) {
			MenuItemCompat.setShowAsAction(menu.findItem(R.id.actionDownload), MenuItemCompat.SHOW_AS_ACTION_NEVER);


		} else {
			menu.findItem(R.id.actionDownload).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		}
		mode.setTitle("set a point" );
		return true;
	}


	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		CommunicatorMapActivity communicatorMapActivity = (CommunicatorMapActivity) context;

			switch (item.getItemId()) {
				case R.id.actionDownload:
					communicatorMapActivity.actionDownloadRegion();
					break;
				case R.id.actactionDellRegion:
					communicatorMapActivity.actionDelRegion();
					break;
			}

		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		CommunicatorMapActivity communicatorMapActivity = (CommunicatorMapActivity) context;
		communicatorMapActivity.deleteActionOfflineRegion();
		communicatorMapActivity.autoOrientationOff(false);
	}
}
