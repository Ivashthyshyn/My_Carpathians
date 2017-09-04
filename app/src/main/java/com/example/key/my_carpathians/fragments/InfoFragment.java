package com.example.key.my_carpathians.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 */
@EFragment
public class InfoFragment extends Fragment {
	Place place;
	Rout rout;

	@ViewById(R.id.titleText)
	TextView titleText;

	public InfoFragment() {
		// Required empty public constructor
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_info, container, false);
	}
	@AfterViews
	void afterViews(){
		if (place != null) {
			titleText.setText(place.getTitlePlace());
		}else if(rout != null){
			titleText.setText(rout.getTitleRout());
		}
	}

	public void setData(Place place, Rout rout){
		this.place = place;
		this.rout = rout;
	}
}
