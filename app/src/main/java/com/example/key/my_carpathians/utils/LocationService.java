package com.example.key.my_carpathians.utils;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.cocoahero.android.geojson.Position;
import com.example.key.my_carpathians.interfaces.ILocation;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import static com.example.key.my_carpathians.activities.MapsActivity.COMMAND_NO_SAVE;
import static com.example.key.my_carpathians.activities.MapsActivity.COMMAND_REC_PLACE;
import static com.example.key.my_carpathians.activities.MapsActivity.COMMAND_REC_ROUT;
import static com.example.key.my_carpathians.activities.MapsActivity.ERROR_TRACK;
import static com.example.key.my_carpathians.activities.MapsActivity.TO_SERVICE_COMMANDS;
import static com.example.key.my_carpathians.activities.MapsActivity.TO_SERVICE_TRACK_NAME;
import static com.example.key.my_carpathians.activities.StartActivity.PLACE;
import static com.example.key.my_carpathians.activities.StartActivity.PREFS_NAME;
import static com.example.key.my_carpathians.activities.StartActivity.ROOT_PATH;
import static com.example.key.my_carpathians.activities.StartActivity.ROUT;
import static com.example.key.my_carpathians.utils.ObjectService.FILE_EXISTS;


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
    public static final String CREATED_BY_USER_PLACE_LIST = "created_place_list";

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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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
            mIntCommand = 0;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    private void savePlaceToSDCard(String mNamePlace) {

       String mRootPathString = getApplicationContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(ROOT_PATH, null);

        if (mLocation != null) {
	        Place mPlace = new Place();
	        mPlace.setNamePlace(mNamePlace);
	        mPlace.setPositionPlace(new com.example.key.my_carpathians.models.
			        Position(mLocation.getLatitude(), mLocation.getLongitude()));
            ObjectService objectService = new ObjectService(getApplicationContext(), mRootPathString);
            String outcome = objectService.savePlace(mNamePlace, mPlace, false);
            if (outcome.equals(FILE_EXISTS) & owner != null){
                owner.messageForActivity(ROUT, mNamePlace);
            }else{
                Toast.makeText(getApplicationContext(), outcome, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void saveRoutToSDCard(String mNameRout) {
        String mRootPathString = getApplicationContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(ROOT_PATH, null);

        if (mPositionList.size() > 2) {
	    Rout mRout = new Rout();
	    mRout.setNameRout(mNameRout);
	    mRout.setPositionRout(new com.example.key.my_carpathians.models.Position(
	    		mPositionList.get(0).getLatitude(), mPositionList.get(0).getLongitude()));
        ObjectService objectService = new ObjectService(getApplicationContext(), mRootPathString);
        String outcome = objectService.saveRout(mNameRout, mPositionList, mRout, false);
        if (outcome.equals(FILE_EXISTS) & owner != null){
            owner.messageForActivity(ROUT, mNameRout);
        }else{
            Toast.makeText(getApplicationContext(), outcome, Toast.LENGTH_LONG).show();
        }
    }else {
    if (owner != null){
        owner.messageForActivity(ERROR_TRACK, "");
    }
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
                owner.update(location, PLACE);
            }
            owner.update(location, 101);
        }else if(owner == null && mIntCommand != COMMAND_REC_ROUT){
            if (mLocationRequest.getFastestInterval() == FASTEST_INTERVAL_ACTIVE)
            mLocationRequest.setInterval(UPDATE_INTERVAL_PASSIVE);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL_PASSIVE);
        }else if (mIntCommand == COMMAND_REC_ROUT) {
            mLocationRequest.setInterval(UPDATE_INTERVAL_REC);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL_REC);
            saveLocationToLocationList(location);
            owner.update(location, ROUT);
        }
    }




    private void saveLocationToLocationList(Location location) {

        Position position = new Position(location);
        if(position.getAltitude() != 0 && mPositionList.size() != 0){
            for(int i = 0; i < mPositionList.size(); i++ ){
                if (position == mPositionList.get(i)){
                    break;
                }else if (i == mPositionList.size() -1){
                    mPositionList.add(position);

                }
            }
//TODO need to verify bottom line
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
