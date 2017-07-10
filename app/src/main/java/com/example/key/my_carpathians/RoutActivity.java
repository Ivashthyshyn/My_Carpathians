package com.example.key.my_carpathians;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.key.my_carpathians.database.Rout;
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
import java.util.HashSet;
import java.util.Set;

import static com.example.key.my_carpathians.PlaceActivity.GEOJSON_ROUT;
import static com.example.key.my_carpathians.PlaceActivity.LATITUDE;
import static com.example.key.my_carpathians.PlaceActivity.LONGITUDE;
import static com.example.key.my_carpathians.RoutsRecyclerAdapter.RoutsViewHolder.PUT_EXTRA_ROUT;
import static com.example.key.my_carpathians.StartActivity.PREFS_NAME;

@EActivity
public class RoutActivity extends AppCompatActivity {
    private boolean connected = false;
    private static final String TEMPORARY_FILE = "fileToView";
    private static final String PREFS_LIST_ROUTS_NAME = "listOfRoutsMame";
    private File localFile;
    private Rout mRoutClass;
    private URI fileUri;
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
                downloadFile(mRoutClass.getUrlRoutsTrack(),TEMPORARY_FILE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean isOnline() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) RoutActivity.this
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

    @Click(R.id.buttonShowRoutOnMap)
    public void buttonShowRoutOnMapWasClicked(){


        Intent mapIntent = new Intent(RoutActivity.this,MapsActivity_.class);
        mapIntent.putExtra(LONGITUDE, mRoutClass.getPositionRout().getLongitude());
        mapIntent.putExtra(LATITUDE, mRoutClass.getPositionRout().getLatitude());
        mapIntent.putExtra(GEOJSON_ROUT, fileUri.getPath().toString());
        startActivity(mapIntent);
    }

    @Click(R.id.buttonDownloadRout)
    public void buttonDownloadRoutWasClicked(){
        if (isOnline()) {
            downloadFile(mRoutClass.getUrlRoutsTrack(), mRoutClass.getNameRout());
        }else {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(RoutActivity.this);

            // Setting Dialog Title
            alertDialog.setTitle("Attention!");

            // Setting Dialog Message
            alertDialog.setMessage("Sorry, but you need an internet connection to download a route. After downloading the file, you can use it offline ");

            // On pressing Settings button
            alertDialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    dialog.cancel();
                }
            });

            // Showing Alert Message
            alertDialog.show();

        }
    }

    private void downloadFile(String uriRout, String fileName) {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference httpsReference = storage.getReferenceFromUrl(uriRout);

        File rootPath = new File(Environment.getExternalStorageDirectory(), "Rout");
        if(!rootPath.exists()) {
            rootPath.mkdirs();
        }

        localFile = new File(rootPath, fileName );
        fileUri = localFile.toURI();

        httpsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.e("firebase ",";local tem file created  created " +localFile.toString());

               if (!localFile.getName().equals(TEMPORARY_FILE)){
                   SharedPreferences mSharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                   mUserNameList = mSharedPreferences.getStringSet(PREFS_LIST_ROUTS_NAME,new HashSet<String>());
                   mUserNameList.add(localFile.getName());
                   mSharedPreferences.edit().putStringSet(PREFS_LIST_ROUTS_NAME, mUserNameList).apply();
               }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("firebase ",";local tem file not created  created " +exception.toString());
            }
        });
    }



}
