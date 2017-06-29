package com.example.key.my_carpathians.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.key.my_carpathians.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
@EActivity
public class SettingsActivity extends AppCompatActivity {

    private FirebaseUser user;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    
    @ViewById(R.id.oldEmail)
    EditText oldEmail;

    @ViewById(R.id.newEmail)
    EditText newEmail;

    @ViewById(R.id.password3)
    EditText password;

    @ViewById(R.id.newPassword)
    EditText newPassword;
    
    @ViewById(R.id.progressBar3)
    ProgressBar progressBar;
    
    @ViewById(R.id.changeEmail)
    Button changeEmail;

    @ViewById(R.id.changePass)
    Button changePassword;

    @ViewById(R.id.send)
    Button sendEmail;

    @ViewById(R.id.remove)
    Button remove;

    @ViewById(R.id.signOut)
    Button signOut;

    @ViewById(R.id.buttonChangeEmail)
    Button buttonChangeEmail;

    @ViewById(R.id.buttonChangePassword3)
    Button buttonChangePassword3;

    @ViewById(R.id.buttonSendingPassReset)
    Button buttonSendingPassReset;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        user = FirebaseAuth.getInstance().getCurrentUser();


        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(SettingsActivity.this, LoginActivity_.class));
                    finish();
                }
            }
        };

        //   Define the provider
        if (user.getProviders().get(0).equals("password")) {
            oldEmail.setVisibility(View.GONE);
            newEmail.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            newPassword.setVisibility(View.GONE);
            changeEmail.setVisibility(View.GONE);
            changePassword.setVisibility(View.GONE);
            sendEmail.setVisibility(View.GONE);
            remove.setVisibility(View.GONE);

            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        }else{
                buttonChangeEmail.setVisibility(View.GONE);
                buttonChangePassword3.setVisibility(View.GONE);
                buttonSendingPassReset.setVisibility(View.GONE);
                oldEmail.setVisibility(View.GONE);
                newEmail.setVisibility(View.GONE);
                password.setVisibility(View.GONE);
                newPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.GONE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.GONE);
                remove.setVisibility(View.GONE);

                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
        }
    }
    @Click(R.id.buttonChangeEmail)
    public void buttonChangeEmailWasClicked(){
        oldEmail.setVisibility(View.GONE);
        newEmail.setVisibility(View.VISIBLE);
        password.setVisibility(View.GONE);
        newPassword.setVisibility(View.GONE);
        changeEmail.setVisibility(View.VISIBLE);
        changePassword.setVisibility(View.GONE);
        sendEmail.setVisibility(View.GONE);
        remove.setVisibility(View.GONE);
    }
    @Click(R.id.buttonChangePassword3)
    public void buttonChangePasswordWasClicked(){
        oldEmail.setVisibility(View.GONE);
        newEmail.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        newPassword.setVisibility(View.VISIBLE);
        changeEmail.setVisibility(View.GONE);
        changePassword.setVisibility(View.VISIBLE);
        sendEmail.setVisibility(View.GONE);
        remove.setVisibility(View.GONE);
    }

    @Click(R.id.buttonSendingPassReset)
    public void buttonSendingPassResetWasClicked(){
        oldEmail.setVisibility(View.VISIBLE);
        newEmail.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        newPassword.setVisibility(View.GONE);
        changeEmail.setVisibility(View.GONE);
        changePassword.setVisibility(View.GONE);
        sendEmail.setVisibility(View.VISIBLE);
        remove.setVisibility(View.GONE);
    }

    @Click(R.id.buttonRemoveUserButton)
    public void buttonRemoveUserButtonWasClicked(){
        progressBar.setVisibility(View.VISIBLE);
        if (user != null) {
            user.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SettingsActivity.this, "Your profile is deleted:( Create a account now!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SettingsActivity.this, SignupActivity.class));
                                finish();
                                progressBar.setVisibility(View.GONE);
                            } else {
                                Toast.makeText(SettingsActivity.this, "Failed to delete your account!", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
        }  
    }

    @Click(R.id.changeEmail)
    public void changeEmailWasClicked(){
        progressBar.setVisibility(View.VISIBLE);
        if (user != null && !newEmail.getText().toString().trim().equals("")) {
            user.updateEmail(newEmail.getText().toString().trim())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SettingsActivity.this, "Email address is updated. Please sign in with new email id!", Toast.LENGTH_LONG).show();
                                signOut();
                                progressBar.setVisibility(View.GONE);
                            } else {
                                Toast.makeText(SettingsActivity.this, "Failed to update email!", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
        } else if (newEmail.getText().toString().trim().equals("")) {
            newEmail.setError("Enter email");
            progressBar.setVisibility(View.GONE);
        }
    }

    @Click(R.id.changePass)
    public void changePassWasClicked(){
        progressBar.setVisibility(View.VISIBLE);
        if (user != null && !newPassword.getText().toString().trim().equals("")) {
            if (newPassword.getText().toString().trim().length() < 6) {
                newPassword.setError("Password too short, enter minimum 6 characters");
                progressBar.setVisibility(View.GONE);
            } else {
                user.updatePassword(newPassword.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SettingsActivity.this, "Password is updated, sign in with new password!", Toast.LENGTH_SHORT).show();
                                    signOut();
                                    progressBar.setVisibility(View.GONE);
                                } else {
                                    Toast.makeText(SettingsActivity.this, "Failed to update password!", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        } else if (newPassword.getText().toString().trim().equals("")) {
            newPassword.setError("Enter password");
            progressBar.setVisibility(View.GONE);
        }
    }

    @Click(R.id.send)
    public void sendWasClicked(){
        progressBar.setVisibility(View.VISIBLE);
        if (!oldEmail.getText().toString().trim().equals("")) {
            auth.sendPasswordResetEmail(oldEmail.getText().toString().trim())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SettingsActivity.this, "Reset password email is sent!", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            } else {
                                Toast.makeText(SettingsActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
        } else {
            oldEmail.setError("Enter email");
            progressBar.setVisibility(View.GONE);
        }
    }



    @Click(R.id.signOut)
    public void signOutWasClicked(){
        signOut();
    }
    
    //sign out method
    public void signOut() {
        auth.signOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
}