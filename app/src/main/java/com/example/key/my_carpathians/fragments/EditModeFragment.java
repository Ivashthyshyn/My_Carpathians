package com.example.key.my_carpathians.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.interfaces.CommunicatorActionActivity;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 .
 */
@EFragment
public class EditModeFragment extends DialogFragment {
	private static final int GALLERY_REQUEST = 1;
	Place place = null;
	Rout rout = null;

	@ViewById(R.id.buttonChengUrlPhoto)
	Button buttonChangUrlPhoto;

	@ViewById(R.id.editTextName)
	EditText editTextName;

	@ViewById(R.id.editTextTitle)
	EditText editTextTitle;

	@ViewById(R.id.textViewStringUrl)
	TextView textViewStringUrl;

	@ViewById(R.id.fabOk)
	FloatingActionButton fabOk;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_edit_mode, container, false);
	}

	@AfterViews
	void afterView(){
		if (place == null && rout != null){
			editTextName.setText(rout.getNameRout());
			textViewStringUrl.setText(rout.getUrlRout());

		}else if (rout == null && place != null){
			editTextName.setText(place.getNamePlace());
			textViewStringUrl.setText(place.getUrlPlace());
		}
	}

    public void setData(Rout rout, Place place){
		this.rout = rout;
	    this.place = place;
	}
	@Click(R.id.buttonChengUrlPhoto)
	public void buttonChangUrlPhotoWasClicked(View view){
		Intent intentFromGalery =new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intentFromGalery.setType("image/*");
		startActivityForResult(intentFromGalery, GALLERY_REQUEST);
		}
	@Click(R.id.fabOk)
	public void fabOkWasClicked(View view){
		if (place == null){
			if (editTextName.getText().toString().isEmpty() ){
				Toast.makeText(getContext(),"Будь ласка вкажіть назву, це поле є обов'зковим для введення", Toast.LENGTH_SHORT).show();
			}else {
				rout.setNameRout(editTextName.getText().toString());
				rout.setTitleRout(editTextTitle.getText().toString());
				CommunicatorActionActivity communicatorActionActivity = (CommunicatorActionActivity) getContext();
				communicatorActionActivity.saveChanges(rout, place);
				this.dismiss();
			}
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GALLERY_REQUEST
				&& resultCode == Activity.RESULT_OK) {
			Uri selectedImage = data.getData();
			textViewStringUrl.setText(selectedImage.toString());

			Bitmap yourSelectedImage = null;
			try {
				yourSelectedImage = MediaStore.Images.Media.getBitmap(
						getContext().getContentResolver(), selectedImage);
			} catch (IOException e) {
				e.printStackTrace();
			}
			File rootPath = new File(getContext().getExternalFilesDir(
					Environment.DIRECTORY_DOWNLOADS), "Photos");
			if (!rootPath.exists()) {
				rootPath.mkdirs();
			}

			File file = new File(rootPath, rout.getNameRout() + ".jpg");
			if (file.exists()) {
				file.delete();
			}
				String uri = String.valueOf(file.toURI());
				try {
					FileOutputStream fileOutputStream = new FileOutputStream(file);
					yourSelectedImage.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream);
					fileOutputStream.flush();
					fileOutputStream.close();
					rout.setUrlRout(uri);
					Toast.makeText(getContext(), "Photo saved", Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(getContext(), "Photo do not saved", Toast.LENGTH_SHORT).show();
				}


		}
	}
}
