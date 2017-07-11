package com.example.key.my_carpathians;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.key.my_carpathians.database.Place;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import static com.example.key.my_carpathians.PlacesRecyclerAdapter.ViewHolder.PUT_EXTRA_PLASE;
import static com.example.key.my_carpathians.StartActivity.PREFS_NAME;

@EActivity
public class PlaceActivity extends AppCompatActivity {


    private boolean connected = false;
    public static final String GEOJSON_ROUT = "geojson_rout";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

    Place myPlace;
    @ViewById(R.id.imageView)
    ImageView imageView;
    @ViewById(R.id.textName)
    TextView textName;
    @ViewById(R.id.titleText)
    TextView titleText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        String dataKey = getIntent().getStringExtra(PUT_EXTRA_PLASE);

        Query myPlaces = myRef.child("Places").child(dataKey);
        myPlaces.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              myPlace = dataSnapshot.getValue(Place.class);
                Glide
                        .with(getApplicationContext())
                        .load(myPlace.getUrlPlace())
                        .into(imageView);
                textName.setText(myPlace.getNamePlace());
                titleText.setText(myPlace.getTitlePlace());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public boolean isOnline() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) PlaceActivity.this
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();
            return connected;


        } catch (Exception e) {
            System.out.println("CheckConnectivity Exception: " + e.getMessage());
            Log.v("connectivity", e.toString());
        }
        return connected;
    }

    @Click(R.id.buttonShowPlaceOnMap)
    public void buttonShowPlaceOnMapWasClicked(){
        Intent mapIntent = new Intent(PlaceActivity.this,MapsActivity_.class);
        mapIntent.putExtra(LONGITUDE, myPlace.getPositionPlace().getLongitude());
        mapIntent.putExtra(LATITUDE, myPlace.getPositionPlace().getLatitude());
        SharedPreferences mSharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        mSharedPreferences.edit().putString(GEOJSON_ROUT, null).apply();
        startActivity(mapIntent);
    }

    @Click(R.id.buttonShowWalkRout)
    public void buttonShowWalkRoutWasClicked(){

    }

    @Click(R.id.buttonPlacesAruond)
    void buttonPlacesAruondWasClicked(){
        List<Place> placeList = (List<Place>) getIntent().getSerializableExtra("fdfd");
        List<Place> placesAround = new ArrayList<>();
        List<String> placesAroudName = new ArrayList<>();
        for (int i =0;i < placeList.size();i++){
            Place mPlace =  placeList.get(i);
            double lat = mPlace.getPositionPlace().getLatitude();
            double lng = mPlace.getPositionPlace().getLongitude();
            if ( myPlace.getPositionPlace().getLongitude() + 0.4 > lng
                    && myPlace.getPositionPlace().getLongitude() - 0.4 < lng
                    && myPlace.getPositionPlace().getLatitude() + 0.3 > lat
                    && myPlace.getPositionPlace().getLatitude() - 0.3 < lat
                    && !myPlace.getNamePlace().equals(mPlace.getNamePlace())){
                placesAround.add(mPlace);
                placesAroudName.add(mPlace.getNamePlace());
            }
        }
        if (placesAround.size() != 0 ) {
            showListDialog(placesAroudName);
        }else {
            Toast.makeText(PlaceActivity.this, "No place around ", Toast.LENGTH_SHORT).show();
        }
    }

    private void showListDialog(final List<String> placesAround) {
        final boolean[] mCheckedItems = new boolean[placesAround.size()];
        final CharSequence[] items = placesAround.toArray(new CharSequence[placesAround.size()]);
        AlertDialog dialog = new AlertDialog.Builder(PlaceActivity.this)
                .setTitle(getString(R.string.navigate_title))
                .setMultiChoiceItems(items, mCheckedItems,  new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which, boolean isChecked) {
                        mCheckedItems[which] = isChecked;
                    }
                })
                .setPositiveButton("Show on map", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        for (int i = 0; i < items.length; i++) {
                            if (!mCheckedItems[i]){
                                placesAround.remove(i);
                                placesAround.size();
                            }

                        }

                    }
                })

                .setNegativeButton("All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // When the user cancels, don't do anything.
                        // The dialog will automatically close
                    }
                }).create();
        dialog.show();
    }


}
