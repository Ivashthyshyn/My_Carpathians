package com.example.key.my_carpathians.utils;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.LineString;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Position;
import com.example.key.my_carpathians.models.Rout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;
import static com.example.key.my_carpathians.activities.StartActivity.PREFS_NAME;
import static com.example.key.my_carpathians.fragments.EditModeFragment.NO_PUBLISH_CONSTANT;
import static com.example.key.my_carpathians.utils.LocationService.CREATED_BY_USER_PLACE_LIST;
import static com.example.key.my_carpathians.utils.LocationService.CREATED_BY_USER_ROUT_LIST;
import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

/**
 * Created by key on 29.09.17.
 */

public class ObjectSaver {
	public static final String FILE_EXISTS = "file_exists";


	public String saveRout(String name, List<com.cocoahero.android.geojson.Position> positionList){

		LineString lineString = new LineString();
		lineString.setPositions(positionList);
		Feature feature = new Feature();
		try {
			JSONObject geoJSON = new JSONObject();
			feature.setProperties(new JSONObject());
			feature.setGeometry(lineString);
			feature.setIdentifier("key.my_carpathians");
			geoJSON.put("features", new JSONArray().put(feature.toJSON()));
			geoJSON.put("type", "FeatureCollection");

			File rootPath = new File(getApplicationContext().getExternalFilesDir(
					Environment.DIRECTORY_DOWNLOADS), "Routs");
			if (!rootPath.exists()) {
				rootPath.mkdirs();
			}
			File localFile = new File(rootPath, name);
			if (localFile.exists()) {
				return FILE_EXISTS;
			} else {
				String fileUri = String.valueOf(Uri.fromFile(localFile));
				Writer output = new BufferedWriter(new FileWriter(localFile));
				output.write(geoJSON.toString());
				output.close();
				SharedPreferences mSharedPreferences = getApplicationContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
				Rout mRout = new Rout();
				mRout.setNameRout(name);
				mRout.setUrlRoutsTrack(fileUri);

				File rootPath2 = new File(getApplicationContext().getExternalFilesDir(
						Environment.DIRECTORY_DOWNLOADS), "Created");
				if (!rootPath2.exists()) {
					rootPath2.mkdirs();
				}

				File file = new File(rootPath2, name + NO_PUBLISH_CONSTANT);
				if (file.exists()) {
					return FILE_EXISTS;
				} else {
					try {
						FileOutputStream fileOutputStream = new FileOutputStream(file);
						ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
						objectOutputStream.writeObject(mRout);
						objectOutputStream.close();
						fileOutputStream.close();
						Set<String> createdByUserTrackList = new HashSet<>(mSharedPreferences.getStringSet(CREATED_BY_USER_ROUT_LIST, new HashSet<String>()));
						createdByUserTrackList.add(name + NO_PUBLISH_CONSTANT);
						mSharedPreferences.edit().putStringSet(CREATED_BY_USER_ROUT_LIST, createdByUserTrackList).apply();
						return ("Rout saved");
					} catch (IOException e) {
						e.printStackTrace();
						return( e.getMessage());
					}
				}
			}

		} catch (Exception e) {
			return( e.getMessage());
		}
	}
	public String savePlace(String name, Position position){
		Place mPlace = new Place();
		mPlace.setNamePlace(name);
		mPlace.setPositionPlace(position);

		File rootPath = new File(getApplicationContext().getExternalFilesDir(
				Environment.DIRECTORY_DOWNLOADS), "Created");
		if (!rootPath.exists()) {
			rootPath.mkdirs();
		}

		File file = new File(rootPath, name);
		String fileUri = String.valueOf(Uri.fromFile(file));
		if (file.exists()) {
			return FILE_EXISTS;
		}
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(mPlace);
			objectOutputStream.close();
			fileOutputStream.close();
			SharedPreferences mSharedPreferences = getApplicationContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
			Set<String> createdByUserTrackList = new HashSet<>(mSharedPreferences.getStringSet(CREATED_BY_USER_PLACE_LIST, new HashSet<String>()));
			createdByUserTrackList.add(name);
			mSharedPreferences.edit().putStringSet(CREATED_BY_USER_PLACE_LIST, createdByUserTrackList).apply();
			return "Rout saved";
		} catch (IOException e) {
			return e.getMessage();
		}
	}
}
