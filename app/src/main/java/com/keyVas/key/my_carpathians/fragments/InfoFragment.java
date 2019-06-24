package com.keyVas.key.my_carpathians.fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.keyVas.key.my_carpathians.R;
import com.keyVas.key.my_carpathians.models.Place;
import com.keyVas.key.my_carpathians.models.Rout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import static com.keyVas.key.my_carpathians.fragments.EditModeFragment.HARD;
import static com.keyVas.key.my_carpathians.fragments.EditModeFragment.LIGHT;
import static com.keyVas.key.my_carpathians.fragments.EditModeFragment.MEDIUM;
import static com.keyVas.key.my_carpathians.models.Place.EN;
import static com.keyVas.key.my_carpathians.utils.LocaleHelper.SELECTED_LANGUAGE;

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

	public InfoFragment() {
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
		String mUserLanguage = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(SELECTED_LANGUAGE, EN);
		if (place != null) {
			groupForRout.setVisibility(View.GONE);
			textViewNameObject.setText(place.getNamePlace(mUserLanguage));
			titleText.setText(place.getTitlePlace(mUserLanguage));
		}else if(rout != null){
			textViewNameObject.setText(rout.getNameRout(mUserLanguage));
			textDifficultyValue.setText(difficultyLevel(rout.getRoutsLevel()));
			if (rout.getLengthRout() != null) {
				String mRoutLength = rout.getLengthRout() + getString(R.string.km);
				textRoutLength.setText(mRoutLength);
			}
			titleText.setText(rout.getTitleRout(mUserLanguage));
		}
	}

	public void setPlace(Place place) {
		this.place = place;
	}
	public void setRout(Rout rout) {
		this.rout = rout;
	}

	private String difficultyLevel(int routsLevel) {
		switch (routsLevel){
			case LIGHT:
				textDifficultyValue.setTextColor(getResources().getColor(R.color.color_level_green ));
				return getString(R.string.light);
			case MEDIUM:
				textDifficultyValue.setTextColor(getResources().getColor(R.color.color_level_yellow));
				return getString(R.string.medium);
			case HARD:
				textDifficultyValue.setTextColor(getResources().getColor(R.color.color_level_red ));
				return getString(R.string.hard);
			default:
				textDifficultyValue.setTextColor(getResources().getColor(R.color.background_material_light));
				return getString(R.string.unknown);
		}

	}
}
