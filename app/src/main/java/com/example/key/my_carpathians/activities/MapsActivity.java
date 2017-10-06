package com.example.key.my_carpathians.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cocoahero.android.geojson.Position;
import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.interfaces.ILocation;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;
import com.example.key.my_carpathians.utils.AltitudeFinder;
import com.example.key.my_carpathians.utils.LocationService;
import com.example.key.my_carpathians.utils.ObjectService;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
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
import java.util.List;
import java.util.Random;

import static com.example.key.my_carpathians.activities.ActionActivity.SELECTED_USER_PLACES;
import static com.example.key.my_carpathians.activities.ActionActivity.SELECTED_USER_ROUTS;
import static com.example.key.my_carpathians.activities.StartActivity.PRODUCE_MODE;
import static com.example.key.my_carpathians.adapters.FavoritesRecyclerAdapter.PLACE;
import static com.example.key.my_carpathians.adapters.FavoritesRecyclerAdapter.ROUT;
import static com.example.key.my_carpathians.utils.LocationService.DEFINED_LOCATION;
import static com.example.key.my_carpathians.utils.ObjectService.FILE_EXISTS;
import static com.mapbox.mapboxsdk.storage.FileSource.isExternalStorageReadable;

@EActivity
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final double PERIMETER_SIZE_TO_LATITUDE = 0.3;
    public static final double PERIMETER_SIZE_TO_LONGITUDE = 0.4;
    public static final String TO_SERVICE_COMMANDS = "service_commands";
    public static final int COMMAND_REC_ROUT = 4;
    public static final int COMMAND_REC_PLACE = 5;
    public static final int COMMAND_NO_SAVE = 3;
    public static final String TO_SERVICE_TRACK_NAME = "track_name";
    public static final int ERROR_TRACK = 10;
    public MapsActivity permissionsManager;
    public ILocation iCapture;
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";
    public ArrayList<String> selectUserRouts = null;
    public List<Place> selectUserPlacesList = null;
    public List<Position> createdTrackPosition;
    public Polyline recLine;
    public Marker startMarker;
    public AlertDialog alert;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private static final String TAG = "MapsActivity";
    private boolean isEndNotified;
    private ProgressBar progressBar;
    private OfflineManager offlineManager;
    private int regionSelected;
    private double lng;
    private double lat;
    private boolean switchCheck = false;
    private LocationService locationService;
    boolean checkForRecButton = true;
    private boolean mTypeMode;
    private int mPointCounter = 0;
    private Marker mMarker;
	private boolean connected;
    @ViewById(R.id.buttonRecTrack)
    FloatingActionButton buttonRecTrack;

    @ViewById(R.id.toolsGPSContainer)
    LinearLayout toolsGPSContainer;

    @ViewById(R.id.buttonShowMyLocation)
    FloatingActionButton buttonShowMyLocation;

    @ViewById(R.id.buttonHandEditMode)
    FloatingActionButton buttonHandEditMode;

    @ViewById(R.id.toolsHandContainer)
    LinearLayout toolsHandContainer;

    @ViewById(R.id.buttonOnBack)
    FloatingActionButton buttonTouchCreator;

    private ServiceConnection captureServiceConnection;
    private MapboxMap.OnMyLocationChangeListener myLocationChangeListener;


    @Override
    public void onBackPressed() {
        if (captureServiceConnection != null){
            Intent intent = new Intent(this, LocationService.class);
            unbindService(captureServiceConnection);
            MapsActivity.this.stopService(intent);
            mapboxMap.setMyLocationEnabled(false);
            captureServiceConnection = null;
        }
        super.onBackPressed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iCapture = new ILocation() {
            @Override
            public void update(Location location, int type) {
                if (type == DEFINED_LOCATION) {
                    alert.dismiss();
                    showCreateNameDialog(PLACE, null);
                }
                if(location != null) {
                    myLocationChangeListener.onMyLocationChange(location);
                    mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))  // set the camera's center position
                                    .build()));
                }
                if (!checkForRecButton) {
                    showRecLine(new LatLng(location.getLatitude(), location.getLongitude()));
                }
            }

            @Override
            public void connectionState(int state) {
                if (state == 1) {
                    Toast.makeText(MapsActivity.this, "This device is not supported.", Toast.LENGTH_LONG).show();
                } else {
                    GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
                    apiAvailability.getErrorDialog(MapsActivity.this, state, PLAY_SERVICES_RESOLUTION_REQUEST)
                            .show();
                }
            }

            @Override
            public void messageForActivity(int type, String name) {
                if (type == ERROR_TRACK){
                    showErrorDialog(name);
                }else {
                    showCreateNameDialog(type, name);
                }
            }
        };

        selectUserRouts = getIntent().getStringArrayListExtra(SELECTED_USER_ROUTS);
        selectUserPlacesList = (List<Place>) getIntent().getSerializableExtra(SELECTED_USER_PLACES);
        mTypeMode = getIntent().getBooleanExtra(PRODUCE_MODE, false);
        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_maps);
        offlineManager = OfflineManager.getInstance(MapsActivity.this);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private void showErrorDialog(String name) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Save data");

        // Setting Dialog Message
        alertDialog.setMessage("Ваш трек містить менше трьох локацій. Будь ласка перевірте якість сигналу gps і спробуйте знову");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                MapsActivity.this.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();

    }

    private void startGPS() {

        if (mapboxMap != null && !switchCheck) {
            checkGPSEnabled();
            toolsHandContainer.setVisibility(View.GONE);
            toggleGps(true);
            buttonShowMyLocation.setImageResource(R.drawable.ic_location_disabled_24dp);
            if (mTypeMode){
                buttonRecTrack.setVisibility(View.VISIBLE);
            }
            switchCheck = true;
        } else if (switchCheck) {
            toolsHandContainer.setVisibility(View.VISIBLE);
            toggleGps(false);
            buttonShowMyLocation.setImageResource(R.drawable.ic_my_location_24dp);
            buttonRecTrack.setVisibility(View.GONE);
            switchCheck = false;
        }
    }

    private void checkGPSEnabled() {
        LocationManager lm = (LocationManager) MapsActivity.this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!gps_enabled) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);

                // Setting Dialog Title
                alertDialog.setTitle("g_p_s is settings");

                // Setting Dialog Message
                alertDialog.setMessage("g_p_s is not enabled. Do you want to go to settings menu?");

                // On pressing Settings button
                alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        MapsActivity.this.startActivity(intent);
                    }
                });

                // on pressing cancel button
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                // Showing Alert Message
                alertDialog.show();

            }
        } catch (Exception ex) {
        }

    }


    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.getUiSettings().setCompassGravity(Gravity.CENTER_HORIZONTAL);

        Uri rootPathForRoutsString;
        if (isExternalStorageReadable()) {
            rootPathForRoutsString = Uri.fromFile(MapsActivity.this.getExternalFilesDir(
                    Environment.DIRECTORY_DOWNLOADS)).buildUpon().appendPath("Routs").build();
        }else{
            rootPathForRoutsString = Uri.fromFile(MapsActivity.this.getFilesDir()).buildUpon().appendPath("Routs").build();
        }
        if (selectUserRouts != null && selectUserRouts.size() > 0) {
            for (int i = 0; i < selectUserRouts.size(); i++) {
                String mUriString = rootPathForRoutsString.buildUpon().appendPath(selectUserRouts.get(i)).build().getPath();
                if (mUriString != null) {
                    new DrawGeoJson(mUriString).execute();
                }
            }
        }
        if (selectUserPlacesList != null && selectUserPlacesList.size() > 0) {
            for (int i = 0; i < selectUserPlacesList.size(); i++) {
                lat = selectUserPlacesList.get(i).getPositionPlace().getLatitude();
                lng = selectUserPlacesList.get(i).getPositionPlace().getLongitude();
                if (lat != 0 && lng != 0) {
                    IconFactory iconFactory = IconFactory.getInstance(MapsActivity.this);
                    Icon icon = iconFactory.fromResource(R.drawable.marcer);
                    mapboxMap.addMarker(new MarkerViewOptions().icon(icon).position(new LatLng(lat, lng)));

                }
            }
        }
        if (mTypeMode) {
            lat = 48.635022;
            lng = 24.141080;
        }else {
            toolsHandContainer.setVisibility(View.GONE);
        }
        mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(new LatLng(lat, lng))  // set the camera's center position
                        .zoom(10)  // set the camera's zoom level
                        .tilt(20)  // set the camera's tilt
                        .build()));
    }

    // This method show download dialog
    private void downloadRegionDialog() {
        // Set up download interaction. Display a dialog
        // when the user clicks download button and require
        // a user-provided region name
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);

        final EditText regionNameEdit = new EditText(MapsActivity.this);
        regionNameEdit.setHint(getString(R.string.set_region_name_hint));

        // Build the dialog box
        builder.setTitle(getString(R.string.dialog_title))
                .setView(regionNameEdit)
                .setMessage(getString(R.string.dialog_message))
                .setPositiveButton(getString(R.string.dialog_positive_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String regionName = regionNameEdit.getText().toString();
                        // Require a region name to begin the download.
                        // If the user-provided string is empty, display
                        // a toast message and do not begin download.
                        if (regionName.length() == 0) {
                            Toast.makeText(MapsActivity.this, getString(R.string.dialog_toast), Toast.LENGTH_SHORT).show();
                        } else {
                            // Begin download process
                            downloadOfflineRegion(regionName);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Display the dialog
        builder.show();
    }


    //
    private void downloadedRegionList() {
        // Build a region list when the user clicks the list button

        // Reset the region selected int to 0
        regionSelected = 0;

        // Query the DB asynchronously
        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
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
                AlertDialog dialog = new AlertDialog.Builder(MapsActivity.this)
                        .setTitle(getString(R.string.navigate_title))
                        .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Track which region the user selects
                                regionSelected = which;
                            }
                        })
                        .setPositiveButton(getString(R.string.navigate_positive_button), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                Toast.makeText(MapsActivity.this, items[regionSelected], Toast.LENGTH_LONG).show();

                                // Get the region bounds and zoom
                                LatLngBounds bounds = ((OfflineTilePyramidRegionDefinition)
                                        offlineRegions[regionSelected].getDefinition()).getBounds();
                                double regionZoom = ((OfflineTilePyramidRegionDefinition)
                                        offlineRegions[regionSelected].getDefinition()).getMinZoom();

                                // Create new camera position
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(bounds.getCenter())
                                        .zoom(regionZoom)
                                        .build();

                                // Move camera to new position
                                mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

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
                                offlineRegions[regionSelected].delete(new OfflineRegion.OfflineRegionDeleteCallback() {
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

    // This method creates and loads the offline region visible on the screen
    private void downloadOfflineRegion(final String regionName) {
        // Create a bounding box for the offline region
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(new LatLng(lat + PERIMETER_SIZE_TO_LATITUDE,
                        lng + PERIMETER_SIZE_TO_LONGITUDE)) // Northeast
                .include(new LatLng(lat - PERIMETER_SIZE_TO_LATITUDE,
                        lng - PERIMETER_SIZE_TO_LONGITUDE)) // Southwest
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
        offlineManager.createOfflineRegion(
                definition,
                metadata,
                new OfflineManager.CreateOfflineRegionCallback() {
                    @Override
                    public void onCreate(OfflineRegion offlineRegion) {
                        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);

                        // Display the download progress bar
                        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
                        startProgress();

                        // Monitor the download progress using setObserver
                        offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
                            @Override
                            public void onStatusChanged(OfflineRegionStatus status) {

                                // Calculate the download percentage and update the progress bar
                                double percentage = status.getRequiredResourceCount() >= 0
                                        ? (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
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
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (locationService != null) {
            locationService.setOwner(iCapture);
        }
        if (switchCheck) {
            checkGPSEnabled();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (locationService != null) {
            locationService.setOwner(null);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        // Ensure no memory leak occurs if we register the location listener but the call hasn't
        // been made yet.

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
        outState.putBoolean("switchCheck", switchCheck);
        outState.putBoolean("checkForRecButton", checkForRecButton);
    }

    // This method monitors the position of the user on the map
    private void toggleGps(boolean checker) {
        if (checker) {
            captureServiceConnection= new ServiceConnection() {

                public void onServiceConnected(ComponentName className, IBinder service) {
                    LocationService.MyLocalBinder binder = (LocationService.MyLocalBinder) service;
                    locationService = binder.getService();
                    locationService.setOwner(iCapture);
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
            bindService(intent, captureServiceConnection, Context.BIND_AUTO_CREATE);
            mapboxMap.getMyLocationViewSettings().setPadding(0, 200, 0, 0);
            mapboxMap.getMyLocationViewSettings().setForegroundTintColor(Color.parseColor("#56B881"));
            mapboxMap.getMyLocationViewSettings().setAccuracyTintColor(Color.parseColor("#FBB03B"));
            mapboxMap.setOnMyLocationChangeListener(myLocationChangeListener);
            mapboxMap.setMyLocationEnabled(true);
        }else{
            Intent intent = new Intent(this, LocationService.class);
            unbindService(captureServiceConnection);
            MapsActivity.this.stopService(intent);
            mapboxMap.setMyLocationEnabled(false);
            captureServiceConnection = null;
        }
    }


    // Progress bar methods
    private void startProgress() {

        // Start and show the progress bar
        isEndNotified = false;
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setPercentage(final int percentage) {
        progressBar.setIndeterminate(false);
        progressBar.setProgress(percentage);
    }

    private void endProgress(final String message) {
        // Don't notify more than once
        if (isEndNotified) {
            return;
        }

        // Stop and hide the progress bar
        isEndNotified = true;
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);

        // Show a toast
        Toast.makeText(MapsActivity.this, message, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
                            LatLng latLng = new LatLng(coord.getDouble(1), coord.getDouble(0));
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

    @Click(R.id.buttonDownloadOfflineRegion)
    void buttonDownloadOfflineRegion() {
        downloadRegionDialog();
    }

    @Click(R.id.buttonShowListRegion)
    void buttonShowListRegion() {
        downloadedRegionList();
    }

    @Click(R.id.buttonRecTrack)
    void buttonRecTrackWasClicked() {
        if (checkForRecButton) {
            showChoseCreateObjectDialog(false);
        } else {
            showCreateNameDialog(ROUT, null);

        }
    }

    private void showChoseCreateObjectDialog(final boolean handType) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_write_choose, null);
        builder.setView(dialogView);
        ImageButton imageButtonRout = (ImageButton)dialogView.findViewById(R.id.imageButtonRout);
        imageButtonRout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (handType) {
                    createdTrackPosition = new ArrayList<Position>();
                    enabledHandsMode(COMMAND_REC_ROUT);

                    alert.dismiss();
                }else {
                    Intent serviceIntent = new Intent(MapsActivity.this, LocationService.class);
                    serviceIntent.putExtra(TO_SERVICE_COMMANDS, COMMAND_REC_ROUT);
                    MapsActivity.this.startService(serviceIntent);
                    autoOrientationOff(true);
                    flashingColorAnimation(true);
                    checkForRecButton = false;
                    createdTrackPosition = new ArrayList<Position>();
                    alert.dismiss();
                }
            }
        });
        ImageButton imageButtonPlace = (ImageButton)dialogView.findViewById(R.id.imageButtonPlace);
        imageButtonPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (handType) {
                    createdTrackPosition = new ArrayList<Position>();
                    enabledHandsMode(COMMAND_REC_PLACE);
                    alert.dismiss();
                }else {
	                Intent serviceIntent = new Intent(MapsActivity.this, LocationService.class);
	                serviceIntent.putExtra(TO_SERVICE_COMMANDS, COMMAND_REC_PLACE);
	                MapsActivity.this.startService(serviceIntent);
	                autoOrientationOff(true);
	                buttonRecTrack.setClickable(false);
	                checkForRecButton = false;
                    createdTrackPosition = new ArrayList<Position>();
	                alert.dismiss();
	                showProgressDialog();
                }
            }
        });
	    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
		    @Override
		    public void onCancel(DialogInterface dialogInterface) {
			    buttonTouchCreator.setVisibility(View.GONE);
			    buttonHandEditMode.setImageResource(R.drawable.hand_icon);
			    mapboxMap.setOnMapClickListener(null);
                toolsGPSContainer.setVisibility(View.VISIBLE);
		    }
	    });
        alert = builder.create();
        alert.show();

    }

    private void showProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("Please wait while the system finds your placement");
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progres_item, null);
        RotateLoading rotateLoading = (RotateLoading)dialogView.findViewById(R.id.rotateloading);
        builder.setView(dialogView);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                Intent serviceIntent = new Intent(MapsActivity.this, LocationService.class);
                serviceIntent.putExtra(TO_SERVICE_COMMANDS, COMMAND_NO_SAVE);
                MapsActivity.this.startService(serviceIntent);
                autoOrientationOff(false);
                checkForRecButton = true;
                flashingColorAnimation(false);
                buttonRecTrack.setClickable(true);
            }
        });
        alert = builder.create();
        rotateLoading.start();
        alert.show();
    }


    @UiThread
    public void showCreateNameDialog(final int model, String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        final EditText nameInput = new EditText(this);
        builder.setView(nameInput);
        nameInput.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        if (text != null) {
            builder.setTitle("Вибачте але це імя вже використовується");
            nameInput.setText(text);
        }else if(model == ROUT && text == null){
            builder.setTitle("Введіть назву вашого маршруту");
        }else if(model == PLACE && text == null){
            builder.setTitle("Введіть назву вашого місця");
            checkForRecButton = true;
        }
        builder.setPositiveButton("Зберегти", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (mMarker != null || createdTrackPosition.size() > 2){
                    saveCreatedObject(model, nameInput.getText().toString());
	                mPointCounter = 0;


                }else {
                    Intent serviceIntent = new Intent(MapsActivity.this, LocationService.class);
                    serviceIntent.putExtra(TO_SERVICE_TRACK_NAME, nameInput.getText().toString());
                    serviceIntent.putExtra(TO_SERVICE_COMMANDS, model);
                    MapsActivity.this.startService(serviceIntent);
                    autoOrientationOff(false);
                    flashingColorAnimation(false);
                    dialog.cancel();
                }
            }
        });
        builder.setNegativeButton("Не зберігати", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mMarker != null || createdTrackPosition.size() > 2){
                   removeViews("canceled by user");
                }else {
                    Intent serviceIntent = new Intent(MapsActivity.this, LocationService.class);
                    serviceIntent.putExtra(TO_SERVICE_COMMANDS, COMMAND_NO_SAVE);
                    MapsActivity.this.startService(serviceIntent);
                    autoOrientationOff(false);
                    flashingColorAnimation(false);
                    removeViews("canceled by user");
                }
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (mMarker != null || createdTrackPosition.size() > 2){
                    removeViews("canceled by user");
                }else {
                    Intent serviceIntent = new Intent(MapsActivity.this, LocationService.class);
                    serviceIntent.putExtra(TO_SERVICE_COMMANDS, COMMAND_NO_SAVE);
                    MapsActivity.this.startService(serviceIntent);
                    autoOrientationOff(false);
                    flashingColorAnimation(false);
                    removeViews("canceled by user");
                }
            }
        });
        alert = builder.create();
        alert.show();
    }
	@Background
    public void saveCreatedObject(int model, String name) {
        if (model == PLACE){
            Place mPlace = new Place();
            mPlace.setNamePlace(name);
            mPlace.setPositionPlace(new com.example.key.my_carpathians.models.Position(
                    mMarker.getPosition().getLatitude(), mMarker.getPosition().getLongitude()));

            ObjectService objectService = new ObjectService(MapsActivity.this);
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
            ObjectService objectService = new ObjectService(MapsActivity.this);
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
            int countsPolylinesInMap = mapboxMap.getPolylines().size();
            for(int i = 0; i < countsPolylinesInMap; i++){
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
			connected = networkInfo != null && networkInfo.isAvailable() &&
					networkInfo.isConnected();
			return connected;


		} catch (Exception e) {
			System.out.println("CheckConnectivity Exception: " + e.getMessage());
			Log.v("connectivity", e.toString());
		}
		return connected;
	}
    /**
     *
     * This method turns on and turns off flashing animation for button buttonRecTrack.
     */
    private void flashingColorAnimation(boolean b) {
        buttonRecTrack.setImageResource(android.R.drawable.ic_notification_overlay);
        if(b) {
            Animation mAnimation = new AlphaAnimation(1, 0);
            mAnimation.setDuration(300);
            mAnimation.setInterpolator(new LinearInterpolator());
            mAnimation.setRepeatCount(Animation.INFINITE);
            mAnimation.setRepeatMode(Animation.REVERSE);
            buttonRecTrack.startAnimation(mAnimation);
        }else {
            buttonRecTrack.clearAnimation();
            buttonRecTrack.setImageResource(android.R.drawable.ic_menu_edit);
            if (recLine != null) {
                mapboxMap.removePolyline(recLine);
                mapboxMap.removeMarker(startMarker);
            }
        }
 
    }

    /**
     * This method turns off rotation of the device while recording a track, and turns on after rec.
     */
    private void autoOrientationOff(boolean b) {
        if (b) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        }else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }

    private void showRecLine(LatLng latLng) {
        Position mPos = new Position(latLng.getLatitude(), latLng.getLongitude(), 0);
        createdTrackPosition.add(mPos);
        if (createdTrackPosition.size() == 1) {
            IconFactory iconFactory = IconFactory.getInstance(MapsActivity.this);
            Icon iconStart = iconFactory.fromResource(R.drawable.marcer_flag_start);
            startMarker = mapboxMap.addMarker(new MarkerViewOptions().icon(iconStart).position(latLng)
                    .title("Початок"));
        }else if(createdTrackPosition.size() > 1){
            LatLng p1 = new LatLng(createdTrackPosition.get(createdTrackPosition.size() - 1).getLatitude(), createdTrackPosition.get(createdTrackPosition.size() - 1).getLongitude());
            LatLng p2 = new LatLng(createdTrackPosition.get(createdTrackPosition.size() - 2).getLatitude(), createdTrackPosition.get(createdTrackPosition.size() - 2).getLongitude());



            mapboxMap.addPolyline(new PolylineOptions().add(p1,p2).width(2));
             mapboxMap.addMarker(new MarkerOptions().setPosition(latLng));


        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        switchCheck =  savedInstanceState.getBoolean("switchCheck");
        checkForRecButton = savedInstanceState.getBoolean("checkForRecButton");
    }



    @Click(R.id.buttonShowMyLocation)
    void buttonShowMyLocationWasClicked(){
        if (!checkForRecButton){
            showCreateNameDialog(ROUT, null);

        }else {
            startGPS();
        }
    }
    @Click(R.id.buttonHandEditMode)
     void  buttonHandEditModeWasClicked(){
        if (mPointCounter == 0 &&  createdTrackPosition == null) {
            buttonTouchCreator.setVisibility(View.VISIBLE);
            buttonHandEditMode.setImageResource(R.drawable.hand_write_icon);
            showChoseCreateObjectDialog(true);
            autoOrientationOff(true);
            toolsGPSContainer.setVisibility(View.GONE);
        }else {
            buttonTouchCreator.setVisibility(View.GONE);
            toolsGPSContainer.setVisibility(View.VISIBLE);
            buttonHandEditMode.setImageResource(R.drawable.hand_icon);
            mapboxMap.setOnMapClickListener(null);
            if (mMarker != null){
                showCreateNameDialog(PLACE, null);
            }else if (createdTrackPosition.size() > 2){
                showCreateNameDialog(ROUT, null);
            }else{
                removeViews(null);
            }

        }
    }

    public void enabledHandsMode(final int type){
        MapboxMap.OnMapClickListener fingerTouchListener = new MapboxMap.OnMapClickListener() {
                @Override
                public void onMapClick(@NonNull LatLng point) {
                    mPointCounter++;
                    if (type == COMMAND_REC_PLACE) {
                        if (mMarker == null) {
                            mMarker = mapboxMap.addMarker(new MarkerViewOptions()
                                    .position(point)
                                    .title("Intervention")
                                    .snippet("Desc inter"));
                        }else {
                            mMarker.remove();
                            mMarker = mapboxMap.addMarker(new MarkerViewOptions()
                                    .position(point)
                                    .title("Intervention")
                                    .snippet("Desc inter"));
                        }
                    }else if(type == COMMAND_REC_ROUT){
                        showRecLine(point);
                    }

                }
            };

            mapboxMap.setOnMapClickListener(fingerTouchListener);
    }
    @Click(R.id.buttonOnBack)
    void  buttonOnBackWasClicked(){
        if (mapboxMap.getPolylines().size() > 0) {
               mapboxMap.getPolylines().get(mPointCounter -2).remove();
                createdTrackPosition.remove(createdTrackPosition.size() - 1);
            mPointCounter--;
        }
         if (mapboxMap.getMarkers().size() > 1 & mMarker == null){
             mapboxMap.getMarkers().get(mPointCounter).remove();

        }else if (startMarker != null){
             startMarker.remove();
             createdTrackPosition.remove(createdTrackPosition.size() - 1);
             mPointCounter = 0;
         }else if(mMarker != null){
	         mMarker.remove();
             mPointCounter = 0;
             mMarker = null;
         }

    }
}

