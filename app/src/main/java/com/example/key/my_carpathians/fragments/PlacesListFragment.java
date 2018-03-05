package com.example.key.my_carpathians.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter;
import com.example.key.my_carpathians.interfaces.IRotation;
import com.example.key.my_carpathians.models.Place;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import java.util.ArrayList;
import java.util.List;

import static com.example.key.my_carpathians.activities.StartActivity.FA_PLACE;
import static com.example.key.my_carpathians.activities.StartActivity.FA_ROUT;
import static com.example.key.my_carpathians.activities.StartActivity.MY_PLACE;
import static com.example.key.my_carpathians.activities.StartActivity.MY_ROUT;


/**
 * Created by Key on 10.06.2017.
 */
@EFragment
public class PlacesListFragment extends Fragment implements IRotation {
	RecyclerView recyclerView;
	PlacesRecyclerAdapter recyclerAdapter;
	CardView emptyView;
	private List<Place> mSearchList;
	private View fragmentView;
	@FragmentArg
	ArrayList<Place> placeList;
	@FragmentArg
	int mMode;
	private ImageView imageForEmptyView;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		fragmentView = inflater.inflate(R.layout.fragment_places_list, container, false);
		emptyView = (CardView) fragmentView.findViewById(R.id.emptyViewForPlace);
		imageForEmptyView = (ImageView)fragmentView.findViewById(R.id.emptyImage);
		recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recyclerViewForPlace);
		return fragmentView;
	}


	private void updateLayoutManager() {
		boolean isPortrait = getResources().getBoolean(R.bool.orientation_portrait);
		getActivity().getChangingConfigurations();
		if (isPortrait) {
			LinearLayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());
			recyclerView.setLayoutManager(mLayoutManager);
		} else {
			GridLayoutManager gridLayoutManager = new GridLayoutManager(this.getActivity(), 2);
			recyclerView.setLayoutManager(gridLayoutManager);
		}
	}
	@AfterViews
	public void afterView(){
		updateLayoutManager();
		createList();
	}

	private void createList() {
		if (placeList != null && placeList.size() > 0) {
			recyclerAdapter = new PlacesRecyclerAdapter(placeList, mMode);
			recyclerView.setAdapter(recyclerAdapter);

		}
	}


	public void setList(ArrayList<Place> placeList, int mode) {
		this.placeList = placeList;
		this.mMode = mode;
		if (recyclerAdapter != null & placeList != null) {
			recyclerAdapter.setList(placeList, mode);
			recyclerView.removeAllViews();
			if (placeList.size() == 0) {
				emptyView.setVisibility(View.VISIBLE);
				imageForEmptyView.setImageResource(selectImageResourceFromMode(mode));
			} else {
				emptyView.setVisibility(View.GONE);
			}
		} else if (fragmentView != null) {
			createList();
		}
	}

	private int selectImageResourceFromMode(int mode) {
		if (mode == MY_PLACE | mode == MY_ROUT ) {
			return R.drawable.ic_create_black_24px;
		}else if (mode == FA_PLACE | mode == FA_ROUT){
			return R.drawable.ic_favorite;
		}
		return R.drawable.ic_map_marker;
	}


	public void dismissActionMode() {

		if (recyclerAdapter != null && recyclerAdapter.ismMode()) {
			recyclerAdapter.removeSelection();  // remove selection
			recyclerAdapter.setNullToActionMode();
		}
	}


	public void filter(String query) {
		if (query != null) {
			query = query.toLowerCase();
			mSearchList = new ArrayList<>();
			for (Place place : placeList) {
				final String text = place.getNamePlace().toLowerCase();
				if (text.contains(query)) {
					mSearchList.add(place);
				}
			}
			recyclerAdapter.setList(mSearchList, mMode);
		} else {
			recyclerAdapter.setList(placeList, mMode);
		}
	}

	@Override
	public void onRotation() {
		updateLayoutManager();
	}
}
