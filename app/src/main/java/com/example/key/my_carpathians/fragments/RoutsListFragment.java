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

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.adapters.RoutsRecyclerAdapter;
import com.example.key.my_carpathians.interfaces.IRotation;
import com.example.key.my_carpathians.models.Rout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import java.util.ArrayList;
import java.util.List;

@EFragment
public class RoutsListFragment extends Fragment implements IRotation {
	RecyclerView recyclerView;
	RoutsRecyclerAdapter recyclerAdapter;
	CardView emptyView;
	@FragmentArg
	ArrayList<Rout> mRoutsList;
	@FragmentArg
	int mMode;
	private List<Rout> mSearchList;
	private View fragmentView;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle
			savedInstanceState) {
		fragmentView = inflater.inflate(R.layout.fragment_routs_list, container, false);
		emptyView = (CardView) fragmentView.findViewById(R.id.emptyViewForRout);
		recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recyclerViewForRout);

		return fragmentView;
	}

	private void createList() {
		if (mRoutsList != null && mRoutsList.size() > 0) {
			recyclerAdapter = new RoutsRecyclerAdapter(mRoutsList, mMode);
			recyclerView.setAdapter(recyclerAdapter);

		}
	}

	@AfterViews
	public void afterViews(){
		updateLayoutManager();
		createList();
	}
	public void setList(ArrayList<Rout> routList, int mode) {
		this.mRoutsList = routList;
		this.mMode = mode;
		if (recyclerAdapter != null & routList != null) {
			recyclerAdapter.setList(routList, mode);
			recyclerView.removeAllViews();
			if (routList.size() == 0) {
				emptyView.setVisibility(View.VISIBLE);
			} else {
				emptyView.setVisibility(View.GONE);
			}
		} else if (fragmentView != null) {
			createList();
		}
	}


	public void dismissActionMode() {

		if (recyclerAdapter != null && recyclerAdapter.ismMode()) {
			recyclerAdapter.removeSelection();
			recyclerAdapter.setNullToActionMode();
		}
	}

	public void filter(String query) {
		if (query != null && !query.equals("")) {
			query = query.toLowerCase();
			mSearchList = new ArrayList<>();
			for (Rout rout : mRoutsList) {
				final String text = rout.getNameRout().toLowerCase();
				if (text.contains(query)) {
					mSearchList.add(rout);
				}
			}
			recyclerAdapter.setList(mSearchList, mMode);
		} else {
			recyclerAdapter.setList(mRoutsList, mMode);
		}
	}

	@Override
	public void onRotation() {
		updateLayoutManager();
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
}