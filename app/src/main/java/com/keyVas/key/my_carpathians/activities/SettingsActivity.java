package com.keyVas.key.my_carpathians.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.keyVas.key.my_carpathians.R;
import com.keyVas.key.my_carpathians.utils.LocaleHelper;
import com.shawnlin.numberpicker.NumberPicker;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Arrays;

import static com.keyVas.key.my_carpathians.activities.StartActivity.LOGIN_NAME;
import static com.keyVas.key.my_carpathians.activities.StartActivity.PREFS_NAME;
import static com.keyVas.key.my_carpathians.activities.StartActivity.RC_SIGN_IN;
import static com.keyVas.key.my_carpathians.models.Place.EN;
import static com.keyVas.key.my_carpathians.models.Place.RU;
import static com.keyVas.key.my_carpathians.models.Place.UA;
import static com.keyVas.key.my_carpathians.utils.LocaleHelper.SELECTED_LANGUAGE;

@EActivity
public class SettingsActivity extends AppCompatActivity implements
		GoogleApiClient.OnConnectionFailedListener , RadioGroup.OnCheckedChangeListener{
	public static final String VALUE_PLACE_AROUND_RADIUS = "value_place_around_radius";
	public static final String VALUE_ROUT_AROUND_RADIUS = "value_rout_around_radius";
	public static final String VALUE_OFFLINE_REGION_AROUND_RADIUS = "value_offline_region_radius";
	public static final int AVERAGE_VALUE = 30;
	public SharedPreferences sharedPreferences;
	@ViewById(R.id.radioGroup)
	RadioGroup radioGroup;
	@ViewById(R.id.pickerOfPlacesAround)
	NumberPicker pickerOfPlacesAround;
	@ViewById(R.id.pickerOfRoutesAround)
	NumberPicker pickerOfRoutesAround;
	@ViewById(R.id.pickerOfRegion)
	NumberPicker pickerOfRegion;
	@ViewById(R.id.toolbarSettingsActivity)
	Toolbar toolbar;
	private CallbackManager mCallbackManager;
	private GoogleApiClient mGoogleApiClient;
	private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
	private LoginManager facebookLoginManager;
	private AlertDialog alert;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		setSupportActionBar(toolbar);
		toolbar.showOverflowMenu();
		setTitle(R.string.settings);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		setupLanguageRadioGroup();
		pickerOfRegion.setValue(sharedPreferences.getInt(VALUE_OFFLINE_REGION_AROUND_RADIUS, AVERAGE_VALUE));
		pickerOfPlacesAround.setValue(sharedPreferences.getInt(VALUE_PLACE_AROUND_RADIUS, AVERAGE_VALUE));
		pickerOfRoutesAround.setValue(sharedPreferences.getInt(VALUE_ROUT_AROUND_RADIUS, AVERAGE_VALUE));
		pickerOfRoutesAround.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				sharedPreferences.edit().putInt(VALUE_ROUT_AROUND_RADIUS,  pickerOfRoutesAround.getValue()).apply();
			}
		});
		pickerOfPlacesAround.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				sharedPreferences.edit().putInt(VALUE_PLACE_AROUND_RADIUS, pickerOfPlacesAround.getValue()).apply();
			}
		});
		pickerOfRegion.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				sharedPreferences.edit().putInt(VALUE_OFFLINE_REGION_AROUND_RADIUS,  pickerOfRegion.getValue()).apply();
			}
		});
	}

	private void setupLanguageRadioGroup() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

		switch (preferences.getString(SELECTED_LANGUAGE, EN)){
			case UA : radioGroup.check(R.id.radioButtonUk);
				break;
			case RU : radioGroup.check(R.id.radioButtonRu);
				break;
			case EN : radioGroup.check(R.id.radioButtonEn);
				break;
			default: radioGroup.check(R.id.radioButtonEn);
				break;
		}
		radioGroup.setOnCheckedChangeListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.settings_menu, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_factory_settings:
				sharedPreferences.edit().putInt(VALUE_ROUT_AROUND_RADIUS, AVERAGE_VALUE).apply();
				sharedPreferences.edit().putInt(VALUE_PLACE_AROUND_RADIUS, AVERAGE_VALUE).apply();
				sharedPreferences.edit().putInt(VALUE_OFFLINE_REGION_AROUND_RADIUS,  AVERAGE_VALUE).apply();
				buttonDeleteAccountWasClicked();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Click(R.id.buttonDeleteAccount)
	public void buttonDeleteAccountWasClicked(){
		showDeleteAccountDialog();
	}

	private void showDeleteAccountDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Reauthentication!");
		if (isOnline()) {
			builder.setMessage("You must pass authentication ");
			builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (mUser == null) {
						Toast.makeText(SettingsActivity.this, "You are not autentification", Toast.LENGTH_SHORT).show();
					} else if (mUser.isAnonymous()) {
						Toast.makeText(SettingsActivity.this, "You are not autentification", Toast.LENGTH_SHORT).show();
					} else {
						if (mUser.getProviders().get(0).equals("google.com")) {
							loginGoogle();

						} else if (mUser.getProviders().get(0).equals("facebook.com")) {
							loginFacebook();

						} else if (mUser.getProviders().get(0).equals("password")) {
							showLoginDialog();
						}
					}
				}
			});
		}else {
			builder.setMessage("No access to the internet");
			builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intentSettingsNetwork = new Intent(Intent.ACTION_MAIN);
					intentSettingsNetwork.setClassName("com.android.phone", "com.android.phone.NetworkSetting");
					startActivity(intentSettingsNetwork);
				}
			});
		}
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	private void showLoginDialog() {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Log in !");
			builder.setMessage("Please log in to delete your account");
		final EditText emailInput = new EditText(this);
		final EditText passwordInput = new EditText(this);
		LinearLayout linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.addView(emailInput);
		linearLayout.addView(passwordInput);
			builder.setView(linearLayout);

			emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
			emailInput.setText(sharedPreferences.getString(LOGIN_NAME, null));
			passwordInput.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);

			builder.setPositiveButton("oK",null);
			builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.dismiss();
				}
			});
			alert = builder.create();
			alert.show();
			alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (emailInput.getText().toString().equals("") ){
						emailInput.setError("Enter name");
					}else if (passwordInput.getText().toString().equals("")) {
						passwordInput.setError("Enter password");
					}else {
						AuthCredential credential = EmailAuthProvider.getCredential(emailInput.getText().toString(), passwordInput.getText().toString());
						mUser.reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
							@Override
							public void onSuccess(Void aVoid) {
								Toast.makeText(SettingsActivity.this, "user was deleted", Toast.LENGTH_SHORT).show();
								mUser.delete();
								sharedPreferences.edit().putString(LOGIN_NAME, null);
								alert.dismiss();
								startActivity(new Intent(SettingsActivity.this, SettingsActivity_.class));

							}
						}).addOnFailureListener(new OnFailureListener() {
							@Override
							public void onFailure(@NonNull Exception e) {
								Toast.makeText(SettingsActivity.this, "not a valid login or password", Toast.LENGTH_SHORT).show();
							}
						});
					}
				}
			});
	}

	private void loginGoogle() {

		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(getString(R.string.default_web_client_id))
				.requestEmail()
				.build();
		// [END config_signin]
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
					.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
					.build();
		}
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
		startActivityForResult(signInIntent, RC_SIGN_IN);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Pass the activity result back to the Facebook SDK
		if (mCallbackManager != null) {
			mCallbackManager.onActivityResult(requestCode, resultCode, data);
		}
		// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
		if (requestCode == RC_SIGN_IN) {
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
			if (result.isSuccess()) {
				// Google Sign In was successful, authenticate with Firebase
				GoogleSignInAccount account = result.getSignInAccount();
				firebaceAuthWithGoogle(account);
			} else {
				Toast.makeText(SettingsActivity.this,
						"Login not success", Toast.LENGTH_SHORT).show();
			}
		}
	}
	private void firebaceAuthWithGoogle(final GoogleSignInAccount acct) {


		AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
		mUser.reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				Toast.makeText(SettingsActivity.this, "user was deleted", Toast.LENGTH_SHORT).show();
				mUser.delete();
				startActivity(new Intent(SettingsActivity.this, SettingsActivity_.class));
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Toast.makeText(SettingsActivity.this, "an error occurred", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void loginFacebook() {
			// loginFacebook.setVisibility(View.VISIBLE);

			mCallbackManager = CallbackManager.Factory.create();

			//loginFacebook.setReadPermissions("email");
			// Callback registration
			facebookLoginManager = LoginManager.getInstance();
			facebookLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
				@Override
				public void onSuccess(LoginResult loginResult) {
					Toast toast = Toast.makeText(SettingsActivity.this, "Logged In", Toast.LENGTH_SHORT);
					handleFacebookAccessToken(loginResult.getAccessToken());
					toast.show();
				}

				@Override
				public void onCancel() {

				}

				@Override
				public void onError(FacebookException exception) {

				}

			});

		facebookLoginManager.logInWithReadPermissions(SettingsActivity.this, Arrays.asList("public_profile", "email"));
	}
	private void handleFacebookAccessToken(AccessToken token) {
		//Log.d(TAG, "handleFacebookAccessToken:" + token)

		AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
		mUser.reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				Toast.makeText(SettingsActivity.this, "user was deleted", Toast.LENGTH_SHORT).show();
				mUser.delete();
				startActivity(new Intent(SettingsActivity.this, SettingsActivity_.class));
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Toast.makeText(SettingsActivity.this, "an error occurred", Toast.LENGTH_SHORT).show();
			}
		});

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Toast.makeText(SettingsActivity.this, "connection failed", Toast.LENGTH_SHORT).show();
	}
	public boolean isOnline() {
		boolean connected = false;
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) SettingsActivity.this
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
			connected = networkInfo != null && networkInfo.isAvailable() &&
					networkInfo.isConnected();
			return connected;


		} catch (Exception e) {
			System.out.println("CheckConnectivity Exception: " + e.getMessage());
			Log.v("connectivity", e.toString());
		}
		return connected;
	}


	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = preferences.edit();
		switch (checkedId){
			case R.id.radioButtonUk :
				editor.putString(SELECTED_LANGUAGE, UA);
				editor.apply();
				break;
			case R.id.radioButtonRu :
				editor.putString(SELECTED_LANGUAGE, RU);
				editor.apply();
				break;
			case R.id.radioButtonEn :
				editor.putString(SELECTED_LANGUAGE, EN);
				editor.apply();
				break;
		}
		Intent refresh = new Intent(this, SettingsActivity_.class);
		startActivity(refresh);
		finish();
	}
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(LocaleHelper.onAttach(base));
	}
}

