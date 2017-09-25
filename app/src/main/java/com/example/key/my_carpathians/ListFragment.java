package com.example.key.my_carpathians;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.EFragment;

import java.util.List;


/**
 * Created by Key on 10.06.2017.
 */
@EFragment
public class ListFragment extends Fragment {
    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    List<?> mPlacesQuery;
    int type = 0;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_recycler_list, container, false);
        recyclerView = (RecyclerView) fragment.findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        Log.d("debugMode", "The application stopped after this");
        recyclerView.setLayoutManager(mLayoutManager);
        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(mPlacesQuery, type);
        recyclerView.setAdapter(recyclerAdapter);
        return fragment;
    }
    public void setList(List<?> listQuery, int type){
       this.mPlacesQuery = listQuery;
       this.type = type;

    }
}
