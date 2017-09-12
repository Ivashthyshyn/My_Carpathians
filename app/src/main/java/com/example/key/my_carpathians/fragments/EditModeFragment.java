package com.example.key.my_carpathians.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.interfaces.CommunicatorActionActivity;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.example.key.my_carpathians.activities.ActionActivity.STORAGE_CONSTANT;
import static com.example.key.my_carpathians.activities.StartActivity.PREFS_NAME;
import static com.example.key.my_carpathians.utils.LocationService.CREATED_BY_USER_PLACE_LIST;
import static com.example.key.my_carpathians.utils.LocationService.CREATED_BY_USER_ROUT_LIST;

/**
 .
 */
@EFragment
public class EditModeFragment extends DialogFragment {
	public static final int GALLERY_REQUEST = 1;
	public static final int LIGHT = 1;
	public static final int MEDIUM = 2;
	public static final int HARD = 3;
	public static final String NO_PUBLISH_CONSTANT = "_";
	private static final int TITLE_PHOTO = 0;
	private static final int MORE_PHOTO_1 = 1;
	private static final int MORE_PHOTO_2 = 2;
	private static final int MORE_PHOTO_3 = 3;
	private int mPhotoSwicher = 0;

	private Rout mRout = null ;
	private Place mPlace = null;
	int routsLevel = 0;
	Bitmap bitmap = null;
	Bitmap bitmap1 = null;
	Bitmap bitmap2 = null;
	Bitmap bitmap3 = null;
	String uriPhoto1 = null;
	String uriPhoto2 = null;
	String uriPhoto3 = null;
	String name;
	String uriTitlePhoto = null;
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
	Button buttonSaveData;

	@ViewById(R.id.cropImage)
	CropImageView cropImageView;

	@ViewById(R.id.cropTools)
	LinearLayout cropTools;

	@ViewById(R.id.croperGroup)
	FrameLayout croperGroup;

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



	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                     Bundle savedInstanceState) {
		view =inflater.inflate(R.layout.fragment_edit_mode, container, false);
		return view;
	}

	@AfterViews
	void afterView(){
			editTextName.setText(name);
		if (uriTitlePhoto != null){
			buttonAddPhoto.setBackgroundResource(R.drawable.ic_exchange);
		}

			Glide
				.with(getContext())
				.load(STORAGE_CONSTANT + name)
				.diskCacheStrategy(DiskCacheStrategy.NONE)
				.skipMemoryCache(true)
				.into(imageTitlePhoto);
			morePhotos(name);
			if(mPlace != null){
				radioGroup.setVisibility(View.GONE);
				editTextTitle.setText(mPlace.getTitlePlace());
			}else if (mRout != null){
				radioGroup.setVisibility(View.VISIBLE);
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
				croperGroup.setVisibility(View.GONE);
				cropTools.setVisibility(View.GONE);
				editGroup.setVisibility(View.VISIBLE);
				groupMorePhoto.setVisibility(View.VISIBLE);

				switch (mPhotoSwicher){
					case TITLE_PHOTO:
						uriTitlePhoto = "";
						bitmap = cropImageView.getCroppedImage();
						imageTitlePhoto.setImageBitmap(bitmap);
						buttonAddPhoto.setBackgroundResource(R.drawable.ic_exchange);
						break;
					case MORE_PHOTO_1:
						bitmap1 = cropImageView.getCroppedImage();
						imageAdd1.setImageBitmap(bitmap1);
						break;
					case MORE_PHOTO_2:
						bitmap2 = cropImageView.getCroppedImage();
						imageAdd2.setImageBitmap(bitmap2);
						break;
					case MORE_PHOTO_3:
						bitmap3= cropImageView.getCroppedImage();
						imageAdd3.setImageBitmap(bitmap3);
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
	private void morePhotos(String name) {

			for (int i = 1; i <= 3; i++) {
				File photoFile = new File(STORAGE_CONSTANT,  name + String.valueOf(i));
				if (photoFile.exists()) {
					Uri uri = Uri.fromFile(photoFile);
					switch (i){
						case 1: imageAdd1.setImageURI(uri);
							uriPhoto1 = uri.toString();
							break;
						case 2: imageAdd2.setImageURI(uri);
							uriPhoto2 = uri.toString();
							break;
						case 3: imageAdd3.setImageURI(uri);
							uriPhoto3 = uri.toString();
					}
				}
			}

	}
    public void setData(Rout rout, Place place){

	    if (rout != null){
		    mRout = rout;
		    name = rout.getNameRout();
		    uriTitlePhoto = rout.getUrlRout();
	    }else if(place != null){
		    mPlace = place;
		    name = place.getNamePlace();
		    uriTitlePhoto = place.getUrlPlace();
	    }
	}
	@Click(R.id.buttonAddPhoto)
	public void buttonAddPhotoWasClicked(View view){
		mPhotoSwicher = TITLE_PHOTO;
		 searchPhotoInGallery();
		}

	private void searchPhotoInGallery() {
		Intent intentFromGalery =new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intentFromGalery.setType("image/*");
		startActivityForResult(intentFromGalery, GALLERY_REQUEST);
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
				mRout.setRoutsLevel(routsLevel);
				mRout.setUrlRout(savePhotoToSDCard(mRout.getNameRout(), bitmap, uriTitlePhoto));
				 if(bitmap1 != null | bitmap2 != null | bitmap3 != null ){
					 savePhotoToSDCard(mRout.getNameRout() + String.valueOf(MORE_PHOTO_1), bitmap1, uriPhoto1);
					 savePhotoToSDCard(mRout.getNameRout() + String.valueOf(MORE_PHOTO_2), bitmap2, uriPhoto2);
					 savePhotoToSDCard(mRout.getNameRout() + String.valueOf(MORE_PHOTO_3), bitmap3, uriPhoto3);
				 }
				 File rootPath = new File(getContext().getExternalFilesDir(
						 Environment.DIRECTORY_DOWNLOADS), "Created");
				 if (!rootPath.exists()) {
					 rootPath.mkdirs();
				 }

				 File file = new File(rootPath, mRout.getNameRout() + NO_PUBLISH_CONSTANT);
				 String fileUri = String.valueOf(file.toURI());
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
					 objectOutputStream.writeObject(mRout);
					 objectOutputStream.close();
					 fileOutputStream.close();
					 SharedPreferences mSharedPreferences = getContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
					 Set<String> createdByUserTrackList = new HashSet<>(mSharedPreferences.getStringSet(CREATED_BY_USER_ROUT_LIST, new HashSet<String>()));
					 if (createdByUserTrackList.add(mRout.getNameRout())) {
						 createdByUserTrackList.remove(name);
						 mSharedPreferences.edit().remove(name).apply();
					 }
					 mSharedPreferences.edit().putString(mRout.getNameRout(), fileUri).apply();
					 mSharedPreferences.edit().putStringSet(CREATED_BY_USER_ROUT_LIST, createdByUserTrackList).apply();
					 Toast.makeText(getContext(), "Rout saved", Toast.LENGTH_LONG).show();
					 CommunicatorActionActivity communicatorActionActivity = (CommunicatorActionActivity)getContext();
					 communicatorActionActivity.saveChanges(mRout, null);
				 } catch (IOException e) {
					 e.printStackTrace();
				 }




			}else if(mPlace != null) {
				 mPlace.setNamePlace(editTextName.getText().toString());
				 mPlace.setTitlePlace(editTextTitle.getText().toString());
				 mPlace.setUrlPlace(savePhotoToSDCard(mPlace.getNamePlace(), bitmap, uriTitlePhoto));
				 if(bitmap1 != null | bitmap2 != null | bitmap3 != null ){
					 savePhotoToSDCard(mRout.getNameRout() + MORE_PHOTO_1, bitmap1, uriPhoto1);
					 savePhotoToSDCard(mRout.getNameRout() + MORE_PHOTO_2, bitmap2, uriPhoto2);
					 savePhotoToSDCard(mRout.getNameRout() + MORE_PHOTO_3, bitmap3, uriPhoto3);
				 }
				 File rootPath = new File(getContext().getExternalFilesDir(
						 Environment.DIRECTORY_DOWNLOADS), "Created");
				 if (!rootPath.exists()) {
					 rootPath.mkdirs();
				 }

				 File file = new File(rootPath, mPlace.getNamePlace());
				 String fileUri = String.valueOf(file.toURI());
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
						 objectOutputStream.writeObject(mPlace);
						 objectOutputStream.close();
						 fileOutputStream.close();
						 SharedPreferences mSharedPreferences = getContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
						 Set<String> createdByUserPlaceList = new HashSet<>(mSharedPreferences.getStringSet(CREATED_BY_USER_PLACE_LIST, new HashSet<String>()));
						 if (createdByUserPlaceList.add(mPlace.getNamePlace())) {
							 createdByUserPlaceList.remove(name);
							 mSharedPreferences.edit().remove(name).apply();
						 }
						 mSharedPreferences.edit().putString(mPlace.getNamePlace(), fileUri).apply();
						 mSharedPreferences.edit().putStringSet(CREATED_BY_USER_PLACE_LIST, createdByUserPlaceList).apply();
						 Toast.makeText(getContext(), "Place saved", Toast.LENGTH_LONG).show();
						 CommunicatorActionActivity communicatorActionActivity = (CommunicatorActionActivity) getContext();
						 communicatorActionActivity.saveChanges(null, mPlace);
					 } catch (IOException e) {
						 e.printStackTrace();
					 }
				 }

	}

	private String savePhotoToSDCard(String namePlace, Bitmap bitmap, String uri) {
			if (bitmap != null) {
				File rootPath = new File(getContext().getExternalFilesDir(
						Environment.DIRECTORY_DOWNLOADS), "Photos");
				if (!rootPath.exists()) {
					rootPath.mkdirs();
				}

				File file = new File(rootPath, namePlace);
				if (file.exists()) {
					file.delete();
				}
				uri = String.valueOf(file.toURI());
				try {
					FileOutputStream fileOutputStream = new FileOutputStream(file);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream);
					fileOutputStream.flush();
					fileOutputStream.close();
					Toast.makeText(getContext(), "Photo saved", Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(getContext(), "Photo do not saved", Toast.LENGTH_SHORT).show();
				}
			} else {
				File file = new File(uri);
				if (file.exists()) {
					file.renameTo(new File(STORAGE_CONSTANT + namePlace));
				}
			}
		return uri;
	}

	@Click(R.id.buttonCrop)
	public void buttonCrop(){
	cropImageView.getCroppedImageAsync();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GALLERY_REQUEST
				&& resultCode == RESULT_OK) {
			Uri selectedImage = data.getData();
			if ( selectedImage!= null) {
				editGroup.setVisibility(View.GONE);
				croperGroup.setVisibility(View.VISIBLE);
				cropTools.setVisibility(View.VISIBLE);
				cropImageView.setImageUriAsync(selectedImage);
				progressViewText.setText("Loading...");
				progressView.setVisibility(View.VISIBLE);
			}
		}
	}

	@Click(R.id.buttonBakCrop)
	public void buttonBakCropWasClicked(){
		cropImageView.clearImage();
		croperGroup.setVisibility(View.GONE);
		cropTools.setVisibility(View.GONE);
		groupMorePhoto.setVisibility(View.VISIBLE);
		editGroup.setVisibility(View.VISIBLE);
	}
	@Click(R.id.imageAdd1)
	public void imageAdd1WasClicked(View view){
		if (uriPhoto1 != null){
			showAlertDialog(uriPhoto1);
		}
		mPhotoSwicher = MORE_PHOTO_1;
		searchPhotoInGallery();
	}

	private void showAlertDialog(final String uriPhoto1) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				builder.setTitle("Photo");
		builder.setPositiveButton("Поміняти", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				searchPhotoInGallery();
			}
		});
		builder.setNegativeButton("Видалити", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {

			}
		});
	}

	@Click(R.id.imageAdd2)
	public void imageAdd2WasClicked(View view){
		if (uriPhoto2 != null){
			showAlertDialog(uriPhoto2);
		}
		mPhotoSwicher = MORE_PHOTO_2;
		searchPhotoInGallery();
	}
	@Click(R.id.imageAdd3)
	public void imageAdd3WasClicked(View view){
		if (uriPhoto3 != null){
			showAlertDialog(uriPhoto3);
		}
		mPhotoSwicher = MORE_PHOTO_3;
		searchPhotoInGallery();
	}
}
