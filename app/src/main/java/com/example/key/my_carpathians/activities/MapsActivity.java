package com.example.key.my_carpathians.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.cocoahero.android.geojson.Position;
import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.interfaces.CommunicatorMapActivity;
import com.example.key.my_carpathians.interfaces.ILocation;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;
import com.example.key.my_carpathians.utils.AltitudeFinder;
import com.example.key.my_carpathians.utils.GPSActionModeCallback;
import com.example.key.my_carpathians.utils.HandActionModeCallback;
import com.example.key.my_carpathians.utils.LocationService;
import com.example.key.my_carpathians.utils.ObjectService;
import com.example.key.my_carpathians.utils.OfflineRegionActionModeCallbac;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;
import com.mapbox.services.commons.utils.TextUtils;
import com.victor.loading.rotate.RotateLoading;

import org.androidannotations.annotations.Background;
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
import java.util.List;
import java.util.Random;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.example.key.my_carpathians.activities.ActionActivity.ROUT_STR;
import static com.example.key.my_carpathians.activities.ActionActivity.SELECTED_USER_PLACES;
import static com.example.key.my_carpathians.activities.ActionActivity.SELECTED_USER_ROUTS;
import static com.example.key.my_carpathians.activities.SettingsActivity.AVERAGE_VALUE;
import static com.example.key.my_carpathians.activities.SettingsActivity.VALUE_OFFLINE_REGION_AROUND_RADIUS;
import static com.example.key.my_carpathians.activities.StartActivity.OFFLINE_MAP;
import static com.example.key.my_carpathians.activities.StartActivity.PLACE;
import static com.example.key.my_carpathians.activities.StartActivity.PREFS_NAME;
import static com.example.key.my_carpathians.activities.StartActivity.PRODUCE_MODE;
import static com.example.key.my_carpathians.activities.StartActivity.ROOT_PATH;
import static com.example.key.my_carpathians.activities.StartActivity.ROUT;
import static com.example.key.my_carpathians.utils.ObjectService.FILE_EXISTS;

@EActivity
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, CommunicatorMapActivity {
    public static final double CONSTANT_PERIMETER_SIZE = 0.01;
    public static final String TO_SERVICE_COMMANDS = "service_commands";
    public static final int COMMAND_REC_ROUT = 4;
    public static final int COMMAND_REC_PLACE = 5;
    public static final int COMMAND_NO_SAVE = 3;
    public static final String TO_SERVICE_TRACK_NAME = "track_name";
    public static final int ERROR_TRACK = 10;
    public static final int COMMAND_PAUSE_REC_ROUT = 6;
    public static final int START = 100;
    public static final int REC =200;
    public static final int REGION = 1001;
    private static final double DEFAULT_PERIMETER_MULTIPLIER_VALUE = 0.08;
    private static final String CHECK_FOR_REC_BUTTON = "check_for_rec_button";
    public ILocation iCapture;
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";
    public ArrayList<String> selectUserRouts = null;
    public List<Place> selectUserPlacesList = null;
    public List<Position> createdTrackPosition;
    public Marker startMarker;
    public AlertDialog alert;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private MapView mMapView;
    private MapboxMap mapboxMap;
    private static final String TAG = "MapsActivity";
    private boolean isEndNotified;
    private ProgressBar mProgressBar;
    private OfflineManager mOfflineManager;
    private int mRegionSelected;
    private boolean mChecker = false;
    private LocationService mLocationService;
    private boolean mCheckForRecButton = true;
    private boolean mTypeMode;
    private int mPointCounter = 0;
    private Marker mMarker;
	private boolean isConnected;
    private ServiceConnection mCaptureServiceConnection;
    private MapboxMap.OnMyLocationChangeListener myLocationChangeListener;
    public SharedPreferences sharedPreferences;
    private String mRootPathString;
    private Menu mMenu;
    private ActionMode mActionMode;
    private boolean isFlash = true;
    private boolean isStartedAnimationChecker;
    private PolygonOptions mPolygonOptions;
    private LatLng mPoint;
    private com.mapbox.mapboxsdk.annotations.Polygon mOfflinePolygon;
    private double mPerimeterMultiplierValue;
    private String mOfflineRegionName;
    @ViewById(R.id.seekBar)
    SeekBar seekBar;

    @ViewById(R.id.toolBarMapActivity)
    Toolbar toolbar;

    @ViewById(R.id.progressGPS)
    RelativeLayout customProgressBarGPS;

    @ViewById(R.id.gpsLoading)
    RotateLoading gpsLoading;
    private LatLng mPointPlace;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        location();
        sharedPreferences = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        mRootPathString = sharedPreferences.getString(ROOT_PATH, null);
        selectUserRouts = getIntent().getStringArrayListExtra(SELECTED_USER_ROUTS);
        selectUserPlacesList = (List<Place>) getIntent().getSerializableExtra(SELECTED_USER_PLACES);
        mTypeMode = getIntent().getBooleanExtra(PRODUCE_MODE, false);
        mOfflineRegionName = getIntent().getStringExtra(OFFLINE_MAP);
        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapView.
        Mapbox.getInstance(this, getString(R.string.access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_maps);
        setSupportActionBar(toolbar);
        toolbar.showOverflowMenu();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mOfflineManager = OfflineManager.getInstance(MapsActivity.this);
        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
    }

    private void location() {
        iCapture = new ILocation() {
            @Override
            public void update(Location location, int type) {

                if(location != null) {
                    if(customProgressBarGPS.getVisibility() == View.VISIBLE){
                        enabledProgressGPS(false, type);
                    }

                    myLocationChangeListener.onMyLocationChange(location);
                    mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))  // set the camera's center position
                                    .build()));
                }

                showRecLine(new LatLng(location.getLatitude(), location.getLongitude()), type);

            }

            @Override
            public void connectionState(int state) {
                if (state == 1) {
                    Toast.makeText(MapsActivity.this,
                            getString(R.string.disabled_google_play_servise), Toast.LENGTH_LONG).show();
                } else {
                    GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
                    apiAvailability.getErrorDialog(MapsActivity.this, state, PLAY_SERVICES_RESOLUTION_REQUEST)
                            .show();
                }
            }

            @Override
            public void messageForActivity(int type, String name) {
                if (type == ERROR_TRACK){
                    showErrorDialog();
                }else {
                    showCreateNameDialog(type, name);
                }
            }
        };
    }

    private void showErrorDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.save_date));

        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.save_date_message_error_save_rout));

        // On pressing Settings button
        alertDialog.setPositiveButton(getString(R.string.settings), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                MapsActivity.this.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton(getString(R.string.cancelled), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();

    }

    @Override
    public void onBackPressed() {
        if (mCaptureServiceConnection != null & mChecker){
            Intent intent = new Intent(this, LocationService.class);
            unbindService(mCaptureServiceConnection);
            MapsActivity.this.stopService(intent);
            mapboxMap.setMyLocationEnabled(false);
            mCaptureServiceConnection = null;
        }
        if (mOfflinePolygon != null){
            removeAll();
        }else {
            super.onBackPressed();
        }
    }

    private void enabledGPS(boolean b) {

        if (b) {
            enabledProgressGPS(true, 0);
            toggleGps(true);
            mMenu.findItem(R.id.action_g_p_s).setIcon(R.drawable.ic_location_disabled_24dp);
            mChecker = true;
        } else {
            enabledProgressGPS(false, 0);
            toggleGps(false);
            mMenu.findItem(R.id.action_g_p_s).setIcon(R.drawable.ic_my_location_24dp);
            mChecker = false;
        }
    }

    private void checkGPSEnabled() {

        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);

            // Setting Dialog Title
            alertDialog.setTitle(getString(R.string.permission));

            // Setting Dialog Message
            alertDialog.setMessage(getString(R.string.permission_not_granted)
                    + getString(R.string.permission_location));

            // On pressing Settings button
            alertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{ACCESS_FINE_LOCATION}, 69);
                }
            });

            // on pressing cancel button
            alertDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            // Showing Alert Message
            alertDialog.show();
        } else {
            LocationManager lm = (LocationManager) MapsActivity
                    .this.getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled;
            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (!gps_enabled) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);

                    // Setting Dialog Title
                    alertDialog.setTitle(getString(R.string.settings));
                    // Setting Dialog Message
                    alertDialog.setMessage(getString(R.string.settings_gps_message));

                    // On pressing Settings button
                    alertDialog.setPositiveButton(getString(R.string.settings), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            MapsActivity.this.startActivity(intent);
                            enabledGPS(true);
                        }
                    });

                    // on pressing cancel button
                    alertDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    // Showing Alert Message
                    alertDialog.show();

                }else{
                    enabledGPS(true);
                }
            } catch (Exception ex) {
            }
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map_activity, menu);
        this.mMenu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mTypeMode){
            menu.findItem(R.id.auto_create_offline_region).setVisible(false);

        }else {
            menu.findItem(R.id.create_place).setVisible(false);
            menu.findItem(R.id.create_rout).setVisible(false);
            menu.findItem(R.id.download_region).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_place:
                showCreateDialog(PLACE);
                return true;
            case R.id.create_rout:
                showCreateDialog(ROUT);
                return true;
            case R.id.download_region:
                startActionModOfflineRegion();
                return true;
            case R.id.action_g_p_s:
                if(mapboxMap != null & !mChecker){
                    checkGPSEnabled();

                }else{
                    enabledGPS(false);
                }
                return true;
            case R.id.offline_regions:
                downloadedRegionList();
                return true;
            case R.id.auto_create_offline_region:
                startActionModOfflineRegion();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startActionModOfflineRegion() {
        if (isOnline()){
            if (mTypeMode){
                mActionMode = this.startSupportActionMode(new OfflineRegionActionModeCallbac(this));
                autoOrientationOff(true);
                enabledHandsMode(REGION);
                mActionMode.setTitle(getString(R.string.action_mode_title_new_region));
                mActionMode.setSubtitle(getString(R.string.action_mode_tsubitle_new_region));
            }else{
                double valueOfflineRegionPerimeter = CONSTANT_PERIMETER_SIZE
                        * getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .getInt(VALUE_OFFLINE_REGION_AROUND_RADIUS, AVERAGE_VALUE );
                downloadRegionDialog(valueOfflineRegionPerimeter);
            }


        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.internet));
            builder.setMessage(getString(R.string.need_internet_to_use));
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(getString(R.string.settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setClassName("com.android.phone",
                            "com.android.phone.NetworkSetting");
                    startActivity(intent);
                    dialog.dismiss();
                }
            });
             builder.create().show();
        }
    }
	private void showRegion(LatLng point) {
        mPoint = point;
        seekBar.setVisibility(View.VISIBLE);
        seekBar.setSecondaryProgress(1);
        seekBar.setProgress(1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updatePolygonView(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mapboxMap.setOnMapClickListener(null);
        mActionMode.setSubtitle(null);
        mActionMode.getMenu().findItem(R.id.actionDownload).setVisible(true);
        mActionMode.getMenu().findItem(R.id.actactionDellRegion).setVisible(true);
		List<LatLng> points = new ArrayList<>();

        mPerimeterMultiplierValue = DEFAULT_PERIMETER_MULTIPLIER_VALUE;
        points.add(new LatLng(point.getLatitude()+ mPerimeterMultiplierValue, point.getLongitude() - mPerimeterMultiplierValue));
        points.add(new LatLng(point.getLatitude()- mPerimeterMultiplierValue, point.getLongitude() - mPerimeterMultiplierValue));
        points.add(new LatLng(point.getLatitude()- mPerimeterMultiplierValue, point.getLongitude() + mPerimeterMultiplierValue));
        points.add(new LatLng(point.getLatitude()+ mPerimeterMultiplierValue, point.getLongitude() + mPerimeterMultiplierValue));

		mPolygonOptions = new PolygonOptions();
		mPolygonOptions.addAll(points);
		mPolygonOptions.fillColor(getResources().getColor(R.color.region_background));
        mPolygonOptions.alpha(0.4f);
		mOfflinePolygon =  mapboxMap.addPolygon(mPolygonOptions);
		;
	}


    private void updatePolygonView(double progress) {
           progress = progress/100 * 0.3;
            mPerimeterMultiplierValue = 0.08 + progress;
            List<LatLng> points = new ArrayList<>();
            points.add(new LatLng(mPoint.getLatitude() + mPerimeterMultiplierValue, mPoint.getLongitude() - mPerimeterMultiplierValue));
            points.add(new LatLng(mPoint.getLatitude() - mPerimeterMultiplierValue, mPoint.getLongitude() - mPerimeterMultiplierValue));
            points.add(new LatLng(mPoint.getLatitude() - mPerimeterMultiplierValue, mPoint.getLongitude() + mPerimeterMultiplierValue));
            points.add(new LatLng(mPoint.getLatitude() + mPerimeterMultiplierValue, mPoint.getLongitude() + mPerimeterMultiplierValue));
           if (mOfflinePolygon != null) {
               mapboxMap.removePolygon(mOfflinePolygon);
           }
            mPolygonOptions = null;
            mPolygonOptions = new PolygonOptions();
            mPolygonOptions.addAll(points);
            mPolygonOptions.fillColor(getResources().getColor(R.color.region_background));
            mPolygonOptions.alpha(0.4f);
            mOfflinePolygon = mapboxMap.addPolygon(mPolygonOptions);
            mapboxMap.moveCamera(CameraUpdateFactory.zoomTo(10 - (progress * 5)));
    }


    private void showCreateDialog(final int typeObject) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        if (typeObject == PLACE){
            builder.setTitle(getString(R.string.dialog_new_place));
            builder.setMessage(getString(R.string.dialog_new_places_message));
        }else if (typeObject == ROUT){
            builder.setTitle(getString(R.string.dialog_new_rout));
            builder.setMessage(getString(R.string.dialog_new_rout_message));
        }
        builder.setPositiveButton(getString(R.string.gps_mode), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActionModeGPS(typeObject);
                if (typeObject == ROUT){
                    createdTrackPosition = new ArrayList<>();
                }
                autoOrientationOff(true);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.hands_mode), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mChecker){
                    enabledGPS(false);
                }
                startActionModeHand(typeObject);
                autoOrientationOff(true);
                if (typeObject == ROUT){
                    createdTrackPosition = new ArrayList<>();
                }
                enabledHandsMode(typeObject);
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void startActionModeGPS(int typeObject) {
      mActionMode = this.startSupportActionMode(
              new GPSActionModeCallback(this, typeObject));
      if (mCaptureServiceConnection == null && !mChecker){
          checkGPSEnabled();
      }
        if (typeObject == PLACE){
            Intent serviceIntent = new Intent(MapsActivity.this, LocationService.class);
            serviceIntent.putExtra(TO_SERVICE_COMMANDS, COMMAND_REC_PLACE);
            MapsActivity.this.startService(serviceIntent);
            autoOrientationOff(true);
            mCheckForRecButton = false;
            mActionMode.setTitle(getString(R.string.action_mode_title_gps_connection));
            mActionMode.setSubtitle(R.string.action_mode_subtitle_gps_connection);

        }else if (typeObject == ROUT){
                autoOrientationOff(true);
                mCheckForRecButton = true;
                mActionMode.setTitle(getString(R.string.action_mode_title_gps_connection));
                mActionMode.setSubtitle(R.string.action_mode_subtitle_gps_connection);
        }
    }

    private void startActionModeHand(int typeObject) {
       mActionMode = this.startSupportActionMode(new HandActionModeCallback(this, typeObject));
       if (typeObject == PLACE) {
           mActionMode.setTitle(getString(R.string.dialog_new_place));
           mActionMode.setSubtitle(getString(R.string.subtitle_new_place));
       }else if(typeObject == ROUT){
           mActionMode.setTitle(getString(R.string.action_mode_title_new_rout));
           mActionMode.setSubtitle(getString(R.string.subtitle_new_rout));
       }

    }

    private void changeHandModeItemForPlace(int type) {
        if (type == PLACE) {
            MenuItem saveItem = mActionMode.getMenu().findItem(R.id.action_save);
            saveItem.setVisible(true);
            saveItem.setEnabled(true);
            MenuItem delItem = mActionMode.getMenu().findItem(R.id.action_del);
            delItem.setVisible(true);
            delItem.setEnabled(true);
            mActionMode.setSubtitle(null);
        }else if (type == ROUT){
            MenuItem saveItem = mActionMode.getMenu().findItem(R.id.action_save);
            saveItem.setVisible(true);
            saveItem.setEnabled(true);
            MenuItem delItem = mActionMode.getMenu().findItem(R.id.action_del);
            delItem.setVisible(true);
            delItem.setEnabled(true);
            MenuItem undoAction = mActionMode.getMenu().findItem(R.id.action_beck);
            undoAction.setVisible(true);
            undoAction.setEnabled(true);
            mActionMode.setSubtitle(null);
        }
    }



    @Override
    public void onMapReady(final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mPoint = new LatLng();
        mapboxMap.getUiSettings().setCompassGravity(Gravity.BOTTOM);
        mapboxMap.getUiSettings().setCompassMargins(20,20,20,20);

        if (selectUserRouts != null && selectUserRouts.size() > 0 && mRootPathString != null) {
            Uri rootPathForRoutsString = Uri.parse(mRootPathString).buildUpon()
                    .appendPath(ROUT_STR).build();
            for (int i = 0; i < selectUserRouts.size(); i++) {
                String mUriString = rootPathForRoutsString.buildUpon()
                        .appendPath(selectUserRouts.get(i)).build().getPath();
                if (mUriString != null) {
                    new DrawGeoJson(mUriString).execute();
                }
            }
        }
        if (selectUserPlacesList != null && selectUserPlacesList.size() > 0) {
            for (int i = 0; i < selectUserPlacesList.size(); i++) {
                mPointPlace = new LatLng();
                mPointPlace.setLatitude(selectUserPlacesList.get(i).getPositionPlace().getLatitude());
                mPointPlace.setLongitude(selectUserPlacesList.get(i).getPositionPlace().getLongitude());
                if (mPointPlace != null) {
                    IconFactory iconFactory = IconFactory.getInstance(MapsActivity.this);
                    Icon icon = iconFactory.fromResource(R.drawable.marcer);
                    mapboxMap.addMarker(new MarkerViewOptions().icon(icon).position(mPointPlace)
                            .title(selectUserPlacesList.get(i).getNamePlace()));

                }
            }
        }
        if (mOfflineRegionName != null){
            mOfflineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
                @Override
                public void onList(final OfflineRegion[] offlineRegions) {
                    // Check result. If no regions have been
                    // downloaded yet, notify user and return
                    if (offlineRegions == null || offlineRegions.length == 0) {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.toast_no_regions_yet), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Add all of the region names to a list

                    for (OfflineRegion offlineRegion : offlineRegions) {
                        if (mOfflineRegionName.equals(getRegionName(offlineRegion))){
                            // Get the region bounds and zoom
                            LatLngBounds bounds = offlineRegion.getDefinition().getBounds();
                            double regionZoom = ((OfflineTilePyramidRegionDefinition)
                                    offlineRegion.getDefinition()).getMinZoom();

                            // Create new camera position
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(bounds.getCenter())
                                    .zoom(regionZoom)
                                    .build();

                            enabledPerimeterOfflineRegion(bounds);

                            // Move camera to new position
                            mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                            Toast.makeText(MapsActivity.this,
                                    mOfflineRegionName, Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error: " + error);
                }
            });
        }
        if (mTypeMode ) {
            mPoint.setLatitude(24.141080 );
            mPoint.setLongitude(48.635022);
        }else if(mPointPlace != null && selectUserRouts != null && selectUserRouts.size() == 0 ) {
          mPoint = mPointPlace;
        }
        mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(mPoint)  // set the camera's center position
                        .zoom(10)  // set the camera's zoom level
                        .tilt(20)  // set the camera's tilt
                        .build()));

    }

    // This method show download dialog
    private void downloadRegionDialog(final double perimeterValue ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);

        final EditText regionNameEdit = new EditText(MapsActivity.this);
        regionNameEdit.setHint(getString(R.string.set_region_name_hint));

        // Build the dialog box
        builder.setTitle(getString(R.string.dialog_title))
                .setView(regionNameEdit)
                .setMessage(getString(R.string.dialog_message))
                .setPositiveButton(getString(R.string.dialog_positive_button),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String regionName = regionNameEdit.getText().toString();
                        // Require a region name to begin the download.
                        // If the user-provided string is empty, display
                        // a toast message and do not begin download.
                        if (regionName.length() == 0) {
                            Toast.makeText(MapsActivity.this,
                                    getString(R.string.dialog_toast), Toast.LENGTH_SHORT).show();
                        } else {
                            // Begin download process
                            downloadOfflineRegion(regionName, perimeterValue);
                            dialog.dismiss();
                            if (mActionMode != null){
                                mActionMode.finish();
                            }
                        }
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_button),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (mActionMode != null){
                            mActionMode.finish();
                        }
                    }
                });

        // Display the dialog
        builder.show();
    }


    //
    private void downloadedRegionList() {
        // Build a region list when the user clicks the list button

        // Reset the region selected int to 0
        mRegionSelected = 0;

        // Query the DB asynchronously
        mOfflineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(final OfflineRegion[] offlineRegions) {
                // Check result. If no regions have been
                // downloaded yet, notify user and return
                if (offlineRegions == null || offlineRegions.length == 0) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.toast_no_regions_yet), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add all of the region names to a list
                ArrayList<String> offlineRegionsNames = new ArrayList<>();
                for (OfflineRegion offlineRegion : offlineRegions) {
                    offlineRegionsNames.add(getRegionName(offlineRegion));
                }
                final CharSequence[] items = offlineRegionsNames
                        .toArray(new CharSequence[offlineRegionsNames.size()]);

                // Build a dialog containing the list of regions
                AlertDialog dialog = new AlertDialog.Builder(MapsActivity.this)
                        .setTitle(getString(R.string.navigate_title))
                        .setSingleChoiceItems(items, 0,
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Track which region the user selects
                                mRegionSelected = which;
                            }
                        })
                        .setPositiveButton(getString(R.string.navigate_positive_button),
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                Toast.makeText(MapsActivity.this,
                                        items[mRegionSelected], Toast.LENGTH_LONG).show();

                                // Get the region bounds and zoom
                                LatLngBounds bounds = ((OfflineTilePyramidRegionDefinition)
                                        offlineRegions[mRegionSelected].getDefinition()).getBounds();
                                double regionZoom = ((OfflineTilePyramidRegionDefinition)
                                        offlineRegions[mRegionSelected].getDefinition()).getMinZoom();

                                // Create new camera position
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(bounds.getCenter())
                                        .zoom(regionZoom)
                                        .build();

                                enabledPerimeterOfflineRegion(bounds);

                                // Move camera to new position
                                mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                            }
                        })
                        .setNeutralButton(getString(R.string.navigate_neutral_button_title),
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // Make mProgressBar indeterminate and
                                // set it to visible to signal that
                                // the deletion process has begun
                                mProgressBar.setIndeterminate(true);
                                mProgressBar.setVisibility(View.VISIBLE);

                                // Begin the deletion process
                                offlineRegions[mRegionSelected]
                                        .delete(new OfflineRegion.OfflineRegionDeleteCallback() {
                                    @Override
                                    public void onDelete() {
                                        // Once the region is deleted, remove the
                                        // mProgressBar and display a toast
                                        mProgressBar.setVisibility(View.INVISIBLE);
                                        mProgressBar.setIndeterminate(false);
                                        Toast.makeText(getApplicationContext(),
                                                getString(R.string.toast_region_deleted),
                                                Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onError(String error) {
                                        mProgressBar.setVisibility(View.INVISIBLE);
                                        mProgressBar.setIndeterminate(false);
                                        Log.e(TAG, "Error: " + error);
                                    }
                                });
                            }
                        })
                        .setNegativeButton(getString(R.string.navigate_negative_button_title),
                                new DialogInterface.OnClickListener() {
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

    private void enabledPerimeterOfflineRegion(LatLngBounds bounds) {
        if (bounds != null) {
            List<LatLng> points = new ArrayList<>();


            points.add(bounds.getNorthEast());
            points.add(bounds.getSouthEast());
            points.add(bounds.getSouthWest());
            points.add(bounds.getNorthWest());

            mPolygonOptions = new PolygonOptions();
            mPolygonOptions.addAll(points);
            mPolygonOptions.fillColor(getResources().getColor(R.color.polygon_background));
            mPolygonOptions.alpha(0.4f);
            mOfflinePolygon = mapboxMap.addPolygon(mPolygonOptions);
        }else {
            if(mOfflinePolygon != null && mPolygonOptions != null) {
                mPolygonOptions = null;
                mapboxMap.removePolygon(mOfflinePolygon);
            }
        }
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

    // This method creates and loads the offline region visible on the screen
    private void downloadOfflineRegion(final String regionName , double perimeterValue ) {
        // Create a bounding box for the offline region
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(new LatLng(mPoint.getLatitude() + perimeterValue,
                        mPoint.getLongitude() + perimeterValue)) // Northeast
                .include(new LatLng(mPoint.getLatitude() - perimeterValue,
                        mPoint.getLongitude() - perimeterValue)) // Southwest
                .build();
        // Define the offline region
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                mapboxMap.getStyleUrl(),
                latLngBounds,
                9,
                14,
                this.getResources().getDisplayMetrics().density);

        // Set the metadata
        byte[] metadata;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JSON_FIELD_REGION_NAME, regionName);
            String json = jsonObject.toString();
            metadata = json.getBytes(JSON_CHARSET);
        } catch (Exception exception) {
            Log.e(TAG, "Failed to encode metadata: " + exception.getMessage());
            metadata = null;
        }

        // Create the region asynchronously
        mOfflineManager.createOfflineRegion(
                definition,
                metadata,
                new OfflineManager.CreateOfflineRegionCallback() {
                    @Override
                    public void onCreate(OfflineRegion offlineRegion) {
                        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);

                        // Display the download progress bar
                        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
                        startProgress();

                        // Monitor the download progress using setObserver
                        offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
                            @Override
                            public void onStatusChanged(OfflineRegionStatus status) {

                                // Calculate the download percentage and update the progress bar
                                double percentage = status.getRequiredResourceCount() >= 0
                                        ? (100.0 * status.getCompletedResourceCount()
                                        / status.getRequiredResourceCount()) :
                                        0.0;

                                if (status.isComplete()) {
                                    // Download complete
                                    endProgress("Region downloaded successfully.");
                                } else if (status.isRequiredResourceCountPrecise()) {
                                    // Switch to determinate state
                                    setPercentage((int) Math.round(percentage));
                                }
                            }

                            @Override
                            public void onError(OfflineRegionError error) {
                                // If an error occurs, print to logcat
                                Log.e(TAG, "onError reason: " + error.getReason());
                                Log.e(TAG, "onError message: " + error.getMessage());
                            }

                            @Override
                            public void mapboxTileCountLimitExceeded(long limit) {
                                // Notify if offline region exceeds maximum tile count
                                Log.e(TAG, "Mapbox tile count limit exceeded: " + limit);
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error: " + error);
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if (mLocationService != null) {
            mLocationService.setOwner(iCapture);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        if (mLocationService != null) {
            mLocationService.setOwner(null);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        // Ensure no memory leak occurs if we register the location listener but the call hasn't
        // been made yet.

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
        outState.putBoolean(CHECK_FOR_REC_BUTTON, mCheckForRecButton);
    }

    // This method monitors the position of the user on the map
    private void toggleGps(boolean checker) {
        if (checker) {
            mCaptureServiceConnection = new ServiceConnection() {

                public void onServiceConnected(ComponentName className, IBinder service) {
                    LocationService.MyLocalBinder binder = (LocationService.MyLocalBinder) service;
                    mLocationService = binder.getService();
                    mLocationService.setOwner(iCapture);
                }

                public void onServiceDisconnected(ComponentName arg0) {
                }
            };
            myLocationChangeListener= new MapboxMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(@Nullable Location location) {

                }
            };
            Intent intent = new Intent(this, LocationService.class);
            startService(intent);
            bindService(intent, mCaptureServiceConnection, Context.BIND_AUTO_CREATE);
            mapboxMap.getMyLocationViewSettings().setPadding(0, 200, 0, 0);
            mapboxMap.getMyLocationViewSettings().setForegroundTintColor(
                    getResources().getColor(R.color.my_location_tint_color));
            mapboxMap.getMyLocationViewSettings().setAccuracyTintColor(
                    getResources().getColor(R.color.my_location_accuracy_tint_color));
            mapboxMap.setOnMyLocationChangeListener(myLocationChangeListener);
            mapboxMap.setMyLocationEnabled(true);
        }else{
            if (mCaptureServiceConnection != null ){
                Intent intent = new Intent(this, LocationService.class);
                unbindService(mCaptureServiceConnection);
                MapsActivity.this.stopService(intent);
                mapboxMap.setMyLocationEnabled(false);
                mCaptureServiceConnection = null;
            }
        }
    }


    // Progress bar methods
    private void startProgress() {

        // Start and show the progress bar
        isEndNotified = false;
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void setPercentage(final int percentage) {
        mProgressBar.setIndeterminate(false);
        mProgressBar.setProgress(percentage);
    }

    private void endProgress(final String message) {
        // Don't notify more than once
        if (isEndNotified) {
            return;
        }

        // Stop and hide the progress bar
        isEndNotified = true;
        mProgressBar.setIndeterminate(false);
        mProgressBar.setVisibility(View.GONE);

        // Show a toast
        Toast.makeText(MapsActivity.this, message, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 69 && grantResults.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(ACCESS_FINE_LOCATION) && grantResults[i]
                        == PackageManager.PERMISSION_GRANTED) {
                    enabledGPS(true);
                }
            }

        }
    }

    @Override
    public void undoAction() {
        undo();
        if (createdTrackPosition != null && createdTrackPosition.size() == 0){
            MenuItem saveItem = mActionMode.getMenu().findItem(R.id.action_save);
            saveItem.setVisible(false);
            saveItem.setEnabled(false);
            MenuItem delItem = mActionMode.getMenu().findItem(R.id.action_del);
            delItem.setVisible(false);
            delItem.setEnabled(false);
            MenuItem undoAction = mActionMode.getMenu().findItem(R.id.action_beck);
            undoAction.setVisible(false);
            undoAction.setEnabled(false);
            mActionMode.setTitle(getString(R.string.title_macke_chois));
        }
    }

    @Override
    public void saveAction() {
        mapboxMap.setOnMapClickListener(null);
        if (mMarker != null){
            showCreateNameDialog(PLACE, null);
        }else if (createdTrackPosition.size() > 2){
            showCreateNameDialog(ROUT, null);
        }else{
            removeViews(null);
        }
    }

    @Override
    public void deleteActionForHand() {
        MenuItem saveItem = mActionMode.getMenu().findItem(R.id.action_save);
        saveItem.setVisible(false);
        saveItem.setEnabled(false);
        MenuItem delItem = mActionMode.getMenu().findItem(R.id.action_del);
        delItem.setVisible(false);
        delItem.setEnabled(false);
        MenuItem undoAction = mActionMode.getMenu().findItem(R.id.action_beck);
        undoAction.setVisible(false);
        undoAction.setEnabled(false);
        mActionMode.setTitle(getString(R.string.title_macke_chois));
        removeAll();
    }


    /**
     *
     */
    private class DrawGeoJson extends AsyncTask<Void, Void, List<LatLng>> {
        String mNameFileFromURI;

        private DrawGeoJson(String uri) {
            this.mNameFileFromURI = uri;
        }

        @Override
        protected List<LatLng> doInBackground(Void... voids) {

            ArrayList<LatLng> points = new ArrayList<>();
            try {
                // Load GeoJSON file
                File file = new File(mNameFileFromURI);
                InputStream fileInputStream = new FileInputStream(file);
                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(fileInputStream, Charset.forName("UTF-8")));
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
                        JSONArray coordinates = geometry.getJSONArray("coordinates");
                        for (int lc = 0; lc < coordinates.length(); lc++) {
                            JSONArray coordinate = coordinates.getJSONArray(lc);
                            LatLng latLng = new LatLng(coordinate.getDouble(1), coordinate.getDouble(0));
                            points.add(latLng);
                        }
                    }
                }
            } catch (Exception exception) {
                Log.e(TAG, "Exception Loading GeoJSON: " + exception.toString());
            }

            return points;
        }

        @Override
        protected void onPostExecute(List<LatLng> points) {
            super.onPostExecute(points);
            String[] mColors = {
                    "#39add1", // light blue
                    "#3079ab", // dark blue
                    "#c25975", // mauve
                    "#e15258", // red
                    "#f9845b", // orange
                    "#838cc7", // lavender
                    "#7d669e", // purple
                    "#53bbb4", // aqua
                    "#51b46d", // green
                    "#e0ab18", // mustard
                    "#637a91", // dark gray
                    "#f092b0", // pink
                    "#b7c0c7"  // light gray
            };
            Random randomGenerator = new Random(); // Construct a new Random number generator
            int randomNumber = randomGenerator.nextInt(mColors.length);

            if (points.size() > 0) {
                mPoint = points.get(points.size()/2);
                // Draw polyline on map
                mapboxMap.addPolyline(new PolylineOptions()
                        .addAll(points)
                        .color(Color.parseColor(mColors[randomNumber]))
                        .width(2));
                IconFactory iconFactory = IconFactory.getInstance(MapsActivity.this);
                Icon iconStart = iconFactory.fromResource(R.drawable.marcer_flag_start);
                Icon iconFinish = iconFactory.fromResource(R.drawable.marcer_flag_finish);
                mapboxMap.addMarker(new MarkerViewOptions().icon(iconStart).position(points.get(0)).title("Початок"));
                mapboxMap.addMarker(new MarkerViewOptions().icon(iconFinish).position(points.get(points.size() - 1)).title("Кінець"));
                mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder()
                                .target(new LatLng(points.get(0).getLatitude(), points.get(0).getLongitude()))  // set the camera's center position
                                .zoom(12)  // set the camera's zoom level
                                .tilt(20)  // set the camera's tilt
                                .build()));
            }
        }
    }


    @UiThread
    public void showCreateNameDialog(final int model, String text) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        final EditText nameInput = new EditText(this);
        builder.setView(nameInput);

        nameInput.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        if (text != null) {
            builder.setTitle("Error!");
            builder.setMessage("Вибачте але це імя вже використовується");
            nameInput.setText(text);
        }else if(model == ROUT && text == null){
            builder.setTitle("Save new rout!");
            builder.setMessage("Введіть назву вашого маршруту");
        }else if(model == PLACE && text == null){
            builder.setTitle("Save new place!");
            builder.setMessage("Введіть назву вашого місця");
            mCheckForRecButton = true;
        }
        builder.setPositiveButton("Зберегти",null);
        builder.setNegativeButton("Не зберігати", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if ((!mChecker && mMarker != null) || createdTrackPosition != null ){
                   removeViews("canceled by user");
                    mapboxMap.setOnMapClickListener(null);
                    mActionMode.finish();
                }else {
                    Intent serviceIntent = new Intent(MapsActivity.this, LocationService.class);
                    serviceIntent.putExtra(TO_SERVICE_COMMANDS, COMMAND_NO_SAVE);
                    MapsActivity.this.startService(serviceIntent);
                    removeViews("canceled by user");
                    autoOrientationOff(false);
                    removeViews("canceled by user");
                    mActionMode.finish();

                }
            }
        });
        alert = builder.create();
        alert.show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameInput.getText().toString().equals("") ){
                    nameInput.setError("Enter name");
                }else {
                    if ((!mChecker && mMarker != null) || (createdTrackPosition != null && createdTrackPosition.size() > 2)) {
                        saveCreatedObject(model, nameInput.getText().toString());
                        mPointCounter = 0;
                        mapboxMap.setOnMapClickListener(null);
                        alert.dismiss();
                        mActionMode.finish();

                    } else {
                        Intent serviceIntent = new Intent(MapsActivity.this, LocationService.class);
                        serviceIntent.putExtra(TO_SERVICE_TRACK_NAME, nameInput.getText().toString());
                        serviceIntent.putExtra(TO_SERVICE_COMMANDS, model);
                        MapsActivity.this.startService(serviceIntent);
                        autoOrientationOff(false);
                        alert.dismiss();
                        mActionMode.finish();
                    }
                }
            }
        });
    }
	@Background
    public void saveCreatedObject(int model, String name) {
        if (model == PLACE && mMarker != null){
            Place mPlace = new Place();
            mPlace.setNamePlace(name);
            mPlace.setPositionPlace(new com.example.key.my_carpathians.models.Position(
                    mMarker.getPosition().getLatitude(), mMarker.getPosition().getLongitude()));
            ObjectService objectService = new ObjectService(MapsActivity.this, mRootPathString);
            String outcome = objectService.savePlace(name, mPlace, false);
           if (outcome.equals(FILE_EXISTS)){
               showCreateNameDialog(0, name);
            }else{
               removeViews(outcome);
           }
        }else if(model == ROUT) {
            if (isOnline()) {
                AltitudeFinder altitudeFinder = new AltitudeFinder();
                List<com.mapbox.services.commons.models.Position> pos = new ArrayList<>();
                for (int i = 0; i < createdTrackPosition.size(); i++) {
                    pos.add(com.mapbox.services.commons.models.Position.fromCoordinates(
                            createdTrackPosition.get(i).getLatitude(),
                            createdTrackPosition.get(i).getLongitude()));
                }
                createdTrackPosition = altitudeFinder.extractAltitude(pos);
            }
            ObjectService objectService = new ObjectService(MapsActivity.this, mRootPathString);
            Rout mRout = new Rout();
            mRout.setNameRout(name);
            mRout.setPositionRout(new com.example.key.my_carpathians.models.Position(createdTrackPosition.get(0).getLatitude(),
                    createdTrackPosition.get(0).getLongitude()));
            String outcome = objectService.saveRout(name, createdTrackPosition, mRout, false);
            if (outcome.equals(FILE_EXISTS)){
                showCreateNameDialog(0, name);
            }else{
                removeViews(outcome);
            }

        }
    }


    @UiThread
    public void removeViews(String message){
        Toast.makeText(MapsActivity.this, message, Toast.LENGTH_LONG).show();
        if (mMarker != null){
            mMarker.remove();
            mMarker = null;
        }
        if (createdTrackPosition != null){
            int countsPolyLinesInMap = mapboxMap.getPolylines().size();
            for(int i = 0; i < countsPolyLinesInMap; i++){
                mapboxMap.getPolylines().get(0).remove();
            }
            int countsMarkersInMap = mapboxMap.getMarkers().size();
            for (int i = 0; i < countsMarkersInMap;i++){
                mapboxMap.getMarkers().get(0).remove();
            }
            createdTrackPosition.clear();
            createdTrackPosition = null;
            mPointCounter = 0;
        }

    }

	public boolean isOnline() {
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) MapsActivity.this
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
			isConnected = networkInfo != null && networkInfo.isAvailable() &&
					networkInfo.isConnected();
			return isConnected;


		} catch (Exception e) {
			System.out.println("CheckConnectivity Exception: " + e.getMessage());
			Log.v("connectivity", e.toString());
		}
		return isConnected;
	}

//Todo need update this method
	private void enabledRecAnimation(boolean b){
        isStartedAnimationChecker = b;
        startAnimation();
    }




    @UiThread
    public void flashingColorAnimation() {
        if (isStartedAnimationChecker && isFlash){
            MenuItem recInd = mActionMode.getMenu().findItem(R.id.recIndicator);
            recInd.setVisible(true);
            recInd.getIcon().setAlpha(100);
            isFlash = false;
            startAnimation();
        }else if ((isStartedAnimationChecker && !isFlash)){
            MenuItem recInd = mActionMode.getMenu().findItem(R.id.recIndicator);
            recInd.setVisible(true);
            recInd.getIcon().setAlpha(0);
            isFlash = true;
            startAnimation();
        }else {
            MenuItem recInd = mActionMode.getMenu().findItem(R.id.recIndicator);
            recInd.setVisible(false);
            isFlash = false;
        }
 
    }
    @Background( delay = 1000)
    public void startAnimation() {
        flashingColorAnimation();
    }

    /**
     * This method turns off rotation of the device while recording a track, and turns on after rec.
     */
    @Override
    public void autoOrientationOff(boolean yes) {
        if (yes) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        }else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }

    @Override
    public void actionDelRegion() {
        removeAll();
        seekBar.setOnSeekBarChangeListener(null);
        seekBar.setVisibility(View.INVISIBLE);
        mActionMode.getMenu().findItem(R.id.actactionDellRegion).setVisible(false);
        mActionMode.getMenu().findItem(R.id.actionDownload).setVisible(false);
        mActionMode.setSubtitle(getString(R.string.subtitle_tab_to_select));

        enabledHandsMode(REGION);
    }

    @Override
    public void actionStartRecTrack() {
        if (mCheckForRecButton) {
            Intent serviceIntent = new Intent(MapsActivity.this, LocationService.class);
            serviceIntent.putExtra(TO_SERVICE_COMMANDS, COMMAND_REC_ROUT);
            MapsActivity.this.startService(serviceIntent);
            autoOrientationOff(true);
            mCheckForRecButton = false;
            enabledRecAnimation(true);
            mActionMode.setSubtitle(getString(R.string.rec));
            mActionMode.getMenu().findItem(R.id.actionSaveRecord).setVisible(false);
            if ( mActionMode.getMenu().findItem(R.id.actionStop).isVisible()) {
                mActionMode.getMenu().findItem(R.id.actionStop).setIcon(R.drawable.ic_media_stop_dark);
            }
            if (createdTrackPosition == null) {
                createdTrackPosition = new ArrayList<>();
            }

            mActionMode.getMenu().findItem(R.id.actionStartRec).setIcon(android.R.drawable.ic_media_pause);
        }else {
            Intent serviceIntent = new Intent(MapsActivity.this, LocationService.class);
            serviceIntent.putExtra(TO_SERVICE_COMMANDS, COMMAND_PAUSE_REC_ROUT);
            MapsActivity.this.startService(serviceIntent);
            autoOrientationOff(true);
            enabledRecAnimation(false);
            mActionMode.setSubtitle(getString(R.string.pause));
            mActionMode.getMenu().findItem(R.id.actionStartRec).setIcon(android.R.drawable.ic_media_play);
            if ( mActionMode.getMenu().findItem(R.id.actionStop).isVisible()) {
                mActionMode.getMenu().findItem(R.id.actionStop).setIcon(R.drawable.ic_done_white_24px);
            }
            mCheckForRecButton = true;
        }

    }

    @Override
    public void actionStopRecTrack() {
        if (!mCheckForRecButton) {
            Intent serviceIntent = new Intent(MapsActivity.this, LocationService.class);
            serviceIntent.putExtra(TO_SERVICE_COMMANDS, COMMAND_PAUSE_REC_ROUT);
            MapsActivity.this.startService(serviceIntent);
            autoOrientationOff(true);
            enabledRecAnimation(false);
            mActionMode.setSubtitle(getString(R.string.finish));
            mActionMode.getMenu().findItem(R.id.actionStartRec).setIcon(android.R.drawable.ic_media_play);
            mActionMode.getMenu().findItem(R.id.actionStartRec).setVisible(false);
            mActionMode.getMenu().findItem(R.id.actionStop).setVisible(true);
            mActionMode.getMenu().findItem(R.id.actionStop).setIcon(R.drawable.ic_done_white_24px);
            mCheckForRecButton = true;
        }else {
            showCreateNameDialog(ROUT, null);
        }

    }

    @Override
    public void actionDownloadRegion() {
        downloadRegionDialog(mPerimeterMultiplierValue);

    }


    @Override
    public void actionSaveLocation() {
        showCreateNameDialog(PLACE, null);
    }

    @Override
    public void enabledProgressGPS(boolean b, int type) {
        startTimeOutChecker();

        if (b) {
            customProgressBarGPS.setVisibility(View.VISIBLE);
            gpsLoading.start();
        }else {
            customProgressBarGPS.setVisibility(View.GONE);
            gpsLoading.stop();
            if (type == PLACE && mActionMode != null ) {

                visibleActionsIconForRecPlace(true);
                mActionMode.setTitle("New Place");
                mActionMode.setSubtitle("location defined");
            }else if(mActionMode != null ) {
                visibleActionsIconForRecRout(START);
                mActionMode.setTitle("New Rout");
                mActionMode.setSubtitle("please start rec");
            }
        }

    }
    @Background(delay = 30000)
    public void startTimeOutChecker() {
        showMessageAboutProblemWithGPSSignal();
    }
    @UiThread
    public void showMessageAboutProblemWithGPSSignal(){
            if ( customProgressBarGPS.getVisibility() == View.VISIBLE){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("GPS signal");
                builder.setMessage("Please, make sure you are in the open air and try again");
                builder.setPositiveButton("wait", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        enabledGPS(false);
                        mActionMode.finish();
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
    }



    private void visibleActionsIconForRecRout(int action) {
        if(mActionMode != null) {
            switch (action) {
                case START: {
                    MenuItem itemPlay = mActionMode.getMenu().findItem(R.id.actionSaveRecord);
                    MenuItem itemStop = mActionMode.getMenu().findItem(R.id.actionStop);
                    MenuItem itemRec = mActionMode.getMenu().findItem(R.id.actionStartRec);
                    itemPlay.setVisible(false);
                    itemRec.setVisible(true);
                    itemStop.setVisible(false);
                    itemPlay.setEnabled(false);
                    itemRec.setEnabled(true);
                    itemStop.setEnabled(false);
                    break;
                }
                case REC: {
                    MenuItem itemStop = mActionMode.getMenu().findItem(R.id.actionStop);
                    itemStop.setVisible(true);
                    itemStop.setEnabled(true);
                    break;
                }

            }
        }
    }

    private void visibleActionsIconForRecPlace(boolean b) {
        if(mActionMode != null) {
            MenuItem itemSave = mActionMode.getMenu().findItem(R.id.actionSaveRecord);
            MenuItem itemRefresh = mActionMode.getMenu().findItem(R.id.actionStop);
            itemRefresh.setVisible(b);
            itemSave.setVisible(b);
            itemRefresh.setEnabled(b);
            itemSave.setEnabled(b);
        }

    }

    @Override
    public void deleteActionForGPS() {
        Intent serviceIntent = new Intent(MapsActivity.this, LocationService.class);
        serviceIntent.putExtra(TO_SERVICE_COMMANDS, COMMAND_NO_SAVE);
        MapsActivity.this.startService(serviceIntent);
        removeViews("canceled by user");
        autoOrientationOff(false);
        removeViews("canceled by user");
        removeAll();

    }

    @Override
    public void actionRefreshLocation() {
        removeAll();
        visibleActionsIconForRecPlace(false);
        enabledProgressGPS(true, PLACE);
        Intent serviceIntent = new Intent(MapsActivity.this, LocationService.class);
        serviceIntent.putExtra(TO_SERVICE_COMMANDS, COMMAND_REC_PLACE);
        MapsActivity.this.startService(serviceIntent);
        autoOrientationOff(true);
        mCheckForRecButton = false;
        createdTrackPosition = new ArrayList<>();
    }

    @Override
    public void deleteActionOfflineRegion() {
        removeAll();
        seekBar.setOnSeekBarChangeListener(null);
        seekBar.setVisibility(View.INVISIBLE);
    }

    private void showRecLine(LatLng latLng, int typeObject) {
            if (typeObject == PLACE | typeObject == REGION) {
                if (mMarker == null) {
                    mMarker = mapboxMap.addMarker(new MarkerViewOptions()
                            .position(latLng)
                            .title("Intervention")
                            .snippet("Desc inter"));
                } else {
                    mMarker.remove();
                    mMarker = mapboxMap.addMarker(new MarkerViewOptions()
                            .position(latLng)
                            .title("Intervention")
                            .snippet("Desc inter"));
                }
                if( typeObject == REGION){
                showRegion(latLng);
                }

            } else if (typeObject == ROUT) {

                Position mPos = new Position(latLng.getLatitude(), latLng.getLongitude(), 0);
                createdTrackPosition.add(mPos);
                if (createdTrackPosition.size() == 1) {
                    IconFactory iconFactory = IconFactory.getInstance(MapsActivity.this);
                    Icon iconStart = iconFactory.fromResource(R.drawable.marcer_flag_start);
                    startMarker = mapboxMap.addMarker(new MarkerViewOptions().icon(iconStart).position(latLng)
                            .title("Початок"));
                } else if (createdTrackPosition.size() > 1) {
                    LatLng p1 = new LatLng(createdTrackPosition.get(createdTrackPosition.size() - 1).getLatitude(), createdTrackPosition.get(createdTrackPosition.size() - 1).getLongitude());
                    LatLng p2 = new LatLng(createdTrackPosition.get(createdTrackPosition.size() - 2).getLatitude(), createdTrackPosition.get(createdTrackPosition.size() - 2).getLongitude());


                    mapboxMap.addPolyline(new PolylineOptions().add(p1, p2).width(2));
                    mapboxMap.addMarker(new MarkerOptions().setPosition(latLng));


                if (createdTrackPosition.size() > 0 && !mCheckForRecButton) {
                    visibleActionsIconForRecRout(REC);
                }
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCheckForRecButton = savedInstanceState.getBoolean(CHECK_FOR_REC_BUTTON);
    }

    public void enabledHandsMode(final int type){
        MapboxMap.OnMapClickListener fingerTouchListener = new MapboxMap.OnMapClickListener() {
                @Override
                public void onMapClick(@NonNull LatLng point) {
                    changeHandModeItemForPlace(type);
                    mPointCounter++;
                    if (type == PLACE) {
                        showRecLine(point, PLACE);

                    }else if(type == ROUT){
                        showRecLine(point, ROUT);
                    }else if(type == REGION){
                        showRecLine(point, REGION);
                    }

                }
            };

            mapboxMap.setOnMapClickListener(fingerTouchListener);
    }



    void undo(){
        if (mapboxMap.getPolylines().size() > 0) {
               mapboxMap.getPolylines().get(mPointCounter -2).remove();
                createdTrackPosition.remove(createdTrackPosition.size() - 1);
            mPointCounter--;
        }
         if (mapboxMap.getMarkers().size() > 1 & mMarker == null){
             mapboxMap.getMarkers().get(mPointCounter).remove();

        }else if (startMarker != null && createdTrackPosition != null
                 && createdTrackPosition.size() > 0){
             startMarker.remove();
             createdTrackPosition.remove(createdTrackPosition.size() - 1);
             mPointCounter = 0;
         }else if(mMarker != null){
	         mMarker.remove();
             mPointCounter = 0;
             mMarker = null;
         }

    }
   public void removeAll() {

       int mCountPolyLines = mapboxMap.getPolylines().size();
       if (mCountPolyLines > 0) {

           for (int i = 0; i < mCountPolyLines; i++) {
               mapboxMap.getPolylines().get(0).remove();
           }
       }
       int mCountMarkers = mapboxMap.getMarkers().size();
       if (mCountMarkers > 0) {
           for (int i = 0; i < mCountMarkers; i++) {
               mapboxMap.getMarkers().get(0).remove();
           }
       }
       mPointCounter = 0;
       if (createdTrackPosition != null) {
           createdTrackPosition.clear();
       }
       if (mOfflinePolygon != null && mapboxMap.getPolygons().size() > 0){
           int  mCountPolygons = mapboxMap.getPolygons().size();
           for (int i = 0; i <  mCountPolygons; i++) {
               mapboxMap.getPolygons().get(0).remove();
           }
            mOfflinePolygon = null;
           mPolygonOptions = null;
       }
   }
}

