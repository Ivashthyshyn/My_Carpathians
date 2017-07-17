package com.example.key.my_carpathians.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.key.my_carpathians.activities.StartActivity.PREFS_NAME;
import static com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter.PLACE_LIST;
import static com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter.ROUTS_LIST;
import static com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter.ViewHolder.PUT_EXTRA_PLASE;

@EActivity
public class PlaceActivity extends AppCompatActivity {

    public static final String SELECTED_USER_ROUTS = "selected-user_routs";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String SELECTED_USER_PLACES = "selectedUserPlaces";
    public SharedPreferences sharedPreferences;
    private List<Place> selectedUserPlacesList = new ArrayList<>();
    Place myPlace;
    @ViewById(R.id.imageView)
    ImageView imageView;
    @ViewById(R.id.textName)
    TextView textName;
    @ViewById(R.id.titleText)
    TextView titleText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);


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
        selectedUserPlacesList.add(myPlace);
        ArrayList<Place> arrayList = (ArrayList<Place>) selectedUserPlacesList;
        Intent mapIntent = new Intent(PlaceActivity.this,MapsActivity_.class);
        mapIntent.putExtra(SELECTED_USER_PLACES, arrayList);
        startActivity(mapIntent);
    }

    @Click(R.id.buttonShowAroundRout)
    public void buttonShowAroundRoutWasClicked(){
        List<Rout> routList = (List<Rout>) getIntent().getSerializableExtra(ROUTS_LIST);
        List<Rout> routsAround = new ArrayList<>();
        List<String> routsAroundName = new ArrayList<>();
        for (int i = 0;i < routList.size();i++){
            Rout mRout =  routList.get(i);
            double lat = mRout.getPositionRout().getLatitude();
            double lng = mRout.getPositionRout().getLongitude();
            if ( myPlace.getPositionPlace().getLongitude() + 0.4 > lng
                    && myPlace.getPositionPlace().getLongitude() - 0.4 < lng
                    && myPlace.getPositionPlace().getLatitude() + 0.3 > lat
                    && myPlace.getPositionPlace().getLatitude() - 0.3 < lat
                    && !myPlace.getNamePlace().equals(mRout.getNameRout())){
                routsAround.add(mRout);
                routsAroundName.add(mRout.getNameRout());
            }
        }
        if (routsAround.size() != 0 ) {
            showListDialogForRouts(routsAroundName, routsAround);
        }else {
            Toast.makeText(PlaceActivity.this, "No place around ", Toast.LENGTH_SHORT).show();
        }
    }

    @Click(R.id.buttonPlacesAruond)
    void buttonPlacesAroundWasClicked(){
        List<Place> placeList = (List<Place>) getIntent().getSerializableExtra(PLACE_LIST);
        List<Place> placesAround = new ArrayList<>();
        List<String> placesAroundName = new ArrayList<>();
        for (int i =0;i < placeList.size();i++){
            Place mPlace =  placeList.get(i);
            double lat = mPlace.getPositionPlace().getLatitude();
            double lng = mPlace.getPositionPlace().getLongitude();
            if ( myPlace.getPositionPlace().getLongitude() + 0.4 > lng
                    && myPlace.getPositionPlace().getLongitude() - 0.4 < lng
                    && myPlace.getPositionPlace().getLatitude() + 0.3 > lat
                    && myPlace.getPositionPlace().getLatitude() - 0.3 < lat
                    && !myPlace.getNamePlace().equals(mPlace.getNamePlace())){
                placesAround.add(mPlace);
                placesAroundName.add(mPlace.getNamePlace());
            }
        }
        if (placesAround.size() != 0 ) {
            showListDialogForPlaces(placesAroundName, placesAround);
        }else {
            Toast.makeText(PlaceActivity.this, "No place around ", Toast.LENGTH_SHORT).show();
        }
    }

    private void showListDialogForPlaces(final List<String> stringList, final List<Place> objectList) {
        final boolean[] mCheckedItems = new boolean[stringList.size()];
        final CharSequence[] items = stringList.toArray(new CharSequence[stringList.size()]);
        AlertDialog dialog = new AlertDialog.Builder(PlaceActivity.this)
                .setTitle(getString(R.string.navigate_title))
                .setMultiChoiceItems(items, mCheckedItems,  new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which, boolean isChecked) {
                        mCheckedItems[which] = isChecked;
                    }
                })
                .setPositiveButton("Додати до карти", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        for (int i = 0; i < items.length; i++) {
                            if (!mCheckedItems[i]){
                                objectList.remove(i);
                            }
                        }
                        selectedUserPlacesList = objectList;
                    }
                })

                .setNegativeButton("Детальніше", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        for (int i = 0; i < items.length; i++) {
                            if (!mCheckedItems[i]) {
                                objectList.remove(i);
                            }
                        }
                        // TODO need add intent
                    }
                }).create();
        dialog.show();
    }
    private void showListDialogForRouts(final List<String> stringList, final List<Rout> objectList) {
        final boolean[] mCheckedItems = new boolean[stringList.size()];
        final CharSequence[] items = stringList.toArray(new CharSequence[stringList.size()]);
        AlertDialog dialog = new AlertDialog.Builder(PlaceActivity.this)
                .setTitle(getString(R.string.navigate_title))
                .setMultiChoiceItems(items, mCheckedItems,  new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which, boolean isChecked) {
                        mCheckedItems[which] = isChecked;
                    }
                })
                .setPositiveButton("Додати до карти", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Set<String> selectedUserRoutes = sharedPreferences
                                .getStringSet(SELECTED_USER_ROUTS, new HashSet<String>());
                        for (int i = 0; i < items.length; i++) {
                            if (!mCheckedItems[i]){
                                stringList.remove(i);
                            }else {
                                selectedUserRoutes.add(stringList.get(i));
                            }
                        }
                        sharedPreferences.edit().putStringSet(SELECTED_USER_ROUTS, selectedUserRoutes).apply();
                    }
                })

                .setNegativeButton("Детальніше", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        for (int i = 0; i < items.length; i++) {
                            if (!mCheckedItems[i]) {
                                objectList.remove(i);
                            }
                        }
                        // TODO need add intent
                    }
                }).create();
        dialog.show();
    }


}
