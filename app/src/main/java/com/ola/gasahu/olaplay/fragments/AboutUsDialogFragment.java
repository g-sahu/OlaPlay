package com.ola.gasahu.olaplay.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.ola.gasahu.olaplay.BuildConfig;
import com.ola.gasahu.olaplay.R;
import com.ola.gasahu.olaplay.utilities.MediaPlayerConstants;


public class AboutUsDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String versionName = MediaPlayerConstants.VERSION + " " + BuildConfig.VERSION_NAME;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_about_us, null);
        TextView versionNumber = (TextView) dialogView.findViewById(R.id.versionNumber);
        versionNumber.setText(versionName);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);
        return builder.create();
    }
}
