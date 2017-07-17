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
import com.example.key.my_carpathians.models.Position;
import com.example.key.my_carpathians.models.Rout;

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
import static com.example.key.my_carpathians.adapters.RoutsRecyclerAdapter.RoutsViewHolder.PUT_EXTRA_ROUT;

@EActivity
public class ActionActivity extends AppCompatActivity {

    public static final String SELECTED_USER_ROUTS = "selected-user_routs";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String SELECTED_USER_PLACES = "selectedUserPlaces";
    public SharedPreferences sharedPreferences;
    List<Rout> routList;
    List<Place> placeList;
    Place myPlace;
    Rout myRout;
    Position myPosition;
    String myName;
    private List<Place> selectedUserPlacesList = new ArrayList<>();
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

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
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
        selectedUserPlacesList.add(myPlace);
        ArrayList<Place> arrayList = (ArrayList<Place>) selectedUserPlacesList;
        Intent mapIntent = new Intent(ActionActivity.this,MapsActivity_.class);
        mapIntent.putExtra(SELECTED_USER_PLACES, arrayList);
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
            if ( myPosition.getLongitude() + 0.4 > lng
                    && myPosition.getLongitude() - 0.4 < lng
                    && myPosition.getLatitude() + 0.3 > lat
                    && myPosition.getLatitude() - 0.3 < lat
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
            if ( myPosition.getLongitude() + 0.4 > lng
                    && myPosition.getLongitude() - 0.4 < lng
                    && myPosition.getLatitude() + 0.3 > lat
                    && myPosition.getLatitude() - 0.3 < lat
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
        AlertDialog dialog = new AlertDialog.Builder(ActionActivity.this)
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
        AlertDialog dialog = new AlertDialog.Builder(ActionActivity.this)
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
