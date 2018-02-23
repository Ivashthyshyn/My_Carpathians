package com.example.key.my_carpathians.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
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
import com.example.key.my_carpathians.utils.AltitudeFinder;
import com.example.key.my_carpathians.utils.EditObjectActionModeCallback;
import com.example.key.my_carpathians.utils.ObjectService;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import static com.example.key.my_carpathians.activities.StartActivity.ROOT_PATH;
import static com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter.ViewHolder.PUT_EXTRA_PLACE;
import static com.example.key.my_carpathians.adapters.RoutsRecyclerAdapter.PUT_EXTRA_POINTS;
import static com.example.key.my_carpathians.adapters.RoutsRecyclerAdapter.RoutsViewHolder.PUT_EXTRA_ROUT;

@EActivity
public class ActionActivity extends AppCompatActivity implements CommunicatorActionActivity {

    public static final String SELECTED_USER_ROUTS = "selected-user_routs";
    public static final String SELECTED_USER_PLACES = "selected_user_places";
	public static final String LOGIN = "login";
	public static final String PHOTO_STR = "Photos";
	public static final String ROUT_STR = "Rout";
	public static final String PLACE_IMAGE_STR = "placeImage";
	public static final String RATING_STR = "Rating";
	public static final String PLACE_STR = "Places";
	public List<Rout> routList;
    public List<Place> placeList;
    public List<Position> pointsRout;
    public Place myPlace;
    public Rout myRout;
	public InfoFragment infoFragment;
	public EditModeFragment editFragment;
    public com.example.key.my_carpathians.models.Position myPosition;
    public String myName;
    public ArrayList<String> selectedUserRouts = new ArrayList<>();
    public Set<String> selectedUserPlacesStringList = new ArraySet<>();
	public SharedPreferences sharedPreferences;
	public ArrayList<Place> selectedUserPlacesList = new ArrayList<>();
	private boolean mProduceMode = false;
	public List<String> photoUrlList = new ArrayList<>();
	private int mItemUrlList = 0;
	private ViewPagerAdapter viewPagerAdapter;
	private boolean connected = false;
	private FirebaseDatabase database;
	private DatabaseReference myRef;
	private List<Position> mPositionList;
	private String mRootPathString;
	private ActionMode mActionMode;
	@ViewById(R.id.uploadBar)
	ProgressBar uploadBar;
	@ViewById(R.id.toolBarActionActivity)
	Toolbar toolbar;
	@ViewById(R.id.appBarLayout)
	AppBarLayout appBarLayout;

    @ViewById(R.id.imageView)
    ImageView imageView;
	@ViewById(R.id.ratingBar)
	RatingBar ratingBar;

    @ViewById(graph)
    GraphView graphView;
	@ViewById(R.id.buttonShowOnMap)
    FloatingActionButton buttonShowOnMap;

	@ViewById(R.id.fabChangePhotoLeft)
	FloatingActionButton fabChangePhotoLeft;

	@ViewById(R.id.fabChangePhotoRight)
	FloatingActionButton fabChangePhotoRight;

	@ViewById(R.id.tabLayout)
	TabLayout tabLayout;
	@ViewById(R.id.viewpager)
	ViewPager viewPager;



	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);
        setSupportActionBar(toolbar);
	    toolbar.showOverflowMenu();
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		setupSizeViews();
        tabLayout.setupWithViewPager(viewPager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        sharedPreferences = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
	    mRootPathString = sharedPreferences.getString(ROOT_PATH, null);
	    routList = (List<Rout>) getIntent().getSerializableExtra(PUT_EXTRA_ROUTS_LIST);
        placeList = (List<Place>) getIntent().getSerializableExtra(PUT_EXTRA_PLACE_LIST);
        pointsRout = (List<Position>)getIntent().getSerializableExtra(PUT_EXTRA_POINTS);
        myPlace = (Place) getIntent().getSerializableExtra(PUT_EXTRA_PLACE);
        myRout = (Rout) getIntent().getSerializableExtra(PUT_EXTRA_ROUT);
	    mProduceMode = getIntent().getBooleanExtra(PRODUCE_MODE, false);
		produsedMode();
    }

	private void produsedMode() {
		infoFragment = new InfoFragment_().builder()
				.place(myPlace)
				.rout(myRout)
				.build();

		if (mProduceMode){
			viewPagerAdapter.addFragment(infoFragment, getResources().getString(R.string.title_info));
			viewPager.setAdapter(viewPagerAdapter);
			viewPager.setCurrentItem(0);
			ratingBar.setVisibility(View.GONE);
			setBaseInformation(myPlace, myRout);

		}else {
			PlaceAroundFragment placeAroundFragment = new PlaceAroundFragment_();
			viewPagerAdapter.addFragment(placeAroundFragment, getResources().getString(R.string.title_place_around));

			viewPagerAdapter.addFragment(infoFragment, getResources().getString(R.string.title_info));
			RoutsAroundFragment routsAroundFragment = new RoutsAroundFragment_();
			viewPagerAdapter.addFragment(routsAroundFragment, getResources().getString(R.string.title_rout_around));
			viewPager.setAdapter(viewPagerAdapter);
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
		CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, height);
		appBarLayout.setLayoutParams(params);

	}

	private void setBaseInformation(Place place, Rout rout) {
		if (place != null  ) {
			if (isOnline() | mProduceMode){
				morePhotos(place.getNamePlace());
				fabChangePhotoLeft.setVisibility(View.GONE);
			}else{
				fabChangePhotoLeft.setVisibility(View.GONE);
				fabChangePhotoRight.setVisibility(View.GONE);
			}

			if (infoFragment != null) {
				infoFragment.setPlace(place);
				infoFragment.setRout(rout);
				viewPagerAdapter.notifyDataSetChanged();
			}
			if (mRootPathString != null) {
				Uri photoUri = Uri.parse(mRootPathString);

			File titlePhotoFile = new File(photoUri.buildUpon().appendPath(PHOTO_STR).build().getPath(), myPlace.getNamePlace());
				Glide
						.with(ActionActivity.this)
						.load(titlePhotoFile)
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.skipMemoryCache(true)
						.into(imageView);
				photoUrlList.add(0, Uri.fromFile(titlePhotoFile).getPath());
				if(photoUrlList.size() == 1){
					fabChangePhotoRight.setVisibility(View.GONE);
				}
			}else{
				fabChangePhotoLeft.setVisibility(View.GONE);
				fabChangePhotoRight.setVisibility(View.GONE);
			}

				graphView.setVisibility(View.GONE);

			getRating(place.getNamePlace());
			myPosition = place.getPositionPlace();
			myName = place.getNamePlace();
		} else if (rout != null) {
			photoUrlList.add("graph");
			getRating(rout.getNameRout());
			if (infoFragment != null) {
				infoFragment.setPlace(null);
				infoFragment.setRout(rout);
				viewPagerAdapter.notifyDataSetChanged();
			}
            if (rout.getUrlRout() != null && isOnline() | mProduceMode) {
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
            if (mRootPathString != null){
	            Uri uriRootRout = Uri.parse(mRootPathString);
	            createDataPoint(uriRootRout.buildUpon().appendPath(ROUT_STR)
			            .appendPath(myRout.getNameRout()).build().getPath());
            }
			myPosition = rout.getPositionRout();
			myName = rout.getNameRout();
		}
	}

	private void morePhotos(String name) {
		if (mProduceMode){
				Uri rootPathForPhotosString =  Uri.parse(mRootPathString)
						.buildUpon().appendPath(PHOTO_STR).build();
			for (int i = 1; i <= 3; i++) {
				File photoFile = new File(rootPathForPhotosString.buildUpon()
						.appendPath(name + String.valueOf(i)).build().getPath());
				if (photoFile.exists()) {
					photoUrlList.add(Uri.fromFile(photoFile).getPath());
				}
			}

		}else {
			FirebaseDatabase database = FirebaseDatabase.getInstance();
			DatabaseReference myRef = database.getReference();
			Query myPlace = myRef.child(PHOTO_STR).child(name).child(PLACE_IMAGE_STR);
			myPlace.addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					if (dataSnapshot != null) {
						fabChangePhotoRight.setVisibility(View.VISIBLE);
						for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
							String value = postSnapshot.getValue(String.class);
							photoUrlList.add(value);
						}
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
		Query myPlace = myRef.child(RATING_STR).child(namePlace);

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
		myRef.child(RATING_STR).child(myName).child(mAuth.getCurrentUser().getUid()).setValue(rating);
	}

	public void showLoginDialog() {
		AlertDialog.Builder builder;
		if (isOnline()) {
			builder = new AlertDialog.Builder(this);
			builder.setTitle(getResources().getString(R.string.title_login_dialog));
			builder.setMessage(getResources().getString(R.string.message_login_dialog));

			builder.setPositiveButton(getResources().getString(R.string.registration), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int arg1) {
				Intent intent = new Intent(ActionActivity.this, StartActivity_.class);
					intent.putExtra(LOGIN, true );
					startActivity(intent);
					dialog.dismiss();

				}
			});
			builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int arg1) {
					dialog.dismiss();
				}
			});
			builder.setCancelable(true);
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
				}
			});

			AlertDialog alert = builder.create();
			alert.show();
		} else {
			builder = new AlertDialog.Builder(this);
			builder.setTitle(getResources().getString(R.string.internet));
			builder.setMessage(getResources().getString(R.string.no_internet) +" " +
					getResources().getString(R.string.no_internet2));

			builder.setPositiveButton(getResources().getString(R.string.ok),
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int arg1) {

				}
			});
			builder.setCancelable(true);
			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	@Background
    public void createDataPoint(String uriRoutTrack) {
       mPositionList = new ArrayList<>();
        try {
            // Load GeoJSON file
            File file = new File(uriRoutTrack);
	        if (file.exists()) {
		        InputStream fileInputStream = new FileInputStream(file);
		        BufferedReader rd = new BufferedReader(new InputStreamReader(fileInputStream,
				        Charset.forName("UTF-8")));
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
	    Integer     xi = 0;
	    for (int i = 1; i < size; i++) {
		    Integer yi = (int) mPositionList.get(i).getAltitude();
		    xi = xi + (int) TurfMeasurement.distance((mPositionList.get(i - 1)), mPositionList.get(i), TurfConstants.UNIT_METERS);
		    DataPoint v = new DataPoint(xi, yi);
		    values[i] = v;
	    }
	    values[0] = new DataPoint(0, (int) mPositionList.get(0).getAltitude());
	    LineGraphSeries series = new LineGraphSeries<DataPoint>(values);
	    series.setThickness(8);
	    graphView.addSeries(series);
	    if(mPositionList.get(0).getAltitude() == 0 & isOnline()){
		    downloadAltitude();
	    }
    }
    @Click(R.id.buttonShowOnMap)
    public void buttonShowOnMapWasClicked() {
        if(mProduceMode) {
            if (myPlace != null) {
                selectedUserPlacesList.add(myPlace);
            }
            if (myRout != null) {
                selectedUserRouts.add(myRout.getNameRout());
            }
            Intent mapIntent = new Intent(ActionActivity.this, MapsActivity_.class);
            mapIntent.putExtra(SELECTED_USER_PLACES, selectedUserPlacesList);
            mapIntent.putStringArrayListExtra(SELECTED_USER_ROUTS, selectedUserRouts);
            mapIntent.putExtra(PRODUCE_MODE, mProduceMode);
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

	private void showPublisherDialog(String problem) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.publish));
		builder.setMessage(problem);
		builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
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
				showPublisherDialog(getResources().getString(R.string.error_publisher)
						+ "\n" + getResources().getString(R.string.error_publisher_1p));
				problem++;
			} else if (place.getPositionPlace() == null) {
				showPublisherDialog(getResources().getString(R.string.error_publisher)
						+ "\n" + getResources().getString(R.string.error_publisher_2p));
				problem++;
			} else if (place.getTitlePlace().isEmpty()) {
				showPublisherDialog(getResources().getString(R.string.error_publisher)
						+ "\n" +getResources().getString(R.string.error_publisher_3p));
				problem++;
			} else if (place.getUrlPlace().isEmpty()) {
				showPublisherDialog(getResources().getString(R.string.error_publisher)
						+ "\n" + getResources().getString(R.string.error_publisher_4p));
				problem++;
			}

		}else if (rout != null){
			if (rout.getNameRout().isEmpty()) {
				showPublisherDialog(getResources().getString(R.string.error_publisher)
						+ "\n" + getResources().getString(R.string.error_publisher_1r));
				problem++;
			} else if (rout.getPositionRout() == null) {
				showPublisherDialog(getResources().getString(R.string.error_publisher)
						+ "\n" + getResources().getString(R.string.error_publisher_2r));
				problem++;
			} else if (rout.getUrlRout().isEmpty()) {
				showPublisherDialog(getResources().getString(R.string.error_publisher)
						+ "\n" + getResources().getString(R.string.error_publisher_3r));
				problem++;
			}else if (rout.getTitleRout().isEmpty()) {
				showPublisherDialog(getResources().getString(R.string.error_publisher)
						+ "\n" + getResources().getString(R.string.error_publisher_4r));
				problem++;
			}else if (rout.getRoutsLevel() == 0) {
				showPublisherDialog(getResources().getString(R.string.error_publisher)
						+ "\n" + getResources().getString(R.string.error_publisher_5r)	);
			problem++;
		}
		}
		return problem;
	}

	@Background
	public void saveToFireBase(final Place place, final Rout rout) {
		if (place != null){
			database = FirebaseDatabase.getInstance();
			myRef = database.getReference();
			FirebaseAuth mAuth = FirebaseAuth.getInstance();
			String email = mAuth.getCurrentUser().getEmail();
			place.setPublisher(email);
			myRef.child(PLACE_STR).child(place.getNamePlace())
					.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot snapshot) {
					if (snapshot.getValue() == null) {
						FirebaseStorage storage = FirebaseStorage.getInstance();
						StorageReference storageRef = storage.getReference();

						File file = new File(place.getUrlPlace());
						if (file.exists()) {
							StorageReference riversRef = storageRef.child(PLACE_IMAGE_STR +"/"
									+ (Uri.fromFile(file)).getLastPathSegment());
							UploadTask uploadTask = riversRef.putFile(Uri.fromFile(file));
							uploadBar.setVisibility(View.VISIBLE);
							uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
								@Override
								public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
									double progress = (100.0 * taskSnapshot.getBytesTransferred())
											/ taskSnapshot.getTotalByteCount();
									uploadBar.setProgress((int) progress);
									if (progress == 100.0) {
										uploadBar.setVisibility(View.GONE);
									}
								}
							});
							uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
								@Override
								public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
									// taskSnapshot.getMetadata() contains file metadata
									// such as size, content-type, and download URL.
									Uri downloadUrl = taskSnapshot.getDownloadUrl();
									Uri rootPathForPhotosString = Uri.fromFile(ActionActivity
											.this.getExternalFilesDir(
											Environment.DIRECTORY_DOWNLOADS)).buildUpon()
											.appendPath(PHOTO_STR).build();

									place.setUrlPlace(downloadUrl.toString());
									myRef.child(PHOTO_STR).child(place.getNamePlace()).setValue(place);
									for (int i = 1; i <= 3; i++) {
										File photoFile = new File(rootPathForPhotosString.buildUpon()
												.appendPath(place.getNamePlace()
														+ String.valueOf(i)).build().getPath());
										if (photoFile.exists()) {
											FirebaseStorage storage = FirebaseStorage.getInstance();
											StorageReference storageRef = storage.getReference();
											StorageReference riversRef = storageRef
													.child(PLACE_IMAGE_STR +"/" + place.getNamePlace()
															+ String.valueOf(i));
											UploadTask uploadTask = riversRef.putFile(Uri.fromFile(photoFile));
											uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
												@Override
												public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
													Toast.makeText(ActionActivity.this, getResources().getString(R.string.enother_photo_downlod), Toast.LENGTH_SHORT).show();
													myRef.child(PHOTO_STR).child(place.getNamePlace())
															.child(taskSnapshot.getDownloadUrl()
																	.getLastPathSegment())
															.setValue(taskSnapshot.getDownloadUrl()
																	.toString());
												}

											})
													.addOnFailureListener(new OnFailureListener() {
														@Override
														public void onFailure(@NonNull Exception e) {
															Toast.makeText(ActionActivity.this, getResources().getString(R.string.error_downloading_photo), Toast.LENGTH_SHORT).show();
														}
													});
										}
									}

								}

							}).addOnFailureListener(new OnFailureListener() {
								@Override
								public void onFailure(@NonNull Exception e) {
									Toast.makeText(ActionActivity.this, getResources().getString(R.string.error_uploading), Toast.LENGTH_SHORT).show();
								}
							});
						}
					} else {
						showAlreadyExistDialog(PLACE_STR);
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
			myRef.child(ROUT_STR).child(rout.getNameRout())
					.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot snapshot) {
					if (snapshot.getValue() == null) {
						FirebaseStorage storage = FirebaseStorage.getInstance();
						StorageReference storageRef = storage.getReference();
						File file = new File(rout.getUrlRoutsTrack());
						if (file.exists()) {
							StorageReference riversRef = storageRef.child("geojson/" + (Uri.fromFile(file)).getLastPathSegment());
							UploadTask uploadTask = riversRef.putFile(Uri.fromFile(file));
							uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
								@Override
								public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
									rout.setUrlRoutsTrack(taskSnapshot.getDownloadUrl().toString());
									Toast.makeText(ActionActivity.this, "Track saved", Toast.LENGTH_SHORT).show();
								}
							});
						}
						File file1 = new File(rout.getUrlRout());
						if(file1.exists()) {

							StorageReference riversRef1 = storageRef.child(PLACE_IMAGE_STR + "/" + (Uri.fromFile(file1)).getLastPathSegment());
							UploadTask uploadTask1 = riversRef1.putFile(Uri.fromFile(file1));
							uploadBar.setVisibility(View.VISIBLE);
							uploadTask1.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
								@Override
								public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
									double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
									uploadBar.setProgress((int) progress);
									if (progress == 100.0) {
										uploadBar.setVisibility(View.GONE);
									}
								}
							});

							uploadTask1.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
								@Override
								public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
									// taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
									Uri downloadUrl = taskSnapshot.getDownloadUrl();
									Uri rootPathForPhotosString = Uri.fromFile(ActionActivity.this.getExternalFilesDir(
											Environment.DIRECTORY_DOWNLOADS)).buildUpon().appendPath(PHOTO_STR).build();
									rout.setUrlRout(downloadUrl.toString());
									myRef.child(ROUT_STR).child(rout.getNameRout()).setValue(rout);
									for (int i = 1; i <= 3; i++) {
										File photoFile = new File(rootPathForPhotosString.buildUpon().appendPath(rout.getNameRout() + String.valueOf(i)).build().getPath());
										if (photoFile.exists()) {
											FirebaseStorage storage = FirebaseStorage.getInstance();
											StorageReference storageRef = storage.getReference();
											StorageReference riversRef = storageRef.child(PLACE_IMAGE_STR + "/" + rout.getNameRout() + String.valueOf(i));
											UploadTask uploadTask = riversRef.putFile(Uri.fromFile(photoFile));
											uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
												@Override
												public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
													Toast.makeText(ActionActivity.this, getResources().getString(R.string.enother_photo_downlod), Toast.LENGTH_SHORT).show();
													myRef.child(PHOTO_STR).child(rout.getNameRout()).child(taskSnapshot.getDownloadUrl().getLastPathSegment()).setValue(taskSnapshot.getDownloadUrl().toString());
												}

											})
													.addOnFailureListener(new OnFailureListener() {
														@Override
														public void onFailure(@NonNull Exception e) {
															Toast.makeText(ActionActivity.this, getResources().getString(R.string.error_downloading_photo), Toast.LENGTH_SHORT).show();
														}
													});
										}
									}

								}
							}).addOnFailureListener(new OnFailureListener() {
								@Override
								public void onFailure(@NonNull Exception e) {
									Log.e("Error upload", e.toString());
									Toast.makeText(ActionActivity.this, getResources()
											.getString(R.string.error_uploading), Toast.LENGTH_SHORT).show();
								}
							});
						}
					} else {
						showAlreadyExistDialog(ROUT_STR);

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
		builder.setTitle(getResources().getString(R.string.publish))
				.setMessage( name +" " + getResources().getString(R.string.already_exist))
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						editCreatedObject();
						dialogInterface.dismiss();
					}
				})
				.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
					}
				});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();

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
	   mActionMode.finish();
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

public void ratingBarDialog(){
		final AlertDialog.Builder ratingDialog = new AlertDialog.Builder(this);

		ratingDialog.setIcon(android.R.drawable.btn_star_big_on);
		ratingDialog.setTitle(getResources().getString(R.string.set_rating));

		View linearLayout = getLayoutInflater().inflate(R.layout.ratingdialog, null);
		ratingDialog.setView(linearLayout);

		final RatingBar rating = (RatingBar)linearLayout.findViewById(R.id.ratingbar2);

		ratingDialog.setPositiveButton(getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						setRating(rating.getRating());
						dialog.dismiss();
					}
				})

				.setNegativeButton(getResources().getString(R.string.no),
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
			ObjectService objectService = new ObjectService(ActionActivity.this, mRootPathString);
			objectService.saveRout(myName, null, myRout, true);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_action_activity, menu);
		if (mProduceMode){
			MenuItem actionEdit = menu.findItem(R.id.action_edit);
			actionEdit.setVisible(true);
			MenuItem actionPublisher = menu.findItem(R.id.action_publish);
			actionPublisher.setVisible(true);
			MenuItem actionAddToFavorite = menu.findItem(R.id.action_add_to_favorites);
			actionAddToFavorite.setVisible(false);
		}else{
			MenuItem actionEdit = menu.findItem(R.id.action_edit);
			actionEdit.setVisible(false);
			MenuItem actionPublisher = menu.findItem(R.id.action_publish);
			actionPublisher.setVisible(false);
			MenuItem actionAddToFavorite = menu.findItem(R.id.action_add_to_favorites);
			actionAddToFavorite.setVisible(true);
		}

		return true;
	}


	@Click(R.id.ratingBarrContainer)
	public void ratingBarWasClicked(){
		if (FirebaseAuth.getInstance().getCurrentUser() != null && !FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {
			ratingBarDialog();
		}else{
			showLoginDialog();
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_add_to_favorites:
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

				Toast.makeText(ActionActivity.this, getResources().getString(R.string.add_to_fevorite), LENGTH_LONG).show();
				return true;
			case R.id.action_edit:
				editCreatedObject();
				return true;
			case R.id.action_publish:
				if (isOnline()) {
					if (dataIntegrityCheck(myPlace, myRout) == 0) {
						saveToFireBase(myPlace, myRout);
					}
				} else {
					showPublisherDialog(getResources().getString(R.string.offline_message));

				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void editCreatedObject() {
		FragmentManager fm = getSupportFragmentManager();
		editFragment = new EditModeFragment_();
		editFragment.setData(myRout, myPlace, mRootPathString);
		mActionMode = this.startSupportActionMode(new EditObjectActionModeCallback(this, editFragment, fm));

	}
	@Override
	public void autoOrientationOff(boolean yes) {
		if (yes) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		}else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
	}
}
