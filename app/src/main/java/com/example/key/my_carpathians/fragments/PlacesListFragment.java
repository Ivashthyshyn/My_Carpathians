package com.example.key.my_carpathians.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter;
import com.example.key.my_carpathians.models.Place;

import org.androidannotations.annotations.EFragment;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Key on 10.06.2017.
 */
@EFragment
public class PlacesListFragment extends Fragment  {
    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    List<Place> placeList;
    PlacesRecyclerAdapter recyclerAdapter;
	CardView emptyView;
    private int mMode;
    private ActionMode mActionMode;
    private List<Place> mSearchList;
    private View fragmentView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_places_list, container, false);
        emptyView = (CardView)fragmentView.findViewById(R.id.emptyViewForPlace);
        recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recyclerViewForPlace);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        createList();
        return fragmentView;
    }

    private void createList() {
        if (placeList != null && placeList.size() > 0) {
            recyclerAdapter = new PlacesRecyclerAdapter(placeList, mMode);
            recyclerView.setAdapter(recyclerAdapter);

        }
    }



    public void setList(List<Place> placeList, int mode){
       this.placeList = placeList;
       this.mMode = mode;
        if (recyclerAdapter != null & placeList != null) {
            recyclerAdapter.setList(placeList, mode);
            recyclerView.removeAllViews();
            if (placeList.size() == 0) {
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.GONE);
            }
        }else if(fragmentView != null){
            createList();
        }
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
        }else{
            recyclerAdapter.setList(placeList, mMode);
        }
    }
}
