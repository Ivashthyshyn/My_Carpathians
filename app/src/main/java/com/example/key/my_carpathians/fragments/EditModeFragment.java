package com.example.key.my_carpathians.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
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
import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.activities.StartActivity_;
import com.example.key.my_carpathians.interfaces.CommunicatorActionActivity;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;
import com.example.key.my_carpathians.utils.ObjectService;
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
import static com.example.key.my_carpathians.utils.ObjectService.ERROR;
import static com.mapbox.mapboxsdk.storage.FileSource.isExternalStorageReadable;

/**
 .
 */
@EFragment
public class EditModeFragment extends DialogFragment {
	public static final int GALLERY_REQUEST = 1;
	public static final int LIGHT = 1;
	public static final int MEDIUM = 2;
	public static final int HARD = 3;
	private static final int TITLE_PHOTO = 0;
	private static final int MORE_PHOTO_1 = 1;
	private static final int MORE_PHOTO_2 = 2;
	private static final int MORE_PHOTO_3 = 3;
	private int mPhotoSwicher = 0;
	private String mTrackLength = null;
	private com.example.key.my_carpathians.models.Position mPositionRout = null;
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

	@ViewById(R.id.buttonDeleteObject)
	FloatingActionButton buttonDeleteObject;
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
	void afterView(){


			if(mPlace != null){
				name = mPlace.getNamePlace();
				uriTitlePhoto = mPlace.getUrlPlace();
				editTextName.setText(name);
				if (uriTitlePhoto != null){
					buttonAddPhoto.setBackgroundResource(R.drawable.ic_exchange);
				}

				radioGroup.setVisibility(View.GONE);
				editTextTitle.setText(mPlace.getTitlePlace());
				Glide
						.with(getContext())
						.load(mPlace.getUrlPlace())
						.into(imageTitlePhoto);
				morePhotos(name);
			}else if (mRout != null){
				name = mRout.getNameRout();
				editTextName.setText(name);
				uriTitlePhoto = mRout.getUrlRout();
				if (uriTitlePhoto != null){
					buttonAddPhoto.setBackgroundResource(R.drawable.ic_exchange);
				}


				Glide
						.with(getContext())
						.load(mRout.getUrlRout())
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
				editTextTitle.setText(mRout.getTitleRout());
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
				buttonDeleteObject.setVisibility(View.VISIBLE);
				editGroup.setVisibility(View.VISIBLE);
				groupMorePhoto.setVisibility(View.VISIBLE);

				switch (mPhotoSwicher){
					case TITLE_PHOTO:
						uriTitlePhoto = savePhotoToSDCard(name, cropImageView.getCroppedImage(), null);
						if (mPlace !=  null){
							mPlace.setUrlPlace(uriTitlePhoto);
						}else if(mRout != null){
							mRout.setUrlRout(uriTitlePhoto);
						}
						Glide
								.with(getContext())
								.load(uriTitlePhoto)
								.into(imageTitlePhoto);
						buttonAddPhoto.setBackgroundResource(R.drawable.ic_exchange);
						break;
					case MORE_PHOTO_1:
						uriPhoto1 = savePhotoToSDCard(name + 1, cropImageView.getCroppedImage(), null);
						Glide
								.with(getContext())
								.load(uriPhoto1)
								.into(imageAdd1);
						break;
					case MORE_PHOTO_2:
						uriPhoto2 = savePhotoToSDCard(name + 2, cropImageView.getCroppedImage(), null);
						Glide
								.with(getContext())
								.load(uriPhoto2)
								.into(imageAdd2);
						break;
					case MORE_PHOTO_3:
						uriPhoto3 = savePhotoToSDCard(name + 3, cropImageView.getCroppedImage(), null);
						Glide
								.with(getContext())
								.load(uriPhoto3)
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
			// Load GeoJSON file
			File file = new File(uri);
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
			Toast.makeText(getContext(),"Не вдалось визначити довжину трека", Toast.LENGTH_SHORT).show();
			mTrackLength = "unknown";
		}


		if (points.size() > 0) {
			mPositionRout = new com.example.key.my_carpathians.models.Position();
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
			mTrackLength = (df.format(dis) + "km");
		} else {
			mTrackLength = "unknown";
		}
	}

	private void morePhotos(String name) {
			Uri rootPathForPhotosString;
			if (isExternalStorageReadable()) {
				rootPathForPhotosString = Uri.fromFile(getContext().getExternalFilesDir(
						Environment.DIRECTORY_DOWNLOADS)).buildUpon().appendPath("Photos").build();
			}else{
				rootPathForPhotosString = Uri.fromFile(getContext().getFilesDir()).buildUpon().appendPath("Photos").build();
			}
			for (int i = 1; i <= 3; i++) {
				File photoFile = new File(rootPathForPhotosString.buildUpon().appendPath(name + String.valueOf(i)).build().getPath());
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
	public void buttonAddPhotoWasClicked(View view){
		if(uriTitlePhoto != null){
			mPhotoSwicher = TITLE_PHOTO;
			showAlertDialog(uriTitlePhoto, true);
		}else {
			mPhotoSwicher = TITLE_PHOTO;
			searchPhotoInGallery();
		}
		}

	private void searchPhotoInGallery() {
		Intent intentFromGallery =new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			 if (editTextName.getText().toString().isEmpty()) {
				Toast.makeText(getContext(), "Будь ласка вкажіть назву, це поле є обов'зковим для введення", Toast.LENGTH_SHORT).show();
			} else if (editTextTitle.getText().toString() == null) {
				Toast.makeText(getContext(), "Будь ласка коротко опишіть обєкт, це поле є обов'зковим для введення", Toast.LENGTH_SHORT).show();
			} else if (routsLevel == 0 && mRout != null) {
				Toast.makeText(getContext(), "Складність є важливим критерієм для вашого обєкта, визначте його", Toast.LENGTH_SHORT).show();
			} else if (uriTitlePhoto == null){
				Toast.makeText(getContext(), "Без титульної фотографії ваш обєкт буде не цілим", Toast.LENGTH_SHORT).show();
			}else if (mRout != null){
				mRout.setNameRout(editTextName.getText().toString());
				mRout.setTitleRout(editTextTitle.getText().toString());
				mRout.setLengthRout(mTrackLength);
				mRout.setPositionRout(mPositionRout);
				mRout.setRoutsLevel(routsLevel);
				mRout.setUrlRout(savePhotoToSDCard(mRout.getNameRout(), null, uriTitlePhoto));
					 savePhotoToSDCard(mRout.getNameRout() + String.valueOf(MORE_PHOTO_1), null, uriPhoto1);
					 savePhotoToSDCard(mRout.getNameRout() + String.valueOf(MORE_PHOTO_2), null, uriPhoto2);
					 savePhotoToSDCard(mRout.getNameRout() + String.valueOf(MORE_PHOTO_3), null, uriPhoto3);

				ObjectService objectService = new ObjectService(getContext(), rootPathString);
				 String outcome = objectService.saveRout(name, null, mRout, true);
				 Toast.makeText(getContext(), outcome, Toast.LENGTH_LONG).show();
				 if (outcome.equals("Rout saved")) {
					 CommunicatorActionActivity communicatorActionActivity = (CommunicatorActionActivity) getContext();
					 communicatorActionActivity.saveChanges(mRout, null);
				 }
			}else if(mPlace != null) {
				 mPlace.setNamePlace(editTextName.getText().toString());
				 mPlace.setTitlePlace(editTextTitle.getText().toString());
				 mPlace.setUrlPlace(savePhotoToSDCard(mPlace.getNamePlace(), null, uriTitlePhoto));
					 savePhotoToSDCard(mPlace.getNamePlace() + MORE_PHOTO_1, null, uriPhoto1);
					 savePhotoToSDCard(mPlace.getNamePlace() + MORE_PHOTO_2, null, uriPhoto2);
					 savePhotoToSDCard(mPlace.getNamePlace() + MORE_PHOTO_3, null, uriPhoto3);

				 ObjectService objectService = new ObjectService(getContext(), rootPathString);
				 String outcome =  objectService.savePlace( name, mPlace, true);
				 Toast.makeText(getContext(), outcome, Toast.LENGTH_LONG).show();
				 if (outcome.equals("Place saved")) {
					 CommunicatorActionActivity communicatorActionActivity = (CommunicatorActionActivity) getContext();
					 communicatorActionActivity.saveChanges(null, mPlace);
				 }

				 }
	}

	private String savePhotoToSDCard(String name, Bitmap bitmap, String uri) {
			if (bitmap != null) {
				Uri rootPathForPhotos;
				if (isExternalStorageReadable()) {
					rootPathForPhotos = Uri.fromFile(getContext().getExternalFilesDir(
							Environment.DIRECTORY_DOWNLOADS)).buildUpon().appendPath("Photos").build();
				}else{
					rootPathForPhotos = Uri.fromFile(getContext().getFilesDir()).buildUpon().appendPath("Photos").build();
				}

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
					Toast.makeText(getContext(), "Photo saved", Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(getContext(), "Photo do not saved", Toast.LENGTH_SHORT).show();
					return null;
				}
			} else if(uri != null ) {
				File file = new File(uri);
				File newFile ;
				if (file.exists()) {
					Uri rootPathForTitlePhotoString = Uri.fromFile(getContext().getExternalFilesDir(
							Environment.DIRECTORY_DOWNLOADS)).buildUpon().appendPath("Photos").build();
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
				buttonDeleteObject.setVisibility(View.GONE);
				cropImageView.setImageUriAsync(selectedImage);
				progressViewText.setText("Loading...");
				progressView.setVisibility(View.VISIBLE);
			}
		}
	}

	@Click(R.id.buttonBakCrop)
	public void buttonBakCropWasClicked(){
		cropImageView.clearImage();
		cropToolsFrame.setVisibility(View.GONE);
		buttonSaveData.setVisibility(View.VISIBLE);
		buttonDeleteObject.setVisibility(View.VISIBLE);
		groupMorePhoto.setVisibility(View.VISIBLE);
		editGroup.setVisibility(View.VISIBLE);
	}
	@Click(R.id.imageAdd1)
	public void imageAdd1WasClicked(View view){
		if (uriPhoto1 != null){
			mPhotoSwicher = MORE_PHOTO_1;
			showAlertDialog(uriPhoto1, false);
		}else {
			mPhotoSwicher = MORE_PHOTO_1;
			searchPhotoInGallery();
		}
	}

	private void showAlertDialog(final String uriPhoto, boolean necessarily) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("Photo");
		if (necessarily){
			builder.setMessage("Титульна фотографія є обов'язковую якщо ви зашочете зробити свій обєкт публічним");
		}else{
			builder.setMessage("Додаткові фотографії не є обовязковими для публікації");
		}

		builder.setPositiveButton("Вибрати", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				searchPhotoInGallery();
				dialogInterface.dismiss();
			}
		});
		builder.setNegativeButton("Видалити", new DialogInterface.OnClickListener() {
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
	public void imageAdd2WasClicked(View view){
		if (uriPhoto2 != null){
			mPhotoSwicher = MORE_PHOTO_2;
			showAlertDialog(uriPhoto2, false);
		}else {
			mPhotoSwicher = MORE_PHOTO_2;
			searchPhotoInGallery();
		}
	}
	@Click(R.id.imageAdd3)
	public void imageAdd3WasClicked(View view){
		if (uriPhoto3 != null){
			mPhotoSwicher = MORE_PHOTO_3;
			showAlertDialog(uriPhoto3, false);
		}else {
			mPhotoSwicher = MORE_PHOTO_3;
			searchPhotoInGallery();
		}
	}
	@Click(R.id.buttonDeleteObject)
	public void fabDeleteCreatedObject(){
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("Deleting!");
		if (mRout != null){
			builder.setMessage("Do You really want to delete Rout " + mRout.getNameRout() );

		}else if (mPlace != null) {
			builder.setMessage("Do You really want to delete Place " + mPlace.getNamePlace() );
		}
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				if (mRout != null){
					ObjectService objectService = new ObjectService(getContext(), rootPathString);
					String mOutcome = objectService.deleteRout(mRout.getNameRout());
					if(!mOutcome.equals(ERROR)){
						Toast.makeText(getContext(),mOutcome,Toast.LENGTH_LONG ).show();
						Intent intent = new Intent(getContext(), StartActivity_.class);
						startActivity(intent);
						dialogInterface.dismiss();
					}else{
						Toast.makeText(getContext(),mOutcome,Toast.LENGTH_LONG ).show();
					}

				}else if (mPlace != null) {
					ObjectService objectService = new ObjectService(getContext(), rootPathString);
					String mOutcome = objectService.deletePlace(mPlace.getNamePlace());
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
}
