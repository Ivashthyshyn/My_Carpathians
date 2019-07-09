package com.keyVas.key.my_carpathians.activities.map;

import android.annotation.SuppressLint;
import android.app.Application;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.keyVas.key.my_carpathians.utils.Resource;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;


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

import static android.os.Looper.getMainLooper;

public class MapViewModel extends AndroidViewModel {

    private static final String TAG = "MapViewModel";

    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 3000;

    private static final long DEFAULT_MAX_WAIT_TIME = 2000;

    private MutableLiveData<List<LatLng>> routPoints = new MutableLiveData<>();

    private MutableLiveData<Location> myLocation = new MutableLiveData<>();

    private MutableLiveData<Resource<Integer>> loadingOfflineMap = new MutableLiveData<>();

    private LocationEngine locationEngine;

    public MapViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<LatLng>> getRoutPoints() {
        return routPoints;
    }

    public LiveData<Resource<Integer>> getLoadingOfflineMap() {
        return loadingOfflineMap;
    }

    public LiveData<Location> getMyLocation() {
        return myLocation;
    }

    @SuppressLint("MissingPermission")
    public void initMyLocation() {
        if (locationEngine == null) {
            locationEngine = LocationEngineProvider.getBestLocationEngine(getApplication());
            LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                    .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

            locationEngine.requestLocationUpdates(request, locationCallback, getMainLooper());
            locationEngine.getLastLocation(locationCallback);
        }
    }

    private LocationEngineCallback<LocationEngineResult> locationCallback = new LocationEngineCallback<LocationEngineResult>() {
        @Override
        public void onSuccess(LocationEngineResult result) {
            if (result != null) {
                myLocation.setValue(result.getLastLocation());
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
        }
    };

    public void drawGeoJson(String path) {
        Runnable runnable = () -> {
            ArrayList<LatLng> points = new ArrayList<>();
            try {
                // Load GeoJSON file
                File file = new File(path);
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
                    if (!TextUtils.isEmpty(type) && type.equalsIgnoreCase("LineString")) {
                        JSONArray coordinates = geometry.getJSONArray("coordinates");
                        for (int lc = 0; lc < coordinates.length(); lc++) {
                            JSONArray coordinate = coordinates.getJSONArray(lc);
                            LatLng latLng = new LatLng(coordinate.getDouble(1),
                                    coordinate.getDouble(0));
                            points.add(latLng);
                        }
                    }
                }
            } catch (Exception exception) {
                Log.e(TAG, "Exception Loading GeoJSON: " + exception.toString());
                Toast.makeText(getApplication(), "Geo JSON Drawing failed", Toast.LENGTH_SHORT).show();
            }
            if (points.size() > 0)
                routPoints.postValue(points);

        };
        runnable.run();
    }

    public void downloadOfflineRegion(OfflineTilePyramidRegionDefinition definition, byte[] metadata) {
        OfflineManager.getInstance(getApplication().getBaseContext()).createOfflineRegion(definition, metadata,
                new OfflineManager.CreateOfflineRegionCallback() {
                    @Override
                    public void onCreate(OfflineRegion offlineRegion) {
                        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);

                        loadingOfflineMap.postValue(Resource.loading(1));
                        offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
                            @Override
                            public void onStatusChanged(OfflineRegionStatus status) {
                                double percentage = status.getRequiredResourceCount() >= 0
                                        ? (100.0 * status.getCompletedResourceCount()
                                        / status.getRequiredResourceCount()) :
                                        0.0;


                                if (status.isComplete()) {
                                    loadingOfflineMap.postValue(Resource.success());
                                } else if (status.isRequiredResourceCountPrecise()) {
                                    loadingOfflineMap.postValue(Resource.loading((int) percentage));
                                }
                            }

                            @Override
                            public void onError(OfflineRegionError error) {
                                loadingOfflineMap.postValue(Resource.error(error.getMessage(), null));
                            }

                            @Override
                            public void mapboxTileCountLimitExceeded(long limit) {
                                loadingOfflineMap.postValue(Resource.error("Mapbox tile count limit exceeded: " + limit, null));
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        loadingOfflineMap.postValue(Resource.error(error, null));
                    }
                });
    }
}
