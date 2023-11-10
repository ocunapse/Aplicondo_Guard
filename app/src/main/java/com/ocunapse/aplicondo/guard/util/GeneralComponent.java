package com.ocunapse.aplicondo.guard.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.ocunapse.aplicondo.guard.R;

public class GeneralComponent {

    public static void AlertBox(Context ctx, String msg){
        AlertBox(ctx,msg,null);
    }

    public static void AlertBox(Context ctx, int msg){
        AlertBox(ctx,msg,null);
    }

    public static void AlertBox(Context ctx, int msg,DialogInterface.OnClickListener ocl){
        String message = ctx.getString(msg);
        AlertBox(ctx,message,ocl);
    }

    public static void AlertBox(Context ctx, String msg, DialogInterface.OnClickListener ocl){
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(ctx);
        dlgAlert.setMessage(msg);
        dlgAlert.setTitle(ctx.getString(R.string.general_title));
        dlgAlert.setPositiveButton("OK", ocl);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    public static void ProgressBox(){

    }
}
