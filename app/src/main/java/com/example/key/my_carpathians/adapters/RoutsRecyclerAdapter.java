package com.example.key.my_carpathians.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.interfaces.Communicator;
import com.example.key.my_carpathians.models.Rout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.services.api.utils.turf.TurfConstants;
import com.mapbox.services.api.utils.turf.TurfMeasurement;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.commons.utils.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
import static com.example.key.my_carpathians.activities.StartActivity.PREFS_NAME;
import static com.example.key.my_carpathians.adapters.FavoritesRecyclerAdapter.ROUT;
import static com.example.key.my_carpathians.fragments.EditModeFragment.HARD;
import static com.example.key.my_carpathians.fragments.EditModeFragment.LIGHT;
import static com.example.key.my_carpathians.fragments.EditModeFragment.MEDIUM;

/**
 *
 */

public class RoutsRecyclerAdapter extends RecyclerView.Adapter<RoutsRecyclerAdapter.RoutsViewHolder> {
    public static final String PUT_EXTRA_POINTS = "put_extra_point_list";
    public Context context;
    private List<Rout> routs;


    public RoutsRecyclerAdapter(List<Rout> routList) {
        this.routs = routList;
    }

    @Override
    public RoutsRecyclerAdapter.RoutsViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lis_item_for_rout, parent, false);
        context = mView.getContext();

        RoutsViewHolder mHolder = new RoutsViewHolder(mView, new RoutsViewHolder.ClickListener() {
            @Override
            public void onPressed(Rout routObject) {
                if (routObject != null) {
                    Communicator communicator = (Communicator)context;
                    communicator.putStringNameRout(routObject.getNameRout(), ROUT);
                }
            }

        });

        return mHolder;
    }

    @Override
    public void onBindViewHolder(RoutsViewHolder holder, int position) {
        holder.mRout = routs.get(position);
        holder.textNameRout.setText(holder.mRout.getNameRout());
        LengthRout lengthRout = new LengthRout(holder.mRout.getNameRout(), holder.textLengthTrack);
        lengthRout.doInBackground();

        ratingRout(holder.mRout.getNameRout(), holder.ratingBar);
        switch (holder.mRout.getRoutsLevel()) {
            case LIGHT:
                holder.buttonTypeAndLevel.setBackgroundResource(R.color.colorGreenPrimary);
                return;
            case MEDIUM:
                holder.buttonTypeAndLevel.setBackgroundResource(R.color.colorYellowPrimary);
                return;
            case HARD:
                holder.buttonTypeAndLevel.setBackgroundResource(R.color.colorRedPrimary);
        }
    }

    private void ratingRout(String nameRout, final RatingBar ratingBar) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference();
            Query myPlace = myRef.child("Rating").child(nameRout);

            myPlace.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int counter = 0;
                    float sum = 0;
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        counter++;
                        float value = postSnapshot.getValue(float.class);
                        sum = sum + value;

                    }
                     float averageValue = sum/counter;
                    ratingBar.setRating( averageValue);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    ratingBar.setRating(0);
                }
            });

    }


    public class LengthRout extends AsyncTask<Void, Void, Void> {
        String name;
        TextView textView;

        LengthRout(String name, TextView textView) {
            this.textView = textView;
            this.name = name;
        }


        @Override
        protected Void doInBackground(Void... strings) {
            List<Position> points = new ArrayList<>();
            URI mUri = URI.create(context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .getString(name, null));
            try {
                // Load GeoJSON file
                File file = new File(mUri);
                if (file.exists()) {

                    InputStream fileInputStream = new FileInputStream(file);
                    BufferedReader rd = new BufferedReader(new InputStreamReader(fileInputStream, Charset.forName("UTF-8")));
                    StringBuilder sb = new StringBuilder();
                    int cp;
                    while ((cp = rd.read()) != -1) {
                        sb.append((char) cp);
                    }

                    fileInputStream.close();
                    // Parse JSON
                    JSONObject json = new JSONObject(sb.toString());
                    JSONArray features = json.getJSONArray("features");
                    JSONObject feature = features.getJSONObject(0);
                    JSONObject geometry = feature.getJSONObject("geometry");
                    if (geometry != null) {
                        String type = geometry.getString("type");

                        // Our GeoJSON only has one feature: a line string
                        if (!TextUtils.isEmpty(type) && type.equalsIgnoreCase("LineString")) {

                            // Get the Coordinates
                            JSONArray coordinates = geometry.getJSONArray("coordinates");
                            for (int lc = 0; lc < coordinates.length(); lc++) {
                                JSONArray coordinate = coordinates.getJSONArray(lc);
                                Position position = Position.fromCoordinates(coordinate.getDouble(1), coordinate.getDouble(0), coordinate.getDouble(2));
                                points.add(position);
                            }
                        }
                    }
                }
            } catch (Exception exception) {
                Log.e(TAG, "Exception Loading GeoJSON: " + exception.toString());
            }


            if (points.size() > 0) {

                LineString lineString = LineString.fromCoordinates(points);
                double dis = 0;
                if (points.size() > 0) {
                    dis = TurfMeasurement.lineDistance(lineString, TurfConstants.UNIT_KILOMETERS);
                }
                DecimalFormat df = new DecimalFormat("#.#");
                df.setRoundingMode(RoundingMode.CEILING);
                textView.setText(df.format(dis) + "km");
            } else {
                textView.setText("0");
            }
            return null;
        }

    }


    @Override
    public int getItemCount() {
        return routs.size();
    }

    public static class RoutsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public static final String PUT_EXTRA_ROUT = "routName";
        public Button buttonTypeAndLevel;
        public TextView textNameRout;
        final public TextView textLengthTrack;
        public RatingBar ratingBar;
        private Rout mRout;
        private ClickListener mClickListener;


        RoutsViewHolder(View itemView, ClickListener listener) {
            super(itemView);
            mClickListener = listener;
            buttonTypeAndLevel = (Button) itemView.findViewById(R.id.buttonTypeAndLevel);
            textNameRout = (TextView) itemView.findViewById(R.id.textNameRout);
            textLengthTrack = (TextView) itemView.findViewById(R.id.textLenghtTrack);
            ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBarSmall);
            mRout = null;
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            mClickListener.onPressed(mRout);
        }

        interface ClickListener {
            void onPressed(Rout nameRout);
        }
    }

}
