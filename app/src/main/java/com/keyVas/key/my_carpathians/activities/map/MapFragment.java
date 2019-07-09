package com.keyVas.key.my_carpathians.activities.map;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.keyVas.key.my_carpathians.R;
import com.keyVas.key.my_carpathians.databinding.MapFragmentBinding;
import com.keyVas.key.my_carpathians.dialogs.DownloadRegionDialog;
import com.keyVas.key.my_carpathians.dialogs.InternetConnectionDialog;
import com.keyVas.key.my_carpathians.models.Place;
import com.keyVas.key.my_carpathians.models.Rout;
import com.keyVas.key.my_carpathians.utils.ConnectionUtils;
import com.keyVas.key.my_carpathians.utils.Resource;
import com.keyVas.key.my_carpathians.utils.Status;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
//
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;
import static com.keyVas.key.my_carpathians.activities.ActionActivity.SELECTED_USER_PLACES;
import static com.keyVas.key.my_carpathians.activities.ActionActivity.SELECTED_USER_ROUTS;
import static com.keyVas.key.my_carpathians.activities.SettingsActivity.AVERAGE_VALUE;
import static com.keyVas.key.my_carpathians.activities.SettingsActivity.VALUE_OFFLINE_REGION_AROUND_RADIUS;
import static com.keyVas.key.my_carpathians.activities.StartActivity.OFFLINE_MAP;
import static com.keyVas.key.my_carpathians.activities.StartActivity.PREFS_NAME;
import static com.keyVas.key.my_carpathians.activities.StartActivity.PRODUCE_MODE;
import static com.keyVas.key.my_carpathians.activities.StartActivity.ROOT_PATH;
import static com.keyVas.key.my_carpathians.models.Place.EN;
import static com.keyVas.key.my_carpathians.utils.LocaleHelper.SELECTED_LANGUAGE;

public class MapFragment extends Fragment implements PermissionsListener, OnMapReadyCallback, MapboxMap.CancelableCallback, DownloadRegionDialog.DownloadRegionListener, OfflineManager.ListOfflineRegionsCallback {

    private static final int CONSTANT_PERIMETER_SIZE = 1122;
    private MapViewModel viewModel;

    private static final String TAG = "NewMapActivity";
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";
    PolygonOptions mPolygonOptions;
    MapFragmentBinding binding;
    List<Rout> selectUserRouts;
    List<Place> selectUserPlacesList;
    MapboxMap mapboxMap;
    LatLng mPoint;
    LatLng mPointPlace;
    SharedPreferences sharedPreferences;
    String mRootPathString;
    String mUserLanguage;
    private boolean mTypeMode;
    private String mOfflineRegionName;
    private OfflineManager mOfflineManager;
    private Polygon mOfflinePolygon;
    private PermissionsManager permissionsManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(requireActivity(), getString(R.string.access_token));
        binding = DataBindingUtil.inflate(inflater, R.layout.map_fragment, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(MapViewModel.class);

        binding.toolbarMap.inflateMenu(R.menu.menu_map_activity);
        binding.toolbarMap.setOnMenuItemClickListener(this::onOptionsItemSelected);
        viewModel.getLoadingOfflineMap().observe(this, this::onLoadOfflineMap);
        viewModel.getRoutPoints().observe(this, this::onDrawRout);
        setupMapBox(savedInstanceState);
        initDate();
    }

    private void onLoadOfflineMap(Resource<Integer> doubleResource) {
        switch (doubleResource.status) {
            case Status.SUCCESS:
                binding.progressBar.setVisibility(View.GONE);
                Snackbar.make(binding.getRoot(), R.string.region_downloads, Snackbar.LENGTH_LONG).show();
                break;
            case Status.ERROR:
                Snackbar.make(binding.getRoot(), doubleResource.message, Snackbar.LENGTH_LONG).show();
                binding.progressBar.setVisibility(View.GONE);
                break;
            case Status.LOADING:
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.progressBar.setProgress(doubleResource.data);
                break;
            default:
                break;
        }

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.download_region:
                startActionModOfflineRegion();
                return true;
            case R.id.action_g_p_s:
                if (mapboxMap != null) {
                    //   checkGPSEnabled();

                } else {
                    //    enabledGPS(false);
                }
                return true;
            case R.id.offline_regions:
                //  downloadedRegionList();
                return true;
            case R.id.auto_create_offline_region:
                startActionModOfflineRegion();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startActionModOfflineRegion() {
        if (ConnectionUtils.isConnected(requireContext())) {
            if (mTypeMode) {
                // some UI desing
            }
            double valueOfflineRegionPerimeter = CONSTANT_PERIMETER_SIZE
                    * requireContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .getInt(VALUE_OFFLINE_REGION_AROUND_RADIUS, AVERAGE_VALUE);
            showDownloadRegionDialog(valueOfflineRegionPerimeter);


        } else {
            showConnectionDialog();
        }
    }


    private void showConnectionDialog() {
        InternetConnectionDialog connectionDialog = new InternetConnectionDialog();
        connectionDialog.show(getChildFragmentManager(), InternetConnectionDialog.TAG);

    }

    private void showDownloadRegionDialog(final double perimeterValue) {
        DownloadRegionDialog downloadRegionDialog = DownloadRegionDialog.newInstance(perimeterValue);
        downloadRegionDialog.show(getChildFragmentManager(), DownloadRegionDialog.TAG);
    }

    private void downloadOfflineRegion(final String regionName, double perimeterValue) {
        // Create a bounding box for the offline region
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(new LatLng(mPoint.getLatitude() + perimeterValue,
                        mPoint.getLongitude() + perimeterValue)) // Northeast
                .include(new LatLng(mPoint.getLatitude() - perimeterValue,
                        mPoint.getLongitude() - perimeterValue)) // Southwest
                .build();
        // Define the offline region
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                mapboxMap.getStyle().getUrl(),
                latLngBounds,
                9,
                14,
                this.getResources().getDisplayMetrics().density);

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
        viewModel.downloadOfflineRegion(definition, metadata);
    }

    private void onDrawRout(List<LatLng> points) {

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
            mPoint = points.get(points.size() / 2);
            // Draw polyline on map
            mapboxMap.addPolyline(new PolylineOptions()
                    .addAll(points)
                    .color(Color.parseColor(mColors[randomNumber]))
                    .width(2));
            IconFactory iconFactory = IconFactory.getInstance(requireContext());
            Icon iconStart = iconFactory.fromResource(R.drawable.marcer_flag_start);
            Icon iconFinish = iconFactory.fromResource(R.drawable.marcer_flag_finish);
//            mapboxMap.addMarker(new MarkerViewOptions().icon(iconStart)
//                    .position(points.get(0))
//                    .title(getString(R.string.start)));
//            mapboxMap.addMarker(new MarkerViewOptions().icon(iconFinish)
//                    .position(points.get(points.size() - 1))
//                    .title(getString(R.string.finish)));
            mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(new LatLng(points.get(0).getLatitude(), points.get(0).getLongitude()))  // set the camera's center position
                            .zoom(12)  // set the camera's zoom level
                            .tilt(20)  // set the camera's tilt
                            .build()));
        }
    }

    private void setupMapBox(Bundle savedInstanceState) {

        binding.mapView.getMapAsync(this);
    }

    private void initDate() {
        mOfflineManager = OfflineManager.getInstance(requireContext());
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        mRootPathString = sharedPreferences.getString(ROOT_PATH, null);
        mUserLanguage = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString(SELECTED_LANGUAGE, EN);
        selectUserRouts = (List<Rout>) requireActivity().getIntent().getSerializableExtra(SELECTED_USER_ROUTS);
        selectUserPlacesList = (List<Place>) requireActivity().getIntent().getSerializableExtra(SELECTED_USER_PLACES);
        mTypeMode = requireActivity().getIntent().getBooleanExtra(PRODUCE_MODE, false);
        mOfflineRegionName = requireActivity().getIntent().getStringExtra(OFFLINE_MAP);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(getString(R.string.mapboxStyle), this::onStyleLoaded);

    }

    private void onStyleLoaded(Style style) {
        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {
            initMapLocale();
            initData();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(requireActivity());
        }
    }

    private void initMapLocale() {

    }

    @SuppressLint("MissingPermission")
    private void initializeLocationEngine() {
        viewModel.initMyLocation();
        viewModel.getMyLocation().observe(this, this::onUpdateLocation);
    }

    private void onUpdateLocation(Location location) {
        mapboxMap.getLocationComponent().forceLocationUpdate(location);
    }

    private void initData() {
        mPoint = new LatLng();
        mapboxMap.getUiSettings().setCompassGravity(Gravity.BOTTOM);
        mapboxMap.getUiSettings().setCompassMargins(20, 20, 20, 20);

        if (selectUserRouts != null && selectUserRouts.size() > 0 && mRootPathString != null) {
            for (Rout rout : selectUserRouts) {
                if (rout.getUrlRoutsTrack() != null) {
                    viewModel.drawGeoJson(Uri.parse(rout.getUrlRoutsTrack()).getPath());
                }
            }
        }
        if (selectUserPlacesList != null && selectUserPlacesList.size() > 0) {
            for (Place place : selectUserPlacesList) {
                mPointPlace = new LatLng();
                mPointPlace.setLatitude(place.getPositionPlace().getLatitude());
                mPointPlace.setLongitude(place.getPositionPlace().getLongitude());
                if (mPointPlace != null) {
                    IconFactory iconFactory = IconFactory.getInstance(requireContext());
                    Icon icon = iconFactory.fromResource(R.drawable.marcer);
//                    mapboxMap.addMarker(new MarkerViewOptions().icon(icon).position(mPointPlace)
//                            .title(place.getNamePlace(mUserLanguage)));
                }
            }
        }
        if (mOfflineRegionName != null) {
            mOfflineManager.listOfflineRegions(this);
        }
        if (mTypeMode || mPointPlace != null) {
            mPoint = mPointPlace;
        } else {
            mPoint.setLatitude(48.635022);
            mPoint.setLongitude(24.141080);
        }
        mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(mPoint)  // set the camera's center position
                        .zoom(10)  // set the camera's zoom level
                        .tilt(20)  // set the camera's tilt
                        .build()));
    }

    @SuppressLint("StringFormatInvalid")
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
        } else {
            if (mOfflinePolygon != null && mPolygonOptions != null) {
                mPolygonOptions = null;
                mapboxMap.removePolygon(mOfflinePolygon);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        binding.mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.mapView.onSaveInstanceState(outState);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 69 && grantResults.length > 0) {
//            for (int i = 0; i < permissions.length; i++) {
//                if (permissions[i].equals(ACCESS_FINE_LOCATION) && grantResults[i]
//                        == PackageManager.PERMISSION_GRANTED) {
//                    //  enabledGPS(true);
//                }
//            }
//        }
//    }

    @Override
    public void onDownloadRegion(String name, double value) {
        downloadOfflineRegion(name, value);
    }

    @Override
    public void onList(OfflineRegion[] offlineRegions) {
        if (offlineRegions == null || offlineRegions.length == 0) {
            Toast.makeText(requireContext(),
                    getString(R.string.toast_no_regions_yet), Toast.LENGTH_SHORT).show();
            return;
        }
        for (OfflineRegion offlineRegion : offlineRegions) {
            if (mOfflineRegionName.equals(getRegionName(offlineRegion))) {
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

                Toast.makeText(requireContext(),
                        mOfflineRegionName, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "Error: " + error);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionResult(boolean granted) {
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onFinish() {

    }
}