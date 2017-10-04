package com.example.key.my_carpathians.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.ArraySet;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.LineString;
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
import com.example.key.my_carpathians.utils.AltitudeFinder;
import com.example.key.my_carpathians.utils.ObjectSaver;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.mapbox.services.api.utils.turf.TurfConstants;
import com.mapbox.services.api.utils.turf.TurfMeasurement;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.commons.utils.TextUtils;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
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
	public  InfoFragment infoFragment;
	public  EditModeFragment editFragment;
    public com.example.key.my_carpathians.models.Position myPosition;
    public String myName;
    public ArrayList<String> selectedUserRouts = new ArrayList<>();
    public Set<String> selectedUserPlacesStringList = new ArraySet<>();
	public SharedPreferences sharedPreferences;
	public ArrayList<Place> selectedUserPlacesList = new ArrayList<>();
	private boolean mProdusedMode = false;
	public List<String> photoUrlList = new ArrayList<>();
	private int mItemUrlList = 0;
	private ViewPagerAdapter adapter;
	private boolean connected = false;
	private FirebaseDatabase database;
	private DatabaseReference myRef;
	private List<Position> mPositionList;
	@ViewById(R.id.uploadBar)
	ProgressBar uploadBar;

   @ViewById(R.id.toolbar)
    Toolbar toolbar;

	@ViewById(R.id.appBarLayout)
	AppBarLayout appBarLayout;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setupSizeViews();
        tabLayout.setupWithViewPager(viewPager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        sharedPreferences = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        routList = (List<Rout>) getIntent().getSerializableExtra(PUT_EXTRA_ROUTS_LIST);
        placeList = (List<Place>) getIntent().getSerializableExtra(PUT_EXTRA_PLACE_LIST);
        pointsRout = (List<Position>)getIntent().getSerializableExtra(PUT_EXTRA_POINTS);
        myPlace = (Place) getIntent().getSerializableExtra(PUT_EXTRA_PLACE);
        myRout = (Rout) getIntent().getSerializableExtra(PUT_EXTRA_ROUT);
	    mProdusedMode = getIntent().getBooleanExtra(PRODUCE_MODE, false);
	    if (mProdusedMode){

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

            InfoFragment infoFragment = new InfoFragment_();
            adapter.addFragment(infoFragment, "INFO");
            infoFragment.setData(myPlace, myRout);
            RoutsAroundFragment routsAroundFragment = new RoutsAroundFragment_();
            adapter.addFragment(routsAroundFragment, "ROUT AROUND");

            viewPager.setAdapter(adapter);
		    buttonEdit.setVisibility(View.GONE);
		    buttonPublish.setVisibility(View.GONE);
            viewPager.setCurrentItem(1);
            viewPager.setOffscreenPageLimit(2);
		    setBaseInformation(myPlace, myRout);
		    routsAroundFragment.setData(myRout, routList, myPosition);
		    placeAroundFragment.setData(myPlace, placeList, myPosition);
	    }
    }

	private void setupSizeViews() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int height = metrics.heightPixels / 3;
CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.WRAP_CONTENT, height);
		appBarLayout.setLayoutParams(params);

	}

	private void setBaseInformation(Place place, Rout rout) {
		if (place != null  ) {
			if (isOnline() | mProdusedMode){
				morePhotos(place.getNamePlace());
				fabChangePhotoLeft.setVisibility(View.GONE);
			}else{
				fabChangePhotoLeft.setVisibility(View.GONE);
				fabChangePhotoRight.setVisibility(View.GONE);
			}

			textName.setText(place.getNamePlace());
			if (infoFragment != null) {
				infoFragment.setData(place, rout);
				adapter.notifyDataSetChanged();
			}

			Uri rootPathForTitlePhotoString;
			if (isExternalStorageReadable()) {
				rootPathForTitlePhotoString = Uri.fromFile(ActionActivity.this.getExternalFilesDir(
						Environment.DIRECTORY_DOWNLOADS)).buildUpon().appendPath("Photos").build();
			}else{
				rootPathForTitlePhotoString = Uri.parse(place.getUrlPlace());
			}
				Glide
						.with(ActionActivity.this)
						.load(rootPathForTitlePhotoString.buildUpon().appendPath(place.getNamePlace()).build())
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.skipMemoryCache(true)
						.into(imageView);
				photoUrlList.add(0, rootPathForTitlePhotoString.toString());
				graphView.setVisibility(View.GONE);

			getRating(place.getNamePlace());
			myPosition = place.getPositionPlace();
			myName = place.getNamePlace();
		} else if (rout != null) {
			textName.setText(rout.getNameRout());
			photoUrlList.add("graph");
			getRating(rout.getNameRout());
			if (infoFragment != null) {
				infoFragment.setData(place, rout);
				adapter.notifyDataSetChanged();
			}
            if (rout.getUrlRout() != null && isOnline() | mProdusedMode) {
	            photoUrlList.add(rout.getUrlRout());
	            morePhotos(rout.getNameRout());
	            imageView.setVisibility(View.GONE);
	            fabChangePhotoLeft.setVisibility(View.GONE);
	            fabChangePhotoRight.setVisibility(View.VISIBLE);
            }else{
	            fabChangePhotoLeft.setVisibility(View.GONE);
	            fabChangePhotoRight.setVisibility(View.GONE);
	            imageView.setVisibility(View.GONE);
            }
			Uri rootPathForRoutsString;
			if (isExternalStorageReadable()) {
				rootPathForRoutsString = Uri.fromFile(ActionActivity.this.getExternalFilesDir(
						Environment.DIRECTORY_DOWNLOADS)).buildUpon().appendPath("Routs").build();
			}else{
				rootPathForRoutsString = Uri.fromFile(ActionActivity.this.getFilesDir()).buildUpon().appendPath("Routs").build();
			}

            createDataPoint(rootPathForRoutsString.buildUpon().appendPath(rout.getNameRout()).build());

			myPosition = rout.getPositionRout();
			myName = rout.getNameRout();
		}
	}
	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	private void morePhotos(String name) {
		if (mProdusedMode && isExternalStorageReadable() ){
				Uri rootPathForPhotosString = Uri.fromFile(ActionActivity.this.getExternalFilesDir(
						Environment.DIRECTORY_DOWNLOADS)).buildUpon().appendPath("Photos").build();
			for (int i = 1; i <= 3; i++) {
				File photoFile = new File(rootPathForPhotosString.buildUpon().appendPath(name + String.valueOf(i)).build().getPath());
				if (photoFile.exists()) {
					photoUrlList.add(Uri.fromFile(photoFile).getPath());
				}
			}

		}else {
			FirebaseDatabase database = FirebaseDatabase.getInstance();
			DatabaseReference myRef = database.getReference();
			Query myPlace = myRef.child("Photos").child(name).child("placeImage");

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
	}



	private void getRating(String namePlace) {
		database = FirebaseDatabase.getInstance();
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
		database = FirebaseDatabase.getInstance();
		DatabaseReference myRef = database.getReference();
		FirebaseAuth mAuth = FirebaseAuth.getInstance();
		myRef.child("Rating").child(myName).child(mAuth.getCurrentUser().getUid()).setValue(rating);

	}

	@Background
    public void createDataPoint(Uri uriRoutTrack) {
       mPositionList = new ArrayList<>();
        try {
            // Load GeoJSON file

            File file = new File(uriRoutTrack.getPath());
	        if (file.exists()) {
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
					        mPositionList.add(position);
				        }
						if (mPositionList.size() > 0){
							buildGraph(mPositionList);
						}
			        }
		        }

	        }
        } catch (Exception exception) {
            Log.e(TAG, "Exception Loading GeoJSON: " + exception.toString());
        }



    }

    @UiThread
   public void buildGraph(List<Position> positions){
	    int size = positions.size();
	    DataPoint[] values = new DataPoint[size];
	    Integer xi = 0;
	    for (int i = 1; i < size; i++) {
		    Integer yi = (int) mPositionList.get(i).getAltitude();
		    xi = xi + (int) TurfMeasurement.distance(mPositionList.get(i - 1), mPositionList.get(i), TurfConstants.UNIT_METERS);
		    DataPoint v = new DataPoint(xi, yi);
		    values[i] = v;
	    }
	    values[0] = new DataPoint(0, (int) mPositionList.get(0).getAltitude());
	    LineGraphSeries series = new LineGraphSeries<DataPoint>(values);
	    series.setThickness(8);
	    graphView.addSeries(series);
	    GridLabelRenderer gridLabel = graphView.getGridLabelRenderer();
	    gridLabel.setHorizontalAxisTitle("meters");
	    gridLabel.setVerticalAxisTitle("meters");
	    if(mPositionList.get(0).getAltitude() == 0 & isOnline()){
		    downloadAltitude();
	    }
    }
    @Click(R.id.buttonShowOnMap)
    public void buttonShowOnMapWasClicked() {
        if(mProdusedMode) {
            if (myPlace != null) {
                selectedUserPlacesList.add(myPlace);
            }
            if (myRout != null) {
                selectedUserRouts.add(myRout.getNameRout());
            }
            Intent mapIntent = new Intent(ActionActivity.this, MapsActivity_.class);
            mapIntent.putExtra(SELECTED_USER_PLACES, selectedUserPlacesList);
            mapIntent.putStringArrayListExtra(SELECTED_USER_ROUTS, selectedUserRouts);
            mapIntent.putExtra(PRODUCE_MODE, mProdusedMode);
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
    @Click(R.id.buttonPublish)
    void buttonPublishWasClicked(){
	    if (isOnline()) {
		    if (dataIntegrityCheck(myPlace, myRout) == 0) {
			    saveToFirebase(myPlace, myRout);
		    }
	    } else {
		    showPublisherDialog("Sorry, no internet access, you will be able to post data later when " +
				    " there is a good connection to the network ");

	    }

    }

	private void showPublisherDialog(String problem) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Publish");
		builder.setMessage(problem);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}

	private int dataIntegrityCheck(Place place, Rout rout) {

		int problem = 0;
		if (place != null){
			if (place.getNamePlace().isEmpty()) {
				showPublisherDialog("Для поблікації потрібно заповнити всі дані" + "\n" +
						"Ви не вказали імя місця");
				problem++;
			} else if (place.getPositionPlace() == null) {
				showPublisherDialog("Для поблікації потрібно заповнити всі дані" + "\n" +
						"Не вказана позиція");
				problem++;
			} else if (place.getTitlePlace().isEmpty()) {
				showPublisherDialog("Для поблікації потрібно заповнити всі дані" + "\n" +
						"Не вказано титульної інформації про місце");
				problem++;
			} else if (place.getUrlPlace().isEmpty()) {
				showPublisherDialog("Для поблікації потрібно заповнити всі дані" + "\n" +
						"Ви не встановили титульну фотографію");
				problem++;
			}

		}else if (rout != null){
			if (rout.getNameRout().isEmpty()) {
				showPublisherDialog("Для поблікації потрібно заповнити всі дані" + "\n" +
						"Ви не вказали імя маршруту");
				problem++;
			} else if (rout.getPositionRout() == null) {
				showPublisherDialog("Для поблікації потрібно заповнити всі дані" + "\n" +
						"Не вказана позиція");
				problem++;
			} else if (rout.getUrlRout().isEmpty()) {
				showPublisherDialog("Для поблікації потрібно заповнити всі дані" + "\n" +
						"Не вказана титульна фотографія");
				problem++;
			}else if (rout.getTitleRout().isEmpty()) {
				showPublisherDialog("Для поблікації потрібно заповнити всі дані" + "\n" +
						"Немає титульної інформації");
				problem++;
			}else if (rout.getRoutsLevel() == 0) {
				showPublisherDialog("Для поблікації потрібно заповнити всі дані" + "\n" +
						"Не вказана складність маршруту");
			problem++;
		}
		}
		return problem;
	}

	@Background
	public void saveToFirebase(final Place place, final Rout rout) {
		if (place != null){
			database = FirebaseDatabase.getInstance();
			myRef = database.getReference();
			FirebaseAuth mAuth = FirebaseAuth.getInstance();
			String email = mAuth.getCurrentUser().getEmail();
			place.setPublisher(email);
			myRef.child("Places").child(place.getNamePlace()).addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot snapshot) {
					if (snapshot.getValue() == null) {
						FirebaseStorage storage = FirebaseStorage.getInstance();
						StorageReference storageRef = storage.getReference();
						Uri uri = Uri.parse(place.getUrlPlace());
						StorageReference riversRef = storageRef.child("placeImage/" + uri.getLastPathSegment());
						UploadTask uploadTask = riversRef.putFile(uri);
						uploadBar.setVisibility(View.VISIBLE);
						uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
							@Override
							public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
								double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
								uploadBar.setProgress((int) progress);
								if (progress == 100.0){
									uploadBar.setVisibility(View.GONE);
								}
							}
						});
						uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
								@Override
								public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
									// taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
									Uri downloadUrl = taskSnapshot.getDownloadUrl();
									Uri rootPathForPhotosString = Uri.fromFile(ActionActivity.this.getExternalFilesDir(
												Environment.DIRECTORY_DOWNLOADS)).buildUpon().appendPath("Photos").build();

									place.setUrlPlace(downloadUrl.toString());
									myRef.child("Places").child(place.getNamePlace()).setValue(place);
									for (int i = 1; i <= 3; i++){
										File photoFile = new File(rootPathForPhotosString.buildUpon().appendPath(place.getNamePlace() + String.valueOf(i)).build().getPath());
										if (photoFile.exists()){
											FirebaseStorage storage = FirebaseStorage.getInstance();
											StorageReference storageRef = storage.getReference();
											StorageReference riversRef = storageRef.child("placeImage/" + place.getNamePlace() + String.valueOf(i));
											UploadTask uploadTask = riversRef.putFile(Uri.fromFile(photoFile));
											uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
												@Override
												public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
													Toast.makeText(ActionActivity.this, "  Another photo download", Toast.LENGTH_SHORT).show();
													myRef.child("Photos").child(place.getNamePlace()).child(taskSnapshot.getDownloadUrl().getLastPathSegment()).setValue(taskSnapshot.getDownloadUrl().toString());
												}

											})
											.addOnFailureListener(new OnFailureListener() {
														@Override
														public void onFailure(@NonNull Exception e) {
															Toast.makeText(ActionActivity.this, "Сталася помилка одна з додаткових фотографій не завантажилась", Toast.LENGTH_SHORT).show();
														}
													});
										}
									}

								}
						}).addOnFailureListener(new OnFailureListener() {
							@Override
							public void onFailure(@NonNull Exception e) {
								Toast.makeText(ActionActivity.this, "Сталася помила завантаження не відбулося", Toast.LENGTH_SHORT).show();
							}
						});
					} else {
						showAlreadyExistDialog("Place");
					}
				}

				@Override
				public void onCancelled(DatabaseError databaseError) {

				}
			});


		}else if (rout != null){
			database = FirebaseDatabase.getInstance();
			myRef = database.getReference();
			FirebaseAuth mAuth = FirebaseAuth.getInstance();
			rout.setPublisher(mAuth.getCurrentUser().getEmail());
			myRef.child("Rout").child(rout.getNameRout()).addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot snapshot) {
					if (snapshot.getValue() == null) {
						FirebaseStorage storage = FirebaseStorage.getInstance();
						StorageReference storageRef = storage.getReference();
						Uri uri = Uri.parse(rout.getUrlRoutsTrack());
						StorageReference riversRef = storageRef.child("geojson/" + uri.getLastPathSegment());
						UploadTask uploadTask = riversRef.putFile(uri);
						uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
							@Override
							public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
								rout.setUrlRoutsTrack(taskSnapshot.getDownloadUrl().toString());
								Toast.makeText(ActionActivity.this, "Track saved", Toast.LENGTH_SHORT).show();
							}
						});
						Uri uri1 = Uri.parse(rout.getUrlRout());
						StorageReference riversRef1 = storageRef.child("placeImage/" + uri1.getLastPathSegment());
						UploadTask uploadTask1 = riversRef1.putFile(uri1);
						uploadBar.setVisibility(View.VISIBLE);
						uploadTask1.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
							@Override
							public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
								double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
								uploadBar.setProgress((int) progress);
								if (progress == 100.0){
									uploadBar.setVisibility(View.GONE);
								}
							}
						});
						uploadTask1.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
							@Override
							public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
								// taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
								Uri downloadUrl = taskSnapshot.getDownloadUrl();
								Uri	rootPathForPhotosString = Uri.fromFile(ActionActivity.this.getExternalFilesDir(
											Environment.DIRECTORY_DOWNLOADS)).buildUpon().appendPath("Photos").build();
								rout.setUrlRout(downloadUrl.toString());
								myRef.child("Rout").child(rout.getNameRout()).setValue(rout);
								for (int i = 1; i <= 3; i++){
									File photoFile = new File(rootPathForPhotosString.buildUpon().appendPath(rout.getNameRout() + String.valueOf(i)).build().getPath());
									if (photoFile.exists()){
										FirebaseStorage storage = FirebaseStorage.getInstance();
										StorageReference storageRef = storage.getReference();
										StorageReference riversRef = storageRef.child("placeImage/" + rout.getNameRout() + String.valueOf(i));
										UploadTask uploadTask = riversRef.putFile(Uri.fromFile(photoFile));
										uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
											@Override
											public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
												Toast.makeText(ActionActivity.this, "  Another photo download", Toast.LENGTH_SHORT).show();
												myRef.child("Photos").child(rout.getNameRout()).child(taskSnapshot.getDownloadUrl().getLastPathSegment()).setValue(taskSnapshot.getDownloadUrl().toString());
											}

										})
												.addOnFailureListener(new OnFailureListener() {
													@Override
													public void onFailure(@NonNull Exception e) {
														Toast.makeText(ActionActivity.this, "Сталася помилка одна з додаткових фотографій не завантажилась", Toast.LENGTH_SHORT).show();
													}
												});
									}
								}

							}
						}).addOnFailureListener(new OnFailureListener() {
							@Override
							public void onFailure(@NonNull Exception e) {
								Toast.makeText(ActionActivity.this, "Сталася помила завантаження не відбулося", Toast.LENGTH_SHORT).show();
							}
						});
					} else {
						showAlreadyExistDialog("Rout");

					}
				}

				@Override
				public void onCancelled(DatabaseError databaseError) {

				}
			});
		}
	}

	private void showAlreadyExistDialog(String name) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ActionActivity.this);
		builder.setTitle("Публікація даних")
				.setMessage( name +"з таким іменем уже існує! Змінити назву?")
				.setPositiveButton("Так", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						buttonEditWasClicked();
						dialogInterface.dismiss();
					}
				})
				.setNegativeButton("Ні", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
					}
				});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();

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
				    .diskCacheStrategy(DiskCacheStrategy.NONE)
				    .skipMemoryCache(true)
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


		}else if (mItemUrlList == 0 && photoUrlList.get(0).equals("graph")) {
			fabChangePhotoRight.setVisibility(View.VISIBLE);
			fabChangePhotoLeft.setVisibility(View.GONE);
			imageView.setVisibility(View.GONE);
			graphView.setVisibility(View.VISIBLE);
		}else{
			fabChangePhotoRight.setVisibility(View.VISIBLE);
			fabChangePhotoLeft.setVisibility(View.GONE);
			imageView.setVisibility(View.VISIBLE);
			Glide
					.with(ActionActivity.this)
					.load(photoUrlList.get(mItemUrlList))
					.into(imageView);
		}
	}
    @Override
    public void saveChanges(Rout rout, Place place) {
	    myRout = rout;
	    myPlace = place;
	    photoUrlList.clear();
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

	@Background
	public void  downloadAltitude(){
		AltitudeFinder altitudeFinder = new AltitudeFinder();
		List<com.cocoahero.android.geojson.Position> hadAltitudePosition = altitudeFinder.extractAltitude(mPositionList);
		List<Position> positions = new ArrayList<>();
		for (int i = 0; i < hadAltitudePosition.size(); i++){
			positions.add(Position.fromCoordinates(hadAltitudePosition.get(i).getLatitude(),
					hadAltitudePosition.get(i).getLongitude(),
					hadAltitudePosition.get(i).getAltitude()));
		}



		if (hadAltitudePosition .size() > 0){
			buildGraph(positions);
			ObjectSaver objectSaver = new ObjectSaver();
			String outcome = objectSaver.saveRout(myName, null, myRout, true);
		}

		LineString lineString = new LineString();
		lineString.setPositions(hadAltitudePosition);
		Feature feature = new Feature();
		try {
			JSONObject geoJSON = new JSONObject();
			feature.setProperties(new JSONObject());
			feature.setGeometry(lineString);
			feature.setIdentifier("key.my_carpathians");
			geoJSON.put("features", new JSONArray().put(feature.toJSON()));
			geoJSON.put("type", "FeatureCollection");

			File rootPath = new File(getApplicationContext().getExternalFilesDir(
					Environment.DIRECTORY_DOWNLOADS), "Routs");
			if (!rootPath.exists()) {
				rootPath.mkdirs();
			}
			File localFile = new File(rootPath, myName);
			if (localFile.exists()) {
				localFile.delete();
			}
			String fileUri = String.valueOf(Uri.fromFile(localFile));
			Writer output = new BufferedWriter(new FileWriter(localFile));
			output.write(geoJSON.toString());
			output.close();
			Rout mRout = new Rout();
			mRout.setNameRout(myName);
			mRout.setUrlRoutsTrack(fileUri);
			createDataPoint(Uri.fromFile(localFile));
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
}
