package com.example.key.my_carpathians;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

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

import static com.example.key.my_carpathians.PlacesRecyclerAdapter.ViewHolder.PUT_EXTRA_PLASE;
import static com.example.key.my_carpathians.StartActivity.PREFS_NAME;

@EActivity
public class PlaceActivity extends AppCompatActivity {

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


}
