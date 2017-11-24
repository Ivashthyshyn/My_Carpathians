package com.example.key.my_carpathians.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.adapters.ViewPagerAdapter;
import com.example.key.my_carpathians.fragments.PlacesListFragment;
import com.example.key.my_carpathians.fragments.PlacesListFragment_;
import com.example.key.my_carpathians.fragments.RoutsListFragment;
import com.example.key.my_carpathians.fragments.RoutsListFragment_;
import com.example.key.my_carpathians.interfaces.CommunicatorStartActivity;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;
import com.example.key.my_carpathians.utils.ObjectService;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.key.my_carpathians.activities.ActionActivity.LOGIN;
import static com.example.key.my_carpathians.activities.MapsActivity.JSON_CHARSET;
import static com.example.key.my_carpathians.activities.MapsActivity.JSON_FIELD_REGION_NAME;
import static com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter.ViewHolder.PUT_EXTRA_PLACE;
import static com.example.key.my_carpathians.adapters.RoutsRecyclerAdapter.RoutsViewHolder.PUT_EXTRA_ROUT;
import static com.example.key.my_carpathians.utils.LocationService.CREATED_BY_USER_PLACE_LIST;
import static com.example.key.my_carpathians.utils.LocationService.CREATED_BY_USER_ROUT_LIST;
import static com.example.key.my_carpathians.utils.ObjectService.ERROR;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with mFirebaseUser interaction.
 */
@EActivity
public class StartActivity extends AppCompatActivity implements
		GoogleApiClient.OnConnectionFailedListener, CommunicatorStartActivity {

	public static final String PUT_EXTRA_PLACE_LIST = "place_list";
	public static final String PUT_EXTRA_ROUTS_LIST = "routs_list";
	public static final String FAVORITES_ROUTS_LIST = "favorites_user_routs";
	public static final String FAVORITES_PLACE_LIST = "favorites_user_places";
	public static final String PREFS_NAME = "MyPrefsFile";
	public static final String PRODUCE_MODE = "manufacturer_mode";
	public static final String ROOT_PATH = "root_path";
	public static final String OFFLINE_MAP = "offline_map";
	public static final String LOGIN_NAME = "login_name";
	public static final int ROUT = 1;
	public static final int PLACE = 0;
	public static final int MY_ROUT = 5;
	public static final int MY_PLACE = 4;
	public static final int FA_PLACE = 2;
	public static final int FA_ROUT = 3;
	public static final int RC_SIGN_IN = 9001;
	public static final String TAG = "StartActivity";
	public Context context = StartActivity.this;
	public ArrayList<Place> places = new ArrayList<>();
	public ArrayList<Rout> routs = new ArrayList<>();
	public AlertDialog.Builder builder;
	public ActionBarDrawerToggle actionBarDrawerToggle;
	public ViewPagerAdapter adapter;
	private boolean mConnected = false;
	private String[] mPermissionList = new String[]{
			ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE
			, READ_EXTERNAL_STORAGE};
	private FirebaseAuth.AuthStateListener mAuthListener;
	private SharedPreferences mSharedPreferences;
	private CallbackManager mCallbackManager;
	private FirebaseUser mUser;
	private FirebaseAuth mAuth;
	private GoogleApiClient mGoogleApiClient;
	private String mUserUID = null;
	private boolean mTypeMode = false;
	private Uri mRootPath;
	private int mRegionSelected;
	private LoginManager mFacebookLoginManager;
	private String mQuery;
	@ViewById(R.id.fabRecEditor)
	FloatingActionButton fabRecEditor;
	@ViewById(R.id.userAcountImage)
	CircleImageView userAccountImage;
	@ViewById(R.id.facebokLoginButton)
	Button facebookLoginButton;
	@ViewById(R.id.googleLoginButton)
	Button googleLoginButton;
	@ViewById(R.id.textViewEmail)
	TextView textViewEmail;
	@ViewById(R.id.email)
	EditText inputEmail;
	@ViewById(R.id.password)
	EditText inputPassword;
	@ViewById(R.id.progressBar)
	ProgressBar progressBar;
	@ViewById(R.id.emailLoginButton)
	Button emailLoginButton;
	@ViewById(R.id.inputEmailLayout)
	TextInputLayout inputEmailLayout;
	@ViewById(R.id.inputPasswordLayout)
	TextInputLayout inputPasswordLayout;
	@ViewById(R.id.buttonResetPassword)
	Button buttonResetPassword;
	@ViewById(R.id.buttonFavorites)
	LinearLayout buttonFavorites;
	@ViewById(R.id.buttonCreated)
	LinearLayout buttonCreated;
	@ViewById(R.id.toolbar)
	Toolbar toolbar;
	@ViewById(R.id.viewpagerActionActivity)
	ViewPager viewPager;
	@ViewById(R.id.tabLayout)
	TabLayout tabLayout;
	@ViewById(R.id.drawer_layout)
	DrawerLayout mDrawerLayout;
	@ViewById(R.id.settingsGroup)
	LinearLayout settingsGroup;
	@ViewById(R.id.buttonLogout)
	ImageButton buttonLogout;
	@ViewById(R.id.buttonAuthorization)
	Button buttonAuthorization;




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		setSupportActionBar(toolbar);
		toolbar.showOverflowMenu();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		tabLayout.setupWithViewPager(viewPager);
		adapter = new ViewPagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(adapter);
		mAuth = FirebaseAuth.getInstance();
		viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
			@Override
			public void onPageScrollStateChanged(int state) {

				if (state == PLACE) {
					RoutsListFragment routsListFragment = (RoutsListFragment) adapter.getItem(ROUT);
					routsListFragment.dismissActionMode();
				} else if (state == ROUT) {
					PlacesListFragment placesListFragment = (PlacesListFragment) adapter.getItem(PLACE);
					placesListFragment.dismissActionMode();
				}
				if (mQuery != null) {
					if (tabLayout.getTabCount() != 0 && state == PLACE) {
						PlacesListFragment placesListFragment = (PlacesListFragment) adapter.getItem(PLACE);
						placesListFragment.filter(mQuery);
					} else if (tabLayout.getTabCount() != 0 && state == ROUT) {
						RoutsListFragment routsListFragment = (RoutsListFragment) adapter.getItem(ROUT);
						routsListFragment.filter(mQuery);
					}
				}
			}
		});

		actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);
		mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
		actionBarDrawerToggle.syncState();

		mSharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		checkCurrentUser();
		mAuthListener = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
				mUser = firebaseAuth.getCurrentUser();
				if (mUser != null && !mUser.isAnonymous()) {
					mTypeMode = true;
					buttonLogout.setVisibility(View.VISIBLE);
				} else if (mUser != null && mUser.isAnonymous()) {
					showInterfaceForAnonymous();
					mTypeMode = false;
					buttonLogout.setVisibility(View.VISIBLE);
				} else {
					showableLogInGroup(true);
					mTypeMode = false;
				}

			}
		};


		if (mSharedPreferences.getString(ROOT_PATH, null) == null) {
			checkAllPermission();
		} else {
			mRootPath = Uri.parse(mSharedPreferences.getString(ROOT_PATH, null));
			getDateFromFirebace();
		}

	}

	public void getDateFromFirebace() {
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference myRef = database.getReference();

		Query myPlace = myRef.child("Places");
		myPlace.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				if (places.size() > 0) {
					places.clear();
				}
				for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
					Place place = postSnapshot.getValue(Place.class);
					places.add(place);
				}
				downloadPhoto(places);

				if (tabLayout.getTabCount() == 0) {
					PlacesListFragment placesListFragment = new PlacesListFragment_();
					placesListFragment.setList(places, PLACE);
					adapter.addFragment(placesListFragment, "Place");
					adapter.notifyDataSetChanged();

				} else {
					tabLayout.getTabAt(0).setIcon(null);
					PlacesListFragment placesListFragment = (PlacesListFragment) adapter.getItem(0);
					placesListFragment.setList(places, PLACE);
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});

		Query myRouts = myRef.child("Rout");
		myRouts.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				if (routs.size() > 0) {
					routs.clear();
				}
				for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
					Rout rout = postSnapshot.getValue(Rout.class);
					routs.add(rout);
				}
				if (isOnline()) {
					downloadRoutToStorage(routs);
				}
				if (tabLayout.getTabCount() == 1) {
					RoutsListFragment routsListFragment = new RoutsListFragment_();
					routsListFragment.setList(routs, ROUT);
					adapter.addFragment(routsListFragment, "Routs");
					adapter.notifyDataSetChanged();

				} else {
					tabLayout.getTabAt(1).setIcon(null);
					RoutsListFragment routsListFragment = (RoutsListFragment_) adapter.getItem(1);
					routsListFragment.setList(routs, ROUT);
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});


		if (getIntent().getBooleanExtra(LOGIN, false)) {
			mDrawerLayout.openDrawer(Gravity.START, true);
			checkCurrentUser();
		}
		if (viewPager.getAdapter() == null) {
			viewPager.setAdapter(adapter);
		}

	}


	private void checkAllPermission() {
		List<String> mListPerm = new ArrayList<>();
		for (int i = 0; i < mPermissionList.length; i++) {
			if (ContextCompat.checkSelfPermission(StartActivity.this,
					mPermissionList[i])
					!= PackageManager.PERMISSION_GRANTED) {
				mListPerm.add(mPermissionList[i]);
			} else {

				if (mPermissionList[i].equals(WRITE_EXTERNAL_STORAGE)) {
					if (isExternalStorageWritable()) {
						mRootPath = Uri.fromFile(context.getExternalFilesDir(
								Environment.DIRECTORY_DOWNLOADS));
						mSharedPreferences.edit().putString(ROOT_PATH, mRootPath.toString()).apply();
						getDateFromFirebace();
					} else {
						if (context.getFilesDir().getFreeSpace() > 1250000L) {
							mRootPath = Uri.fromFile(context.getDir("my_carpathians", Context.MODE_PRIVATE));
							mSharedPreferences.edit().putString(ROOT_PATH, mRootPath.toString()).apply();
							getDateFromFirebace();
						} else {
							AlertDialog.Builder noAvailableStorageDialog = new AlertDialog.Builder(StartActivity.this);
							noAvailableStorageDialog.setTitle("Save Data");
							noAvailableStorageDialog.setMessage("External memory is not available! \n" +
									"And in the interior storage  is not enough free space.\n" + " You can not use this program in ofline");
							noAvailableStorageDialog.setPositiveButton("Oк", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
									startActivity(settingsIntent);
								}

							});
							AlertDialog alertDialog = builder.create();
							alertDialog.show();
						}
					}

				} else {
					Toast.makeText(this, "" + mPermissionList[i].toString() + " is already granted.", Toast.LENGTH_SHORT).show();
				}
			}
		}
		if (mListPerm.size() > 0) {
			String[] permision = new String[mListPerm.size()];
			for (int i = 0; i < mListPerm.size(); i++) {
				permision[i] = mListPerm.get(i);
			}

			ActivityCompat.requestPermissions(StartActivity.this, permision, 69);
		}


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		MenuItem item = menu.findItem(R.id.actionSearch);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				mQuery = newText;
				if (tabLayout.getTabCount() > 0 && tabLayout.getSelectedTabPosition() == PLACE) {
					PlacesListFragment placesListFragment = (PlacesListFragment) adapter.getItem(tabLayout.getSelectedTabPosition());
					placesListFragment.filter(newText);
				} else if (tabLayout.getTabCount() > 0 && tabLayout.getSelectedTabPosition() == ROUT) {
					RoutsListFragment routsListFragment = (RoutsListFragment) adapter.getItem(tabLayout.getSelectedTabPosition());
					routsListFragment.filter(newText);
				}

				return true;
			}
		});
		MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				if (tabLayout.getTabCount() == 2) {
					PlacesListFragment placesListFragment = (PlacesListFragment) adapter.getItem(PLACE);
					placesListFragment.filter(null);
					RoutsListFragment routsListFragment = (RoutsListFragment) adapter.getItem(ROUT);
					routsListFragment.filter(null);
					mQuery = null;
				}
				return true;
			}
		});
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		// If request is cancelled, the result arrays are empty.
		if (requestCode == 69 && grantResults.length > 0) {
			for (int i = 0; i < permissions.length; i++) {
				if (permissions[i].equals(ACCESS_FINE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
					Toast.makeText(StartActivity.this, " You gave permission" + permissions[i], Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(StartActivity.this, " You do not gave permission " + permissions[i], Toast.LENGTH_SHORT).show();
				}
				if (permissions[i].equals(WRITE_EXTERNAL_STORAGE)) {

					if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
						mRootPath = Uri.fromFile(context.getExternalFilesDir(
								Environment.DIRECTORY_DOWNLOADS));
						mSharedPreferences.edit().putString(ROOT_PATH, mRootPath.toString()).apply();
						getDateFromFirebace();
					} else {
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setTitle("Download Data!");
						builder.setMessage("You do not gave permission to download data to External Storage! Save data to Internal storage& ");
						builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								mRootPath = Uri.fromFile(context.getDir("my_carpathians", Context.MODE_PRIVATE));
								mSharedPreferences.edit().putString(ROOT_PATH, mRootPath.toString()).apply();
								getDateFromFirebace();
								dialogInterface.dismiss();
							}
						});
						builder.setNegativeButton("permission", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								checkAllPermission();
								dialogInterface.dismiss();
							}
						});
						builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialogInterface) {

							}
						});
						AlertDialog alertDialog = builder.create();
						alertDialog.show();
					}
				}
				if (permissions[i].equals(READ_EXTERNAL_STORAGE) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
				} else {

				}
			}

		} else {
			Toast.makeText(StartActivity.this, " You do not gave any permission ", Toast.LENGTH_SHORT).show();


		}
	}

	private void checkCurrentUser() {
		mUser = mAuth.getCurrentUser();
		if (mUser == null) {
			showableLogInGroup(true);
			updateUI(null, null);
			mTypeMode = false;
			showLoginDialog();
		} else if (mUser.isAnonymous()) {
			updateUI("Anonymous", null);
			mUserUID = mUser.getUid();
			mTypeMode = false;
			showInterfaceForAnonymous();
			produceToolsVisibility(mTypeMode);
		} else {
			if (mUser.getProviders().get(0).equals("google.com")) {
				produceToolsVisibility(mTypeMode);
				mTypeMode = true;
				loginGoogle();
				mUserUID = mUser.getUid();
				updateUI(mUser.getProviderData().get(0).getDisplayName(),
						String.valueOf(mUser.getProviderData().get(0).getPhotoUrl()));
				buttonLogout.setVisibility(View.VISIBLE);
			} else if (mUser.getProviders().get(0).equals("facebook.com")) {
				produceToolsVisibility(mTypeMode);
				mTypeMode = true;
				mUserUID = mUser.getUid();
				loginFacebook();
				updateUI(mUser.getProviderData().get(0).getDisplayName(),
						String.valueOf(mUser.getProviderData().get(0).getPhotoUrl()));
				buttonLogout.setVisibility(View.VISIBLE);
			} else if (mUser.getProviders().get(0).equals("password")) {
				produceToolsVisibility(mTypeMode);
				mTypeMode = true;
				mUserUID = mUser.getUid();
				updateUI(mUser.getProviderData().get(0).getEmail(), null);
				buttonLogout.setVisibility(View.VISIBLE);
			}
		}
		produceToolsVisibility(mTypeMode);
	}

	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}


	private void produceToolsVisibility(boolean mTypeMode) {
		if (mTypeMode) {
			buttonCreated.setVisibility(View.VISIBLE);
			fabRecEditor.setAlpha((float) 1);
		} else {
			buttonCreated.setVisibility(View.GONE);
			fabRecEditor.setAlpha((float) 0.5);
		}
	}

	private void showInterfaceForAnonymous() {

		buttonAuthorization.setText("Authentication");
		showableLogInGroup(false);
		buttonLogout.setVisibility(View.VISIBLE);
	}

	private void signOut() {
		if (mAuth.getCurrentUser() != null) {
			mAuth.signOut();
			updateUI(null, null);
			showableLogInGroup(true);
		}
	}


	/**
	 * This method is download and save routs track to SD card in package "Rout"
	 */
	@Background
	public void downloadRoutToStorage(List<Rout> routsList) {
		for (int i = 0; i < routsList.size(); i++) {
			FirebaseStorage storage = FirebaseStorage.getInstance();
			StorageReference httpsReference = storage.getReferenceFromUrl(routsList.get(i).getUrlRoutsTrack());
			File rootPath = new File(this.mRootPath.buildUpon().appendPath("Routs").build().getPath());
			if (!rootPath.exists()) {
				rootPath.mkdirs();
			}
			final File localFile = new File(rootPath, routsList.get(i).getNameRout());
			if (!localFile.exists()) {
				httpsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
					@Override
					public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
						Log.e("firebase ", ";local tem file  created " + localFile.toString());
					}
				}).addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception exception) {

						Log.e("firebase ", ";local tem file not created " + exception.toString());
					}
				});
			}
		}
	}

	@Background
	public void downloadPhoto(List<Place> placeList) {
		for (int i = 0; i < placeList.size(); i++) {
			try {
				URL url = new URL(placeList.get(i).getUrlPlace());

				File rootPath = new File(this.mRootPath.getPath(), "Photos");
				if (!rootPath.exists()) {
					rootPath.mkdirs();
				}


				File file = new File(rootPath, placeList.get(i).getNamePlace());
				if (!file.exists()) {
					URLConnection urlConnection = url.openConnection();
					InputStream inputStream = null;
					HttpURLConnection httpConn = (HttpURLConnection) urlConnection;
					httpConn.setRequestMethod("GET");
					httpConn.connect();

					if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
						inputStream = httpConn.getInputStream();
					}

					FileOutputStream fos = new FileOutputStream(file);
					int totalSize = httpConn.getContentLength();
					int downloadedSize = 0;
					byte[] buffer = new byte[1024];
					int bufferLength = 0;
					while ((bufferLength = inputStream.read(buffer)) > 0) {
						fos.write(buffer, 0, bufferLength);
						downloadedSize += bufferLength;
						Log.i("Progress:", "downloadedSize:" + downloadedSize + "totalSize:" + totalSize);
					}

					fos.close();
					Log.d("test", "Image Saved in sdcard..");
				}
			} catch (IOException io) {
				io.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	@Override
	public void onStart() {
		super.onStart();
		mAuth.addAuthStateListener(mAuthListener);

	}

	@Override
	public void onBackPressed() {

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		}


	}


	@Override
	public void onStop() {
		super.onStop();
		if (mAuthListener != null) {
			mAuth.removeAuthStateListener(mAuthListener);
		}
	}


	void showLoginDialog() {
		if (isOnline()) {
			builder = new AlertDialog.Builder(this);
			builder.setTitle("Для того щоб отримати доступ до всіх функцій програми потрібно зареєструватися");
			builder.setMessage("Виберіть спосіб реєстраці");

			builder.setPositiveButton("Зареєструватися", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int arg1) {

					mDrawerLayout.openDrawer(Gravity.START);
				}
			});
			builder.setNegativeButton("Анонімний вхід", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int arg1) {
					signInAnonymously();
					mDrawerLayout.closeDrawer(Gravity.START);

				}
			});
			builder.setCancelable(true);
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					signInAnonymously();
				}
			});

			AlertDialog alert = builder.create();
			alert.show();
		} else {
			builder = new AlertDialog.Builder(this);
			builder.setTitle("Мережа Інтернет");
			builder.setMessage("Нажаль ви зараз ви не підключені до мережі інтернет!" +
					" Керування автентифікацією недоступне! ");

			builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int arg1) {
					Intent intentSettingsNetwork = new Intent(Intent.ACTION_MAIN);
					intentSettingsNetwork.setClassName("com.android.phone", "com.android.phone.NetworkSetting");
					startActivity(intentSettingsNetwork);
				}
			});

			builder.setCancelable(true);


			AlertDialog alert = builder.create();
			alert.show();
		}
	}


	private void signInAnonymously() {
		mAuth.signInAnonymously()
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							// Sign in success, update UI with the signed-in mFirebaseUser's information
							Log.d(TAG, "signInAnonymously:success");

						} else {
							// If sign in fails, display a message to the mFirebaseUser.
							Log.w(TAG, "signInAnonymously:failure", task.getException());
							Toast.makeText(context, "Authentication failed.",
									Toast.LENGTH_SHORT).show();

						}
					}
				});
	}

	public boolean isOnline() {
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) StartActivity.this
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
			mConnected = networkInfo != null && networkInfo.isAvailable() &&
					networkInfo.isConnected();
			return mConnected;


		} catch (Exception e) {
			System.out.println("CheckConnectivity Exception: " + e.getMessage());
			Log.v("connectivity", e.toString());
		}
		return mConnected;
	}

	@Override
	public void putStringNamePlace(Place place) {
		Intent intentActionActivity = new Intent(context, ActionActivity_.class);
		intentActionActivity.putExtra(PUT_EXTRA_PLACE, place);
		ArrayList<Place> arrayListPlace = places;
		ArrayList<Rout> arrayListRouts = routs;
		intentActionActivity.putExtra(PUT_EXTRA_PLACE_LIST, arrayListPlace);
		intentActionActivity.putExtra(PUT_EXTRA_ROUTS_LIST, arrayListRouts);
		if (places.contains(place)) {
			intentActionActivity.putExtra(PRODUCE_MODE, false);
		} else {
			intentActionActivity.putExtra(PRODUCE_MODE, true);
		}
		startActivity(intentActionActivity);
	}

	@Override
	public void putStringNameRout(Rout rout) {
		Intent intentActionActivity = new Intent(context, ActionActivity_.class);
		intentActionActivity.putExtra(PUT_EXTRA_PLACE_LIST, places);
		intentActionActivity.putExtra(PUT_EXTRA_ROUTS_LIST, routs);
		intentActionActivity.putExtra(PUT_EXTRA_ROUT, rout);
		if (routs.contains(rout)) {
			intentActionActivity.putExtra(PRODUCE_MODE, false);
		} else {
			intentActionActivity.putExtra(PRODUCE_MODE, true);
		}
		startActivity(intentActionActivity);

	}


	public void showCreatedList(List<String> createdPlaces, List<String> createdRouts) {
		int dialogFlag = 0;

		if (createdPlaces != null) {
			List<Place> createdP = new ArrayList<>();
			if (createdPlaces.size() > 0) {
				for (int i = 0; i < createdPlaces.size(); i++) {
					File rootPath = new File(context.getExternalFilesDir(
							Environment.DIRECTORY_DOWNLOADS), "Created");
					if (!rootPath.exists()) {
						rootPath.mkdirs();
					}

					File file = new File(rootPath, createdPlaces.get(i));
					if (file.exists()) {
						try {
							FileInputStream fileIn = new FileInputStream(file);
							ObjectInputStream objectInputStream = new ObjectInputStream(fileIn);
							Place place = (Place) objectInputStream.readObject();
							objectInputStream.close();
							fileIn.close();
							createdP.add(place);

						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}
				if (createdP != null && tabLayout.getTabCount() > 0) {
					tabLayout.getTabAt(0).setIcon(R.drawable.ic_create_black_24px);
					PlacesListFragment placesListFragment = (PlacesListFragment) adapter.getItem(0);
					placesListFragment.setList(createdP, MY_PLACE);
				}
			} else {
				if (tabLayout.getTabCount() > 0) {
					tabLayout.getTabAt(0).setIcon(R.drawable.ic_create_black_24px);
					PlacesListFragment placesListFragment = (PlacesListFragment) adapter.getItem(0);
					placesListFragment.setList(new ArrayList<Place>(), MY_PLACE);
				}
				dialogFlag++;

			}


		} else {
			dialogFlag++;
		}
		if (createdRouts != null) {
			List<Rout> createdR = new ArrayList<>();
			if (createdRouts.size() > 0) {
				for (int i = 0; i < createdRouts.size(); i++) {
					File rootPath = new File(context.getExternalFilesDir(
							Environment.DIRECTORY_DOWNLOADS), "Created");
					if (!rootPath.exists()) {
						rootPath.mkdirs();
					}

					File file = new File(rootPath, createdRouts.get(i));
					if (file.exists()) {
						try {
							FileInputStream fileIn = new FileInputStream(file);
							ObjectInputStream objectInputStream = new ObjectInputStream(fileIn);
							Rout rout = (Rout) objectInputStream.readObject();
							objectInputStream.close();
							fileIn.close();
							createdR.add(rout);

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				if (createdR != null && tabLayout.getTabCount() > 1) {
					tabLayout.getTabAt(1).setIcon(R.drawable.ic_create_black_24px);
					RoutsListFragment routsListFragment = (RoutsListFragment) adapter.getItem(1);
					routsListFragment.setList(createdR, MY_ROUT);
				}
			} else {
				if (tabLayout.getTabCount() > 1) {
					tabLayout.getTabAt(1).setIcon(R.drawable.ic_create_black_24px);
					RoutsListFragment routsListFragment = (RoutsListFragment) adapter.getItem(1);
					routsListFragment.setList(new ArrayList<Rout>(), MY_ROUT);
				}
				dialogFlag++;
			}
		} else {
			dialogFlag++;
		}
		if (dialogFlag > 1) {
			showFavoriteEmptyDialog("Created");
			setDrawerState(false);
		} else {

			setDrawerState(false);
		}

	}

	public void showFavoriteList(List<String> favoritesPlaces, List<String> favoritesRouts) {
		int dialogFlag = 0;
		if (favoritesPlaces != null) {
			List<Place> favoriteP = new ArrayList<>();
			if (favoritesPlaces.size() > 0) {
				for (int i = 0; i < places.size(); i++) {
					for (int s = 0; s < favoritesPlaces.size(); s++) {
						if (places.get(i).getNamePlace().equals(favoritesPlaces.get(s))) {
							favoriteP.add(places.get(i));
							break;
						}
					}
				}
				if (favoriteP != null && tabLayout.getTabCount() > 0) {
					tabLayout.getTabAt(0).setIcon(R.drawable.ic_favorite_border_black_24px);
					PlacesListFragment placesListFragment = (PlacesListFragment) adapter.getItem(0);
					placesListFragment.setList(favoriteP, FA_PLACE);
				}
			} else {
				if (tabLayout.getTabCount() > 0) {
					tabLayout.getTabAt(0).setIcon(R.drawable.ic_favorite_border_black_24px);
					PlacesListFragment placesListFragment = (PlacesListFragment) adapter.getItem(0);
					placesListFragment.setList(new ArrayList<Place>(), FA_PLACE);
				}
				dialogFlag++;

			}


		} else {
			dialogFlag++;
		}
		if (favoritesRouts != null) {
			List<Rout> favoriteR = new ArrayList<>();
			if (favoritesRouts.size() > 0) {
				for (int i = 0; i < routs.size(); i++) {
					for (int s = 0; s < favoritesRouts.size(); s++) {
						if (routs.get(i).getNameRout().equals(favoritesRouts.get(s))) {
							favoriteR.add(routs.get(i));
							break;
						}
					}

				}
				if (favoriteR != null && tabLayout.getTabCount() > 1) {
					tabLayout.getTabAt(1).setIcon(R.drawable.ic_favorite_border_black_24px);
					RoutsListFragment routsListFragment = (RoutsListFragment) adapter.getItem(1);
					routsListFragment.setList(favoriteR, FA_ROUT);
				}
			} else {
				if (tabLayout.getTabCount() > 1) {
					tabLayout.getTabAt(1).setIcon(R.drawable.ic_favorite_border_black_24px);
					RoutsListFragment routsListFragment = (RoutsListFragment) adapter.getItem(1);
					routsListFragment.setList(new ArrayList<Rout>(), FA_ROUT);
				}
				dialogFlag++;
			}
		} else {
			dialogFlag++;
		}
		if (dialogFlag > 1) {
			showFavoriteEmptyDialog("Favorite");
			setDrawerState(false);
		} else {

			setDrawerState(false);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		setDrawerState(true);
		getDateFromFirebace();
	}

	public void setDrawerState(boolean isEnabled) {
		if (isEnabled) {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
			actionBarDrawerToggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_UNLOCKED);
			actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
			actionBarDrawerToggle.syncState();
		} else {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			actionBarDrawerToggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			actionBarDrawerToggle.setDrawerIndicatorEnabled(false);
			actionBarDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					getDateFromFirebace();
					setDrawerState(true);
				}
			});
			actionBarDrawerToggle.syncState();

		}
	}

	private void showFavoriteEmptyDialog(String nameList) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Favorite");
		builder.setMessage("Your " + nameList + " List is empty!");
		builder.setIcon(R.drawable.ic_favorite_border_black_24px);
		builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
			}
		});
		AlertDialog dialogAboutEmptyFavoriteList = builder.create();
		dialogAboutEmptyFavoriteList.show();
	}

	@Override
	public void deletedFromFavoriteList(final List<String> names, final int type) {

		if (type == ROUT) {
			Set<String> favoritesRoutsList = new HashSet<>(mSharedPreferences.getStringSet(FAVORITES_ROUTS_LIST, new HashSet<String>()));
			favoritesRoutsList.removeAll(names);
			SharedPreferences.Editor editor = mSharedPreferences.edit();
			editor.putStringSet(FAVORITES_ROUTS_LIST, favoritesRoutsList);
			editor.apply();
			ArrayList<String> mListRouts = null;
			if (favoritesRoutsList != null) {
				mListRouts = new ArrayList<>(favoritesRoutsList);
			}
			showFavoriteList(null, mListRouts);
		} else if (type == PLACE) {
			Set<String> favoritesPlacesList = new HashSet<>(mSharedPreferences.getStringSet(FAVORITES_PLACE_LIST, new HashSet<String>()));
			favoritesPlacesList.removeAll(names);
			SharedPreferences.Editor editor = mSharedPreferences.edit();
			editor.putStringSet(FAVORITES_PLACE_LIST, favoritesPlacesList);
			editor.apply();
			ArrayList<String> mListPlaces = null;
			if (favoritesPlacesList != null) {
				mListPlaces = new ArrayList<>(favoritesPlacesList);
			}
			showFavoriteList(mListPlaces, null);

		}

	}

	@Override
	public void deletedFromCreatedList(final List<String> names, final int type) {

		if (type == ROUT) {
			for (int p = 0; p < names.size(); p++) {
				ObjectService objectService = new ObjectService(StartActivity.this, mRootPath.toString());
				String mOutcome = objectService.deleteRout(names.get(p));
				if (!mOutcome.equals(ERROR)) {
					Toast.makeText(StartActivity.this, mOutcome, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(StartActivity.this, mOutcome, Toast.LENGTH_LONG).show();
				}
			}
		} else if (type == PLACE) {
			for (int p = 0; p < names.size(); p++) {
				ObjectService objectService = new ObjectService(StartActivity.this, mRootPath.toString());
				String mOutcome = objectService.deletePlace(names.get(p));
				if (!mOutcome.equals(ERROR)) {
					Toast.makeText(StartActivity.this, mOutcome, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(StartActivity.this, mOutcome, Toast.LENGTH_LONG).show();
				}
			}
		}

	}


	@Click(R.id.fabRecEditor)
	void fabRecEditorWasClicked() {
		if (mTypeMode) {
			Intent intentMapActivity = new Intent(context, MapsActivity_.class);
			intentMapActivity.putExtra(PRODUCE_MODE, mTypeMode);
			startActivity(intentMapActivity);
		} else {
			showLoginDialog();
		}
	}

	@Click(R.id.buttonResetPassword)
	public void buttonResetPasswordWasClicked() {
		if (isOnline()) {
			startActivity(new Intent(StartActivity.this, ResetPasswordActivity_.class));
		} else {
			showLoginDialog();
		}
	}

	@Click(R.id.emailLoginButton)
	public void emailLoginButtonWasClicked() {
		if (isOnline()) {
			final CharSequence email = inputEmail.getText();
			final CharSequence password = inputPassword.getText();


			if (TextUtils.isEmpty(email)) {
				Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
				return;
			}

			if (TextUtils.isEmpty(password)) {
				Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
				return;
			}

			progressBar.setVisibility(View.VISIBLE);

			if (isValidEmail(email)) {
				mAuth.signInWithEmailAndPassword(email.toString(), password.toString())
						.addOnCompleteListener(StartActivity.this, new OnCompleteListener<AuthResult>() {
							@Override
							public void onComplete(@NonNull Task<AuthResult> task) {
								// If sign in fails, display a message to the mUser. If sign in succeeds
								// the mAuth state listener will be notified and logic to handle the
								// signed in mUser can be handled in the listener.
								progressBar.setVisibility(View.GONE);
								if (!task.isSuccessful()) {
									// there was an error
									if (password.length() < 6) {
										inputPassword.setError(getString(R.string.minimum_password));
									} else {
										AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
										builder.setTitle("Registration!");
										builder.setMessage("An account with this email address and password  does not exist! Create a new account?");
										builder.setPositiveButton("Create new", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												createNewAccount();
											}
										});
										builder.setNegativeButton("Check password", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
											}
										});
										builder.show();
									}
								} else {
									updateUI(email.toString(), null);
									showableLogInGroup(false);

								}
							}
						});
			} else {
				inputEmail.setError("Invalid email!");
			}
		} else {
			showLoginDialog();
		}
	}

	private void createNewAccount() {
		progressBar.setVisibility(View.VISIBLE);
		//create user
		mAuth.createUserWithEmailAndPassword(inputEmail.getText().toString(), inputPassword.getText().toString())
				.addOnCompleteListener(StartActivity.this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						Toast.makeText(StartActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
						progressBar.setVisibility(View.GONE);
						// If sign in fails, display a message to the user. If sign in succeeds
						// the auth state listener will be notified and logic to handle the
						// signed in user can be handled in the listener.
						if (!task.isSuccessful()) {
							Toast.makeText(StartActivity.this, "Authentication failed." + task.getException(),
									Toast.LENGTH_LONG).show();
						} else {
							mSharedPreferences.edit().putString(LOGIN_NAME, inputEmail.getText().toString()).apply();
							updateUI(inputEmail.getText().toString(), null);
							mUserUID = mAuth.getCurrentUser().getUid();
							mTypeMode = true;
							produceToolsVisibility(mTypeMode);
							progressBar.setVisibility(View.GONE);
						}
					}
				});


	}

	private boolean isValidEmail(CharSequence email) {
		if (email == null) {
			return false;
		} else {
			return Patterns.EMAIL_ADDRESS.matcher(email).matches();
		}
	}

	private void loginFacebook() {
		if (isOnline()) {
			// loginFacebook.setVisibility(View.VISIBLE);

			// If using in a fragment
			AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
				@Override
				protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken2) {
					Log.d(TAG, "onCurrentAccessTokenChanged()");
					if (accessToken == null) {
						showableLogInGroup(false);
					} else if (accessToken2 == null) {
						LoginManager.getInstance().logOut();
						mAuth.signOut();
						showableLogInGroup(true);
					}
				}
			};
			mCallbackManager = CallbackManager.Factory.create();

			//loginFacebook.setReadPermissions("email");
			// Callback registration
			mFacebookLoginManager = LoginManager.getInstance();
			mFacebookLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
				@Override
				public void onSuccess(LoginResult loginResult) {
					Toast toast = Toast.makeText(StartActivity.this, "Logged In", Toast.LENGTH_SHORT);
					handleFacebookAccessToken(loginResult.getAccessToken());
					toast.show();
				}

				@Override
				public void onCancel() {
					showableLogInGroup(true);
					updateUI(null, null);
				}

				@Override
				public void onError(FacebookException exception) {
					progressBar.setVisibility(View.GONE);
					showableLogInGroup(true);
					updateUI(null, null);
				}

			});
		} else {
			showLoginDialog();
		}

	}

	private void showableLogInGroup(boolean b) {
		if (b) {
			inputPasswordLayout.setVisibility(View.VISIBLE);
			inputEmailLayout.setVisibility(View.VISIBLE);
			if (mSharedPreferences.getString(LOGIN_NAME, null) != null) {
				inputEmail.setText(mSharedPreferences.getString(LOGIN_NAME, null));
			}
			emailLoginButton.setVisibility(View.VISIBLE);
			buttonResetPassword.setVisibility(View.VISIBLE);
			facebookLoginButton.setVisibility(View.VISIBLE);
			googleLoginButton.setVisibility(View.VISIBLE);
			buttonAuthorization.setVisibility(View.GONE);
			settingsGroup.setVisibility(View.GONE);
			buttonLogout.setVisibility(View.GONE);
		} else {
			inputPasswordLayout.setVisibility(View.GONE);
			inputEmailLayout.setVisibility(View.GONE);
			emailLoginButton.setVisibility(View.GONE);
			buttonResetPassword.setVisibility(View.GONE);
			facebookLoginButton.setVisibility(View.GONE);
			googleLoginButton.setVisibility(View.GONE);
			settingsGroup.setVisibility(View.VISIBLE);
			buttonLogout.setVisibility(View.VISIBLE);
		}
	}

	private void loginGoogle() {

		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(getString(R.string.default_web_client_id))
				.requestEmail()
				.build();
		// [END config_signin]
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
					.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
					.build();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Pass the activity result back to the Facebook SDK
		if (mCallbackManager != null) {
			mCallbackManager.onActivityResult(requestCode, resultCode, data);
		}
		// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
		if (requestCode == RC_SIGN_IN) {
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
			if (result.isSuccess()) {
				// Google Sign In was successful, authenticate with Firebase
				GoogleSignInAccount account = result.getSignInAccount();
				firebaseAuthWithGoogle(account);
			} else {
				// Google Sign In failed, update UI appropriately
				// [START_EXCLUDE]
				showableLogInGroup(true);
				// [END_EXCLUDE]
			}
		}
	}

	private void updateUI(String email, String photoUrl) {
		if (photoUrl != null) {
			Glide
					.with(this)
					.load(photoUrl)
					.into(userAccountImage);
		} else {
			userAccountImage.setImageResource(R.drawable.user_default);
		}
		if (email != null) {
			textViewEmail.setText(email);
		} else {
			textViewEmail.setText("");
			inputEmail.setText("");
			inputPassword.setText("");
		}
	}

	private void handleFacebookAccessToken(AccessToken token) {
		//Log.d(TAG, "handleFacebookAccessToken:" + token)

		AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
		mAuth.signInWithCredential(credential)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						//Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

						// If sign in fails, display a message to the mUser. If sign in succeeds
						// the mAuth state listener will be notified and logic to handle the
						// signed in mUser can be handled in the listener.
						if (!task.isSuccessful()) {
							progressBar.setVisibility(View.GONE);
							//Log.w(TAG, "signInWithCredential", task.getException());
							Toast.makeText(StartActivity.this, "Authentication failed.",
									Toast.LENGTH_SHORT).show();
							showableLogInGroup(true);

						} else {
							Profile profile = Profile.getCurrentProfile();
							updateUI(profile.getFirstName(), profile.getProfilePictureUri(200, 200).toString());
							mUserUID = mAuth.getCurrentUser().getUid();
							mTypeMode = true;
							produceToolsVisibility(mTypeMode);
							progressBar.setVisibility(View.GONE);
						}
					}
				});
	}

	private void signIn() {
		progressBar.setVisibility(View.VISIBLE);
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
		startActivityForResult(signInIntent, RC_SIGN_IN);
	}


	private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
		Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

		AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
		mAuth.signInWithCredential(credential)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							// Sign in success, update UI with the signed-in mUser's information
							Log.d(TAG, "signInWithCredential:success");
							updateUI(acct.getDisplayName(), String.valueOf(acct.getPhotoUrl()));
							mUserUID = mAuth.getCurrentUser().getUid();
							produceToolsVisibility(mTypeMode = true);
							progressBar.setVisibility(View.GONE);

						} else {
							// If sign in fails, display a message to the mUser.
							Log.w(TAG, "signInWithCredential:failure", task.getException());
							Toast.makeText(StartActivity.this, "Authentication failed.",
									Toast.LENGTH_SHORT).show();
							updateUI(null, null);
							showableLogInGroup(true);
							progressBar.setVisibility(View.GONE);
						}
					}
				});
	}


	private void googleSignOut() {
		Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
				new ResultCallback<Status>() {
					@Override
					public void onResult(@NonNull Status status) {
						if (status.isSuccess()) {
							signOut();
						}
						// ...
					}
				});
	}


	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Log.d(TAG, "onConnectionFailed:" + connectionResult);
		Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
	}

	@Click(R.id.facebokLoginButton)
	void facebookLoginButtonWasClicked() {
		if (isOnline()) {
			showableLogInGroup(false);
			loginFacebook();
			sigInFacebook();
		} else {
			showLoginDialog();
		}
	}

	private void sigInFacebook() {
		progressBar.setVisibility(View.VISIBLE);
		mFacebookLoginManager.logInWithReadPermissions(StartActivity.this, Arrays.asList("public_profile", "email"));
	}

	@Click(R.id.googleLoginButton)
	void googleLoginButtonWasClicked() {
		if (isOnline()) {
			showableLogInGroup(false);
			loginGoogle();
			signIn();
			//  loginGoogle.setVisibility(View.VISIBLE);
		} else {
			showLoginDialog();
		}
	}

	@Click(R.id.buttonLogout)
	void buttonLogoutWasClicked() {
		if (isOnline()) {

			if (mUser.isAnonymous()) {
				signOut();
			} else {
				if (mUser.getProviders().get(0).equals("google.com")) {
					googleSignOut();
				} else if (mUser.getProviders().get(0).equals("facebook.com")) {
					LoginManager.getInstance().logOut();
					signOut();
				} else if (mUser.getProviders().get(0).equals("password")) {
					signOut();
				}
			}
		} else {
			showLoginDialog();
		}
	}

	@Click(R.id.buttonAuthorization)
	void buttonAuthorizationWasClicked() {
		if (isOnline()) {
			signOut();
			showableLogInGroup(true);
		} else {
			showLoginDialog();
		}
	}


	@Click(R.id.buttonFavorites)
	void buttonFavoritesWasClicked() {
		mSharedPreferences = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		Set<String> favoritesPlacesList = mSharedPreferences.getStringSet(FAVORITES_PLACE_LIST, null);
		Set<String> favoritesRoutsList = mSharedPreferences.getStringSet(FAVORITES_ROUTS_LIST, null);
		ArrayList<String> mListRouts;
		ArrayList<String> mListPlaces;
		if (favoritesPlacesList != null && favoritesPlacesList.size() > 0) {
			mListPlaces = new ArrayList<>(favoritesPlacesList);
		} else {
			mListPlaces = new ArrayList<>();
		}

		if (favoritesRoutsList != null && favoritesRoutsList.size() > 0) {
			mListRouts = new ArrayList<>(favoritesRoutsList);

		} else {
			mListRouts = new ArrayList<>();
		}
		showFavoriteList(mListPlaces, mListRouts);

	}


	@Click(R.id.buttonCreated)
	void buttonMyCreated() {
		mSharedPreferences = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		Set<String> createdByUserPlaceList = mSharedPreferences.getStringSet(CREATED_BY_USER_PLACE_LIST, null);
		mSharedPreferences = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		Set<String> createdByUserTrackList = mSharedPreferences.getStringSet(CREATED_BY_USER_ROUT_LIST, null);
		ArrayList<String> mListRouts;
		ArrayList<String> mListPlaces;
		if (createdByUserPlaceList != null && createdByUserPlaceList.size() > 0) {
			mListPlaces = new ArrayList<>(createdByUserPlaceList);
		} else {
			mListPlaces = new ArrayList<>();
		}
		if (createdByUserTrackList != null && createdByUserTrackList.size() > 0) {
			mListRouts = new ArrayList<>(createdByUserTrackList);

		} else {
			mListRouts = new ArrayList<>();
		}
		showCreatedList(mListPlaces, mListRouts);
	}

	@Click(R.id.buttonMapOffline)
	public void buttonMapOfflineWasClicked() {
		// Build a region list when the user clicks the list button

		// Reset the region selected int to 0
		mRegionSelected = 0;
		// Query the DB asynchronously
		Mapbox.getInstance(this, getString(R.string.access_token));
		OfflineManager.getInstance(StartActivity.this).listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
			@Override
			public void onList(final OfflineRegion[] offlineRegions) {
				// Check result. If no regions have been
				// downloaded yet, notify user and return
				if (offlineRegions == null || offlineRegions.length == 0) {
					Toast.makeText(getApplicationContext(), getString(R.string.toast_no_regions_yet), Toast.LENGTH_SHORT).show();
					return;
				}

				// Add all of the region names to a list
				ArrayList<String> offlineRegionsNames = new ArrayList<>();
				for (OfflineRegion offlineRegion : offlineRegions) {
					offlineRegionsNames.add(getRegionName(offlineRegion));
				}
				final CharSequence[] items = offlineRegionsNames.toArray(new CharSequence[offlineRegionsNames.size()]);

				// Build a dialog containing the list of regions
				AlertDialog dialog = new AlertDialog.Builder(StartActivity.this)
						.setTitle(getString(R.string.navigate_title))
						.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// Track which region the user selects
								mRegionSelected = which;
							}
						})
						.setPositiveButton(getString(R.string.navigate_positive_button), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {

								Toast.makeText(StartActivity.this, items[mRegionSelected], Toast.LENGTH_LONG).show();
								Intent mapIntent = new Intent(StartActivity.this, MapsActivity_.class);
								mapIntent.putExtra(OFFLINE_MAP, items[mRegionSelected]);
								startActivity(mapIntent);

							}
						})
						.setNeutralButton(getString(R.string.navigate_neutral_button_title), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// Make progressBar indeterminate and
								// set it to visible to signal that
								// the deletion process has begun
								progressBar.setIndeterminate(true);
								progressBar.setVisibility(View.VISIBLE);

								// Begin the deletion process
								offlineRegions[mRegionSelected].delete(new OfflineRegion.OfflineRegionDeleteCallback() {
									@Override
									public void onDelete() {
										// Once the region is deleted, remove the
										// progressBar and display a toast
										progressBar.setVisibility(View.INVISIBLE);
										progressBar.setIndeterminate(false);
										Toast.makeText(getApplicationContext(), getString(R.string.toast_region_deleted),
												Toast.LENGTH_LONG).show();
									}

									@Override
									public void onError(String error) {
										progressBar.setVisibility(View.INVISIBLE);
										progressBar.setIndeterminate(false);
										Log.e(TAG, "Error: " + error);
									}
								});
							}
						})
						.setNegativeButton(getString(R.string.navigate_negative_button_title), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// When the user cancels, don't do anything.
								// The dialog will automatically close
							}
						}).create();
				dialog.show();

			}

			@Override
			public void onError(String error) {
				Log.e(TAG, "Error: " + error);
			}
		});
	}

	private String getRegionName(OfflineRegion offlineRegion) {
		// Get the region name from the offline region metadata
		String regionName;

		try {
			byte[] metadata = offlineRegion.getMetadata();
			String json = new String(metadata, JSON_CHARSET);
			JSONObject jsonObject = new JSONObject(json);
			regionName = jsonObject.getString(JSON_FIELD_REGION_NAME);
		} catch (Exception exception) {
			Log.e(TAG, "Failed to decode metadata: " + exception.getMessage());
			regionName = String.format(getString(R.string.region_name), offlineRegion.getID());
		}
		return regionName;
	}

	@Click(R.id.buttonSettings)
	public void buttonSettingsWasClicked() {
		startActivity(new Intent(StartActivity.this, SettingsActivity_.class));
	}
}
