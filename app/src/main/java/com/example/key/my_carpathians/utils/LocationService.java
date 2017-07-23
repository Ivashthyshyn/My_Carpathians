package com.example.key.my_carpathians.utils;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.Geometry;
import com.cocoahero.android.geojson.GeometryCollection;
import com.cocoahero.android.geojson.LineString;
import com.cocoahero.android.geojson.Point;
import com.cocoahero.android.geojson.Position;
import com.cocoahero.android.geojson.PositionList;
import com.example.key.my_carpathians.interfaces.ILocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.key.my_carpathians.activities.MapsActivity.TO_SERVICE_COMMANDS;
import static com.example.key.my_carpathians.activities.MapsActivity.TO_SERVICE_TRACK_NAME;
import static com.example.key.my_carpathians.activities.StartActivity.PREFS_NAME;


public class LocationService extends Service implements
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private static final long UPDATE_INTERVAL_ACTIVE = 1000 * 30;
    private static final long FASTEST_INTERVAL_ACTIVE = 1000 * 20;
    private static final long UPDATE_INTERVAL_PASSIVE = 1000 * 90;
    private static final long FASTEST_INTERVAL_PASSIVE = 1000 * 60;
    public static final String CREATED_BY_USER_TRACK_LIST = "created_track_list";

    private final IBinder myBinder = new MyLocalBinder();

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private ILocation owner;
    private int mIntCommand;
    private List<Position> mPositionList = new ArrayList<>();
    private String mNameTrack;
    @Override
    public void onCreate() {
        if (isGooglePlayServicesAvailable()) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL_ACTIVE);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL_ACTIVE);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * The binder that returns the service activity.
     */
    public class MyLocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return myBinder;
    }

    /**
     * Bound methods.
     * <p>
     * Set the owner, to be notified when the position changes.
     *
     * @param owner
     */
    public void setOwner(ILocation owner) {
        this.owner = owner;
    }

    /**
     * Start the service and keep it running when the phone is idle.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mIntCommand = intent.getIntExtra(TO_SERVICE_COMMANDS, 0);
        mNameTrack = intent.getStringExtra(TO_SERVICE_TRACK_NAME);
        if (mNameTrack != null){
            saveTrackToSDCard(mNameTrack);
        }
        return START_STICKY;
    }

    private void saveTrackToSDCard(String mNameTrack) {
        LineString lineString = new LineString();
        lineString.setPositions( mPositionList);

        Feature feature = new Feature();


        try {
            JSONObject geoJSON = new JSONObject();
            feature.setProperties(new JSONObject());
            feature.setGeometry(lineString);
            feature.setIdentifier("key.my_carpathians");
            geoJSON.put("feature", new JSONArray().put(feature.toJSON()));
            geoJSON.put("type","FeatureCollection");

            File rootPath = new File(Environment.getExternalStorageDirectory(), "Routs");
            Writer output = null;
            final File localFile = new File(rootPath, mNameTrack +".geojson" );
            final URI fileUri = localFile.toURI();
            output = new BufferedWriter(new FileWriter(localFile));
            output.write(geoJSON.toString());
            output.close();
            SharedPreferences mSharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            Set<String> CreatedByUserTrackList = mSharedPreferences.getStringSet(CREATED_BY_USER_TRACK_LIST,new HashSet<String>());
            CreatedByUserTrackList.add(mNameTrack);
            mSharedPreferences.edit().putString(mNameTrack, fileUri.toString()).apply();
            boolean isSaved = mSharedPreferences.edit().putStringSet(mNameTrack, CreatedByUserTrackList).commit();
            if(isSaved) {
                Toast.makeText(getApplicationContext(), "Track saved", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Callback when the location changes. Inform the owner.
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        if (owner != null) {
            if (mLocationRequest.getFastestInterval() == FASTEST_INTERVAL_PASSIVE){
                mLocationRequest.setInterval(UPDATE_INTERVAL_ACTIVE);
                mLocationRequest.setFastestInterval(FASTEST_INTERVAL_ACTIVE);
            }

            owner.update(location);
        }else {
            if (mLocationRequest.getFastestInterval() == FASTEST_INTERVAL_ACTIVE)
            mLocationRequest.setInterval(UPDATE_INTERVAL_PASSIVE);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL_PASSIVE);
        }
        if (mIntCommand == 1)
            saveLocationDate(location);
    }

    private void saveLocationDate(Location location) {
        Position position = new Position(location);
        mPositionList.add(position);
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                owner.connectionState(resultCode);
            } else {
                owner.connectionState(1);
            }
            return false;
        }
        return true;
    }
}
