package com.keyVas.key.my_carpathians.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.keyVas.key.my_carpathians.R;
import com.keyVas.key.my_carpathians.adapters.AroundObjectListAdapter;
import com.keyVas.key.my_carpathians.models.Position;
import com.keyVas.key.my_carpathians.models.Rout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.keyVas.key.my_carpathians.activities.SettingsActivity.AVERAGE_VALUE;
import static com.keyVas.key.my_carpathians.activities.SettingsActivity.VALUE_ROUT_AROUND_RADIUS;
import static com.keyVas.key.my_carpathians.activities.StartActivity.PREFS_NAME;

@EFragment
public class RoutsAroundFragment extends Fragment {
    public static final double CONSTANT_PERIMETER_SIZE = 0.01;
    Position position;
    Rout rout;
    List<Rout> routList;
    List<Rout> routsAround;
    View view;
    RecyclerView recyclerView;
    TextView textTitleRoutAround;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_routs_around, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewRoutAround);
        textTitleRoutAround = view.findViewById(R.id.textTitleRoutAround);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());
        Log.d("debugMode", "The application stopped after this");
        recyclerView.setLayoutManager(mLayoutManager);
        return view;
    }

    @AfterViews
    public void afterView() {
        searchRoutAround();
        if (routsAround != null && routsAround.size() == 0) {
            textTitleRoutAround.setText(getString(R.string.no_rout_around));
        } else {
            AroundObjectListAdapter recyclerAdapter = new AroundObjectListAdapter(null, routsAround);
            recyclerView.setAdapter(recyclerAdapter);
        }
    }


    public void setData(Rout rout, List<Rout> routList, Position position) {
        this.rout = rout;
        this.routList = routList;
        this.position = position;

    }

    private void searchRoutAround() {
        String name;
        if (rout == null) {
            name = "";
        } else {
            name = rout.routKey();
        }
        routsAround = new ArrayList<>();
        List<String> routsAroundName = new ArrayList<>();
        if (routList.size() > 0) {
            for (int i = 0; i < routList.size(); i++) {
                Rout mRout = routList.get(i);
                double lat = mRout.getPositionRout().getLatitude();
                double lng = mRout.getPositionRout().getLongitude();
                SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                double perimeterValueForLongitude = CONSTANT_PERIMETER_SIZE *
                        sharedPreferences.getInt(VALUE_ROUT_AROUND_RADIUS, AVERAGE_VALUE);
                if (position.getLongitude() + perimeterValueForLongitude > lng
                        && position.getLongitude() - perimeterValueForLongitude < lng
                        && position.getLatitude() + perimeterValueForLongitude > lat
                        && position.getLatitude() - perimeterValueForLongitude < lat
                        && !name.equals(mRout.routKey())) {
                    routsAround.add(mRout);
                    routsAroundName.add(mRout.routKey());
                }
            }
        }
        if (routsAround.size() != 0) {
            //Todo need to add image to viewpager about no rout around
        } else {
        }
    }
}
