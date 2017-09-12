package com.example.key.my_carpathians.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import static com.example.key.my_carpathians.fragments.EditModeFragment.HARD;
import static com.example.key.my_carpathians.fragments.EditModeFragment.LIGHT;
import static com.example.key.my_carpathians.fragments.EditModeFragment.MEDIUM;

/**
 */
@EFragment
public class InfoFragment extends Fragment {
	Place place;
	Rout rout;
	View view;
	@ViewById(R.id.titleText)
	TextView titleText;

	@ViewById(R.id.textViewNameObject)
	TextView textViewNameObject;

	@ViewById(R.id.textRoutLength)
	TextView textRoutLength;

	@ViewById(R.id.textDifficultyValue)
	TextView textDifficultyValue;

	@ViewById(R.id.groupForRout)
	LinearLayout groupForRout;
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
	view = inflater.inflate(R.layout.fragment_info, container, false);
		return  view;
	}
	@AfterViews
	void afterViews(){
		if (place != null) {
			groupForRout.setVisibility(View.GONE);
			textViewNameObject.setText(place.getNamePlace());
			titleText.setText(place.getTitlePlace());
		}else if(rout != null){
			textViewNameObject.setText(rout.getNameRout());
			textDifficultyValue.setText(dificultyLevel(rout.getRoutsLevel()));
			textRoutLength.setText("12");
			titleText.setText(rout.getTitleRout());
		}
	}

	private String dificultyLevel(int routsLevel) {
		switch (routsLevel){
			case LIGHT:
				return "Light";
			case MEDIUM:
				return "Medium";
			case HARD:
				return "Hard";
			default:
				return "unknown";
		}

	}

	public void setData(Place place, Rout rout){
		this.place = place;
		this.rout = rout;
		if (view != null){
			afterViews();
		}
	}
}
