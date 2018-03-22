package com.keyVas.key.my_carpathians.utils;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.keyVas.key.my_carpathians.R;
import com.keyVas.key.my_carpathians.adapters.PlacesRecyclerAdapter;
import com.keyVas.key.my_carpathians.adapters.RoutsRecyclerAdapter;
import com.keyVas.key.my_carpathians.models.Place;
import com.keyVas.key.my_carpathians.models.Rout;

import java.util.List;

import static com.keyVas.key.my_carpathians.activities.StartActivity.FA_PLACE;
import static com.keyVas.key.my_carpathians.activities.StartActivity.FA_ROUT;
import static com.keyVas.key.my_carpathians.activities.StartActivity.MY_PLACE;
import static com.keyVas.key.my_carpathians.activities.StartActivity.MY_ROUT;
import static com.keyVas.key.my_carpathians.activities.StartActivity.PLACE;
import static com.keyVas.key.my_carpathians.activities.StartActivity.ROUT;

/**
 * Created by key on 20.10.17.
 */

public class ToolbarActionModeCallback implements ActionMode.Callback {

	private Context context;
	private PlacesRecyclerAdapter placesRecyclerAdapter;
	private RoutsRecyclerAdapter routsRecyclerAdapter;
	private List<Place> placeArrayList;
	private List<Rout> routArrayList;
	private int mType;


	public ToolbarActionModeCallback(Context context, PlacesRecyclerAdapter placesRecyclerAdapter, RoutsRecyclerAdapter routsRecyclerAdapter, List<Place> placeArrayList, List<Rout> routArrayList, int type) {
		this.context = context;
		this.placesRecyclerAdapter = placesRecyclerAdapter;
		this.routsRecyclerAdapter = routsRecyclerAdapter;
		this.placeArrayList = placeArrayList;
		this.routArrayList = routArrayList;
		this.mType = type;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mode.getMenuInflater().inflate(R.menu.action_mode_start_activity, menu);//Inflate the menu over action mode
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

		//Sometimes the meu will not be visible so for that we need to set their visibility manually in this method
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
		switch (item.getItemId()) {
			case R.id.action_delete:

				//Check if current action mode is from ListView Fragment or RecyclerView Fragment
				 if (mType == MY_PLACE){
					placesRecyclerAdapter.deletePlaceFromCreated();//delete selected rows
				}else if (mType == MY_ROUT){
					routsRecyclerAdapter.deleteRoutFromCreated();//delete selected rows
				}else if (mType == FA_PLACE){
					 placesRecyclerAdapter.deletePlaceFromFavorite();
				}else if (mType == FA_ROUT){
					 routsRecyclerAdapter.deleteRoutFromFavorite();
				}
				break;
		}
		return false;
	}


	@Override
	public void onDestroyActionMode(ActionMode mode) {

		//When action mode destroyed remove selected selections and set action mode to null
		//First check current fragment action mode
		if (mType == PLACE | mType == FA_PLACE | mType == MY_PLACE) {
			placesRecyclerAdapter.removeSelection();  // remove selection
			placesRecyclerAdapter.setNullToActionMode();//Set action mode null
		} else if(mType == ROUT | mType == FA_ROUT | mType == MY_ROUT) {
			routsRecyclerAdapter.removeSelection();  // remove selection
			routsRecyclerAdapter.setNullToActionMode();//Set action mode null

		}
	}

}
