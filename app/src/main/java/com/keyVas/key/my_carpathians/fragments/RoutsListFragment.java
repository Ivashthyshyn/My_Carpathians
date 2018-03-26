package com.keyVas.key.my_carpathians.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.keyVas.key.my_carpathians.R;
import com.keyVas.key.my_carpathians.adapters.RoutsRecyclerAdapter;
import com.keyVas.key.my_carpathians.interfaces.IRotation;
import com.keyVas.key.my_carpathians.models.Rout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import java.util.ArrayList;
import java.util.List;

import static com.keyVas.key.my_carpathians.activities.StartActivity.FA_PLACE;
import static com.keyVas.key.my_carpathians.activities.StartActivity.FA_ROUT;
import static com.keyVas.key.my_carpathians.activities.StartActivity.MY_PLACE;
import static com.keyVas.key.my_carpathians.activities.StartActivity.MY_ROUT;
import static com.keyVas.key.my_carpathians.models.Place.EN;
import static com.keyVas.key.my_carpathians.utils.LocaleHelper.SELECTED_LANGUAGE;

@EFragment
public class RoutsListFragment extends Fragment implements IRotation {
	RecyclerView recyclerView;
	RoutsRecyclerAdapter recyclerAdapter;
	CardView emptyView;
	@FragmentArg
	ArrayList<Rout> mRoutsList;
	@FragmentArg
	int mMode;
	private View fragmentView;
	private ImageView imageForEmptyView;
	private String mUserLanguage;
	private boolean isPortrait;

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
		emptyView = fragmentView.findViewById(R.id.emptyViewForRout);
		imageForEmptyView = fragmentView.findViewById(R.id.emptyImageRoutesList);
		recyclerView = fragmentView.findViewById(R.id.recyclerViewForRout);

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
		checkOrientation(getResources().getConfiguration());
		mUserLanguage = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(SELECTED_LANGUAGE, EN);
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
		return R.drawable.ic_route;
	}

	public void dismissActionMode() {

		if (recyclerAdapter != null && recyclerAdapter.ismMode()) {
			recyclerAdapter.removeSelection();
			recyclerAdapter.setNullToActionMode();
		}
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		checkOrientation(newConfig);

	}
	private void checkOrientation(Configuration configuration) {
		int currentOrientation = configuration.orientation;

		isPortrait = (currentOrientation == Configuration.ORIENTATION_PORTRAIT);

	}
	public void filter(String query) {
		if (query != null && !query.equals("")) {
			query = query.toLowerCase();
			List<Rout> mSearchList = new ArrayList<>();
			for (Rout rout : mRoutsList) {
				final String text = rout.getNameRout(mUserLanguage).toLowerCase();
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