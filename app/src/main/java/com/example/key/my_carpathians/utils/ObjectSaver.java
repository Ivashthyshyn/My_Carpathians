package com.example.key.my_carpathians.utils;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.LineString;
import com.example.key.my_carpathians.models.Place;
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

	public String saveRout(String name, List<com.cocoahero.android.geojson.Position> positionList, Rout rout, boolean replaceExistFile) {
		SharedPreferences mSharedPreferences = getApplicationContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		if (!replaceExistFile) {
			String mRootPathToSaveTrack;
			if (isExternalStorageWritable()) {
				mRootPathToSaveTrack = Uri.fromFile(getApplicationContext().getExternalFilesDir(
						Environment.DIRECTORY_DOWNLOADS)).buildUpon().appendPath("Routs").build()
						.getPath();
			} else {
				mRootPathToSaveTrack = Uri.fromFile(getApplicationContext().getFilesDir()).buildUpon()
						.appendPath("Routs").build().getPath();
			}

			try {
				LineString lineString = new LineString();
				lineString.setPositions(positionList);
				Feature feature = new Feature();
				JSONObject geoJSON = new JSONObject();
				feature.setProperties(new JSONObject());
				feature.setGeometry(lineString);
				feature.setIdentifier("key.my_carpathians");
				geoJSON.put("features", new JSONArray().put(feature.toJSON()));
				geoJSON.put("type", "FeatureCollection");

				File rootPath = new File(mRootPathToSaveTrack);
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
					rout.setUrlRoutsTrack(fileUri);

					String mRootPathToSaveRout;
					if (isExternalStorageWritable()) {
						mRootPathToSaveRout = Uri.fromFile(getApplicationContext().getExternalFilesDir(
								Environment.DIRECTORY_DOWNLOADS)).buildUpon().appendPath("Created").build()
								.getPath();
					} else {
						mRootPathToSaveRout = Uri.fromFile(getApplicationContext().getFilesDir()).buildUpon()
								.appendPath("Created").build().getPath();
					}

					File rootPath2 = new File(mRootPathToSaveRout);
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
							objectOutputStream.writeObject(rout);
							objectOutputStream.close();
							fileOutputStream.close();
							Set<String> createdByUserTrackList = new HashSet<>(mSharedPreferences.getStringSet(CREATED_BY_USER_ROUT_LIST, new HashSet<String>()));
							createdByUserTrackList.add(name + NO_PUBLISH_CONSTANT);
							mSharedPreferences.edit().putStringSet(CREATED_BY_USER_ROUT_LIST, createdByUserTrackList).apply();
							return ("Rout saved");
						} catch (IOException e) {
							e.printStackTrace();
							return (e.getMessage());
						}
					}
				}

			} catch (Exception e) {
				return (e.getMessage());
			}
		} else {
			String mRootPathToSaveRout;
			if (isExternalStorageWritable()) {
				mRootPathToSaveRout = Uri.fromFile(getApplicationContext().getExternalFilesDir(
						Environment.DIRECTORY_DOWNLOADS)).buildUpon().appendPath("Created").build()
						.getPath();
			} else {
				mRootPathToSaveRout = Uri.fromFile(getApplicationContext().getFilesDir()).buildUpon()
						.appendPath("Created").build().getPath();
			}

			File rootPath2 = new File(mRootPathToSaveRout);
			if (!rootPath2.exists()) {
				rootPath2.mkdirs();
			}

			File file = new File(rootPath2, rout.getNameRout() + NO_PUBLISH_CONSTANT);
			if (file.exists()) {
				file.delete();
			} else {
				File betterFile = new File(rootPath2, name);
				if (betterFile.exists()) {
					file.delete();
				}
			}
			try {
				FileOutputStream fileOutputStream = new FileOutputStream(file);
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
				objectOutputStream.writeObject(rout);
				objectOutputStream.close();
				fileOutputStream.close();
				Set<String> createdByUserTrackList = new HashSet<>(mSharedPreferences.getStringSet(CREATED_BY_USER_ROUT_LIST, new HashSet<String>()));
				if (createdByUserTrackList.add(rout.getNameRout() + NO_PUBLISH_CONSTANT)) {
					createdByUserTrackList.remove(name + NO_PUBLISH_CONSTANT );
				}
				mSharedPreferences.edit().putStringSet(CREATED_BY_USER_ROUT_LIST, createdByUserTrackList).apply();
				return ("Rout saved");
			} catch (IOException e) {
				e.printStackTrace();
				return (e.getMessage());
			}

		}
	}
	public String savePlace(String name,  Place place, boolean replaceExistFile){
		SharedPreferences mSharedPreferences = getApplicationContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		if (!replaceExistFile) {
			String mRootPathToSavePlace;
			if (isExternalStorageWritable()) {
				mRootPathToSavePlace = Uri.fromFile(getApplicationContext().getExternalFilesDir(
						Environment.DIRECTORY_DOWNLOADS)).buildUpon().appendPath("Created").build()
						.getPath();
			} else {
				mRootPathToSavePlace = Uri.fromFile(getApplicationContext().getFilesDir()).buildUpon()
						.appendPath("Created").build().getPath();
			}

			File rootPath = new File(mRootPathToSavePlace);
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
				objectOutputStream.writeObject(place);
				objectOutputStream.close();
				fileOutputStream.close();
				Set<String> createdByUserTrackList = new HashSet<>(mSharedPreferences.getStringSet(CREATED_BY_USER_PLACE_LIST, new HashSet<String>()));
				createdByUserTrackList.add(name);
				mSharedPreferences.edit().putStringSet(CREATED_BY_USER_PLACE_LIST, createdByUserTrackList).apply();
				return "Place saved";
			} catch (IOException e) {
				return e.getMessage();
			}
		}else {
			String mRootPathToSavePlace;
			if (isExternalStorageWritable()) {
				mRootPathToSavePlace = Uri.fromFile(getApplicationContext().getExternalFilesDir(
						Environment.DIRECTORY_DOWNLOADS)).buildUpon().appendPath("Created").build()
						.getPath();
			} else {
				mRootPathToSavePlace = Uri.fromFile(getApplicationContext().getFilesDir()).buildUpon()
						.appendPath("Created").build().getPath();
			}

			File rootPath = new File(mRootPathToSavePlace);
			if (!rootPath.exists()) {
				rootPath.mkdirs();
			}

			File file = new File(rootPath, place.getNamePlace());
			if (file.exists()) {
				file.delete();
			} else {
				File betterFile = new File(rootPath, name);
				if (betterFile.exists()) {
					file.delete();
				}
			}
			try {
				FileOutputStream fileOutputStream = new FileOutputStream(file);
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
				objectOutputStream.writeObject(place);
				objectOutputStream.close();
				fileOutputStream.close();
				Set<String> createdByUserPlaceList = new HashSet<>(mSharedPreferences.getStringSet(CREATED_BY_USER_PLACE_LIST, new HashSet<String>()));
				if (createdByUserPlaceList.add(place.getNamePlace())) {
					createdByUserPlaceList.remove(name);
				}
				mSharedPreferences.edit().putStringSet(CREATED_BY_USER_PLACE_LIST, createdByUserPlaceList).apply();
				return ("Place saved");
			} catch (IOException e) {
				return (e.getMessage());
			}
		}
	}
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}
}
