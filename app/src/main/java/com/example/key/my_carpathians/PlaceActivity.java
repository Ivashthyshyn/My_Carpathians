package com.example.key.my_carpathians;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.key.my_carpathians.database.Place;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.net.URI;

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
    File localFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);
        downloadFile();
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
        startActivity(mapIntent);
    }

    @Click(R.id.buttonShowWalkRout)
    public void buttonShowWalkRoutWasClicked(){

    }

    private void downloadFile() {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference httpsReference = storage.getReferenceFromUrl("gs://my-carpathians-1496328028184.appspot.com/geojson/NaGoverlu.geojson");

        File rootPath = new File(Environment.getExternalStorageDirectory(), "Rout");
        if(!rootPath.exists()) {
            rootPath.mkdirs();
        }

        localFile = new File(rootPath,"goverla.geojson");
        final URI fileUri = localFile.toURI();

        httpsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.e("firebase ",";local tem file created  created " +localFile.toString());
                SharedPreferences mSharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                mSharedPreferences.edit().putString(GEOJSON_ROUT, fileUri.getPath().toString()).apply();
                //  updateDb(timestamp,localFile.toString(),position);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("firebase ",";local tem file not created  created " +exception.toString());
            }
        });
    }

}
