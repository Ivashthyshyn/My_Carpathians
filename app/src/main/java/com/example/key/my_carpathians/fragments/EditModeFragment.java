package com.example.key.my_carpathians.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
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

/**
 .
 */
@EFragment
public class EditModeFragment extends DialogFragment {
	public static final int GALLERY_REQUEST = 1;
	public static final int LIGHT = 1;
	public static final int MEDIUM = 2;
	public static final int HARD = 3;

	private Rout mRout = null ;
	private Place mPlace = null;
	int routsLevel = 0;
	Bitmap mBitmap;
	String name;
	String uriTitlePhoto = null;
	View view;

	@ViewById(R.id.buttonAddPhoto)
	FloatingActionButton buttonAddPhoto;

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

			Glide
				.with(getContext())
				.load(STORAGE_CONSTANT + name)
				.diskCacheStrategy(DiskCacheStrategy.NONE)
				.skipMemoryCache(true)
				.into(imageTitlePhoto);
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
				mBitmap = cropImageView.getCroppedImage();
				imageTitlePhoto.setImageBitmap(mBitmap);


			}
		});


		cropImageView.setOnSetImageUriCompleteListener(new CropImageView.OnSetImageUriCompleteListener() {
			@Override
			public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
				progressView.setVisibility(View.INVISIBLE);
			}
		});
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
				mRout.setUrlRout(uriTitlePhoto);
				CommunicatorActionActivity communicatorActionActivity = (CommunicatorActionActivity)getContext();
				communicatorActionActivity.saveChanges(mRout, null);
				 this.dismiss();
			}else if(mPlace != null) {
				 mPlace.setNamePlace(editTextName.getText().toString());
				 mPlace.setTitlePlace(editTextTitle.getText().toString());
				 savePhotoToSDCard(mPlace.getNamePlace());

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
						 Set<String> createdByUserTrackList = new HashSet<>(mSharedPreferences.getStringSet(CREATED_BY_USER_PLACE_LIST, new HashSet<String>()));
						 if (createdByUserTrackList.add(mPlace.getNamePlace())) {
							 createdByUserTrackList.remove(name);
							 mSharedPreferences.edit().remove(name).apply();
						 }
						 mSharedPreferences.edit().putString(mPlace.getNamePlace(), fileUri).apply();
						 mSharedPreferences.edit().putStringSet(CREATED_BY_USER_PLACE_LIST, createdByUserTrackList).apply();
						 Toast.makeText(getContext(), "Place saved", Toast.LENGTH_LONG).show();
						 CommunicatorActionActivity communicatorActionActivity = (CommunicatorActionActivity) getContext();
						 communicatorActionActivity.saveChanges(null, mPlace);
					 } catch (IOException e) {
						 e.printStackTrace();
					 }
				 }

	}

	private void savePhotoToSDCard(String namePlace) {
		mBitmap = cropImageView.getCroppedImage();
		imageTitlePhoto.setImageBitmap(mBitmap);
		if (mBitmap != null) {
			File rootPath = new File(getContext().getExternalFilesDir(
					Environment.DIRECTORY_DOWNLOADS), "Photos");
			if (!rootPath.exists()) {
				rootPath.mkdirs();
			}

			File file = new File(rootPath, namePlace);
			if (file.exists()) {
				file.delete();
			}
			uriTitlePhoto = String.valueOf(file.toURI());
			try {
				FileOutputStream fileOutputStream = new FileOutputStream(file);
				mBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream);
				fileOutputStream.flush();
				fileOutputStream.close();
				Toast.makeText(getContext(), "Photo saved", Toast.LENGTH_SHORT).show();
				mPlace.setUrlPlace(uriTitlePhoto);
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(getContext(), "Photo do not saved", Toast.LENGTH_SHORT).show();
			}
		}else{
			File file = new File(uriTitlePhoto);
			if (file.exists()) {
				file.renameTo(new File(STORAGE_CONSTANT + namePlace));
			}
		}
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
				groupMorePhoto.setVisibility(View.GONE);
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
}
