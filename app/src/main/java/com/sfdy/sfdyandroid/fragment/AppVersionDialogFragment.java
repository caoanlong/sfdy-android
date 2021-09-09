package com.sfdy.sfdyandroid.fragment;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class AppVersionDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("确定吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    dismiss();
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    dismiss();
                });
        return builder.create();
    }
}
