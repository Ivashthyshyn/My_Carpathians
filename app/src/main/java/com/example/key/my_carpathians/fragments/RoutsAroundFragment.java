package com.example.key.my_carpathians.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.adapters.CheckListAdapter;
import com.example.key.my_carpathians.models.Position;
import com.example.key.my_carpathians.models.Rout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

import java.util.ArrayList;
import java.util.List;

import static com.example.key.my_carpathians.activities.MapsActivity.PERIMETER_SIZE_TO_LATITUDE;
import static com.example.key.my_carpathians.activities.MapsActivity.PERIMETER_SIZE_TO_LONGITUDE;

@EFragment
public class RoutsAroundFragment extends Fragment {
	Position position;
	Rout rout;
	List<Rout> routList;
	List<Rout> routsAround;
	View view;
	RecyclerView recyclerView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_routs_around, container, false);
		recyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewRoutAround);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());
		Log.d("debugMode", "The application stopped after this");
		recyclerView.setLayoutManager(mLayoutManager);
		return view;
	}

	@AfterViews
	public void afterView(){
		CheckListAdapter recyclerAdapter = new CheckListAdapter( null, routsAround);
		recyclerView.setAdapter(recyclerAdapter);
	}


	public void setData(Rout rout, List<Rout> routList, Position position) {
		this.rout = rout;
		this.routList = routList;
		this.position = position;
		searchRoutAround();
	}

	private void searchRoutAround() {
		String name;
		if (rout == null) {
			name = "";
		} else{
			name = rout.getNameRout();
		}
		routsAround = new ArrayList<>();
		List<String> routsAroundName = new ArrayList<>();
		for (int i = 0; i < routList.size(); i++) {
			Rout mRout = routList.get(i);
			double lat = mRout.getPositionRout().getLatitude();
			double lng = mRout.getPositionRout().getLongitude();
			if (position.getLongitude() + PERIMETER_SIZE_TO_LONGITUDE > lng
					&& position.getLongitude() - PERIMETER_SIZE_TO_LONGITUDE < lng
					&& position.getLatitude() + PERIMETER_SIZE_TO_LATITUDE > lat
					&& position.getLatitude() - PERIMETER_SIZE_TO_LATITUDE < lat
					&& !name.equals(mRout.getNameRout())) {
				routsAround.add(mRout);
				routsAroundName.add(mRout.getNameRout());
			}
		}
		if (routsAround.size() != 0) {
			//Todo need to add image to viewpager about no rout around
		} else {
		}
	}
}
