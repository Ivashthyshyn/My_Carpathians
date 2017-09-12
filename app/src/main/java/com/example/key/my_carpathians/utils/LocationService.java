package com.example.key.my_carpathians.utils;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.LineString;
import com.cocoahero.android.geojson.Position;
import com.example.key.my_carpathians.interfaces.ILocation;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.key.my_carpathians.activities.MapsActivity.BREAK_UP_CONNECTION;
import static com.example.key.my_carpathians.activities.MapsActivity.COMMAND_NO_SAVE;
import static com.example.key.my_carpathians.activities.MapsActivity.COMMAND_REC_PLACE;
import static com.example.key.my_carpathians.activities.MapsActivity.COMMAND_REC_ROUT;
import static com.example.key.my_carpathians.activities.MapsActivity.TO_SERVICE_COMMANDS;
import static com.example.key.my_carpathians.activities.MapsActivity.TO_SERVICE_TRACK_NAME;
import static com.example.key.my_carpathians.activities.StartActivity.PREFS_NAME;
import static com.example.key.my_carpathians.adapters.FavoritesRecyclerAdapter.PLACE;
import static com.example.key.my_carpathians.adapters.FavoritesRecyclerAdapter.ROUT;
import static com.example.key.my_carpathians.fragments.EditModeFragment.NO_PUBLISH_CONSTANT;


public class LocationService extends Service implements
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private static final long UPDATE_INTERVAL_ACTIVE = 1000 * 30;
    private static final long FASTEST_INTERVAL_ACTIVE = 1000 * 20;
    private static final long UPDATE_INTERVAL_PASSIVE = 1000 * 90;
    private static final long FASTEST_INTERVAL_PASSIVE = 1000 * 60;
    private static final long UPDATE_INTERVAL_REC = 1000 * 20;
    private static final long FASTEST_INTERVAL_REC = 1000 * 10;

    public static final String CREATED_BY_USER_ROUT_LIST = "created_rout_list";
    public static final int DEFINED_LOCATION = 1;
    public static final String CREATED_BY_USER_PLACE_LIST ="created_place_list" ;

    private final IBinder myBinder = new MyLocalBinder();

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private ILocation owner;
    private int mIntCommand = 0;
    private List<Position> mPositionList = new ArrayList<>();
    private String mNameTrack;
    private Location mLocation;

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
        if (mIntCommand == ROUT){
            saveRoutToSDCard(mNameTrack);
            mLocationRequest.setInterval(UPDATE_INTERVAL_ACTIVE);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL_ACTIVE);
        }else if(mIntCommand == PLACE){
            savePlaceToSDCard(mNameTrack);
            mLocationRequest.setInterval(UPDATE_INTERVAL_ACTIVE);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL_ACTIVE);
        }else if (mIntCommand == COMMAND_NO_SAVE){
            mPositionList.clear();
            mLocation = null;
            mLocationRequest.setInterval(UPDATE_INTERVAL_ACTIVE);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL_ACTIVE);
        }else if(mIntCommand == BREAK_UP_CONNECTION){
            mGoogleApiClient.disconnect();
            super.onDestroy();
        }
        return START_STICKY;
    }

    private void savePlaceToSDCard(String mNamePlace) {
        if (mLocation != null) {
            Place mPlace = new Place();
            mPlace.setNamePlace(mNamePlace);
            com.example.key.my_carpathians.models.Position position = new com.example.key.my_carpathians.models.Position();
            position.setLatitude(mLocation.getLatitude());
            position.setLongitude(mLocation.getLongitude());
            mPlace.setPositionPlace(position);

            File rootPath = new File(getApplicationContext().getExternalFilesDir(
                    Environment.DIRECTORY_DOWNLOADS), "Created");
            if (!rootPath.exists()) {
                rootPath.mkdirs();
            }

            File file = new File(rootPath, mNamePlace);
            String fileUri = String.valueOf(file.toURI());
            if (file.exists()) {
                owner.messageForActivity(PLACE, mNamePlace);
            }
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(mPlace);
                objectOutputStream.close();
                fileOutputStream.close();
                SharedPreferences mSharedPreferences = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                Set<String> createdByUserTrackList = new HashSet<>(mSharedPreferences.getStringSet(CREATED_BY_USER_PLACE_LIST, new HashSet<String>()));
                createdByUserTrackList.add(mNamePlace);
                mSharedPreferences.edit().putString(mNamePlace, fileUri).apply();
                mSharedPreferences.edit().putStringSet(CREATED_BY_USER_PLACE_LIST, createdByUserTrackList).apply();
                Toast.makeText(getApplicationContext(), "Place saved", Toast.LENGTH_LONG).show();
                mLocation = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveRoutToSDCard(String mNameRout) {

        LineString lineString = new LineString();
        lineString.setPositions( mPositionList);
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
            File localFile = new File(rootPath, mNameRout + ".geojson");
            if (localFile.exists()) {
                owner.messageForActivity(ROUT, mNameRout);
            }else {
                String fileUri = String.valueOf(localFile.toURI());
                Writer output = new BufferedWriter(new FileWriter(localFile));
                output.write(geoJSON.toString());
                output.close();
                SharedPreferences mSharedPreferences = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                mSharedPreferences.edit().putString(mNameRout, fileUri).apply();
                Rout mRout = new Rout();
                mRout.setNameRout(mNameRout);
                mRout.setUrlRoutsTrack(fileUri);

                File rootPath2 = new File(getApplicationContext().getExternalFilesDir(
                        Environment.DIRECTORY_DOWNLOADS), "Created");
                if (!rootPath2.exists()) {
                    rootPath2.mkdirs();
                }

                File file = new File(rootPath2, mNameRout + NO_PUBLISH_CONSTANT);
                String fileUri2 = String.valueOf(file.toURI());
                if (file.exists()) {
                    owner.messageForActivity(ROUT, mNameRout);
                }else {
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                        objectOutputStream.writeObject(mRout);
                        objectOutputStream.close();
                        fileOutputStream.close();
                        Set<String> createdByUserTrackList = new HashSet<>(mSharedPreferences.getStringSet(CREATED_BY_USER_ROUT_LIST, new HashSet<String>()));
                        createdByUserTrackList.add(mNameRout + NO_PUBLISH_CONSTANT);
                        mSharedPreferences.edit().putString(mNameRout + NO_PUBLISH_CONSTANT, fileUri2).apply();
                        mSharedPreferences.edit().putStringSet(CREATED_BY_USER_ROUT_LIST, createdByUserTrackList).apply();
                        Toast.makeText(getApplicationContext(), "Rout saved", Toast.LENGTH_LONG).show();
                        mPositionList.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
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
       if (owner != null && mIntCommand != COMMAND_REC_ROUT) {
            if (mLocationRequest.getFastestInterval() == FASTEST_INTERVAL_PASSIVE){
                mLocationRequest.setInterval(UPDATE_INTERVAL_ACTIVE);
                mLocationRequest.setFastestInterval(FASTEST_INTERVAL_ACTIVE);
            }
            if (mIntCommand == COMMAND_REC_PLACE){
                mLocation = location;
                mIntCommand = 0;
                owner.update(location, DEFINED_LOCATION);
            }
            owner.update(location, 0);
        }else if(owner == null && mIntCommand != COMMAND_REC_ROUT){
            if (mLocationRequest.getFastestInterval() == FASTEST_INTERVAL_ACTIVE)
            mLocationRequest.setInterval(UPDATE_INTERVAL_PASSIVE);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL_PASSIVE);
        }else if (mIntCommand == COMMAND_REC_ROUT) {
            mLocationRequest.setInterval(UPDATE_INTERVAL_REC);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL_REC);
            saveRoutLocationList(location);
            owner.update(location, 0);
        }
    }




    private void saveRoutLocationList(Location location) {

        Position position = new Position(location);
        if(position.getAltitude() != 0 && mPositionList.size() != 0){
            for(int i = 0; i < mPositionList.size(); i++ ){
                if (position == mPositionList.get(i)){
                    break;
                }else if (i == mPositionList.size() -1){
                    mPositionList.add(position);

                }
            }

        }else if(mPositionList.size() == 0 && position.getAltitude() != 0){
            mPositionList.add(position);
        }
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
