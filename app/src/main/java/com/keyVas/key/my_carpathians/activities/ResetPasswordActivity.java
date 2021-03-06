package com.keyVas.key.my_carpathians.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.keyVas.key.my_carpathians.R;
import com.keyVas.key.my_carpathians.utils.LocaleHelper;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity
public class ResetPasswordActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @ViewById(R.id.email1)
    EditText inputEmail;

    @ViewById(R.id.progressBar1)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        auth = FirebaseAuth.getInstance();

    }

    @Click(R.id.buttonResetPassword1)
    public void buttonResetPasswordWasClicked(){

        String email = inputEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplication(),
                    getString(R.string.registered_email_id), Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ResetPasswordActivity.this,
                                    getString(R.string.sent_instructions), Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Toast.makeText(ResetPasswordActivity.this,
                                    getString(R.string.failed_reset_email), Toast.LENGTH_SHORT)
                                    .show();
                        }

                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
    @Click(R.id.buutonBack)
    public void buttonBackWasClicked(){
        finish();
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }
}
