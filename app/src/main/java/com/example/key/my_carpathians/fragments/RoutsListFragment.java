package com.example.key.my_carpathians.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.adapters.RoutsRecyclerAdapter;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;

import org.androidannotations.annotations.EFragment;

import java.util.List;

@EFragment
public class RoutsListFragment extends Fragment {
        RecyclerView recyclerView;
        LinearLayoutManager mLayoutManager;
        List<Place> mPlacesQuery;
        List<Rout> mRoutsQuery;
        @Nullable
        @Override
        public View onCreateView (LayoutInflater inflater, @Nullable ViewGroup container, Bundle
        savedInstanceState){
        View fragment = inflater.inflate(R.layout.fragment_routs_list, container, false);
        recyclerView = (RecyclerView) fragment.findViewById(R.id.recyclerViewForRout);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        Log.d("debugMode", "The application stopped after this");
        recyclerView.setLayoutManager(mLayoutManager);
        RoutsRecyclerAdapter recyclerAdapter = new RoutsRecyclerAdapter(mRoutsQuery , mPlacesQuery);
        recyclerView.setAdapter(recyclerAdapter);
        return fragment;
    }

    public void setList(List<Rout> routList, List<Place> placeList) {
        this.mRoutsQuery = routList;
        this.mPlacesQuery = placeList;

    }

}