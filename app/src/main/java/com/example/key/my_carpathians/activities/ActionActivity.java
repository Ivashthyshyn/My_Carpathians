package com.example.key.my_carpathians.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Position;
import com.example.key.my_carpathians.models.Rout;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.widget.Toast.LENGTH_LONG;
import static com.example.key.my_carpathians.activities.MapsActivity.PERIMETER_SIZE_TO_LATITUDE;
import static com.example.key.my_carpathians.activities.MapsActivity.PERIMETER_SIZE_TO_LONGITUDE;
import static com.example.key.my_carpathians.activities.StartActivity.FAVORITES_PLACE_LIST;
import static com.example.key.my_carpathians.activities.StartActivity.FAVORITES_ROUTS_LIST;
import static com.example.key.my_carpathians.activities.StartActivity.PREFS_NAME;
import static com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter.PLACE_LIST;
import static com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter.ROUTS_LIST;
import static com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter.ViewHolder.PUT_EXTRA_PLASE;
import static com.example.key.my_carpathians.adapters.RoutsRecyclerAdapter.RoutsViewHolder.PUT_EXTRA_ROUT;

@EActivity
public class ActionActivity extends AppCompatActivity {

    public static final String SELECTED_USER_ROUTS = "selected-user_routs";
    public static final String SELECTED_USER_PLACES = "selected_user_places";
    private SharedPreferences sharedPreferences;
    List<Rout> routList;
    List<Place> placeList;
    Place myPlace;
    Rout myRout;
    Position myPosition;
    String myName;
    ArrayList<String> selectedUserRouts = new ArrayList<>();
    ArrayList<String> selectedUserPlacesStringList = new ArrayList<>();
    boolean inAlertDialogCheckedSomething = false;
    AlertDialog alertDialog;
    private ArrayList<Place> selectedUserPlacesList = new ArrayList<>();
    @ViewById(R.id.imageView)
    ImageView imageView;
    @ViewById(R.id.textName)
    TextView textName;
    @ViewById(R.id.titleText)
    TextView titleText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);
        sharedPreferences = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        routList = (List<Rout>) getIntent().getSerializableExtra(ROUTS_LIST);
        placeList = (List<Place>) getIntent().getSerializableExtra(PLACE_LIST);
        myPlace = (Place) getIntent().getSerializableExtra(PUT_EXTRA_PLASE);
        myRout = (Rout) getIntent().getSerializableExtra(PUT_EXTRA_ROUT);
        if (myPlace != null){
            textName.setText(myPlace.getNamePlace());
            titleText.setText(myPlace.getTitlePlace());
            Glide
                    .with(ActionActivity.this)
                    .load(myPlace.getUrlPlace())
                    .into(imageView);
            myPosition = myPlace.getPositionPlace();
            myName = myPlace.getNamePlace();
        }else if (myRout != null){
            textName.setText(myRout.getNameRout());
            titleText.setText(myRout.getTitleRout());
            Glide
                    .with(ActionActivity.this)
                    .load(myRout.getUrlRout())
                    .into(imageView);
            myPosition = myRout.getPositionRout();
            myName = myRout.getNameRout();
        }

    }

    @Click(R.id.buttonShowPlaceOnMap)
    public void buttonShowPlaceOnMapWasClicked(){
        if(myPlace != null) {
            selectedUserPlacesList.add(myPlace);
        }
        if (myRout != null){
            selectedUserRouts.add(myRout.getNameRout());
        }
        Intent mapIntent = new Intent(ActionActivity.this,MapsActivity_.class);
        mapIntent.putExtra(SELECTED_USER_PLACES, selectedUserPlacesList);
        mapIntent.putStringArrayListExtra(SELECTED_USER_ROUTS, selectedUserRouts);
        startActivity(mapIntent);
    }

    @Click(R.id.buttonShowAroundRout)
    public void buttonShowAroundRoutWasClicked(){
        List<Rout> routsAround = new ArrayList<>();
        List<String> routsAroundName = new ArrayList<>();
        for (int i = 0;i < routList.size();i++){
            Rout mRout =  routList.get(i);
            double lat = mRout.getPositionRout().getLatitude();
            double lng = mRout.getPositionRout().getLongitude();
            if ( myPosition.getLongitude() + PERIMETER_SIZE_TO_LONGITUDE > lng
                    && myPosition.getLongitude() - PERIMETER_SIZE_TO_LONGITUDE < lng
                    && myPosition.getLatitude() + PERIMETER_SIZE_TO_LATITUDE > lat
                    && myPosition.getLatitude() - PERIMETER_SIZE_TO_LATITUDE < lat
                    && !myName.equals(mRout.getNameRout())){
                routsAround.add(mRout);
                routsAroundName.add(mRout.getNameRout());
            }
        }
        if (routsAround.size() != 0 ) {
            showListDialogForRouts(routsAroundName, routsAround);
        }else {
            Toast.makeText(ActionActivity.this, "No place around ", Toast.LENGTH_SHORT).show();
        }
    }

    @Click(R.id.buttonPlacesAruond)
    void buttonPlacesAroundWasClicked(){
        List<Place> placesAround = new ArrayList<>();
        List<String> placesAroundName = new ArrayList<>();
        for (int i =0;i < placeList.size();i++){
            Place mPlace =  placeList.get(i);
            double lat = mPlace.getPositionPlace().getLatitude();
            double lng = mPlace.getPositionPlace().getLongitude();
            if ( myPosition.getLongitude() + PERIMETER_SIZE_TO_LONGITUDE > lng
                    && myPosition.getLongitude() - PERIMETER_SIZE_TO_LONGITUDE < lng
                    && myPosition.getLatitude() + PERIMETER_SIZE_TO_LATITUDE > lat
                    && myPosition.getLatitude() - PERIMETER_SIZE_TO_LATITUDE < lat
                    && !myName.equals(mPlace.getNamePlace())){
                placesAround.add(mPlace);
                placesAroundName.add(mPlace.getNamePlace());
            }
        }
        if (placesAround.size() != 0 ) {
            showListDialogForPlaces(placesAroundName, placesAround);
        }else {
            Toast.makeText(ActionActivity.this, "No place around ", Toast.LENGTH_SHORT).show();
        }
    }

    private void showListDialogForPlaces(final List<String> stringList, final List<Place> objectList) {
        final boolean[] mCheckedItems = new boolean[stringList.size()];
        final CharSequence[] items = stringList.toArray(new CharSequence[stringList.size()]);
        AlertDialog.Builder dialog = new AlertDialog.Builder(ActionActivity.this)
                .setTitle(getString(R.string.navigate_title))
                .setMultiChoiceItems(items, mCheckedItems,  new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which, boolean isChecked) {
                        if(isChecked){
                            inAlertDialogCheckedSomething = true;
                        }
                        mCheckedItems[which] = isChecked;
                    }
                })
                .setPositiveButton("Додати до карти", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {}
                })

                .setNegativeButton("Ні", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {}
                });
        alertDialog = dialog.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inAlertDialogCheckedSomething){
                    for (int i = 0; i < items.length; i++) {
                        if (!mCheckedItems[i]) {
                            objectList.remove(i);
                            stringList.remove(i);
                        }
                    }
                    selectedUserPlacesList = (ArrayList<Place>) objectList;
                    selectedUserPlacesStringList = (ArrayList<String>) stringList;

                    inAlertDialogCheckedSomething = false;
                    alertDialog.dismiss();
                }else{
                    Toast.makeText(ActionActivity.this, "Please selected something",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void showListDialogForRouts(final List<String> stringList, final List<Rout> objectList) {
        final boolean[] mCheckedItems = new boolean[stringList.size()];
        final CharSequence[] items = stringList.toArray(new CharSequence[stringList.size()]);

        AlertDialog.Builder dialog = new AlertDialog.Builder(ActionActivity.this)
                .setTitle(getString(R.string.navigate_title))
                .setMultiChoiceItems(items, mCheckedItems,  new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which, boolean isChecked) {
                        if(isChecked){
                            inAlertDialogCheckedSomething = true;
                        }
                        mCheckedItems[which] = isChecked;
                    }
                })
                .setPositiveButton("Додати до карти", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })

                .setNegativeButton("Детальніше", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        alertDialog = dialog.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inAlertDialogCheckedSomething){
                    for (int i = 0; i < items.length; i++) {
                        if (mCheckedItems[i]) {
                           selectedUserRouts.add(stringList.get(i));
                        }
                        inAlertDialogCheckedSomething = false;
                        alertDialog.dismiss();
                    }
                }else{
                    Toast.makeText(ActionActivity.this, "Please selected something",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inAlertDialogCheckedSomething){
                    for (int i = 0; i < items.length; i++) {
                        if (mCheckedItems[i]) {
                           selectedUserRouts.add(stringList.get(i));
                        }
                        inAlertDialogCheckedSomething = false;
                        alertDialog.dismiss();
                    }
                }else{
                    Toast.makeText(ActionActivity.this, "Please selected something",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        selectedUserRouts.clear();
        selectedUserPlacesStringList.clear();
        selectedUserPlacesList.clear();
    }

    @Click(R.id.buttonAddToFavorites)
    void buttonAddToFavoritesWasClicked(){
        if (myPlace != null) {
            selectedUserPlacesStringList.add(myPlace.getNamePlace());
        }
        if(myRout != null) {
            selectedUserRouts.add(myRout.getNameRout());
        }
        Set<String> favoritesPlacesList = new HashSet<>(sharedPreferences.getStringSet(FAVORITES_PLACE_LIST, new HashSet<String>()));
        favoritesPlacesList.addAll(selectedUserPlacesStringList);
        Set<String> favoritesRoutsList = new HashSet<>(sharedPreferences.getStringSet(FAVORITES_ROUTS_LIST, new HashSet<String>()));
        favoritesRoutsList.addAll(selectedUserRouts);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(FAVORITES_PLACE_LIST, favoritesPlacesList);
        editor.putStringSet(FAVORITES_ROUTS_LIST, favoritesRoutsList);
        editor.apply();

        Toast.makeText(ActionActivity.this, " Add to favorites", LENGTH_LONG).show();

    }

}
