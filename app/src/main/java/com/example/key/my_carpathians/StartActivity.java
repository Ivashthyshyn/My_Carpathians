package com.example.key.my_carpathians;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.example.key.my_carpathians.database.Places;
import com.example.key.my_carpathians.database.Routs;
import com.example.key.my_carpathians.login.LoginActivity_;
import com.example.key.my_carpathians.login.SettingsActivity_;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@EActivity
public class StartActivity extends AppCompatActivity {
    private static final String TAG = "StartActivity";
    public static final int TYPE_OF_LIST_PLACE = 1;
    public static final int TYPE_OF_LIST_ROUTS = 2;
    public static final String PREFS_NAME = "MyPrefsFile";
    private String userUid;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    public FragmentManager fragmentManager;
    public ListFragment listFragment;
    public ArrayList<Places> places = new ArrayList<>();
    public ArrayList<Routs> routs = new ArrayList<>();
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
                    userUid = user.getUid();

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
        myPlace.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Places place = new Places();
                   place = data.getValue(Places.class);
                    places.add(place);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
            // TODO: implement the ChildEventListener methods as documented above
            // ...
        });


        Query myRouts = myRef.child("Routs");
        myRouts.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Routs rout = new Routs();
                    rout = data.getValue(Routs.class);
                    routs.add(rout);
                }
            }


            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Click(R.id.buttonPlace)
    void buttonPlaceWasClicked(){

        fragmentManager = getSupportFragmentManager();
        if(listFragment != null) {
            fragmentManager.beginTransaction().remove(listFragment).commit();
        }
        listFragment = new ListFragment_();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, listFragment);
        fragmentTransaction.commit();
        listFragment.setList(places,TYPE_OF_LIST_PLACE);

    }
    @Click(R.id.buttonRoutes)
    void buttonRoutesWasClicked(){
        fragmentManager = getSupportFragmentManager();
        if(listFragment != null) {
            fragmentManager.beginTransaction().remove(listFragment).commit();
        }
        listFragment = new ListFragment_();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, listFragment);
        fragmentTransaction.commit();
        listFragment.setList(routs,TYPE_OF_LIST_ROUTS);
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
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
                            FirebaseUser user = mAuth.getCurrentUser();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(context, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }


}
