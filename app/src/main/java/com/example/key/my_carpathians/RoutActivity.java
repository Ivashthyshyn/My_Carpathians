package com.example.key.my_carpathians;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.key.my_carpathians.database.Rout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import static com.example.key.my_carpathians.PlaceActivity.LATITUDE;
import static com.example.key.my_carpathians.PlaceActivity.LONGITUDE;
import static com.example.key.my_carpathians.RoutsRecyclerAdapter.RoutsViewHolder.PUT_EXTRA_ROUT;

@EActivity
public class RoutActivity extends AppCompatActivity {

    private Rout mRoutClass;
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
        startActivity(mapIntent);
    }


}
