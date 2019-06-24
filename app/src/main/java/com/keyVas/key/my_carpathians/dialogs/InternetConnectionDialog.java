package com.keyVas.key.my_carpathians.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import com.keyVas.key.my_carpathians.R;

public class InternetConnectionDialog extends DialogFragment {

    public static final String TAG = "InternetConnectionDialo";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.internet));
        builder.setMessage(getString(R.string.need_internet_to_use));
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss());
        builder.setNegativeButton(getString(R.string.settings), (dialog, which) -> onShowSettings() );
        return builder.create();
    }

        void onShowSettings(){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName("com.android.phone",
                    "com.android.phone.NetworkSetting");
            startActivity(intent);
            dismiss();
        }
}
