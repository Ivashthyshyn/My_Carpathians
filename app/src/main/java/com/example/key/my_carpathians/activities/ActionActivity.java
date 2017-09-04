package com.example.key.my_carpathians.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.ArraySet;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.adapters.ViewPagerAdapter;
import com.example.key.my_carpathians.fragments.EditModeFragment;
import com.example.key.my_carpathians.fragments.EditModeFragment_;
import com.example.key.my_carpathians.fragments.InfoFragment;
import com.example.key.my_carpathians.fragments.InfoFragment_;
import com.example.key.my_carpathians.fragments.PlaceAroundFragment;
import com.example.key.my_carpathians.fragments.PlaceAroundFragment_;
import com.example.key.my_carpathians.fragments.RoutsAroundFragment;
import com.example.key.my_carpathians.fragments.RoutsAroundFragment_;
import com.example.key.my_carpathians.interfaces.CommunicatorActionActivity;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.mapbox.services.api.utils.turf.TurfConstants;
import com.mapbox.services.api.utils.turf.TurfMeasurement;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.commons.utils.TextUtils;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.widget.Toast.LENGTH_LONG;
import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
import static com.example.key.my_carpathians.R.id.graph;
import static com.example.key.my_carpathians.activities.StartActivity.FAVORITES_PLACE_LIST;
import static com.example.key.my_carpathians.activities.StartActivity.FAVORITES_ROUTS_LIST;
import static com.example.key.my_carpathians.activities.StartActivity.PREFS_NAME;
import static com.example.key.my_carpathians.activities.StartActivity.PRODUCE_MODE;
import static com.example.key.my_carpathians.activities.StartActivity.PUT_EXTRA_PLACE_LIST;
import static com.example.key.my_carpathians.activities.StartActivity.PUT_EXTRA_ROUTS_LIST;
import static com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter.ViewHolder.PUT_EXTRA_PLACE;
import static com.example.key.my_carpathians.adapters.RoutsRecyclerAdapter.PUT_EXTRA_POINTS;
import static com.example.key.my_carpathians.adapters.RoutsRecyclerAdapter.RoutsViewHolder.PUT_EXTRA_ROUT;

@EActivity
public class ActionActivity extends AppCompatActivity implements CommunicatorActionActivity {

    public static final String SELECTED_USER_ROUTS = "selected-user_routs";
    public static final String SELECTED_USER_PLACES = "selected_user_places";
    public List<Rout> routList;
    public List<Place> placeList;
    public List<Position> pointsRout;
    public Place myPlace;
    public Rout myRout;
    public com.example.key.my_carpathians.models.Position myPosition;
    public String myName;
    public ArrayList<String> selectedUserRouts = new ArrayList<>();
    public Set<String> selectedUserPlacesStringList = new ArraySet<>();
    private SharedPreferences sharedPreferences;
    private ArrayList<Place> selectedUserPlacesList = new ArrayList<>();
	private boolean mTypeMode = false;
    public  Toolbar toolbar;
    public ViewPager viewPager;
    @ViewById(R.id.imageView)
    ImageView imageView;
    @ViewById(R.id.textName)
    TextView textName;

    @ViewById(graph)
    GraphView graphView;
	@ViewById(R.id.buttonShowOnMap)
    FloatingActionButton buttonShowOnMap;
	@ViewById(R.id.buttonAddToFavorites)
	FloatingActionButton buttonAddToFavorites;
	@ViewById(R.id.buttonShowPhoto)
    ImageButton buttonShowPhoto;
	@ViewById (R.id.buttonEdit)
	FloatingActionButton buttonEdit;
	@ViewById(R.id.buttonPublish)
	FloatingActionButton buttonPublish;
    private boolean flagButtonShoePhoto = true;
    private TabLayout tabLayout;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout.setupWithViewPager(viewPager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());


        sharedPreferences = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        routList = (List<Rout>) getIntent().getSerializableExtra(PUT_EXTRA_ROUTS_LIST);
        placeList = (List<Place>) getIntent().getSerializableExtra(PUT_EXTRA_PLACE_LIST);
        pointsRout = (List<Position>)getIntent().getSerializableExtra(PUT_EXTRA_POINTS);
        myPlace = (Place) getIntent().getSerializableExtra(PUT_EXTRA_PLACE);
        myRout = (Rout) getIntent().getSerializableExtra(PUT_EXTRA_ROUT);
	    mTypeMode = getIntent().getBooleanExtra(PRODUCE_MODE, false);
	    if (mTypeMode){
            setBaseInformation(myPlace, myRout);
            InfoFragment infoFragment = new InfoFragment_();
            adapter.addFragment(infoFragment, "Info");
            infoFragment.setData(myPlace, myRout);
            viewPager.setAdapter(adapter);
		    buttonAddToFavorites.setVisibility(View.GONE);
            buttonPublish.setVisibility(View.VISIBLE);
            viewPager.setCurrentItem(0);


	    }else {
            setBaseInformation(myPlace, myRout);
            PlaceAroundFragment placeAroundFragment = new PlaceAroundFragment_();
            adapter.addFragment(placeAroundFragment, "PLACE AROUND");
            placeAroundFragment.setData(myPlace, placeList, myPosition);
            InfoFragment infoFragment = new InfoFragment_();
            adapter.addFragment(infoFragment, "INFO");
            infoFragment.setData(myPlace, myRout);
            RoutsAroundFragment routsAroundFragment = new RoutsAroundFragment_();
            adapter.addFragment(routsAroundFragment, "ROUT AROUND");
            routsAroundFragment.setData(myRout, routList, myPosition);
            viewPager.setAdapter(adapter);
		    buttonShowPhoto.setVisibility(View.GONE);
		    buttonEdit.setVisibility(View.GONE);
		    buttonPublish.setVisibility(View.GONE);
            viewPager.setCurrentItem(1);
            viewPager.setOffscreenPageLimit(2);

	    }
    }

	private void setBaseInformation(Place place, Rout rout) {
		if (place != null) {
			textName.setText(myPlace.getNamePlace());

			graphView.setVisibility(View.GONE);
			Glide
					.with(ActionActivity.this)
					.load("file:/storage/sdcard0/Android/data/com.example.key.my_carpathians/files/Download/Photos/" + myPlace.getNamePlace())
					.into(imageView);
			myPosition = myPlace.getPositionPlace();
			myName = myPlace.getNamePlace();
		} else if (rout != null) {
			textName.setText(myRout.getNameRout());

            imageView.setVisibility(View.GONE);
            if (myRout.getUrlRout() == null){
                buttonShowPhoto.setAlpha((float)0.5);
            }
            Glide
                    .with(ActionActivity.this)
                    .load(rout.getUrlRout())
                    .into(imageView);
            if (mTypeMode) {
                createDataPoint(URI.create(myRout.getUrlRoutsTrack()));
            }else
                createDataPoint(URI.create(getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .getString(myRout.getNameRout(), null)));
			myPosition = myRout.getPositionRout();
			myName = myRout.getNameRout();
		}
	}

	//Todo need optimise code
    private void createDataPoint(URI uriRoutTrack) {
        List<Position> points = new ArrayList<>();
        try {
            // Load GeoJSON file
            File file = new File(uriRoutTrack);
            InputStream fileInputStream = new FileInputStream(file);
            BufferedReader rd = new BufferedReader(new InputStreamReader(fileInputStream, Charset.forName("UTF-8")));
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }

            fileInputStream.close();

            // Parse JSON
            JSONObject json = new JSONObject(sb.toString());
            JSONArray features = json.getJSONArray("features");
            JSONObject feature = features.getJSONObject(0);
            JSONObject geometry = feature.getJSONObject("geometry");
            if (geometry != null) {
                String type = geometry.getString("type");

                // Our GeoJSON only has one feature: a line string
                if (!TextUtils.isEmpty(type) && type.equalsIgnoreCase("LineString")) {

                    // Get the Coordinates
                    JSONArray coords = geometry.getJSONArray("coordinates");
                    for (int lc = 0; lc < coords.length(); lc++) {
                        JSONArray coord = coords.getJSONArray(lc);
                        Position position = Position.fromCoordinates(coord.getDouble(1), coord.getDouble(0), coord.getDouble(2));
                        points.add(position);
                    }
                }
            }
        } catch (Exception exception) {
            Log.e(TAG, "Exception Loading GeoJSON: " + exception.toString());
        }
        int size = points.size();
        DataPoint[] values = new DataPoint[size];
        Integer xi = 0;
        for (int i=1; i<size; i++) {
            Integer yi = (int)points.get(i).getAltitude();
            xi = xi + (int)TurfMeasurement.distance(points.get(i -1),points.get(i),TurfConstants.UNIT_METERS);
            DataPoint v = new DataPoint(xi, yi);
            values[i] = v;
        }
        values[0] = new DataPoint(0,(int)points.get(0).getAltitude());
        LineGraphSeries series = new LineGraphSeries<DataPoint>(values);
        series.setThickness(8);
        graphView.addSeries(series);
        GridLabelRenderer gridLabel = graphView.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("meters");
        gridLabel.setVerticalAxisTitle("meters");


    }

    @Click(R.id.buttonShowOnMap)
    public void buttonShowOnMapWasClicked() {
        if(mTypeMode) {
            if (myPlace != null) {
                selectedUserPlacesList.add(myPlace);
            }
            if (myRout != null) {
                selectedUserRouts.add(myRout.getNameRout());
            }
            Intent mapIntent = new Intent(ActionActivity.this, MapsActivity_.class);
            mapIntent.putExtra(SELECTED_USER_PLACES, selectedUserPlacesList);
            mapIntent.putStringArrayListExtra(SELECTED_USER_ROUTS, selectedUserRouts);
            mapIntent.putExtra(PRODUCE_MODE, mTypeMode);
            startActivity(mapIntent);
        }else{
            if (myPlace != null) {
                selectedUserPlacesList.add(myPlace);
            }
            if (myRout != null) {
                selectedUserRouts.add(myRout.getNameRout());
            }
            Intent mapIntent = new Intent(ActionActivity.this, MapsActivity_.class);
            mapIntent.putExtra(SELECTED_USER_PLACES, selectedUserPlacesList);
            mapIntent.putStringArrayListExtra(SELECTED_USER_ROUTS, selectedUserRouts);
            startActivity(mapIntent);
        }
    }




    @Override
    protected void onStart() {
        super.onStart();
        selectedUserRouts.clear();
        selectedUserPlacesStringList.clear();
        selectedUserPlacesList.clear();
    }

    @Click(R.id.buttonAddToFavorites)
    void buttonAddToFavoritesWasClicked() {
        if (myPlace != null) {
            selectedUserPlacesStringList.add(myPlace.getNamePlace());
        }
        if (myRout != null) {
            selectedUserRouts.add(myRout.getNameRout());
        }
        Set<String> favoritesPlacesList = new HashSet<>(sharedPreferences.getStringSet(FAVORITES_PLACE_LIST, new HashSet<String>()));
        favoritesPlacesList.addAll(selectedUserPlacesStringList);
        Set<String> favoritesRoutsList = new HashSet<>(sharedPreferences.getStringSet(FAVORITES_ROUTS_LIST, new HashSet<String>()));
        favoritesRoutsList.addAll(selectedUserRouts);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(FAVORITES_PLACE_LIST, favoritesPlacesList);
        editor.putStringSet(FAVORITES_ROUTS_LIST, favoritesRoutsList);
        editor.apply();

        Toast.makeText(ActionActivity.this, " Add to favorites", LENGTH_LONG).show();

    }

    @Click(R.id.buttonEdit)
    void buttonEditWasClicked(){
        FragmentManager fm = getSupportFragmentManager();
        EditModeFragment editFragment = new EditModeFragment_();
        editFragment.show(fm, "fragment_edit");
        editFragment.setData(myRout, myPlace);

    }
    @Click(R.id.buttonShowPhoto)
    public void buttonShowPhotoWasClicked(){
        if (flagButtonShoePhoto) {
            graphView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            buttonShowPhoto.setImageResource(android.R.drawable.ic_menu_slideshow);
            flagButtonShoePhoto = false;
        }else{
            imageView.setVisibility(View.GONE);
            graphView.setVisibility(View.VISIBLE);
            buttonShowPhoto.setImageResource(android.R.drawable.ic_menu_report_image);
            flagButtonShoePhoto = true;
        }
    }

    @Override
    public void saveChanges(Rout rout, Place place) {
        setBaseInformation(place, rout);
    }

    @Override
    public void addToMap(Rout rout, Place place) {
        if (rout != null) {
            if (selectedUserRouts.contains(rout.getNameRout())) {
                selectedUserRouts.remove(rout.getNameRout());
            }else{
                selectedUserRouts.add(rout.getNameRout());
            }
        }
        if (place != null) {
            if (selectedUserPlacesList.contains(place)){
                selectedUserPlacesList.remove(place);
            }else {
                selectedUserPlacesList.add(place);
            }
            if (!selectedUserPlacesStringList.add(place.getNamePlace())){
                selectedUserPlacesStringList.remove(place.getNamePlace());
            }
        }
    }
}
