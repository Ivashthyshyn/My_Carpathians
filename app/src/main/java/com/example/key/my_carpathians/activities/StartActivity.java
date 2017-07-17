package com.example.key.my_carpathians.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.fragments.PlacesListFragment;
import com.example.key.my_carpathians.fragments.PlacesListFragment_;
import com.example.key.my_carpathians.fragments.RoutsListFragment;
import com.example.key.my_carpathians.fragments.RoutsListFragment_;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@EActivity
public class StartActivity extends AppCompatActivity {
    private boolean connected = false;
    private static final String TAG = "StartActivity";
    public static final String PREFS_NAME = "MyPrefsFile";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    public FragmentManager fragmentManager;
    public PlacesListFragment placesListFragment;
    public RoutsListFragment routsListFragment;
    public ArrayList<Place> places = new ArrayList<>();
    public ArrayList<Rout> routs = new ArrayList<>();
    public  AlertDialog.Builder builder;
    private Context context = StartActivity.this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_start);


        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {

                    showLoginDialog();
                }
                // ...
            }
        };

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();

        Query myPlace = myRef.child("Places");
        myPlace.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Place university = postSnapshot.getValue(Place.class);
                    places.add(university);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Query myRouts = myRef.child("Rout");
        myRouts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Rout rout = postSnapshot.getValue(Rout.class);
                    if (isOnline()) {
                        downloadRoutToStorage(rout.getUrlRoutsTrack(), rout.getNameRout());
                    }
                    routs.add(rout);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * This method is download and save routs track to SD card in package "Rout"
     * @param urlRoutsTrack is a file address in database Storage from downloading
     * @param nameRout is the file name that is written to SD card
     */
    @Background
    public void downloadRoutToStorage(String urlRoutsTrack,  String nameRout) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference httpsReference = storage.getReferenceFromUrl(urlRoutsTrack);
        String dsd =  httpsReference.getName();
        File rootPath = new File(Environment.getExternalStorageDirectory(), "Rout");
        if(!rootPath.exists()) {
            rootPath.mkdirs();
        }

        final File localFile = new File(rootPath, dsd );
        if (!localFile.exists()) {

        final URI fileUri = localFile.toURI();

        httpsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                SharedPreferences mSharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                mSharedPreferences.edit().putString(localFile.getName().toString(), fileUri.toString()).apply();

                Log.e("firebase ",";local tem file created  created " +localFile.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

                Log.e("firebase ",";local tem file not created  created " +exception.toString());
            }
        });
        }
    }


    @Click(R.id.buttonPlace)
    void buttonPlaceWasClicked(){

        fragmentManager = getSupportFragmentManager();
        if(placesListFragment != null) {
            fragmentManager.beginTransaction().remove(placesListFragment).commit();
        }
        placesListFragment = new PlacesListFragment_();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, placesListFragment);
        fragmentTransaction.commit();
        if(places != null) {
            placesListFragment.setList(places, routs);
        }
    }
    @Click(R.id.buttonRoutes)
    void buttonRoutesWasClicked(){
        fragmentManager = getSupportFragmentManager();
        if(routsListFragment != null) {
            fragmentManager.beginTransaction().remove(routsListFragment).commit();
        }
        routsListFragment = new RoutsListFragment_();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, routsListFragment);
        fragmentTransaction.commit();
        if(routs != null) {
            routsListFragment.setList(routs);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onBackPressed() {
        if ( placesListFragment != null){
            fragmentManager.beginTransaction().remove(placesListFragment).commit();
            placesListFragment = null;
        }else if (routsListFragment != null){
            fragmentManager.beginTransaction().remove(routsListFragment).commit();
            routsListFragment = null;
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Click(R.id.buttonSettings)
    public void buttonSettingsWasClicked(){
        startActivity(new Intent(StartActivity.this, SettingsActivity_.class));
    }

    void showLoginDialog(){
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Раді вітати вас у нашому додатку для людей які полюбляють активний відпочинок");
        builder.setMessage("Виберіть спосіб реєстраці");

        builder.setPositiveButton("Зареєструватися", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

                startActivity(new Intent(context, LoginActivity_.class));
            }
        });
        builder.setNegativeButton("Анонімний вхід", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                signInAnonymously();
            }
        });
        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                signInAnonymously();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(context, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
    public boolean isOnline() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) StartActivity.this
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

}
