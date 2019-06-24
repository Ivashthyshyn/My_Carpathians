package com.keyVas.key.my_carpathians.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.keyVas.key.my_carpathians.R;
import com.keyVas.key.my_carpathians.activities.StartActivity_;
import com.keyVas.key.my_carpathians.interfaces.CommunicatorActionActivity;
import com.keyVas.key.my_carpathians.models.Place;
import com.keyVas.key.my_carpathians.models.Rout;
import com.keyVas.key.my_carpathians.utils.StorageSaveHelper;
import com.mapbox.services.api.utils.turf.TurfConstants;
import com.mapbox.services.api.utils.turf.TurfMeasurement;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.commons.utils.TextUtils;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.keyVas.key.my_carpathians.activities.StartActivity.PREFS_NAME;
import static com.keyVas.key.my_carpathians.activities.StartActivity.ROOT_PATH;
import static com.keyVas.key.my_carpathians.models.Place.EN;
import static com.keyVas.key.my_carpathians.utils.LocaleHelper.SELECTED_LANGUAGE;
import static com.keyVas.key.my_carpathians.utils.StorageSaveHelper.ERROR;

/**
 .
 */
@EFragment
public class EditModeFragment extends DialogFragment implements View.OnFocusChangeListener {
	public static final int GALLERY_REQUEST = 1;
	public static final int LIGHT = 1;
	public static final int MEDIUM = 2;
	public static final int HARD = 3;
	private static final int TITLE_PHOTO = 0;
	private static final int MORE_PHOTO_1 = 1;
	private static final int MORE_PHOTO_2 = 2;
	private static final int MORE_PHOTO_3 = 3;
	private int mPhotoSwitcher = 0;
	private String mTrackLength = null;
	private com.keyVas.key.my_carpathians.models.Position mPositionRout = null;
	private Rout mRout = null ;
	private Place mPlace = null;
	int routsLevel = 0;
	String uriTitlePhoto = null;
	String uriPhoto1 = null;
	String uriPhoto2 = null;
	String uriPhoto3 = null;
	String name;

	View view;
	@ViewById(R.id.imageAdd1)
	ImageButton imageAdd1;

	@ViewById(R.id.appbar)
	AppBarLayout mAppBarLayout;
	@ViewById(R.id.imageAdd2)
	ImageButton imageAdd2;
	@ViewById(R.id.imageAdd3)
	ImageButton imageAdd3;

	@ViewById(R.id.buttonAddPhoto)
	ImageButton buttonAddPhoto;

	@ViewById(R.id.editTextName)
	EditText editTextName;

	@ViewById(R.id.editTextTitle)
	EditText editTextTitle;

	@ViewById(R.id.buttonSaveData)
	FloatingActionButton buttonSaveData;

	@ViewById(R.id.cropImage)
	CropImageView cropImageView;

	@ViewById(R.id.cropToolsFrame)
	LinearLayout cropToolsFrame;


	@ViewById(R.id.buttonBakCrop)
	ImageButton buttonBakCrop;

	@ViewById(R.id.buttonRotationCrop)
	ImageButton buttonRotationCrop;

	@ViewById(R.id.buttonCrop)
	ImageButton buttonCrop;

	@ViewById(R.id.progressCropBar)
	ProgressBar progressCropBar;

	@ViewById(R.id.progressViewText)
	TextView progressViewText;

	@ViewById(R.id.progressView)
	LinearLayout progressView;

	@ViewById(R.id.editGroup)
	LinearLayout editGroup;

	@ViewById(R.id.imageTitlePhoto)
	ImageView imageTitlePhoto;

	@ViewById(R.id.difficultyRatioGroup)
	RadioGroup radioGroup;

	@ViewById(R.id.groupMorePhoto)
	LinearLayout groupMorePhoto;
	private String rootPathString;
	private String mRootPathString;
	private String mUserLanguage;


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null){
			mPlace = (Place)savedInstanceState.getSerializable("Place");
			mRout = (Rout)savedInstanceState.getSerializable("Rout");

					}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mPlace != null) {
			outState.putSerializable("Place", mPlace);
		}else if (mRout != null){
			outState.putSerializable("Rout", mRout);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                     Bundle savedInstanceState) {
		view =inflater.inflate(R.layout.fragment_edit_mode, container, false);
		return view;
	}

	@AfterViews
	public void afterView(){
		SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		mUserLanguage = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(SELECTED_LANGUAGE, EN);
		mRootPathString = sharedPreferences.getString(ROOT_PATH, null);
		editTextName.setOnFocusChangeListener(this);
		editTextTitle.setOnFocusChangeListener(this);
			if(mPlace != null){
				name = mPlace.placeKey();
				uriTitlePhoto = mPlace.getUrlPlace();
				editTextName.setText(name);
				if (uriTitlePhoto != null){
					buttonAddPhoto.setImageResource(R.drawable.ic_refresh_48px);
				}

				radioGroup.setVisibility(View.GONE);
				editTextTitle.setText(mPlace.placeKey());
				Glide
						.with(getContext())
						.load(mPlace.getUrlPlace())
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.into(imageTitlePhoto);
				morePhotos(name);
			}else if (mRout != null){
				name = mRout.routKey();
				editTextName.setText(name);
				uriTitlePhoto = mRout.getUrlRout();
				if (uriTitlePhoto != null){
					buttonAddPhoto.setImageResource(R.drawable.ic_refresh_48px);
				}


				Glide
						.with(getContext())
						.load(mRout.getUrlRout())
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.into(imageTitlePhoto);
				morePhotos(name);
				determineLength(mRout.getUrlRoutsTrack());
				radioGroup.setVisibility(View.VISIBLE);
				for (int i = 1; i < radioGroup.getChildCount(); i++) {
					RadioButton rButton = (RadioButton) radioGroup.getChildAt(i);

					if (mRout.getRoutsLevel() == i) {
						routsLevel = mRout.getRoutsLevel();
						rButton.setChecked(true);
					}
				}
				editTextTitle.setText(mRout.getTitleRout(mUserLanguage));
			}


		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case -1:
						routsLevel = 0;
						break;
					case R.id.radioButtonLight:
						routsLevel = LIGHT;

						break;
					case R.id.radioButtonMedium:
						routsLevel = MEDIUM;
						break;
					case R.id.radioButtonHard:
						routsLevel = HARD;
						break;
				}
			}

		});
		cropImageView.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
			@Override
			public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
				progressView.setVisibility(View.INVISIBLE);
				cropToolsFrame.setVisibility(View.GONE);
				buttonSaveData.setVisibility(View.VISIBLE);
				editGroup.setVisibility(View.VISIBLE);
				groupMorePhoto.setVisibility(View.VISIBLE);

				switch (mPhotoSwitcher){
					case TITLE_PHOTO:
						uriTitlePhoto = savePhoto(name, cropImageView.getCroppedImage(), null);
						if (mPlace !=  null){
							mPlace.setUrlPlace(uriTitlePhoto);
						}else if(mRout != null){
							mRout.setUrlRout(uriTitlePhoto);
						}
						Glide
								.with(getContext())
								.load(uriTitlePhoto)
								.diskCacheStrategy(DiskCacheStrategy.NONE)
								.into(imageTitlePhoto);
						buttonAddPhoto.setImageResource(R.drawable.ic_refresh_48px);
						break;
					case MORE_PHOTO_1:
						uriPhoto1 = savePhoto(name + 1, cropImageView.getCroppedImage(), null);
						Glide
								.with(getContext())
								.load(uriPhoto1)
								.diskCacheStrategy(DiskCacheStrategy.NONE)
								.into(imageAdd1);
						break;
					case MORE_PHOTO_2:
						uriPhoto2 = savePhoto(name + 2, cropImageView.getCroppedImage(), null);
						Glide
								.with(getContext())
								.load(uriPhoto2)
								.diskCacheStrategy(DiskCacheStrategy.NONE)
								.into(imageAdd2);
						break;
					case MORE_PHOTO_3:
						uriPhoto3 = savePhoto(name + 3, cropImageView.getCroppedImage(), null);
						Glide
								.with(getContext())
								.load(uriPhoto3)
								.diskCacheStrategy(DiskCacheStrategy.NONE)
								.into(imageAdd3);
						break;
				}

			}
		});


		cropImageView.setOnSetImageUriCompleteListener(new CropImageView.OnSetImageUriCompleteListener() {
			@Override
			public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
				progressView.setVisibility(View.INVISIBLE);
			}
		});
	}
	@Background
	public void determineLength(String uri) {
		List<Position> points = new ArrayList<>();

		try {
			File file = new File(uri);
			if (file.exists()) {

				InputStream fileInputStream = new FileInputStream(file);
				BufferedReader rd = new BufferedReader(new InputStreamReader(fileInputStream,
						Charset.forName("UTF-8")));
				StringBuilder sb = new StringBuilder();
				int cp;
				while ((cp = rd.read()) != -1) {
					sb.append((char) cp);
				}

				fileInputStream.close();
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
							Position position = Position.fromCoordinates(coordinate.getDouble(1),
									coordinate.getDouble(0), coordinate.getDouble(2));
							points.add(position);
						}
					}
				}
			}
		} catch (Exception exception) {
			Toast.makeText(getContext(),getString(R.string.route_length_not_available),
					Toast.LENGTH_SHORT).show();
			mTrackLength = getString(R.string.unknown);
		}


		if (points.size() > 0) {
			mPositionRout = new com.keyVas.key.my_carpathians.models.Position();
			// todo need to verify
			mPositionRout.setLatitude(points.get(0).getLongitude());
			mPositionRout.setLongitude(points.get(0).getLatitude());

			LineString lineString = LineString.fromCoordinates(points);
			double dis = 0;
			if (points.size() > 0) {
				dis = TurfMeasurement.lineDistance(lineString, TurfConstants.UNIT_KILOMETERS);
			}
			DecimalFormat df = new DecimalFormat("#.#");
			df.setRoundingMode(RoundingMode.CEILING);
			mTrackLength = (df.format(dis) + getString(R.string.km));
		} else {
			mTrackLength = getString(R.string.unknown);
		}
	}

	private void morePhotos(String name) {
			Uri rootPathForPhotosString = Uri.parse(mRootPathString).buildUpon()
					.appendPath("Photos").build();

			for (int i = 1; i <= 3; i++) {
				File photoFile = new File(rootPathForPhotosString.buildUpon()
						.appendPath(name + String.valueOf(i)).build().getPath());
				if (photoFile.exists()) {
					Uri uri = Uri.fromFile(photoFile);
					switch (i){
						case 1: imageAdd1.setImageURI(uri);
							uriPhoto1 = uri.getPath();
							break;
						case 2: imageAdd2.setImageURI(uri);
							uriPhoto2 = uri.getPath();
							break;
						case 3: imageAdd3.setImageURI(uri);
							uriPhoto3 = uri.getPath();
					}
				}
			}

	}
    public void setData(Rout rout, Place place, String rootPathString){
	this.rootPathString = rootPathString;
	    if (rout != null){
		    mRout = rout;
	    }else if(place != null){
		    mPlace = place;
	    }
	}
	@Click(R.id.buttonAddPhoto)
	public void buttonAddPhotoWasClicked(){
		if(uriTitlePhoto != null){
			mPhotoSwitcher = TITLE_PHOTO;
			showAlertDialog(uriTitlePhoto, true);
		}else {
			mPhotoSwitcher = TITLE_PHOTO;
			searchPhotoInGallery();
		}
		}

	private void searchPhotoInGallery() {
		Intent intentFromGallery = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intentFromGallery.setType("image/*");
		startActivityForResult(intentFromGallery, GALLERY_REQUEST);
	}
	@Click(R.id.buttonCrop)
	public void buttonCrop(){
		cropImageView.getCroppedImageAsync();
	}
	@Click(R.id.buttonRotationCrop)
	public void buttonRotationCropWasClicked(){
		cropImageView.rotateImage(90);
	}

	@Click(R.id.buttonSaveData)
	public void buttonSaveDataWasClicked(){
		InputMethodManager imm = (InputMethodManager) getContext()
				.getSystemService(Activity.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			 if (editTextName.getText().toString().isEmpty()) {
				Toast.makeText(getContext(), getString(R.string.enter_name),
						Toast.LENGTH_SHORT).show();
			} else if (editTextTitle.getText().toString().isEmpty()) {
				Toast.makeText(getContext(), getString(R.string.enter_title_info),
						Toast.LENGTH_SHORT).show();
			} else if (routsLevel == 0 && mRout != null) {
				Toast.makeText(getContext(), getString(R.string.enter_difficulty),
						Toast.LENGTH_SHORT).show();
			} else if (uriTitlePhoto == null){
				Toast.makeText(getContext(), getString(R.string.enter_title_photo),
						Toast.LENGTH_SHORT).show();
			}else if (mRout != null){
				mRout.setNameRout(editTextName.getText().toString());
				mRout.setTitleRout(editTextTitle.getText().toString());
				mRout.setLengthRout(mTrackLength);
				mRout.setPositionRout(mPositionRout);
				mRout.setRoutsLevel(routsLevel);
				mRout.setUrlRout(savePhoto(mRout.routKey(), null, uriTitlePhoto));
					 savePhoto(mRout.routKey() + String.valueOf(MORE_PHOTO_1),
							 null, uriPhoto1);
					 savePhoto(mRout.routKey() + String.valueOf(MORE_PHOTO_2),
							 null, uriPhoto2);
					 savePhoto(mRout.routKey() + String.valueOf(MORE_PHOTO_3),
							 null, uriPhoto3);

				StorageSaveHelper storageSaveHelper = new StorageSaveHelper(getContext(), rootPathString);
				 String outcome = storageSaveHelper.saveRout(name, null, mRout, true);
				 Toast.makeText(getContext(), outcome, Toast.LENGTH_LONG).show();
				 if (outcome.equals("Rout saved")) {
					 CommunicatorActionActivity communicatorActionActivity = (CommunicatorActionActivity) getContext();
					 communicatorActionActivity.saveChanges(mRout, null);
				 }
			}else if(mPlace != null) {
				 mPlace.setNamePlace(editTextName.getText().toString());
				 mPlace.setTitlePlace(editTextTitle.getText().toString());
				 mPlace.setUrlPlace(savePhoto(mPlace.placeKey(), null, uriTitlePhoto));
					 savePhoto(mPlace.placeKey() + MORE_PHOTO_1, null, uriPhoto1);
					 savePhoto(mPlace.placeKey() + MORE_PHOTO_2, null, uriPhoto2);
					 savePhoto(mPlace.placeKey() + MORE_PHOTO_3, null, uriPhoto3);

				 StorageSaveHelper storageSaveHelper = new StorageSaveHelper(getContext(), rootPathString);
				 String outcome =  storageSaveHelper.savePlace( name, mPlace, true);
				 Toast.makeText(getContext(), outcome, Toast.LENGTH_LONG).show();
				 if (outcome.equals("Place saved")) {
					 CommunicatorActionActivity communicatorActionActivity = (CommunicatorActionActivity) getContext();
					 communicatorActionActivity.saveChanges(null, mPlace);
				 }

				 }
	}

	private String savePhoto(String name, Bitmap bitmap, String uri) {
			if (bitmap != null) {
				Uri rootPathForPhotos = Uri.parse(mRootPathString).buildUpon().appendPath("Photos").build();


				File file = new File(rootPathForPhotos.getPath(), name);
				if (file.exists()) {
					file.delete();
				}
				uri = Uri.fromFile(file).getPath();
				try {
					FileOutputStream fileOutputStream = new FileOutputStream(file);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream);
					fileOutputStream.flush();
					fileOutputStream.close();
					Toast.makeText(getContext(), getString(R.string.photo_saved),
							Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(getContext(), getString(R.string.photo_not_saved),
							Toast.LENGTH_SHORT).show();
					return null;
				}
			} else if(uri != null ) {
				File file = new File(uri);
				File newFile ;
				if (file.exists()) {
					Uri rootPathForTitlePhotoString = Uri.parse(mRootPathString).buildUpon()
							.appendPath("Photos").build();
					newFile = new File(rootPathForTitlePhotoString.buildUpon().appendPath(name).build().getPath());
					file.renameTo(newFile);
					uri = Uri.fromFile(newFile).getPath();
				}else {
					uri = Uri.fromFile(file).getPath();
				}
			}
		return uri;
	}



	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GALLERY_REQUEST
				&& resultCode == RESULT_OK) {
			Uri selectedImage = data.getData();
			if ( selectedImage!= null) {
				editGroup.setVisibility(View.GONE);
				cropToolsFrame.setVisibility(View.VISIBLE);
				buttonSaveData.setVisibility(View.GONE);
				cropImageView.setImageUriAsync(selectedImage);
				progressViewText.setText(getString(R.string.loading));
				progressView.setVisibility(View.VISIBLE);
			}
		}
	}

	@Click(R.id.buttonBakCrop)
	public void buttonBakCropWasClicked(){
		cropImageView.clearImage();
		cropToolsFrame.setVisibility(View.GONE);
		buttonSaveData.setVisibility(View.VISIBLE);
		groupMorePhoto.setVisibility(View.VISIBLE);
		editGroup.setVisibility(View.VISIBLE);
	}
	@Click(R.id.imageAdd1)
	public void imageAdd1WasClicked(){
		if (uriPhoto1 != null){
			mPhotoSwitcher = MORE_PHOTO_1;
			showAlertDialog(uriPhoto1, false);
		}else {
			mPhotoSwitcher = MORE_PHOTO_1;
			searchPhotoInGallery();
		}
	}

	private void showAlertDialog(final String uriPhoto, boolean necessarily) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(getString(R.string.photo));
		if (necessarily){
			builder.setMessage(getString(R.string.photo_message_necessarily));
		}else{
			builder.setMessage(getString(R.string.additional_photo_message_necessarily));
		}

		builder.setPositiveButton(getString(R.string.select), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				searchPhotoInGallery();
				dialogInterface.dismiss();
			}
		});
		builder.setNegativeButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				File file = new File(uriPhoto);
				if (file.exists()) {
					file.delete();
				}
				morePhotos(name);
				dialogInterface.dismiss();
			}
		});
		builder.create();
		builder.show();
	}

	@Click(R.id.imageAdd2)
	public void imageAdd2WasClicked(){
		if (uriPhoto2 != null){
			mPhotoSwitcher = MORE_PHOTO_2;
			showAlertDialog(uriPhoto2, false);
		}else {
			mPhotoSwitcher = MORE_PHOTO_2;
			searchPhotoInGallery();
		}
	}
	@Click(R.id.imageAdd3)
	public void imageAdd3WasClicked(){
		if (uriPhoto3 != null){
			mPhotoSwitcher = MORE_PHOTO_3;
			showAlertDialog(uriPhoto3, false);
		}else {
			mPhotoSwitcher = MORE_PHOTO_3;
			searchPhotoInGallery();
		}
	}

	public void deleteCreatedObject(){
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("Deleting!");
		if (mRout != null){
			builder.setMessage(getString(R.string.delete_asc_route) +" " + mRout.getNameRout(mUserLanguage) + "?");

		}else if (mPlace != null) {
			builder.setMessage(getString(R.string.delete_ask_place) + " " + mPlace.getNamePlace(mUserLanguage) + "?");
		}
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				if (mRout != null){
					StorageSaveHelper storageSaveHelper = new StorageSaveHelper(getContext(), rootPathString);
					String mOutcome = storageSaveHelper.deleteRout(mRout.routKey());
					if(!mOutcome.equals(ERROR)){
						Toast.makeText(getContext(),mOutcome,Toast.LENGTH_LONG ).show();
						Intent intent = new Intent(getContext(), StartActivity_.class);
						startActivity(intent);
						dialogInterface.dismiss();
					}else{
						Toast.makeText(getContext(),mOutcome,Toast.LENGTH_LONG ).show();
					}

				}else if (mPlace != null) {
					StorageSaveHelper storageSaveHelper = new StorageSaveHelper(getContext(), rootPathString);
					String mOutcome = storageSaveHelper.deletePlace(mPlace.placeKey());
					if(!mOutcome.equals(ERROR)){
						Toast.makeText(getContext(),mOutcome,Toast.LENGTH_LONG ).show();
						Intent intent = new Intent(getContext(), StartActivity_.class);
						startActivity(intent);
						dialogInterface.dismiss();
					}else{
						Toast.makeText(getContext(),mOutcome,Toast.LENGTH_LONG ).show();
					}
				}
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			mAppBarLayout.setExpanded(false, true);
		}
	}


}
