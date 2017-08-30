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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.adapters.FavoritesRecyclerAdapter;
import com.example.key.my_carpathians.fragments.PlacesListFragment;
import com.example.key.my_carpathians.fragments.PlacesListFragment_;
import com.example.key.my_carpathians.fragments.RoutsListFragment;
import com.example.key.my_carpathians.fragments.RoutsListFragment_;
import com.example.key.my_carpathians.interfaces.Communicator;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
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
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.key.my_carpathians.activities.MapsActivity.REC_MODE;
import static com.example.key.my_carpathians.adapters.FavoritesRecyclerAdapter.MY_PLACE;
import static com.example.key.my_carpathians.adapters.FavoritesRecyclerAdapter.MY_ROUT;
import static com.example.key.my_carpathians.adapters.FavoritesRecyclerAdapter.PLACE;
import static com.example.key.my_carpathians.adapters.FavoritesRecyclerAdapter.ROUT;
import static com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter.ViewHolder.PUT_EXTRA_PLACE;
import static com.example.key.my_carpathians.adapters.RoutsRecyclerAdapter.RoutsViewHolder.PUT_EXTRA_ROUT;
import static com.example.key.my_carpathians.utils.LocationService.CREATED_BY_USER_PLACE_LIST;
import static com.example.key.my_carpathians.utils.LocationService.CREATED_BY_USER_ROUT_LIST;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with mFirebaseUser interaction.
 */
@EActivity
public class StartActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,Communicator {
    public static final String PUT_EXTRA_PLACE_LIST = "place_list";
    public static final String PUT_EXTRA_ROUTS_LIST = "routs_list";
    public static final String FAVORITES_ROUTS_LIST = "favorites_user_routs";
    public static final String FAVORITES_PLACE_LIST = "favorites_user_places";
    public static final String ACTION_MODE = "action_mode";
    public static final String PREFS_NAME = "MyPrefsFile";
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "StartActivity";
    public static final String PRODUCE_MODE = "manufacturer_mode";
    public FragmentManager fragmentManager;
    public PlacesListFragment placesListFragment;
    public RoutsListFragment routsListFragment;
    public ArrayList<Place> places = new ArrayList<>();
    public ArrayList<Rout> routs = new ArrayList<>();
    public AlertDialog.Builder builder;
    @ViewById(R.id.buttonFastRec)
    Button buttonFastRec;
    @ViewById(R.id.userAcountImage)
    CircleImageView userAccountImage;
    @ViewById(R.id.facebokLoginButton)
    Button facebookLoginButton;
    @ViewById(R.id.googleLoginButton)
    Button googleLoginButton;
    @ViewById(R.id.textViewEmail)
    TextView textViewEmail;
    @ViewById(R.id.googleButton)
    SignInButton loginGoogle;
    @ViewById(R.id.buttonLogout)
    Button buttonLogaut;
    @ViewById(R.id.buttonGoogleLogout)
    Button buttonGoogleLogout;
    @ViewById(R.id.facebookButton)
    LoginButton loginFacebook;
    @ViewById(R.id.email)
    EditText inputEmail;
    @ViewById(R.id.password)
    EditText inputPassword;
    @ViewById(R.id.progressBar)
    ProgressBar progressBar;
    @ViewById(R.id.emailLoginButton)
    Button emailLoginButton;
    @ViewById(R.id.inputEmailLayout)
    TextInputLayout inputEmailLayout;
    @ViewById(R.id.inputPasswordLayout)
    TextInputLayout inputPasswordLayout;
    @ViewById(R.id.buttonResetPassword)
    Button buttonResetPassword;
    @ViewById(R.id.buttonCreateNewAccount)
    Button buttonCreateNewAccount;
    @ViewById(R.id.buttonFavoritesPlaces)
    Button buttonFavoritesPlaces;
    @ViewById(R.id.buttonFavoritesRouts)
    Button buttonFavoritesRouts;
    @ViewById(R.id.buttonCreatedPlaces)
    Button buttonCreatedPlaces;
    @ViewById(R.id.buttonCreatedRouts)
    Button buttonCreatedRouts;
    @ViewById(R.id.listViewPlace)
    RecyclerView listOfPlaces;
    @ViewById(R.id.listViewRout)
    RecyclerView listOfRouts;
    @ViewById(R.id.listViewCreatedRouts)
    RecyclerView listCreatedRouts;
    @ViewById(R.id.listViewCreatedPlaces)
    RecyclerView listCreatedPlaces;
    @ViewById(R.id.textViewCreated)
    TextView textViewCreated;
    private boolean connected = false;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Context context = StartActivity.this;
    private SharedPreferences mSharedPreferences;
    private DrawerLayout mDrawerLayout;
    private FirebaseUser mFirebaseUser;
    private CallbackManager mCallbackManager;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
	private String mUserUID;
    private boolean mTypeMode = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mAuth = FirebaseAuth.getInstance();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                mUser = mAuth.getCurrentUser();
                if (mUser == null) {
                    showableLogInGroup(true);
                    mTypeMode = false;
                } else if (mUser.isAnonymous()) {
                    updateUI("Anonymous", null);
	                mUserUID = mUser.getUid();
                    mTypeMode = false;
                    showInterfaceForAnonymous();
                    produceToolsVisibility(mTypeMode);
                } else {
                    if (mUser.getProviders().get(0).equals("google.com")) {
                        produceToolsVisibility(mTypeMode);
                        mTypeMode = true;
                        loginGoogle();
	                    mUserUID = mUser.getUid();
                        updateUI(mUser.getProviderData().get(0).getDisplayName(),
                                String.valueOf(mUser.getProviderData().get(0).getPhotoUrl()));
                        buttonGoogleLogout.setVisibility(View.VISIBLE);
                    } else if (mUser.getProviders().get(0).equals("facebook.com")) {
                        produceToolsVisibility(mTypeMode);
                        mTypeMode = true;
	                    mUserUID = mUser.getUid();
                        loginFacebook();
                        updateUI(mUser.getProviderData().get(0).getDisplayName(),
                                String.valueOf(mUser.getProviderData().get(0).getPhotoUrl()));
                    } else if (mUser.getProviders().get(0).equals("password")) {
                        produceToolsVisibility(mTypeMode);
                        mTypeMode = true;
	                    mUserUID = mUser.getUid();
                        updateUI(mUser.getProviderData().get(0).getEmail(), null);
                        buttonLogaut.setVisibility(View.VISIBLE);
                        buttonLogaut.setText("LOG OUT");
                    }
                }
                produceToolsVisibility(mTypeMode);
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        mSharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFirebaseUser = firebaseAuth.getCurrentUser();
                if (mFirebaseUser != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + mFirebaseUser.getUid());
                } else {
                    showLoginDialog();
                }

            }
        };

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();

        Query myPlace = myRef.child("Places");
        myPlace.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Place place = postSnapshot.getValue(Place.class);
                    places.add(place);

                }
                downloadPhoto(places);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Query myRouts = myRef.child("Rout");
        myRouts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
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

    private void produceToolsVisibility(boolean mTypeMode) {
        if(mTypeMode){
            buttonCreatedPlaces.setVisibility(View.VISIBLE);
            buttonCreatedRouts.setVisibility(View.VISIBLE);
            textViewCreated.setVisibility(View.VISIBLE);
            buttonFastRec.setAlpha((float) 1);
        }else{
            buttonCreatedPlaces.setVisibility(View.GONE);
            buttonCreatedRouts.setVisibility(View.GONE);
            textViewCreated.setVisibility(View.GONE);
            buttonFastRec.setAlpha((float) 0.5);
        }
    }

    private void showInterfaceForAnonymous() {
        buttonLogaut.setVisibility(View.VISIBLE);
        buttonLogaut.setText("Authentication");
        showableLogInGroup(false);
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null, null);
        showableLogInGroup(true);

    }


    /**
     * This method is download and save routs track to SD card in package "Rout"
     *
     * @param urlRoutsTrack is a file address in database Storage from downloading
     * @param nameRout      is the file name that is written to SD card
     */
    @Background
    public void downloadRoutToStorage(String urlRoutsTrack, final String nameRout) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference httpsReference = storage.getReferenceFromUrl(urlRoutsTrack);
        String nameFileInStorage = httpsReference.getName();
        File rootPath = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOWNLOADS), "Routs");
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }

        final File localFile = new File(rootPath, nameFileInStorage);
        if (!localFile.exists()) {

            final URI fileUri = localFile.toURI();

            httpsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    mSharedPreferences.edit().putString(nameRout, fileUri.toString()).apply();

                    Log.e("firebase ", ";local tem file created  created " + localFile.toString());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                    Log.e("firebase ", ";local tem file not created  created " + exception.toString());
                }
            });
        }
    }

    @Background
    public void downloadPhoto(List<Place> placeList) {
        for (int i = 0; i < placeList.size(); i++) {
            try {
                URL url = new URL(placeList.get(i).getUrlPlace());

                File rootPath = new File(context.getExternalFilesDir(
                        Environment.DIRECTORY_DOWNLOADS), "Photos");
                if (!rootPath.exists()) {
                    rootPath.mkdirs();
                }


                File file = new File(rootPath, placeList.get(i).getNamePlace());
                if (!file.exists()) {
                    URLConnection urlConnection = url.openConnection();
                    InputStream inputStream = null;
                    HttpURLConnection httpConn = (HttpURLConnection) urlConnection;
                    httpConn.setRequestMethod("GET");
                    httpConn.connect();

                    if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        inputStream = httpConn.getInputStream();
                    }

                    FileOutputStream fos = new FileOutputStream(file);
                    int totalSize = httpConn.getContentLength();
                    int downloadedSize = 0;
                    byte[] buffer = new byte[1024];
                    int bufferLength = 0;
                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, bufferLength);
                        downloadedSize += bufferLength;
                        Log.i("Progress:", "downloadedSize:" + downloadedSize + "totalSize:" + totalSize);
                    }

                    fos.close();
                    Log.d("test", "Image Saved in sdcard..");
                }
            } catch (IOException io) {
                io.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Click(R.id.buttonPlace)
    void buttonPlaceWasClicked() {
        if (places.size() != 0 & routs.size() != 0) {
            fragmentManager = getSupportFragmentManager();
            placesListFragment = new PlacesListFragment_();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentTransaction.add(R.id.drawer_layout, placesListFragment);
            fragmentTransaction.commit();
            placesListFragment.setList(places);
        } else if (places.size() == 0 & !isOnline()) {
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Мережа Інтернет");
            builder.setMessage("Ви жодного разу після встановлення програми не підключались до мережі " +
                    "Потрібно хочаб раз завантажити дані з інтернету.  " + "Завантаження відбувається автоматично");

            builder.setPositiveButton("Так", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {

                }
            });

            builder.setCancelable(true);


            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Click(R.id.buttonRoutes)
    void buttonRoutesWasClicked() {
        if (routs.size() != 0 & routs.size() != 0) {
            fragmentManager = getSupportFragmentManager();
            routsListFragment = new RoutsListFragment_();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentTransaction.add(R.id.drawer_layout, routsListFragment);
            fragmentTransaction.commit();
            routsListFragment.setList(routs);
        } else if (routs.size() == 0 & !isOnline()) {
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Мережа Інтернет");
            builder.setMessage("Ви жодного разу після встановлення програми не підключались до мережі " +
                    "Потрібно хочаб раз завантажити дані з інтернету.  " + "Завантаження відбувається автоматично");

            builder.setPositiveButton("Так", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {

                }
            });

            builder.setCancelable(true);


            AlertDialog alert = builder.create();
            alert.show();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onBackPressed() {
        if (placesListFragment != null) {
            fragmentManager.beginTransaction().remove(placesListFragment).commit();
            placesListFragment = null;
        } else if (routsListFragment != null) {
            fragmentManager.beginTransaction().remove(routsListFragment).commit();
            routsListFragment = null;
        } else {
            super.onBackPressed();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    void showLoginDialog() {
        if (isOnline()) {
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Для того щоб отримати доступ до всіх функцій програми потрібно зареєструватися");
            builder.setMessage("Виберіть спосіб реєстраці");

            builder.setPositiveButton("Зареєструватися", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {

                    mDrawerLayout.openDrawer(Gravity.START);
                }
            });
            builder.setNegativeButton("Анонімний вхід", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    signInAnonymously();
                    mDrawerLayout.closeDrawer(Gravity.START);

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
        } else {
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Мережа Інтернет");
            builder.setMessage("Нажаль ви зараз ви не підключені до мережі інтернет!" +
                    " Керування автентифікацією недоступне! ");

            builder.setPositiveButton("Так", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {

                }
            });

            builder.setCancelable(true);


            AlertDialog alert = builder.create();
            alert.show();
        }
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

    @Override
    public void putStringNameRout(String name, int type) {
        if (type == MY_ROUT){
	        File rootPath = new File(context.getExternalFilesDir(
			        Environment.DIRECTORY_DOWNLOADS), "Created");
	        if (!rootPath.exists()) {
		        rootPath.mkdirs();
	        }

		        File file = new File(rootPath, name);
		        if (file.exists()) {
			        try {
				        FileInputStream fileIn = new FileInputStream(file);
				        ObjectInputStream objectInputStream = new ObjectInputStream(fileIn);
                        Rout rout = (Rout)objectInputStream.readObject();
				        objectInputStream.close();
                        fileIn.close();
				        Intent intentActionActivity = new Intent(context, ActionActivity_.class);
				        intentActionActivity.putExtra(PUT_EXTRA_ROUT, rout);
				        intentActionActivity.putExtra(PRODUCE_MODE, mTypeMode);
				        startActivity(intentActionActivity);

			        } catch (Exception e) {
				        e.printStackTrace();
			        }
		        }
        }else{
        for (int i = 0; i < routs.size(); i++) {
	        if (routs.get(i).getNameRout().equals(name)) {
		        Intent intentActionActivity = new Intent(context, ActionActivity_.class);

                ArrayList<Place> arrayListPlace = (ArrayList<Place>) places;
                ArrayList<Rout> arrayListRouts = (ArrayList<Rout>) routs;
                intentActionActivity.putExtra(PUT_EXTRA_PLACE_LIST, arrayListPlace);
                intentActionActivity.putExtra(PUT_EXTRA_ROUTS_LIST, arrayListRouts);
                intentActionActivity.putExtra(PUT_EXTRA_ROUT, routs.get(i));
                intentActionActivity.putExtra(PRODUCE_MODE, mTypeMode);
		        startActivity(intentActionActivity);
            }
        }

        }
    }

    @Override
    public void putStringNamePlace(String name, int type) {
        if (type == MY_PLACE){
            File rootPath = new File(context.getExternalFilesDir(
                    Environment.DIRECTORY_DOWNLOADS), "Created");
            if (!rootPath.exists()) {
                rootPath.mkdirs();
            }

            File file = new File(rootPath, name);
            if (file.exists()) {
                try {
                    FileInputStream fileIn = new FileInputStream(file);
                    ObjectInputStream objectInputStream = new ObjectInputStream(fileIn);
                    Place place = (Place) objectInputStream.readObject();
                    objectInputStream.close();
                    fileIn.close();
                    Intent intentActionActivity = new Intent(context, ActionActivity_.class);
                    intentActionActivity.putExtra(PUT_EXTRA_PLACE, place);
                    intentActionActivity.putExtra(PRODUCE_MODE, mTypeMode);
                    startActivity(intentActionActivity);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else if (type == PLACE){
        for (int i = 0; i < places.size(); i++) {
            if (places.get(i).getNamePlace().equals(name)) {
                Intent intentActionActivity = new Intent(context, ActionActivity_.class);
                intentActionActivity.putExtra(PUT_EXTRA_PLACE, places.get(i));
                ArrayList<Place> arrayListPlace = (ArrayList<Place>) places;
                ArrayList<Rout> arrayListRouts = (ArrayList<Rout>) routs;
                intentActionActivity.putExtra(PUT_EXTRA_PLACE_LIST, arrayListPlace);
                intentActionActivity.putExtra(PUT_EXTRA_ROUTS_LIST, arrayListRouts);
                intentActionActivity.putExtra(PRODUCE_MODE, mTypeMode);
                startActivity(intentActionActivity);
                }
            }

        }
    }

    @Click(R.id.buttonFastRec)
    void buttonFastRecWasClicked() {
        if (mTypeMode) {
            Intent intentMapActivity = new Intent(context, MapsActivity_.class);
            intentMapActivity.putExtra(ACTION_MODE, REC_MODE);
            intentMapActivity.putExtra(PRODUCE_MODE, mTypeMode);
            startActivity(intentMapActivity);
        }else {
            showLoginDialog();
        }
    }

    @Click(R.id.buttonResetPassword)
    public void buttonResetPasswordWasClicked() {
        startActivity(new Intent(StartActivity.this, ResetPasswordActivity_.class));
    }

    @Click(R.id.buttonCreateNewAccount)
    public void buttonCreateNewAccount() {
        startActivity(new Intent(StartActivity.this, SignupActivity_.class));
    }

    @Click(R.id.emailLoginButton)
    public void emailLoginButtonWasClicked() {
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

        //authenticate mUser
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(StartActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the mUser. If sign in succeeds
                        // the mAuth state listener will be notified and logic to handle the
                        // signed in mUser can be handled in the listener.
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
        loginFacebook.setVisibility(View.VISIBLE);
        // If using in a fragment
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken2) {
                Log.d(TAG, "onCurrentAccessTokenChanged()");
                if (accessToken == null) {
                    showableLogInGroup(false);
                    loginFacebook.setVisibility(View.VISIBLE);
                } else if (accessToken2 == null) {
                    LoginManager.getInstance().logOut();
                    mAuth.signOut();
                    showableLogInGroup(true);
                }
            }
        };
        mCallbackManager = CallbackManager.Factory.create();

        loginFacebook.setReadPermissions("email");
        // Callback registration
        loginFacebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast toast = Toast.makeText(StartActivity.this, "Logged In", Toast.LENGTH_SHORT);
                handleFacebookAccessToken(loginResult.getAccessToken());
                toast.show();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                updateUI(null, null);
            }

        });


    }

    private void showableLogInGroup(boolean b) {
        if (b) {
            inputPasswordLayout.setVisibility(View.VISIBLE);
            inputEmailLayout.setVisibility(View.VISIBLE);
            emailLoginButton.setVisibility(View.VISIBLE);
            buttonResetPassword.setVisibility(View.VISIBLE);
            buttonCreateNewAccount.setVisibility(View.VISIBLE);
            facebookLoginButton.setVisibility(View.VISIBLE);
            googleLoginButton.setVisibility(View.VISIBLE);
            loginFacebook.setVisibility(View.GONE);
            loginGoogle.setVisibility(View.GONE);
            buttonLogaut.setVisibility(View.GONE);
            buttonGoogleLogout.setVisibility(View.GONE);
        } else {
            inputPasswordLayout.setVisibility(View.GONE);
            inputEmailLayout.setVisibility(View.GONE);
            emailLoginButton.setVisibility(View.GONE);
            buttonResetPassword.setVisibility(View.GONE);
            buttonCreateNewAccount.setVisibility(View.GONE);
            facebookLoginButton.setVisibility(View.GONE);
            googleLoginButton.setVisibility(View.GONE);
            buttonGoogleLogout.setVisibility(View.GONE);
        }
    }

    private void loginGoogle() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        if (mCallbackManager != null) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
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
                updateUI(null, null);
                // [END_EXCLUDE]
            }
        }
    }

    private void updateUI(String email, String photoUrl) {
        if (photoUrl != null) {
            Glide
                    .with(this)
                    .load(photoUrl)
                    .into(userAccountImage);
        } else {
            userAccountImage.setImageResource(R.drawable.user_default);
        }
        if (email != null) {
            textViewEmail.setText(email);
        } else {
            textViewEmail.setText("");
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        //Log.d(TAG, "handleFacebookAccessToken:" + token)

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the mUser. If sign in succeeds
                        // the mAuth state listener will be notified and logic to handle the
                        // signed in mUser can be handled in the listener.
                        if (!task.isSuccessful()) {

                            //Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(StartActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            Profile profile = Profile.getCurrentProfile();
                            updateUI(profile.getFirstName(), profile.getProfilePictureUri(200, 200).toString());
	                        mUserUID = mAuth.getCurrentUser().getUid();
                            mTypeMode = true;
                            produceToolsVisibility(mTypeMode);
                        }
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in mUser's information
                            Log.d(TAG, "signInWithCredential:success");
                            loginGoogle.setVisibility(View.GONE);
                            buttonGoogleLogout.setVisibility(View.VISIBLE);
                            updateUI(acct.getEmail(), String.valueOf(acct.getPhotoUrl()));
							mUserUID = mAuth.getCurrentUser().getUid();
                            produceToolsVisibility(mTypeMode = true);

                        } else {
                            // If sign in fails, display a message to the mUser.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(StartActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null, null);
                        }
                    }
                });
    }


    private void googleSignOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            signOut();
                        }
                        // ...
                    }
                });
    }


    @Click(R.id.googleButton)
    public void buttonGoogleLoginWasClicked() {
        signIn();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Click(R.id.facebokLoginButton)
    void facebookLoginButtonWasClicked() {
        showableLogInGroup(false);
        loginFacebook.setVisibility(View.VISIBLE);
        loginFacebook();
    }

    @Click(R.id.googleLoginButton)
    void googleLoginButtonWasClicked() {
        showableLogInGroup(false);
        loginGoogle();
        loginGoogle.setVisibility(View.VISIBLE);
    }

    @Click(R.id.buttonLogout)
    void buttonLogoutWasClicked() {
        signOut();
        showableLogInGroup(true);
    }

    @Click(R.id.buttonGoogleLogout)
    void buttonGoogleLogout() {
        googleSignOut();
    }

    @Click(R.id.buttonFavoritesPlaces)
    void buttonFavoritesPlaces() {
        if (listOfPlaces.getVisibility() == View.GONE) {
            mSharedPreferences = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            Set<String> favoritesPlacesList = mSharedPreferences.getStringSet(FAVORITES_PLACE_LIST, null);
            if (favoritesPlacesList != null) {
                ArrayList<String> listPlaces = new ArrayList<>(favoritesPlacesList);
                listOfPlaces.setVisibility(View.VISIBLE);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
                Log.d("debugMode", "The application stopped after this");
                listOfPlaces.setLayoutManager(mLayoutManager);
                FavoritesRecyclerAdapter recyclerAdapter = new FavoritesRecyclerAdapter(StartActivity.this, listPlaces, PLACE);
                listOfPlaces.setAdapter(recyclerAdapter);
            }
        }else { listOfPlaces.setVisibility(View.GONE);}
    }

    @Click(R.id.buttonFavoritesRouts)
    void buttonFavoritesRouts() {
        if (listOfRouts.getVisibility() == View.GONE) {
            mSharedPreferences = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            Set<String> favoritesRoutsList = mSharedPreferences.getStringSet(FAVORITES_ROUTS_LIST, null);
            if (favoritesRoutsList != null) {
                ArrayList<String> listRouts = new ArrayList<>(favoritesRoutsList);
                listOfRouts.setVisibility(View.VISIBLE);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
                Log.d("debugMode", "The application stopped after this");
                listOfRouts.setLayoutManager(mLayoutManager);
                FavoritesRecyclerAdapter recyclerAdapter = new FavoritesRecyclerAdapter(StartActivity.this, listRouts, ROUT);
                listOfRouts.setAdapter(recyclerAdapter);
            }
        }else{listOfRouts.setVisibility(View.GONE);}
    }

    @Click(R.id.buttonCreatedPlaces)
    void buttonCreatedPlaces() {
        if (listCreatedPlaces.getVisibility() == View.GONE) {
            mSharedPreferences = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            Set<String> createdByUserPlaceList = mSharedPreferences.getStringSet(CREATED_BY_USER_PLACE_LIST, null);

            if (createdByUserPlaceList != null) {
                listCreatedPlaces.setVisibility(View.VISIBLE);

                ArrayList<String> listPlaces = new ArrayList<>(createdByUserPlaceList);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
                Log.d("debugMode", "The application stopped after this");
                listCreatedPlaces.setLayoutManager(mLayoutManager);
                FavoritesRecyclerAdapter recyclerAdapter = new FavoritesRecyclerAdapter( StartActivity.this, listPlaces, MY_PLACE);
                listCreatedPlaces.setAdapter(recyclerAdapter);

            }
        }else{
            listCreatedPlaces.setVisibility(View.GONE);}
    }

    @Click(R.id.buttonCreatedRouts)
    void buttonCreatedRouts() {
        if (listCreatedRouts.getVisibility() == View.GONE) {
            mSharedPreferences = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            Set<String> createdByUserTrackList = mSharedPreferences.getStringSet(CREATED_BY_USER_ROUT_LIST, null);

            if (createdByUserTrackList != null) {
                listCreatedRouts.setVisibility(View.VISIBLE);

                ArrayList<String> listTrack = new ArrayList<>(createdByUserTrackList);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
                Log.d("debugMode", "The application stopped after this");
                listCreatedRouts.setLayoutManager(mLayoutManager);
                FavoritesRecyclerAdapter recyclerAdapter = new FavoritesRecyclerAdapter( StartActivity.this, listTrack, MY_ROUT);
                listCreatedRouts.setAdapter(recyclerAdapter);

            }
        }else{
            listCreatedRouts.setVisibility(View.GONE);}
    }

}
