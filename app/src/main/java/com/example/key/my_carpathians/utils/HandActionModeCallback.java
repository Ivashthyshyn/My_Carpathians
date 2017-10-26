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
 * Created by key on 24.10.17.
 */

public class HandActionModeCallback implements ActionMode.Callback {
	private Context context;

	private int mType;


	public HandActionModeCallback(Context context, int type) {
		this.context = context;
		this.mType = type;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mode.getMenuInflater().inflate(R.menu.action_mode_hand, menu);//Inflate the menu over action mode
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

		//So here show action menu according to SDK Levels
		if (Build.VERSION.SDK_INT < 11) {
			MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_beck), MenuItemCompat.SHOW_AS_ACTION_NEVER);
			MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_save), MenuItemCompat.SHOW_AS_ACTION_NEVER);
			MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_del), MenuItemCompat.SHOW_AS_ACTION_NEVER);

		} else {
			menu.findItem(R.id.action_beck).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.findItem(R.id.action_save).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.findItem(R.id.action_del).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);


		}
		if (mType == PLACE){
			MenuItem undoAction = menu.findItem(R.id.action_beck);
			undoAction.setVisible(false);
			MenuItem actionSave = menu.findItem(R.id.action_save);
			actionSave.setVisible(true);
			MenuItem actionDelete =menu.findItem(R.id.action_del);
			actionDelete.setVisible(true);
		}else if(mType == ROUT){
			MenuItem undoAction = menu.findItem(R.id.action_beck);
			undoAction.setVisible(true);
			MenuItem actionSave = menu.findItem(R.id.action_save);
			actionSave.setVisible(true);
			MenuItem actionDelete =menu.findItem(R.id.action_del);
			actionDelete.setVisible(true);
		}


		return true;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		CommunicatorMapActivity communicatorMapActivity = (CommunicatorMapActivity) context;
		if (mType == PLACE) {
			switch (item.getItemId()) {
				case R.id.action_save:
					communicatorMapActivity.saveAction();
					break;
				case R.id.action_beck:
					communicatorMapActivity.undoAction();
					break;
				case R.id.action_del:
					communicatorMapActivity.undoAction();
					break;
			}
		}else if(mType == ROUT){
			switch (item.getItemId()) {
				case R.id.action_save:
					communicatorMapActivity.saveAction();
					break;
				case R.id.action_beck:
					communicatorMapActivity.undoAction();
					break;
				case R.id.action_del:
					communicatorMapActivity.deleteAction();
					break;
			}
		}
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		CommunicatorMapActivity communicatorMapActivity = (CommunicatorMapActivity) context;
		if(mType == PLACE){
			communicatorMapActivity.undoAction();
		}else if(mType == ROUT){
			communicatorMapActivity.deleteAction();
		}
		communicatorMapActivity.autoOrientationOff(false);
	}
}
