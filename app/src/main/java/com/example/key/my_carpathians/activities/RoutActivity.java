package com.example.key.my_carpathians.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.models.Rout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.HashSet;
import java.util.Set;

import static com.example.key.my_carpathians.activities.PlaceActivity.GEOJSON_ROUT;
import static com.example.key.my_carpathians.activities.PlaceActivity.LATITUDE;
import static com.example.key.my_carpathians.activities.PlaceActivity.LONGITUDE;
import static com.example.key.my_carpathians.activities.StartActivity.PREFS_NAME;
import static com.example.key.my_carpathians.adapters.RoutsRecyclerAdapter.RoutsViewHolder.PUT_EXTRA_ROUT;

@EActivity
public class RoutActivity extends AppCompatActivity {

    private static final String PREFS_LIST_ROUTS_NAME = "listOfRoutsMame";
    private Rout mRoutClass;
    private Set<String> mUserNameList;
    @ViewById(R.id.textRoutTitle)
    TextView textRoutTitle;
    @ViewById(R.id.textRoutName)
    TextView textRoutName;
    @ViewById(R.id.imageRoutView)
    ImageView imageRoutView;
    @ViewById(R.id.buttonAnotherRouts)
    Button buttonAnotherRouts;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rout);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        String dataKey = getIntent().getStringExtra(PUT_EXTRA_ROUT);
        Query mRout = myRef.child("Rout").child(dataKey);
        mRout.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mRoutClass = dataSnapshot.getValue(Rout.class);
                Glide
                        .with(getApplicationContext())
                        .load(mRoutClass.getUrlRout())
                        .into(imageRoutView);
                textRoutName.setText(mRoutClass.getNameRout());
                textRoutTitle.setText(mRoutClass.getTitleRout());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    @Click(R.id.buttonShowRoutOnMap)
    public void buttonShowRoutOnMapWasClicked(){
        Intent mapIntent = new Intent(RoutActivity.this,MapsActivity_.class);
        mapIntent.putExtra(LONGITUDE, mRoutClass.getPositionRout().getLongitude());
        mapIntent.putExtra(LATITUDE, mRoutClass.getPositionRout().getLatitude());
        mapIntent.putExtra(GEOJSON_ROUT, mRoutClass.getNameRout());
        startActivity(mapIntent);
    }

    @Click(R.id.buttonDownloadRout)
    public void buttonDownloadRoutWasClicked(){
        SharedPreferences mSharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        mUserNameList = mSharedPreferences.getStringSet(PREFS_LIST_ROUTS_NAME,new HashSet<String>());
        mUserNameList.add(mRoutClass.getNameRout());
        mSharedPreferences.edit().putStringSet(PREFS_LIST_ROUTS_NAME, mUserNameList).apply();
    }

}
