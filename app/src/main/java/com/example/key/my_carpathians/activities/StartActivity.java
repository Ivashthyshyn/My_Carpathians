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
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.fragments.MyFavoritesFragment;
import com.example.key.my_carpathians.fragments.PlacesListFragment;
import com.example.key.my_carpathians.fragments.PlacesListFragment_;
import com.example.key.my_carpathians.fragments.RoutsListFragment;
import com.example.key.my_carpathians.fragments.RoutsListFragment_;
import com.example.key.my_carpathians.interfaces.Comunicator;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
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
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.fabric.sdk.android.Fabric;

import static com.example.key.my_carpathians.activities.MapsActivity.REC_MODE;
import static com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter.ViewHolder.PUT_EXTRA_PLACE;
import static com.example.key.my_carpathians.adapters.RoutsRecyclerAdapter.RoutsViewHolder.PUT_EXTRA_ROUT;
import static com.example.key.my_carpathians.utils.LocationService.CREATED_BY_USER_TRACK_LIST;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with mFirebaseUser interaction.
 */
@EActivity
public class StartActivity extends AppCompatActivity implements Comunicator,
        GoogleApiClient.OnConnectionFailedListener {
    private static final int RC_SIGN_IN = 9001;
    public static final String FAVORITES_ROUTS_LIST = "favorites_user_routs";
    public static final String FAVORITES_PLACE_LIST = "favorites_user_places";
    public static final String ACTION_MODE = "action_mode";
    public FragmentManager fragmentManager;
    public PlacesListFragment placesListFragment;
    public RoutsListFragment routsListFragment;
    public MyFavoritesFragment myFavoritesFragment;
    public ArrayList<Place> places = new ArrayList<>();
    public ArrayList<Rout> routs = new ArrayList<>();
    public  AlertDialog.Builder builder;
    private boolean connected = false;
    private static final String TAG = "StartActivity";
    public static final String PREFS_NAME = "MyPrefsFile";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Context context = StartActivity.this;
    private SharedPreferences  mSharedPreferences;
    private DrawerLayout mDrawerLayout;
    private ListView mListIthemView;
    private  FirebaseUser mFirebaseUser;
    private CallbackManager mCallbackManager;




    @ViewById(R.id.userAcountImage)
    public ImageView userAccountImage;

    @ViewById(R.id.facebokLoginButton)
    public Button facebookLogin;

    @ViewById(R.id.googleLoginButton)
    public Button googleLoginButton;

    @ViewById(R.id.googleButton)
    SignInButton googleButton;

   // @ViewById(R.id.greeting)
    TextView greeting;

    @ViewById(R.id.facebookButton)
    LoginButton loginFacebookButton;

    @ViewById(R.id.email)
    EditText inputEmail;

    @ViewById(R.id.password)
    EditText inputPassword;

    @ViewById(R.id.progressBar)
    ProgressBar progressBar;

    @ViewById( R.id.emailLoginButton)
    Button emailLoginButton;

    @ViewById(R.id.inputEmailLayout)
    TextInputLayout inputEmailLayout;

   @ViewById(R.id.inputPasswordLayout)
    TextInputLayout inputPasswordLayout;

    @ViewById(R.id.buttonResetPassword)
    Button buttonResetPassword;

    @ViewById(R.id.buttonCreateNewAccount)
    Button buttonCreateNewAccount;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_start);
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mListIthemView = (ListView)findViewById(R.id.lst_menu_items);
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                loginGoogle();

                if (mFirebaseUser == null ) {
                    showLoginGroup();

                }else if(mFirebaseUser.getProviders() == null || mFirebaseUser.getProviders().size() == 0){
                    List<String> dd =  mFirebaseUser.getProviders();
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(StartActivity.this);

                    // Setting Dialog Title
                    alertDialog.setTitle("Authentication");

                    // Setting Dialog Message
                    alertDialog.setMessage("You need to login to access the settings");

                    // On pressing Settings button
                    alertDialog.setPositiveButton("Login", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int which) {
                         /**   mFirebaseUser.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(StartActivity.this, "Your profile is deleted:( Create a account now!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(StartActivity.this, SignupActivity.class));
                                                finish();
                                              //  progressBar.setVisibility(View.GONE);
                                            } else {
                                                Toast.makeText(StartActivity.this, "Failed to delete your account!", Toast.LENGTH_SHORT).show();
                                             //   progressBar.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                          */
                            signOut();

                            showLoginGroup();
                        }
                    });

                    // on pressing cancel button
                    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mDrawerLayout.closeDrawer(Gravity.LEFT);
                            dialog.cancel();
                        }
                    });

                    // Showing Alert Message
                    alertDialog.show();


                }else if (mFirebaseUser.getProviders().size() > 0){
                   showSiginAutButton();
                }


            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        mListIthemView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int pos, long id){


            }
        });
        final String[] data ={"one","two","three"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
        mListIthemView.setAdapter(adapter);
        mSharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFirebaseUser = firebaseAuth.getCurrentUser();
                if (mFirebaseUser != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + mFirebaseUser.getUid());
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

    private void showSiginAutButton() {

    }

    private void showLoginGroup() {
       facebookLogin.setVisibility(View.VISIBLE);
       googleLoginButton.setVisibility(View.VISIBLE);

    }

    private void signOut() {
        mAuth.signOut();
    }


    /**
     * This method is download and save routs track to SD card in package "Rout"
     * @param urlRoutsTrack is a file address in database Storage from downloading
     * @param nameRout is the file name that is written to SD card
     */
    @Background
    public void downloadRoutToStorage(String urlRoutsTrack,  final String nameRout) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference httpsReference = storage.getReferenceFromUrl(urlRoutsTrack);
        String nameFileInStorage =  httpsReference.getName();
        File rootPath = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOWNLOADS), "Routs");
        if(!rootPath.exists()) {
            rootPath.mkdirs();
        }

        final File localFile = new File(rootPath, nameFileInStorage );
        if (!localFile.exists()) {

        final URI fileUri = localFile.toURI();

        httpsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                mSharedPreferences.edit().putString(nameRout, fileUri.toString()).apply();

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
        placesListFragment = new PlacesListFragment_();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.add(R.id.drawer_layout, placesListFragment);
        fragmentTransaction.commit();
        if(places != null) {
            placesListFragment.setList(places, routs);
        }
    }
    @Click(R.id.buttonRoutes)
    void buttonRoutesWasClicked(){
        fragmentManager = getSupportFragmentManager();
        routsListFragment = new RoutsListFragment_();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.add(R.id.drawer_layout, routsListFragment);
        fragmentTransaction.commit();
        if(routs != null) {
            routsListFragment.setList(routs, places);
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
        }else if (routsListFragment != null) {
            fragmentManager.beginTransaction().remove(routsListFragment).commit();
            routsListFragment = null;
        }else if(myFavoritesFragment != null ){
            fragmentManager.beginTransaction().remove(myFavoritesFragment).commit();
            myFavoritesFragment = null;
        }else {
            super.onBackPressed();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
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

                mDrawerLayout.openDrawer(Gravity.LEFT);
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
                            // Sign in success, update UI with the signed-in mFirebaseUser's information
                            Log.d(TAG, "signInAnonymously:success");

                        } else {
                            // If sign in fails, display a message to the mFirebaseUser.
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
    @Click(R.id.buttonMyFavorites)
    void buttonMyFavoritesWasClicked(){
        mSharedPreferences = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> favoritesPlacesList =  mSharedPreferences.getStringSet(FAVORITES_PLACE_LIST, null);
        Set<String> favoritesRoutsList =  mSharedPreferences.getStringSet(FAVORITES_ROUTS_LIST, null);
        Set<String> createdByUserTrackList =  mSharedPreferences.getStringSet(CREATED_BY_USER_TRACK_LIST, null);
        myFavoritesFragment = new MyFavoritesFragment();
        fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.add(R.id.drawer_layout, myFavoritesFragment);
        fragmentTransaction.commit();
        if (favoritesPlacesList != null ) {
            myFavoritesFragment.setList(favoritesPlacesList, FAVORITES_PLACE_LIST);
        }
        if( favoritesRoutsList != null ){
            myFavoritesFragment.setList(favoritesRoutsList, FAVORITES_ROUTS_LIST);
        }
        if(createdByUserTrackList != null ){
            myFavoritesFragment.setList(createdByUserTrackList, CREATED_BY_USER_TRACK_LIST);
        }
    }

    @Override
    public void putStringNameRout(String name) {
        for (int i = 0; i < routs.size(); i++){
            if (routs.get(i).getNameRout().equals(name)){
                Intent intentActionActivity = new Intent(context, ActionActivity_.class);
                intentActionActivity.putExtra(PUT_EXTRA_ROUT, routs.get(i));
                startActivity(intentActionActivity);
            }
        }
    }

    @Override
    public void putStringNamePlace(String name) {
        for (int i = 0; i < places.size(); i++){
            if (places.get(i).getNamePlace().equals(name)){
                Intent intentActionActivity = new Intent(context, ActionActivity_.class);
                intentActionActivity.putExtra(PUT_EXTRA_PLACE, places.get(i));
                startActivity(intentActionActivity);
            }
        }
    }
    @Click(R.id.buttonFastRec)
    void buttonFastRecWasClicked(){
        Intent intentMapActivity = new Intent(context, MapsActivity_.class);
        intentMapActivity.putExtra(ACTION_MODE, REC_MODE);
        startActivity(intentMapActivity);
    }

    @Click(R.id.buttonResetPassword)
    public void buttonResetPasswordWasClicked(){
        startActivity(new Intent(StartActivity.this, ResetPasswordActivity.class));
    }

    @Click(R.id.buttonCreateNewAccount)
    public void buttonCreateNewAccount(){
        startActivity(new Intent(StartActivity.this, SignupActivity_.class));
    }
    @Click(R.id.emailLoginButton)
    public void emailLoginButtonWasClicked(){
        String email = inputEmail.getText().toString();
        final String password = inputPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

     //   progressBar.setVisibility(View.VISIBLE);

        //authenticate user
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(StartActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the mAuth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            // there was an error
                            if (password.length() < 6) {
                                inputPassword.setError(getString(R.string.minimum_password));
                            } else {
                                Toast.makeText(StartActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Intent intent = new Intent(StartActivity.this, StartActivity_.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    private void loginFacebook() {
        // If using in a fragment

        mCallbackManager = CallbackManager.Factory.create();
        loginFacebookButton.setReadPermissions("email");
        // Callback registration
        loginFacebookButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast toast = Toast.makeText(StartActivity.this, "Logged In", Toast.LENGTH_SHORT);
                handleFacebookAccessToken(loginResult.getAccessToken());
                toast.show();
                updateUI();
            }

            @Override
            public void onCancel() {
                // App code
                updateUI();
            }

            @Override
            public void onError(FacebookException exception) {
                updateUI();
            }
        });
    }
    private void loginGoogle() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, (GoogleApiClient.OnConnectionFailedListener) this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
      //  mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                updateUI();
                // [END_EXCLUDE]
            }
        }
    }

    private void updateUI() {
        boolean enableButtons = AccessToken.getCurrentAccessToken() != null;

        Profile profile = Profile.getCurrentProfile();
        if (enableButtons && profile != null) {
            Glide
                    .with(this)
                    .load(profile.getProfilePictureUri(200, 200).toString())
                    .into(userAccountImage);
           // greeting.setText(getString(R.string.hello_user, profile.getFirstName()));
        } else {
            userAccountImage.setImageResource(R.drawable.user_default);
          //  greeting.setText(null);
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        //Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            //Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(StartActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }else{
                            updateUI();

                            LoginManager.getInstance().logOut();
                            finish();
                        }
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            updateUI();
                            Intent intent = new Intent(StartActivity.this, StartActivity_.class);
                            startActivity(intent);
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(StartActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI();
                        }
                    }
                });
    }




    @Click(R.id.googleButton)
   public void buttonGoogleLoginWasClicked(){
        signIn();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
// An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

   @Click(R.id.facebokLoginButton)
    void facebokLoginButtonWasClicked(){
        inputPasswordLayout.setVisibility(View.GONE);
        inputEmailLayout.setVisibility(View.GONE);
        emailLoginButton.setVisibility(View.GONE);
        buttonResetPassword.setVisibility(View.GONE);
        buttonCreateNewAccount.setVisibility(View.GONE);
       loginFacebookButton.setVisibility(View.VISIBLE);
       loginFacebook();
    }

    @Click(R.id.googleLoginButton)
    void googleLoginButtonWasClicked(){
        inputPasswordLayout.setVisibility(View.GONE);
        inputEmailLayout.setVisibility(View.GONE);
        emailLoginButton.setVisibility(View.GONE);
        buttonResetPassword.setVisibility(View.GONE);
        buttonCreateNewAccount.setVisibility(View.GONE);
        googleButton.setVisibility(View.VISIBLE);
    }


}
