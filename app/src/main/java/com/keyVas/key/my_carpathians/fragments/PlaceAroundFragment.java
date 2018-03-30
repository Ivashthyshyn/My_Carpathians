package com.keyVas.key.my_carpathians.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.keyVas.key.my_carpathians.R;
import com.keyVas.key.my_carpathians.adapters.AroundObjectListAdapter;
import com.keyVas.key.my_carpathians.models.Place;
import com.keyVas.key.my_carpathians.models.Position;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.keyVas.key.my_carpathians.activities.MapsActivity.CONSTANT_PERIMETER_SIZE;
import static com.keyVas.key.my_carpathians.activities.SettingsActivity.AVERAGE_VALUE;
import static com.keyVas.key.my_carpathians.activities.SettingsActivity.VALUE_PLACE_AROUND_RADIUS;
import static com.keyVas.key.my_carpathians.activities.StartActivity.PREFS_NAME;

@EFragment
public class PlaceAroundFragment extends Fragment {
	Place place;
	List<Place> placeList;
	RecyclerView recyclerView;
	TextView textTitlePlacesAround;
	List<Place> placesAround;
	Position position;
	AroundObjectListAdapter recyclerAdapter;

	public PlaceAroundFragment() {
		// Required empty public constructor
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	void searchPlacesAround() {
		String myName;
		if(place == null) {
			myName = "";
		}else{
			myName = place.placeKey();
		}
		placesAround = new ArrayList<>();
		List<String> placesAroundName = new ArrayList<>();
		if(placeList.size() > 0) {
			for (int i = 0; i < placeList.size(); i++) {
				Place mPlace = placeList.get(i);
				double lat = mPlace.getPositionPlace().getLatitude();
				double lng = mPlace.getPositionPlace().getLongitude();
				SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
				double perimeterValueForLongitude = CONSTANT_PERIMETER_SIZE *
						sharedPreferences.getInt(VALUE_PLACE_AROUND_RADIUS, AVERAGE_VALUE);
				if (position.getLongitude() + perimeterValueForLongitude > lng
						&& position.getLongitude() - perimeterValueForLongitude < lng
						&& position.getLatitude() + perimeterValueForLongitude > lat
						&& position.getLatitude() - perimeterValueForLongitude < lat
						&& !myName.equals(mPlace.placeKey())) {
					placesAround.add(mPlace);
					placesAroundName.add(mPlace.placeKey());
				}
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_place_around, container, false);
		recyclerView =  view.findViewById(R.id.recyclerViewPlaceAround);
		textTitlePlacesAround = view.findViewById(R.id.textTitlePlaceAround);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());
		Log.d("debugMode", "The application stopped after this");
		recyclerView.setLayoutManager(mLayoutManager);
		return view;
	}

	@AfterViews
	public void afterView(){
		searchPlacesAround();
		if(placesAround != null && placesAround.size() == 0) {
			textTitlePlacesAround.setText(getString(R.string.no_place_around));
		}else {
			recyclerAdapter = new AroundObjectListAdapter(placesAround, null);
			recyclerView.setAdapter(recyclerAdapter);

		}
	}



	public void setData(Place place, List<Place> placeList, Position myPosition){
		this.place = place;
		this.placeList = placeList;
		this.position = myPosition;

	}
}
