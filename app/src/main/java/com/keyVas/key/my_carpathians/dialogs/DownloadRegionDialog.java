package com.keyVas.key.my_carpathians.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.keyVas.key.my_carpathians.R;

public class DownloadRegionDialog extends DialogFragment {

    public static final String TAG = "DownloadRegionDialog";

    private static final String PERIMETER_VALUE = "perimeter_value";

    private DownloadRegionListener listener;

    public interface DownloadRegionListener {

        void onDownloadRegion(String name, double value);
    }

    public static DownloadRegionDialog newInstance(double perimeter) {
        Bundle args = new Bundle();
        args.putDouble(PERIMETER_VALUE, perimeter);
        DownloadRegionDialog fragment = new DownloadRegionDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (DownloadRegionListener) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        double perimeterValue = getArguments().getDouble(PERIMETER_VALUE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final EditText regionNameEdit = new EditText(getContext());
        regionNameEdit.setHint(getString(R.string.set_region_name_hint));
        builder.setTitle(getString(R.string.dialog_title))
                .setView(regionNameEdit)
                .setMessage(getString(R.string.dialog_message))
                .setPositiveButton(getString(R.string.dialog_positive_button), (dialog, which)
                        -> onDownloadRegion(regionNameEdit.getText().toString(), perimeterValue))
                .setNegativeButton(getString(R.string.dialog_negative_button), (dialog, which) -> dialog.dismiss());
        return builder.create();
    }

    private void onDownloadRegion(String name, double perimeterValue) {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getActivity(), getString(R.string.dialog_toast), Toast.LENGTH_SHORT).show();
        } else {
            listener.onDownloadRegion(name, perimeterValue);
            dismiss();
        }
    }
}
