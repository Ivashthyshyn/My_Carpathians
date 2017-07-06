package com.example.key.my_carpathians;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
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

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
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

import static com.example.key.my_carpathians.PlaceActivity.GEOJSON_ROUT;
import static com.example.key.my_carpathians.PlaceActivity.LATITUDE;
import static com.example.key.my_carpathians.PlaceActivity.LONGITUDE;
import static com.example.key.my_carpathians.StartActivity.PREFS_NAME;

@EActivity
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {


    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private FloatingActionButton floatingActionButton;
    private File localFile;
    private static final String TAG = "MapsActivity";
    private Location mLocation = null;
    private boolean isEndNotified;
    private ProgressBar progressBar;
    private OfflineManager offlineManager;
    public MapsActivity permissionsManager;

    private double lng;
    private double lat;
    private boolean switchCheck = false;
    // JSON encoding/decoding
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";
    public String uriFromFle;
    private LocationService locationService;
    public ILocation iCapture;
    private ServiceConnection captureServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            LocationService.MyLocalBinder binder = (LocationService.MyLocalBinder) service;
            locationService = binder.getService();
            locationService.setOwner(iCapture);
        }

        public void onServiceDisconnected(ComponentName arg0) {
        }
    };
    private MapboxMap.OnMyLocationChangeListener myLocationChangeListener = new MapboxMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(@Nullable Location location) {

        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences mSharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        iCapture = new ILocation() {
            @Override
            public void update(Location location) {
                myLocationChangeListener.onMyLocationChange(location);

            }

            @Override
            public void connectionState(int state) {
                if (state == 1){
                    Toast.makeText(MapsActivity.this,"This device is not supported.",Toast.LENGTH_LONG).show();
                }else {
                    GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
                    apiAvailability.getErrorDialog(MapsActivity.this, state, PLAY_SERVICES_RESOLUTION_REQUEST)
                            .show();
                }
            }
        };

        uriFromFle = mSharedPreferences.getString(GEOJSON_ROUT , "");
        lng = getIntent().getDoubleExtra(LONGITUDE,0);
        lat = getIntent().getDoubleExtra(LATITUDE,0);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_maps);


        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.location_toggle_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mapboxMap != null && switchCheck == false) {
                    checkGPSEnabled();
                    toggleGps();
                    floatingActionButton.setImageResource(R.drawable. ic_location_disabled_24dp);
                    switchCheck = true;
                }else if (switchCheck == true){
                    mapboxMap.setMyLocationEnabled(false);
                    stopService(new Intent(MapsActivity.this, LocationService.class));
                    floatingActionButton.setImageResource(R.drawable.ic_my_location_24dp);
                    switchCheck = false;
                }
            }
        });
    }

    private void checkGPSEnabled() {
        LocationManager lm = (LocationManager)MapsActivity.this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!gps_enabled){
                AlertDialog.Builder alertDialog = new AlertDialog.Builder( MapsActivity.this);

                // Setting Dialog Title
                alertDialog.setTitle("GPS is settings");

                // Setting Dialog Message
                alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

                // On pressing Settings button
                alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
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
        } catch(Exception ex) {}

    }



    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(new LatLng(lng, lat))  // set the camera's center position
                        .zoom(13)  // set the camera's zoom level
                        .tilt(20)  // set the camera's tilt
                        .build()));
        new DrawGeoJson().execute();
    }
    // This method creates and loads the offline region visible on the screen
    private void downloadOfflineRegion() {
        offlineManager = OfflineManager.getInstance(MapsActivity.this);

        // Create a bounding box for the offline region
        LatLngBounds bounds = mapboxMap.getProjection().getVisibleRegion().latLngBounds;
        double minZoom = mapboxMap.getCameraPosition().zoom;
        double maxZoom = mapboxMap.getMaxZoomLevel();
        float pixelRatio = this.getResources().getDisplayMetrics().density;

        // Define the offline region
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                mapboxMap.getStyleUrl(),
                bounds,
                9,
                14,
                MapsActivity.this.getResources().getDisplayMetrics().density);

        // Set the metadata
        byte[] metadata;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JSON_FIELD_REGION_NAME, uriFromFle);
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
        if(switchCheck){
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
    }

    // This method monitors the position of the user on the map
    private void toggleGps() {
        Intent intent = new Intent(this, LocationService.class);
        startService(intent);
        bindService(intent, captureServiceConnection, Context.BIND_AUTO_CREATE);
        mapboxMap.getMyLocationViewSettings().setPadding(0, 200, 0, 0);
        mapboxMap.getMyLocationViewSettings().setForegroundTintColor(Color.parseColor("#56B881"));
        mapboxMap.getMyLocationViewSettings().setAccuracyTintColor(Color.parseColor("#FBB03B"));

        mapboxMap.setMyLocationEnabled(true);
        mapboxMap.setOnMyLocationChangeListener(myLocationChangeListener);
        mapboxMap.setOnMyLocationChangeListener(new MapboxMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(@Nullable Location location) {

            }
        });
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
        @Override
        protected List<LatLng> doInBackground(Void... voids) {

            ArrayList<LatLng> points = new ArrayList<>();

            try {
                // Load GeoJSON file
                File file = new File(uriFromFle);
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

            if (points.size() > 0) {

                // Draw polyline on map
                mapboxMap.addPolyline(new PolylineOptions()
                        .addAll(points)
                        .color(Color.parseColor("#ff6861"))
                        .width(2));
                mapboxMap.addMarker(new MarkerOptions().position(points.get(0)).setTitle("Початок"));
                mapboxMap.addMarker(new MarkerOptions().position(points.get(points.size()-1)).setTitle("Кінець"));
            }
        }
    }
    @Click (R.id.downloadOfflineRegion)
    void downloadOfflineRegionWasClicked(){
    downloadOfflineRegion();
    }

}



