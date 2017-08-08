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
import com.example.key.my_carpathians.interfaces.Comunicator;

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
    public Comunicator comunicator;
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
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_favorites, container, false);
        final ListView listOfPlaces = (ListView)view.findViewById(R.id.listViewPlace);
        if (listPlaces != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, listPlaces);
            listOfPlaces.setAdapter(adapter);
            listOfPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    comunicator = (Comunicator)getActivity();
                    comunicator.putStringNamePlace(listPlaces.get(i));
                }
            });
        }
        final ListView listOfRouts = (ListView) view.findViewById(R.id.listViewRout);
        if (this.listRouts != null) {
            ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, this.listRouts);
            listOfRouts.setAdapter(adapter1);
            listOfRouts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    comunicator = (Comunicator)getActivity();
                    comunicator.putStringNameRout(listRouts.get(i) );
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

                    ArrayList<String> selectedUserTrack = new ArrayList<>();
                    selectedUserTrack.add(listTrack.get(i));
                    Intent mapsActivityIntent = new Intent(getContext(), MapsActivity_.class);
                    mapsActivityIntent.putStringArrayListExtra(SELECTED_USER_ROUTS, selectedUserTrack);
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
