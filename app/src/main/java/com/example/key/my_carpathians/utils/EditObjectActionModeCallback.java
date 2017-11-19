package com.example.key.my_carpathians.utils;


import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.fragments.EditModeFragment;

/**
 * Created by key on 18.11.17.
 */

public class EditObjectActionModeCallback implements ActionMode.Callback{
	private final Context context;
	private EditModeFragment fragment;
	private final FragmentManager fm;

	public EditObjectActionModeCallback(Context context, EditModeFragment fragment, FragmentManager fm) {
		this.context = context;
		this.fragment = fragment;
		this.fm = fm;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mode.getMenuInflater().inflate(R.menu.menu_action_activity, menu);
		mode.setTitle("Edit");
		mode.setSubtitle("please fill in the form");
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		menu.findItem(R.id.action_add_to_favorites).setVisible(false).setEnabled(false);
		menu.findItem(R.id.action_publish).setVisible(false).setEnabled(false);
		menu.findItem(R.id.action_edit).setVisible(false).setEnabled(false);
		menu.findItem(R.id.action_del_created_object).setVisible(true).setEnabled(true);
		android.support.v4.app.FragmentTransaction fragmentTransaction = fm
				.beginTransaction();
		fragmentTransaction.add(R.id.actionActivityContainer, fragment);
		fragmentTransaction.commit();
		return true;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_del_created_object:
				fragment.deleteCreatedObject();
				break;
		}
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		fragment.dismiss();
	}
}
