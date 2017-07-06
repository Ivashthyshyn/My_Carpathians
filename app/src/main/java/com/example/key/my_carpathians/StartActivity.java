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

import com.example.key.my_carpathians.database.Place;
import com.example.key.my_carpathians.database.Rout;
import com.example.key.my_carpathians.login.LoginActivity_;
import com.example.key.my_carpathians.login.SettingsActivity_;
import com.google.android.gms.tasks.OnCompleteListener;
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
      /**
        Place pl = new Place();
        pl.setUrlPlace("https://firebasestorage.googleapis.com/v0/b/my-carpathians-1496328028184.appspot.com/o/placeImage%2FgoverlaWaterfall.jpg?alt=media&token=f39a13b9-7ef2-47ee-94a6-5b9726391ce5");
        pl.setNamePlace("Говерлянський водоспад");
        pl.setTitlePlace("Пру́тський водоспа́д (інша назва — Говерля́нський водоспа́д) — каскадний водоспад в Українських Карпатах (масив Чорногора), на річці Прут. Розташований на півдні Надвірнянського району Івано-Франківської області, на схід від вершини Говерли.\n" +
                "\n" +
                "Водоспад має шість каскадів, висота найбільшого — 12 м. Загальна висота падіння води — 80 м.\n" +
                "\n" +
                "Розташований між північно-східними відногами гір Говерли та Брескул, на краю льодовикового кару, в якому бере початок Прут.\n" +
                "\n" +
                "Водоспад — популярний туристичний об'єкт, пам'ятка природи. Неподалік від нього проходить стежка, що веде на вершину Говерли. Також є стежка вздовж самого потоку.");
*/
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
        /**
        Rout r = new Rout();
        r.setNameRout("Хата - Станькова");
        r.setTitleRout("Дуже важкий маршрут. Проходить по дорозі білямоєї хати. Потрібно оминати вибоїни на асвальті і міни закладені коровами, яких місцеве населення переганяє на пашу по цій дорозі. Також є небезпека заблукати");
        r.setUrlRout("jfjdgk");
        r.setUrlRoutsTrack("kgfkdjg");
        r.setRoutsLevel(1);

        myRef.child("Rout").child("Хата - Станькова").setValue(r);
         */
        Query myRouts = myRef.child("Rout");
        myRouts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Rout rout = postSnapshot.getValue(Rout.class);
                    routs.add(rout);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
            placesListFragment.setList(places);
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
