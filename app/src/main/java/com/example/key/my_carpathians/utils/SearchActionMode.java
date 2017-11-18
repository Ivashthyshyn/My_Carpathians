package com.example.key.my_carpathians.utils;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.fragments.PlacesListFragment;
import com.example.key.my_carpathians.fragments.RoutsListFragment;

import java.util.List;

import static com.example.key.my_carpathians.activities.StartActivity.PLACE;
import static com.example.key.my_carpathians.activities.StartActivity.ROUT;

/**
 * Created by key on 18.11.17.
 */

public class SearchActionMode implements ActionMode.Callback, SearchView.OnQueryTextListener {
	private final Context context;
	private final android.support.v4.app.Fragment fragment;
	private final int mType;
	private List<Object> mSearchList;
	private String mQuery;

	public SearchActionMode(Context context, Fragment fragment, int type) {
		this.context = context;
		this.fragment = fragment;
		this.mType = type;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mode.getMenuInflater().inflate(R.menu.menu_main, menu);
		MenuItem item = menu.findItem(R.id.actionSearch);
		final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
		searchView.setOnQueryTextListener(this);
		MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				if (mType == PLACE && mQuery != null){
					PlacesListFragment placesListFragment = (PlacesListFragment)fragment;
					placesListFragment.filter(mQuery);
				}else if (mType == ROUT && mQuery != null){
					RoutsListFragment routsListFragment = (RoutsListFragment) fragment;
					routsListFragment.filter(mQuery);
				}

			return true;
			}
		});
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {

	}
	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		mQuery = newText;
		if (mType == PLACE){
			PlacesListFragment placesListFragment = (PlacesListFragment)fragment;
			placesListFragment.filter(newText);
		}else if (mType == ROUT){
			RoutsListFragment routsListFragment = (RoutsListFragment) fragment;
			routsListFragment.filter(newText);
		}
		return true;
	}




}
