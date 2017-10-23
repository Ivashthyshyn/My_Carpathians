package com.example.key.my_carpathians.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

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
import static com.example.key.my_carpathians.utils.LocationService.CREATED_BY_USER_PLACE_LIST;
import static com.example.key.my_carpathians.utils.LocationService.CREATED_BY_USER_ROUT_LIST;

/**
 * Created by key on 29.09.17.
 */

public class ObjectService {
	public static final String FILE_EXISTS = "file_exists";
	public static final String ERROR = "error";
	public final String rootPathString;
	public Context context;

	public ObjectService(Context context, String rootPathString){
		this.rootPathString = rootPathString;
		this.context = context;
	}
	public String saveRout(String name, List<com.cocoahero.android.geojson.Position> positionList, Rout rout, boolean replaceExistFile) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		if (!replaceExistFile) {

			String mRootPathToSaveTrack = Uri.parse(rootPathString).buildUpon().appendPath("Routs").build()
						.getPath();


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
					String fileUri =(Uri.fromFile(localFile)).getPath();
					Writer output = new BufferedWriter(new FileWriter(localFile));
					output.write(geoJSON.toString());
					output.close();
					rout.setUrlRoutsTrack(fileUri);

					String mRootPathToSaveRout = Uri.parse(rootPathString).buildUpon().appendPath("Created").build()
								.getPath();


					File rootPath2 = new File(mRootPathToSaveRout);
					if (!rootPath2.exists()) {
						rootPath2.mkdirs();
					}

					File file = new File(rootPath2, name);
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
							createdByUserTrackList.add(name);
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
			String mRootPathToSaveTrack = Uri.parse(rootPathString).buildUpon().appendPath("Routs").build()
						.getPath();
			File rootPath = new File(mRootPathToSaveTrack);
			if (!rootPath.exists()) {
				rootPath.mkdirs();
			}
			File localFile = new File(rootPath, name);
			if (localFile.exists()) {
				File newFile  = new File(rootPath, rout.getNameRout());
				localFile.renameTo(newFile);
                rout.setUrlRoutsTrack(Uri.fromFile(newFile).getPath());

				String mRootPathToSaveRout = Uri.parse(rootPathString).buildUpon().appendPath("Created").build()
							.getPath();
				File rootPath2 = new File(mRootPathToSaveRout);
				if (!rootPath2.exists()) {
					rootPath2.mkdirs();
				}

				File file = new File(rootPath2, rout.getNameRout());
				if (file.exists()) {
					file.delete();
				} else {
					File betterFile = new File(rootPath2, name);
					if (betterFile.exists()) {
						betterFile.delete();
					}
				}
				try {
					FileOutputStream fileOutputStream = new FileOutputStream(file);
					ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
					objectOutputStream.writeObject(rout);
					objectOutputStream.close();
					fileOutputStream.close();
					Set<String> createdByUserTrackList = new HashSet<>(mSharedPreferences.getStringSet(CREATED_BY_USER_ROUT_LIST, new HashSet<String>()));
					if (createdByUserTrackList.add(rout.getNameRout())) {
						createdByUserTrackList.remove(name);
					}
					mSharedPreferences.edit().putStringSet(CREATED_BY_USER_ROUT_LIST, createdByUserTrackList).apply();
					return ("Rout saved");

				} catch (IOException e) {
					e.printStackTrace();
					return (e.getMessage());
				}
			}else {
				return ERROR;
			}
		}

	}
	public String deleteRout(String name){
		SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		String mRootPathToSaveRout = Uri.parse(rootPathString).buildUpon().appendPath("Created").build()
					.getPath();

		File rootPathRout = new File(mRootPathToSaveRout);
		if (!rootPathRout.exists()) {
			rootPathRout.mkdirs();
		}
		File localFileRout = new File(rootPathRout, name);
		if (localFileRout.exists()) {
			localFileRout.delete();
			String mRootPathToSaveTrack = Uri.parse(rootPathString).buildUpon().appendPath("Routs").build()
						.getPath();

			File rootPathTrack = new File(mRootPathToSaveTrack);
			if (!rootPathTrack.exists()) {
				rootPathTrack.mkdirs();
			}
			File localFileTrack = new File(rootPathTrack, name);
			if (localFileTrack.exists()) {
				localFileTrack.delete();
				Set<String> createdByUserTrackList = new HashSet<>(mSharedPreferences.getStringSet(CREATED_BY_USER_ROUT_LIST, new HashSet<String>()));
				if (createdByUserTrackList.contains(name)) {
					createdByUserTrackList.remove(name);
				}
				mSharedPreferences.edit().putStringSet(CREATED_BY_USER_ROUT_LIST, createdByUserTrackList).apply();
				String mRootPathToSaveRoutPhoto = Uri.parse(rootPathString).buildUpon().appendPath("Photo").build()
							.getPath();

				File rootPathRoutPhoto = new File(mRootPathToSaveRoutPhoto);
				if (!rootPathRoutPhoto.exists()) {
					rootPathRoutPhoto.mkdirs();
				}
				File localFileRoutPhoto = new File(rootPathRoutPhoto, name);
				if (localFileRoutPhoto.exists()) {
					localFileRoutPhoto.delete();
					File localFileRoutPhoto1 = new File(rootPathRoutPhoto, name + 1);
					if (localFileRoutPhoto1.exists()) {
						localFileRoutPhoto1.delete();

					}
					File localFileRoutPhoto2 = new File(rootPathRoutPhoto, name + 2);
					if (localFileRoutPhoto2.exists()) {
						localFileRoutPhoto2.delete();

					}
					File localFileRoutPhoto3 = new File(rootPathRoutPhoto, name + 3);
					if (localFileRoutPhoto3.exists()) {
						localFileRoutPhoto3.delete();

					}
				}

				return "Rout deleted";
			}else{
			return ERROR;
			}
		}else{
			return ERROR;
		}

	}
	public String savePlace(String name,  Place place, boolean replaceExistFile){
		SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		if (!replaceExistFile) {
			String mRootPathToSavePlace = Uri.parse(rootPathString).buildUpon().appendPath("Created").build()
						.getPath();
			File rootPath = new File(mRootPathToSavePlace);
			if (!rootPath.exists()) {
				rootPath.mkdirs();
			}

			File file = new File(rootPath, name);
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
			String mRootPathToSavePlace = Uri.parse(rootPathString).buildUpon().appendPath("Created").build()
						.getPath();
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
	public String deletePlace(String name){
		SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		String mRootPathToSavePlace = Uri.parse(rootPathString).buildUpon().appendPath("Created").build()
					.getPath();
		File rootPath = new File(mRootPathToSavePlace);
		if (!rootPath.exists()) {
			rootPath.mkdirs();
		}
		File file = new File(rootPath, name);
		if (file.exists()) {
			file.delete();
			Set<String> createdByUserPlaceList = new HashSet<>(mSharedPreferences.getStringSet(CREATED_BY_USER_PLACE_LIST, new HashSet<String>()));
			if (createdByUserPlaceList.contains(name)) {
				createdByUserPlaceList.remove(name);
			}
			mSharedPreferences.edit().putStringSet(CREATED_BY_USER_PLACE_LIST, createdByUserPlaceList).apply();
			return "Place deleted";
		}else {
			return ERROR;
		}
	}
}
