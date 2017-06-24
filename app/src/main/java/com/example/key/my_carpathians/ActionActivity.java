package com.example.key.my_carpathians;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.key.my_carpathians.database.Places;
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

import static com.example.key.my_carpathians.StartActivity.PREFS_NAME;

@EActivity
public class ActionActivity extends AppCompatActivity {

    public static final String GEOJSON_ROUT = "geojson_rout";
    Places  myPlace;
    @ViewById(R.id.imageView)
    ImageView imageView;
    @ViewById(R.id.textName)
    TextView textName;
    @ViewById(R.id.titleText)
    TextView titleText;
    @ViewById(R.id.buttonShowOnMap)
    Button buttonShowOnMap;
    File localFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);
        downloadFile();




        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        String plaseName = getIntent().getStringExtra("placeName");

        Query myPlaces = myRef.child("Places").child("Plase").child(plaseName);
        myPlaces.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              myPlace = dataSnapshot.getValue(Places.class);
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
    @Click(R.id.buttonShowOnMap)
    public void showOnMap(){
        Intent mapIntent = new Intent(ActionActivity.this,MapsActivity_.class);
        startActivity(mapIntent);
    }


    private void downloadFile() {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference httpsReference = storage.getReferenceFromUrl("gs://my-carpathians-1496328028184.appspot.com/geojson/vylky.geojson");

        File rootPath = new File(Environment.getExternalStorageDirectory(), "Routs");
        if(!rootPath.exists()) {
            rootPath.mkdirs();
        }

        final File localFile = new File(rootPath,"vylky.geojson");
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
