package com.keyVas.key.my_carpathians.utils;

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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.keyVas.key.my_carpathians.interfaces.ILocation;

import java.util.List;

import static com.keyVas.key.my_carpathians.activities.MapsActivity.CANCEL_REC;
import static com.keyVas.key.my_carpathians.activities.MapsActivity.COMMAND_PAUSE_REC_ROUT;
import static com.keyVas.key.my_carpathians.activities.MapsActivity.COMMAND_REC_PLACE;
import static com.keyVas.key.my_carpathians.activities.MapsActivity.COMMAND_REC_ROUT;
import static com.keyVas.key.my_carpathians.activities.MapsActivity.TO_SERVICE_COMMANDS;
import static com.keyVas.key.my_carpathians.activities.StartActivity.PLACE;
import static com.keyVas.key.my_carpathians.activities.StartActivity.ROUT;


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
    public static final int WAIT_MODE = 101;

    private final IBinder myBinder = new MyLocalBinder();

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private ILocation owner;
    private int mIntCommand = 0;
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
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        Toast.makeText(this, connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
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
      if (mIntCommand == CANCEL_REC){
            TrackContainer.getInstance().getPositionList().clear();
            TrackContainer.getInstance().setEnabledGPSRecording(false);
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
            owner.update(location, WAIT_MODE);
        }else if(owner == null && mIntCommand != COMMAND_REC_ROUT){
            if (mLocationRequest.getFastestInterval() == FASTEST_INTERVAL_ACTIVE)
            mLocationRequest.setInterval(UPDATE_INTERVAL_PASSIVE);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL_PASSIVE);
        }else if (mIntCommand == COMMAND_REC_ROUT) {
            mLocationRequest.setInterval(UPDATE_INTERVAL_REC);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL_REC);
            saveLocationToLocationList(location);
           if(owner != null) {
               owner.update(location, ROUT);
           }
        } else if (mIntCommand == COMMAND_PAUSE_REC_ROUT) {
           mLocationRequest.setInterval(UPDATE_INTERVAL_PASSIVE);
           mLocationRequest.setFastestInterval(UPDATE_INTERVAL_PASSIVE);
           if(owner != null) {
               owner.update(location, ROUT);
           }
       }
    }

    private void saveLocationToLocationList(Location location) {
        TrackContainer.getInstance().setEnabledGPSRecording(true);
        List<Position> mPositionList = TrackContainer.getInstance().getPositionList();
        Position position = new Position(
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude());
        if(position.getAltitude() != 0 && mPositionList.size() != 0){
            if (mPositionList.size() > 0 && position == mPositionList.get(mPositionList.size() - 1)){
                mPositionList.add(position);
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
