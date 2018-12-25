package com.kaicomsol.kpos.dialogs;

import android.content.Context;
import android.content.Intent;

import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.activity.LoginActivity;
import com.kaicomsol.kpos.activity.SalesHistoryActivity;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

public class CustomAlertDialog {

    public static void showSuccess(Context context,String msg){
        new PromptDialog(context)
                .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                .setAnimationEnable(true)
                .setTitleText(context.getString(R.string.success))
                .setContentText(msg)
                .setPositiveListener(context.getString(R.string.ok), new PromptDialog.OnPositiveListener() {
                    @Override
                    public void onClick(PromptDialog dialog) {
                        dialog.dismiss();
                    }
                }).show();
    }

    public static void showWarning(Context context,String msg){
        new PromptDialog(context)
                .setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
                .setAnimationEnable(true)
                .setTitleText(context.getString(R.string.warning))
                .setContentText(msg)
                .setPositiveListener(context.getString(R.string.ok), new PromptDialog.OnPositiveListener() {
                    @Override
                    public void onClick(PromptDialog dialog) {
                        dialog.dismiss();
                    }
                }).show();
    }

    public static void showInfo(Context context,String msg){
        new PromptDialog(context)
                .setDialogType(PromptDialog.DIALOG_TYPE_INFO)
                .setAnimationEnable(true)
                .setTitleText(context.getString(R.string.info))
                .setContentText(msg)
                .setPositiveListener(context.getString(R.string.ok), new PromptDialog.OnPositiveListener() {
                    @Override
                    public void onClick(PromptDialog dialog) {
                        dialog.dismiss();
                    }
                }).show();
    }

    public static void showHelp(Context context,String msg){
        new PromptDialog(context)
                .setDialogType(PromptDialog.DIALOG_TYPE_HELP)
                .setAnimationEnable(true)
                .setTitleText(context.getString(R.string.help))
                .setContentText(msg)
                .setPositiveListener(context.getString(R.string.ok), new PromptDialog.OnPositiveListener() {
                    @Override
                    public void onClick(PromptDialog dialog) {
                        dialog.dismiss();
                    }
                }).show();
    }

    public static void showError(Context context,String msg){
        new PromptDialog(context)
                .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                .setAnimationEnable(true)
                .setTitleText(context.getString(R.string.err))
                .setContentText(msg)
                .setPositiveListener(context.getString(R.string.ok), new PromptDialog.OnPositiveListener() {
                    @Override
                    public void onClick(PromptDialog dialog) {
                        dialog.dismiss();
                    }
                }).show();
    }

}
