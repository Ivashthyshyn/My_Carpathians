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
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import static com.example.key.my_carpathians.fragments.EditModeFragment.HARD;
import static com.example.key.my_carpathians.fragments.EditModeFragment.LIGHT;
import static com.example.key.my_carpathians.fragments.EditModeFragment.MEDIUM;

/**
 */
@EFragment
public class InfoFragment extends Fragment {
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

	@FragmentArg
	Place place;

	@FragmentArg
	Rout rout;

	public void setPlace(Place place) {
		this.place = place;
	}

	public void setRout(Rout rout) {
		this.rout = rout;
	}

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
			textDifficultyValue.setText(difficultyLevel(rout.getRoutsLevel()));
			textRoutLength.setText(rout.getLengthRout() + "km");
			titleText.setText(rout.getTitleRout());
		}
	}

	private String difficultyLevel(int routsLevel) {
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
}
