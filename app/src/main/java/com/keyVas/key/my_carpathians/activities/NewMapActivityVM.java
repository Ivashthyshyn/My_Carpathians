package com.keyVas.key.my_carpathians.activities;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.keyVas.key.my_carpathians.utils.Resource;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;
import com.mapbox.services.commons.utils.TextUtils;

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

public class NewMapActivityVM extends AndroidViewModel {

    private static final String TAG = "NewMapActivityVM";

    private MutableLiveData<List<LatLng>> routPoints = new MutableLiveData<>();

    private MutableLiveData<Resource<Integer>> loadingOfflineMap = new MutableLiveData<>();

    public NewMapActivityVM(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<List<LatLng>> getRoutPoints() {
        return routPoints;
    }

    public MutableLiveData<Resource<Integer>> getLoadingOfflineMap() {
        return loadingOfflineMap;
    }

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

