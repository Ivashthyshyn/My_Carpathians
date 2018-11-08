package com.keyVas.key.my_carpathians.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;

import com.keyVas.key.my_carpathians.R;

import java.util.Objects;

public class DownloadRegionDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final EditText regionNameEdit = new EditText(getContext());
        regionNameEdit.setHint(getString(R.string.set_region_name_hint));
        builder.setTitle(getString(R.string.dialog_title))
                .setView(regionNameEdit)
                .setMessage(getString(R.string.dialog_message))
                .setPositiveButton(getString(R.string.dialog_positive_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String regionName = regionNameEdit.getText().toString();
                                if (regionName.length() == 0) {
                                    regionNameEdit.setError("required");
                                    Toast.makeText(getActivity(), getString(R.string.dialog_toast), Toast.LENGTH_SHORT).show();
                                } else {
                                    // Begin download process
                                 //   downloadOfflineRegion(regionName, perimeterValue);
                                    dialog.dismiss();
                                }
                            }
                        })
                .setNegativeButton(getString(R.string.dialog_negative_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });


        return builder.create();
    }
}
