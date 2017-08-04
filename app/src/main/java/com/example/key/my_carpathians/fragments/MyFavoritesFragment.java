package com.example.key.my_carpathians.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.activities.MapsActivity_;

import java.util.ArrayList;
import java.util.Set;

import static com.example.key.my_carpathians.activities.ActionActivity.SELECTED_USER_ROUTS;
import static com.example.key.my_carpathians.activities.StartActivity.FAVORITES_PLACE_LIST;
import static com.example.key.my_carpathians.activities.StartActivity.FAVORITES_ROUTS_LIST;
import static com.example.key.my_carpathians.utils.LocationService.CREATED_BY_USER_TRACK_LIST;

/**
 *
 */
public class MyFavoritesFragment extends Fragment {

    public ArrayList<String> listPlaces;
    public ArrayList<String> listRouts;
    public ArrayList<String> listTrack;
    public MyFavoritesFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_favorites, container, false);
        ListView listPlace = (ListView)view.findViewById(R.id.listViewPlace);
        if (listPlaces != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, listPlaces);
            listPlace.setAdapter(adapter);
            listPlace.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                }
            });
        }
        ListView listPlace1 = (ListView) view.findViewById(R.id.listViewRout);
        if (listRouts != null) {
            ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, listRouts);
            listPlace1.setAdapter(adapter1);
            listPlace1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                }
            });
        }
        ListView listOfTrack = (ListView) view.findViewById(R.id.listViewTrack);
        if (listTrack != null) {
            ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_list_item_1, listTrack);
            listOfTrack.setAdapter(adapter2);
            listOfTrack.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    ArrayList<String> selectedUserRout = new ArrayList<>();
                    selectedUserRout.add(listTrack.get(i));
                    Intent mapsActivityIntent = new Intent(getContext(), MapsActivity_.class);
                    mapsActivityIntent.putStringArrayListExtra(SELECTED_USER_ROUTS, selectedUserRout);
                    startActivity(mapsActivityIntent);
                }
            });
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setList(Set<String> list, String type){
        switch (type){
            case FAVORITES_PLACE_LIST: this.listPlaces =  new ArrayList<>(list);
                return;
            case FAVORITES_ROUTS_LIST: this.listRouts = new ArrayList<>(list);
                return;
            case CREATED_BY_USER_TRACK_LIST: this.listTrack = new ArrayList<>(list);
                return;
        }

    }

}
