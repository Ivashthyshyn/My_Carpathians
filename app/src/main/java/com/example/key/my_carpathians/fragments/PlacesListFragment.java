package com.example.key.my_carpathians.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter;
import com.example.key.my_carpathians.models.Place;

import org.androidannotations.annotations.EFragment;

import java.util.List;


/**
 * Created by Key on 10.06.2017.
 */
@EFragment
public class PlacesListFragment extends Fragment {
    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    List<Place> mPlacesQuery;
    PlacesRecyclerAdapter recyclerAdapter;
    private boolean mMode;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_places_list, container, false);
        recyclerView = (RecyclerView) fragment.findViewById(R.id.recyclerViewForPlace);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerAdapter = new PlacesRecyclerAdapter(mPlacesQuery, mMode);
        recyclerView.setAdapter(recyclerAdapter);
        return fragment;
    }
    public void setList(List<Place> placeList, boolean mode){
       this.mPlacesQuery = placeList;
       this.mMode = mode;
        if (recyclerAdapter != null){
	        recyclerAdapter.setList(placeList, mode);
            recyclerAdapter.notifyDataSetChanged();
        }
    }

}
