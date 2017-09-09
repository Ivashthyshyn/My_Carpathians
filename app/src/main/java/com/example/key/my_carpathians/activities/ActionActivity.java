package com.example.key.my_carpathians.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.ArraySet;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
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
	public static final String STORAGE_CONSTANT = "file:/storage/sdcard0/Android/data/com.example.key.my_carpathians/files/Download/Photos/";
	public List<Rout> routList;
    public List<Place> placeList;
    public List<Position> pointsRout;
    public Place myPlace;
    public Rout myRout;
	public  InfoFragment infoFragment;
	public  EditModeFragment editFragment;
    public com.example.key.my_carpathians.models.Position myPosition;
    public String myName;
    public ArrayList<String> selectedUserRouts = new ArrayList<>();
    public Set<String> selectedUserPlacesStringList = new ArraySet<>();
	public SharedPreferences sharedPreferences;
	public ArrayList<Place> selectedUserPlacesList = new ArrayList<>();
	private boolean mTypeMode = false;
	public List<String> photoUrlList = new ArrayList<>();
	private int mItemUrlList = 0;


   @ViewById(R.id.toolbar)
    Toolbar toolbar;

    @ViewById(R.id.imageView)
    ImageView imageView;
    @ViewById(R.id.textName)
    TextView textName;
	@ViewById(R.id.ratingBar)
	RatingBar ratingBar;
	@ViewById(R.id.buttonRatingBar)
	FloatingActionButton buttonRatingBar;
    @ViewById(graph)
    GraphView graphView;
	@ViewById(R.id.buttonShowOnMap)
    FloatingActionButton buttonShowOnMap;
	@ViewById(R.id.buttonAddToFavorites)
	FloatingActionButton buttonAddToFavorites;

	@ViewById(R.id.fabChangePhotoLeft)
	FloatingActionButton fabChangePhotoLeft;

	@ViewById(R.id.fabChangePhotoRight)
	FloatingActionButton fabChangePhotoRight;

	@ViewById (R.id.buttonEdit)
	FloatingActionButton buttonEdit;
	@ViewById(R.id.buttonPublish)
	FloatingActionButton buttonPublish;
	@ViewById(R.id.tabLayout)
	TabLayout tabLayout;
	@ViewById(R.id.viewpager)
	ViewPager viewPager;
    private ViewPagerAdapter adapter;
	private boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

            infoFragment = new InfoFragment_();
            adapter.addFragment(infoFragment, "INFO");
            infoFragment.setData(myPlace, myRout);
            viewPager.setAdapter(adapter);
		    buttonAddToFavorites.setVisibility(View.GONE);
            buttonPublish.setVisibility(View.VISIBLE);
            viewPager.setCurrentItem(0);
		    buttonEdit.setVisibility(View.VISIBLE);
		    ratingBar.setVisibility(View.GONE);
		    buttonRatingBar.setVisibility(View.GONE);
		    setBaseInformation(myPlace, myRout);

	    }else {

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
		    buttonEdit.setVisibility(View.GONE);
		    buttonPublish.setVisibility(View.GONE);
            viewPager.setCurrentItem(1);
            viewPager.setOffscreenPageLimit(2);
		    setBaseInformation(myPlace, myRout);
	    }
    }

	private void setBaseInformation(Place place, Rout rout) {
		if (place != null  ) {
			if (isOnline()){
				morePhotos(place.getNamePlace());
			}

			textName.setText(place.getNamePlace());
			if (infoFragment != null) {
				infoFragment.setData(place, rout);
				adapter.notifyDataSetChanged();
			}
			getRating(place.getNamePlace());
			graphView.setVisibility(View.GONE);
			fabChangePhotoLeft.setVisibility(View.GONE);
			fabChangePhotoRight.setVisibility(View.GONE);
			Glide
					.with(ActionActivity.this)
					.load(STORAGE_CONSTANT + place.getNamePlace())
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.skipMemoryCache(true)
					.into(imageView);
			myPosition = place.getPositionPlace();
			myName = place.getNamePlace();
		} else if (rout != null) {
			textName.setText(rout.getNameRout());
			photoUrlList.add("graph");

            if (rout.getUrlRout() != null && isOnline()){
	            photoUrlList.add(rout.getUrlRout());
		        morePhotos(rout.getNameRout());
	            imageView.setVisibility(View.GONE);
	            fabChangePhotoLeft.setVisibility(View.GONE);

            }else{
	            fabChangePhotoLeft.setVisibility(View.GONE);
	            fabChangePhotoRight.setVisibility(View.GONE);
	            imageView.setVisibility(View.GONE);
            }

            if (mTypeMode) {
                createDataPoint(URI.create(rout.getUrlRoutsTrack()));
            }else {
	            createDataPoint(URI.create(getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
			            .getString(rout.getNameRout(), null)));
            }
			myPosition = rout.getPositionRout();
			myName = rout.getNameRout();
		}
	}

	private void morePhotos(String name) {
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference myRef = database.getReference();
		Query myPlace = myRef.child("Photos").child(name);

		myPlace.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
					String value = postSnapshot.getValue(String.class);
					photoUrlList.add(value);
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
			}
		});
	}

	private void getRating(String namePlace) {
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference myRef = database.getReference();
		Query myPlace = myRef.child("Rating").child(namePlace);

		myPlace.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				int counter = 0;
				float sum = 0;
				for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
					counter++;
					float value = postSnapshot.getValue(float.class);
					sum = sum + value;

				}
				float averageValue = sum/counter;
				ratingBar.setRating(averageValue);
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				ratingBar.setRating(0);
			}
		});


	}
	public void setRating(float rating) {
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference myRef = database.getReference();
		FirebaseAuth mAuth = FirebaseAuth.getInstance();
		myRef.child("Rating").child(myName).child(mAuth.getCurrentUser().getUid()).setValue(rating);

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
	    android.support.v4.app.FragmentTransaction fragmentTransaction = fm
			    .beginTransaction();
        editFragment = new EditModeFragment_();
	    fragmentTransaction.add(R.id.actionActivityContainer, editFragment);
        editFragment.setData(myRout, myPlace);
	    fragmentTransaction.commit();

    }
    @Click(R.id.fabChangePhotoRight)
    public void fabChangePhotoRightWasClicked(){
	    mItemUrlList++;
	    if (mItemUrlList < photoUrlList.size()) {
		    fabChangePhotoRight.setVisibility(View.VISIBLE);
		    fabChangePhotoLeft.setVisibility(View.VISIBLE);
		    imageView.setVisibility(View.VISIBLE);
		    graphView.setVisibility(View.GONE);
		    Glide
				    .with(ActionActivity.this)
				    .load(photoUrlList.get(mItemUrlList))
				    .into(imageView);
		    if (mItemUrlList == photoUrlList.size() - 1) {
			    fabChangePhotoRight.setVisibility(View.GONE);
		    }
	    }
    }

	@Click(R.id.fabChangePhotoLeft)
	public void fabChangePhotoLeftWasClicked(){

		mItemUrlList--;
		if (mItemUrlList != 0){
			fabChangePhotoRight.setVisibility(View.VISIBLE);
			fabChangePhotoLeft.setVisibility(View.VISIBLE);
			imageView.setVisibility(View.VISIBLE);
			Glide
					.with(ActionActivity.this)
					.load(photoUrlList.get(mItemUrlList))
					.into(imageView);


		}else {
			fabChangePhotoRight.setVisibility(View.VISIBLE);
			fabChangePhotoLeft.setVisibility(View.GONE);
			imageView.setVisibility(View.GONE);
			graphView.setVisibility(View.VISIBLE);
		}
	}
    @Override
    public void saveChanges(Rout rout, Place place) {
        setBaseInformation(place, rout);
	    if(editFragment !=  null){
		    editFragment.dismiss();
	    }
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
	public boolean isOnline() {
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) ActionActivity.this
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
			connected = networkInfo != null && networkInfo.isAvailable() &&
					networkInfo.isConnected();
			return connected;


		} catch (Exception e) {
			System.out.println("CheckConnectivity Exception: " + e.getMessage());
			Log.v("connectivity", e.toString());
		}
		return connected;
	}
	@Click(R.id.buttonRatingBar)
	public void  ratingBarDialog(){
		final AlertDialog.Builder ratingDialog = new AlertDialog.Builder(this);

		ratingDialog.setIcon(android.R.drawable.btn_star_big_on);
		ratingDialog.setTitle("Проголосувати за місце");

		View linearLayout = getLayoutInflater().inflate(R.layout.ratingdialog, null);
		ratingDialog.setView(linearLayout);

		final RatingBar rating = (RatingBar)linearLayout.findViewById(R.id.ratingbar);

		ratingDialog.setPositiveButton("ОК",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						setRating(rating.getRating());
						dialog.dismiss();
					}
				})

				.setNegativeButton("Ні",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		ratingDialog.create();
		ratingDialog.show();
	}

	@Override
	public void onBackPressed() {

		if(editFragment != null){
			editFragment.dismiss();
			editFragment = null;
		}else {
			super.onBackPressed();
		}
	}
}
