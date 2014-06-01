package ru.badr.vkfeed.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * User: Histler
 * Date: 29.05.14
 */
public class DialogUtils {
    public static void showAlert(Context context, String title, String alert,DialogInterface.OnClickListener onOk){
        showAlert(context, title, alert,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }, onOk);
    }

    public static void showAlert(Context context, String title, String alert,DialogInterface.OnClickListener onCancel,DialogInterface.OnClickListener onOk){
        showAlert(context, title, alert,context.getString(android.R.string.cancel), onCancel,context.getString(android.R.string.ok), onOk);
    }

    public static void showAlert(Context context, String title, String alert,String cancelMsg,DialogInterface.OnClickListener onCancel,String okMsg, DialogInterface.OnClickListener onOk)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setCancelable(false);
        builder.setMessage(alert);
        builder.setNegativeButton(cancelMsg,onCancel!=null?onCancel:new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(okMsg,onOk!=null?onOk:new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
