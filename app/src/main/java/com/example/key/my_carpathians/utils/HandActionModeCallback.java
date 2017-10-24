package com.example.key.my_carpathians.utils;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.example.key.my_carpathians.R;

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
			MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_delete), MenuItemCompat.SHOW_AS_ACTION_NEVER);
		} else {
			menu.findItem(R.id.action_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		}

		return true;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {

	}
}
