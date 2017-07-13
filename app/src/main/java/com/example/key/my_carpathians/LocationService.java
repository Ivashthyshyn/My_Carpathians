package com.example.key.my_carpathians;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class LocationService extends Service implements
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final long UPDATE_INTERVAL = 1000 * 2;
    private static final long FASTEST_INTERVAL = 1000 * 10;

    private final IBinder myBinder = new MyLocalBinder();

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private ILocation owner;

    @Override
    public void onCreate() {
        if (isGooglePlayServicesAvailable()) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
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
        return START_STICKY;
    }

    /**
     * Callback when the location changes. Inform the owner.
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        if (owner != null) {
            owner.update(location);
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
