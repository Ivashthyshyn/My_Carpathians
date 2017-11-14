package com.example.key.my_carpathians.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.adapters.AroundObjectListAdapter;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Position;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

import java.util.ArrayList;
import java.util.List;

import static com.example.key.my_carpathians.activities.MapsActivity.PERIMETER_SIZE_TO_OFFLINE_REGION;
import static com.example.key.my_carpathians.activities.MapsActivity.PERIMETER_SIZE_TO_LONGITUDE;

@EFragment
public class PlaceAroundFragment extends Fragment {
	Place place;
	List<Place> placeList;
	RecyclerView recyclerView;
	TextView textTitlePlacceAround;
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
			myName = place.getNamePlace();
		}
		placesAround = new ArrayList<>();
		List<String> placesAroundName = new ArrayList<>();
		for (int i = 0; i < placeList.size(); i++) {
			Place mPlace = placeList.get(i);
			double lat = mPlace.getPositionPlace().getLatitude();
			double lng = mPlace.getPositionPlace().getLongitude();
			if (position.getLongitude() + PERIMETER_SIZE_TO_LONGITUDE > lng
					&& position.getLongitude() - PERIMETER_SIZE_TO_LONGITUDE < lng
					&& position.getLatitude() + PERIMETER_SIZE_TO_OFFLINE_REGION > lat
					&& position.getLatitude() - PERIMETER_SIZE_TO_OFFLINE_REGION < lat
					&& !myName.equals(mPlace.getNamePlace())) {
				placesAround.add(mPlace);
				placesAroundName.add(mPlace.getNamePlace());
			}
		}
		if (placesAround.size() != 0) {

		} else {

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_place_around, container, false);
		recyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewPlaceAround);
		textTitlePlacceAround = (TextView)view.findViewById(R.id.textTitlePlaceAround);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());
		Log.d("debugMode", "The application stopped after this");
		recyclerView.setLayoutManager(mLayoutManager);
		return view;
	}

	@AfterViews
	public void afterView(){
		if(placesAround.size() == 0) {
			textTitlePlacceAround.setText("Поблизу немає жодних місць");
		}else {
			recyclerAdapter = new AroundObjectListAdapter(placesAround, null);
			recyclerView.setAdapter(recyclerAdapter);
		}
	}



	public void setData(Place place, List<Place> placeList, Position myPosition){
		this.place = place;
		this.placeList = placeList;
		this.position = myPosition;
		searchPlacesAround();
	}
}
