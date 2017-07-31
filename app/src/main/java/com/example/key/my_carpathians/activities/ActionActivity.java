package com.example.key.my_carpathians.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.mapbox.services.api.utils.turf.TurfConstants;
import com.mapbox.services.api.utils.turf.TurfMeasurement;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.commons.utils.TextUtils;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.widget.Toast.LENGTH_LONG;
import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
import static com.example.key.my_carpathians.R.id.graph;
import static com.example.key.my_carpathians.activities.MapsActivity.PERIMETER_SIZE_TO_LATITUDE;
import static com.example.key.my_carpathians.activities.MapsActivity.PERIMETER_SIZE_TO_LONGITUDE;
import static com.example.key.my_carpathians.activities.StartActivity.FAVORITES_PLACE_LIST;
import static com.example.key.my_carpathians.activities.StartActivity.FAVORITES_ROUTS_LIST;
import static com.example.key.my_carpathians.activities.StartActivity.PREFS_NAME;
import static com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter.PLACE_LIST;
import static com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter.ROUTS_LIST;
import static com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter.ViewHolder.PUT_EXTRA_PLASE;
import static com.example.key.my_carpathians.adapters.RoutsRecyclerAdapter.RoutsViewHolder.PUT_EXTRA_ROUT;

@EActivity
public class ActionActivity extends AppCompatActivity {

    public static final String SELECTED_USER_ROUTS = "selected-user_routs";
    public static final String SELECTED_USER_PLACES = "selected_user_places";
    public List<Rout> routList;
    public List<Place> placeList;
    public Place myPlace;
    public Rout myRout;
    public com.example.key.my_carpathians.models.Position myPosition;
    public String myName;
    public boolean inAlertDialogCheckedSomething = false;
    public ArrayList<String> selectedUserRouts = new ArrayList<>();
    public ArrayList<String> selectedUserPlacesStringList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private ArrayList<Place> selectedUserPlacesList = new ArrayList<>();

    AlertDialog alertDialog;
    @ViewById(R.id.imageView)
    ImageView imageView;
    @ViewById(R.id.textName)
    TextView textName;
    @ViewById(R.id.titleText)
    TextView titleText;
    @ViewById(graph)
    GraphView graphView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);
        sharedPreferences = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        routList = (List<Rout>) getIntent().getSerializableExtra(ROUTS_LIST);
        placeList = (List<Place>) getIntent().getSerializableExtra(PLACE_LIST);
        myPlace = (Place) getIntent().getSerializableExtra(PUT_EXTRA_PLASE);
        myRout = (Rout) getIntent().getSerializableExtra(PUT_EXTRA_ROUT);
        if (myPlace != null) {
            textName.setText(myPlace.getNamePlace());
            titleText.setText(myPlace.getTitlePlace());
            graphView.setVisibility(View.GONE);
            Glide
                    .with(ActionActivity.this)
                    .load(myPlace.getUrlPlace())
                    .into(imageView);
            myPosition = myPlace.getPositionPlace();
            myName = myPlace.getNamePlace();
        } else if (myRout != null) {
            textName.setText(myRout.getNameRout());
            titleText.setText(myRout.getTitleRout());
            imageView.setVisibility(View.GONE);
            createDataPoint(myRout.getNameRout());
            myPosition = myRout.getPositionRout();
            myName = myRout.getNameRout();
        }

    }

    private void createDataPoint(String nameRout) {
        List<Position> points = new ArrayList<>();
        URI mUri = URI.create(getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(nameRout, null));
        try {
            // Load GeoJSON file
            File file = new File(mUri);
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
                        Position position = Position.fromCoordinates(coord.getDouble(1), coord.getDouble(0), coord.getDouble(2));
                        points.add(position);
                    }
                }
            }
        } catch (Exception exception) {
            Log.e(TAG, "Exception Loading GeoJSON: " + exception.toString());
        }
        int size = points.size();
        DataPoint[] values = new DataPoint[size];
        Integer yi = 0;
        for (int i=1; i<size; i++) {
            Integer xi = (int)points.get(i).getAltitude();
            yi = yi + (int)TurfMeasurement.distance(points.get(i -1),points.get(i),TurfConstants.UNIT_METERS);
            DataPoint v = new DataPoint(xi, yi);
            values[i] = v;
        }
        values[0] = new DataPoint((int)points.get(0).getAltitude(),0);
        LineGraphSeries series = new LineGraphSeries<DataPoint>(values);
        graphView.addSeries(series);

    }

    @Click(R.id.buttonShowPlaceOnMap)
    public void buttonShowPlaceOnMapWasClicked() {
        if (myPlace != null) {
            selectedUserPlacesList.add(myPlace);
        }
        if (myRout != null) {
            selectedUserRouts.add(myRout.getNameRout());
        }
        Intent mapIntent = new Intent(ActionActivity.this, MapsActivity_.class);
        mapIntent.putExtra(SELECTED_USER_PLACES, selectedUserPlacesList);
        mapIntent.putStringArrayListExtra(SELECTED_USER_ROUTS, selectedUserRouts);
        startActivity(mapIntent);
    }

    @Click(R.id.buttonShowAroundRout)
    public void buttonShowAroundRoutWasClicked() {
        List<Rout> routsAround = new ArrayList<>();
        List<String> routsAroundName = new ArrayList<>();
        for (int i = 0; i < routList.size(); i++) {
            Rout mRout = routList.get(i);
            double lat = mRout.getPositionRout().getLatitude();
            double lng = mRout.getPositionRout().getLongitude();
            if (myPosition.getLongitude() + PERIMETER_SIZE_TO_LONGITUDE > lng
                    && myPosition.getLongitude() - PERIMETER_SIZE_TO_LONGITUDE < lng
                    && myPosition.getLatitude() + PERIMETER_SIZE_TO_LATITUDE > lat
                    && myPosition.getLatitude() - PERIMETER_SIZE_TO_LATITUDE < lat
                    && !myName.equals(mRout.getNameRout())) {
                routsAround.add(mRout);
                routsAroundName.add(mRout.getNameRout());
            }
        }
        if (routsAround.size() != 0) {
            showListDialogForRouts(routsAroundName, routsAround);
        } else {
            Toast.makeText(ActionActivity.this, "No place around ", Toast.LENGTH_SHORT).show();
        }
    }

    @Click(R.id.buttonPlacesAruond)
    void buttonPlacesAroundWasClicked() {
        List<Place> placesAround = new ArrayList<>();
        List<String> placesAroundName = new ArrayList<>();
        for (int i = 0; i < placeList.size(); i++) {
            Place mPlace = placeList.get(i);
            double lat = mPlace.getPositionPlace().getLatitude();
            double lng = mPlace.getPositionPlace().getLongitude();
            if (myPosition.getLongitude() + PERIMETER_SIZE_TO_LONGITUDE > lng
                    && myPosition.getLongitude() - PERIMETER_SIZE_TO_LONGITUDE < lng
                    && myPosition.getLatitude() + PERIMETER_SIZE_TO_LATITUDE > lat
                    && myPosition.getLatitude() - PERIMETER_SIZE_TO_LATITUDE < lat
                    && !myName.equals(mPlace.getNamePlace())) {
                placesAround.add(mPlace);
                placesAroundName.add(mPlace.getNamePlace());
            }
        }
        if (placesAround.size() != 0) {
            showListDialogForPlaces(placesAroundName, placesAround);
        } else {
            Toast.makeText(ActionActivity.this, "No place around ", Toast.LENGTH_SHORT).show();
        }
    }

    private void showListDialogForPlaces(final List<String> stringList, final List<Place> objectList) {
        final boolean[] mCheckedItems = new boolean[stringList.size()];
        final CharSequence[] items = stringList.toArray(new CharSequence[stringList.size()]);
        AlertDialog.Builder dialog = new AlertDialog.Builder(ActionActivity.this)
                .setTitle(getString(R.string.navigate_title))
                .setMultiChoiceItems(items, mCheckedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which, boolean isChecked) {
                        if (isChecked) {
                            inAlertDialogCheckedSomething = true;
                        }
                        mCheckedItems[which] = isChecked;
                    }
                })
                .setPositiveButton("Додати до карти", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })

                .setNegativeButton("Ні", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        alertDialog = dialog.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inAlertDialogCheckedSomething) {
                    for (int i = 0; i < items.length; i++) {
                        if (!mCheckedItems[i]) {
                            objectList.remove(i);
                            stringList.remove(i);
                        }
                    }
                    selectedUserPlacesList = (ArrayList<Place>) objectList;
                    selectedUserPlacesStringList = (ArrayList<String>) stringList;

                    inAlertDialogCheckedSomething = false;
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(ActionActivity.this, "Please selected something",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void showListDialogForRouts(final List<String> stringList, final List<Rout> objectList) {
        final boolean[] mCheckedItems = new boolean[stringList.size()];
        final CharSequence[] items = stringList.toArray(new CharSequence[stringList.size()]);

        AlertDialog.Builder dialog = new AlertDialog.Builder(ActionActivity.this)
                .setTitle(getString(R.string.navigate_title))
                .setMultiChoiceItems(items, mCheckedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which, boolean isChecked) {
                        if (isChecked) {
                            inAlertDialogCheckedSomething = true;
                        }
                        mCheckedItems[which] = isChecked;
                    }
                })
                .setPositiveButton("Додати до карти", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })

                .setNegativeButton("Детальніше", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        alertDialog = dialog.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inAlertDialogCheckedSomething) {
                    for (int i = 0; i < items.length; i++) {
                        if (mCheckedItems[i]) {
                            selectedUserRouts.add(stringList.get(i));
                        }
                        inAlertDialogCheckedSomething = false;
                        alertDialog.dismiss();
                    }
                } else {
                    Toast.makeText(ActionActivity.this, "Please selected something",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inAlertDialogCheckedSomething) {
                    for (int i = 0; i < items.length; i++) {
                        if (mCheckedItems[i]) {
                            selectedUserRouts.add(stringList.get(i));
                        }
                        inAlertDialogCheckedSomething = false;
                        alertDialog.dismiss();
                    }
                } else {
                    Toast.makeText(ActionActivity.this, "Please selected something",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        selectedUserRouts.clear();
        selectedUserPlacesStringList.clear();
        selectedUserPlacesList.clear();
    }

    @Click(R.id.buttonAddToFavorites)
    void buttonAddToFavoritesWasClicked() {
        if (myPlace != null) {
            selectedUserPlacesStringList.add(myPlace.getNamePlace());
        }
        if (myRout != null) {
            selectedUserRouts.add(myRout.getNameRout());
        }
        Set<String> favoritesPlacesList = new HashSet<>(sharedPreferences.getStringSet(FAVORITES_PLACE_LIST, new HashSet<String>()));
        favoritesPlacesList.addAll(selectedUserPlacesStringList);
        Set<String> favoritesRoutsList = new HashSet<>(sharedPreferences.getStringSet(FAVORITES_ROUTS_LIST, new HashSet<String>()));
        favoritesRoutsList.addAll(selectedUserRouts);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(FAVORITES_PLACE_LIST, favoritesPlacesList);
        editor.putStringSet(FAVORITES_ROUTS_LIST, favoritesRoutsList);
        editor.apply();

        Toast.makeText(ActionActivity.this, " Add to favorites", LENGTH_LONG).show();

    }

}
